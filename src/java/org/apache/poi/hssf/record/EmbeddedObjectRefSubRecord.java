
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



import org.apache.poi.util.*;

/**
 * A sub-record within the OBJ record which stores a reference to an object
 * stored in a separate entry within the OLE2 compound file.
 *
 * @author Daniel Noll
 */
public class EmbeddedObjectRefSubRecord
    extends SubRecord
{
    public static final short sid = 0x9;

    public short   field_1_stream_id_offset;                    // Offset to stream ID from the point after this value.
    public short[] field_2_unknown;                             // Unknown stuff at the front.  TODO: Confirm that it's a short[]
    // TODO: Consider making a utility class for these.  I've discovered the same field ordering
    //       in FormatRecord and StringRecord, it may be elsewhere too.
    public short   field_3_unicode_len;                         // Length of Unicode string.
    public boolean field_4_unicode_flag;                        // Flags whether the string is Unicode.
    public String  field_5_ole_classname;                       // Classname of the embedded OLE document (e.g. Word.Document.8)
    public int     field_6_stream_id;                           // ID of the OLE stream containing the actual data.

    private int field_5_ole_classname_padding; // developer laziness...
    public byte[] remainingBytes;

    public EmbeddedObjectRefSubRecord()
    {
        field_2_unknown = new short[0];
        remainingBytes = new byte[0];
        field_1_stream_id_offset = 6;
        field_5_ole_classname = "";
    }

    /**
     * Constructs an EmbeddedObjectRef record and sets its fields appropriately.
     *
     * @param in the record input stream.
     */
    public EmbeddedObjectRefSubRecord(RecordInputStream in)
    {
        super(in);
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a EmbeddedObjectRef record");
        }
    }

    public short getSid()
    {
        return sid;
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_stream_id_offset       = in.readShort();
        field_2_unknown                = in.readShortArray();
        field_3_unicode_len            = in.readShort();
        field_4_unicode_flag           = ( in.readByte() & 0x01 ) != 0;

        if ( field_4_unicode_flag )
        {
            field_5_ole_classname      = in.readUnicodeLEString( field_3_unicode_len );
        }
        else
        {
            field_5_ole_classname      = in.readCompressedUnicode( field_3_unicode_len );
        }

        // Padded with NUL bytes.  The -2 is because field_1_stream_id_offset
        // is relative to after the offset field, whereas in.getRecordOffset()
        // is relative to the start of this record (minus the header.)
        field_5_ole_classname_padding = 0;
        while (in.getRecordOffset() - 2 < field_1_stream_id_offset)
        {
            field_5_ole_classname_padding++;
            in.readByte(); // discard
        }

        // Fetch the stream ID
        field_6_stream_id = in.readInt();
        
        // Store what's left
        remainingBytes = in.readRemainder();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = offset;

        LittleEndian.putShort(data, pos, sid); pos += 2;
        LittleEndian.putShort(data, pos, (short)(getRecordSize() - 4)); pos += 2;

        LittleEndian.putShort(data, pos, field_1_stream_id_offset); pos += 2;
        LittleEndian.putShortArray(data, pos, field_2_unknown); pos += field_2_unknown.length * 2 + 2;
        LittleEndian.putShort(data, pos, field_3_unicode_len); pos += 2;
        data[pos] = field_4_unicode_flag ? (byte) 0x01 : (byte) 0x00; pos++;

        if ( field_4_unicode_flag )
        {
            StringUtil.putUnicodeLE( field_5_ole_classname, data, pos ); pos += field_5_ole_classname.length() * 2;
        }
        else
        {
            StringUtil.putCompressedUnicode( field_5_ole_classname, data, pos ); pos += field_5_ole_classname.length();
        }

        // Padded with the same number of NUL bytes as were originally skipped.
        // XXX: This is only accurate until we make the classname mutable.
        pos += field_5_ole_classname_padding;
        
        LittleEndian.putInt(data, pos, field_6_stream_id); pos += 4;

        System.arraycopy(remainingBytes, 0, data, pos, remainingBytes.length);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        // The stream id offset is relative to after the stream ID.
        // Add 2 bytes for the stream id offset and 4 bytes for the stream id itself and 4 byts for the record header.
        return remainingBytes.length + field_1_stream_id_offset + 2 + 4 + 4;
    }

    /**
     * Gets the stream ID containing the actual data.  The data itself
     * can be found under a top-level directory entry in the OLE2 filesystem
     * under the name "MBD<var>xxxxxxxx</var>" where <var>xxxxxxxx</var> is
     * this ID converted into hex (in big endian order, funnily enough.)
     * 
     * @return the data stream ID.
     */
    public int getStreamId()
    {
        return field_6_stream_id;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[ftPictFmla]\n");
        buffer.append("    .streamIdOffset       = ")
            .append("0x").append(HexDump.toHex(  field_1_stream_id_offset ))
            .append(" (").append( field_1_stream_id_offset ).append(" )")
            .append(System.getProperty("line.separator"));
        buffer.append("    .unknown              = ")
            .append("0x").append(HexDump.toHex(  field_2_unknown ))
            .append(" (").append( field_2_unknown.length ).append(" )")
            .append(System.getProperty("line.separator"));
        buffer.append("    .unicodeLen           = ")
            .append("0x").append(HexDump.toHex(  field_3_unicode_len ))
            .append(" (").append( field_3_unicode_len ).append(" )")
            .append(System.getProperty("line.separator"));
        buffer.append("    .unicodeFlag          = ")
            .append("0x").append( field_4_unicode_flag ? 0x01 : 0x00 )
            .append(" (").append( field_4_unicode_flag ).append(" )")
            .append(System.getProperty("line.separator"));
        buffer.append("    .oleClassname         = ")
            .append(field_5_ole_classname)
            .append(System.getProperty("line.separator"));
        buffer.append("    .streamId             = ")
            .append("0x").append(HexDump.toHex(  field_6_stream_id ))
            .append(" (").append( field_6_stream_id ).append(" )")
            .append(System.getProperty("line.separator"));
        buffer.append("[/ftPictFmla]");
        return buffer.toString();
    }

}
