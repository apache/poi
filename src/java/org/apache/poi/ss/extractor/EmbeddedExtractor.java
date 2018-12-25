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

package org.apache.poi.ss.extractor;

import static org.apache.poi.util.StringUtil.endsWithIgnoreCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.Ole10NativeException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ObjectData;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.ShapeContainer;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This extractor class tries to identify various embedded documents within Excel files
 * and provide them via a common interface, i.e. the EmbeddedData instances
 */
@Beta
public class EmbeddedExtractor implements Iterable<EmbeddedExtractor> {
    private static final POILogger LOG = POILogFactory.getLogger(EmbeddedExtractor.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    // contentType
    private static final String CONTENT_TYPE_BYTES = "binary/octet-stream";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_DOC = "application/msword";
    private static final String CONTENT_TYPE_XLS = "application/vnd.ms-excel";

    /**
     * @return the list of known extractors, if you provide custom extractors, override this method
     */
    @Override
    public Iterator<EmbeddedExtractor> iterator() {
        EmbeddedExtractor[] ee = {
            new Ole10Extractor(), new PdfExtractor(), new BiffExtractor(), new OOXMLExtractor(), new FsExtractor()
        };
        return Arrays.asList(ee).iterator();
    }

    public EmbeddedData extractOne(DirectoryNode src) throws IOException {
        for (EmbeddedExtractor ee : this) {
            if (ee.canExtract(src)) {
                return ee.extract(src);
            }
        }
        return null;
    }

    public EmbeddedData extractOne(Picture src) throws IOException {
        for (EmbeddedExtractor ee : this) {
            if (ee.canExtract(src)) {
                return ee.extract(src);
            }
        }
        return null;
    }

    public List<EmbeddedData> extractAll(Sheet sheet) throws IOException {
        Drawing<?> patriarch = sheet.getDrawingPatriarch();
        if (null == patriarch){
            return Collections.emptyList();
        }
        List<EmbeddedData> embeddings = new ArrayList<>();
        extractAll(patriarch, embeddings);
        return embeddings;
    }
    
    protected void extractAll(ShapeContainer<?> parent, List<EmbeddedData> embeddings) throws IOException {
        for (Shape shape : parent) {
            EmbeddedData data = null;
            if (shape instanceof ObjectData) {
                ObjectData od = (ObjectData)shape;
                try {
                    if (od.hasDirectoryEntry()) {
                        data = extractOne((DirectoryNode)od.getDirectory());
                    } else {
                        data = new EmbeddedData(od.getFileName(), od.getObjectData(), od.getContentType());
                    }
                } catch (Exception e) {
                    LOG.log(POILogger.WARN, "Entry not found / readable - ignoring OLE embedding", e);
                }
            } else if (shape instanceof Picture) {
                data = extractOne((Picture)shape);
            } else if (shape instanceof ShapeContainer) {
                extractAll((ShapeContainer<?>)shape, embeddings);
            }
            
            if (data == null) {
                continue;
            }

            data.setShape(shape);
            String filename = data.getFilename();
            String extension = (filename == null || filename.lastIndexOf('.') == -1) ? ".bin" : filename.substring(filename.lastIndexOf('.'));
            
            // try to find an alternative name
            if (filename == null || filename.isEmpty() || filename.startsWith("MBD") || filename.startsWith("Root Entry")) {
                filename = shape.getShapeName();
                if (filename != null) {
                    filename += extension;
                }
            }
            // default to dummy name
            if (filename == null || filename.isEmpty()) {
                filename = "picture_" + embeddings.size() + extension;
            }
            filename = filename.trim();
            data.setFilename(filename);
            
            embeddings.add(data);
        }
    }
    

    public boolean canExtract(DirectoryNode source) {
        return false;
    }

    public boolean canExtract(Picture source) {
        return false;
    }

    protected EmbeddedData extract(DirectoryNode dn) throws IOException {
        assert(canExtract(dn));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(20000);
        try (POIFSFileSystem dest = new POIFSFileSystem()) {
            copyNodes(dn, dest.getRoot());
            // start with a reasonable big size
            dest.writeFilesystem(bos);
        }

        return new EmbeddedData(dn.getName(), bos.toByteArray(), CONTENT_TYPE_BYTES);
    }

    protected EmbeddedData extract(Picture source) throws IOException {
        return null;
    }
    
    public static class Ole10Extractor extends EmbeddedExtractor {
        @Override
        public boolean canExtract(DirectoryNode dn) {
            ClassID clsId = dn.getStorageClsid();
            return ClassIDPredefined.lookup(clsId) == ClassIDPredefined.OLE_V1_PACKAGE;
        }

        @Override
        public EmbeddedData extract(DirectoryNode dn) throws IOException {
            try {
                // TODO: inspect the CompObj record for more details, i.e. the content type
                Ole10Native ole10 = Ole10Native.createFromEmbeddedOleObject(dn);
                return new EmbeddedData(ole10.getFileName(), ole10.getDataBuffer(), CONTENT_TYPE_BYTES);
            } catch (Ole10NativeException e) {
                throw new IOException(e);
            }
        }
    }

    static class PdfExtractor extends EmbeddedExtractor {
        static ClassID PdfClassID = new ClassID("{B801CA65-A1FC-11D0-85AD-444553540000}");
        @Override
        public boolean canExtract(DirectoryNode dn) {
            ClassID clsId = dn.getStorageClsid();
            return (PdfClassID.equals(clsId) || dn.hasEntry("CONTENTS"));
        }

        @Override
        public EmbeddedData extract(DirectoryNode dn) throws IOException {
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream is = dn.createDocumentInputStream("CONTENTS")) {
                IOUtils.copy(is, bos);
                return new EmbeddedData(dn.getName() + ".pdf", bos.toByteArray(), CONTENT_TYPE_PDF);
            }
        }
        
        @Override
        public boolean canExtract(Picture source) {
            PictureData pd = source.getPictureData();
            return (pd != null && pd.getPictureType() == Workbook.PICTURE_TYPE_EMF);
        }

        /**
         * Mac Office encodes embedded objects inside the picture, e.g. PDF is part of an EMF.
         * If an embedded stream is inside an EMF picture, this method extracts the payload.
         *
         * @return the embedded data in an EMF picture or null if none is found
         */
        @Override
        protected EmbeddedData extract(Picture source) throws IOException {
            // check for emf+ embedded pdf (poor mans style :( )
            // Mac Excel 2011 embeds pdf files with this method.
            PictureData pd = source.getPictureData();
            if (pd == null || pd.getPictureType() != Workbook.PICTURE_TYPE_EMF) {
                return null;
            }

            // TODO: investigate if this is just an EMF-hack or if other formats are also embedded in EMF
            byte[] pictureBytes = pd.getData();
            int idxStart = indexOf(pictureBytes, 0, "%PDF-".getBytes(LocaleUtil.CHARSET_1252));
            if (idxStart == -1) {
                return null;
            }
            
            int idxEnd = indexOf(pictureBytes, idxStart, "%%EOF".getBytes(LocaleUtil.CHARSET_1252));
            if (idxEnd == -1) {
                return null;
            }
            
            int pictureBytesLen = idxEnd-idxStart+6;
            byte[] pdfBytes = IOUtils.safelyAllocate(pictureBytesLen, MAX_RECORD_LENGTH);
            System.arraycopy(pictureBytes, idxStart, pdfBytes, 0, pictureBytesLen);
            String filename = source.getShapeName().trim();
            if (!endsWithIgnoreCase(filename, ".pdf")) {
                filename += ".pdf";
            }
            return new EmbeddedData(filename, pdfBytes, CONTENT_TYPE_PDF);
        }
        

    }

    static class OOXMLExtractor extends EmbeddedExtractor {
        @Override
        public boolean canExtract(DirectoryNode dn) {
            return dn.hasEntry("package");
        }

        @Override
        public EmbeddedData extract(DirectoryNode dn) throws IOException {

            ClassIDPredefined clsId = ClassIDPredefined.lookup(dn.getStorageClsid());

            String contentType = null;
            String ext = null;
            
            if (clsId != null) {
                contentType = clsId.getContentType();
                ext = clsId.getFileExtension();
            }
            
            if (contentType == null || ext == null) {
                contentType = "application/zip";
                ext = ".zip";
            }

            DocumentInputStream dis = dn.createDocumentInputStream("package");
            byte[] data = IOUtils.toByteArray(dis);
            dis.close();
            
            return new EmbeddedData(dn.getName()+ext, data, contentType);
        }
    }

    static class BiffExtractor extends EmbeddedExtractor {
        @Override
        public boolean canExtract(DirectoryNode dn) {
            return canExtractExcel(dn) || canExtractWord(dn);
        }
        
        protected boolean canExtractExcel(DirectoryNode dn) {
            ClassIDPredefined clsId = ClassIDPredefined.lookup(dn.getStorageClsid());
            return (ClassIDPredefined.EXCEL_V7 == clsId
                || ClassIDPredefined.EXCEL_V8 == clsId
                || dn.hasEntry("Workbook") /*...*/);
        }

        protected boolean canExtractWord(DirectoryNode dn) {
            ClassIDPredefined clsId = ClassIDPredefined.lookup(dn.getStorageClsid());
            return (ClassIDPredefined.WORD_V7 == clsId
                || ClassIDPredefined.WORD_V8 == clsId
                || dn.hasEntry("WordDocument"));
        }
        
        @Override
        public EmbeddedData extract(DirectoryNode dn) throws IOException {
            EmbeddedData ed = super.extract(dn);
            if (canExtractExcel(dn)) {
                ed.setFilename(dn.getName() + ".xls");
                ed.setContentType(CONTENT_TYPE_XLS);
            } else if (canExtractWord(dn)) {
                ed.setFilename(dn.getName() + ".doc");
                ed.setContentType(CONTENT_TYPE_DOC);
            }
            
            return ed;
        }
    }

    static class FsExtractor extends EmbeddedExtractor {
        @Override
        public boolean canExtract(DirectoryNode dn) {
            return true;
        }
        @Override
        public EmbeddedData extract(DirectoryNode dn) throws IOException {
            EmbeddedData ed = super.extract(dn);
            ed.setFilename(dn.getName() + ".ole");
            // TODO: read the content type from CombObj stream
            return ed;
        }
    }
    
    protected static void copyNodes(DirectoryNode src, DirectoryNode dest) throws IOException {
        for (Entry e : src) {
            if (e instanceof DirectoryNode) {
                DirectoryNode srcDir = (DirectoryNode)e;
                DirectoryNode destDir = (DirectoryNode)dest.createDirectory(srcDir.getName());
                destDir.setStorageClsid(srcDir.getStorageClsid());
                copyNodes(srcDir, destDir);
            } else {
                try (InputStream is = src.createDocumentInputStream(e)) {
                    dest.createDocument(e.getName(), is);
                }
            }
        }
    }
    
    

    /**
     * Knuth-Morris-Pratt Algorithm for Pattern Matching
     * Finds the first occurrence of the pattern in the text.
     */
    private static int indexOf(byte[] data, int offset, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0) {
            return -1;
        }

        for (int i = offset; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) { j++; }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }

    
}
