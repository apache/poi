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
import java.io.UnsupportedEncodingException;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.HPSFRuntimeException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;

/**
 * <p>This class copies a POI file system to a new file and compares the copy
 * with the original.</p>
 * <p>
 * <p>Property set streams are copied logically, i.e. the application
 * establishes a {@link org.apache.poi.hpsf.PropertySet} of an original property
 * set, creates a {@link org.apache.poi.hpsf.PropertySet} and writes the
 * {@link org.apache.poi.hpsf.PropertySet} to the destination POI file
 * system. - Streams which are no property set streams are copied bit by
 * bit.</p>
 * <p>
 * <p>The comparison of the POI file systems is done logically. That means that
 * the two disk files containing the POI file systems do not need to be
 * exactly identical. However, both POI file systems must contain the same
 * files, and most of these files must be bitwise identical. Property set
 * streams, however, are compared logically: they must have the same sections
 * with the same attributes, and the sections must contain the same properties.
 * Details like the ordering of the properties do not matter.</p>
 */
@SuppressWarnings({"java:S106","java:S4823"})
public final class CopyCompare {
    private CopyCompare() {}

    /**
     * Runs the example program. The application expects one or two arguments:
     *
     * <ol>
     * <li>The first argument is the disk file name of the POI filesystem to copy.</li>
     * <li>The second argument is optional. If it is given, it is the name of
     * a disk file the copy of the POI filesystem will be written to. If it is
     * not given, the copy will be written to a temporary file which will be
     * deleted at the end of the program.</li>
     * </ol>
     *
     * @param args Command-line arguments.
     * @throws IOException                  if any I/O exception occurs.
     * @throws UnsupportedEncodingException if a character encoding is not
     *                                      supported.
     */
    public static void main(final String[] args) throws IOException {
        String originalFileName = null;
        String copyFileName = null;

        // Check the command-line arguments.
        if (args.length == 1) {
            originalFileName = args[0];
            File f = TempFile.createTempFile("CopyOfPOIFileSystem-", ".ole2");
            f.deleteOnExit();
            copyFileName = f.getAbsolutePath();
        } else if (args.length == 2) {
            originalFileName = args[0];
            copyFileName = args[1];
        } else {
            System.err.println("Usage: CopyCompare originPOIFS [copyPOIFS]");
            System.exit(1);
        }


        // Read the origin POIFS using the eventing API.
        final POIFSReader r = new POIFSReader();
        try (final POIFSFileSystem poiFs = new POIFSFileSystem();
             OutputStream fos = new FileOutputStream(copyFileName)) {
            r.registerListener(e -> handleEvent(poiFs, e));
            r.setNotifyEmptyDirectories(true);

            r.read(new File(originalFileName));

            // Write the new POIFS to disk.
            poiFs.writeFilesystem(fos);
        }

        // Read all documents from the original POI file system and compare them with
        // the equivalent document from the copy.
        try (POIFSFileSystem opfs = new POIFSFileSystem(new File(originalFileName));
             POIFSFileSystem cpfs = new POIFSFileSystem(new File(copyFileName))) {
            final DirectoryEntry oRoot = opfs.getRoot();
            final DirectoryEntry cRoot = cpfs.getRoot();
            System.out.println(EntryUtils.areDirectoriesIdentical(oRoot, cRoot) ? "Equal" : "Not equal");
        }
    }

    private interface InputStreamSupplier {
        InputStream get() throws IOException, WritingNotSupportedException;
    }

    /**
     * The method is called by POI's eventing API for each file in the origin POIFS.
     */
    public static void handleEvent(final POIFSFileSystem poiFs, final POIFSReaderEvent event) {
        // The following declarations are shortcuts for accessing the "event" object.
        final DocumentInputStream stream = event.getStream();

        try {

            // Find out whether the current document is a property set stream or not.
            InputStreamSupplier su;
            if (stream != null && PropertySet.isPropertySetStream(stream)) {
                // Yes, the current document is a property set stream. Let's create
                // a PropertySet instance from it.
                PropertySet ps = PropertySetFactory.create(stream);

                // Copy the property set to the destination POI file system.
                final PropertySet mps;
                if (ps instanceof DocumentSummaryInformation) {
                    mps = new DocumentSummaryInformation(ps);
                } else if (ps instanceof SummaryInformation) {
                    mps = new SummaryInformation(ps);
                } else {
                    mps = new PropertySet(ps);
                }
                su = mps::toInputStream;
            } else {
                // No, the current document is not a property set stream.
                // We copy it unmodified to the destination POIFS.
                su = event::getStream;
            }

            try (InputStream is = su.get()) {
                final POIFSDocumentPath path = event.getPath();

                // Ensures that the directory hierarchy for a document in a POI fileystem is in place.
                // Get the root directory. It does not have to be created since it always exists in a POIFS.
                DirectoryEntry de = poiFs.getRoot();
                if (File.separator.equals(path.toString())) {
                    de.setStorageClsid(event.getStorageClassId());
                }

                for (int i=0; i<path.length(); i++) {
                    String subDir = path.getComponent(i);
                    if (de.hasEntry(subDir)) {
                        de = (DirectoryEntry)de.getEntry(subDir);
                    } else {
                        de = de.createDirectory(subDir);
                        if (i == path.length()-1) {
                            de.setStorageClsid(event.getStorageClassId());
                        }
                    }
                }

                if (event.getName() != null) {
                    de.createDocument(event.getName(), is);
                }
            }

        } catch (HPSFException | IOException ex) {
            // According to the definition of the processPOIFSReaderEvent method we cannot pass checked
            // exceptions to the caller.
            throw new HPSFRuntimeException("Could not read file " + event.getPath() + "/" + event.getName(), ex);
        }
    }
}
