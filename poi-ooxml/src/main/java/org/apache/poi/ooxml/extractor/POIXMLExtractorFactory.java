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

import static org.apache.poi.extractor.ExtractorFactory.OOXML_PACKAGE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.ExtractorProvider;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xdgf.extractor.XDGFVisioExtractor;
import org.apache.poi.xslf.extractor.XSLFExtractor;
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
public final class POIXMLExtractorFactory implements ExtractorProvider {
    private static final String CORE_DOCUMENT_REL = PackageRelationshipTypes.CORE_DOCUMENT;
    private static final String VISIO_DOCUMENT_REL = PackageRelationshipTypes.VISIO_CORE_DOCUMENT;
    private static final String STRICT_DOCUMENT_REL = PackageRelationshipTypes.STRICT_CORE_DOCUMENT;

    private static final XSLFRelation[] SUPPORTED_XSLF_TYPES = new XSLFRelation[]{
        XSLFRelation.MAIN, XSLFRelation.MACRO, XSLFRelation.MACRO_TEMPLATE,
        XSLFRelation.PRESENTATIONML, XSLFRelation.PRESENTATIONML_TEMPLATE,
        XSLFRelation.PRESENTATION_MACRO
    };

    @Override
    public boolean accepts(FileMagic fm) {
        return fm == FileMagic.OOXML;
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is false.
     */
    public static boolean getThreadPrefersEventExtractors() {
        return ExtractorFactory.getThreadPrefersEventExtractors();
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is to use the thread level setting, which defaults to false.
     */
    public static Boolean getAllThreadsPreferEventExtractors() {
        return ExtractorFactory.getAllThreadsPreferEventExtractors();
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * Will only be used if the All Threads setting is null.
     */
    public static void setThreadPrefersEventExtractors(boolean preferEventExtractors) {
         ExtractorFactory.setThreadPrefersEventExtractors(preferEventExtractors);
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * If set, will take preference over the Thread level setting.
     */
    public static void setAllThreadsPreferEventExtractors(Boolean preferEventExtractors) {
         ExtractorFactory.setAllThreadsPreferEventExtractors(preferEventExtractors);
    }

    /**
     * Should this thread use event based extractors is available?
     * Checks the all-threads one first, then thread specific.
     */
    public static boolean getPreferEventExtractor() {
         return ExtractorFactory.getPreferEventExtractor();
    }

    @Override
    public POITextExtractor create(File f, String password) throws IOException {
        if (FileMagic.valueOf(f) != FileMagic.OOXML) {
            return ExtractorFactory.createExtractor(f, password);
        }

        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(f.toString(), PackageAccess.READ);
            POIXMLTextExtractor ex = create(pkg);
            if (ex == null) {
                pkg.revert();
            }
            return ex;
        } catch (InvalidFormatException ife) {
            throw new IOException(ife);
        } catch (IOException e) {
            if (pkg != null) {
                pkg.revert();
            }
            throw e;
        }
    }

    public POITextExtractor create(InputStream inp, String password) throws IOException {
        InputStream is = FileMagic.prepareToCheckMagic(inp);

        if (FileMagic.valueOf(is) != FileMagic.OOXML) {
            return ExtractorFactory.createExtractor(is, password);
        }

        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(is);
            POIXMLTextExtractor ex = create(pkg);
            if (ex == null) {
                pkg.revert();
            }
            return ex;
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        } catch (RuntimeException | IOException e) {
            if (pkg != null) {
                pkg.revert();
            }
            throw e;
        }
    }

    /**
     * Tries to determine the actual type of file and produces a matching text-extractor for it.
     *
     * @param pkg An {@link OPCPackage}.
     * @return A {@link POIXMLTextExtractor} for the given file.
     * @throws IOException If an error occurs while reading the file
     * @throws IllegalArgumentException If no matching file type could be found.
     */
    public POIXMLTextExtractor create(OPCPackage pkg) throws IOException {
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
                if (core.size() == 1) {
                    return new XDGFVisioExtractor(pkg);
                }
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
                if (rel.getContentType().equals(contentType)) {
                    if (getPreferEventExtractor()) {
                        return new XSSFEventBasedExcelExtractor(pkg);
                    }
                    return new XSSFExcelExtractor(pkg);
                }
            }

            // Is it XWPF?
            for (XWPFRelation rel : XWPFWordExtractor.SUPPORTED_TYPES) {
                if (rel.getContentType().equals(contentType)) {
                    return new XWPFWordExtractor(pkg);
                }
            }

            // Is it XSLF?
            for (XSLFRelation rel : SUPPORTED_XSLF_TYPES) {
                if (rel.getContentType().equals(contentType)) {
                    return new XSLFExtractor(new XMLSlideShow(pkg));
                }
            }

            // special handling for SlideShow-Theme-files,
            if (XSLFRelation.THEME_MANAGER.getContentType().equals(contentType)) {
                return new XSLFExtractor(new XMLSlideShow(pkg));
            }

            // How about xlsb?
            for (XSSFRelation rel : XSSFBEventBasedExcelExtractor.SUPPORTED_TYPES) {
                if (rel.getContentType().equals(contentType)) {
                    return new XSSFBEventBasedExcelExtractor(pkg);
                }
            }

            return null;
        } catch (Error | RuntimeException | XmlException | OpenXML4JException e) { // NOSONAR
            throw new IOException(e);
        }
        // we used to close (revert()) the package here, but this is the callers responsibility
        // and we can't reuse the package
    }

    public POITextExtractor create(POIFSFileSystem fs) throws IOException {
        return create(fs.getRoot(), Biff8EncryptionKey.getCurrentUserPassword());
    }

    @Override
    public POITextExtractor create(DirectoryNode poifsDir, String password) throws IOException {
        // First, check for plain OOXML package
        if (poifsDir.hasEntry(OOXML_PACKAGE)) {
            try (InputStream is = poifsDir.createDocumentInputStream(OOXML_PACKAGE)) {
                return create(is, password);
            }
        }

        if (poifsDir.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
            EncryptionInfo ei = new EncryptionInfo(poifsDir);
            Decryptor dec = ei.getDecryptor();
            try {
                if (!dec.verifyPassword(password)) {
                    throw new IOException("Invalid password specified");
                }
                try (InputStream is = dec.getDataStream(poifsDir)) {
                    return create(is, password);
                } finally {
                    // we should close the underlying file-system as all information
                    // is read now and we should make sure that resources are freed
                    POIFSFileSystem fs = poifsDir.getFileSystem();
                    if (fs != null) {
                        fs.close();
                    }
                }
            } catch (IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        throw new IOException("The OLE2 file neither contained a plain OOXML package node (\"Package\") nor an encrypted one (\"EncryptedPackage\").");
    }
}
