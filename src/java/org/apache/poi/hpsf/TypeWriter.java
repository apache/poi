/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *  Portions of this software are based upon public domain software
 *  originally written at the National Center for Supercomputing Applications,
 *  University of Illinois, Urbana-Champaign.
 *
 *  Portions of this software are based upon public domain software
 *  originally written at the National Center for Supercomputing Applications,
 *  University of Illinois, Urbana-Champaign.
 */
package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

/**
 * <p>Class for writing little-endian data and more.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @version $Id$
 * @since 2003-02-20
 */
public class TypeWriter
{

    /**
     * <p>Writes a two-byte value (short) to an output stream.</p>
     *
     * @param out The stream to write to.
     * @param n The value to write.
     * @return The number of bytes that have been written.
     * @exception IOException if an I/O error occurs
     */
    public static int writeToStream(final OutputStream out, final short n)
        throws IOException
    {
        final int length = LittleEndian.SHORT_SIZE;
        byte[] buffer = new byte[length];
        LittleEndian.putUShort(buffer, 0, n);
        out.write(buffer, 0, length);
        return length;
    }



    /**
     * <p>Writes a four-byte value to an output stream.</p>
     *
     * @param out The stream to write to.
     * @param n The value to write.
     * @exception IOException if an I/O error occurs
     * @return The number of bytes written to the output stream. 
     */
    public static int writeToStream(final OutputStream out, final int n)
        throws IOException
    {
        final int l = LittleEndian.INT_SIZE;
        final byte[] buffer = new byte[l];
        LittleEndian.putInt(buffer, 0, n);
        out.write(buffer, 0, l);
        return l;
        
    }



    /**
     * <p>Writes an unsigned two-byte value to an output stream.</p>
     *
     * @param out The stream to write to
     * @param n The value to write
     * @exception IOException if an I/O error occurs
     */
    public static void writeUShortToStream(final OutputStream out, final int n)
        throws IOException
    {
        int high = n & 0xFFFF0000;
        if (high != 0)
            throw new IllegalPropertySetDataException
                ("Value " + n + " cannot be represented by 2 bytes.");
        writeToStream(out, (short) n);
    }



    /**
     * <p>Writes an unsigned four-byte value to an output stream.</p>
     *
     * @param out The stream to write to.
     * @param n The value to write.
     * @return The number of bytes that have been written to the output stream.
     * @exception IOException if an I/O error occurs
     */
    public static int writeUIntToStream(final OutputStream out, final long n)
        throws IOException
    {
        long high = n & 0xFFFFFFFF00000000L;
        if (high != 0 && high != 0xFFFFFFFF00000000L)
            throw new IllegalPropertySetDataException
                ("Value " + n + " cannot be represented by 4 bytes.");
        return writeToStream(out, (int) n);
    }



    /**
     * <p>Writes a 16-byte {@link ClassID} to an output stream.</p>
     *
     * @param out The stream to write to
     * @param n The value to write
     * @exception IOException if an I/O error occurs
     */
    public static int writeToStream(final OutputStream out, final ClassID n)
        throws IOException
    {
        byte[] b = new byte[16];
        n.write(b, 0);
        out.write(b, 0, b.length);
        return b.length;
    }



    /**
     * <p>Writes an array of {@link Property} instances to an output stream
     * according to the Horrible Property Stream Format.</p>
     *
     * @param out The stream to write to
     * @param properties The array to write to the stream
     * @exception IOException if an I/O error occurs
     */
    public static void writeToStream(final OutputStream out,
                                     final Property[] properties,
                                     final int codepage)
        throws IOException, UnsupportedVariantTypeException
    {
        /* If there are no properties don't write anything. */
        if (properties == null)
            return;

        /* Write the property list. This is a list containing pairs of property
         * ID and offset into the stream. */
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = (Property) properties[i];
            writeUIntToStream(out, p.getID());
            writeUIntToStream(out, p.getSize());
        }

        /* Write the properties themselves. */
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = (Property) properties[i];
            long type = p.getType();
            writeUIntToStream(out, type);
            VariantSupport.write(out, (int) type, p.getValue(), codepage);
        }
    }







}
