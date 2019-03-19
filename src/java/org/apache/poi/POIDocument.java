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

package org.apache.poi;

import static org.apache.poi.hpsf.PropertySetFactory.newDocumentSummaryInformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIDecryptor;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This holds the common functionality for all POI
 *  Document classes.
 * Currently, this relates to Document Information Properties 
 */
public abstract class POIDocument implements Closeable {
    /** Holds metadata on our document */
    private SummaryInformation sInf;
    /** Holds further metadata on our document */
    private DocumentSummaryInformation dsInf;
    /**	The directory that our document lives in */
    private DirectoryNode directory;

    /** For our own logging use */
    private static final POILogger logger = POILogFactory.getLogger(POIDocument.class);

    /* Have the property streams been read yet? (Only done on-demand) */
    private boolean initialized;

    /**
     * Constructs a POIDocument with the given directory node.
     *
     * @param dir The {@link DirectoryNode} where information is read from.
     */
    protected POIDocument(DirectoryNode dir) {
    	this.directory = dir;
    }

    /**
     * Constructs from the default POIFS
     * 
     * @param fs the filesystem the document is read from
     */
    protected POIDocument(POIFSFileSystem fs) {
        this(fs.getRoot());
     }

    /**
     * Fetch the Document Summary Information of the document
     * 
     * @return The Document Summary Information or null 
     *      if it could not be read for this document.
     */
    public DocumentSummaryInformation getDocumentSummaryInformation() {
        if(!initialized) {
            readProperties();
        }
        return dsInf;
    }

    /** 
     * Fetch the Summary Information of the document
     * 
     * @return The Summary information for the document or null
     *      if it could not be read for this document.
     */
    public SummaryInformation getSummaryInformation() {
        if(!initialized) {
            readProperties();
        }
        return sInf;
    }
	
    /**
     * Will create whichever of SummaryInformation
     *  and DocumentSummaryInformation (HPSF) properties
     *  are not already part of your document.
     * This is normally useful when creating a new
     *  document from scratch.
     * If the information properties are already there,
     *  then nothing will happen.
     */
    public void createInformationProperties() {
        if (!initialized) {
            readProperties();
        }
        if (sInf == null) {
            sInf = PropertySetFactory.newSummaryInformation();
        }
        if (dsInf == null) {
            dsInf = newDocumentSummaryInformation();
        }
    }

    /**
     * Find, and create objects for, the standard
     *  Document Information Properties (HPSF).
     * If a given property set is missing or corrupt,
     *  it will remain null;
     */
    protected void readProperties() {
        if (initialized) {
            return;
        }
        DocumentSummaryInformation dsi = readPropertySet(DocumentSummaryInformation.class, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        if (dsi != null) {
            dsInf = dsi;
        }
        SummaryInformation si = readPropertySet(SummaryInformation.class, SummaryInformation.DEFAULT_STREAM_NAME);
        if (si != null) {
            sInf = si;
        }

        // Mark the fact that we've now loaded up the properties
        initialized = true;
    }

    @SuppressWarnings("unchecked")
    private <T> T readPropertySet(Class<T> clazz, String name) {
        String localName = clazz.getName().substring(clazz.getName().lastIndexOf('.')+1);
        try {
            PropertySet ps = getPropertySet(name);
            if (clazz.isInstance(ps)) {
                return (T)ps;
            } else if (ps != null) {
                logger.log(POILogger.WARN, localName+" property set came back with wrong class - "+ps.getClass().getName());
            } else {
                logger.log(POILogger.WARN, localName+" property set came back as null");
            }
        } catch (IOException e) {
            logger.log(POILogger.ERROR, "can't retrieve property set", e);
        }
        return null;
    }
    
    /** 
     * For a given named property entry, either return it or null if
     *  if it wasn't found
     *  
     *  @param setName The property to read
     *  @return The value of the given property or null if it wasn't found.
     *
     * @throws IOException If retrieving properties fails
     */
    @SuppressWarnings("WeakerAccess")
    protected PropertySet getPropertySet(String setName) throws IOException {
        return getPropertySet(setName, getEncryptionInfo());
    }
    
    /** 
     * For a given named property entry, either return it or null if
     *  if it wasn't found
     *  
     *  @param setName The property to read
     *  @param encryptionInfo the encryption descriptor in case of cryptoAPI encryption
     *  @return The value of the given property or null if it wasn't found.
     *
     * @throws IOException If retrieving properties fails
     */
    @SuppressWarnings("WeakerAccess")
    protected PropertySet getPropertySet(String setName, EncryptionInfo encryptionInfo) throws IOException {
        DirectoryNode dirNode = directory;
        
        POIFSFileSystem encPoifs = null;
        String step = "getting";
        try {
            if (encryptionInfo != null && encryptionInfo.isDocPropsEncrypted()) {
                step = "getting encrypted";
                String encryptedStream = getEncryptedPropertyStreamName();
                if (!dirNode.hasEntry(encryptedStream)) {
                    throw new EncryptedDocumentException("can't find encrypted property stream '"+encryptedStream+"'");
                }
                CryptoAPIDecryptor dec = (CryptoAPIDecryptor)encryptionInfo.getDecryptor();
                encPoifs = dec.getSummaryEntries(dirNode, encryptedStream);
                dirNode = encPoifs.getRoot();
            }
            
            //directory can be null when creating new documents
            if (dirNode == null || !dirNode.hasEntry(setName)) {
                return null;
            }
    
            // Find the entry, and get an input stream for it
            step = "getting";
            try (DocumentInputStream dis = dirNode.createDocumentInputStream(dirNode.getEntry(setName))) {
                // Create the Property Set
                step = "creating";
                return PropertySetFactory.create(dis);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error "+step+" property set with name " + setName, e);
        } finally {
            IOUtils.closeQuietly(encPoifs);
        }
    }
    
    /**
     * Writes out the updated standard Document Information Properties (HPSF)
     *  into the currently open POIFSFileSystem
     * 
     * @throws IOException if an error when writing to the open
     *      {@link POIFSFileSystem} occurs
     */
    protected void writeProperties() throws IOException {
        validateInPlaceWritePossible();
        writeProperties(directory.getFileSystem(), null);
    }

    /**
     * Writes out the standard Document Information Properties (HPSF)
     * @param outFS the POIFSFileSystem to write the properties into
     * 
     * @throws IOException if an error when writing to the 
     *      {@link POIFSFileSystem} occurs
     */
    protected void writeProperties(POIFSFileSystem outFS) throws IOException {
        writeProperties(outFS, null);
    }
    /**
     * Writes out the standard Document Information Properties (HPSF)
     * @param outFS the {@link POIFSFileSystem} to write the properties into
     * @param writtenEntries a list of POIFS entries to add the property names too
     * 
     * @throws IOException if an error when writing to the 
     *      {@link POIFSFileSystem} occurs
     */
    protected void writeProperties(POIFSFileSystem outFS, List<String> writtenEntries) throws IOException {
        final EncryptionInfo ei = getEncryptionInfo();
        final boolean encryptProps = (ei != null && ei.isDocPropsEncrypted());
        try (POIFSFileSystem tmpFS = new POIFSFileSystem()) {
            final POIFSFileSystem fs = (encryptProps) ? tmpFS : outFS;

            writePropertySet(SummaryInformation.DEFAULT_STREAM_NAME, getSummaryInformation(), fs, writtenEntries);
            writePropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME, getDocumentSummaryInformation(), fs, writtenEntries);

            if (!encryptProps) {
                return;
            }

            // create empty document summary
            writePropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME, newDocumentSummaryInformation(), outFS);

            // remove summary, if previously available
            if (outFS.getRoot().hasEntry(SummaryInformation.DEFAULT_STREAM_NAME)) {
                outFS.getRoot().getEntry(SummaryInformation.DEFAULT_STREAM_NAME).delete();
            }
            Encryptor encGen = ei.getEncryptor();
            if (!(encGen instanceof CryptoAPIEncryptor)) {
                throw new EncryptedDocumentException(
                    "Using " + ei.getEncryptionMode() + " encryption. Only CryptoAPI encryption supports encrypted property sets!");
            }
            CryptoAPIEncryptor enc = (CryptoAPIEncryptor) encGen;
            try {
                enc.setSummaryEntries(outFS.getRoot(), getEncryptedPropertyStreamName(), fs);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

    private void writePropertySet(String name, PropertySet ps, POIFSFileSystem outFS, List<String> writtenEntries)
    throws IOException {
        if (ps == null) {
            return;
        }
        writePropertySet(name, ps, outFS);
        if (writtenEntries != null) {
            writtenEntries.add(name);
        }
    }
	
    /**
     * Writes out a given PropertySet
     *
     * @param name the (POIFS Level) name of the property to write
     * @param set the PropertySet to write out 
     * @param outFS the {@link POIFSFileSystem} to write the property into
     * 
     * @throws IOException if an error when writing to the 
     *      {@link POIFSFileSystem} occurs
     */
    private void writePropertySet(String name, PropertySet set, POIFSFileSystem outFS) throws IOException {
        try {
            PropertySet mSet = new PropertySet(set);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            mSet.write(bOut);
            byte[] data = bOut.toByteArray();
            ByteArrayInputStream bIn = new ByteArrayInputStream(data);

            // Create or Update the Property Set stream in the POIFS
            outFS.createOrUpdateDocument(bIn, name);

            logger.log(POILogger.INFO, "Wrote property set " + name + " of size " + data.length);
        } catch(WritingNotSupportedException ignored) {
            logger.log( POILogger.ERROR, "Couldn't write property set with name " + name + " as not supported by HPSF yet");
        }
    }

    /**
     * Called during a {@link #write()} to ensure that the Document (and
     *  associated {@link POIFSFileSystem}) was opened in a way compatible
     *  with an in-place write.
     * 
     * @throws IllegalStateException if the document was opened suitably
     */
    protected void validateInPlaceWritePossible() throws IllegalStateException {
        if (directory == null) {
            throw new IllegalStateException("Newly created Document, cannot save in-place");
        }
        if (directory.getParent() != null) {
            throw new IllegalStateException("This is not the root Document, cannot save embedded resource in-place");
        }
        if (directory.getFileSystem() == null ||
            !directory.getFileSystem().isInPlaceWriteable()) {
            throw new IllegalStateException("Opened read-only or via an InputStream, a Writeable File is required");
        }
    }
    
    /**
     * Writes the document out to the currently open {@link File}, via the
     *  writeable {@link POIFSFileSystem} it was opened from.
     *  
     * <p>This will fail (with an {@link IllegalStateException} if the
     *  document was opened read-only, opened from an {@link InputStream}
     *   instead of a File, or if this is not the root document. For those cases, 
     *   you must use {@link #write(OutputStream)} or {@link #write(File)} to 
     *   write to a brand new document.
     *   
     * @since POI 3.15 beta 3
     * 
     * @throws IOException thrown on errors writing to the file
     * @throws IllegalStateException if this isn't from a writable File
     */
    public abstract void write() throws IOException;

    /**
     * Writes the document out to the specified new {@link File}. If the file 
     * exists, it will be replaced, otherwise a new one will be created
     *
     * @since POI 3.15 beta 3
     * 
     * @param newFile The new File to write to.
     * 
     * @throws IOException thrown on errors writing to the file
     */
    public abstract void write(File newFile) throws IOException;

    /**
     * Writes the document out to the specified output stream. The
     * stream is not closed as part of this operation.
     * 
     * Note - if the Document was opened from a {@link File} rather
     *  than an {@link InputStream}, you <b>must</b> write out using
     *  {@link #write()} or to a different File. Overwriting the currently
     *  open file via an OutputStream isn't possible.
     *  
     * If {@code stream} is a {@link java.io.FileOutputStream} on a networked drive
     * or has a high cost/latency associated with each written byte,
     * consider wrapping the OutputStream in a {@link java.io.BufferedOutputStream}
     * to improve write performance, or use {@link #write()} / {@link #write(File)}
     * if possible.
     * 
     * @param out The stream to write to.
     * 
     * @throws IOException thrown on errors writing to the stream
     */
    public abstract void write(OutputStream out) throws IOException;

    /**
     * Closes the underlying {@link POIFSFileSystem} from which
     *  the document was read, if any. Has no effect on documents
     *  opened from an InputStream, or newly created ones.<p>
     *
     * Once {@code close()} has been called, no further operations
     *  should be called on the document.
     */
    @Override
    public void close() throws IOException {
        if (directory != null) {
            if (directory.getFileSystem() != null) {
                directory.getFileSystem().close();
                clearDirectory();
            }
        }
    }

    @Internal
    public DirectoryNode getDirectory() {
        return directory;
    }
    
    /**
     * Clear/unlink the attached directory entry
     */
    @Internal
    protected void clearDirectory() {
        directory = null;
    }
    
    /**
     * check if we were created by POIFS otherwise create a new dummy POIFS
     * for storing the package data
     * 
     * @return {@code true} if dummy directory was created, {@code false} otherwise
     */
    @SuppressWarnings("resource")
    @Internal
    protected boolean initDirectory() {
        if (directory == null) {
            directory = new POIFSFileSystem().getRoot(); // NOSONAR
            return true;
        }
        return false;
    }
    
    /**
     * Replaces the attached directory, e.g. if this document is written
     * to a new POIFSFileSystem
     *
     * @param newDirectory the new directory
     */
    @Internal
    protected void replaceDirectory(DirectoryNode newDirectory) {
        directory = newDirectory;
    }

    /**
     * @return the stream name of the property set collection, if the document is encrypted
     */
    protected String getEncryptedPropertyStreamName() {
        return "encryption";
    }

    /**
     * @return the encryption info if the document is encrypted, otherwise {@code null}
     *
     * @throws IOException If retrieving the encryption information fails
     */
    public EncryptionInfo getEncryptionInfo() throws IOException {
        return null;
    }
}
