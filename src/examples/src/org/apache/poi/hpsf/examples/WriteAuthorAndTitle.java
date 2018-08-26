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
import java.util.HashMap;
import java.util.Map;

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
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
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
public final class WriteAuthorAndTitle {
    private WriteAuthorAndTitle() {}

    /**
     * <p>Runs the example program.</p>
     *
     * @param args Command-line arguments. The first command-line argument must
     * be the name of a POI filesystem to read.
     * @throws IOException if any I/O exception occurs.
     */
    public static void main(final String[] args) throws IOException
    {
        /* Check whether we have exactly two command-line arguments. */
        if (args.length != 2)
        {
            System.err.println("Usage: " + WriteAuthorAndTitle.class.getName() +
                               " originPOIFS destinationPOIFS");
            System.exit(1);
        }
        
        /* Read the names of the origin and destination POI filesystems. */
        final String srcName = args[0];
        final String dstName = args[1];

        /* Read the origin POIFS using the eventing API. The real work is done
         * in the class ModifySICopyTheRest which is registered here as a
         * POIFSReader. */
        final POIFSReader r = new POIFSReader();
        final ModifySICopyTheRest msrl = new ModifySICopyTheRest(dstName);
        r.registerListener(msrl);
        r.read(new File(srcName));

        /* Write the new POIFS to disk. */
        msrl.close();
    }



    /**
     * <p>This class does all the work. As its name implies it modifies a
     * summary information property set and copies everything else unmodified
     * to the destination POI filesystem. Since an instance of it is registered
     * as a {@link POIFSReader} its method {@link 
     * #processPOIFSReaderEvent(POIFSReaderEvent)} is called for each document
     * in the origin POIFS.</p>
     */
    static class ModifySICopyTheRest implements POIFSReaderListener
    {
        private String dstName;
        private OutputStream out;
        private POIFSFileSystem poiFs;


        /**
         * The constructor of a {@link ModifySICopyTheRest} instance creates
         * the target POIFS. It also stores the name of the file the POIFS will
         * be written to once it is complete.
         * 
         * @param dstName The name of the disk file the destination POIFS is to
         * be written to.
         */
        ModifySICopyTheRest(final String dstName)
        {
            this.dstName = dstName;
            poiFs = new POIFSFileSystem();
        }


        /**
         * The method is called by POI's eventing API for each file in the
         * origin POIFS.
         */
        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            /* The following declarations are shortcuts for accessing the
             * "event" object. */
            final POIFSDocumentPath path = event.getPath();
            final String name = event.getName();
            final DocumentInputStream stream = event.getStream();

            Throwable t = null;

            try {
                /* Find out whether the current document is a property set
                 * stream or not. */
                if (PropertySet.isPropertySetStream(stream)) {
                    try {
                        /* Yes, the current document is a property set stream.
                         * Let's create a PropertySet instance from it. */
                        PropertySet ps = PropertySetFactory.create(stream);

                        /* Now we know that we really have a property set. The next
                         * step is to find out whether it is a summary information
                         * or not. */
                        if (ps.isSummaryInformation()) {
                            /* Yes, it is a summary information. We will modify it
                             * and write the result to the destination POIFS. */
                            editSI(poiFs, path, name, ps);
                        } else {
                            /* No, it is not a summary information. We don't care
                             * about its internals and copy it unmodified to the
                             * destination POIFS. */
                            copy(poiFs, path, name, ps);
                        }
                    } catch (NoPropertySetStreamException ex) {
                        /* This exception will not be thrown because we already
                         * checked above. */
                    }
                } else {
                    /* No, the current document is not a property set stream. We
                     * copy it unmodified to the destination POIFS. */
                    copy(poiFs, event.getPath(), event.getName(), stream);
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
         * <p>Receives a summary information property set modifies (or creates)
         * its "author" and "title" properties and writes the result under the
         * same path and name as the origin to a destination POI filesystem.</p>
         *
         * @param poiFs The POI filesystem to write to.
         * @param path The original (and destination) stream's path.
         * @param name The original (and destination) stream's name.
         * @param si The property set. It should be a summary information
         * property set.
         */
        void editSI(final POIFSFileSystem poiFs,
                           final POIFSDocumentPath path,
                           final String name,
                           final PropertySet si)
        throws WritingNotSupportedException, IOException
            
        {
            /* Get the directory entry for the target stream. */
            final DirectoryEntry de = getPath(poiFs, path);

            /* Create a mutable property set as a copy of the original read-only
             * property set. */
            final PropertySet mps = new PropertySet(si);
            
            /* Retrieve the section containing the properties to modify. A
             * summary information property set contains exactly one section. */
            final Section s = mps.getSections().get(0);

            /* Set the properties. */
            s.setProperty(PropertyIDMap.PID_AUTHOR, Variant.VT_LPSTR,
                          "Rainer Klute");
            s.setProperty(PropertyIDMap.PID_TITLE, Variant.VT_LPWSTR,
                          "Test");

            /* Create an input stream containing the bytes the property set
             * stream consists of. */
            final InputStream pss = mps.toInputStream();

            /* Write the property set stream to the POIFS. */
            de.createDocument(name, pss);
        }


        /**
         * <p>Writes a {@link PropertySet} to a POI filesystem. This method is
         * simpler than {@link #editSI} because the origin property set has just
         * to be copied.</p>
         *
         * @param poiFs The POI filesystem to write to.
         * @param path The file's path in the POI filesystem.
         * @param name The file's name in the POI filesystem.
         * @param ps The property set to write.
         */
        public void copy(final POIFSFileSystem poiFs,
                         final POIFSDocumentPath path,
                         final String name,
                         final PropertySet ps)
            throws WritingNotSupportedException, IOException
        {
            final DirectoryEntry de = getPath(poiFs, path);
            final PropertySet mps = new PropertySet(ps);
            de.createDocument(name, mps.toInputStream());
        }



        /**
         * <p>Copies the bytes from a {@link DocumentInputStream} to a new
         * stream in a POI filesystem.</p>
         *
         * @param poiFs The POI filesystem to write to.
         * @param path The source document's path.
         * @param name The source document's name.
         * @param stream The stream containing the source document.
         */
        public void copy(final POIFSFileSystem poiFs,
                         final POIFSDocumentPath path,
                         final String name,
                         final DocumentInputStream stream) throws IOException
        {
            final DirectoryEntry de = getPath(poiFs, path);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = stream.read()) != -1)
                out.write(c);
            stream.close();
            out.close();
            final InputStream in =
                new ByteArrayInputStream(out.toByteArray());
            de.createDocument(name, in);
        }


        /**
         * Writes the POI file system to a disk file.
         */
        public void close() throws IOException
        {
            out = new FileOutputStream(dstName);
            poiFs.writeFilesystem(out);
            out.close();
        }



        /** Contains the directory paths that have already been created in the
         * output POI filesystem and maps them to their corresponding
         * {@link org.apache.poi.poifs.filesystem.DirectoryNode}s. */
        private final Map<String, DirectoryEntry> paths = new HashMap<>();



        /**
         * <p>Ensures that the directory hierarchy for a document in a POI
         * fileystem is in place. When a document is to be created somewhere in
         * a POI filesystem its directory must be created first. This method
         * creates all directories between the POI filesystem root and the
         * directory the document should belong to which do not yet exist.</p>
         * 
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
         * in, if needed.
         * @param path The document's path. This method creates those directory
         * components of this hierarchy which do not yet exist.
         * @return The directory entry of the document path's parent. The caller
         * should use this {@link DirectoryEntry} to create documents in it.
         */
        public DirectoryEntry getPath(final POIFSFileSystem poiFs,
                                      final POIFSDocumentPath path)
        {
            try
            {
                /* Check whether this directory has already been created. */
                final String s = path.toString();
                DirectoryEntry de = paths.get(s);
                if (de != null)
                    /* Yes: return the corresponding DirectoryEntry. */
                    return de;

                /* No: We have to create the directory - or return the root's
                 * DirectoryEntry. */
                int l = path.length();
                if (l == 0)
                    /* Get the root directory. It does not have to be created
                     * since it always exists in a POIFS. */
                    de = poiFs.getRoot();
                else
                {
                    /* Create a subordinate directory. The first step is to
                     * ensure that the parent directory exists: */
                    de = getPath(poiFs, path.getParent());
                    /* Now create the target directory: */
                    de = de.createDirectory(path.getComponent
                                            (path.length() - 1));
                }
                paths.put(s, de);
                return de;
            }
            catch (IOException ex)
            {
                /* This exception will be thrown if the directory already
                 * exists. However, since we have full control about directory
                 * creation we can ensure that this will never happen. */
                ex.printStackTrace(System.err);
                throw new RuntimeException(ex);
            }
        }
    }

}
