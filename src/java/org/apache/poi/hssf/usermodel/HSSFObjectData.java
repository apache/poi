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

package org.apache.poi.hssf.usermodel;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.util.HexDump;

/**
 * Represents binary object (i.e. OLE) data stored in the file.  Eg. A GIF, JPEG etc...
 * <p/>
 * Right now, 13, july, 2012 can not be created from scratch.
 */
public final class HSSFObjectData extends HSSFPicture {

    /**
     * Reference to the filesystem root, required for retrieving the object data.
     */
    private final DirectoryEntry root;

    public HSSFObjectData(EscherContainerRecord spContainer, ObjRecord objRecord, DirectoryEntry root) {
        super(spContainer, objRecord);
        this.root = root;
    }

    /**
     * Returns the OLE2 Class Name of the object
     */
    public String getOLE2ClassName() {
        return findObjectRecord().getOLEClassName();
    }

    /**
     * Gets the object data. Only call for ones that have
     * data though. See {@link #hasDirectoryEntry()}
     *
     * @return the object data as an OLE2 directory.
     * @throws IOException if there was an error reading the data.
     */
    public DirectoryEntry getDirectory() throws IOException {
        EmbeddedObjectRefSubRecord subRecord = findObjectRecord();

        int streamId = subRecord.getStreamId().intValue();
        String streamName = "MBD" + HexDump.toHex(streamId);

        Entry entry = root.getEntry(streamName);
        if (entry instanceof DirectoryEntry) {
            return (DirectoryEntry) entry;
        }

        throw new IOException("Stream " + streamName + " was not an OLE2 directory");
    }

    /**
     * Returns the data portion, for an ObjectData that doesn't have
     * an associated POIFS Directory Entry.
     */
    public byte[] getObjectData() {
        return findObjectRecord().getObjectData();
    }

    /**
     * Does this ObjectData have an associated POIFS Directory Entry?
     * (Not all do, those that don't have a data portion.)
     */
    public boolean hasDirectoryEntry() {
        EmbeddedObjectRefSubRecord subRecord = findObjectRecord();

        // 'stream id' field tells you
        Integer streamId = subRecord.getStreamId();
        return streamId != null && streamId.intValue() != 0;
    }

    /**
     * Finds the EmbeddedObjectRefSubRecord,
     * or throws an Exception if there wasn't one.
     */
    protected EmbeddedObjectRefSubRecord findObjectRecord() {
        Iterator<SubRecord> subRecordIter = getObjRecord().getSubRecords().iterator();

        while (subRecordIter.hasNext()) {
            Object subRecord = subRecordIter.next();
            if (subRecord instanceof EmbeddedObjectRefSubRecord) {
                return (EmbeddedObjectRefSubRecord) subRecord;
            }
        }

        throw new IllegalStateException("Object data does not contain a reference to an embedded object OLE2 directory");
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        throw new IllegalStateException("HSSFObjectData cannot be created from scratch");
    }

    @Override
    protected ObjRecord createObjRecord() {
        throw new IllegalStateException("HSSFObjectData cannot be created from scratch");
    }

    @Override
    protected void afterRemove(HSSFPatriarch patriarch) {
        throw new IllegalStateException("HSSFObjectData cannot be created from scratch");
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        EscherAggregate agg = patriarch._getBoundAggregate();
        agg.associateShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
        EscherBSERecord bse = patriarch.getSheet().getWorkbook().getWorkbook().getBSERecord(getPictureIndex());
        bse.setRef(bse.getRef() + 1);
    }

    @Override
    protected HSSFShape cloneShape() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        byte[] inSp = getEscherContainer().serialize();
        spContainer.fillFields(inSp, 0, new DefaultEscherRecordFactory());
        ObjRecord obj = (ObjRecord) getObjRecord().cloneViaReserialise();
        return new HSSFObjectData(spContainer, obj, root);
    }
}
