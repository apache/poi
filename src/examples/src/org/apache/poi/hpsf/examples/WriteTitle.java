/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hpsf.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * <p>This class is a simple sample application showing how to create a property
 * set and write it to disk.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2003-09-12
 */
public class WriteTitle
{
    /**
     * <p>Runs the example program.</p>
     *
     * @param args Command-line arguments. The first and only command-line 
     * argument is the name of the POI file system to create.
     * @throws IOException if any I/O exception occurs.
     * @throws WritingNotSupportedException if HPSF does not (yet) support 
     * writing a certain property type.
     */
    public static void main(final String[] args)
    throws WritingNotSupportedException, IOException
    {
        /* Check whether we have exactly one command-line argument. */
        if (args.length != 1)
        {
            System.err.println("Usage: " + WriteTitle.class.getName() +
                               "destinationPOIFS");
            System.exit(1);
        }

        final String fileName = args[0];
        final POIFSFileSystem poiFs = new POIFSFileSystem();

        /* Create a mutable property set. Initially it contains a single section
         * with no properties. */
        final MutablePropertySet mps = new MutablePropertySet();

        /* Retrieve the section the property set already contains. */
        final MutableSection ms = (MutableSection) mps.getSections().get(0);

        /* Turn the property set into a summary information property. This is
         * done by setting the format ID of its first section to
         * SectionIDMap.SUMMARY_INFORMATION_ID. */
        ms.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);

        /* Create an empty property. */    
        final MutableProperty p = new MutableProperty();

        /* Fill the property with appropriate settings so that it specifies the
         * document's title. */
        p.setID(PropertyIDMap.PID_TITLE);
        p.setType(Variant.VT_LPWSTR);
        p.setValue("Sample title");

        /* For writing the property set into a POI file system it has to be
         * handed over to the POIFS.createDocument() method as an input stream
         * which produces the bytes making out the property set stream. */
        final InputStream is = mps.toInputStream();

        /* Create the summary information property set in the POI file
         * system. It is given the default name most (if not all) summary
         * information property sets have. */
        poiFs.createDocument(is, SummaryInformation.DEFAULT_STREAM_NAME);

        /* Write the whole POI file system to a disk file. */
        poiFs.writeFilesystem(new FileOutputStream(fileName));
    }

}
