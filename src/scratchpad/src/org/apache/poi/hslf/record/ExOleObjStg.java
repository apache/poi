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

import java.io.*;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.Hashtable;

import org.apache.poi.util.LittleEndian;

/**
 * Storage for embedded OLE objects.
 *
 * @author Daniel Noll
 */
public class ExOleObjStg extends RecordAtom implements PositionDependentRecord, PersistRecord {

    private int _persistId; // Found from PersistPtrHolder

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs a new empty storage container.
     */
    public ExOleObjStg() {
        _header = new byte[8];
        _data = new byte[0];

        LittleEndian.putShort(_header, 0, (short)0x10);
        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected ExOleObjStg(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);
    }

    /**
     * Gets the uncompressed length of the data.
     *
     * @return the uncompressed length of the data.
     */
    public int getDataLength() {
        return LittleEndian.getInt(_data, 0);
    }

    /**
     * Opens an input stream which will decompress the data on the fly.
     *
     * @return the data input stream.
     */
    public InputStream getData() {
        InputStream compressedStream = new ByteArrayInputStream(_data, 4, _data.length);
        return new InflaterInputStream(compressedStream);
    }

    public byte[] getRawData() {
        return _data;
    }

    /**
     * Sets the embedded data.
     *
     * @param data the embedded data.
     */
     public void setData(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //first four bytes is the length of the raw data
        byte[] b = new byte[4];
        LittleEndian.putInt(b, data.length);
        out.write(b);

        DeflaterOutputStream def = new DeflaterOutputStream(out);
        def.write(data, 0, data.length);
        def.finish();
        _data = out.toByteArray();
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Gets the record type.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.ExOleObjStg.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     *
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    /**
     * Fetch our sheet ID, as found from a PersistPtrHolder.
     * Should match the RefId of our matching SlidePersistAtom
     */
    public int getPersistId() {
        return _persistId;
    }

    /**
     * Set our sheet ID, as found from a PersistPtrHolder
     */
    public void setPersistId(int id) {
        _persistId = id;
    }

    /** Our location on the disk, as of the last write out */
    protected int myLastOnDiskOffset;

    /** Fetch our location on the disk, as of the last write out */
    public int getLastOnDiskOffset() { return myLastOnDiskOffset; }

    /**
     * Update the Record's idea of where on disk it lives, after a write out.
     * Use with care...
     */
    public void setLastOnDiskOffset(int offset) {
        myLastOnDiskOffset = offset;
    }

    public void updateOtherRecordReferences(Hashtable oldToNewReferencesLookup) {
        return;
    }

}
