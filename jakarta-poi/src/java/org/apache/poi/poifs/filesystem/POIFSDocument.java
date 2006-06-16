
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
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.ListManagedBlock;
import org.apache.poi.poifs.storage.DocumentBlock;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.SmallDocumentBlock;
import org.apache.poi.util.HexDump;

/**
 * This class manages a document in the POIFS filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSDocument
    implements BATManaged, BlockWritable, POIFSViewable
{
    private DocumentProperty _property;
    private int              _size;

    // one of these stores will be valid
    private SmallBlockStore  _small_store;
    private BigBlockStore    _big_store;

    /**
     * Constructor from large blocks
     *
     * @param name the name of the POIFSDocument
     * @param blocks the big blocks making up the POIFSDocument
     * @param length the actual length of the POIFSDocument
     *
     * @exception IOException
     */

    public POIFSDocument(final String name, final RawDataBlock [] blocks,
                         final int length)
        throws IOException
    {
        _size        = length;
        _big_store   = new BigBlockStore(blocks);
        _property    = new DocumentProperty(name, _size);
        _small_store = new SmallBlockStore(new BlockWritable[ 0 ]);
        _property.setDocument(this);
    }

    /**
     * Constructor from small blocks
     *
     * @param name the name of the POIFSDocument
     * @param blocks the small blocks making up the POIFSDocument
     * @param length the actual length of the POIFSDocument
     */

    public POIFSDocument(final String name,
                         final SmallDocumentBlock [] blocks, final int length)
    {
        _size = length;
        try
        {
            _big_store = new BigBlockStore(new RawDataBlock[ 0 ]);
        }
        catch (IOException ignored)
        {

            // can't happen with that constructor
        }
        _property    = new DocumentProperty(name, _size);
        _small_store = new SmallBlockStore(blocks);
        _property.setDocument(this);
    }

    /**
     * Constructor from small blocks
     *
     * @param name the name of the POIFSDocument
     * @param blocks the small blocks making up the POIFSDocument
     * @param length the actual length of the POIFSDocument
     *
     * @exception IOException
     */

    public POIFSDocument(final String name, final ListManagedBlock [] blocks,
                         final int length)
        throws IOException
    {
        _size     = length;
        _property = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (Property.isSmall(_size))
        {
            _big_store   = new BigBlockStore(new RawDataBlock[ 0 ]);
            _small_store = new SmallBlockStore(blocks);
        }
        else
        {
            _big_store   = new BigBlockStore(blocks);
            _small_store = new SmallBlockStore(new BlockWritable[ 0 ]);
        }
    }

    /**
     * Constructor
     *
     * @param name the name of the POIFSDocument
     * @param stream the InputStream we read data from
     *
     * @exception IOException thrown on read errors
     */

    public POIFSDocument(final String name, final InputStream stream)
        throws IOException
    {
        List blocks = new ArrayList();

        _size = 0;
        while (true)
        {
            DocumentBlock block     = new DocumentBlock(stream);
            int           blockSize = block.size();

            if (blockSize > 0)
            {
                blocks.add(block);
                _size += blockSize;
            }
            if (block.partiallyRead())
            {
                break;
            }
        }
        DocumentBlock[] bigBlocks =
            ( DocumentBlock [] ) blocks.toArray(new DocumentBlock[ 0 ]);

        _big_store = new BigBlockStore(bigBlocks);
        _property  = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (_property.shouldUseSmallBlocks())
        {
            _small_store =
                new SmallBlockStore(SmallDocumentBlock.convert(bigBlocks,
                    _size));
            _big_store   = new BigBlockStore(new DocumentBlock[ 0 ]);
        }
        else
        {
            _small_store = new SmallBlockStore(new BlockWritable[ 0 ]);
        }
    }

    /**
     * Constructor
     *
     * @param name the name of the POIFSDocument
     * @param size the length of the POIFSDocument
     * @param path the path of the POIFSDocument
     * @param writer the writer who will eventually write the document
     *               contents
     *
     * @exception IOException thrown on read errors
     */

    public POIFSDocument(final String name, final int size,
                         final POIFSDocumentPath path,
                         final POIFSWriterListener writer)
        throws IOException
    {
        _size     = size;
        _property = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (_property.shouldUseSmallBlocks())
        {
            _small_store = new SmallBlockStore(path, name, size, writer);
            _big_store   = new BigBlockStore(new Object[ 0 ]);
        }
        else
        {
            _small_store = new SmallBlockStore(new BlockWritable[ 0 ]);
            _big_store   = new BigBlockStore(path, name, size, writer);
        }
    }

    /**
     * return the array of SmallDocumentBlocks used
     *
     * @return array of SmallDocumentBlocks; may be empty, cannot be null
     */

    public BlockWritable [] getSmallBlocks()
    {
        return _small_store.getBlocks();
    }

    /**
     * @return size of the document
     */

    public int getSize()
    {
        return _size;
    }

    /**
     * read data from the internal stores
     *
     * @param buffer the buffer to write to
     * @param offset the offset into our storage to read from
     */

    void read(final byte [] buffer, final int offset)
    {
        if (_property.shouldUseSmallBlocks())
        {
            SmallDocumentBlock.read(_small_store.getBlocks(), buffer, offset);
        }
        else
        {
            DocumentBlock.read(_big_store.getBlocks(), buffer, offset);
        }
    }

    /**
     * Get the DocumentProperty
     *
     * @return the instance's DocumentProperty
     */

    DocumentProperty getDocumentProperty()
    {
        return _property;
    }

    /* ********** START implementation of BlockWritable ********** */

    /**
     * Write the storage to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    public void writeBlocks(final OutputStream stream)
        throws IOException
    {
        _big_store.writeBlocks(stream);
    }

    /* **********  END  implementation of BlockWritable ********** */
    /* ********** START implementation of BATManaged ********** */

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */

    public int countBlocks()
    {
        return _big_store.countBlocks();
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of blocks making up the
     *        filesystem
     */

    public void setStartBlock(final int index)
    {
        _property.setStartBlock(index);
    }

    /* **********  END  implementation of BATManaged ********** */
    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */

    public Object [] getViewableArray()
    {
        Object[] results = new Object[ 1 ];
        String   result;

        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BlockWritable[]       blocks = null;

            if (_big_store.isValid())
            {
                blocks = _big_store.getBlocks();
            }
            else if (_small_store.isValid())
            {
                blocks = _small_store.getBlocks();
            }
            if (blocks != null)
            {
                for (int k = 0; k < blocks.length; k++)
                {
                    blocks[ k ].writeBlocks(output);
                }
                byte[] data = output.toByteArray();

                if (data.length > _property.getSize())
                {
                    byte[] tmp = new byte[ _property.getSize() ];

                    System.arraycopy(data, 0, tmp, 0, tmp.length);
                    data = tmp;
                }
                output = new ByteArrayOutputStream();
                HexDump.dump(data, 0, output, 0);
                result = output.toString();
            }
            else
            {
                result = "<NO DATA>";
            }
        }
        catch (IOException e)
        {
            result = e.getMessage();
        }
        results[ 0 ] = result;
        return results;
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
        return true;
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    public String getShortDescription()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Document: \"").append(_property.getName())
            .append("\"");
        buffer.append(" size = ").append(getSize());
        return buffer.toString();
    }

    /* **********  END  begin implementation of POIFSViewable ********** */
    private class SmallBlockStore
    {
        private SmallDocumentBlock[] smallBlocks;
        private POIFSDocumentPath    path;
        private String               name;
        private int                  size;
        private POIFSWriterListener  writer;

        /**
         * Constructor
         *
         * @param blocks blocks to construct the store from
         */

        SmallBlockStore(final Object [] blocks)
        {
            smallBlocks = new SmallDocumentBlock[ blocks.length ];
            for (int j = 0; j < blocks.length; j++)
            {
                smallBlocks[ j ] = ( SmallDocumentBlock ) blocks[ j ];
            }
            this.path   = null;
            this.name   = null;
            this.size   = -1;
            this.writer = null;
        }

        /**
         * Constructor for a small block store that will be written
         * later
         *
         * @param path path of the document
         * @param name name of the document
         * @param size length of the document
         * @param writer the object that will eventually write the document
         */

        SmallBlockStore(final POIFSDocumentPath path, final String name,
                        final int size, final POIFSWriterListener writer)
        {
            smallBlocks = new SmallDocumentBlock[ 0 ];
            this.path   = path;
            this.name   = name;
            this.size   = size;
            this.writer = writer;
        }

        /**
         * @return true if this store is a valid source of data
         */

        boolean isValid()
        {
            return ((smallBlocks.length > 0) || (writer != null));
        }

        /**
         * @return the SmallDocumentBlocks
         */

        BlockWritable [] getBlocks()
        {
            if (isValid() && (writer != null))
            {
                ByteArrayOutputStream stream  =
                    new ByteArrayOutputStream(size);
                DocumentOutputStream  dstream =
                    new DocumentOutputStream(stream, size);

                writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream,
                        path, name, size));
                smallBlocks = SmallDocumentBlock.convert(stream.toByteArray(),
                                                         size);
            }
            return smallBlocks;
        }
    }   // end private class SmallBlockStore

    private class BigBlockStore
    {
        private DocumentBlock[]     bigBlocks;
        private POIFSDocumentPath   path;
        private String              name;
        private int                 size;
        private POIFSWriterListener writer;

        /**
         * Constructor
         *
         * @param blocks the blocks making up the store
         *
         * @exception IOException on I/O error
         */

        BigBlockStore(final Object [] blocks)
            throws IOException
        {
            bigBlocks = new DocumentBlock[ blocks.length ];
            for (int j = 0; j < blocks.length; j++)
            {
                if (blocks[ j ] instanceof DocumentBlock)
                {
                    bigBlocks[ j ] = ( DocumentBlock ) blocks[ j ];
                }
                else
                {
                    bigBlocks[ j ] =
                        new DocumentBlock(( RawDataBlock ) blocks[ j ]);
                }
            }
            this.path   = null;
            this.name   = null;
            this.size   = -1;
            this.writer = null;
        }

        /**
         * Constructor for a big block store that will be written
         * later
         *
         * @param path path of the document
         * @param name name of the document
         * @param size length of the document
         * @param writer the object that will eventually write the
         *               document
         */

        BigBlockStore(final POIFSDocumentPath path, final String name,
                      final int size, final POIFSWriterListener writer)
        {
            bigBlocks   = new DocumentBlock[ 0 ];
            this.path   = path;
            this.name   = name;
            this.size   = size;
            this.writer = writer;
        }

        /**
         * @return true if this store is a valid source of data
         */

        boolean isValid()
        {
            return ((bigBlocks.length > 0) || (writer != null));
        }

        /**
         * @return the DocumentBlocks
         */

        DocumentBlock [] getBlocks()
        {
            if (isValid() && (writer != null))
            {
                ByteArrayOutputStream stream  =
                    new ByteArrayOutputStream(size);
                DocumentOutputStream  dstream =
                    new DocumentOutputStream(stream, size);

                writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream,
                        path, name, size));
                bigBlocks = DocumentBlock.convert(stream.toByteArray(), size);
            }
            return bigBlocks;
        }

        /**
         * write the blocks to a stream
         *
         * @param stream the stream to which the data is to be written
         *
         * @exception IOException on error
         */

        void writeBlocks(OutputStream stream)
            throws IOException
        {
            if (isValid())
            {
                if (writer != null)
                {
                    DocumentOutputStream dstream =
                        new DocumentOutputStream(stream, size);

                    writer.processPOIFSWriterEvent(
                        new POIFSWriterEvent(dstream, path, name, size));
                    dstream.writeFiller(countBlocks()
                                        * POIFSConstants
                                            .BIG_BLOCK_SIZE, DocumentBlock
                                            .getFillByte());
                }
                else
                {
                    for (int k = 0; k < bigBlocks.length; k++)
                    {
                        bigBlocks[ k ].writeBlocks(stream);
                    }
                }
            }
        }

        /**
         * @return number of big blocks making up this document
         */

        int countBlocks()
        {
            int rval = 0;

            if (isValid())
            {
                if (writer != null)
                {
                    rval = (size + POIFSConstants.BIG_BLOCK_SIZE - 1)
                           / POIFSConstants.BIG_BLOCK_SIZE;
                }
                else
                {
                    rval = bigBlocks.length;
                }
            }
            return rval;
        }
    }   // end private class BigBlockStore
}       // end class POIFSDocument

