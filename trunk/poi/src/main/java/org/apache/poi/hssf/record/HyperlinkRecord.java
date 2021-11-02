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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.hpsf.ClassIDPredefined.FILE_MONIKER;
import static org.apache.poi.hpsf.ClassIDPredefined.STD_MONIKER;
import static org.apache.poi.hpsf.ClassIDPredefined.URL_MONIKER;
import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;
import static org.apache.poi.util.HexDump.toHex;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * The <code>HyperlinkRecord</code> (0x01B8) wraps an HLINK-record
 *  from the Excel-97 format.
 * Supports only external links for now (eg http://)
 */
public final class HyperlinkRecord extends StandardRecord {
    public static final short sid = 0x01B8;
    private static final Logger LOG = LogManager.getLogger(HyperlinkRecord.class);

    /*
     * Link flags
     */
    static final int HLINK_URL    = 0x01;  // File link or URL.
    static final int HLINK_ABS    = 0x02;  // Absolute path.
    static final int HLINK_LABEL  = 0x14;  // Has label/description.
    /** Place in worksheet. If set, the {@link #_textMark} field will be present */
    static final int HLINK_PLACE  = 0x08;
    private static final int  HLINK_TARGET_FRAME  = 0x80;  // has 'target frame'
    private static final int  HLINK_UNC_PATH  = 0x100;  // has UNC path

    /** expected Tail of a URL link */
    private static final byte[] URL_TAIL  = HexRead.readFromString("79 58 81 F4  3B 1D 7F 48   AF 2C 82 5D  C4 85 27 63   00 00 00 00  A5 AB 00 00");
    /** expected Tail of a file link */
    private static final byte[] FILE_TAIL = HexRead.readFromString("FF FF AD DE  00 00 00 00   00 00 00 00  00 00 00 00   00 00 00 00  00 00 00 00");

    private static final int TAIL_SIZE = FILE_TAIL.length;

    /** cell range of this hyperlink */
    private CellRangeAddress _range;

    /** 16-byte GUID */
    private ClassID _guid;
    /** Some sort of options for file links. */
    private int _fileOpts;
    /** Link options. Can include any of HLINK_* flags. */
    private int _linkOpts;
    /** Test label */
    private String _label;

    private String _targetFrame;
    /** Moniker. Makes sense only for URL and file links */
    private ClassID _moniker;
    /** in 8:3 DOS format No Unicode string header,
     * always 8-bit characters, zero-terminated */
    private String _shortFilename;
    /** Link */
    private String _address;
    /**
     * Text describing a place in document.  In Excel UI, this is appended to the
     * address, (after a '#' delimiter).<br>
     * This field is optional.  If present, the {@link #HLINK_PLACE} must be set.
     */
    private String _textMark;

    private byte[] _uninterpretedTail;

    /**
     * Create a new hyperlink
     */
    public HyperlinkRecord() {}


    public HyperlinkRecord(HyperlinkRecord other) {
        super(other);
        _range = (other._range == null) ? null : other._range.copy();
        _guid = (other._guid == null) ? null : other._guid.copy();
        _fileOpts = other._fileOpts;
        _linkOpts = other._linkOpts;
        _label = other._label;
        _targetFrame = other._targetFrame;
        _moniker = (other._moniker == null) ? null : other._moniker.copy();
        _shortFilename = other._shortFilename;
        _address = other._address;
        _textMark = other._textMark;
        _uninterpretedTail = (other._uninterpretedTail == null) ? null : other._uninterpretedTail.clone();
    }


    /**
     * @return the 0-based column of the first cell that contains this hyperlink
     */
    public int getFirstColumn() {
        return _range.getFirstColumn();
    }

    /**
     * Set the first column (zero-based) of the range that contains this hyperlink
     *
     * @param firstCol the first column (zero-based)
     */
    public void setFirstColumn(int firstCol) {
        _range.setFirstColumn(firstCol);
    }

    /**
     * @return the 0-based column of the last cell that contains this hyperlink
     */
    public int getLastColumn() {
        return _range.getLastColumn();
    }

    /**
     * Set the last column (zero-based) of the range that contains this hyperlink
     *
     * @param lastCol the last column (zero-based)
     */
    public void setLastColumn(int lastCol) {
        _range.setLastColumn(lastCol);
    }

    /**
     * @return the 0-based row of the first cell that contains this hyperlink
     */
    public int getFirstRow() {
        return _range.getFirstRow();
    }

    /**
     * Set the first row (zero-based) of the range that contains this hyperlink
     *
     * @param firstRow the first row (zero-based)
     */
    public void setFirstRow(int firstRow) {
        _range.setFirstRow(firstRow);
    }

    /**
     * @return the 0-based row of the last cell that contains this hyperlink
     */
    public int getLastRow() {
        return _range.getLastRow();
    }

    /**
     * Set the last row (zero-based) of the range that contains this hyperlink
     *
     * @param lastRow the last row (zero-based)
     */
    public void setLastRow(int lastRow) {
        _range.setLastRow(lastRow);
    }

    /**
     * @return 16-byte guid identifier Seems to always equal {@link ClassIDPredefined#STD_MONIKER}
     */
    ClassID getGuid() {
        return _guid;
    }

    /**
     * @return 16-byte moniker
     */
    ClassID getMoniker()
    {
        return _moniker;
    }

    private static String cleanString(String s) {
        if (s == null) {
            return null;
        }
        int idx = s.indexOf('\u0000');
        if (idx < 0) {
            return s;
        }
        return s.substring(0, idx);
    }
    private static String appendNullTerm(String s) {
        if (s == null) {
            return null;
        }
        return s + '\u0000';
    }

    /**
     * Return text label for this hyperlink
     *
     * @return  text to display
     */
    public String getLabel() {
        return cleanString(_label);
    }

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
    public void setLabel(String label) {
        _label = appendNullTerm(label);
    }
    public String getTargetFrame() {
        return cleanString(_targetFrame);
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress() {
        if ((_linkOpts & HLINK_URL) != 0 && FILE_MONIKER.equals(_moniker)) {
            return cleanString(_address != null ? _address : _shortFilename);
        } else if((_linkOpts & HLINK_PLACE) != 0) {
            return cleanString(_textMark);
        } else {
            return cleanString(_address);
        }
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    public void setAddress(String address) {
        if ((_linkOpts & HLINK_URL) != 0 && FILE_MONIKER.equals(_moniker)) {
            _shortFilename = appendNullTerm(address);
        } else if((_linkOpts & HLINK_PLACE) != 0) {
            _textMark = appendNullTerm(address);
        } else {
            _address = appendNullTerm(address);
        }
    }

    public String getShortFilename() {
        return cleanString(_shortFilename);
    }

    public void setShortFilename(String shortFilename) {
        _shortFilename = appendNullTerm(shortFilename);
    }

    public String getTextMark() {
        return cleanString(_textMark);
    }
    public void setTextMark(String textMark) {
        _textMark = appendNullTerm(textMark);
    }


    /**
     * Link options. Must be a combination of HLINK_* constants.
     * For testing only
     *
     * @return Link options
     */
    int getLinkOptions(){
        return _linkOpts;
    }

    /**
     * @return Label options
     */
    public int getLabelOptions(){
        return 2; // always 2
    }

    /**
     * @return Options for a file link
     */
    public int getFileOptions(){
        return _fileOpts;
    }


    public HyperlinkRecord(RecordInputStream in) {
        _range = new CellRangeAddress(in);

        _guid = new ClassID(in);

        /*
         * streamVersion (4 bytes): An unsigned integer that specifies the version number
         * of the serialization implementation used to save this structure. This value MUST equal 2.
         */
        int streamVersion = in.readInt();
        if (streamVersion != 0x00000002) {
            throw new RecordFormatException("Stream Version must be 0x2 but found " + streamVersion);
        }
        _linkOpts = in.readInt();

        if ((_linkOpts & HLINK_LABEL) != 0){
            int label_len = in.readInt();
            _label = in.readUnicodeLEString(label_len);
        }

        if ((_linkOpts & HLINK_TARGET_FRAME) != 0){
            int len = in.readInt();
            _targetFrame = in.readUnicodeLEString(len);
        }

        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) != 0) {
            _moniker = null;
            int nChars = in.readInt();
            _address = in.readUnicodeLEString(nChars);
        }

        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) == 0) {
            _moniker = new ClassID(in);

            if(URL_MONIKER.equals(_moniker)){
                int length = in.readInt();
                /*
                 * The value of <code>length<code> be either the byte size of the url field
                 * (including the terminating NULL character) or the byte size of the url field plus 24.
                 * If the value of this field is set to the byte size of the url field,
                 * then the tail bytes fields are not present.
                 */
                int remaining = in.remaining();
                if (length == remaining) {
                    int nChars = length/2;
                    _address = in.readUnicodeLEString(nChars);
                } else {
                    int nChars = (length - TAIL_SIZE)/2;
                    _address = in.readUnicodeLEString(nChars);
                    /*
                     * TODO: make sense of the remaining bytes
                     * According to the spec they consist of:
                     * 1. 16-byte  GUID: This field MUST equal
                     *    {0xF4815879, 0x1D3B, 0x487F, 0xAF, 0x2C, 0x82, 0x5D, 0xC4, 0x85, 0x27, 0x63}
                     * 2. Serial version, this field MUST equal 0 if present.
                     * 3. URI Flags
                     */
                    _uninterpretedTail = readTail(URL_TAIL, in);
                }
            } else if (FILE_MONIKER.equals(_moniker)) {
                _fileOpts = in.readShort();

                int len = in.readInt();
                _shortFilename = StringUtil.readCompressedUnicode(in, len);
                _uninterpretedTail = readTail(FILE_TAIL, in);
                int size = in.readInt();
                if (size > 0) {
                    int charDataSize = in.readInt();

                    //From the spec: An optional unsigned integer that MUST be 3 if present
                    // but some files has 4
                    /*int usKeyValue = */ in.readUShort();

                    _address = StringUtil.readUnicodeLE(in, charDataSize/2);
                } else {
                    _address = null;
                }
            } else if (STD_MONIKER.equals(_moniker)) {
                _fileOpts = in.readShort();

                int len = in.readInt();

                byte[] path_bytes = IOUtils.safelyAllocate(len, HSSFWorkbook.getMaxRecordLength());
                in.readFully(path_bytes);

                _address = new String(path_bytes, StringUtil.UTF8);
            }
        }

        if((_linkOpts & HLINK_PLACE) != 0) {

            int len = in.readInt();
            _textMark = in.readUnicodeLEString(len);
        }

        if (in.remaining() > 0) {
            LOG.atWarn().log("Hyperlink data remains: {} : {}", box(in.remaining()), toHex(in.readRemainder()));
        }
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        _range.serialize(out);

        _guid.write(out);
        out.writeInt(0x00000002); // TODO const
        out.writeInt(_linkOpts);

        if ((_linkOpts & HLINK_LABEL) != 0){
            out.writeInt(_label.length());
            StringUtil.putUnicodeLE(_label, out);
        }
        if ((_linkOpts & HLINK_TARGET_FRAME) != 0){
            out.writeInt(_targetFrame.length());
            StringUtil.putUnicodeLE(_targetFrame, out);
        }

        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) != 0) {
            out.writeInt(_address.length());
            StringUtil.putUnicodeLE(_address, out);
        }

        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) == 0) {
            _moniker.write(out);
            if(URL_MONIKER.equals(_moniker)){
                if (_uninterpretedTail == null) {
                    out.writeInt(_address.length()*2);
                    StringUtil.putUnicodeLE(_address, out);
                } else {
                    out.writeInt(_address.length()*2 + TAIL_SIZE);
                    StringUtil.putUnicodeLE(_address, out);
                    writeTail(_uninterpretedTail, out);
                }
            } else if (FILE_MONIKER.equals(_moniker)){
                out.writeShort(_fileOpts);
                out.writeInt(_shortFilename.length());
                StringUtil.putCompressedUnicode(_shortFilename, out);
                writeTail(_uninterpretedTail, out);
                if (_address == null) {
                    out.writeInt(0);
                } else {
                    int addrLen = _address.length() * 2;
                    out.writeInt(addrLen + 6);
                    out.writeInt(addrLen);
                    out.writeShort(0x0003); // TODO const
                    StringUtil.putUnicodeLE(_address, out);
                }
            }
        }
        if((_linkOpts & HLINK_PLACE) != 0){
               out.writeInt(_textMark.length());
            StringUtil.putUnicodeLE(_textMark, out);
        }
    }

    @Override
    protected int getDataSize() {
        int size = 0;
        size += 2 + 2 + 2 + 2;  //rwFirst, rwLast, colFirst, colLast
        size += ClassID.LENGTH;
        size += 4;  //label_opts
        size += 4;  //link_opts
        if ((_linkOpts & HLINK_LABEL) != 0){
            size += 4;  //link length
            size += _label.length()*2;
        }
        if ((_linkOpts & HLINK_TARGET_FRAME) != 0){
            size += 4;  // int nChars
            size += _targetFrame.length()*2;
        }
        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) != 0) {
            size += 4;  // int nChars
            size += _address.length()*2;
        }
        if ((_linkOpts & HLINK_URL) != 0 && (_linkOpts & HLINK_UNC_PATH) == 0) {
            size += ClassID.LENGTH;
            if(URL_MONIKER.equals(_moniker)){
                size += 4;  //address length
                size += _address.length()*2;
                if (_uninterpretedTail != null) {
                    size += TAIL_SIZE;
                }
            } else if (FILE_MONIKER.equals(_moniker)){
                size += 2;  //file_opts
                size += 4;  //address length
                size += _shortFilename.length();
                size += TAIL_SIZE;
                size += 4;
                if (_address != null) {
                    size += 6;
                    size += _address.length() * 2;
                }

            }
        }
        if((_linkOpts & HLINK_PLACE) != 0){
            size += 4;  //address length
            size += _textMark.length()*2;
        }
        return size;
    }


    private static byte[] readTail(byte[] expectedTail, LittleEndianInput in) {
        byte[] result = new byte[TAIL_SIZE];
        in.readFully(result);
        return result;
    }

    private static void writeTail(byte[] tail, LittleEndianOutput out) {
        out.write(tail);
    }

    @Override
    public short getSid() {
        return HyperlinkRecord.sid;
    }


    /**
     * Based on the link options, is this a url?
     *
     * @return true, if this is a url link
     */
    @SuppressWarnings("unused")
    public boolean isUrlLink() {
       return (_linkOpts & HLINK_URL) > 0
           && (_linkOpts & HLINK_ABS) > 0;
    }
    /**
     * Based on the link options, is this a file?
     *
     * @return true, if this is a file link
     */
    public boolean isFileLink() {
       return (_linkOpts & HLINK_URL) > 0
           && (_linkOpts & HLINK_ABS) == 0;
    }
    /**
     * Based on the link options, is this a document?
     *
     * @return true, if this is a docment link
     */
    public boolean isDocumentLink() {
       return (_linkOpts & HLINK_PLACE) > 0;
    }

    /**
     * Initialize a new url link
     */
    public void newUrlLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER.getClassID();
        _linkOpts = HLINK_URL | HLINK_ABS | HLINK_LABEL;
        setLabel("");
        _moniker = URL_MONIKER.getClassID();
        setAddress("");
        _uninterpretedTail = URL_TAIL;
    }

    /**
     * Initialize a new file link
     */
    public void newFileLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER.getClassID();
        _linkOpts = HLINK_URL | HLINK_LABEL;
        _fileOpts = 0;
        setLabel("");
        _moniker = FILE_MONIKER.getClassID();
        setAddress(null);
        setShortFilename("");
        _uninterpretedTail = FILE_TAIL;
    }

    /**
     * Initialize a new document link
     */
    public void newDocumentLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER.getClassID();
        _linkOpts = HLINK_LABEL | HLINK_PLACE;
        setLabel("");
        _moniker = FILE_MONIKER.getClassID();
        setAddress("");
        setTextMark("");
    }

    @Override
    public HyperlinkRecord copy() {
        return new HyperlinkRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.HYPERLINK;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "range", () -> _range,
            "guid", this::getGuid,
            "linkOpts", () -> getBitsAsString(this::getLinkOptions,
                  new int[]{HLINK_URL,HLINK_ABS,HLINK_PLACE,HLINK_LABEL,HLINK_TARGET_FRAME,HLINK_UNC_PATH},
                  new String[]{"URL","ABS","PLACE","LABEL","TARGET_FRAME","UNC_PATH"}),
            "label", this::getLabel,
            "targetFrame", this::getTargetFrame,
            "moniker", this::getMoniker,
            "textMark", this::getTextMark,
            "address", this::getAddress
        );
    }
}
