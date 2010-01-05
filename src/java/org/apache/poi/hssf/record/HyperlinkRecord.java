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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * The <code>HyperlinkRecord</code> (0x01B8) wraps an HLINK-record
 *  from the Excel-97 format.
 * Supports only external links for now (eg http://)
 *
 * @author      Mark Hissink Muller <a href="mailto:mark@hissinkmuller.nl >mark&064;hissinkmuller.nl</a>
 * @author      Yegor Kozlov (yegor at apache dot org)
 */
public final class HyperlinkRecord extends StandardRecord {
    public final static short sid = 0x01B8;

    static final class GUID {
		/*
		 * this class is currently only used here, but could be moved to a
		 * common package if needed
		 */
		private static final int TEXT_FORMAT_LENGTH = 36;

		public static final int ENCODED_SIZE = 16;

		/** 4 bytes - little endian */
		private final int _d1;
		/** 2 bytes - little endian */
		private final int _d2;
		/** 2 bytes - little endian */
		private final int _d3;
		/**
		 * 8 bytes - serialized as big endian,  stored with inverted endianness here
		 */
		private final long _d4;

		public GUID(LittleEndianInput in) {
			this(in.readInt(), in.readUShort(), in.readUShort(), in.readLong());
		}

		public GUID(int d1, int d2, int d3, long d4) {
			_d1 = d1;
			_d2 = d2;
			_d3 = d3;
			_d4 = d4;
		}

		public void serialize(LittleEndianOutput out) {
			out.writeInt(_d1);
			out.writeShort(_d2);
			out.writeShort(_d3);
			out.writeLong(_d4);
		}

		@Override
		public boolean equals(Object obj) {
			GUID other = (GUID) obj;
            if (obj == null || !(obj instanceof GUID))
                return false;
			return _d1 == other._d1 && _d2 == other._d2
			    && _d3 == other._d3 && _d4 == other._d4;
		}

		public int getD1() {
			return _d1;
		}

		public int getD2() {
			return _d2;
		}

		public int getD3() {
			return _d3;
		}

		public long getD4() {
			//
			ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
			try {
				new DataOutputStream(baos).writeLong(_d4);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			byte[] buf = baos.toByteArray();
			return new LittleEndianByteArrayInputStream(buf).readLong();
		}

		public String formatAsString() {

			StringBuilder sb = new StringBuilder(36);

			int PREFIX_LEN = "0x".length();
			sb.append(HexDump.intToHex(_d1), PREFIX_LEN, 8);
			sb.append("-");
			sb.append(HexDump.shortToHex(_d2), PREFIX_LEN, 4);
			sb.append("-");
			sb.append(HexDump.shortToHex(_d3), PREFIX_LEN, 4);
			sb.append("-");
			char[] d4Chars = HexDump.longToHex(getD4());
			sb.append(d4Chars, PREFIX_LEN, 4);
			sb.append("-");
			sb.append(d4Chars, PREFIX_LEN + 4, 12);
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(formatAsString());
			sb.append("]");
			return sb.toString();
		}

		/**
		 * Read a GUID in standard text form e.g.<br/>
		 * 13579BDF-0246-8ACE-0123-456789ABCDEF 
		 * <br/> -&gt; <br/>
		 *  0x13579BDF, 0x0246, 0x8ACE 0x0123456789ABCDEF
		 */
		public static GUID parse(String rep) {
			char[] cc = rep.toCharArray();
			if (cc.length != TEXT_FORMAT_LENGTH) {
				throw new RecordFormatException("supplied text is the wrong length for a GUID");
			}
			int d0 = (parseShort(cc, 0) << 16) + (parseShort(cc, 4) << 0);
			int d1 = parseShort(cc, 9);
			int d2 = parseShort(cc, 14);
			for (int i = 23; i > 19; i--) {
				cc[i] = cc[i - 1];
			}
			long d3 = parseLELong(cc, 20);

			return new GUID(d0, d1, d2, d3);
		}

		private static long parseLELong(char[] cc, int startIndex) {
			long acc = 0;
			for (int i = startIndex + 14; i >= startIndex; i -= 2) {
				acc <<= 4;
				acc += parseHexChar(cc[i + 0]);
				acc <<= 4;
				acc += parseHexChar(cc[i + 1]);
			}
			return acc;
		}

		private static int parseShort(char[] cc, int startIndex) {
			int acc = 0;
			for (int i = 0; i < 4; i++) {
				acc <<= 4;
				acc += parseHexChar(cc[startIndex + i]);
			}
			return acc;
		}

		private static int parseHexChar(char c) {
			if (c >= '0' && c <= '9') {
				return c - '0';
			}
			if (c >= 'A' && c <= 'F') {
				return c - 'A' + 10;
			}
			if (c >= 'a' && c <= 'f') {
				return c - 'a' + 10;
			}
			throw new RecordFormatException("Bad hex char '" + c + "'");
		}
	}

    /**
     * Link flags
     */
     static final int  HLINK_URL    = 0x01;  // File link or URL.
     static final int  HLINK_ABS    = 0x02;  // Absolute path.
     static final int  HLINK_LABEL  = 0x14;  // Has label/description.
    /** Place in worksheet. If set, the {@link #_textMark} field will be present */
     static final int  HLINK_PLACE  = 0x08;
    private static final int  HLINK_TARGET_FRAME  = 0x80;  // has 'target frame'
    private static final int  HLINK_UNC_PATH  = 0x100;  // has UNC path

     final static GUID STD_MONIKER = GUID.parse("79EAC9D0-BAF9-11CE-8C82-00AA004BA90B");
     final static GUID URL_MONIKER = GUID.parse("79EAC9E0-BAF9-11CE-8C82-00AA004BA90B");
     final static GUID FILE_MONIKER = GUID.parse("00000303-0000-0000-C000-000000000046");
    /** expected Tail of a URL link */
    private final static byte[] URL_TAIL  = HexRead.readFromString("79 58 81 F4  3B 1D 7F 48   AF 2C 82 5D  C4 85 27 63   00 00 00 00  A5 AB 00 00"); 
    /** expected Tail of a file link */
    private final static byte[] FILE_TAIL = HexRead.readFromString("FF FF AD DE  00 00 00 00   00 00 00 00  00 00 00 00   00 00 00 00  00 00 00 00");

    private static final int TAIL_SIZE = FILE_TAIL.length;

    /** cell range of this hyperlink */
    private CellRangeAddress _range;

    /** 16-byte GUID */
    private GUID _guid;
    /** Some sort of options for file links. */
    private int _fileOpts;
    /** Link options. Can include any of HLINK_* flags. */
    private int _linkOpts;
    /** Test label */
    private String _label;

    private String _targetFrame;
    /** Moniker. Makes sense only for URL and file links */
    private GUID _moniker;
    /** in 8:3 DOS format No Unicode string header,
     * always 8-bit characters, zero-terminated */
    private String _shortFilename;
    /** Link */
    private String _address;
    /**
     * Text describing a place in document.  In Excel UI, this is appended to the
     * address, (after a '#' delimiter).<br/>
     * This field is optional.  If present, the {@link #HLINK_PLACE} must be set.
     */
    private String _textMark;
    
    private byte[] _uninterpretedTail;

    /**
     * Create a new hyperlink
     */
    public HyperlinkRecord()
    {

    }

    /**
     * @return the 0-based column of the first cell that contains this hyperlink
     */
    public int getFirstColumn() {
        return _range.getFirstColumn();
    }

    /**
     * Set the first column (zero-based)of the range that contains this hyperlink
     */
    public void setFirstColumn(int col) {
        _range.setFirstColumn(col);
    }

    /**
     * @return the 0-based column of the last cell that contains this hyperlink
     */
    public int getLastColumn() {
        return _range.getLastColumn();
    }

    /**
     * Set the last column (zero-based)of the range that contains this hyperlink
     */
    public void setLastColumn(int col) {
        _range.setLastColumn(col);
    }

    /**
     * @return the 0-based row of the first cell that contains this hyperlink
     */
    public int getFirstRow() {
        return _range.getFirstRow();
    }

    /**
     * Set the first row (zero-based)of the range that contains this hyperlink
     */
    public void setFirstRow(int col) {
        _range.setFirstRow(col);
    }

    /**
     * @return the 0-based row of the last cell that contains this hyperlink
     */
    public int getLastRow() {
        return _range.getLastRow();
    }

    /**
     * Set the last row (zero-based)of the range that contains this hyperlink
     */
    public void setLastRow(int col) {
        _range.setLastRow(col);
    }

    /**
     * @return 16-byte guid identifier Seems to always equal {@link #STD_MONIKER}
     */
    GUID getGuid() {
        return _guid;
    }

    /**
     * @return 16-byte moniker
     */
    GUID getMoniker()
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
        if ((_linkOpts & HLINK_URL) != 0 && FILE_MONIKER.equals(_moniker))
            return cleanString(_address != null ? _address : _shortFilename);
        else if((_linkOpts & HLINK_PLACE) != 0)
            return cleanString(_textMark);
        else
            return cleanString(_address);
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    public void setAddress(String address) {
        if ((_linkOpts & HLINK_URL) != 0 && FILE_MONIKER.equals(_moniker))
            _shortFilename = appendNullTerm(address);
        else if((_linkOpts & HLINK_PLACE) != 0)
            _textMark = appendNullTerm(address);
        else
            _address = appendNullTerm(address);
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
     */
    int getLinkOptions(){
        return _linkOpts;
    }

    /**
     * Label options
     */
    public int getLabelOptions(){
        return 2; // always 2
    }

    /**
     * Options for a file link
     */
    public int getFileOptions(){
        return _fileOpts;
    }


    public HyperlinkRecord(RecordInputStream in) {
        _range = new CellRangeAddress(in);

        _guid = new GUID(in);

        /**
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
            _moniker = new GUID(in);

            if(URL_MONIKER.equals(_moniker)){
                int length = in.readInt();
                /**
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
                    /**
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
                    int optFlags = in.readUShort();
                    if (optFlags != 0x0003) {
                        throw new RecordFormatException("Expected 0x3 but found " + optFlags);
                    }
                    _address = StringUtil.readUnicodeLE(in, charDataSize/2);
                } else {
                    _address = null;
                }
            } else if (STD_MONIKER.equals(_moniker)) {
                _fileOpts = in.readShort();

                int len = in.readInt();

                byte[] path_bytes = new byte[len];
                in.readFully(path_bytes);

                _address = new String(path_bytes);
            }
        }

        if((_linkOpts & HLINK_PLACE) != 0) {

            int len = in.readInt();
            _textMark = in.readUnicodeLEString(len);
        }

        if (in.remaining() > 0) {
            System.out.println(HexDump.toHex(in.readRemainder()));
        }
    }

    public void serialize(LittleEndianOutput out) {
        _range.serialize(out);

        _guid.serialize(out);
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
            _moniker.serialize(out);
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

    protected int getDataSize() {
        int size = 0;
        size += 2 + 2 + 2 + 2;  //rwFirst, rwLast, colFirst, colLast
        size += GUID.ENCODED_SIZE;
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
            size += GUID.ENCODED_SIZE;
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
    	if (false) { // Quite a few examples in the unit tests which don't have the exact expected tail
            for (int i = 0; i < expectedTail.length; i++) {
                if (expectedTail[i] != result[i]) {
                    System.err.println("Mismatch in tail byte [" + i + "]"
                    		+ "expected " + (expectedTail[i] & 0xFF) + " but got " + (result[i] & 0xFF));
                }
            }
    	}
        return result;
    }
    private static void writeTail(byte[] tail, LittleEndianOutput out) {
        out.write(tail);
    }

    public short getSid() {
        return HyperlinkRecord.sid;
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HYPERLINK RECORD]\n");
        buffer.append("    .range   = ").append(_range.formatAsString()).append("\n");
        buffer.append("    .guid    = ").append(_guid.formatAsString()).append("\n");
        buffer.append("    .linkOpts= ").append(HexDump.intToHex(_linkOpts)).append("\n");
        buffer.append("    .label   = ").append(getLabel()).append("\n");
        if ((_linkOpts & HLINK_TARGET_FRAME) != 0) {
            buffer.append("    .targetFrame= ").append(getTargetFrame()).append("\n");
        }
        if((_linkOpts & HLINK_URL) != 0 && _moniker != null) {
            buffer.append("    .moniker   = ").append(_moniker.formatAsString()).append("\n");
        }
        if ((_linkOpts & HLINK_PLACE) != 0) {
            buffer.append("    .textMark= ").append(getTextMark()).append("\n");
        }
        buffer.append("    .address   = ").append(getAddress()).append("\n");
        buffer.append("[/HYPERLINK RECORD]\n");
        return buffer.toString();
    }

    /**
     * Based on the link options, is this a url?
     */
    public boolean isUrlLink() {
       return (_linkOpts & HLINK_URL) > 0 
           && (_linkOpts & HLINK_ABS) > 0;
    }
    /**
     * Based on the link options, is this a file?
     */
    public boolean isFileLink() {
       return (_linkOpts & HLINK_URL) > 0 
           && (_linkOpts & HLINK_ABS) == 0;
    }
    /**
     * Based on the link options, is this a document?
     */
    public boolean isDocumentLink() {
       return (_linkOpts & HLINK_PLACE) > 0; 
    }
    
    /**
     * Initialize a new url link
     */
    public void newUrlLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER;
        _linkOpts = HLINK_URL | HLINK_ABS | HLINK_LABEL;
        setLabel("");
        _moniker = URL_MONIKER;
        setAddress("");
        _uninterpretedTail = URL_TAIL;
    }

    /**
     * Initialize a new file link
     */
    public void newFileLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER;
        _linkOpts = HLINK_URL | HLINK_LABEL;
        _fileOpts = 0;
        setLabel("");
        _moniker = FILE_MONIKER;
        setAddress(null);
        setShortFilename("");
        _uninterpretedTail = FILE_TAIL;
    }

    /**
     * Initialize a new document link
     */
    public void newDocumentLink() {
        _range = new CellRangeAddress(0, 0, 0, 0);
        _guid = STD_MONIKER;
        _linkOpts = HLINK_LABEL | HLINK_PLACE;
        setLabel("");
        _moniker = FILE_MONIKER;
        setAddress("");
        setTextMark("");
    }

    public Object clone() {
        HyperlinkRecord rec = new HyperlinkRecord();
        rec._range = _range.copy();
        rec._guid = _guid;
        rec._linkOpts = _linkOpts;
        rec._fileOpts = _fileOpts;
        rec._label = _label;
        rec._address = _address;
        rec._moniker = _moniker;
        rec._shortFilename = _shortFilename;
        rec._targetFrame = _targetFrame;
        rec._textMark = _textMark;
        rec._uninterpretedTail = _uninterpretedTail;
        return rec;
    }
}
