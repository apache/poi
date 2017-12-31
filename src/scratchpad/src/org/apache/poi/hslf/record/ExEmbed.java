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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * This data represents an embedded object in the document.
 */
public class ExEmbed extends RecordContainer {

    /**
     * Record header data.
     */
    private final byte[] _header;

    // Links to our more interesting children
    private RecordAtom embedAtom;
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
    protected ExEmbed(final byte[] source, final int start, final int len) {
        // Grab the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Find our children
        _children = Record.findChildRecords(source,start+8,len-8);
        findInterestingChildren();
    }

    /**
     * Constructor for derived classes
     *
     * @param embedAtom the new embedAtom
     */
    protected ExEmbed(final RecordAtom embedAtom) {
        this();
        _children[0] = this.embedAtom = embedAtom;
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
        final CString cs1 = new CString();
        cs1.setOptions(0x1 << 4);
        final CString cs2 = new CString();
        cs2.setOptions(0x2 << 4);
        final CString cs3 = new CString();
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
                final CString cs = (CString)_children[i];
                final int opts = cs.getOptions() >> 4;
                switch(opts){
                    case 0x1: menuName = cs; break;
                    case 0x2: progId = cs; break;
                    case 0x3: clipboardName = cs; break;
                    default: break;
                }
            }
        }
    }

    /**
     * Gets the {@link ExEmbedAtom}.
     *
     * @return the {@link ExEmbedAtom}.
     */
    public ExEmbedAtom getExEmbedAtom() {
        return (ExEmbedAtom)embedAtom;
    }

    /**
     * Gets the {@link ExOleObjAtom}.
     *
     * @return the {@link ExOleObjAtom}.
     */
    public ExOleObjAtom getExOleObjAtom() {
        return oleObjAtom;
    }

    /**
     * Gets the name used for menus and the Links dialog box.
     *
     * @return the name used for menus and the Links dialog box.
     */
    public String getMenuName() {
        return menuName == null ? null : menuName.getText();
    }

    public void setMenuName(final String menuName) {
        this.menuName = safeCString(this.menuName, 0x1);
        this.menuName.setText(menuName);
    }

    /**
     * Gets the OLE Programmatic Identifier.
     * 
     * @return the OLE Programmatic Identifier.
     */
    public String getProgId() {
        return progId == null ? null : progId.getText();
    }

    public void setProgId(final String progId) {
        this.progId = safeCString(this.progId, 0x2);
        this.progId.setText(progId);
    }

    
    
    /**
     * Gets the name that appears in the paste special dialog.
     *
     * @return the name that appears in the paste special dialog.
     */
    public String getClipboardName() {
        return clipboardName == null ? null : clipboardName.getText();
    }

    public void setClipboardName(final String clipboardName) {
        this.clipboardName = safeCString(this.clipboardName, 0x3);
        this.clipboardName.setText(clipboardName);
    }
    
    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     *
     * @return the record type.
     */
    @Override
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
    @Override
    public void writeOut(final OutputStream out) throws IOException {
        writeOut(_header[0],_header[1],getRecordType(),_children,out);
    }
    
    private CString safeCString(CString oldStr, int optionsId) {
        CString newStr = oldStr;
        if (newStr == null) {
            newStr = new CString();
            newStr.setOptions(optionsId << 4);
        }

        boolean found = false;
        for (final Record r : _children) {
            // for simplicity just check for object identity
            if (r == newStr) {
                found = true;
                break;
            }
        }

        if (!found) {
            appendChildRecord(newStr);
        }
        
        return newStr;
    }
}
