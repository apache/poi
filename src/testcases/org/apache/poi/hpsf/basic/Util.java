/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * @since 2002-07-20
 * @version $Id$
 */
public class Util
{

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



    /**
     * <p>Returns a textual representation of a {@link Throwable}, including a
     * stacktrace.</p>
     * 
     * @param t The {@link Throwable}
     * 
     * @return a string containing the output of a call to
     * <code>t.printStacktrace()</code>.
     */
    public static String toString(final Throwable t)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        try
        {
            sw.close();
            return sw.toString();
        }
        catch (IOException e)
        {
            final StringBuffer b = new StringBuffer(t.getMessage());
            b.append("\n");
            b.append("Could not create a stacktrace. Reason: ");
            b.append(e.getMessage());
            return b.toString();
        }
    }

}
