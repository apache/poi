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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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
    protected DirectoryNode directory;

    /** For our own logging use */
    private static final POILogger logger = POILogFactory.getLogger(POIDocument.class);

    /* Have the property streams been read yet? (Only done on-demand) */
    private boolean initialized = false;
    
    /**
     * Constructs a POIDocument with the given directory node.
     *
     * @param dir The {@link DirectoryNode} where information is read from.
     */
    protected POIDocument(DirectoryNode dir) {
    	this.directory = dir;
    }

    /**
     * Constructs from an old-style OPOIFS
     * 
     * @param fs the filesystem the document is read from
     */
    protected POIDocument(OPOIFSFileSystem fs) {
       this(fs.getRoot());
    }
    /**
     * Constructs from an old-style OPOIFS
     * 
     * @param fs the filesystem the document is read from
     */
    protected POIDocument(NPOIFSFileSystem fs) {
       this(fs.getRoot());
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
        if(!initialized) readProperties();
        return dsInf;
    }

    /** 
     * Fetch the Summary Information of the document
     * 
     * @return The Summary information for the document or null
     *      if it could not be read for this document.
     */
    public SummaryInformation getSummaryInformation() {
        if(!initialized) readProperties();
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
        if (!initialized) readProperties();
        if (sInf == null) {
            sInf = PropertySetFactory.newSummaryInformation();
        }
        if (dsInf == null) {
            dsInf = PropertySetFactory.newDocumentSummaryInformation();
        }
    }

    /**
     * Find, and create objects for, the standard
     *  Document Information Properties (HPSF).
     * If a given property set is missing or corrupt,
     *  it will remain null;
     */
    protected void readProperties() {
        PropertySet ps;

        // DocumentSummaryInformation
        ps = getPropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        if (ps instanceof DocumentSummaryInformation) {
            dsInf = (DocumentSummaryInformation)ps;
        } else if (ps != null) {
            logger.log(POILogger.WARN, "DocumentSummaryInformation property set came back with wrong class - ", ps.getClass());
        } else {
            logger.log(POILogger.WARN, "DocumentSummaryInformation property set came back as null");
        }

        // SummaryInformation
        ps = getPropertySet(SummaryInformation.DEFAULT_STREAM_NAME);
        if (ps instanceof SummaryInformation) {
            sInf = (SummaryInformation)ps;
        } else if (ps != null) {
            logger.log(POILogger.WARN, "SummaryInformation property set came back with wrong class - ", ps.getClass());
        } else {
            logger.log(POILogger.WARN, "SummaryInformation property set came back as null");
        }

        // Mark the fact that we've now loaded up the properties
        initialized = true;
    }

    /** 
     * For a given named property entry, either return it or null if
     *  if it wasn't found
     *  
     *  @param setName The property to read
     *  @return The value of the given property or null if it wasn't found.
     */
    protected PropertySet getPropertySet(String setName) {
        return getPropertySet(setName, null);
    }
    
    /** 
     * For a given named property entry, either return it or null if
     *  if it wasn't found
     *  
     *  @param setName The property to read
     *  @param encryptionInfo the encryption descriptor in case of cryptoAPI encryption
     *  @return The value of the given property or null if it wasn't found.
     */
    protected PropertySet getPropertySet(String setName, EncryptionInfo encryptionInfo) {
        DirectoryNode dirNode = directory;
        
        NPOIFSFileSystem encPoifs = null;
        String step = "getting";
        try {
            if (encryptionInfo != null) {
                step = "getting encrypted";
                InputStream is = encryptionInfo.getDecryptor().getDataStream(directory);
                try {
                    encPoifs = new NPOIFSFileSystem(is);
                    dirNode = encPoifs.getRoot();
                } finally {
                    is.close();
                }
            }
            
            //directory can be null when creating new documents
            if (dirNode == null || !dirNode.hasEntry(setName)) {
                return null;
            }
    
            // Find the entry, and get an input stream for it
            step = "getting";
            DocumentInputStream dis = dirNode.createDocumentInputStream( dirNode.getEntry(setName) );
            try {
                // Create the Property Set
                step = "creating";
                return PropertySetFactory.create(dis);
            } finally {
                dis.close();
            }
        } catch (Exception e) {
            logger.log(POILogger.WARN, "Error "+step+" property set with name " + setName, e);
            return null;
        } finally {
            if (encPoifs != null) {
                try {
                    encPoifs.close();
                } catch(IOException e) {
                    logger.log(POILogger.WARN, "Error closing encrypted property poifs", e);
                }
            }
        }
    }
    
    /**
     * Writes out the updated standard Document Information Properties (HPSF)
     *  into the currently open NPOIFSFileSystem
     * 
     * @throws IOException if an error when writing to the open
     *      {@link NPOIFSFileSystem} occurs
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
     *      {@link NPOIFSFileSystem} occurs
     */
    protected void writeProperties(NPOIFSFileSystem outFS) throws IOException {
        writeProperties(outFS, null);
    }
    /**
     * Writes out the standard Document Information Properties (HPSF)
     * @param outFS the NPOIFSFileSystem to write the properties into
     * @param writtenEntries a list of POIFS entries to add the property names too
     * 
     * @throws IOException if an error when writing to the 
     *      {@link NPOIFSFileSystem} occurs
     */
    protected void writeProperties(NPOIFSFileSystem outFS, List<String> writtenEntries) throws IOException {
        SummaryInformation si = getSummaryInformation();
        if (si != null) {
            writePropertySet(SummaryInformation.DEFAULT_STREAM_NAME, si, outFS);
            if(writtenEntries != null) {
                writtenEntries.add(SummaryInformation.DEFAULT_STREAM_NAME);
            }
        }
        DocumentSummaryInformation dsi = getDocumentSummaryInformation();
        if (dsi != null) {
            writePropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME, dsi, outFS);
            if(writtenEntries != null) {
                writtenEntries.add(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            }
        }
    }
	
    /**
     * Writes out a given ProperySet
     * @param name the (POIFS Level) name of the property to write
     * @param set the PropertySet to write out 
     * @param outFS the NPOIFSFileSystem to write the property into
     * 
     * @throws IOException if an error when writing to the 
     *      {@link NPOIFSFileSystem} occurs
     */
    protected void writePropertySet(String name, PropertySet set, NPOIFSFileSystem outFS) throws IOException {
        try {
            MutablePropertySet mSet = new MutablePropertySet(set);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            mSet.write(bOut);
            byte[] data = bOut.toByteArray();
            ByteArrayInputStream bIn = new ByteArrayInputStream(data);

            // Create or Update the Property Set stream in the POIFS
            outFS.createOrUpdateDocument(bIn, name);

            logger.log(POILogger.INFO, "Wrote property set " + name + " of size " + data.length);
        } catch(org.apache.poi.hpsf.WritingNotSupportedException wnse) {
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
     * Closes the underlying {@link NPOIFSFileSystem} from which
     *  the document was read, if any. Has no effect on documents
     *  opened from an InputStream, or newly created ones.
     * <p>Once {@link #close()} has been called, no further operations
     *  should be called on the document.
     */
    public void close() throws IOException {
        if (directory != null) {
            if (directory.getNFileSystem() != null) {
                directory.getNFileSystem().close();
                directory = null;
            }
        }
    }

    @Internal
    public DirectoryNode getDirectory() {
        return directory;
    }
}
