
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.poifs.filesystem;

import java.io.*;

import java.util.*;

/**
 * This class provides a wrapper over an OutputStream so that Document
 * writers can't accidently go over their size limits
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class DocumentOutputStream
    extends OutputStream
{
    private OutputStream stream;
    private int          limit;
    private int          written;

    /**
     * Create a DocumentOutputStream
     *
     * @param stream the OutputStream to which the data is actually
     *               read
     * @param limit the maximum number of bytes that can be written
     */

    DocumentOutputStream(final OutputStream stream, final int limit)
    {
        this.stream  = stream;
        this.limit   = limit;
        this.written = 0;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for write is that one byte is written to the output
     * stream. The byte to be written is the eight low-order bits of
     * the argument b. The 24 high-order bits of b are ignored.
     *
     * @param b the byte.
     * @exception IOException if an I/O error occurs. In particular,
     *                        an IOException may be thrown if the
     *                        output stream has been closed, or if the
     *                        writer tries to write too much data.
     */

    public void write(final int b)
        throws IOException
    {
        limitCheck(1);
        stream.write(b);
    }

    /**
     * Writes b.length bytes from the specified byte array
     * to this output stream.
     *
     * @param b the data.
     * @exception IOException if an I/O error occurs.
     */

    public void write(final byte b[])
        throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * Writes len bytes from the specified byte array starting at
     * offset off to this output stream.  The general contract for
     * write(b, off, len) is that some of the bytes in the array b are
     * written to the output stream in order; element b[off] is the
     * first byte written and b[off+len-1] is the last byte written by
     * this operation.<p>
     * If b is null, a NullPointerException is thrown.<p>
     * If off is negative, or len is negative, or off+len is greater
     * than the length of the array b, then an
     * IndexOutOfBoundsException is thrown.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @exception IOException if an I/O error occurs. In particular,
     *                        an IOException</code> is thrown if the
     *                        output stream is closed or if the writer
     *                        tries to write too many bytes.
     */

    public void write(final byte b[], final int off, final int len)
        throws IOException
    {
        limitCheck(len);
        stream.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out.
     *
     * @exception IOException if an I/O error occurs.
     */

    public void flush()
        throws IOException
    {
        stream.flush();
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of close is
     * that it closes the output stream. A closed stream cannot
     * perform output operations and cannot be reopened.
     *
     * @exception IOException if an I/O error occurs.
     */

    public void close()
        throws IOException
    {

        // ignore this call
    }

    /**
     * write the rest of the document's data (fill in at the end)
     *
     * @param totalLimit the actual number of bytes the corresponding
     *                   document must fill
     * @param fill the byte to fill remaining space with
     *
     * @exception IOException on I/O error
     */

    void writeFiller(final int totalLimit, final byte fill)
        throws IOException
    {
        if (totalLimit > written)
        {
            byte[] filler = new byte[ totalLimit - written ];

            Arrays.fill(filler, fill);
            stream.write(filler);
        }
    }

    private void limitCheck(final int toBeWritten)
        throws IOException
    {
        if ((written + toBeWritten) > limit)
        {
            throw new IOException("tried to write too much data");
        }
        written += toBeWritten;
    }
}   // end public class DocumentOutputStream

