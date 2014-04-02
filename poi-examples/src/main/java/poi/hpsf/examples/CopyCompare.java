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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hpsf.HPSFRuntimeException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Util;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;

/**
 * <p>This class copies a POI file system to a new file and compares the copy
 * with the original.</p>
 * 
 * <p>Property set streams are copied logically, i.e. the application
 * establishes a {@link org.apache.poi.hpsf.PropertySet} of an original property
 * set, creates a {@link org.apache.poi.hpsf.MutablePropertySet} from the
 * {@link org.apache.poi.hpsf.PropertySet} and writes the
 * {@link org.apache.poi.hpsf.MutablePropertySet} to the destination POI file
 * system. - Streams which are no property set streams are copied bit by
 * bit.</p>
 * 
 * <p>The comparison of the POI file systems is done logically. That means that
 * the two disk files containing the POI file systems do not need to be
 * exactly identical. However, both POI file systems must contain the same
 * files, and most of these files must be bitwise identical. Property set
 * streams, however, are compared logically: they must have the same sections
 * with the same attributs, and the sections must contain the same properties.
 * Details like the ordering of the properties do not matter.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class CopyCompare
{
    /**
     * <p>Runs the example program. The application expects one or two
     * arguments:</p>
     * 
     * <ol>
     * 
     * <li><p>The first argument is the disk file name of the POI filesystem to
     * copy.</p></li>
     * 
     * <li><p>The second argument is optional. If it is given, it is the name of
     * a disk file the copy of the POI filesystem will be written to. If it is
     * not given, the copy will be written to a temporary file which will be
     * deleted at the end of the program.</p></li>
     * 
     * </ol>
     *
     * @param args Command-line arguments.
     * @exception MarkUnsupportedException if a POI document stream does not
     * support the mark() operation.
     * @exception NoPropertySetStreamException if the application tries to
     * create a property set from a POI document stream that is not a property
     * set stream.
     * @exception IOException if any I/O exception occurs.
     * @exception UnsupportedEncodingException if a character encoding is not
     * supported.
     */
    public static void main(final String[] args)
    throws NoPropertySetStreamException, MarkUnsupportedException,
           UnsupportedEncodingException, IOException
    {
        String originalFileName = null;
        String copyFileName = null;

        /* Check the command-line arguments. */
        if (args.length == 1)
        {
            originalFileName = args[0];
            File f = TempFile.createTempFile("CopyOfPOIFileSystem-", ".ole2");
            f.deleteOnExit();
            copyFileName = f.getAbsolutePath();
        }
        else if (args.length == 2)
        {
            originalFileName = args[0];
            copyFileName = args[1];
        }
        else
        {
            System.err.println("Usage: " + CopyCompare.class.getName() +
                               "originPOIFS [copyPOIFS]");
            System.exit(1);
        }

        /* Read the origin POIFS using the eventing API. The real work is done
         * in the class CopyFile which is registered here as a POIFSReader. */
        final POIFSReader r = new POIFSReader();
        final CopyFile cf = new CopyFile(copyFileName);
        r.registerListener(cf);
        r.read(new FileInputStream(originalFileName));
        
        /* Write the new POIFS to disk. */
        cf.close();

        /* Read all documents from the original POI file system and compare them
         * with the equivalent document from the copy. */
        final POIFSFileSystem opfs =
            new POIFSFileSystem(new FileInputStream(originalFileName));
        final POIFSFileSystem cpfs =
            new POIFSFileSystem(new FileInputStream(copyFileName));

        final DirectoryEntry oRoot = opfs.getRoot();
        final DirectoryEntry cRoot = cpfs.getRoot();
        final StringBuffer messages = new StringBuffer();
        if (equal(oRoot, cRoot, messages))
            System.out.println("Equal");
        else
            System.out.println("Not equal: " + messages.toString());
    }



    /**
     * <p>Compares two {@link DirectoryEntry} instances of a POI file system.
     * The directories must contain the same streams with the same names and
     * contents.</p>
     *
     * @param d1 The first directory.
     * @param d2 The second directory.
     * @param msg The method may append human-readable comparison messages to
     * this string buffer. 
     * @return <code>true</code> if the directories are equal, else
     * <code>false</code>.
     * @exception MarkUnsupportedException if a POI document stream does not
     * support the mark() operation.
     * @exception NoPropertySetStreamException if the application tries to
     * create a property set from a POI document stream that is not a property
     * set stream.
     * @throws UnsupportedEncodingException 
     * @exception IOException if any I/O exception occurs.
     */
    private static boolean equal(final DirectoryEntry d1,
                                 final DirectoryEntry d2,
                                 final StringBuffer msg)
    throws NoPropertySetStreamException, MarkUnsupportedException,
           UnsupportedEncodingException, IOException
    {
        boolean equal = true;
        /* Iterate over d1 and compare each entry with its counterpart in d2. */
        for (final Iterator i = d1.getEntries(); equal && i.hasNext();)
        {
            final Entry e1 = (Entry) i.next();
            final String n1 = e1.getName();
            Entry e2 = null;
            try
            {
                e2 = d2.getEntry(n1);
            }
            catch (FileNotFoundException ex)
            {
                msg.append("Document \"" + e1 + "\" exists, document \"" +
                           e2 + "\" does not.\n");
                equal = false;
                break;
            }

            if (e1.isDirectoryEntry() && e2.isDirectoryEntry())
                equal = equal((DirectoryEntry) e1, (DirectoryEntry) e2, msg);
            else if (e1.isDocumentEntry() && e2.isDocumentEntry())
                equal = equal((DocumentEntry) e1, (DocumentEntry) e2, msg);
            else
            {
                msg.append("One of \"" + e1 + "\" and \"" + e2 + "\" is a " +
                           "document while the other one is a directory.\n");
                equal = false;
            }
        }

        /* Iterate over d2 just to make sure that there are no entries in d2
         * that are not in d1. */
        for (final Iterator i = d2.getEntries(); equal && i.hasNext();)
        {
            final Entry e2 = (Entry) i.next();
            final String n2 = e2.getName();
            Entry e1 = null;
            try
            {
                e1 = d1.getEntry(n2);
            }
            catch (FileNotFoundException ex)
            {
                msg.append("Document \"" + e2 + "\" exitsts, document \"" +
                           e1 + "\" does not.\n");
                equal = false;
                break;
            }
        }
        return equal;
    }



    /**
     * <p>Compares two {@link DocumentEntry} instances of a POI file system.
     * Documents that are not property set streams must be bitwise identical.
     * Property set streams must be logically equal.</p>
     *
     * @param d1 The first document.
     * @param d2 The second document.
     * @param msg The method may append human-readable comparison messages to
     * this string buffer. 
     * @return <code>true</code> if the documents are equal, else
     * <code>false</code>.
     * @exception MarkUnsupportedException if a POI document stream does not
     * support the mark() operation.
     * @exception NoPropertySetStreamException if the application tries to
     * create a property set from a POI document stream that is not a property
     * set stream.
     * @throws UnsupportedEncodingException 
     * @exception IOException if any I/O exception occurs.
     */
    private static boolean equal(final DocumentEntry d1, final DocumentEntry d2,
                                 final StringBuffer msg)
    throws NoPropertySetStreamException, MarkUnsupportedException,
           UnsupportedEncodingException, IOException
    {
        boolean equal = true;
        final DocumentInputStream dis1 = new DocumentInputStream(d1);
        final DocumentInputStream dis2 = new DocumentInputStream(d2);
        if (PropertySet.isPropertySetStream(dis1) &&
            PropertySet.isPropertySetStream(dis2))
        {
            final PropertySet ps1 = PropertySetFactory.create(dis1);
            final PropertySet ps2 = PropertySetFactory.create(dis2);
            equal = ps1.equals(ps2);
            if (!equal)
            {
                msg.append("Property sets are not equal.\n");
                return equal;
            }
        }
        else
        {
            int i1;
            int i2;
            do
            {
                i1 = dis1.read();
                i2 = dis2.read();
                if (i1 != i2)
                {
                    equal = false;
                    msg.append("Documents are not equal.\n");
                    break;
                }
            }
            while (equal && i1 == -1);
        }
        return true;
    }



    /**
     * <p>This class does all the work. Its method {@link
     * #processPOIFSReaderEvent(POIFSReaderEvent)} is called for each file in
     * the original POI file system. Except for property set streams it copies
     * everything unmodified to the destination POI filesystem. Property set
     * streams are copied by creating a new {@link PropertySet} from the
     * original property set by using the {@link
     * MutablePropertySet#MutablePropertySet(PropertySet)} constructor.</p>
     */
    static class CopyFile implements POIFSReaderListener
    {
        String dstName;
        OutputStream out;
        POIFSFileSystem poiFs;


        /**
         * <p>The constructor of a {@link CopyFile} instance creates the target
         * POIFS. It also stores the name of the file the POIFS will be written
         * to once it is complete.</p>
         * 
         * @param dstName The name of the disk file the destination POIFS is to
         * be written to.
         */
        public CopyFile(final String dstName)
        {
            this.dstName = dstName;
            poiFs = new POIFSFileSystem();
        }


        /**
         * <p>The method is called by POI's eventing API for each file in the
         * origin POIFS.</p>
         */
        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            /* The following declarations are shortcuts for accessing the
             * "event" object. */
            final POIFSDocumentPath path = event.getPath();
            final String name = event.getName();
            final DocumentInputStream stream = event.getStream();

            Throwable t = null;

            try
            {
                /* Find out whether the current document is a property set
                 * stream or not. */
                if (PropertySet.isPropertySetStream(stream))
                {
                    /* Yes, the current document is a property set stream.
                     * Let's create a PropertySet instance from it. */
                    PropertySet ps = null;
                    try
                    {
                        ps = PropertySetFactory.create(stream);
                    }
                    catch (NoPropertySetStreamException ex)
                    {
                        /* This exception will not be thrown because we already
                         * checked above. */
                    }

                    /* Copy the property set to the destination POI file
                     * system. */
                    copy(poiFs, path, name, ps);
                }
                else
                    /* No, the current document is not a property set stream. We
                     * copy it unmodified to the destination POIFS. */
                    copy(poiFs, event.getPath(), event.getName(), stream);
            }
            catch (MarkUnsupportedException ex)
            {
                t = ex;
            }
            catch (IOException ex)
            {
                t = ex;
            }
            catch (WritingNotSupportedException ex)
            {
                t = ex;
            }

            /* According to the definition of the processPOIFSReaderEvent method
             * we cannot pass checked exceptions to the caller. The following
             * lines check whether a checked exception occured and throws an
             * unchecked exception. The message of that exception is that of
             * the underlying checked exception. */
            if (t != null)
            {
                throw new HPSFRuntimeException
                    ("Could not read file \"" + path + "/" + name +
                     "\". Reason: " + Util.toString(t));
            }
        }



        /**
         * <p>Writes a {@link PropertySet} to a POI filesystem.</p>
         *
         * @param poiFs The POI filesystem to write to.
         * @param path The file's path in the POI filesystem.
         * @param name The file's name in the POI filesystem.
         * @param ps The property set to write.
         * @throws WritingNotSupportedException 
         * @throws IOException 
         */
        public void copy(final POIFSFileSystem poiFs,
                         final POIFSDocumentPath path,
                         final String name,
                         final PropertySet ps)
            throws WritingNotSupportedException, IOException
        {
            final DirectoryEntry de = getPath(poiFs, path);
            final MutablePropertySet mps = new MutablePropertySet(ps);
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
         * @throws IOException 
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
         * <p>Writes the POI file system to a disk file.</p>
         *
         * @throws FileNotFoundException
         * @throws IOException
         */
        public void close() throws FileNotFoundException, IOException
        {
            out = new FileOutputStream(dstName);
            poiFs.writeFilesystem(out);
            out.close();
        }



        /** Contains the directory paths that have already been created in the
         * output POI filesystem and maps them to their corresponding
         * {@link org.apache.poi.poifs.filesystem.DirectoryNode}s. */
        private final Map paths = new HashMap();



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
                DirectoryEntry de = (DirectoryEntry) paths.get(s);
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
                throw new RuntimeException(ex.toString());
                /* FIXME (2): Replace the previous line by the following once we
                 * no longer need JDK 1.3 compatibility. */
                // throw new RuntimeException(ex);
            }
        }
    }

}
