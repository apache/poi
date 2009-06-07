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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * <p>This is a sample application showing how to easily modify properties in
 * the summary information and in the document summary information. The
 * application reads the name of a POI filesystem from the command line and
 * performs the following actions:</p>
 * 
 * <ul>
 * 
 * <li><p>Open the POI filesystem.</p></li>
 * 
 * <li><p>Read the summary information.</p></li>
 * 
 * <li><p>Read and print the "author" property.</p></li>
 * 
 * <li><p>Change the author to "Rainer Klute".</p></li>
 * 
 * <li><p>Read the document summary information.</p></li>
 * 
 * <li><p>Read and print the "category" property.</p></li>
 * 
 * <li><p>Change the category to "POI example".</p></li>
 * 
 * <li><p>Read the custom properties (if available).</p></li>
 * 
 * <li><p>Insert a new custom property.</p></li>
 * 
 * <li><p>Write the custom properties back to the document summary
 * information.</p></li>
 * 
 * <li><p>Write the summary information to the POI filesystem.</p></li>
 * 
 * <li><p>Write the document summary information to the POI filesystem.</p></li>
 * 
 * <li><p>Write the POI filesystem back to the original file.</p></li>
 * 
 * </ol>
 * 
 * @author Rainer Klute <a
 *         href="mailto:klute@rainer-klute.de">klute@rainer-klute.de</a>
 */
public class ModifyDocumentSummaryInformation
{

    /**
     * <p>Main method - see class description.</p>
     *
     * @param args The command-line parameters.
     * @throws IOException 
     * @throws MarkUnsupportedException 
     * @throws NoPropertySetStreamException 
     * @throws UnexpectedPropertySetTypeException 
     * @throws WritingNotSupportedException 
     */
    public static void main(final String[] args) throws IOException,
            NoPropertySetStreamException, MarkUnsupportedException,
            UnexpectedPropertySetTypeException, WritingNotSupportedException
    {
        /* Read the name of the POI filesystem to modify from the command line.
         * For brevity to boundary check is performed on the command-line
         * arguments. */
        File poiFilesystem = new File(args[0]);

        /* Open the POI filesystem. */
        InputStream is = new FileInputStream(poiFilesystem);
        POIFSFileSystem poifs = new POIFSFileSystem(is);
        is.close();

        /* Read the summary information. */
        DirectoryEntry dir = poifs.getRoot();
        SummaryInformation si;
        try
        {
            DocumentEntry siEntry = (DocumentEntry)
                dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(siEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            si = new SummaryInformation(ps);
        }
        catch (FileNotFoundException ex)
        {
            /* There is no summary information yet. We have to create a new
             * one. */
            si = PropertySetFactory.newSummaryInformation();
        }

        /* Change the author to "Rainer Klute". Any former author value will
         * be lost. If there has been no author yet, it will be created. */
        si.setAuthor("Rainer Klute");
        System.out.println("Author changed to " + si.getAuthor() + ".");


        /* Handling the document summary information is analogous to handling
         * the summary information. An additional feature, however, are the
         * custom properties. */

        /* Read the document summary information. */
        DocumentSummaryInformation dsi;
        try
        {
            DocumentEntry dsiEntry = (DocumentEntry)
                dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(dsiEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            dsi = new DocumentSummaryInformation(ps);
        }
        catch (FileNotFoundException ex)
        {
            /* There is no document summary information yet. We have to create a
             * new one. */
            dsi = PropertySetFactory.newDocumentSummaryInformation();
        }

        /* Change the category to "POI example". Any former category value will
         * be lost. If there has been no category yet, it will be created. */
        dsi.setCategory("POI example");
        System.out.println("Category changed to " + dsi.getCategory() + ".");

        /* Read the custom properties. If there are no custom properties yet,
         * the application has to create a new CustomProperties object. It will
         * serve as a container for custom properties. */
        CustomProperties customProperties = dsi.getCustomProperties();
        if (customProperties == null)
            customProperties = new CustomProperties();
        
        /* Insert some custom properties into the container. */
        customProperties.put("Key 1", "Value 1");
        customProperties.put("Schl\u00fcssel 2", "Wert 2");
        customProperties.put("Sample Number", new Integer(12345));
        customProperties.put("Sample Boolean", new Boolean(true));
        customProperties.put("Sample Date", new Date());

        /* Read a custom property. */
        Object value = customProperties.get("Sample Number");

        /* Write the custom properties back to the document summary
         * information. */
        dsi.setCustomProperties(customProperties);

        /* Write the summary information and the document summary information
         * to the POI filesystem. */
        si.write(dir, SummaryInformation.DEFAULT_STREAM_NAME);
        dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        /* Write the POI filesystem back to the original file. Please note that
         * in production code you should never write directly to the origin
         * file! In case of a writing error everything would be lost. */
        OutputStream out = new FileOutputStream(poiFilesystem);
        poifs.writeFilesystem(out);
        out.close();
        }

}
