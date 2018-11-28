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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFRuntimeException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
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
    public static void main(final String[] args)
            throws UnsupportedEncodingException, IOException {
        String originalFileName = null;
        String copyFileName = null;

        /* Check the command-line arguments. */
        if (args.length == 1) {
            originalFileName = args[0];
            File f = TempFile.createTempFile("CopyOfPOIFileSystem-", ".ole2");
            f.deleteOnExit();
            copyFileName = f.getAbsolutePath();
        } else if (args.length == 2) {
            originalFileName = args[0];
            copyFileName = args[1];
        } else {
            System.err.println("Usage: " + CopyCompare.class.getName() +
                    "originPOIFS [copyPOIFS]");
            System.exit(1);
        }

        /* Read the origin POIFS using the eventing API. The real work is done
         * in the class CopyFile which is registered here as a POIFSReader. */
        final POIFSReader r = new POIFSReader();
        final CopyFile cf = new CopyFile(copyFileName);
        r.registerListener(cf);
        r.setNotifyEmptyDirectories(true);

        r.read(new File(originalFileName));

        /* Write the new POIFS to disk. */
        cf.close();

        /* Read all documents from the original POI file system and compare them
         * with the equivalent document from the copy. */
        try (POIFSFileSystem opfs = new POIFSFileSystem(new File(originalFileName));
             POIFSFileSystem cpfs = new POIFSFileSystem(new File(copyFileName))) {
            final DirectoryEntry oRoot = opfs.getRoot();
            final DirectoryEntry cRoot = cpfs.getRoot();
            System.out.println(EntryUtils.areDirectoriesIdentical(oRoot, cRoot) ? "Equal" : "Not equal");
        }
    }

    /**
     * <p>This class does all the work. Its method {@link
     * #processPOIFSReaderEvent(POIFSReaderEvent)} is called for each file in
     * the original POI file system. Except for property set streams it copies
     * everything unmodified to the destination POI filesystem. Property set
     * streams are copied by creating a new {@link PropertySet} from the
     * original property set by using the {@link
     * PropertySet#PropertySet(PropertySet)} constructor.</p>
     */
    static class CopyFile implements POIFSReaderListener {
        private String dstName;
        private OutputStream out;
        private POIFSFileSystem poiFs;


        /**
         * <p>The constructor of a {@link CopyFile} instance creates the target
         * POIFS. It also stores the name of the file the POIFS will be written
         * to once it is complete.</p>
         *
         * @param dstName The name of the disk file the destination POIFS is to
         *                be written to.
         */
        CopyFile(final String dstName) {
            this.dstName = dstName;
            poiFs = new POIFSFileSystem();
        }


        /**
         * <p>The method is called by POI's eventing API for each file in the
         * origin POIFS.</p>
         */
        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event) {
            /* The following declarations are shortcuts for accessing the
             * "event" object. */
            final POIFSDocumentPath path = event.getPath();
            final String name = event.getName();
            final DocumentInputStream stream = event.getStream();

            Throwable t = null;

            try {
                /* Find out whether the current document is a property set
                 * stream or not. */
                if (stream != null && PropertySet.isPropertySetStream(stream)) {
                    /* Yes, the current document is a property set stream.
                     * Let's create a PropertySet instance from it. */
                    PropertySet ps = null;
                    try {
                        ps = PropertySetFactory.create(stream);
                    } catch (NoPropertySetStreamException ex) {
                        /* This exception will not be thrown because we already
                         * checked above. */
                    }

                    /* Copy the property set to the destination POI file
                     * system. */
                    copy(poiFs, path, name, ps);
                } else {
                    /* No, the current document is not a property set stream. We
                     * copy it unmodified to the destination POIFS. */
                    copy(poiFs, path, name, stream);
                }
            } catch (MarkUnsupportedException | WritingNotSupportedException | IOException ex) {
                t = ex;
            }

            /* According to the definition of the processPOIFSReaderEvent method
             * we cannot pass checked exceptions to the caller. The following
             * lines check whether a checked exception occurred and throws an
             * unchecked exception. The message of that exception is that of
             * the underlying checked exception. */
            if (t != null) {
                throw new HPSFRuntimeException("Could not read file \"" + path + "/" + name, t);
            }
        }


        /**
         * Writes a {@link PropertySet} to a POI filesystem.
         *
         * @param poiFs The POI filesystem to write to.
         * @param path  The file's path in the POI filesystem.
         * @param name  The file's name in the POI filesystem.
         * @param ps    The property set to write.
         */
        public void copy(final POIFSFileSystem poiFs,
                         final POIFSDocumentPath path,
                         final String name,
                         final PropertySet ps)
                throws WritingNotSupportedException, IOException {
            final DirectoryEntry de = getPath(poiFs, path);
            final PropertySet mps;
            try {
                if (ps instanceof DocumentSummaryInformation) {
                    mps = new DocumentSummaryInformation(ps);
                } else if (ps instanceof SummaryInformation) {
                    mps = new SummaryInformation(ps);
                } else {
                    mps = new PropertySet(ps);
                }
            } catch (UnexpectedPropertySetTypeException e) {
                throw new IOException(e);
            }
            de.createDocument(name, mps.toInputStream());
        }


        /**
         * Copies the bytes from a {@link DocumentInputStream} to a new
         * stream in a POI filesystem.
         *
         * @param poiFs  The POI filesystem to write to.
         * @param path   The source document's path.
         * @param name   The source document's name.
         * @param stream The stream containing the source document.
         */
        public void copy(final POIFSFileSystem poiFs,
                         final POIFSDocumentPath path,
                         final String name,
                         final DocumentInputStream stream)
                throws IOException {
            // create the directories to the document
            final DirectoryEntry de = getPath(poiFs, path);
            // check the parameters after the directories have been created
            if (stream == null || name == null) {
                // Empty directory
                return;
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = stream.read()) != -1) {
                out.write(c);
            }
            stream.close();
            out.close();
            final InputStream in =
                    new ByteArrayInputStream(out.toByteArray());
            de.createDocument(name, in);
        }


        /**
         * Writes the POI file system to a disk file.
         */
        public void close() throws IOException {
            out = new FileOutputStream(dstName);
            poiFs.writeFilesystem(out);
            out.close();
        }


        /**
         * Contains the directory paths that have already been created in the
         * output POI filesystem and maps them to their corresponding
         * {@link org.apache.poi.poifs.filesystem.DirectoryNode}s.
         */
        private final Map<String, DirectoryEntry> paths = new HashMap<>();


        /**
         * <p>Ensures that the directory hierarchy for a document in a POI
         * fileystem is in place. When a document is to be created somewhere in
         * a POI filesystem its directory must be created first. This method
         * creates all directories between the POI filesystem root and the
         * directory the document should belong to which do not yet exist.</p>
         * <p>
         * <p>Unfortunately POI does not offer a simple method to interrogate
         * the POIFS whether a certain child node (file or directory) exists in
         * a directory. However, since we always start with an empty POIFS which
         * contains the root directory only and since each directory in the
         * POIFS is created by this method we can maintain the POIFS's directory
         * hierarchy ourselves: The {@link DirectoryEntry} of each directory
         * created is stored in a {@link Map}. The directories' path names map
         * to the corresponding {@link DirectoryEntry} instances.</p>
         *
         * @param poiFs The POI filesystem the directory hierarchy is created
         *              in, if needed.
         * @param path  The document's path. This method creates those directory
         *              components of this hierarchy which do not yet exist.
         * @return The directory entry of the document path's parent. The caller
         * should use this {@link DirectoryEntry} to create documents in it.
         */
        public DirectoryEntry getPath(final POIFSFileSystem poiFs,
                                      final POIFSDocumentPath path) {
            try {
                /* Check whether this directory has already been created. */
                final String s = path.toString();
                DirectoryEntry de = paths.get(s);
                if (de != null)
                    /* Yes: return the corresponding DirectoryEntry. */
                    return de;

                /* No: We have to create the directory - or return the root's
                 * DirectoryEntry. */
                int l = path.length();
                if (l == 0) {
                    /* Get the root directory. It does not have to be created
                     * since it always exists in a POIFS. */
                    de = poiFs.getRoot();
                } else {
                    /* Create a subordinate directory. The first step is to
                     * ensure that the parent directory exists: */
                    de = getPath(poiFs, path.getParent());
                    /* Now create the target directory: */
                    de = de.createDirectory(path.getComponent
                            (path.length() - 1));
                }
                paths.put(s, de);
                return de;
            } catch (IOException ex) {
                /* This exception will be thrown if the directory already
                 * exists. However, since we have full control about directory
                 * creation we can ensure that this will never happen. */
                throw new RuntimeException(ex);
            }
        }
    }

}
