
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

/**
 * This class provides methods to read a DocumentEntry managed by a
 * Filesystem instance.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class DocumentInputStream
    extends InputStream
{

    // current offset into the Document
    private int              _current_offset;

    // current marked offset into the Document (used by mark and
    // reset)
    private int              _marked_offset;

    // the Document's size
    private int              _document_size;

    // have we been closed?
    private boolean          _closed;

    // the actual Document
    private POIFSDocument    _document;

    // buffer used to read one byte at a time
    private byte[]           _tiny_buffer;

    // returned by read operations if we're at end of document
    static private final int EOD = -1;

    /**
     * Create an InputStream from the specified DocumentEntry
     *
     * @param document the DocumentEntry to be read
     *
     * @exception IOException if the DocumentEntry cannot be opened
     *            (like, maybe it has been deleted?)
     */

    public DocumentInputStream(final DocumentEntry document)
        throws IOException
    {
        _current_offset = 0;
        _marked_offset  = 0;
        _document_size  = document.getSize();
        _closed         = false;
        _tiny_buffer    = null;
        if (document instanceof DocumentNode)
        {
            _document = (( DocumentNode ) document).getDocument();
        }
        else
        {
            throw new IOException("Cannot open internal document storage");
        }
    }

    /**
     * Create an InputStream from the specified Document
     *
     * @param document the Document to be read
     *
     * @exception IOException if the DocumentEntry cannot be opened
     *            (like, maybe it has been deleted?)
     */

    public DocumentInputStream(final POIFSDocument document)
        throws IOException
    {
        _current_offset = 0;
        _marked_offset  = 0;
        _document_size  = document.getSize();
        _closed         = false;
        _tiny_buffer    = null;
        _document       = document;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over)
     * from this input stream without blocking by the next caller of a
     * method for this input stream. The next caller might be the same
     * thread or or another thread.
     *
     * @return the number of bytes that can be read from this input
     *         stream without blocking.
     *
     * @exception IOException on error (such as the stream has been
     *            closed)
     */

    public int available()
        throws IOException
    {
        dieIfClosed();
        return _document_size - _current_offset;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     *
     * @exception IOException
     */

    public void close()
        throws IOException
    {
        _closed = true;
    }

    /**
     * Marks the current position in this input stream. A subsequent
     * call to the reset method repositions this stream at the last
     * marked position so that subsequent reads re-read the same
     * bytes.
     * <p>
     * The readlimit arguments tells this input stream to allow that
     * many bytes to be read before the mark position gets
     * invalidated. This implementation, however, does not care.
     * <p>
     * The general contract of mark is that, if the method
     * markSupported returns true, the stream somehow remembers all
     * the bytes read after the call to mark and stands ready to
     * supply those same bytes again if and whenever the method reset
     * is called. However, the stream is not required to remember any
     * data at all if more than readlimit bytes are read from the
     * stream before reset is called. But this stream will.
     *
     * @param ignoredReadlimit the maximum limit of bytes that can be
     *                         read before the mark position becomes
     *                         invalid. Ignored by this
     *                         implementation.
     */

    public void mark(int ignoredReadlimit)
    {
        _marked_offset = _current_offset;
    }

    /**
     * Tests if this input stream supports the mark and reset methods.
     *
     * @return true
     */

    public boolean markSupported()
    {
        return true;
    }

    /**
     * Reads the next byte of data from the input stream. The value
     * byte is returned as an int in the range 0 to 255. If no byte is
     * available because the end of the stream has been reached, the
     * value -1 is returned. The definition of this method in
     * java.io.InputStream allows this method to block, but it won't.
     *
     * @return the next byte of data, or -1 if the end of the stream
     *         is reached.
     *
     * @exception IOException
     */

    public int read()
        throws IOException
    {
        dieIfClosed();
        if (atEOD())
        {
            return EOD;
        }
        if (_tiny_buffer == null)
        {
            _tiny_buffer = new byte[ 1 ];
        }
        _document.read(_tiny_buffer, _current_offset++);
        return ((int)_tiny_buffer[ 0 ]) & 0x000000FF;
    }

    /**
     * Reads some number of bytes from the input stream and stores
     * them into the buffer array b. The number of bytes actually read
     * is returned as an integer. The definition of this method in
     * java.io.InputStream allows this method to block, but it won't.
     * <p>
     * If b is null, a NullPointerException is thrown. If the length
     * of b is zero, then no bytes are read and 0 is returned;
     * otherwise, there is an attempt to read at least one byte. If no
     * byte is available because the stream is at end of file, the
     * value -1 is returned; otherwise, at least one byte is read and
     * stored into b.
     * <p>
     * The first byte read is stored into element b[0], the next one
     * into b[1], and so on. The number of bytes read is, at most,
     * equal to the length of b. Let k be the number of bytes actually
     * read; these bytes will be stored in elements b[0] through
     * b[k-1], leaving elements b[k] through b[b.length-1] unaffected.
     * <p>
     * If the first byte cannot be read for any reason other than end
     * of file, then an IOException is thrown. In particular, an
     * IOException is thrown if the input stream has been closed.
     * <p>
     * The read(b) method for class InputStream has the same effect as:
     * <p>
     * <code>read(b, 0, b.length)</code>
     *
     * @param b the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1
     *         if there is no more data because the end of the stream
     *         has been reached.
     *
     * @exception IOException
     * @exception NullPointerException
     */

    public int read(final byte [] b)
        throws IOException, NullPointerException
    {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to len bytes of data from the input stream into an
     * array of bytes. An attempt is made to read as many as len
     * bytes, but a smaller number may be read, possibly zero. The
     * number of bytes actually read is returned as an integer.
     * <p>
     * The definition of this method in java.io.InputStream allows it
     * to block, but it won't.
     * <p>
     * If b is null, a NullPointerException is thrown.
     * <p>
     * If off is negative, or len is negative, or off+len is greater
     * than the length of the array b, then an
     * IndexOutOfBoundsException is thrown.
     * <p>
     * If len is zero, then no bytes are read and 0 is returned;
     * otherwise, there is an attempt to read at least one byte. If no
     * byte is available because the stream is at end of file, the
     * value -1 is returned; otherwise, at least one byte is read and
     * stored into b.
     * <p>
     * The first byte read is stored into element b[off], the next one
     * into b[off+1], and so on. The number of bytes read is, at most,
     * equal to len. Let k be the number of bytes actually read; these
     * bytes will be stored in elements b[off] through b[off+k-1],
     * leaving elements b[off+k] through b[off+len-1] unaffected.
     * <p>
     * In every case, elements b[0] through b[off] and elements
     * b[off+len] through b[b.length-1] are unaffected.
     * <p>
     * If the first byte cannot be read for any reason other than end
     * of file, then an IOException is thrown. In particular, an
     * IOException is thrown if the input stream has been closed.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b at which the data is
     *            written.
     * @param len the maximum number of bytes to read.
     *
     * @return the total number of bytes read into the buffer, or -1
     *         if there is no more data because the end of the stream
     *         has been reached.
     *
     * @exception IOException
     * @exception NullPointerException
     * @exception IndexOutOfBoundsException
     */

    public int read(final byte [] b, final int off, final int len)
        throws IOException, NullPointerException, IndexOutOfBoundsException
    {
        dieIfClosed();
        if (b == null)
        {
            throw new NullPointerException("buffer is null");
        }
        if ((off < 0) || (len < 0) || (b.length < (off + len)))
        {
            throw new IndexOutOfBoundsException(
                "can't read past buffer boundaries");
        }
        if (len == 0)
        {
            return 0;
        }
        if (atEOD())
        {
            return EOD;
        }
        int limit = Math.min(available(), len);

        if ((off == 0) && (limit == b.length))
        {
            _document.read(b, _current_offset);
        }
        else
        {
            byte[] buffer = new byte[ limit ];

            _document.read(buffer, _current_offset);
            System.arraycopy(buffer, 0, b, off, limit);
        }
        _current_offset += limit;
        return limit;
    }

    /**
     * Repositions this stream to the position at the time the mark
     * method was last called on this input stream.
     * <p>
     * The general contract of reset is:
     * <p>
     * <ul>
     *    <li>
     *        If the method markSupported returns true, then:
     *        <ul>
     *            <li>
     *                If the method mark has not been called since the
     *                stream was created, or the number of bytes read
     *                from the stream since mark was last called is
     *                larger than the argument to mark at that last
     *                call, then an IOException might be thrown.
     *            </li>
     *            <li>
     *                If such an IOException is not thrown, then the
     *                stream is reset to a state such that all the
     *                bytes read since the most recent call to mark
     *                (or since the start of the file, if mark has not
     *                been called) will be resupplied to subsequent
     *                callers of the read method, followed by any
     *                bytes that otherwise would have been the next
     *                input data as of the time of the call to reset.
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         If the method markSupported returns false, then:
     *         <ul>
     *             <li>
     *                 The call to reset may throw an IOException.
     *             </li>
     *             <li>
     *                 If an IOException is not thrown, then the
     *                 stream is reset to a fixed state that depends
     *                 on the particular type of the input and how it
     *                 was created. The bytes that will be supplied to
     *                 subsequent callers of the read method depend on
     *                 the particular type of the input stream.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * <p>
     * All well and good ... this class's markSupported method returns
     * true and this method does not care whether you've called mark
     * at all, or whether you've exceeded the number of bytes
     * specified in the last call to mark. We're basically walking a
     * byte array ... mark and reset to your heart's content.
     */

    public void reset()
    {
        _current_offset = _marked_offset;
    }

    /**
     * Skips over and discards n bytes of data from this input
     * stream. The skip method may, for a variety of reasons, end up
     * skipping over some smaller number of bytes, possibly 0. This
     * may result from any of a number of conditions; reaching end of
     * file before n bytes have been skipped is only one
     * possibility. The actual number of bytes skipped is returned. If
     * n is negative, no bytes are skipped.
     *
     * @param n the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     *
     * @exception IOException
     */

    public long skip(final long n)
        throws IOException
    {
        dieIfClosed();
        if (n < 0)
        {
            return 0;
        }
        int new_offset = _current_offset + ( int ) n;

        if (new_offset < _current_offset)
        {

            // wrap around in converting a VERY large long to an int
            new_offset = _document_size;
        }
        else if (new_offset > _document_size)
        {
            new_offset = _document_size;
        }
        long rval = new_offset - _current_offset;

        _current_offset = new_offset;
        return rval;
    }

    private void dieIfClosed()
        throws IOException
    {
        if (_closed)
        {
            throw new IOException(
                "cannot perform requested operation on a closed stream");
        }
    }

    private boolean atEOD()
    {
        return _current_offset == _document_size;
    }
}   // end public class DocumentInputStream

