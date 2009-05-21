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
import java.util.Date;

import org.apache.poi.hslf.util.SystemTimeUtils;
import org.apache.poi.util.LittleEndian;

/**
 * An atomic record containing information about a comment.
 *
 * @author Daniel Noll
 */

public final class Comment2000Atom extends RecordAtom
{
    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs a brand new comment atom record.
     */
    protected Comment2000Atom() {
        _header = new byte[8];
        _data = new byte[28];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);

        // It is fine for the other values to be zero
    }

    /**
     * Constructs the comment atom record from its source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected Comment2000Atom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);
    }

    /**
     * Gets the comment number (note - each user normally has their own count).
     * @return the comment number.
     */
    public int getNumber() {
        return LittleEndian.getInt(_data,0);
    }

    /**
     * Sets the comment number (note - each user normally has their own count).
     * @param number the comment number.
     */
    public void setNumber(int number) {
        LittleEndian.putInt(_data,0,number);
    }

    /**
     * Gets the date the comment was made.
     * @return the comment date.
     */
    public Date getDate() {
    	return SystemTimeUtils.getDate(_data,4);
    }

    /**
     * Sets the date the comment was made.
     * @param date the comment date.
     */
    public void setDate(Date date) {
    	SystemTimeUtils.storeDate(date, _data, 4);
    }

    /**
     * Gets the X offset of the comment on the page.
     * @return the X offset.
     */
    public int getXOffset() {
        return LittleEndian.getInt(_data,20);
    }

    /**
     * Sets the X offset of the comment on the page.
     * @param xOffset the X offset.
     */
    public void setXOffset(int xOffset) {
        LittleEndian.putInt(_data,20,xOffset);
    }

    /**
     * Gets the Y offset of the comment on the page.
     * @return the Y offset.
     */
    public int getYOffset() {
        return LittleEndian.getInt(_data,24);
    }

    /**
     * Sets the Y offset of the comment on the page.
     * @param yOffset the Y offset.
     */
    public void setYOffset(int yOffset) {
        LittleEndian.putInt(_data,24,yOffset);
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return RecordTypes.Comment2000Atom.typeID; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }
}
