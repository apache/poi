/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hssf.record;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * DConRef records specify a range in a workbook (internal or external) that serves as a data source
 * for pivot tables or data consolidation.
 *
 * Represents a {@code DConRef} Structure
 * <a href="http://msdn.microsoft.com/en-us/library/dd923854(office.12).aspx">[MS-XLS s.
 * 2.4.86]</a>, and the contained {@code DConFile} structure
 * <a href="http://msdn.microsoft.com/en-us/library/dd950157(office.12).aspx">
 * [MS-XLS s. 2.5.69]</a>. This in turn contains a {@code XLUnicodeStringNoCch}
 * <a href="http://msdn.microsoft.com/en-us/library/dd910585(office.12).aspx">
 * [MS-XLS s. 2.5.296]</a>.
 *
 * <pre>
 *         _______________________________
 *        |          DConRef              |
 *(bytes) +-+-+-+-+-+-+-+-+-+-+...+-+-+-+-+
 *        |    ref    |cch|  stFile   | un|
 *        +-+-+-+-+-+-+-+-+-+-+...+-+-+-+-+
 *                              |
 *                     _________|_____________________
 *                    |DConFile / XLUnicodeStringNoCch|
 *                    +-+-+-+-+-+-+-+-+-+-+-+...+-+-+-+
 *             (bits) |h|   reserved  |      rgb      |
 *                    +-+-+-+-+-+-+-+-+-+-+-+...+-+-+-+
 * </pre>
 * Where
 * <ul>
 * <li>{@code DConFile.h = 0x00} if the characters in{@code rgb} are single byte, and
 * {@code DConFile.h = 0x01} if they are double byte.<p>
 * If they are double byte, then
 * <ul>
 * <li> If it exists, the length of {@code DConRef.un = 2}. Otherwise it is 1.
 * <li> The length of {@code DConFile.rgb = (2 * DConRef.cch)}. Otherwise it is equal to
 * {@code DConRef.cch}.
 * </ul>
 * <li>{@code DConRef.rgb} starts with {@code 0x01} if it is an external reference,
 * and with {@code 0x02} if it is a self-reference.
 * </ul>
 *
 * At the moment this class is read-only.
 */
public class DConRefRecord extends StandardRecord {

    /**
     * The id of the record type,
     * <code>sid = {@value}</code>
     */
    public static final short sid = 0x0051;
    /**
     * A RefU structure specifying the range of cells if this record is part of an SXTBL.
     * <a href="http://msdn.microsoft.com/en-us/library/dd920420(office.12).aspx">
     * [MS XLS s.2.5.211]</a>
     */
    private final int firstRow;
    private final int lastRow;
    private final int firstCol;
    private final int lastCol;
    /**
     * the number of chars in the link
     */
    private final int charCount;
    /**
     * the type of characters (single or double byte)
     */
    private final int charType;
    /**
     * The link's path string. This is the {@code rgb} field of a
     * {@code XLUnicodeStringNoCch}. Therefore it will contain at least one leading special
     * character (0x01 or 0x02) and probably other ones.<p>
     * @see <A href="http://msdn.microsoft.com/en-us/library/dd923491(office.12).aspx">
     * DConFile [MS-XLS s. 2.5.77]</A> and
     * <A href="http://msdn.microsoft.com/en-us/library/dd950157(office.12).aspx">
     * VirtualPath [MS-XLS s. 2.5.69]</a>
     */
    private final byte[] path;
    /**
     * unused bits at the end, must be set to 0.
     */
    private byte[] _unused;

    public DConRefRecord(DConRefRecord other) {
        super(other);
        firstCol = other.firstCol;
        firstRow = other.firstRow;
        lastCol = other.lastCol;
        lastRow = other.lastRow;
        charCount = other.charCount;
        charType = other.charType;
        path = (other.path == null) ? null : other.path.clone();
        _unused = (other._unused == null) ? null : other._unused.clone();
    }

    /**
     * Read constructor.
     *
     * @param data byte array containing a DConRef Record, including the header.
     */
    public DConRefRecord(byte[] data) {
        this(bytesToRIStream(data));
    }

    /**
     * Read Constructor.
     *
     * @param inStream RecordInputStream containing a DConRefRecord structure.
     */
    public DConRefRecord(RecordInputStream inStream) {
        if (inStream.getSid() != sid) {
            throw new RecordFormatException("Wrong sid: " + inStream.getSid());
        }

        firstRow = inStream.readUShort();
        lastRow = inStream.readUShort();
        firstCol = inStream.readUByte();
        lastCol = inStream.readUByte();

        charCount = inStream.readUShort();

        // 7 bits reserved + 1 bit type - first bit only
        charType = inStream.readUByte() & 0x01;

        // bytelength is the length of the string in bytes, which depends on whether the string is
        // made of single- or double-byte chars. This is given by charType, which equals 0 if
        // single-byte, 1 if double-byte.
        final int byteLength = charCount * (charType + 1);

        path = IOUtils.safelyAllocate(byteLength, HSSFWorkbook.getMaxRecordLength());
        inStream.readFully(path);

        // If it's a self reference, the last one or two bytes (depending on char type) are the
        // unused field. Not sure If i need to bother with this...
        if (path[0] == 0x02) {
            _unused = inStream.readRemainder();
        }
    }

    /*
     * assuming this wants the number of bytes returned by {@link serialize(LittleEndianOutput)},
     * that is, (length - 4).
     */
    @Override
    protected int getDataSize()
    {
        int sz = 9 + path.length;
        if (path[0] == 0x02)
            sz += _unused.length;
        return sz;
    }

    @Override
    protected void serialize(LittleEndianOutput out)
    {
        out.writeShort(firstRow);
        out.writeShort(lastRow);
        out.writeByte(firstCol);
        out.writeByte(lastCol);
        out.writeShort(charCount);
        out.writeByte(charType);
        out.write(path);
        if (path[0] == 0x02)
            out.write(_unused);
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    /**
     * @return The first column of the range.
     */
    public int getFirstColumn()
    {
        return firstCol;
    }

    /**
     * @return The first row of the range.
     */
    public int getFirstRow()
    {
        return firstRow;
    }

    /**
     * @return The last column of the range.
     */
    public int getLastColumn()
    {
        return lastCol;
    }

    /**
     * @return The last row of the range.
     */
    public int getLastRow()
    {
        return lastRow;
    }

    /**
     *
     * @return raw path byte array.
     */
    public byte[] getPath()
    {
        return Arrays.copyOf(path, path.length);
    }

    /**
     * @return the link's path, with the special characters stripped/replaced. May be null.
     * See MS-XLS 2.5.277 (VirtualPath)
     */
    public String getReadablePath()
    {
        if (path != null)
        {
            //all of the path strings start with either 0x02 or 0x01 followed by zero or
            //more of 0x01..0x08
            int offset = 1;
            while (offset < path.length && path[offset] < 0x20) {
                offset++;
            }
            String out = new String(Arrays.copyOfRange(path, offset, path.length), StringUtil.UTF8);
            //UNC paths have \u0003 chars as path separators.
            out = out.replace("\u0003", "/");
            return out;
        }
        return null;
    }

    /**
     * Checks if the data source in this reference record is external to this sheet or internal.
     *
     * @return true iff this is an external reference.
     */
    public boolean isExternalRef()
    {
        return path[0] == 0x01;
    }

    @Override
    public DConRefRecord copy() {
        return new DConRefRecord(this);
    }

    private static RecordInputStream bytesToRIStream(byte[] data) {
        RecordInputStream ric = new RecordInputStream(new UnsynchronizedByteArrayInputStream(data));
        ric.nextRecord();
        return ric;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DCON_REF;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "firstRow", this::getFirstRow,
            "lastRow", this::getLastRow,
            "firstColumn", this::getFirstColumn,
            "lastColumn", this::getLastColumn,
            "charCount", () -> charCount,
            "charType", () -> charType,
            "path", this::getReadablePath
        );
    }
}
