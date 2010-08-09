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
package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.util.*;

/**
 * This structure specifies the properties of a list or drop-down list embedded object in a sheet.
 */
public class LbsDataSubRecord extends SubRecord {

    public static final int sid = 0x0013;

    /**
     * From [MS-XLS].pdf 2.5.147 FtLbsData:
     *
     * An unsigned integer that indirectly specifies whether
     * some of the data in this structure appear in a subsequent Continue record.
     * If _cbFContinued is 0x00, all of the fields in this structure except sid and _cbFContinued
     *  MUST NOT exist. If this entire structure is contained within the same record,
     * then _cbFContinued MUST be greater than or equal to the size, in bytes,
     * of this structure, not including the four bytes for the ft and _cbFContinued fields
     */
    private int _cbFContinued;

    /**
     * a formula that specifies the range of cell values that are the items in this list.
     */
    private int _unknownPreFormulaInt;
    private Ptg _linkPtg;
    private Byte _unknownPostFormulaByte;

    /**
     * An unsigned integer that specifies the number of items in the list.
     */
    private int _cLines;

    /**
     * An unsigned integer that specifies the one-based index of the first selected item in this list.
     * A value of 0x00 specifies there is no currently selected item.
     */
    private int _iSel;

    /**
     *  flags that tell what data follows
     */
    private int _flags;

    /**
     * An ObjId that specifies the edit box associated with this list.
     * A value of 0x00 specifies that there is no edit box associated with this list.
     */
    private int _idEdit;

    /**
     * An optional LbsDropData that specifies properties for this dropdown control.
     * This field MUST exist if and only if the containing Obj?s cmo.ot is equal to 0x14.
     */
    private LbsDropData _dropData;

    /**
     * An optional array of strings where each string specifies an item in the list.
     * The number of elements in this array, if it exists, MUST be {@link #_cLines}
     */
    private String[] _rgLines;

    /**
     * An optional array of booleans that specifies
     * which items in the list are part of a multiple selection
     */
    private boolean[] _bsels;

    /**
     * @param in the stream to read data from
     * @param cbFContinued the seconf short in the record header
     * @param cmoOt the containing Obj's {@link CommonObjectDataSubRecord#field_1_objectType}
     */
    public LbsDataSubRecord(LittleEndianInput in, int cbFContinued, int cmoOt) {
        _cbFContinued = cbFContinued;

        int encodedTokenLen = in.readUShort();
        if (encodedTokenLen > 0) {
            int formulaSize = in.readUShort();
            _unknownPreFormulaInt = in.readInt();

            Ptg[] ptgs = Ptg.readTokens(formulaSize, in);
            if (ptgs.length != 1) {
                throw new RecordFormatException("Read " + ptgs.length
                        + " tokens but expected exactly 1");
            }
            _linkPtg = ptgs[0];
            switch (encodedTokenLen - formulaSize - 6) {
                case 1:
                    _unknownPostFormulaByte = in.readByte();
                    break;
                case 0:
                    _unknownPostFormulaByte = null;
                    break;
                default:
                    throw new RecordFormatException("Unexpected leftover bytes");
            }
        }

        _cLines = in.readUShort();
        _iSel = in.readUShort();
        _flags = in.readUShort();
        _idEdit = in.readUShort();

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This field MUST exist if and only if the containing Obj?s cmo.ot is equal to 0x14.
        if(cmoOt == 0x14) {
            _dropData = new LbsDropData(in);
        }

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This array MUST exist if and only if the fValidPlex flag (0x2) is set
        if((_flags & 0x2) != 0) {
            _rgLines = new String[_cLines];
            for(int i=0; i < _cLines; i++) {
                _rgLines[i] = StringUtil.readUnicodeString(in);
            }
        }

        // bits 5-6 in the _flags specify the type
        // of selection behavior this list control is expected to support

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This array MUST exist if and only if the wListType field is not equal to 0.
        if(((_flags >> 4) & 0x2) != 0) {
            _bsels = new boolean[_cLines];
            for(int i=0; i < _cLines; i++) {
                _bsels[i] = in.readByte() == 1;
            }
        }

    }

    LbsDataSubRecord(){

    }

    /**
     *
     * @return a new instance of LbsDataSubRecord to construct auto-filters
     * @see org.apache.poi.hssf.model.ComboboxShape#createObjRecord(org.apache.poi.hssf.usermodel.HSSFSimpleShape, int)
     */
    public static LbsDataSubRecord newAutoFilterInstance(){
        LbsDataSubRecord lbs = new LbsDataSubRecord();
        lbs._cbFContinued = 0x1FEE;  //autofilters seem to alway have this magic number
        lbs._iSel = 0x000;

        lbs._flags = 0x0301;
        lbs._dropData = new LbsDropData();
        lbs._dropData._wStyle = LbsDropData.STYLE_COMBO_SIMPLE_DROPDOWN;

        // the number of lines to be displayed in the dropdown
        lbs._dropData._cLine = 8;
        return lbs;
    }

    /**
     * @return true as LbsDataSubRecord is always the last sub-record
     */
    @Override
    public boolean isTerminating(){
        return true;
    }

    @Override
    protected int getDataSize() {
        int result = 2; // 2 initial shorts

        // optional link formula
        if (_linkPtg != null) {
            result += 2; // encoded Ptg size
            result += 4; // unknown int
            result += _linkPtg.getSize();
            if (_unknownPostFormulaByte != null) {
                result += 1;
            }
        }

        result += 4 * 2; // 4 shorts
        if(_dropData != null) {
            result += _dropData.getDataSize();
        }
        if(_rgLines != null) {
            for(String str : _rgLines){
                result += StringUtil.getEncodedSize(str);
            }
        }
        if(_bsels != null) {
            result += _bsels.length;
        }
        return result;
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(_cbFContinued);

        if (_linkPtg == null) {
            out.writeShort(0);
        } else {
            int formulaSize = _linkPtg.getSize();
            int linkSize = formulaSize + 6;
            if (_unknownPostFormulaByte != null) {
                linkSize++;
            }
            out.writeShort(linkSize);
            out.writeShort(formulaSize);
            out.writeInt(_unknownPreFormulaInt);
            _linkPtg.write(out);
            if (_unknownPostFormulaByte != null) {
                out.writeByte(_unknownPostFormulaByte.intValue());
            }
        }

        out.writeShort(_cLines);
        out.writeShort(_iSel);
        out.writeShort(_flags);
        out.writeShort(_idEdit);

        if(_dropData != null) {
            _dropData.serialize(out);
        }

        if(_rgLines != null) {
            for(String str : _rgLines){
                StringUtil.writeUnicodeString(out, str);
            }
        }

        if(_bsels != null) {
            for(boolean val : _bsels){
                out.writeByte(val ? 1 : 0);
            }
        }
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(256);

        sb.append("[ftLbsData]\n");
        sb.append("    .unknownShort1 =").append(HexDump.shortToHex(_cbFContinued)).append("\n");
        sb.append("    .formula        = ").append('\n');
        if(_linkPtg != null) sb.append(_linkPtg.toString()).append(_linkPtg.getRVAType()).append('\n');
        sb.append("    .nEntryCount   =").append(HexDump.shortToHex(_cLines)).append("\n");
        sb.append("    .selEntryIx    =").append(HexDump.shortToHex(_iSel)).append("\n");
        sb.append("    .style         =").append(HexDump.shortToHex(_flags)).append("\n");
        sb.append("    .unknownShort10=").append(HexDump.shortToHex(_idEdit)).append("\n");
        if(_dropData != null) sb.append('\n').append(_dropData.toString());
        sb.append("[/ftLbsData]\n");
        return sb.toString();
    }

    /**
     *
     * @return the formula that specifies the range of cell values that are the items in this list.
     */
    public Ptg getFormula(){
        return _linkPtg;
    }

    /**
     * @return the number of items in the list
     */
    public int getNumberOfItems(){
        return _cLines;
    }

    /**
     * This structure specifies properties of the dropdown list control
     */
    public static class LbsDropData {
        /**
         * Combo dropdown control
         */
        public static int STYLE_COMBO_DROPDOWN = 0;
        /**
         * Combo Edit dropdown control
         */
        public static int STYLE_COMBO_EDIT_DROPDOWN = 1;
        /**
         * Simple dropdown control (just the dropdown button)
         */
        public static int STYLE_COMBO_SIMPLE_DROPDOWN = 2;

        /**
         *  An unsigned integer that specifies the style of this dropdown. 
         */
        private int _wStyle;

        /**
         * An unsigned integer that specifies the number of lines to be displayed in the dropdown.
         */
        private int _cLine;

        /**
         * An unsigned integer that specifies the smallest width in pixels allowed for the dropdown window
         */
        private int _dxMin;

        /**
         * a string that specifies the current string value in the dropdown
         */
        private String _str;

        /**
         * Optional, undefined and MUST be ignored.
         * This field MUST exist if and only if the size of str in bytes is an odd number
         */
        private Byte _unused;

        public LbsDropData(){
            _str = "";
            _unused = 0;
        }

        public LbsDropData(LittleEndianInput in){
            _wStyle = in.readUShort();
            _cLine = in.readUShort();
            _dxMin = in.readUShort();
            _str = StringUtil.readUnicodeString(in);
            if(StringUtil.getEncodedSize(_str) % 2 != 0){
                _unused = in.readByte();
            }
        }

        /**
         *  Set the style of this dropdown.
         *
         * Possible values:
         *  <p>
         *  0  Combo dropdown control
         *  1  Combo Edit dropdown control
         *  2  Simple dropdown control (just the dropdown button)
         *
         */
        public void setStyle(int style){
            _wStyle = style;
        }

        /**
         * Set the number of lines to be displayed in the dropdown.
         */
        public void setNumLines(int num){
            _cLine = num;
        }

        public void serialize(LittleEndianOutput out) {
            out.writeShort(_wStyle);
            out.writeShort(_cLine);
            out.writeShort(_dxMin);
            StringUtil.writeUnicodeString(out, _str);
            if(_unused != null) out.writeByte(_unused);
        }

        public int getDataSize() {
            int size = 6;
            size += StringUtil.getEncodedSize(_str);
            size += _unused;
            return size;
        }

        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append("[LbsDropData]\n");
            sb.append("  ._wStyle:  ").append(_wStyle).append('\n');
            sb.append("  ._cLine:  ").append(_cLine).append('\n');
            sb.append("  ._dxMin:  ").append(_dxMin).append('\n');
            sb.append("  ._str:  ").append(_str).append('\n');
            if(_unused != null) sb.append("  ._unused:  ").append(_unused).append('\n');
            sb.append("[/LbsDropData]\n");

            return sb.toString();
        }
    }
}
