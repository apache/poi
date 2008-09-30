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

import java.io.IOException;
import java.util.Arrays;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;

/**
 * The <code>HyperlinkRecord</code> wraps an HLINK-record 
 *  from the Excel-97 format.
 * Supports only external links for now (eg http://) 
 *
 * @author      Mark Hissink Muller <a href="mailto:mark@hissinkmuller.nl >mark&064;hissinkmuller.nl</a>
 * @author      Yegor Kozlov (yegor at apache dot org)
 */
public class HyperlinkRecord extends Record {
    /**
     * Link flags
     */
    protected static final int  HLINK_URL    = 0x01;  // File link or URL.
    protected static final int  HLINK_ABS    = 0x02;  // Absolute path.
    protected static final int  HLINK_LABEL  = 0x14;  // Has label.
    protected static final int  HLINK_PLACE  = 0x08;  // Place in worksheet.


    protected final static byte[] STD_MONIKER = {(byte)0xD0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                                                 (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B };
    protected final static byte[] URL_MONIKER = {(byte)0xE0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                                                 (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B };
    protected final static byte[] FILE_MONIKER = {0x03, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46};

    /**
     * Tail of a URL link
     */
    protected final static byte[] URL_TAIL = {0x79, 0x58, (byte)0x81, (byte)0xF4, 0x3B, 0x1D, 0x7F, 0x48, (byte)0xAF, 0x2C,
                                              (byte)0x82, 0x5D, (byte)0xC4, (byte)0x85, 0x27, 0x63, 0x00, 0x00, 0x00,
                                               0x00, (byte)0xA5, (byte)0xAB, 0x00, 0x00};

    /**
     * Tail of a file link
     */
    protected final static byte[] FILE_TAIL = {(byte)0xFF, (byte)0xFF, (byte)0xAD, (byte)0xDE, 0x00, 0x00, 0x00, 0x00,
                                                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public final static short sid = 0x1b8;

    /**
     * First row of the hyperlink
     */
    private int rwFirst;

    /**
     * Last row of the hyperlink
     */
    private int rwLast;

    /**
     * First column of the hyperlink
     */
    private short colFirst;

    /**
     * Last column of the hyperlink
     */
    private short colLast;

    /**
     * 16-byte GUID
     */
    private byte[] guid;

    /**
     * Some sort of options. Seems to always equal 2
     */
    private int label_opts;

    /**
     * Some sort of options for file links.
     */
    private short file_opts;

    /**
     * Link options. Can include any of HLINK_* flags.
     */
    private int link_opts;

    /**
     * Test label
     */
    private String label;

    /**
     * Moniker. Makes sense only for URL and file links
     */
    private byte[] moniker;

    /**
     * Link
     */
    private String address;

    /**
     * Remaining bytes
     */
    private byte[] tail;

    /**
     * Create a new hyperlink
     */
    public HyperlinkRecord()
    {

    }

    /**
     * Return the column of the first cell that contains the hyperlink
     *
     * @return the 0-based column of the first cell that contains the hyperlink
     */
   public short getFirstColumn()
    {
        return colFirst;
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    public void setFirstColumn(short col)
    {
        this.colFirst = col;
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
    */
    public short getLastColumn()
    {
        return colLast;
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    public void setLastColumn(short col)
    {
        this.colLast = col;
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the first cell that contains the hyperlink
     */
    public int getFirstRow()
    {
        return rwFirst;
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    public void setFirstRow(int row)
    {
        this.rwFirst = row;
    }

    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    public int getLastRow()
    {
        return rwLast;
    }

    /**
     * Set the row of the last cell that contains the hyperlink
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    public void setLastRow(int row)
    {
        this.rwLast = row;
    }

    /**
     * Returns a 16-byte guid identifier. Seems to always equal {@link STD_MONIKER}
     *
     * @return 16-byte guid identifier
     */
    public byte[] getGuid()
    {
        return guid;
    }

    /**
     * Returns a 16-byte moniker.
     *
     * @return 16-byte moniker
     */
    public byte[] getMoniker()
    {
        return moniker;
    }


    /**
     * Return text label for this hyperlink
     *
     * @return  text to display
     */
    public String getLabel()
    {
    	if(label == null) return null;
    	
        int idx = label.indexOf('\u0000');
        return idx == -1 ? label : label.substring(0, idx);
    }

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
     public void setLabel(String label)
    {
        this.label = label + '\u0000';
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress()
    {
    	if(address == null) return null;
    	
        int idx = address.indexOf('\u0000');
        return idx == -1 ? address : address.substring(0, idx);
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    public void setAddress(String address)
    {
        this.address = address + '\u0000';
    }

    /**
     * Link options. Must be a combination of HLINK_* constants.
     */
    public int getLinkOptions(){
        return link_opts;
    }

    /**
     * Label options
     */
    public int getLabelOptions(){
        return label_opts;
    }

    /**
     * Options for a file link
     */
    public int getFileOptions(){
        return file_opts;
    }

    public byte[] getTail(){
        return tail;
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public HyperlinkRecord(RecordInputStream in)
    {
        try {
            rwFirst = in.readShort();
            rwLast = in.readUShort();
            colFirst = in.readShort();
            colLast = in.readShort();

            // 16-byte GUID
            guid = new byte[16];
            in.read(guid);

            label_opts = in.readInt();
            link_opts = in.readInt();

            if ((link_opts & HLINK_LABEL) != 0){
                int label_len = in.readInt();
                label = in.readUnicodeLEString(label_len);
            }

            if ((link_opts & HLINK_URL) != 0){
                moniker = new byte[16];
                in.read(moniker);

                if(Arrays.equals(URL_MONIKER, moniker)){
                    int len = in.readInt();

                    address = in.readUnicodeLEString(len/2);

                    tail = in.readRemainder();
                } else if (Arrays.equals(FILE_MONIKER, moniker)){
                    file_opts = in.readShort();

                    int len = in.readInt();

                    byte[] path_bytes = new byte[len];
                    in.read(path_bytes);

                    address = new String(path_bytes);

                    tail = in.readRemainder();
                }
            } else if((link_opts & HLINK_PLACE) != 0){
                int len = in.readInt();
                address = in.readUnicodeLEString(len);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public short getSid()
    {
        return HyperlinkRecord.sid;
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = offset;
        LittleEndian.putShort(data, pos, sid); pos += 2;
        LittleEndian.putShort(data, pos, ( short )(getRecordSize()-4)); pos += 2;
        LittleEndian.putUShort(data, pos, rwFirst); pos += 2;
        LittleEndian.putUShort(data, pos, rwLast); pos += 2;
        LittleEndian.putShort(data, pos, colFirst); pos += 2;
        LittleEndian.putShort(data, pos, colLast); pos += 2;

        System.arraycopy(guid, 0, data, pos, guid.length); pos += guid.length;

        LittleEndian.putInt(data, pos, label_opts); pos += 4;
        LittleEndian.putInt(data, pos, link_opts); pos += 4;

        if ((link_opts & HLINK_LABEL) != 0){
            LittleEndian.putInt(data, pos, label.length()); pos += 4;
            StringUtil.putUnicodeLE(label, data, pos);  pos += label.length()*2;
        }
        if ((link_opts & HLINK_URL) != 0){
            System.arraycopy(moniker, 0, data, pos, moniker.length); pos += moniker.length;
            if(Arrays.equals(URL_MONIKER, moniker)){
                LittleEndian.putInt(data, pos, address.length()*2 + tail.length); pos += 4;
                StringUtil.putUnicodeLE(address, data, pos);  pos += address.length()*2;
                if(tail.length > 0){
                    System.arraycopy(tail, 0, data, pos, tail.length); pos += tail.length;
                }
            } else if (Arrays.equals(FILE_MONIKER, moniker)){
                LittleEndian.putShort(data, pos, file_opts); pos += 2;
                LittleEndian.putInt(data, pos, address.length()); pos += 4;
                byte[] bytes = address.getBytes();
                System.arraycopy(bytes, 0, data, pos, bytes.length); pos += bytes.length;
                if(tail.length > 0){
                    System.arraycopy(tail, 0, data, pos, tail.length); pos += tail.length;
                }
            }
        } else if((link_opts & HLINK_PLACE) != 0){
            LittleEndian.putInt(data, pos, address.length()); pos += 4;
            StringUtil.putUnicodeLE(address, data, pos);  pos += address.length()*2;
        }
    	return getRecordSize();
    }

    public int getRecordSize()
    {
        int size = 4;
        size += 2 + 2 + 2 + 2;  //rwFirst, rwLast, colFirst, colLast
        size += guid.length;
        size += 4;  //label_opts
        size += 4;  //link_opts
        if ((link_opts & HLINK_LABEL) != 0){
            size += 4;  //link length
            size += label.length()*2;
        }
        if ((link_opts & HLINK_URL) != 0){
            size += moniker.length;  //moniker length
            if(Arrays.equals(URL_MONIKER, moniker)){
                size += 4;  //address length
                size += address.length()*2;
                size += tail.length;
            } else if (Arrays.equals(FILE_MONIKER, moniker)){
                size += 2;  //file_opts
                size += 4;  //address length
                size += address.length();
                size += tail.length;
            }
        } else if((link_opts & HLINK_PLACE) != 0){
            size += 4;  //address length
            size += address.length()*2;
        }
        return size;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HYPERLINK RECORD]\n");
        buffer.append("    .rwFirst            = ").append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .rwLast         = ").append(Integer.toHexString(getLastRow())).append("\n");
        buffer.append("    .colFirst            = ").append(Integer.toHexString(getFirstColumn())).append("\n");
        buffer.append("    .colLast         = ").append(Integer.toHexString(getLastColumn())).append("\n");
        buffer.append("    .guid        = ").append(HexDump.toHex(guid)).append("\n");
        buffer.append("    .label_opts          = ").append(label_opts).append("\n");
        buffer.append("    .label          = ").append(getLabel()).append("\n");
        if((link_opts & HLINK_URL) != 0){
            buffer.append("    .moniker          = ").append(HexDump.toHex(moniker)).append("\n");
        }
        buffer.append("    .address            = ").append(getAddress()).append("\n");
        buffer.append("[/HYPERLINK RECORD]\n");
        return buffer.toString();
    }

    /**
     * Initialize a new url link
     */
    public void newUrlLink(){
        rwFirst = 0;
        rwLast = 0;
        colFirst = 0;
        colLast = 0;
        guid = STD_MONIKER;
        label_opts = 0x2;
        link_opts = HLINK_URL | HLINK_ABS | HLINK_LABEL;
        label = "" + '\u0000';
        moniker = URL_MONIKER;
        address = "" + '\u0000';
        tail = URL_TAIL;
    }

    /**
     * Initialize a new file link
     */
    public void newFileLink(){
        rwFirst = 0;
        rwLast = 0;
        colFirst = 0;
        colLast = 0;
        guid = STD_MONIKER;
        label_opts = 0x2;
        link_opts = HLINK_URL | HLINK_LABEL;
        file_opts = 0;
        label = "" + '\u0000';
        moniker = FILE_MONIKER;
        address = "" + '\0';
        tail = FILE_TAIL;
    }

    /**
     * Initialize a new document link
     */
    public void newDocumentLink(){
        rwFirst = 0;
        rwLast = 0;
        colFirst = 0;
        colLast = 0;
        guid = STD_MONIKER;
        label_opts = 0x2;
        link_opts = HLINK_LABEL | HLINK_PLACE;
        label = "" + '\u0000';
        moniker = FILE_MONIKER;
        address = "" + '\0';
        tail = new byte[]{};
    }

    public Object clone() {
        HyperlinkRecord rec = new HyperlinkRecord();
        rec.rwFirst = rwFirst;
        rec.rwLast = rwLast;
        rec.colFirst = colFirst;
        rec.colLast = colLast;
        rec.guid = guid;
        rec.label_opts = label_opts;
        rec.link_opts = link_opts;
        rec.file_opts = file_opts;
        rec.label = label;
        rec.address = address;
        rec.moniker = moniker;
        rec.tail = tail;
        return rec;
    }


}
