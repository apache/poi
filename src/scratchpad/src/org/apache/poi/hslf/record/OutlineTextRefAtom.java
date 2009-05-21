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

import org.apache.poi.util.LittleEndian;

import java.io.OutputStream;
import java.io.IOException;

/**
 * OEPlaceholderAtom (3998).
 * <br>
 * What MSDN says about  <code>OutlineTextRefAtom</code>:
 * <p>
 * Appears in a slide to indicate a text that is already contained in the document,
 * in a SlideListWithText containter. Sometimes slide texts are not contained
 * within the slide container to be able to delay loading a slide and still display
 * the title and body text in outline view.
 * </p>
 *
 * @author Yegor Kozlov
 */

public final class OutlineTextRefAtom extends RecordAtom {
    /**
     * record header
     */
    private byte[] _header;

    /**
     * the text's index within the SlideListWithText (0 for title, 1..n for the nth body)
     */
    private int _index;

    /**
     * Build an instance of <code>OutlineTextRefAtom</code> from on-disk data
     */
    protected OutlineTextRefAtom(byte[] source, int start, int len) {
        // Get the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Grab the record data
        _index = LittleEndian.getInt(source, start+8);
    }

    /**
     * Create a new instance of <code>FontEntityAtom</code>
     */
    protected OutlineTextRefAtom() {
        _index = 0;

        _header = new byte[8];
        LittleEndian.putUShort(_header, 0, 0);
        LittleEndian.putUShort(_header, 2, (int)getRecordType());
        LittleEndian.putInt(_header, 4, 4);
    }

    public long getRecordType() {
        return RecordTypes.OutlineTextRefAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written to disk
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);

        byte[] recdata = new byte[4];
        LittleEndian.putInt(recdata, 0, _index);
        out.write(recdata);
    }

    /**
     * Sets text's index within the SlideListWithText container
     * (0 for title, 1..n for the nth body).
     *
     * @param idx 0-based text's index
     */
    public void setTextIndex(int idx){
        _index = idx;
    }

    /**
     * Return text's index within the SlideListWithText container
     * (0 for title, 1..n for the nth body).
     *
     * @return idx text's index
     */
    public int getTextIndex(){
        return _index;
    }

}
