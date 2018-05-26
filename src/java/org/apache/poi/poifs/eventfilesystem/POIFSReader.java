
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
        

package org.apache.poi.poifs.eventfilesystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.OPOIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.property.RootProperty;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.apache.poi.util.IOUtils;

/**
 * An event-driven reader for POIFS file systems. Users of this class
 * first create an instance of it, then use the registerListener
 * methods to register POIFSReaderListener instances for specific
 * documents. Once all the listeners have been registered, the read()
 * method is called, which results in the listeners being notified as
 * their documents are read.
 */

public class POIFSReader
{
    private final POIFSReaderRegistry registry;
    private boolean registryClosed;
    private boolean notifyEmptyDirectories;

    /**
     * Create a POIFSReader
     */

    public POIFSReader()
    {
        registry       = new POIFSReaderRegistry();
        registryClosed = false;
    }

    /**
     * Read from an InputStream and process the documents we get
     *
     * @param stream the InputStream from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */

    public void read(final InputStream stream)
        throws IOException
    {
        registryClosed = true;

        // read the header block from the stream
        HeaderBlock header_block = new HeaderBlock(stream);

        // read the rest of the stream into blocks
        RawDataBlockList data_blocks = new RawDataBlockList(stream, header_block.getBigBlockSize());

        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        new BlockAllocationTableReader(header_block.getBigBlockSize(),
                                       header_block.getBATCount(),
                                       header_block.getBATArray(),
                                       header_block.getXBATCount(),
                                       header_block.getXBATIndex(),
                                       data_blocks);

        // get property table from the document
        PropertyTable properties =
            new PropertyTable(header_block, data_blocks);

        // process documents
        RootProperty root = properties.getRoot();
        processProperties(SmallBlockTableReader
            .getSmallDocumentBlocks(
                  header_block.getBigBlockSize(),
                  data_blocks, root,
                  header_block.getSBATStart()
            ),
            data_blocks, root.getChildren(), new POIFSDocumentPath()
        );
    }

    /**
     * Register a POIFSReaderListener for all documents
     *
     * @param listener the listener to be registered
     *
     * @exception NullPointerException if listener is null
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        if (registryClosed)
        {
            throw new IllegalStateException();
        }
        registry.registerListener(listener);
    }

    /**
     * Register a POIFSReaderListener for a document in the root
     * directory
     *
     * @param listener the listener to be registered
     * @param name the document name
     *
     * @exception NullPointerException if listener is null or name is
     *                                 null or empty
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener,
                                 final String name)
    {
        registerListener(listener, null, name);
    }

    /**
     * Register a POIFSReaderListener for a document in the specified
     * directory
     *
     * @param listener the listener to be registered
     * @param path the document path; if null, the root directory is
     *             assumed
     * @param name the document name
     *
     * @exception NullPointerException if listener is null or name is
     *                                 null or empty
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener,
                                 final POIFSDocumentPath path,
                                 final String name)
    {
        if ((listener == null) || (name == null) || (name.length() == 0))
        {
            throw new NullPointerException();
        }
        if (registryClosed)
        {
            throw new IllegalStateException();
        }
        registry.registerListener(listener,
                                  (path == null) ? new POIFSDocumentPath()
                                                 : path, name);
    }

    /**
     * Activates the notification of empty directories.<p>
     * If this flag is activated, the {@link POIFSReaderListener listener} receives
     * {@link POIFSReaderEvent POIFSReaderEvents} with nulled {@code name} and {@code stream}
     *
     * @param notifyEmptyDirectories
     */
    public void setNotifyEmptyDirectories(boolean notifyEmptyDirectories) {
        this.notifyEmptyDirectories = notifyEmptyDirectories;
    }


    /**
     * read in files
     *
     * @param args names of the files
     *
     * @exception IOException
     */

    public static void main(String args[])
        throws IOException
    {
        if (args.length == 0)
        {
            System.err.println("at least one argument required: input filename(s)");
            System.exit(1);
        }

        // register for all
        for (String arg : args)
        {
            POIFSReader         reader   = new POIFSReader();
            POIFSReaderListener listener = new SampleListener();

            reader.registerListener(listener);
            System.out.println("reading " + arg);
            FileInputStream istream = new FileInputStream(arg);

            reader.read(istream);
            istream.close();
        }
    }

    private void processProperties(final BlockList small_blocks,
                                   final BlockList big_blocks,
                                   final Iterator<Property> properties,
                                   final POIFSDocumentPath path)
    throws IOException {
        if (!properties.hasNext() && notifyEmptyDirectories) {
            Iterator<POIFSReaderListener> listeners  = registry.getListeners(path, ".");
            while (listeners.hasNext()) {
                POIFSReaderListener pl = listeners.next();
                POIFSReaderEvent pe = new POIFSReaderEvent(null, path, null);
                pl.processPOIFSReaderEvent(pe);
            }
            return;
        }

        while (properties.hasNext())
        {
            Property property = properties.next();
            String   name     = property.getName();

            if (property.isDirectory()) {
                POIFSDocumentPath new_path = new POIFSDocumentPath(path,new String[]{name});
                DirectoryProperty dp = (DirectoryProperty) property;
                processProperties(small_blocks, big_blocks, dp.getChildren(), new_path);
            } else {
                int startBlock = property.getStartBlock();
                Iterator<POIFSReaderListener> listeners  = registry.getListeners(path, name);

                if (listeners.hasNext())
                {
                    int            size     = property.getSize();
                    OPOIFSDocument document = null;

                    if (property.shouldUseSmallBlocks())
                    {
                        document =
                            new OPOIFSDocument(name, small_blocks
                                .fetchBlocks(startBlock, -1), size);
                    }
                    else
                    {
                        document =
                            new OPOIFSDocument(name, big_blocks
                                .fetchBlocks(startBlock, -1), size);
                    }
                    while (listeners.hasNext())
                    {
                        POIFSReaderListener listener = listeners.next();
                        try (DocumentInputStream dis = new DocumentInputStream(document)) {
                            listener.processPOIFSReaderEvent(new POIFSReaderEvent(dis, path, name));
                        }
                    }
                }
                else
                {

                    // consume the document's data and discard it
                    if (property.shouldUseSmallBlocks())
                    {
                        small_blocks.fetchBlocks(startBlock, -1);
                    }
                    else
                    {
                        big_blocks.fetchBlocks(startBlock, -1);
                    }
                }
            }
        }
    }

    private static class SampleListener
        implements POIFSReaderListener
    {

        /**
         * Constructor SampleListener
         */

        SampleListener()
        {
        }

        /**
         * Method processPOIFSReaderEvent
         *
         * @param event
         */

        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event) {
            DocumentInputStream istream = event.getStream();
            POIFSDocumentPath   path    = event.getPath();
            String              name    = event.getName();

            try {
                byte[] data = IOUtils.toByteArray(istream);
                int pathLength = path.length();

                for (int k = 0; k < pathLength; k++) {
                    System.out.print("/" + path.getComponent(k));
                }
                System.out.println("/" + name + ": " + data.length + " bytes read");
            } catch (IOException ignored) {
            } finally {
                IOUtils.closeQuietly(istream);
            }
        }
    }
}

