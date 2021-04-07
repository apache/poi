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
package org.apache.poi.xssf.streaming;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.poifs.crypt.temp.SXSSFWorkbookWithCustomZipEntrySource;
import org.apache.poi.poifs.crypt.temp.SheetDataWriterWithDecorator;

// a class to record a list of temporary files that are written to disk
// afterwards, a test function can check whether these files were encrypted or not
public class TempFileRecordingSXSSFWorkbookWithCustomZipEntrySource extends SXSSFWorkbookWithCustomZipEntrySource {

    private final List<File> tempFiles = new ArrayList<>();

    List<File> getTempFiles() {
        return new ArrayList<>(tempFiles);
    }
    
    @Override
    protected SheetDataWriter createSheetDataWriter() throws IOException {
        return new TempFileRecordingSheetDataWriterWithDecorator();
    }

    class TempFileRecordingSheetDataWriterWithDecorator extends SheetDataWriterWithDecorator {

        TempFileRecordingSheetDataWriterWithDecorator() throws IOException {
            super();
            tempFiles.add(getTempFile());
        }
    }
}