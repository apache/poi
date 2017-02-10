
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


package org.apache.poi.poifs.filesystem;

import java.io.*;

import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.util.CloseIgnoringInputStream;

/**
 * Transition class for the move from {@link POIFSFileSystem} to 
 *  {@link OPOIFSFileSystem}, and from {@link NPOIFSFileSystem} to
 *  {@link POIFSFileSystem}. 
 * <p>This has been updated to be powered by the NIO-based NPOIFS
 *  {@link NPOIFSFileSystem}.
 */
public class POIFSFileSystem
    extends NPOIFSFileSystem // TODO Temporary workaround during #56791
    implements POIFSViewable
{
    /**
     * Convenience method for clients that want to avoid the auto-close behaviour of the constructor.
     */
    public static InputStream createNonClosingInputStream(InputStream is) {
        return new CloseIgnoringInputStream(is);
    }

    /**
     * Constructor, intended for writing
     */
    public POIFSFileSystem()
    {
        super();
    }

    /**
     * Create a POIFSFileSystem from an <tt>InputStream</tt>.  Normally the stream is read until
     * EOF.  The stream is always closed.<p/>
     *
     * Some streams are usable after reaching EOF (typically those that return <code>true</code>
     * for <tt>markSupported()</tt>).  In the unlikely case that the caller has such a stream
     * <i>and</i> needs to use it after this constructor completes, a work around is to wrap the
     * stream in order to trap the <tt>close()</tt> call.  A convenience method (
     * <tt>createNonClosingInputStream()</tt>) has been provided for this purpose:
     * <pre>
     * InputStream wrappedStream = POIFSFileSystem.createNonClosingInputStream(is);
     * HSSFWorkbook wb = new HSSFWorkbook(wrappedStream);
     * is.reset();
     * doSomethingElse(is);
     * </pre>
     * Note also the special case of <tt>ByteArrayInputStream</tt> for which the <tt>close()</tt>
     * method does nothing.
     * <pre>
     * ByteArrayInputStream bais = ...
     * HSSFWorkbook wb = new HSSFWorkbook(bais); // calls bais.close() !
     * bais.reset(); // no problem
     * doSomethingElse(bais);
     * </pre>
     *
     * @param stream the InputStream from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */

    public POIFSFileSystem(InputStream stream)
        throws IOException
    {
        super(stream);
    }

    /**
     * <p>Creates a POIFSFileSystem from a <tt>File</tt>. This uses less memory than
     *  creating from an <tt>InputStream</tt>.</p>
     *  
     * <p>Note that with this constructor, you will need to call {@link #close()}
     *  when you're done to have the underlying file closed, as the file is
     *  kept open during normal operation to read the data out.</p> 
     * @param readOnly whether the POIFileSystem will only be used in read-only mode
     *  
     * @param file the File from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */
    public POIFSFileSystem(File file, boolean readOnly) throws IOException {
        super(file, readOnly);
    }
    
    /**
     * <p>Creates a POIFSFileSystem from a <tt>File</tt>. This uses less memory than
     *  creating from an <tt>InputStream</tt>. The File will be opened read-only</p>
     *  
     * <p>Note that with this constructor, you will need to call {@link #close()}
     *  when you're done to have the underlying file closed, as the file is
     *  kept open during normal operation to read the data out.</p> 
     *  
     * @param file the File from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */
    public POIFSFileSystem(File file) throws IOException {
        super(file);
    }
    
    /**
     * Checks that the supplied InputStream (which MUST
     *  support mark and reset, or be a PushbackInputStream)
     *  has a POIFS (OLE2) header at the start of it.
     * If your InputStream does not support mark / reset,
     *  then wrap it in a PushBackInputStream, then be
     *  sure to always use that, and not the original!
     * @param inp An InputStream which supports either mark/reset, or is a PushbackInputStream
     */
    public static boolean hasPOIFSHeader(InputStream inp) throws IOException {
        return NPOIFSFileSystem.hasPOIFSHeader(inp);
    }
    /**
     * Checks if the supplied first 8 bytes of a stream / file
     *  has a POIFS (OLE2) header.
     */
    public static boolean hasPOIFSHeader(byte[] header8Bytes) {
        return NPOIFSFileSystem.hasPOIFSHeader(header8Bytes);
    }
    
    /**
     * Creates a new {@link POIFSFileSystem} in a new {@link File}.
     * Use {@link #POIFSFileSystem(File)} to open an existing File,
     *  this should only be used to create a new empty filesystem.
     *
     * @param file The file to create and open
     * @return The created and opened {@link POIFSFileSystem}
     */
    public static POIFSFileSystem create(File file) throws IOException {
        // TODO Make this nicer!
        // Create a new empty POIFS in the file
        POIFSFileSystem tmp = new POIFSFileSystem();
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                tmp.writeFilesystem(out);
            } finally {
                out.close();
            }
        } finally {
            tmp.close();
        }
        
        // Open it up again backed by the file
        return new POIFSFileSystem(file, false);
    }

    /**
     * read in a file and write it back out again
     *
     * @param args names of the files; arg[ 0 ] is the input file,
     *             arg[ 1 ] is the output file
     */
    public static void main(String args[]) throws IOException
    {
        OPOIFSFileSystem.main(args);
    }
}
