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

import java.io.OutputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * This data represents an embedded object in the document.
 *
 * @author Daniel Noll
 */
public class ExEmbed extends RecordContainer {

    /**
     * Record header data.
     */
    private byte[] _header;

    // Links to our more interesting children
    protected RecordAtom embedAtom;
    private ExOleObjAtom oleObjAtom;
    private CString menuName;
    private CString progId;
    private CString clipboardName;

    /**
     * Set things up, and find our more interesting children
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected ExEmbed(byte[] source, int start, int len) {
        // Grab the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Find our children
        _children = Record.findChildRecords(source,start+8,len-8);
        findInterestingChildren();
    }

    /**
     * Create a new ExEmbed, with blank fields
     */
    public ExEmbed() {
        _header = new byte[8];
        _children = new Record[5];

        // Setup our header block
        _header[0] = 0x0f; // We are a container record
        LittleEndian.putShort(_header, 2, (short)getRecordType());

        // Setup our child records
        CString cs1 = new CString();
        cs1.setOptions(0x1 << 4);
        CString cs2 = new CString();
        cs2.setOptions(0x2 << 4);
        CString cs3 = new CString();
        cs3.setOptions(0x3 << 4);
        _children[0] = new ExEmbedAtom();
        _children[1] = new ExOleObjAtom();
        _children[2] = cs1;
        _children[3] = cs2;
        _children[4] = cs3;
        findInterestingChildren();
    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper methods.
     */
    private void findInterestingChildren() {

        // First child should be the ExHyperlinkAtom
        if(_children[0] instanceof ExEmbedAtom) {
            embedAtom = (ExEmbedAtom)_children[0];
        } else {
            logger.log(POILogger.ERROR, "First child record wasn't a ExEmbedAtom, was of type " + _children[0].getRecordType());
        }

        // Second child should be the ExOleObjAtom
        if (_children[1] instanceof ExOleObjAtom) {
            oleObjAtom = (ExOleObjAtom)_children[1];
        } else {
            logger.log(POILogger.ERROR, "Second child record wasn't a ExOleObjAtom, was of type " + _children[1].getRecordType());
        }

        for (int i = 2; i < _children.length; i++) {
            if (_children[i] instanceof CString){
                CString cs = (CString)_children[i];
                int opts = cs.getOptions() >> 4;
                switch(opts){
                    case 0x1: menuName = cs; break;
                    case 0x2: progId = cs; break;
                    case 0x3: clipboardName = cs; break;
                }
            }
        }
    }

    /**
     * Gets the {@link ExEmbedAtom}.
     *
     * @return the {@link ExEmbedAtom}.
     */
    public ExEmbedAtom getExEmbedAtom()
    {
        return (ExEmbedAtom)embedAtom;
    }

    /**
     * Gets the {@link ExOleObjAtom}.
     *
     * @return the {@link ExOleObjAtom}.
     */
    public ExOleObjAtom getExOleObjAtom()
    {
        return oleObjAtom;
    }

    /**
     * Gets the name used for menus and the Links dialog box.
     *
     * @return the name used for menus and the Links dialog box.
     */
    public String getMenuName()
    {
        return menuName == null ? null : menuName.getText();
    }

    public void setMenuName(String s)
    {
        if(menuName != null) menuName.setText(s);
    }

    /**
     * Gets the OLE Programmatic Identifier.
     * 
     * @return the OLE Programmatic Identifier.
     */
    public String getProgId()
    {
        return progId == null ? null : progId.getText();
    }

    public void setProgId(String s)
    {
        if(progId != null) progId.setText(s);
    }
    /**
     * Gets the name that appears in the paste special dialog.
     *
     * @return the name that appears in the paste special dialog.
     */
    public String getClipboardName()
    {
        return clipboardName == null ? null : clipboardName.getText();
    }

    public void setClipboardName(String s)
    {
        if(clipboardName != null) clipboardName.setText(s);
    }
    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.ExEmbed.typeID;
    }

    /**
     * Have the contents printer out into an OutputStream, used when
     * writing a file back out to disk.
     *
     * @param out the output stream.
     * @throws IOException if there was an error writing to the stream.
     */
    public void writeOut(OutputStream out) throws IOException {
        writeOut(_header[0],_header[1],getRecordType(),_children,out);
    }
}
