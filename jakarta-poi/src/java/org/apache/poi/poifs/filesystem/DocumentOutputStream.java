
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

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

