/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * General holder for PersistPtrFullBlock and PersistPtrIncrementalBlock
 *  records. We need to handle them specially, since we have to go around
 *  updating UserEditAtoms if they shuffle about on disk
 * These hold references to where slides "live". If the position of a slide
 *  moves, then we have update all of these. If we come up with a new version
 *  of a slide, then we have to add one of these to the end of the chain
 *  (via CurrentUserAtom and UserEditAtom) pointing to the new slide location
 */

public final class PersistPtrHolder extends PositionDependentRecordAtom {

    private final byte[] _header;
    private byte[] _ptrData; // Will need to update this once we allow updates to _slideLocations
    private final long _type;

    /**
     * Holds the lookup for slides to their position on disk.
     * You always need to check the most recent PersistPtrHolder
     *  that knows about a given slide to find the right location
     */
    private final Map<Integer,Integer> _slideLocations;

    private static final BitField persistIdFld = BitFieldFactory.getInstance(0X000FFFFF);
    private static final BitField cntPersistFld  = BitFieldFactory.getInstance(0XFFF00000);

    /**
     * Return the value we were given at creation, be it 6001 or 6002
     */
    @Override
    public long getRecordType() { return _type; }

    /**
     * Get the list of slides that this PersistPtrHolder knows about.
     * (They will be the keys in the map for looking up the positions
     *  of these slides)
     */
    public int[] getKnownSlideIDs() {
        int[] ids = new int[_slideLocations.size()];
        int i = 0;
        for (Integer slideId : _slideLocations.keySet()) {
            ids[i++] = slideId;
        }
        return ids;
    }

    /**
     * Get the lookup from slide numbers to byte offsets, for the slides
     *  known about by this PersistPtrHolder.
     */
    public Map<Integer,Integer> getSlideLocationsLookup() {
        return Collections.unmodifiableMap(_slideLocations);
    }

    /**
     * Create a new holder for a PersistPtr record
     */
    protected PersistPtrHolder(byte[] source, int start, int len) {
        // Sanity Checking - including whole header, so treat
        //  length as based of 0, not 8 (including header size based)
        if(len < 8) { len = 8; }

        // Treat as an atom, grab and hold everything
        _header = Arrays.copyOfRange(source, start, start+8);
        _type = LittleEndian.getUShort(_header,2);

        // Try to make sense of the data part:
        // Data part is made up of a number of these sets:
        //   32 bit info value
        //      12 bits count of # of entries
        //      base number for these entries
        //   count * 32 bit offsets
        // Repeat as many times as you have data
        _slideLocations = new HashMap<>();
        _ptrData = IOUtils.safelyClone(source, start+8, len-8, RecordAtom.getMaxRecordLength());

        int pos = 0;
        while(pos < _ptrData.length) {
            // Grab the info field
            int info = LittleEndian.getInt(_ptrData,pos);

            // First 20 bits = offset number
            // Remaining 12 bits = offset count
            int offset_no = persistIdFld.getValue(info);
            int offset_count = cntPersistFld.getValue(info);

            // Wind on by the 4 byte info header
            pos += 4;

            // Grab the offsets for each of the sheets
            for(int i=0; i<offset_count; i++) {
                int sheet_no = offset_no + i;
                int sheet_offset = (int)LittleEndian.getUInt(_ptrData,pos);
                _slideLocations.put(sheet_no, sheet_offset);

                // Wind on by 4 bytes per sheet found
                pos += 4;
            }
        }
    }

    /**
     *  remove all slide references
     *
     *  Convenience method provided, for easier reviewing of invocations
     */
    public void clear() {
        _slideLocations.clear();
    }

    /**
     * Adds a new slide, notes or similar, to be looked up by this.
     */
    public void addSlideLookup(int slideID, int posOnDisk) {
        if (_slideLocations.containsKey(slideID)) {
            throw new CorruptPowerPointFileException("A record with persistId "+slideID+" already exists.");
        }

        _slideLocations.put(slideID, posOnDisk);
    }

    /**
     * At write-out time, update the references to the sheets to their
     *  new positions
     */
    @Override
    public void updateOtherRecordReferences(Map<Integer,Integer> oldToNewReferencesLookup) {
        // Loop over all the slides we know about
        // Find where they used to live, and where they now live
        for (Entry<Integer,Integer> me : _slideLocations.entrySet()) {
            Integer oldPos = me.getValue();
            Integer newPos = oldToNewReferencesLookup.get(oldPos);

            if (newPos == null) {
                Integer id = me.getKey();
                LOG.atWarn().log("Couldn't find the new location of the \"slide\" with id {} that used to " +
                        "be at {}. Not updating the position of it, you probably won't be able to find it any more " +
                        "(if you ever could!)", id, oldPos);
            } else {
                me.setValue(newPos);
            }
        }
    }

    private void normalizePersistDirectory() {
        // Building the info block
        // First 20 bits = offset number = slide ID (persistIdFld, i.e. first slide ID of a continuous group)
        // Remaining 12 bits = offset count = 1 (cntPersistFld, i.e. continuous entries in a group)
        //
        // the info block is then followed by the slide offset (32 bits)

        int[] infoBlocks = new int[_slideLocations.size()*2];
        int lastSlideId = -1;
        int lastPersistIdx = 0;
        int lastIdx = -1;
        int entryCnt = 0;
        int baseSlideId = -1;

        Iterable<Entry<Integer,Integer>> iter = _slideLocations.entrySet().stream()
            .sorted(Comparator.comparingInt(Entry::getKey))::iterator;
        for (Entry<Integer, Integer> me : iter) {
            int nextSlideId = me.getKey();
            if (lastSlideId + 1 < nextSlideId) {
                // start new PersistDirectoryEntry
                lastPersistIdx = ++lastIdx;
                entryCnt = 0;
                baseSlideId = nextSlideId;
            }

            int infoBlock = persistIdFld.setValue(0, baseSlideId);
            infoBlock = cntPersistFld.setValue(infoBlock, ++entryCnt);
            infoBlocks[lastPersistIdx] = infoBlock;
            // add the offset
            infoBlocks[++lastIdx] = me.getValue();

            lastSlideId = nextSlideId;
        }

        // Save the new ptr data
        _ptrData = new byte[(lastIdx+1)*LittleEndianConsts.INT_SIZE];
        for (int idx = 0; idx<=lastIdx; idx++) {
            LittleEndian.putInt(_ptrData, idx*LittleEndianConsts.INT_SIZE, infoBlocks[idx]);
        }

        // Update the atom header
        LittleEndian.putInt(_header, 4, _ptrData.length);
    }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        normalizePersistDirectory();
        out.write(_header);
        out.write(_ptrData);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "slideLocations", this::getSlideLocationsLookup
        );
    }
}
