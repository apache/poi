
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.nio.ByteArrayBackedDataSource;
import org.apache.poi.poifs.nio.DataSource;
import org.apache.poi.poifs.nio.FileBackedDataSource;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.NPropertyTable;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockAllocationTableWriter;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.poifs.storage.HeaderBlockWriter;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.apache.poi.poifs.storage.SmallBlockTableWriter;
import org.apache.poi.poifs.storage.BATBlock.BATBlockAndIndex;
import org.apache.poi.util.CloseIgnoringInputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LongField;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.
 * This is the new NIO version
 */

public class NPOIFSFileSystem
    implements POIFSViewable
{
	private static final POILogger _logger =
		POILogFactory.getLogger(NPOIFSFileSystem.class);

    /**
     * Convenience method for clients that want to avoid the auto-close behaviour of the constructor.
     */
    public static InputStream createNonClosingInputStream(InputStream is) {
       return new CloseIgnoringInputStream(is);
    }
   
    private NPropertyTable  _property_table;
    private List<BATBlock> _bat_blocks;
    private HeaderBlock    _header;
    private DirectoryNode  _root;
    
    private DataSource _data;
    
    private List          _documents; // TODO - probably remove this shortly

    /**
     * What big block size the file uses. Most files
     *  use 512 bytes, but a few use 4096
     */
    private POIFSBigBlockSize bigBlockSize = 
       POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;

    /**
     * Constructor, intended for writing
     */
    public NPOIFSFileSystem()
    {
        _header         = new HeaderBlock(bigBlockSize);
        _property_table = new NPropertyTable(_header);
        _bat_blocks     = new ArrayList<BATBlock>();
        _root           = null;
    }

    /**
     * Creates a POIFSFileSystem from a <tt>File</tt>. This uses less memory than
     *  creating from an <tt>InputStream</tt>
     *  
     * @param file the File from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */
    public NPOIFSFileSystem(File file)
         throws IOException
    {
       this();
       
       // Open the underlying channel
       FileChannel channel = (new RandomAccessFile(file, "r")).getChannel();
       
       // Get the header
       ByteBuffer headerBuffer = ByteBuffer.allocate(POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
       IOUtils.readFully(channel, headerBuffer);
       
       // Have the header processed
       _header = new HeaderBlock(headerBuffer);
       
       // Now process the various entries
       _data = new FileBackedDataSource(channel);
       readCoreContents();
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

    public NPOIFSFileSystem(InputStream stream)
        throws IOException
    {
        this();
        
        ReadableByteChannel channel = null;
        boolean success = false;
        
        try {
           // Turn our InputStream into something NIO based
           channel = Channels.newChannel(stream);
           
           // Get the header
           ByteBuffer headerBuffer = ByteBuffer.allocate(POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
           IOUtils.readFully(channel, headerBuffer);
           
           // Have the header processed
           _header = new HeaderBlock(headerBuffer);
   
           // We need to buffer the whole file into memory when
           //  working with an InputStream.
           // The max possible size is when each BAT block entry is used
           int maxSize = BATBlock.calculateMaximumSize(_header); 
           ByteBuffer data = ByteBuffer.allocate(maxSize);
           // Copy in the header
           headerBuffer.position(0);
           data.put(headerBuffer);
           data.position(headerBuffer.capacity());
           // Now read the rest of the stream
           IOUtils.readFully(channel, data);
           success = true;
           
           // Turn it into a DataSource
           _data = new ByteArrayBackedDataSource(data.array(), data.position());
        } finally {
           // As per the constructor contract, always close the stream
           if(channel != null)
              channel.close();
           closeInputStream(stream, success);
        }
        
        // Now process the various entries
        readCoreContents();
    }
    /**
     * @param stream the stream to be closed
     * @param success <code>false</code> if an exception is currently being thrown in the calling method
     */
    private void closeInputStream(InputStream stream, boolean success) {
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
     * Read and process the PropertiesTable and the
     *  FAT / XFAT blocks, so that we're ready to
     *  work with the file
     */
    private void readCoreContents() throws IOException {
       // Grab the block size
       bigBlockSize = _header.getBigBlockSize();
       
       // Each block should only ever be used by one of the
       //  FAT, XFAT or Property Table. Ensure it does
       ChainLoopDetector loopDetector = new ChainLoopDetector();
       
       // Read the FAT blocks
       for(int fatAt : _header.getBATArray()) {
          loopDetector.claim(fatAt);
          ByteBuffer fatData = getBlockAt(fatAt);
          BATBlock bat = BATBlock.createBATBlock(bigBlockSize, fatData);
          bat.setOurBlockIndex(fatAt);
          _bat_blocks.add(bat);
       }
       
       // Now read the XFAT blocks
       BATBlock xfat; 
       int nextAt = _header.getXBATIndex();
       for(int i=0; i<_header.getXBATCount(); i++) {
          loopDetector.claim(nextAt);
          ByteBuffer fatData = getBlockAt(nextAt);
          xfat = BATBlock.createBATBlock(bigBlockSize, fatData);
          xfat.setOurBlockIndex(nextAt);
          nextAt = xfat.getValueAt(bigBlockSize.getNextXBATChainOffset());
          
          _bat_blocks.add(xfat);
       }
       
       // We're now able to load steams
       // Use this to read in the properties
       _property_table = new NPropertyTable(_header, this);
    }
    
    /**
     * Load the block at the given offset.
     */
    protected ByteBuffer getBlockAt(final int offset) throws IOException {
       // The header block doesn't count, so add one
       long startAt = (offset+1) * bigBlockSize.getBigBlockSize();
       return _data.read(bigBlockSize.getBigBlockSize(), startAt);
    }
    
    /**
     * Returns the BATBlock that handles the specified offset,
     *  and the relative index within it
     */
    protected BATBlockAndIndex getBATBlockAndIndex(final int offset) {
       return BATBlock.getBATBlockAndIndex(
             offset, _header, _bat_blocks
       );
    }
    
    /**
     * Works out what block follows the specified one.
     */
    protected int getNextBlock(final int offset) {
       BATBlockAndIndex bai = getBATBlockAndIndex(offset);
       return bai.getBlock().getValueAt( bai.getIndex() );
    }
    
    /**
     * Changes the record of what block follows the specified one.
     */
    protected void setNextBlock(final int offset, final int nextBlock) {
       BATBlockAndIndex bai = getBATBlockAndIndex(offset);
       bai.getBlock().setValueAt(
             bai.getIndex(), nextBlock
       );
    }
    
    /**
     * Finds a free block, and returns its offset.
     * This method will extend the file if needed, and if doing
     *  so, allocate new FAT blocks to address the extra space.
     */
    protected int getFreeBlock() throws IOException {
       // First up, do we have any spare ones?
       int offset = 0;
       for(int i=0; i<_bat_blocks.size(); i++) {
          boolean isXBAT = (i >= _header.getBATCount());
          
          int numSectors = bigBlockSize.getBATEntriesPerBlock();
          if(isXBAT) {
             numSectors = bigBlockSize.getXBATEntriesPerBlock();
          }

          // Check this one
          BATBlock bat = _bat_blocks.get(i);
          if(bat.hasFreeSectors()) {
             // Claim one of them and return it
             for(int j=0; j<numSectors; j++) {
                int batValue = bat.getValueAt(j);
                if(batValue == POIFSConstants.UNUSED_BLOCK) {
                   // Bingo
                   return offset + j;
                }
             }
          }
          
          // Move onto the next BAT/XBAT
          offset += numSectors;
       }
       
       // If we get here, then there aren't any
       //  free sectors in any of the BATs or XBATs
       // So, we need to extend the file and add another
       boolean isBAT = true;
       if(_header.getBATCount() >= 109) {
          isBAT = false;
       }
       
       // Create a new BATBlock
       BATBlock newBAT = BATBlock.createEmptyBATBlock(bigBlockSize, !isBAT);
       newBAT.setOurBlockIndex(offset);
       // Ensure there's a spot in the file for it
       ByteBuffer buffer = ByteBuffer.allocate(bigBlockSize.getBigBlockSize());
       int writeTo = (1+offset) * bigBlockSize.getBigBlockSize(); // Header isn't in BATs
       _data.write(buffer, writeTo);
       
       // Allocate ourself within ourselves, at the first point
       if(isBAT) {
          newBAT.setValueAt(0, POIFSConstants.FAT_SECTOR_BLOCK);
       } else {
          newBAT.setValueAt(0, POIFSConstants.DIFAT_SECTOR_BLOCK);
       }

       // Store us
       _bat_blocks.add(newBAT);
       if(isBAT) {
          // Put it in the BAT array in the header
          int[] newBATs = new int[_header.getBATCount()+1];
          System.arraycopy(_header.getBATArray(), 0, newBATs, 0, newBATs.length-1);
          newBATs[newBATs.length-1] = offset;
          _header.setBATArray(newBATs);
          _header.setBATCount(newBATs.length);
       } else if(_header.getXBATCount() == 0) {
          // Store our first XBAT offset in the header
          _header.setXBATStart(offset);
          _header.setXBATCount(1);
       } else {
          // Chain it off the last XBAT
          BATBlock lastXBAT = _bat_blocks.get(_bat_blocks.size()-1);
          lastXBAT.setValueAt(bigBlockSize.getNextXBATChainOffset(), offset);
          _header.setXBATCount(_header.getXBATCount()+1);
       }
       
       // The first offset stores us, but the 2nd is free
       return offset+1;
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
        
        // Finally have the property table serialise itself
        _property_table.write(
              new NPOIFSStream(this, _header.getPropertyStart())
        );
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

        new NPOIFSFileSystem(istream).writeFilesystem(ostream);
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
           // TODO
//            _root = new DirectoryNode(_property_table.getRoot(), this, null);
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
    
    /**
     * Used to detect if a chain has a loop in it, so
     *  we can bail out with an error rather than
     *  spinning away for ever... 
     */
    protected class ChainLoopDetector {
       private boolean[] used_blocks;
       protected ChainLoopDetector() throws IOException {
          int numBlocks = (int)Math.ceil(_data.size()/bigBlockSize.getBigBlockSize());
          used_blocks = new boolean[numBlocks];
       }
       protected void claim(int offset) {
          if(offset >= used_blocks.length) {
             // They're writing, and have had new blocks requested
             //  for the write to proceed. That means they're into
             //  blocks we've allocated for them, so are safe
             return;
          }
          
          // Claiming an existing block, ensure there's no loop
          if(used_blocks[offset]) {
             throw new IllegalStateException(
                   "Potential loop detected - Block " + offset + 
                   " was already claimed but was just requested again"
             );
          }
          used_blocks[offset] = true;
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
}

