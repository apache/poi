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

import org.apache.poi.util.LittleEndian;

/**
 * NOTE: Comment Associated with a Cell (1Ch)
 *
 * @author Yegor Kozlov
 */
public class NoteRecord extends Record {
    public final static short sid = 0x1C;

    /**
     * Flag indicating that the comment is hidden (default)
     */
    public final static short NOTE_HIDDEN = 0x0;

    /**
     * Flag indicating that the comment is visible
     */
    public final static short NOTE_VISIBLE = 0x2;

    private short           field_1_row;
    private short           field_2_col;
    private short           field_3_flags;
    private short           field_4_shapeid;
    private String          field_5_author;

    /**
     * Construct a new <code>NoteRecord</code> and
     * fill its data with the default values
     */
    public NoteRecord()
    {
        field_5_author = "";
        field_3_flags = 0;
    }

    /**
     * Constructs a <code>NoteRecord</code> and fills its fields
     * from the supplied <code>RecordInputStream</code>.
     *
     * @param in the stream to read from
     */
    public NoteRecord(RecordInputStream in)
    {
        super(in);

    }

    /**
     * @return id of this record.
     */
    public short getSid()
    {
        return sid;
    }

    /**
     * Read the record data from the supplied <code>RecordInputStream</code>
     */
    protected void fillFields(RecordInputStream in)
    {
        field_1_row = in.readShort();
        field_2_col = in.readShort();
        field_3_flags = in.readShort();
        field_4_shapeid = in.readShort();
        int length = in.readShort();
        byte[] bytes = in.readRemainder();
        field_5_author = new String(bytes, 1, length);
    }

    /**
     * Serialize the record data into the supplied array of bytes
     *
     * @param offset offset in the <code>data</code>
     * @param data the data to serialize into
     *
     * @return size of the record
     */
    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset , field_1_row);
        LittleEndian.putShort(data, 6 + offset , field_2_col);
        LittleEndian.putShort(data, 8 + offset , field_3_flags);
        LittleEndian.putShort(data, 10 + offset , field_4_shapeid);
        LittleEndian.putShort(data, 12 + offset , (short)field_5_author.length());

        byte[] str = field_5_author.getBytes();
        System.arraycopy(str, 0, data, 15 + offset, str.length);

        return getRecordSize();
    }

    /**
     * Size of record
     */
    public int getRecordSize()
    {
        int retval = 4 + 2 + 2 + 2 + 2 + 2 + 1 + field_5_author.length() + 1;

        return retval;
    }

    /**
     * Convert this record to string.
     * Used by BiffViewer and other utulities.
     */
     public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[NOTE]\n");
        buffer.append("    .recordid = 0x" + Integer.toHexString( getSid() ) + ", size = " + getRecordSize() + "\n");
        buffer.append("    .row =     " + field_1_row + "\n");
        buffer.append("    .col =     " + field_2_col + "\n");
        buffer.append("    .flags =   " + field_3_flags + "\n");
        buffer.append("    .shapeid = " + field_4_shapeid + "\n");
        buffer.append("    .author =  " + field_5_author + "\n");
        buffer.append("[/NOTE]\n");
        return buffer.toString();
    }

    /**
     * Return the row that contains the comment
     *
     * @return the row that contains the comment
     */
    public short getRow(){
        return field_1_row;
    }

    /**
     * Specify the row that contains the comment
     *
     * @param row the row that contains the comment
     */
    public void setRow(short row){
        field_1_row = row;
    }

    /**
     * Return the column that contains the comment
     *
     * @return the column that contains the comment
     */
    public short getColumn(){
        return field_2_col;
    }

    /**
     * Specify the column that contains the comment
     *
     * @param col the column that contains the comment
     */
    public void setColumn(short col){
        field_2_col = col;
    }

    /**
     * Options flags.
     *
     * @return the options flag
     * @see #NOTE_VISIBLE
     * @see #NOTE_HIDDEN
     */
    public short getFlags(){
        return field_3_flags;
    }

    /**
     * Options flag
     *
     * @param flags the options flag
     * @see #NOTE_VISIBLE
     * @see #NOTE_HIDDEN
     */
    public void setFlags(short flags){
        field_3_flags = flags;
    }

    /**
     * Object id for OBJ record that contains the comment
     */
    public short getShapeId(){
        return field_4_shapeid;
    }

    /**
     * Object id for OBJ record that contains the comment
     */
    public void setShapeId(short id){
        field_4_shapeid = id;
    }

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    public String getAuthor(){
        return field_5_author;
    }

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    public void setAuthor(String author){
        field_5_author = author;
    }

    public Object clone() {
        NoteRecord rec = new NoteRecord();
        rec.field_1_row = field_1_row;
        rec.field_2_col = field_2_col;
        rec.field_3_flags = field_3_flags;
        rec.field_4_shapeid = field_4_shapeid;
        rec.field_5_author = field_5_author;
        return rec;
    }

}
