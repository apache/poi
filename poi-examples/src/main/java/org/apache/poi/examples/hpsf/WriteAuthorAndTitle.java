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

package org.apache.poi.examples.hpsf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.hpsf.HPSFRuntimeException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * <p>This class is a sample application which shows how to write or modify the
 * author and title property of an OLE 2 document. This could be done in two
 * different ways:</p>
 *
 * <ul>
 *
 * <li><p>The first approach is to open the OLE 2 file as a POI filesystem
 * (see class {@link POIFSFileSystem}), read the summary information property
 * set (see classes {@link SummaryInformation} and {@link PropertySet}), write
 * the author and title properties into it and write the property set back into
 * the POI filesystem.</p></li>
 *
 * <li><p>The second approach does not modify the original POI filesystem, but
 * instead creates a new one. All documents from the original POIFS are copied
 * to the destination POIFS, except for the summary information stream. The
 * latter is modified by setting the author and title property before writing
 * it to the destination POIFS. It there are several summary information streams
 * in the original POIFS - e.g. in subordinate directories - they are modified
 * just the same.</p></li>
 *
 * </ul>
 *
 * <p>This sample application takes the second approach. It expects the name of
 * the existing POI filesystem's name as its first command-line parameter and
 * the name of the output POIFS as the second command-line argument. The
 * program then works as described above: It copies nearly all documents
 * unmodified from the input POI filesystem to the output POI filesystem. If it
 * encounters a summary information stream it reads its properties. Then it sets
 * the "author" and "title" properties to new values and writes the modified
 * summary information stream into the output file.</p>
 *
 * <p>Further explanations can be found in the HPSF HOW-TO.</p>
 */
@SuppressWarnings({"java:S106","java:S4823"})
public final class WriteAuthorAndTitle {
    private WriteAuthorAndTitle() {}

    /**
     * <p>Runs the example program.</p>
     *
     * @param args Command-line arguments. The first command-line argument must
     * be the name of a POI filesystem to read.
     * @throws IOException if any I/O exception occurs.
     */
    public static void main(final String[] args) throws IOException {
        /* Check whether we have exactly two command-line arguments. */
        if (args.length != 2) {
            System.err.println("Usage: WriteAuthorAndTitle originPOIFS destinationPOIFS");
            System.exit(1);
        }

        /* Read the names of the origin and destination POI filesystems. */
        final String srcName = args[0];
        final String dstName = args[1];

        /* Read the origin POIFS using the eventing API. The real work is done
         * in the class ModifySICopyTheRest which is registered here as a
         * POIFSReader. */
        try (POIFSFileSystem poifs = new POIFSFileSystem();
             OutputStream out = new FileOutputStream(dstName)) {
            final POIFSReader r = new POIFSReader();
            r.registerListener(e -> handleEvent(poifs, e));
            r.read(new File(srcName));

            /* Write the new POIFS to disk. */
            poifs.writeFilesystem(out);
        }
    }

    private interface InputStreamSupplier {
        InputStream get() throws IOException, WritingNotSupportedException;
    }

    /**
     * The method is called by POI's eventing API for each file in the origin POIFS.
     */
    private static void handleEvent(final POIFSFileSystem poiFs, final POIFSReaderEvent event) {
        // The following declarations are shortcuts for accessing the "event" object.
        final DocumentInputStream stream = event.getStream();

        try {
            final InputStreamSupplier isSup;

            // Find out whether the current document is a property set stream or not.
            if (PropertySet.isPropertySetStream(stream)) {
                // Yes, the current document is a property set stream. Let's create a PropertySet instance from it.
                PropertySet ps = PropertySetFactory.create(stream);

                // Now we know that we really have a property set.
                // The next step is to find out whether it is a summary information or not.
                if (ps.isSummaryInformation()) {
                    // Create a mutable property set as a copy of the original read-only property set.
                    ps = new PropertySet(ps);

                    // Retrieve the section containing the properties to modify.
                    // A summary information property set contains exactly one section.
                    final Section s = ps.getSections().get(0);

                    // Set the properties.
                    s.setProperty(PropertyIDMap.PID_AUTHOR, Variant.VT_LPSTR, "Rainer Klute");
                    s.setProperty(PropertyIDMap.PID_TITLE, Variant.VT_LPWSTR, "Test");
                }

                isSup = ps::toInputStream;
            } else {
                // No, the current document is not a property set stream. We copy it unmodified to the destination POIFS.
                isSup = event::getStream;
            }

            try (InputStream is = isSup.get()) {
                final POIFSDocumentPath path = event.getPath();

                // Ensures that the directory hierarchy for a document in a POI fileystem is in place.
                // Get the root directory. It does not have to be created since it always exists in a POIFS.
                DirectoryEntry de = poiFs.getRoot();

                for (int i=0; i<path.length(); i++) {
                    String subDir = path.getComponent(i);
                    de = (de.hasEntry(subDir)) ? (DirectoryEntry)de.getEntry(subDir) : de.createDirectory(subDir);
                }

                de.createDocument(event.getName(), is);
            }

        } catch (MarkUnsupportedException | WritingNotSupportedException | IOException | NoPropertySetStreamException ex) {
            // According to the definition of the processPOIFSReaderEvent method we cannot pass checked
            // exceptions to the caller.
            throw new HPSFRuntimeException("Could not read file " + event.getPath() + "/" + event.getName(), ex);
        }
    }
}
