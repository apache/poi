
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

import java.util.*;

import java.io.*;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;

/**
 * Test (Proof of concept) program that employs the
 * POIFSReaderListener and POIFSWriterListener interfaces
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class ReaderWriter
    implements POIFSReaderListener, POIFSWriterListener
{
    private POIFSFileSystem filesystem;
    private DirectoryEntry  root;

    // keys are DocumentDescriptors, values are byte[]s
    private Map             dataMap;

    /**
     * Constructor ReaderWriter
     *
     *
     * @param filesystem
     *
     */

    ReaderWriter(final POIFSFileSystem filesystem)
    {
        this.filesystem = filesystem;
        root            = this.filesystem.getRoot();
        dataMap         = new HashMap();
    }

    /**
     * Method main
     *
     *
     * @param args
     *
     * @exception IOException
     *
     */

    public static void main(String [] args)
        throws IOException
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
            FileInputStream istream = new FileInputStream(args[ 0 ]);

            reader.read(istream);
            istream.close();
            FileOutputStream ostream = new FileOutputStream(args[ 1 ]);

            filesystem.writeFilesystem(ostream);
            ostream.close();
        }
    }

    /* ********** START implementation of POIFSReaderListener ********** */

    /**
     * Process a POIFSReaderEvent that this listener had registered
     * for
     *
     * @param event the POIFSReaderEvent
     */

    public void processPOIFSReaderEvent(final POIFSReaderEvent event)
    {
        DocumentInputStream istream = event.getStream();
        POIFSDocumentPath   path    = event.getPath();
        String              name    = event.getName();

        try
        {
            int    size = istream.available();
            byte[] data = new byte[ istream.available() ];

            istream.read(data);
            DocumentDescriptor descriptor = new DocumentDescriptor(path,
                                                name);

            System.out.println("adding document: " + descriptor + " (" + size
                               + " bytes)");
            dataMap.put(descriptor, data);
            int            pathLength = path.length();
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

    public void processPOIFSWriterEvent(final POIFSWriterEvent event)
    {
        try
        {
            DocumentDescriptor descriptor =
                new DocumentDescriptor(event.getPath(), event.getName());

            System.out.println("looking up document: " + descriptor + " ("
                               + event.getLimit() + " bytes)");
            event.getStream().write(( byte [] ) dataMap.get(descriptor));
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

