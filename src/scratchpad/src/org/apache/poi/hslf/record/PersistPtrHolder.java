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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.BitField;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * General holder for PersistPtrFullBlock and PersistPtrIncrementalBlock
 *  records. We need to handle them specially, since we have to go around
 *  updating UserEditAtoms if they shuffle about on disk
 * These hold references to where slides "live". If the position of a slide
 *  moves, then we have update all of these. If we come up with a new version
 *  of a slide, then we have to add one of these to the end of the chain
 *  (via CurrentUserAtom and UserEditAtom) pointing to the new slide location
 *
 * @author Nick Burch
 */

public final class PersistPtrHolder extends PositionDependentRecordAtom
{

	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 100_000;

	private final byte[] _header;
	private byte[] _ptrData; // Will need to update this once we allow updates to _slideLocations
	private long _type;

	/**
	 * Holds the lookup for slides to their position on disk.
	 * You always need to check the most recent PersistPtrHolder
	 *  that knows about a given slide to find the right location
	 */
	private Map<Integer,Integer> _slideLocations;

	private static final BitField persistIdFld = new BitField(0X000FFFFF);
	private static final BitField cntPersistFld  = new BitField(0XFFF00000);
	
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
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);
		_type = LittleEndian.getUShort(_header,2);

		// Try to make sense of the data part:
		// Data part is made up of a number of these sets:
		//   32 bit info value
		//		12 bits count of # of entries
		//      base number for these entries
		//   count * 32 bit offsets
		// Repeat as many times as you have data
		_slideLocations = new HashMap<>();
		_ptrData = IOUtils.safelyAllocate(len-8, MAX_RECORD_LENGTH);
		System.arraycopy(source,start+8,_ptrData,0,_ptrData.length);

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
                logger.log(POILogger.WARN, "Couldn't find the new location of the \"slide\" with id " + id + " that used to be at " + oldPos);
                logger.log(POILogger.WARN, "Not updating the position of it, you probably won't be able to find it any more (if you ever could!)");
            } else {
                me.setValue(newPos);
            }
	    }
	}

	private void normalizePersistDirectory() {
        TreeMap<Integer,Integer> orderedSlideLocations = new TreeMap<>(_slideLocations);
        
        @SuppressWarnings("resource")
        BufAccessBAOS bos = new BufAccessBAOS(); // NOSONAR
        byte intbuf[] = new byte[4];
        int lastPersistEntry = -1;
        int lastSlideId = -1;
        for (Entry<Integer,Integer> me : orderedSlideLocations.entrySet()) {
            int nextSlideId = me.getKey();
            int offset = me.getValue();
            try {
                // Building the info block
                // First 20 bits = offset number = slide ID (persistIdFld, i.e. first slide ID of a continuous group)
                // Remaining 12 bits = offset count = 1 (cntPersistFld, i.e. continuous entries in a group)
                
                if (lastSlideId+1 == nextSlideId) {
                    // use existing PersistDirectoryEntry, need to increase entry count
                    assert(lastPersistEntry != -1);
                    int infoBlock = LittleEndian.getInt(bos.getBuf(), lastPersistEntry);
                    int entryCnt = cntPersistFld.getValue(infoBlock);
                    infoBlock = cntPersistFld.setValue(infoBlock, entryCnt+1);
                    LittleEndian.putInt(bos.getBuf(), lastPersistEntry, infoBlock);
                } else {
                    // start new PersistDirectoryEntry
                    lastPersistEntry = bos.size();
                    int infoBlock = persistIdFld.setValue(0, nextSlideId);
                    infoBlock = cntPersistFld.setValue(infoBlock, 1);
                    LittleEndian.putInt(intbuf, 0, infoBlock);
                    bos.write(intbuf);
                }
                // Add to the ptrData offset lookup hash
                LittleEndian.putInt(intbuf, 0, offset);
                bos.write(intbuf);
                lastSlideId = nextSlideId;
            } catch (IOException e) {
                // ByteArrayOutputStream is very unlikely throwing a IO exception (maybe because of OOM ...)
                throw new HSLFException(e);
            }
        }
        
        // Save the new ptr data
        _ptrData = bos.toByteArray();

        // Update the atom header
        LittleEndian.putInt(_header,4,bos.size());
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
	
    private static class BufAccessBAOS extends ByteArrayOutputStream {
        public byte[] getBuf() {
            return buf;
        }
    }
}
