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

package org.apache.poi.extractor;

import static org.apache.poi.hssf.model.InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.extractor.OldExcelExtractor;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * ExtractorFactory for HSSF and Old Excel format
 */
public class MainExtractorFactory implements ExtractorProvider {
    @Override
    public boolean accepts(FileMagic fm) {
        return FileMagic.OLE2 == fm;
    }

    @SuppressWarnings({"java:S2095"})
    @Override
    public POITextExtractor create(File file, String password) throws IOException {
        return create(new POIFSFileSystem(file, true).getRoot(), password);
    }

    @Override
    public POITextExtractor create(InputStream inputStream, String password) throws IOException {
        return create(new POIFSFileSystem(inputStream).getRoot(), password);
    }

    @Override
    public POITextExtractor create(DirectoryNode poifsDir, String password) throws IOException {
        final String oldPW = Biff8EncryptionKey.getCurrentUserPassword();
        try {
            Biff8EncryptionKey.setCurrentUserPassword(password);

            // Look for certain entries in the stream, to figure it out from
            for (String workbookName : WORKBOOK_DIR_ENTRY_NAMES) {
                if (poifsDir.hasEntry(workbookName)) {
                    return ExtractorFactory.getPreferEventExtractor() ? new EventBasedExcelExtractor(poifsDir) : new ExcelExtractor(poifsDir);
                }
            }

            if (poifsDir.hasEntry(InternalWorkbook.OLD_WORKBOOK_DIR_ENTRY_NAME)) {
                return new OldExcelExtractor(poifsDir);
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(oldPW);
        }

        return null;
    }
}
