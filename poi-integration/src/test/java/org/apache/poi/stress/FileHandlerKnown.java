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
package org.apache.poi.stress;

import java.io.File;
import java.io.InputStream;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public enum FileHandlerKnown {
    HDGF(HDGFFileHandler::new),
    HMEF(HMEFFileHandler::new),
    HPBF(HPBFFileHandler::new),
    HPSF(HPSFFileHandler::new),
    HSLF(HSLFFileHandler::new),
    HSMF(HSMFFileHandler::new),
    HSSF(HSSFFileHandler::new),
    HWPF(HWPFFileHandler::new),
    OPC(OPCFileHandler::new),
    POIFS(POIFSFileHandler::new),
    XDGF(XDGFFileHandler::new),
    XSLF(XSLFFileHandler::new),
    XSSFB(XSSFBFileHandler::new),
    XSSF(XSSFFileHandler::new),
    XWPF(XWPFFileHandler::new),
    OWPF(OWPFFileHandler::new),
    NULL(NullFileHandler::new)
    ;

    public final Supplier<FileHandler> fileHandler;

    FileHandlerKnown(Supplier<FileHandler> fileHandler) {
        this.fileHandler = fileHandler;
    }

    private static class NullFileHandler implements FileHandler {
        @Override
        public void handleFile(InputStream stream, String path) {}

        @Override
        public void handleExtracting(File file) {}

        @Override
        public void handleAdditional(File file) {}
    }
}
