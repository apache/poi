
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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockAllocationTableWriter;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.poifs.storage.HeaderBlockReader;
import org.apache.poi.poifs.storage.HeaderBlockWriter;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.apache.poi.poifs.storage.SmallBlockTableWriter;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LongField;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSFileSystem
    implements POIFSViewable
{
	private static final POILogger _logger =
		POILogFactory.getLogger(POIFSFileSystem.class);

    private static final class CloseIgnoringInputStream extends InputStream {

        private final InputStream _is;
        public CloseIgnoringInputStream(InputStream is) {
            _is = is;
        }
        public int read() throws IOException {
            return _is.read();
        }
        public int read(byte[] b, int off, int len) throws IOException {
            return _is.read(b, off, len);
        }
        public void close() {
            // do nothing
        }
    }

    /**
     * Convenience method for clients that want to avoid the auto-close behaviour of the constructor.
     */
    public static InputStream createNonClosingInputStream(InputStream is) {
        return new CloseIgnoringInputStream(is);
    }

    private PropertyTable _property_table;
    private List          _documents;
    private DirectoryNode _root;

    /**
     * What big block size the file uses. Most files
     *  use 512 bytes, but a few use 4096
     */
    private POIFSBigBlockSize bigBlockSize = 
       POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;

    /**
     * Constructor, intended for writing
     */
    public POIFSFileSystem()
    {
        _property_table = new PropertyTable(bigBlockSize);
        _documents      = new ArrayList();
        _root           = null;
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
        this();
        boolean success = false;

        HeaderBlockReader header_block_reader;
        RawDataBlockList data_blocks;
        try {
            // read the header block from the stream
            header_block_reader = new HeaderBlockReader(stream);
            bigBlockSize = header_block_reader.getBigBlockSize();

            // read the rest of the stream into blocks
            data_blocks = new RawDataBlockList(stream, bigBlockSize);
            success = true;
        } finally {
            closeInputStream(stream, success);
        }


        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        new BlockAllocationTableReader(header_block_reader.getBigBlockSize(),
                                       header_block_reader.getBATCount(),
                                       header_block_reader.getBATArray(),
                                       header_block_reader.getXBATCount(),
                                       header_block_reader.getXBATIndex(),
                                       data_blocks);

        // get property table from the document
        PropertyTable properties =
            new PropertyTable(bigBlockSize,
                              header_block_reader.getPropertyStart(),
                              data_blocks);

        // init documents
        processProperties(
        		SmallBlockTableReader.getSmallDocumentBlocks(
        		      bigBlockSize, data_blocks, properties.getRoot(),
        				header_block_reader.getSBATStart()
        		),
        		data_blocks,
        		properties.getRoot().getChildren(),
        		null,
        		header_block_reader.getPropertyStart()
        );

        // For whatever reason CLSID of root is always 0.
        getRoot().setStorageClsid(properties.getRoot().getStorageClsid());
    }
    /**
     * @param stream the stream to be closed
     * @param success <code>false</code> if an exception is currently being thrown in the calling method
     */
    private void closeInputStream(InputStream stream, boolean success) {

        if(stream.markSupported() && !(stream instanceof ByteArrayInputStream)) {
            String msg = "POIFS is closing the supplied input stream of type ("
                    + stream.getClass().getName() + ") which supports mark/reset.  "
                    + "This will be a problem for the caller if the stream will still be used.  "
                    + "If that is the case the caller should wrap the input stream to avoid this close logic.  "
                    + "This warning is only temporary and will not be present in future versions of POI.";
            _logger.log(POILogger.WARN, msg);
        }
        try {
            stream.close();
        } catch (IOException e) {
            if(success) {
                throw new RuntimeException(e);
            }
            // else not success? Try block did not complete normally
            // just print stack trace and leave original ex to be thrown
            e.printStackTrace();
        }
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
        // We want to peek at the first 8 bytes
        inp.mark(8);

        byte[] header = new byte[8];
        IOUtils.readFully(inp, header);
        LongField signature = new LongField(HeaderBlockConstants._signature_offset, header);

        // Wind back those 8 bytes
        if(inp instanceof PushbackInputStream) {
            PushbackInputStream pin = (PushbackInputStream)inp;
            pin.unread(header);
        } else {
            inp.reset();
        }

        // Did it match the signature?
        return (signature.get() == HeaderBlockConstants._signature);
    }

    /**
     * Create a new document to be added to the root directory
     *
     * @param stream the InputStream from which the document's data
     *               will be obtained
     * @param name the name of the new POIFSDocument
     *
     * @return the new DocumentEntry
     *
     * @exception IOException on error creating the new POIFSDocument
     */

    public DocumentEntry createDocument(final InputStream stream,
                                        final String name)
        throws IOException
    {
        return getRoot().createDocument(name, stream);
    }

    /**
     * create a new DocumentEntry in the root entry; the data will be
     * provided later
     *
     * @param name the name of the new DocumentEntry
     * @param size the size of the new DocumentEntry
     * @param writer the writer of the new DocumentEntry
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    public DocumentEntry createDocument(final String name, final int size,
                                        final POIFSWriterListener writer)
        throws IOException
    {
        return getRoot().createDocument(name, size, writer);
    }

    /**
     * create a new DirectoryEntry in the root directory
     *
     * @param name the name of the new DirectoryEntry
     *
     * @return the new DirectoryEntry
     *
     * @exception IOException on name duplication
     */

    public DirectoryEntry createDirectory(final String name)
        throws IOException
    {
        return getRoot().createDirectory(name);
    }

    /**
     * Write the filesystem out
     *
     * @param stream the OutputStream to which the filesystem will be
     *               written
     *
     * @exception IOException thrown on errors writing to the stream
     */

    public void writeFilesystem(final OutputStream stream)
        throws IOException
    {

        // get the property table ready
        _property_table.preWrite();

        // create the small block store, and the SBAT
        SmallBlockTableWriter      sbtw       =
            new SmallBlockTableWriter(bigBlockSize, _documents, _property_table.getRoot());

        // create the block allocation table
        BlockAllocationTableWriter bat        =
            new BlockAllocationTableWriter(bigBlockSize);

        // create a list of BATManaged objects: the documents plus the
        // property table and the small block table
        List bm_objects = new ArrayList();

        bm_objects.addAll(_documents);
        bm_objects.add(_property_table);
        bm_objects.add(sbtw);
        bm_objects.add(sbtw.getSBAT());

        // walk the list, allocating space for each and assigning each
        // a starting block number
        Iterator iter = bm_objects.iterator();

        while (iter.hasNext())
        {
            BATManaged bmo         = ( BATManaged ) iter.next();
            int        block_count = bmo.countBlocks();

            if (block_count != 0)
            {
                bmo.setStartBlock(bat.allocateSpace(block_count));
            }
            else
            {

                // Either the BATManaged object is empty or its data
                // is composed of SmallBlocks; in either case,
                // allocating space in the BAT is inappropriate
            }
        }

        // allocate space for the block allocation table and take its
        // starting block
        int               batStartBlock       = bat.createBlocks();

        // get the extended block allocation table blocks
        HeaderBlockWriter header_block_writer = new HeaderBlockWriter(bigBlockSize);
        BATBlock[]        xbat_blocks         =
            header_block_writer.setBATBlocks(bat.countBlocks(),
                                             batStartBlock);

        // set the property table start block
        header_block_writer.setPropertyStart(_property_table.getStartBlock());

        // set the small block allocation table start block
        header_block_writer.setSBATStart(sbtw.getSBAT().getStartBlock());

        // set the small block allocation table block count
        header_block_writer.setSBATBlockCount(sbtw.getSBATBlockCount());

        // the header is now properly initialized. Make a list of
        // writers (the header block, followed by the documents, the
        // property table, the small block store, the small block
        // allocation table, the block allocation table, and the
        // extended block allocation table blocks)
        List writers = new ArrayList();

        writers.add(header_block_writer);
        writers.addAll(_documents);
        writers.add(_property_table);
        writers.add(sbtw);
        writers.add(sbtw.getSBAT());
        writers.add(bat);
        for (int j = 0; j < xbat_blocks.length; j++)
        {
            writers.add(xbat_blocks[ j ]);
        }

        // now, write everything out
        iter = writers.iterator();
        while (iter.hasNext())
        {
            BlockWritable writer = ( BlockWritable ) iter.next();

            writer.writeBlocks(stream);
        }
    }

    /**
     * read in a file and write it back out again
     *
     * @param args names of the files; arg[ 0 ] is the input file,
     *             arg[ 1 ] is the output file
     *
     * @exception IOException
     */

    public static void main(String args[])
        throws IOException
    {
        if (args.length != 2)
        {
            System.err.println(
                "two arguments required: input filename and output filename");
            System.exit(1);
        }
        FileInputStream  istream = new FileInputStream(args[ 0 ]);
        FileOutputStream ostream = new FileOutputStream(args[ 1 ]);

        new POIFSFileSystem(istream).writeFilesystem(ostream);
        istream.close();
        ostream.close();
    }

    /**
     * get the root entry
     *
     * @return the root entry
     */

    public DirectoryNode getRoot()
    {
        if (_root == null)
        {
            _root = new DirectoryNode(_property_table.getRoot(), this, null);
        }
        return _root;
    }

    /**
     * open a document in the root entry's list of entries
     *
     * @param documentName the name of the document to be opened
     *
     * @return a newly opened DocumentInputStream
     *
     * @exception IOException if the document does not exist or the
     *            name is that of a DirectoryEntry
     */

    public DocumentInputStream createDocumentInputStream(
            final String documentName)
        throws IOException
    {
    	return getRoot().createDocumentInputStream(documentName);
    }

    /**
     * add a new POIFSDocument
     *
     * @param document the POIFSDocument being added
     */

    void addDocument(final POIFSDocument document)
    {
        _documents.add(document);
        _property_table.addProperty(document.getDocumentProperty());
    }

    /**
     * add a new DirectoryProperty
     *
     * @param directory the DirectoryProperty being added
     */

    void addDirectory(final DirectoryProperty directory)
    {
        _property_table.addProperty(directory);
    }

    /**
     * remove an entry
     *
     * @param entry to be removed
     */

    void remove(EntryNode entry)
    {
        _property_table.removeProperty(entry.getProperty());
        if (entry.isDocumentEntry())
        {
            _documents.remove((( DocumentNode ) entry).getDocument());
        }
    }

    private void processProperties(final BlockList small_blocks,
                                   final BlockList big_blocks,
                                   final Iterator properties,
                                   final DirectoryNode dir,
                                   final int headerPropertiesStartAt)
        throws IOException
    {
        while (properties.hasNext())
        {
            Property      property = ( Property ) properties.next();
            String        name     = property.getName();
            DirectoryNode parent   = (dir == null)
                                     ? (( DirectoryNode ) getRoot())
                                     : dir;

            if (property.isDirectory())
            {
                DirectoryNode new_dir =
                    ( DirectoryNode ) parent.createDirectory(name);

                new_dir.setStorageClsid( property.getStorageClsid() );

                processProperties(
                    small_blocks, big_blocks,
                    (( DirectoryProperty ) property).getChildren(),
                    new_dir, headerPropertiesStartAt);
            }
            else
            {
                int           startBlock = property.getStartBlock();
                int           size       = property.getSize();
                POIFSDocument document   = null;

                if (property.shouldUseSmallBlocks())
                {
                    document =
                        new POIFSDocument(name,
                                          small_blocks.fetchBlocks(startBlock, headerPropertiesStartAt),
                                          size);
                }
                else
                {
                    document =
                        new POIFSDocument(name,
                                          big_blocks.fetchBlocks(startBlock, headerPropertiesStartAt),
                                          size);
                }
                parent.createDocument(document);
            }
        }
    }

    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */

    public Object [] getViewableArray()
    {
        if (preferArray())
        {
            return (( POIFSViewable ) getRoot()).getViewableArray();
        }
        return new Object[ 0 ];
    }

    /**
     * Get an Iterator of objects, some of which may implement
     * POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty
     * back end store
     */

    public Iterator getViewableIterator()
    {
        if (!preferArray())
        {
            return (( POIFSViewable ) getRoot()).getViewableIterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return true if a viewer should call getViewableArray, false if
     *         a viewer should call getViewableIterator
     */

    public boolean preferArray()
    {
        return (( POIFSViewable ) getRoot()).preferArray();
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    public String getShortDescription()
    {
        return "POIFS FileSystem";
    }

    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    public int getBigBlockSize() {
    	return bigBlockSize.getBigBlockSize();
    }
    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    public POIFSBigBlockSize getBigBlockSizeDetails() {
      return bigBlockSize;
    }

    /* **********  END  begin implementation of POIFSViewable ********** */
}   // end public class POIFSFileSystem

