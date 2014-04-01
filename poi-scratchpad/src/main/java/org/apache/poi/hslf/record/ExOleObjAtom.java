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

/**
 * Atom storing information for an OLE object.
 *
 * <!--
 * offset   type    name         description
 *
 * 0        uint4   drawAspect   Stores whether the object can be completely seen
 *                               (value of 1), or if only the icon is visible (value of 4).
 *
 * 4        sint4    type        Specifies whether the object is embedded or linked.
 *                               0 - embedded
 *                               1 - linked
 *
 * 8        sint4    objID       Unique identifier for the OLE object
 *
 * 2        sint4    subType     This specifies the type of ole object.
 *                               0 - Default object
 *                               1 - Microsoft Clipart Gallery
 *                               2 - Microsoft Word table
 *                               3 - Microsoft Excel
 *                               4 - Microsoft Graph
 *                               5 - Microsoft Organization Chart
 *                               6 - Microsoft Equation Editor
 *                               7 - Microsoft Wordart object
 *                               8 - Sound
 *                               9 - Image
 *                               10 - PowerPoint presentation
 *                               11 - PowerPoint slide
 *                               12 - Microsoft Project
 *                               13 - Microsoft Note-It Ole
 *                               14 - Microsoft Excel chart
 *                               15 - Media Player object
 *
 * 16       sint4    objStgDataRef    Reference to persist object
 *
 * 20       bool1    isBlank          Set if the object's image is blank
 *           (note: KOffice has this as an int.)
 * -->
 *
 * @author Daniel Noll
 */
public class ExOleObjAtom extends RecordAtom {

    /**
     * The object) is displayed as an embedded object inside of a container,
     */
    public static final int DRAW_ASPECT_VISIBLE = 1;
    /**
     *   The object is displayed as a thumbnail image.
     */
    public static final int DRAW_ASPECT_THUMBNAIL = 2;
    /**
     *   The object is displayed as an icon.
     */
    public static final int DRAW_ASPECT_ICON = 4;
    /**
     *   The object is displayed on the screen as though it were printed to a printer.
     */
    public static final int DRAW_ASPECT_DOCPRINT = 8;

    /**
     * An embedded OLE object; the object is serialized and saved within the file.
     */
    public static final int TYPE_EMBEDDED = 0;
    /**
     * A linked OLE object; the object is saved outside of the file.
     */
    public static final int TYPE_LINKED = 1;
    /**
     * The OLE object is an ActiveX control.
     */
    public static final int TYPE_CONTROL = 2;

    public static final int SUBTYPE_DEFAULT = 0;
    public static final int SUBTYPE_CLIPART_GALLERY = 1;
    public static final int SUBTYPE_WORD_TABLE = 2;
    public static final int SUBTYPE_EXCEL = 3;
    public static final int SUBTYPE_GRAPH = 4;
    public static final int SUBTYPE_ORGANIZATION_CHART = 5;
    public static final int SUBTYPE_EQUATION = 6;
    public static final int SUBTYPE_WORDART = 7;
    public static final int SUBTYPE_SOUND = 8;
    public static final int SUBTYPE_IMAGE = 9;
    public static final int SUBTYPE_POWERPOINT_PRESENTATION = 10;
    public static final int SUBTYPE_POWERPOINT_SLIDE = 11;
    public static final int SUBTYPE_PROJECT = 12;
    public static final int SUBTYPE_NOTEIT = 13;
    public static final int SUBTYPE_EXCEL_CHART = 14;
    public static final int SUBTYPE_MEDIA_PLAYER = 15;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs a brand new link related atom record.
     */
    public ExOleObjAtom() {
        _header = new byte[8];
        _data = new byte[24];

        LittleEndian.putShort(_header, 0, (short)1); //MUST be 0x1
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
    protected ExOleObjAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);

        // Must be at least 24 bytes long
        if(_data.length < 24) {
        	throw new IllegalArgumentException("The length of the data for a ExOleObjAtom must be at least 24 bytes, but was only " + _data.length);
        }
    }

    /**
     * Gets whether the object can be completely seen, or if only the
     * icon is visible.
     *
     * @return the draw aspect, one of the {@code DRAW_ASPECT_*} constants.
     */
    public int getDrawAspect() {
        return LittleEndian.getInt(_data, 0);
    }

    /**
     * Sets whether the object can be completely seen, or if only the
     * icon is visible.
     *
     * @param aspect the draw aspect, one of the {@code DRAW_ASPECT_*} constants.
     */
     public void setDrawAspect(int aspect) {
        LittleEndian.putInt(_data, 0, aspect);
    }

    /**
     * Gets whether the object is embedded or linked.
     *
     * @return the type, one of the {@code TYPE_EMBEDDED_*} constants.
     */
    public int getType() {
        return LittleEndian.getInt(_data, 4);
    }

    /**
     * Sets whether the object is embedded or linked.
     *
     * @param type the type, one of the {@code TYPE_EMBEDDED_*} constants.
     */
    public void setType(int type) {
        LittleEndian.putInt(_data, 4, type);
    }

    /**
     * Gets the unique identifier for the OLE object.
     *
     * @return the object ID.
     */
    public int getObjID() {
        return LittleEndian.getInt(_data, 8);
    }

    /**
     * Sets the unique identifier for the OLE object.
     *
     * @param id the object ID.
     */
    public void setObjID(int id) {
        LittleEndian.putInt(_data, 8, id);
    }

    /**
     * Gets the type of OLE object.
     * 
     * @return the sub-type, one of the {@code SUBTYPE_*} constants.
     */
    public int getSubType() {
        return LittleEndian.getInt(_data, 12);
    }

    /**
     * Sets the type of OLE object.
     *
     * @param type the sub-type, one of the {@code SUBTYPE_*} constants.
     */
    public void setSubType(int type) {
        LittleEndian.putInt(_data, 12, type);
    }

    /**
     * Gets the reference to the persistent object
     *
     * @return the reference to the persistent object, corresponds with an
     *         {@code ExOleObjStg} storage container.
     */
    public int getObjStgDataRef() {
        return LittleEndian.getInt(_data, 16);
    }

    /**
     * Sets the reference to the persistent object
     *
     * @param ref the reference to the persistent object, corresponds with an
     *         {@code ExOleObjStg} storage container.
     */
    public void setObjStgDataRef(int ref) {
        LittleEndian.putInt(_data, 16, ref);
    }

    /**
     * Gets whether the object's image is blank.
     *
     * @return {@code true} if the object's image is blank.
     */
    public boolean getIsBlank() {
        // Even though this is a mere boolean, KOffice's code says it's an int.
        return LittleEndian.getInt(_data, 20) != 0;
    }
    
    /**
     * Gets misc options (the last four bytes in the atom).
     *
     * @return {@code true} if the object's image is blank.
     */
    public int getOptions() {
        // Even though this is a mere boolean, KOffice's code says it's an int.
        return LittleEndian.getInt(_data, 20);
    }

    /**
     * Sets misc options (the last four bytes in the atom).
     */
    public void setOptions(int opts) {
        // Even though this is a mere boolean, KOffice's code says it's an int.
        LittleEndian.putInt(_data, 20, opts);
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     */
    public long getRecordType() {
        return RecordTypes.ExOleObjAtom.typeID;
    }

    /**
     * Have the contents printer out into an OutputStream, used when
     * writing a file back out to disk
     * (Normally, atom classes will keep their bytes around, but
     * non atom classes will just request the bytes from their
     * children, then chuck on their header and return)
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append("ExOleObjAtom\n");
        buf.append("  drawAspect: " + getDrawAspect() + "\n");
        buf.append("  type: " + getType() + "\n");
        buf.append("  objID: " + getObjID() + "\n");
        buf.append("  subType: " + getSubType() + "\n");
        buf.append("  objStgDataRef: " + getObjStgDataRef() + "\n");
        buf.append("  options: " + getOptions() + "\n");
        return buf.toString();
    }
}
