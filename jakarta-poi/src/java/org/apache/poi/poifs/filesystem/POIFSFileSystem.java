
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

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockAllocationTableWriter;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.HeaderBlockReader;
import org.apache.poi.poifs.storage.HeaderBlockWriter;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.apache.poi.poifs.storage.SmallBlockTableWriter;
import org.apache.poi.poifs.storage.SmallDocumentBlock;

/**
 * This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSFileSystem
    implements POIFSViewable
{
    private PropertyTable _property_table;
    private List          _documents;
    private DirectoryNode _root;

    /**
     * Constructor, intended for writing
     */

    public POIFSFileSystem()
    {
        _property_table = new PropertyTable();
        _documents      = new ArrayList();
        _root           = null;
    }

    /**
     * Create a POIFSFileSystem from an InputStream
     *
     * @param stream the InputStream from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */

    public POIFSFileSystem(final InputStream stream)
        throws IOException
    {
        this();

        // read the header block from the stream
        HeaderBlockReader header_block_reader = new HeaderBlockReader(stream);

        // read the rest of the stream into blocks
        RawDataBlockList  data_blocks         = new RawDataBlockList(stream);

        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        new BlockAllocationTableReader(header_block_reader.getBATCount(),
                                       header_block_reader.getBATArray(),
                                       header_block_reader.getXBATCount(),
                                       header_block_reader.getXBATIndex(),
                                       data_blocks);

        // get property table from the document
        PropertyTable properties =
            new PropertyTable(header_block_reader.getPropertyStart(),
                              data_blocks);

        // init documents
        processProperties(SmallBlockTableReader
            .getSmallDocumentBlocks(data_blocks, properties
                .getRoot(), header_block_reader
                    .getSBATStart()), data_blocks, properties.getRoot()
                        .getChildren(), null);
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
            new SmallBlockTableWriter(_documents, _property_table.getRoot());

        // create the block allocation table
        BlockAllocationTableWriter bat        =
            new BlockAllocationTableWriter();

        // create a list of BATManaged objects: the documents plus the
        // property table and the small block table
        List                       bm_objects = new ArrayList();

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
        HeaderBlockWriter header_block_writer = new HeaderBlockWriter();
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

    public DirectoryEntry getRoot()
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
        Entry document = getRoot().getEntry(documentName);

        if (!document.isDocumentEntry())
        {
            throw new IOException("Entry '" + documentName
                                  + "' is not a DocumentEntry");
        }
        return new DocumentInputStream(( DocumentEntry ) document);
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
                                   final DirectoryNode dir)
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
                    (( DirectoryProperty ) property).getChildren(), new_dir);
            }
            else
            {
                int           startBlock = property.getStartBlock();
                int           size       = property.getSize();
                POIFSDocument document   = null;

                if (property.shouldUseSmallBlocks())
                {
                    document =
                        new POIFSDocument(name, small_blocks
                            .fetchBlocks(startBlock), size);
                }
                else
                {
                    document =
                        new POIFSDocument(name,
                                          big_blocks.fetchBlocks(startBlock),
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
        else
        {
            return new Object[ 0 ];
        }
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
        else
        {
            return Collections.EMPTY_LIST.iterator();
        }
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

    /* **********  END  begin implementation of POIFSViewable ********** */
}   // end public class POIFSFileSystem

