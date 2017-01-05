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

package org.apache.poi.hpsf;

import java.util.Arrays;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.StringUtil;

/**
 *  <p>Represents a class ID (16 bytes). Unlike other little-endian
 *  type the {@link ClassID} is not just 16 bytes stored in the wrong
 *  order. Instead, it is a double word (4 bytes) followed by two
 *  words (2 bytes each) followed by 8 bytes.</p>
 */
public class ClassID
{
    public static final ClassID OLE10_PACKAGE  = new ClassID("{0003000C-0000-0000-C000-000000000046}");
    public static final ClassID PPT_SHOW       = new ClassID("{64818D10-4F9B-11CF-86EA-00AA00B929E8}");
    public static final ClassID XLS_WORKBOOK   = new ClassID("{00020841-0000-0000-C000-000000000046}");
    public static final ClassID TXT_ONLY       = new ClassID("{5e941d80-bf96-11cd-b579-08002b30bfeb}");

    // Excel V3
    public static final ClassID EXCEL_V3       = new ClassID("{00030000-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL_V3_CHART = new ClassID("{00030001-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL_V3_MACRO = new ClassID("{00030002-0000-0000-C000-000000000046}");
    // Excel V5
    public static final ClassID EXCEL95        = new ClassID("{00020810-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL95_CHART  = new ClassID("{00020811-0000-0000-C000-000000000046}");
    // Excel V8
    public static final ClassID EXCEL97        = new ClassID("{00020820-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL97_CHART  = new ClassID("{00020821-0000-0000-C000-000000000046}");
    // Excel V11
    public static final ClassID EXCEL2003      = new ClassID("{00020812-0000-0000-C000-000000000046}");
    // Excel V12
    public static final ClassID EXCEL2007      = new ClassID("{00020830-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL2007_MACRO= new ClassID("{00020832-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL2007_XLSB = new ClassID("{00020833-0000-0000-C000-000000000046}");
    // Excel V14
    public static final ClassID EXCEL2010      = new ClassID("{00024500-0000-0000-C000-000000000046}");
    public static final ClassID EXCEL2010_CHART= new ClassID("{00024505-0014-0000-C000-000000000046}");
    public static final ClassID EXCEL2010_ODS  = new ClassID("{EABCECDB-CC1C-4A6F-B4E3-7F888A5ADFC8}");
    
    public static final ClassID WORD97         = new ClassID("{00020906-0000-0000-C000-000000000046}");
    public static final ClassID WORD95         = new ClassID("{00020900-0000-0000-C000-000000000046}");
    public static final ClassID WORD2007       = new ClassID("{F4754C9B-64F5-4B40-8AF4-679732AC0607}");
    public static final ClassID WORD2007_MACRO = new ClassID("{18A06B6B-2F3F-4E2B-A611-52BE631B2D22}");
    
    public static final ClassID POWERPOINT97   = new ClassID("{64818D10-4F9B-11CF-86EA-00AA00B929E8}");
    public static final ClassID POWERPOINT95   = new ClassID("{EA7BAE70-FB3B-11CD-A903-00AA00510EA3}");
    public static final ClassID POWERPOINT2007 = new ClassID("{CF4F55F4-8F87-4D47-80BB-5808164BB3F8}");
    public static final ClassID POWERPOINT2007_MACRO = new ClassID("{DC020317-E6E2-4A62-B9FA-B3EFE16626F4}");
    
    public static final ClassID EQUATION30     = new ClassID("{0002CE02-0000-0000-C000-000000000046}");
	
    /** <p>The number of bytes occupied by this object in the byte
     * stream.</p> */
    public static final int LENGTH = 16;
	
    /**
     * <p>The bytes making out the class ID in correct order,
     * i.e. big-endian.</p>
     */
    private final byte[] bytes = new byte[LENGTH];



    /**
     *  <p>Creates a {@link ClassID} and reads its value from a byte
     *  array.</p>
     *
     * @param src The byte array to read from.
     * @param offset The offset of the first byte to read.
     */
    public ClassID(final byte[] src, final int offset) {
        read(src, offset);
    }


    /**
     *  <p>Creates a {@link ClassID} and initializes its value with
     *  0x00 bytes.</p>
     */
    public ClassID() {
        Arrays.fill(bytes, (byte)0);
    }


    /**
     * <p>Creates a {@link ClassID} from a human-readable representation of the Class ID in standard 
     * format <code>"{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}"</code>.</p>
     * 
     * @param externalForm representation of the Class ID represented by this object.
     */
    public ClassID(String externalForm) {
        String clsStr = externalForm.replaceAll("[{}-]", "");
        for (int i=0; i<clsStr.length(); i+=2) {
        	bytes[i/2] = (byte)Integer.parseInt(clsStr.substring(i, i+2), 16);
        }
    }
    

    /**
     * @return The number of bytes occupied by this object in the byte
     * stream.
     */
    public int length() {
        return LENGTH;
    }



    /**
     * <p>Gets the bytes making out the class ID. They are returned in
     * correct order, i.e. big-endian.</p>
     *
     * @return the bytes making out the class ID.
     */
    public byte[] getBytes() {
        return bytes;
    }



    /**
     * <p>Sets the bytes making out the class ID.</p>
     *
     * @param bytes The bytes making out the class ID in big-endian format. They
     * are copied without their order being changed.
     */
    public void setBytes(final byte[] bytes) {
        System.arraycopy(bytes, 0, this.bytes, 0, LENGTH);
    }



    /**
     * <p>Reads the class ID's value from a byte array by turning
     * little-endian into big-endian.</p>
     *
     * @param src The byte array to read from
     *
     * @param offset The offset within the <var>src</var> byte array
     *
     * @return A byte array containing the class ID.
     */
    public byte[] read(final byte[] src, final int offset) {
        /* Read double word. */
        bytes[0] = src[3 + offset];
        bytes[1] = src[2 + offset];
        bytes[2] = src[1 + offset];
        bytes[3] = src[0 + offset];

        /* Read first word. */
        bytes[4] = src[5 + offset];
        bytes[5] = src[4 + offset];

        /* Read second word. */
        bytes[6] = src[7 + offset];
        bytes[7] = src[6 + offset];

        /* Read 8 bytes. */
        System.arraycopy(src, 8 + offset, bytes, 8, 8);

        return bytes;
    }



    /**
     * <p>Writes the class ID to a byte array in the
     * little-endian format.</p>
     *
     * @param dst The byte array to write to.
     *
     * @param offset The offset within the <var>dst</var> byte array.
     *
     * @exception ArrayStoreException if there is not enough room for the class
     * ID 16 bytes in the byte array after the <var>offset</var> position.
     */
    public void write(final byte[] dst, final int offset)
    throws ArrayStoreException {
        /* Check array size: */
        if (dst.length < LENGTH) {
            throw new ArrayStoreException
                ("Destination byte[] must have room for at least 16 bytes, " +
                 "but has a length of only " + dst.length + ".");
        }
        
        /* Write double word. */
        dst[0 + offset] = bytes[3];
        dst[1 + offset] = bytes[2];
        dst[2 + offset] = bytes[1];
        dst[3 + offset] = bytes[0];

        /* Write first word. */
        dst[4 + offset] = bytes[5];
        dst[5 + offset] = bytes[4];

        /* Write second word. */
        dst[6 + offset] = bytes[7];
        dst[7 + offset] = bytes[6];

        /* Write 8 bytes. */
        System.arraycopy(bytes, 8, dst, 8 + offset, 8);
    }



    /**
     * <p>Checks whether this <code>ClassID</code> is equal to another
     * object.</p>
     *
     * @param o the object to compare this <code>PropertySet</code> with
     * @return <code>true</code> if the objects are equal, else
     * <code>false</code>.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof ClassID)) {
            return false;
        }
        final ClassID cid = (ClassID) o;
        if (bytes.length != cid.bytes.length) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != cid.bytes[i]) {
                return false;
            }
        }
        return true;
    }



    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new String(bytes, StringUtil.UTF8).hashCode();
    }

    /**
     * <p>Returns a human-readable representation of the Class ID in standard 
     * format <code>"{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}"</code>.</p>
     * 
     * @return String representation of the Class ID represented by this object.
     */
    @Override
    public String toString() {
        StringBuilder sbClassId = new StringBuilder(38);
        sbClassId.append('{');
        for (int i = 0; i < LENGTH; i++) {
            sbClassId.append(HexDump.toHex(bytes[i]));
            if (i == 3 || i == 5 || i == 7 || i == 9) {
                sbClassId.append('-');
            }
        }
        sbClassId.append('}');
        return sbClassId.toString();
    }
}
