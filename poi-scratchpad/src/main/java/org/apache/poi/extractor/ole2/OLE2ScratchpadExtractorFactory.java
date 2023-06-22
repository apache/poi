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
package org.apache.poi.extractor.ole2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.ExtractorProvider;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.extractor.OutlookTextExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * Scratchpad-specific logic for {@link ExtractorFactory} and
 *  {@link ExtractorFactory}, which permit the other two to run with
 *  no Scratchpad jar (though without functionality!)
 * <p>Note - should not be used standalone, always use via the other
 *  two classes</p>
 */
@SuppressWarnings("WeakerAccess")
public class OLE2ScratchpadExtractorFactory implements ExtractorProvider {
    private static final Logger LOG = LogManager.getLogger(OLE2ScratchpadExtractorFactory.class);

    private static final String[] OUTLOOK_ENTRY_NAMES = {
        // message bodies, saved as plain text (PtypString)
        // The first short (0x1000, 0x0047, 0x0037) refer to the Property ID (see [MS-OXPROPS].pdf)
        // the second short (0x001e, 0x001f, 0x0102) refer to the type of data stored in this entry
        // https://msdn.microsoft.com/endatatypes.Ex-us/library/cc433490(v=exchg.80).aspx
        // @see org.apache.poi.hsmf.Types.MAPIType
        "__substg1.0_1000001E", //PidTagBody ASCII
        "__substg1.0_1000001F", //PidTagBody Unicode
        "__substg1.0_0047001E", //PidTagMessageSubmissionId ASCII
        "__substg1.0_0047001F", //PidTagMessageSubmissionId Unicode
        "__substg1.0_0037001E", //PidTagSubject ASCII
        "__substg1.0_0037001F", //PidTagSubject Unicode
    };


    @Override
    public boolean accepts(FileMagic fm) {
        return FileMagic.OLE2 == fm;
    }

    @SuppressWarnings("java:S2095")
    @Override
    public POITextExtractor create(File file, String password) throws IOException {
        return create(new POIFSFileSystem(file, true).getRoot(), password);
    }

    @Override
    public POITextExtractor create(InputStream inputStream, String password) throws IOException {
        return create(new POIFSFileSystem(inputStream).getRoot(), password);
    }

    /**
     * Look for certain entries in the stream, to figure it
     * out what format is desired
     * Note - doesn't check for core-supported formats!
     * Note - doesn't check for OOXML-supported formats
     *
     * @param poifsDir the directory node to be inspected
     * @return the format specific text extractor
     *
     * @throws IOException when the format specific extraction fails because of invalid entires
     */
    @SuppressWarnings("java:S2093")
    @Override
    public POITextExtractor create(DirectoryNode poifsDir, String password) throws IOException {
        final String oldPW = Biff8EncryptionKey.getCurrentUserPassword();
        try {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            if (poifsDir.hasEntryCaseInsensitive("WordDocument")) {
                // Old or new style word document?
                try {
                    return new WordExtractor(poifsDir);
                } catch (OldWordFileFormatException e) {
                    return new Word6Extractor(poifsDir);
                }
            }

            if (poifsDir.hasEntryCaseInsensitive(HSLFSlideShow.POWERPOINT_DOCUMENT) || poifsDir.hasEntryCaseInsensitive(HSLFSlideShow.PP97_DOCUMENT)) {
                return new SlideShowExtractor<>((HSLFSlideShow)SlideShowFactory.create(poifsDir));
            }

            if (poifsDir.hasEntryCaseInsensitive("VisioDocument")) {
                return new VisioTextExtractor(poifsDir);
            }

            if (poifsDir.hasEntryCaseInsensitive("Quill")) {
                return new PublisherTextExtractor(poifsDir);
            }

            for (String entryName : OUTLOOK_ENTRY_NAMES) {
                if (poifsDir.hasEntryCaseInsensitive(entryName)) {
                    return new OutlookTextExtractor(poifsDir);
                }
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(oldPW);
        }

        return null;
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     *
     * @param ext the extractor holding the directory to start parsing
     * @param dirs a list to be filled with directory references holding embedded
     * @param nonPOIFS a list to be filled with streams which aren't based on POIFS entries
     */
    @Override
    public void identifyEmbeddedResources(POIOLE2TextExtractor ext, List<Entry> dirs, List<InputStream> nonPOIFS) {
        // Find all the embedded directories
        DirectoryEntry root = ext.getRoot();
        if (root == null) {
            throw new IllegalStateException("The extractor didn't know which POIFS it came from!");
        }

        if (ext instanceof ExcelExtractor) {
            // These are in MBD... under the root
            StreamSupport.stream(root.spliterator(), false)
                .filter(entry -> entry.getName().startsWith("MBD"))
                .forEach(dirs::add);
        } else if (ext instanceof WordExtractor) {
            // These are in ObjectPool -> _... under the root
            try {
                DirectoryEntry op = (DirectoryEntry) root.getEntryCaseInsensitive("ObjectPool");
                StreamSupport.stream(op.spliterator(), false)
                    .filter(entry -> entry.getName().startsWith("_"))
                    .forEach(dirs::add);
            } catch(FileNotFoundException e) {
                LOG.atInfo().withThrowable(e).log("Ignoring FileNotFoundException while extracting Word document");
                // ignored here
            }
            //} else if(ext instanceof PowerPointExtractor) {
            // Tricky, not stored directly in poifs
            // TODO
        } else if (ext instanceof OutlookTextExtractor) {
            // Stored in the Attachment blocks
            MAPIMessage msg = ((OutlookTextExtractor)ext).getMAPIMessage();
            for (AttachmentChunks attachment : msg.getAttachmentFiles()) {
                if (attachment.getAttachData() != null) {
                    byte[] data = attachment.getAttachData().getValue();
                    try {
                        nonPOIFS.add(UnsynchronizedByteArrayInputStream.builder().setByteArray(data).get() );
                    } catch (IOException e) {
                        // is actually impossible with ByteArray, but still declared in the interface
                        throw new IllegalStateException(e);
                    }
                } else if (attachment.getAttachmentDirectory() != null) {
                    dirs.add(attachment.getAttachmentDirectory().getDirectory());
                }
            }
        }
    }
}
