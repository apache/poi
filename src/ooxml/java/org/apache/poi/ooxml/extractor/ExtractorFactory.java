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
package org.apache.poi.ooxml.extractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.extractor.OLE2ExtractorFactory;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.extractor.OutlookTextExtactor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Removal;
import org.apache.poi.xdgf.extractor.XDGFVisioExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlException;

/**
 * Figures out the correct POITextExtractor for your supplied
 *  document, and returns it.
 *  
 * <p>Note 1 - will fail for many file formats if the POI Scratchpad jar is
 *  not present on the runtime classpath</p>
 * <p>Note 2 - rather than using this, for most cases you would be better
 *  off switching to <a href="http://tika.apache.org">Apache Tika</a> instead!</p>
 */
@SuppressWarnings("WeakerAccess")
public final class ExtractorFactory {
    private static final POILogger logger = POILogFactory.getLogger(ExtractorFactory.class);
    
    public static final String CORE_DOCUMENT_REL = PackageRelationshipTypes.CORE_DOCUMENT;
    private static final String VISIO_DOCUMENT_REL = PackageRelationshipTypes.VISIO_CORE_DOCUMENT;
    private static final String STRICT_DOCUMENT_REL = PackageRelationshipTypes.STRICT_CORE_DOCUMENT;

    private ExtractorFactory() {
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is false.
     */
    public static boolean getThreadPrefersEventExtractors() {
        return OLE2ExtractorFactory.getThreadPrefersEventExtractors();
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is to use the thread level setting, which defaults to false.
     */
    public static Boolean getAllThreadsPreferEventExtractors() {
        return OLE2ExtractorFactory.getAllThreadsPreferEventExtractors();
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * Will only be used if the All Threads setting is null.
     */
    public static void setThreadPrefersEventExtractors(boolean preferEventExtractors) {
         OLE2ExtractorFactory.setThreadPrefersEventExtractors(preferEventExtractors);
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * If set, will take preference over the Thread level setting.
     */
    public static void setAllThreadsPreferEventExtractors(Boolean preferEventExtractors) {
         OLE2ExtractorFactory.setAllThreadsPreferEventExtractors(preferEventExtractors);
    }

    /**
     * Should this thread use event based extractors is available?
     * Checks the all-threads one first, then thread specific.
     */
    public static boolean getPreferEventExtractor() {
         return OLE2ExtractorFactory.getPreferEventExtractor();
    }

    @SuppressWarnings("unchecked")
    public static <T extends POITextExtractor> T createExtractor(File f) throws IOException, OpenXML4JException, XmlException {
        POIFSFileSystem fs = null;
        try {
            fs = new POIFSFileSystem(f);
            if (fs.getRoot().hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
                return (T)createEncryptedOOXMLExtractor(fs);
            }
            POITextExtractor extractor = createExtractor(fs);
            extractor.setFilesystem(fs);
            return (T)extractor;
        } catch (OfficeXmlFileException e) {
            // ensure file-handle release
            IOUtils.closeQuietly(fs);
            OPCPackage pkg = OPCPackage.open(f.toString(), PackageAccess.READ);
            T t = (T)createExtractor(pkg);
            t.setFilesystem(pkg);
            return t;
        } catch (NotOLE2FileException ne) {
            // ensure file-handle release
            IOUtils.closeQuietly(fs);
            throw new IllegalArgumentException("Your File was neither an OLE2 file, nor an OOXML file");
        } catch (OpenXML4JException | Error | RuntimeException | IOException | XmlException e) { // NOSONAR
            // ensure file-handle release
            IOUtils.closeQuietly(fs);
            throw e;
        }
    }

    public static POITextExtractor createExtractor(InputStream inp) throws IOException, OpenXML4JException, XmlException {
        InputStream is = FileMagic.prepareToCheckMagic(inp);

        FileMagic fm = FileMagic.valueOf(is);
        
        switch (fm) {
        case OLE2:
            POIFSFileSystem fs = new POIFSFileSystem(is);
            boolean isEncrypted = fs.getRoot().hasEntry(Decryptor.DEFAULT_POIFS_ENTRY); 
            return isEncrypted ? createEncryptedOOXMLExtractor(fs) : createExtractor(fs);
        case OOXML:
            return createExtractor(OPCPackage.open(is));
        default:
            throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
        }
    }

    /**
     * Tries to determine the actual type of file and produces a matching text-extractor for it.
     *
     * @param pkg An {@link OPCPackage}.
     * @return A {@link POIXMLTextExtractor} for the given file.
     * @throws IOException If an error occurs while reading the file 
     * @throws OpenXML4JException If an error parsing the OpenXML file format is found. 
     * @throws XmlException If an XML parsing error occurs.
     * @throws IllegalArgumentException If no matching file type could be found.
     */
    public static POITextExtractor createExtractor(OPCPackage pkg) throws IOException, OpenXML4JException, XmlException {
        try {
            // Check for the normal Office core document
            PackageRelationshipCollection core;
            core = pkg.getRelationshipsByType(CORE_DOCUMENT_REL);
              
            // If nothing was found, try some of the other OOXML-based core types
            if (core.size() == 0) {
                // Could it be an OOXML-Strict one?
                core = pkg.getRelationshipsByType(STRICT_DOCUMENT_REL);
            }
            if (core.size() == 0) {
                // Could it be a visio one?
                core = pkg.getRelationshipsByType(VISIO_DOCUMENT_REL);
                if (core.size() == 1)
                    return new XDGFVisioExtractor(pkg);
            }
              
            // Should just be a single core document, complain if not
            if (core.size() != 1) {
                throw new IllegalArgumentException("Invalid OOXML Package received - expected 1 core document, found " + core.size());
            }
     
            // Grab the core document part, and try to identify from that
            final PackagePart corePart = pkg.getPart(core.getRelationship(0));
            final String contentType = corePart.getContentType();
     
            // Is it XSSF?
            for (XSSFRelation rel : XSSFExcelExtractor.SUPPORTED_TYPES) {
                if ( rel.getContentType().equals( contentType ) ) {
                    if (getPreferEventExtractor()) {
                        return new XSSFEventBasedExcelExtractor(pkg);
                    }
                    return new XSSFExcelExtractor(pkg);
                }
            }
     
            // Is it XWPF?
            for (XWPFRelation rel : XWPFWordExtractor.SUPPORTED_TYPES) {
                if ( rel.getContentType().equals( contentType ) ) {
                    return new XWPFWordExtractor(pkg);
                }
            }
     
            // Is it XSLF?
            for (XSLFRelation rel : XSLFPowerPointExtractor.SUPPORTED_TYPES) {
                if ( rel.getContentType().equals( contentType ) ) {
                    return new SlideShowExtractor<>(new XMLSlideShow(pkg));
                }
            }
     
            // special handling for SlideShow-Theme-files, 
            if (XSLFRelation.THEME_MANAGER.getContentType().equals(contentType)) {
                return new SlideShowExtractor<>(new XMLSlideShow(pkg));
            }

            // How about xlsb?
            for (XSSFRelation rel : XSSFBEventBasedExcelExtractor.SUPPORTED_TYPES) {
                if (rel.getContentType().equals(contentType)) {
                    return new XSSFBEventBasedExcelExtractor(pkg);
                }
            }

            throw new IllegalArgumentException("No supported documents found in the OOXML package (found "+contentType+")");

        } catch (IOException | Error | RuntimeException | XmlException | OpenXML4JException e) { // NOSONAR
            // ensure that we close the package again if there is an error opening it, however
            // we need to revert the package to not re-write the file via close(), which is very likely not wanted for a TextExtractor!
            pkg.revert();
            throw e;
        }
    }

    public static <T extends POITextExtractor> T createExtractor(POIFSFileSystem fs) throws IOException, OpenXML4JException, XmlException {
        return createExtractor(fs.getRoot());
    }

    @SuppressWarnings("unchecked")
    public static <T extends POITextExtractor> T createExtractor(DirectoryNode poifsDir) throws IOException, OpenXML4JException, XmlException
    {
        // First, check for OOXML
        for (String entryName : poifsDir.getEntryNames()) {
            if (entryName.equals("Package")) {
                OPCPackage pkg = OPCPackage.open(poifsDir.createDocumentInputStream("Package"));
                return (T)createExtractor(pkg);
            }
        }

        // If not, ask the OLE2 code to check, with Scratchpad if possible
        return (T)OLE2ExtractorFactory.createExtractor(poifsDir);
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     *
     *  @deprecated Use the method with correct "embedded"
     */
    @Deprecated
    @Removal(version="4.2")
    public static POITextExtractor[] getEmbededDocsTextExtractors(POIOLE2TextExtractor ext) throws IOException, OpenXML4JException, XmlException {
        return getEmbeddedDocsTextExtractors(ext);
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     */
    public static POITextExtractor[] getEmbeddedDocsTextExtractors(POIOLE2TextExtractor ext) throws IOException, OpenXML4JException, XmlException {
        // All the embedded directories we spotted
        ArrayList<Entry> dirs = new ArrayList<>();
        // For anything else not directly held in as a POIFS directory
        ArrayList<InputStream> nonPOIFS = new ArrayList<>();

        // Find all the embedded directories
        DirectoryEntry root = ext.getRoot();
        if (root == null) {
            throw new IllegalStateException("The extractor didn't know which POIFS it came from!");
        }

        if (ext instanceof ExcelExtractor) {
            // These are in MBD... under the root
            Iterator<Entry> it = root.getEntries();
            while (it.hasNext()) {
                Entry entry = it.next();
                if (entry.getName().startsWith("MBD")) {
                    dirs.add(entry);
                }
            }
        } else if (ext instanceof WordExtractor) {
            // These are in ObjectPool -> _... under the root
            try {
                DirectoryEntry op = (DirectoryEntry) root.getEntry("ObjectPool");
                Iterator<Entry> it = op.getEntries();
                while (it.hasNext()) {
                    Entry entry = it.next();
                    if (entry.getName().startsWith("_")) {
                        dirs.add(entry);
                    }
                }
            } catch (FileNotFoundException e) {
                logger.log(POILogger.INFO, "Ignoring FileNotFoundException while extracting Word document", e.getLocalizedMessage());
                // ignored here
            }
        //} else if(ext instanceof PowerPointExtractor) {
            // Tricky, not stored directly in poifs
            // TODO
        } else if (ext instanceof OutlookTextExtactor) {
            // Stored in the Attachment blocks
            MAPIMessage msg = ((OutlookTextExtactor)ext).getMAPIMessage();
            for (AttachmentChunks attachment : msg.getAttachmentFiles()) {
                if (attachment.getAttachData() != null) {
                    byte[] data = attachment.getAttachData().getValue();
                    nonPOIFS.add( new ByteArrayInputStream(data) );
                } else if (attachment.getAttachmentDirectory() != null) {
                    dirs.add(attachment.getAttachmentDirectory().getDirectory());
                }
            }
        }

        // Create the extractors
        if (dirs.size() == 0 && nonPOIFS.size() == 0){
            return new POITextExtractor[0];
        }

        ArrayList<POITextExtractor> textExtractors = new ArrayList<>();
        for (Entry dir : dirs) {
            textExtractors.add(createExtractor((DirectoryNode) dir));
        }
        for (InputStream nonPOIF : nonPOIFS) {
            try {
                 textExtractors.add(createExtractor(nonPOIF));
            } catch (IllegalArgumentException e) {
                // Ignore, just means it didn't contain
                //  a format we support as yet
                logger.log(POILogger.INFO, "Format not supported yet", e.getLocalizedMessage());
            } catch (XmlException | OpenXML4JException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return textExtractors.toArray(new POITextExtractor[0]);
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     *
     *  @deprecated Use the method with correct "embedded"
     */
    @Deprecated
    @Removal(version="4.2")
    @NotImplemented
    @SuppressWarnings({"UnusedParameters", "UnusedReturnValue"})
    public static POITextExtractor[] getEmbededDocsTextExtractors(POIXMLTextExtractor ext) {
        return getEmbeddedDocsTextExtractors(ext);
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     */
    @NotImplemented
    @SuppressWarnings({"UnusedParameters", "UnusedReturnValue"})
    public static POITextExtractor[] getEmbeddedDocsTextExtractors(POIXMLTextExtractor ext) {
        throw new IllegalStateException("Not yet supported");
    }
    
    private static POITextExtractor createEncryptedOOXMLExtractor(POIFSFileSystem fs)
    throws IOException {
        String pass = Biff8EncryptionKey.getCurrentUserPassword();
        if (pass == null) {
            pass = Decryptor.DEFAULT_PASSWORD;
        }
        
        EncryptionInfo ei = new EncryptionInfo(fs);
        Decryptor dec = ei.getDecryptor();
        InputStream is = null;
        try {
            if (!dec.verifyPassword(pass)) {
                throw new EncryptedDocumentException("Invalid password specified - use Biff8EncryptionKey.setCurrentUserPassword() before calling extractor");
            }
            is = dec.getDataStream(fs);
            return createExtractor(OPCPackage.open(is));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        } finally {
            IOUtils.closeQuietly(is);

            // also close the NPOIFSFileSystem here as we read all the data
            // while decrypting
            fs.close();
        }
    }
}
