/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 *
 */
package org.apache.poi.hpsf;

import java.io.*;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 *  <p>Represents a class ID (16 bytes). Unlike other little-endian
 *  type the {@link ClassID} is not just 16 bytes stored in the wrong
 *  order. Instead, it is a double word (4 bytes) followed by two
 *  words (2 bytes each) followed by 8 bytes.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @version $Id$
 * @since 2002-02-09
 */
public class ClassID
{

    /**
     * <p>The bytes making out the class ID in correct order,
     * i.e. big-endian.</p>
     */
    protected byte[] bytes;



    /**
     *  <p>Creates a {@link ClassID} and reads its value from a byte
     *  array.</p>
     *
     * @param src The byte array to read from.
     * @param offset The offset of the first byte to read.
     */
    public ClassID(final byte[] src, final int offset)
    {
        read(src, offset);
    }


    /**
     *  <p>Creates a {@link ClassID} and initializes its value with
     *  0x00 bytes.</p>
     */
    public ClassID()
    {
        bytes = new byte[LENGTH];
        for (int i = 0; i < LENGTH; i++)
            bytes[i] = 0x00;
    }



    /** <p>The number of bytes occupied by this object in the byte
     * stream.</p> */
    public static final int LENGTH = 16;

    /**
     * @return The number of bytes occupied by this object in the byte
     * stream.
     */
    public int length()
    {
        return LENGTH;
    }



    /**
     * <p>Gets the bytes making out the class ID. They are returned in
     * correct order, i.e. big-endian.</p>
     *
     * @return the bytes making out the class ID.
     */
    public byte[] getBytes()
    {
        return bytes;
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
    public byte[] read(final byte[] src, final int offset)
    {
        bytes = new byte[16];

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
        for (int i = 8; i < 16; i++)
            bytes[i] = src[i + offset];

        return bytes;
    }



    /**
     * <p>Writes the class ID to a byte array in the
     * little-endian.</p>
     *
     * @param dst The byte array to write to.
     *
     * @param offset The offset within the <var>dst</var> byte array.
     *
     * @exception ArrayStoreException if there is not enough room for the class
     * ID 16 bytes in the byte array after the <var>offset</var> position.
     */
    public void write(final byte[] dst, final int offset)
    throws ArrayStoreException
    {
        /* Check array size: */
        if (dst.length < 16)
            throw new ArrayStoreException
                ("Destination byte[] must have room for at least 16 bytes, " +
                 "but has a length of only " + dst.length + ".");
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
        for (int i = 8; i < 16; i++)
            dst[i + offset] = bytes[i];
    }



    /**
     * <p>Checks whether this <code>ClassID</code> is equal to another
     * object.</p>
     *
     * @param o the object to compare this <code>PropertySet</code> with
     * @return <code>true</code> if the objects are equal, else
     * <code>false</code>.</p>
     */
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof ClassID))
            return false;
        final ClassID cid = (ClassID) o;
        if (bytes.length != cid.bytes.length)
            return false;
        for (int i = 0; i < bytes.length; i++)
            if (bytes[i] != cid.bytes[i])
                return false;
        return true;
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return new String(bytes).hashCode();
    }



    /**
     * <p>Returns a human-readable representation of the Class ID in standard 
     * format <code>"{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}"</code>.</p>
     * 
     * @return String representation of the Class ID represented by this object.
     */
    public String toString()
    {
        StringBuffer sbClassId = new StringBuffer(38);
        sbClassId.append('{');
        for (int i = 0; i < 16; i++)
        {
            sbClassId.append(HexDump.toHex(bytes[i]));
            if (i == 3 || i == 5 || i == 7 || i == 9)
                sbClassId.append('-');
        }
        sbClassId.append('}');
        return sbClassId.toString();
    }

}
