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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hslf.model.textproperties.IndentProp;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * Specifies the Indent Level for the text
 */
public final class MasterTextPropAtom extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    // indent details
    private List<IndentProp> indents;

    /**
     * Constructs a new empty master text prop atom.
     */
    public MasterTextPropAtom() {
        _header = new byte[8];
        _data = new byte[0];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);
        
        indents = new ArrayList<>();
    }

    /**
     * Constructs the ruler atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected MasterTextPropAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = IOUtils.safelyAllocate(len-8, MAX_RECORD_LENGTH);
        System.arraycopy(source,start+8,_data,0,len-8);

        try {
            read();
        } catch (Exception e){
            logger.log(POILogger.ERROR, "Failed to parse MasterTextPropAtom", e);
        }
    }

    /**
     * Gets the record type.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.MasterTextPropAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        write();
        out.write(_header);
        out.write(_data);
    }
    
    /**
     * Write the internal variables to the record bytes
     */
    private void write() {
        int pos = 0;
        _data = IOUtils.safelyAllocate(indents.size()*6, MAX_RECORD_LENGTH);
        for (IndentProp prop : indents) {
            LittleEndian.putInt(_data, pos, prop.getCharactersCovered());
            LittleEndian.putShort(_data, pos+4, (short)prop.getIndentLevel());
            pos += 6;
        }
    }

    /**
     * Read the record bytes and initialize the internal variables
     */
    private void read() {
        int pos = 0;
        indents = new ArrayList<>(_data.length / 6);
        
        while (pos <= _data.length - 6) {
            int count = LittleEndian.getInt(_data, pos);
            short indent = LittleEndian.getShort(_data, pos+4);
            indents.add(new IndentProp(count, indent));
            pos += 6;
        }
    }
    
    /**
     * Returns the indent that applies at the given text offset
     */
    public int getIndentAt(int offset) {
        int charsUntil = 0;
        for (IndentProp prop : indents) {
            charsUntil += prop.getCharactersCovered();
            if (offset < charsUntil) {
                return prop.getIndentLevel();
            }
        }
        return -1;
    }
    
    public List<IndentProp> getIndents() {
        return Collections.unmodifiableList(indents);
    }
}
