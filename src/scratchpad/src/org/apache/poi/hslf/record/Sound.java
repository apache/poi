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

import org.apache.poi.util.POILogger;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A container holding information about a sound. It contains:
 * <p>
 * <li>1. CString (4026), Instance 0: Name of sound (e.g. "crash")
 * <li>2. CString (4026), Instance 1: Type of sound (e.g. ".wav")
 * <li>3. CString (4026), Instance 2: Reference id of sound in sound collection
 * <li>4. CString (4026), Instance 3, optional: Built-in id of sound, for sounds we ship. This is the id that?s in the reg file.
 * <li>5. SoundData (2023), optional
 * </p>
 *
 * @author Yegor Kozlov
 */
public final class Sound extends RecordContainer {
    /**
     * Record header data.
     */
    private byte[] _header;

    // Links to our more interesting children
    private CString _name;
    private CString _type;
    private SoundData _data;


    /**
     * Set things up, and find our more interesting children
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected Sound(byte[] source, int start, int len) {
        // Grab the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Find our children
        _children = Record.findChildRecords(source,start+8,len-8);
        findInterestingChildren();
    }

    private void findInterestingChildren() {
        // First child should be the ExHyperlinkAtom
        if(_children[0] instanceof CString) {
            _name = (CString)_children[0];
        } else {
            logger.log(POILogger.ERROR, "First child record wasn't a CString, was of type " + _children[0].getRecordType());
        }

        // Second child should be the ExOleObjAtom
        if (_children[1] instanceof CString) {
            _type = (CString)_children[1];
        } else {
            logger.log(POILogger.ERROR, "Second child record wasn't a CString, was of type " + _children[1].getRecordType());
        }

        for (int i = 2; i < _children.length; i++) {
            if(_children[i] instanceof SoundData){
                _data = (SoundData)_children[i];
                break;
            }
        }

    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.Sound.typeID;
    }

    /**
     * Have the contents printer out into an OutputStream, used when
     * writing a file back out to disk.
     *
     * @param out the output stream.
     * @throws java.io.IOException if there was an error writing to the stream.
     */
    public void writeOut(OutputStream out) throws IOException {
        writeOut(_header[0],_header[1],getRecordType(),_children,out);
    }

    /**
     * Name of the sound (e.g. "crash")
     *
     * @return name of the sound
     */
    public String getSoundName(){
        return _name.getText();
    }

    /**
     * Type of the sound (e.g. ".wav")
     *
     * @return type of the sound
     */
    public String getSoundType(){
        return _type.getText();
    }

    /**
     * The sound data
     *
     * @return the sound data.
     */
    public byte[] getSoundData(){
        return _data == null ? null : _data.getData();
    }
}
