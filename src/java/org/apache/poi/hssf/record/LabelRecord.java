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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;

/**
 * Label Record (0x0204) - read only support for strings stored directly in the cell...
 * Don't use this (except to read), use LabelSST instead <P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 * 
 * @see org.apache.poi.hssf.record.LabelSSTRecord
 */
public final class LabelRecord extends Record implements CellValueRecordInterface, Cloneable {
    private final static POILogger logger = POILogFactory.getLogger(LabelRecord.class);

    public final static short sid = 0x0204;

    private int               field_1_row;
    private short             field_2_column;
    private short             field_3_xf_index;
    private short             field_4_string_len;
    private byte              field_5_unicode_flag;
    private String            field_6_value;

    /** Creates new LabelRecord */
    public LabelRecord()
    {
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public LabelRecord(RecordInputStream in)
    {
        field_1_row          = in.readUShort();
        field_2_column       = in.readShort();
        field_3_xf_index     = in.readShort();
        field_4_string_len   = in.readShort();
        field_5_unicode_flag = in.readByte();
        if (field_4_string_len > 0) {
            if (isUnCompressedUnicode()) {
                field_6_value = in.readUnicodeLEString(field_4_string_len);
            } else {
                field_6_value = in.readCompressedUnicode(field_4_string_len);
            }
        } else {
            field_6_value = "";
        }

        if (in.remaining() > 0) {
           logger.log(POILogger.INFO,
                   "LabelRecord data remains: " + in.remaining() +
                           " : " + HexDump.toHex(in.readRemainder())
           );
        }
    }

/*
 * READ ONLY ACCESS... THIS IS FOR COMPATIBILITY ONLY...USE LABELSST! public
 */
    @Override
    public int getRow()
    {
        return field_1_row;
    }

    @Override
    public short getColumn()
    {
        return field_2_column;
    }

    @Override
    public short getXFIndex()
    {
        return field_3_xf_index;
    }

    /**
     * get the number of characters this string contains
     * @return number of characters
     */
    public short getStringLength()
    {
        return field_4_string_len;
    }

    /**
     * is this uncompressed unicode (16bit)?  Or just 8-bit compressed?
     * @return isUnicode - True for 16bit- false for 8bit
     */
    public boolean isUnCompressedUnicode()
    {
        return (field_5_unicode_flag & 0x01) != 0;
    }

    /**
     * get the value
     *
     * @return the text string
     * @see #getStringLength()
     */
    public String getValue()
    {
        return field_6_value;
    }

    /**
     * THROWS A RUNTIME EXCEPTION..  USE LABELSSTRecords.  YOU HAVE NO REASON to use LABELRecord!!
     */
    @Override
    public int serialize(int offset, byte [] data) {
        throw new RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST");
    }
    @Override
    public int getRecordSize() {
        throw new RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST");
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
		sb.append("[LABEL]\n");
		sb.append("    .row       = ").append(HexDump.shortToHex(getRow())).append("\n");
		sb.append("    .column    = ").append(HexDump.shortToHex(getColumn())).append("\n");
		sb.append("    .xfindex   = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
		sb.append("    .string_len= ").append(HexDump.shortToHex(field_4_string_len)).append("\n");
		sb.append("    .unicode_flag= ").append(HexDump.byteToHex(field_5_unicode_flag)).append("\n");
		sb.append("    .value       = ").append(getValue()).append("\n");
		sb.append("[/LABEL]\n");
        return sb.toString();
    }

    /**
	 * NO-OP!
	 */
    @Override
    public void setColumn(short col)
    {
    }

    /**
     * NO-OP!
     */
    @Override
    public void setRow(int row)
    {
    }

    /**
     * no op!
     */
    @Override
    public void setXFIndex(short xf)
    {
    }

    @Override
    public LabelRecord clone() {
      LabelRecord rec = new LabelRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_column = field_2_column;
      rec.field_3_xf_index = field_3_xf_index;
      rec.field_4_string_len = field_4_string_len;
      rec.field_5_unicode_flag = field_5_unicode_flag;
      rec.field_6_value = field_6_value;
      return rec;
    }
}
