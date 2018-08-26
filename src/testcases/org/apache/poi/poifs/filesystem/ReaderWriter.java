
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.util.IOUtils;

/**
 * Test (Proof of concept) program that employs the
 * POIFSReaderListener and POIFSWriterListener interfaces
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public final class ReaderWriter
    implements POIFSReaderListener, POIFSWriterListener
{
    private final DirectoryEntry  root;

    // keys are DocumentDescriptors, values are byte[]s
    private final Map<DocumentDescriptor, byte[]> dataMap = new HashMap<>();

    private ReaderWriter(final POIFSFileSystem filesystem) {
        root = filesystem.getRoot();
    }

    public static void main(String [] args) throws IOException
    {
        if (args.length != 2)
        {
            System.err.println(
                "two arguments required: one input file name and one output file name");
        }
        else
        {
            POIFSReader     reader     = new POIFSReader();
            POIFSFileSystem filesystem = new POIFSFileSystem();

            reader.registerListener(new ReaderWriter(filesystem));

            reader.read(new File(args[ 0 ]));
            FileOutputStream ostream = new FileOutputStream(args[ 1 ]);

            filesystem.writeFilesystem(ostream);
            ostream.close();

            filesystem.close();
        }
    }

    /* ********** START implementation of POIFSReaderListener ********** */

    /**
     * Process a POIFSReaderEvent that this listener had registered
     * for
     *
     * @param event the POIFSReaderEvent
     */

    @Override
    public void processPOIFSReaderEvent(final POIFSReaderEvent event)
    {
        @SuppressWarnings("resource")
        DocumentInputStream istream = event.getStream();
        POIFSDocumentPath   path    = event.getPath();
        String              name    = event.getName();

        try {
            byte[] data = IOUtils.toByteArray(istream);
            int    size = data.length;

            DocumentDescriptor descriptor = new DocumentDescriptor(path,
                                                name);

            System.out.println("adding document: " + descriptor + " (" + size
                               + " bytes)");
            dataMap.put(descriptor, data);
            //int            pathLength = path.length();
            DirectoryEntry entry      = root;

            for (int k = 0; k < path.length(); k++)
            {
                String componentName = path.getComponent(k);
                Entry  nextEntry     = null;

                try
                {
                    nextEntry = entry.getEntry(componentName);
                }
                catch (FileNotFoundException ignored)
                {
                    try
                    {
                        nextEntry = entry.createDirectory(componentName);
                    }
                    catch (IOException e)
                    {
                        System.out.println("Unable to create directory");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                entry = ( DirectoryEntry ) nextEntry;
            }
            entry.createDocument(name, size, this);
        }
        catch (IOException ignored)
        {
        }
    }

    /* **********  END  implementation of POIFSReaderListener ********** */
    /* ********** START implementation of POIFSWriterListener ********** */

    /**
     * Process a POIFSWriterEvent that this listener had registered
     * for
     *
     * @param event the POIFSWriterEvent
     */

    @Override
    public void processPOIFSWriterEvent(final POIFSWriterEvent event)
    {
        try
        {
            DocumentDescriptor descriptor =
                new DocumentDescriptor(event.getPath(), event.getName());

            System.out.println("looking up document: " + descriptor + " ("
                               + event.getLimit() + " bytes)");
            event.getStream().write(dataMap.get(descriptor));
        }
        catch (IOException e)
        {
            System.out.println("Unable to write document");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* **********  END  implementation of POIFSWriterListener ********** */
}   // end public class ReaderWriter

