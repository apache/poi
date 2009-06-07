
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

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;



/**
 * <p>Static utility methods needed by the HPSF test cases.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
final class Util {

    /**
     * <p>Reads bytes from an input stream and writes them to an
     * output stream until end of file is encountered.</p>
     *
     * @param in the input stream to read from
     * 
     * @param out the output stream to write to
     * 
     * @exception IOException if an I/O exception occurs
     */
    public static void copy(final InputStream in, final OutputStream out)
        throws IOException
    {
        final int BUF_SIZE = 1000;
        byte[] b = new byte[BUF_SIZE];
        int read;
        boolean eof = false;
        while (!eof)
        {
            try
            {
                read = in.read(b, 0, BUF_SIZE);
                if (read > 0)
                    out.write(b, 0, read);
                else
                    eof = true;
            }
            catch (EOFException ex)
            {
                eof = true;
            }
        }
    }



    /**
     * <p>Reads all files from a POI filesystem and returns them as an
     * array of {@link POIFile} instances. This method loads all files
     * into memory and thus does not cope well with large POI
     * filessystems.</p>
     * 
     * @param poiFs The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @return The POI files. The elements are ordered in the same way
     * as the files in the POI filesystem.
     * 
     * @exception FileNotFoundException if the file containing the POI 
     * filesystem does not exist
     * 
     * @exception IOException if an I/O exception occurs
     */
    public static POIFile[] readPOIFiles(final File poiFs)
        throws FileNotFoundException, IOException
    {
        return readPOIFiles(poiFs, null);
    }



    /**
     * <p>Reads a set of files from a POI filesystem and returns them
     * as an array of {@link POIFile} instances. This method loads all
     * files into memory and thus does not cope well with large POI
     * filessystems.</p>
     * 
     * @param poiFs The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @param poiFiles The names of the POI files to be read.
     *
     * @return The POI files. The elements are ordered in the same way
     * as the files in the POI filesystem.
     * 
     * @exception FileNotFoundException if the file containing the POI 
     * filesystem does not exist
     * 
     * @exception IOException if an I/O exception occurs
     */
    public static POIFile[] readPOIFiles(final File poiFs,
                                         final String[] poiFiles)
        throws FileNotFoundException, IOException
    {
        final List files = new ArrayList();
        POIFSReader r = new POIFSReader();
        POIFSReaderListener pfl = new POIFSReaderListener()
        {
            public void processPOIFSReaderEvent(final POIFSReaderEvent event)
            {
                try
                {
                    final POIFile f = new POIFile();
                    f.setName(event.getName());
                    f.setPath(event.getPath());
                    final InputStream in = event.getStream();
                    final ByteArrayOutputStream out =
                        new ByteArrayOutputStream();
                    Util.copy(in, out);
                    out.close();
                    f.setBytes(out.toByteArray());
                    files.add(f);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        if (poiFiles == null)
            /* Register the listener for all POI files. */
            r.registerListener(pfl);
        else
            /* Register the listener for the specified POI files
             * only. */
            for (int i = 0; i < poiFiles.length; i++)
                r.registerListener(pfl, poiFiles[i]);

        /* Read the POI filesystem. */
        r.read(new FileInputStream(poiFs));
        POIFile[] result = new POIFile[files.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = (POIFile) files.get(i);
        return result;
    }



    /**
     * <p>Read all files from a POI filesystem which are property set streams
     * and returns them as an array of {@link org.apache.poi.hpsf.PropertySet}
     * instances.</p>
     * 
     * @param poiFs The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @return The property sets. The elements are ordered in the same way
     * as the files in the POI filesystem.
     * 
     * @exception FileNotFoundException if the file containing the POI 
     * filesystem does not exist
     * 
     * @exception IOException if an I/O exception occurs
     */
    public static POIFile[] readPropertySets(final File poiFs)
        throws FileNotFoundException, IOException
    {
        final List files = new ArrayList(7);
        final POIFSReader r = new POIFSReader();
        POIFSReaderListener pfl = new POIFSReaderListener()
        {
            public void processPOIFSReaderEvent(final POIFSReaderEvent event)
            {
                try
                {
                    final POIFile f = new POIFile();
                    f.setName(event.getName());
                    f.setPath(event.getPath());
                    final InputStream in = event.getStream();
                    if (PropertySet.isPropertySetStream(in))
                    {
                        final ByteArrayOutputStream out =
                            new ByteArrayOutputStream();
                        Util.copy(in, out);
                        out.close();
                        f.setBytes(out.toByteArray());
                        files.add(f);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };

        /* Register the listener for all POI files. */
        r.registerListener(pfl);

        /* Read the POI filesystem. */
        r.read(new FileInputStream(poiFs));
        POIFile[] result = new POIFile[files.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = (POIFile) files.get(i);
        return result;
    }



    /**
     * <p>Prints the system properties to System.out.</p>
     */
    public static void printSystemProperties()
    {
        final Properties p = System.getProperties();
        final List names = new LinkedList();
        for (Iterator i = p.keySet().iterator(); i.hasNext();)
            names.add(i.next());
        Collections.sort(names);
        for (final Iterator i = names.iterator(); i.hasNext();)
        {
            String name = (String) i.next();
            String value = (String) p.get(name);
            System.out.println(name + ": " + value);
        }
        System.out.println("Current directory: " +
                           System.getProperty("user.dir"));
    }
}
