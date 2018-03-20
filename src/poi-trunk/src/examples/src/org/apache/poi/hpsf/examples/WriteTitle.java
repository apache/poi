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

package org.apache.poi.hpsf.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * <p>This class is a simple sample application showing how to create a property
 * set and write it to disk.</p>
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

        /* Create a mutable property set. Initially it contains a single section
         * with no properties. */
        final PropertySet mps = new PropertySet();

        /* Retrieve the section the property set already contains. */
        final Section ms = mps.getSections().get(0);

        /* Turn the property set into a summary information property. This is
         * done by setting the format ID of its first section to
         * SectionIDMap.SUMMARY_INFORMATION_ID. */
        ms.setFormatID(SummaryInformation.FORMAT_ID);

        /* Create an empty property. */    
        final Property p = new Property();

        /* Fill the property with appropriate settings so that it specifies the
         * document's title. */
        p.setID(PropertyIDMap.PID_TITLE);
        p.setType(Variant.VT_LPWSTR);
        p.setValue("Sample title");

        /* Place the property into the section. */
        ms.setProperty(p);

        /* Create the POI file system the property set is to be written to. */
        try (final POIFSFileSystem poiFs = new POIFSFileSystem()) {
            /* For writing the property set into a POI file system it has to be
             * handed over to the POIFS.createDocument() method as an input stream
             * which produces the bytes making out the property set stream. */
            final InputStream is = mps.toInputStream();

            /* Create the summary information property set in the POI file
             * system. It is given the default name most (if not all) summary
             * information property sets have. */
            poiFs.createDocument(is, SummaryInformation.DEFAULT_STREAM_NAME);

            /* Write the whole POI file system to a disk file. */
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                poiFs.writeFilesystem(fos);
            }
        }
    }
}
