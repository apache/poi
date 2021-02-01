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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FileHandlerFactory {
    // map from patterns for mimetypes to the FileHandlers that should be able to
    // work with that file
    // use a Set<Pair> to have a defined order of applying the matches
    private static final Map<Pattern, FileHandler> MIME_TYPES = new HashMap<>();
    static {
        ////////////////// Word

        MIME_TYPES.put(Pattern.compile("application/vnd.ms-word.document.macroenabled.12"), new XWPFFileHandler());
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-word.template.macroenabled.12"), new XWPFFileHandler());

        // application/msword
        MIME_TYPES.put(Pattern.compile(".*msword.*"), new HWPFFileHandler());
        // application/vnd.ms-word
        MIME_TYPES.put(Pattern.compile(".*ms-word.*"), new HWPFFileHandler());

        // application/vnd.openxmlformats-officedocument.wordprocessingml.document
        MIME_TYPES.put(Pattern.compile(".*wordprocessingml.*"), new XWPFFileHandler());

        ////////////////// Excel
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-excel.addin.macroEnabled.12"), new XSSFFileHandler());
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-excel.sheet.binary.macroEnabled.12"), new XSSFFileHandler());

        // application/msexcel
        MIME_TYPES.put(Pattern.compile(".*msexcel.*"), new HSSFFileHandler());
        // application/vnd.ms-excel
        MIME_TYPES.put(Pattern.compile(".*ms-excel.*"), new HSSFFileHandler());

        // application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
        MIME_TYPES.put(Pattern.compile(".*spreadsheetml.*"), new XSSFFileHandler());

        ////////////////// Powerpoint

        // application/vnd.ms-powerpoint
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-powerpoint"), new HSLFFileHandler());
        // application/vnd.ms-officetheme
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-officetheme"), new HSLFFileHandler());

        // application/vnd.openxmlformats-officedocument.presentationml.presentation
        MIME_TYPES.put(Pattern.compile(".*presentationml.*"), new XSLFFileHandler());
        // application/vnd.ms-powerpoint.presentation.macroenabled.12
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-powerpoint.presentation.macroenabled.12"), new XSLFFileHandler());
        // application/vnd.ms-powerpoint.slideshow.macroenabled.12
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-powerpoint.slideshow.macroenabled.12"), new XSLFFileHandler());

        ////////////////// Mail/TNEF

        // application/vnd.ms-tnef
        MIME_TYPES.put(Pattern.compile(".*ms-tnef.*"), new HMEFFileHandler());

        // application/vnd.ms-outlook
        MIME_TYPES.put(Pattern.compile("application/vnd.ms-outlook"), new HSMFFileHandler());

        ////////////////// Visio

        // application/vnd.visio
        MIME_TYPES.put(Pattern.compile("application/vnd.visio.*"), new HDGFFileHandler());

        // application/vnd.ms-visio.drawing
        MIME_TYPES.put(Pattern.compile(".*vnd.ms-visio\\."), new XDGFFileHandler());

        //application/vnd.ms-visio.viewer
        MIME_TYPES.put(Pattern.compile(".*visio.*"), new HDGFFileHandler());


        ////////////////// Publisher

        // application/x-mspublisher
        MIME_TYPES.put(Pattern.compile("application/x-mspublisher"), new HPBFFileHandler());


        ////////////////// Others

        // special type used by Tika
        MIME_TYPES.put(Pattern.compile("application/x-tika-ooxml.*"), new OPCFileHandler());
        // special type used by Tika
        MIME_TYPES.put(Pattern.compile("application/x-tika-msoffice.*"), new POIFSFileHandler());

        // application/x-tika-old-excel
        MIME_TYPES.put(Pattern.compile("application/x-tika-old-excel"), new POIFSFileHandler());

        // application/vnd.openxmlformats-officedocument.drawingml.chart+xml
        // ?!MIME_TYPES.put(Pattern.compile(".*drawingml.*"), ".dwg");

        // application/vnd.openxmlformats-officedocument.vmlDrawing
        // ?!MIME_TYPES.put(Pattern.compile(".*vmlDrawing.*"), ".dwg");
    }

    public static FileHandler getHandler(String mimeType) {
        for(Map.Entry<Pattern,FileHandler> entry : MIME_TYPES.entrySet()) {
            if(entry.getKey().matcher(mimeType).matches()) {
                return entry.getValue();
            }
        }

        return null;
    }
}
