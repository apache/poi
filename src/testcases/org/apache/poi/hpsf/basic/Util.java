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

package org.apache.poi.hpsf.basic;

import java.io.*;
import java.util.*;
import org.apache.poi.poifs.eventfilesystem.*;



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
     * @param file The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @return The POI files. The elements are ordered in the same way
     * as the files in the POI filesystem.
     */
    public static POIFile[] readPOIFiles(final File poiFs)
	throws FileNotFoundException, IOException
    {
	final List files = new ArrayList();
	POIFSReader r = new POIFSReader();
	r.registerListener(new POIFSReaderListener()
	    {
		public void processPOIFSReaderEvent(POIFSReaderEvent event)
		{
		    try
		    {
			POIFile f = new POIFile();
			f.setName(event.getName());
			f.setPath(event.getPath());
			InputStream in = event.getStream();
			ByteArrayOutputStream out =
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
	    });
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
	Properties p = System.getProperties();
	List names = new LinkedList();
	for (Iterator i = p.keySet().iterator(); i.hasNext();)
	    names.add(i.next());
	Collections.sort(names);
	for (Iterator i = names.iterator(); i.hasNext();)
        {
	    String name = (String) i.next();
	    String value = (String) p.get(name);
	    System.out.println(name + ": " + value);
	}
	System.out.println("Current directory: " +
			   System.getProperty("user.dir"));
    }

}
