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

import java.io.File;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;

/**
 * A command line wrapper around {@link ExtractorFactory}, useful
 * for when debugging.
 */
public final class CommandLineTextExtractor {
    public static final String DIVIDER = "=======================";

    private CommandLineTextExtractor() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("   CommandLineTextExtractor <filename> [filename] [filename]");
            System.exit(1);
        }

        for (String arg : args) {
            System.out.println(DIVIDER);

            File f = new File(arg);
            System.out.println(f);

            try (POITextExtractor extractor = ExtractorFactory.createExtractor(f)) {
                POITextExtractor metadataExtractor =
                        extractor.getMetadataTextExtractor();

                System.out.println("   " + DIVIDER);
                String metaData = metadataExtractor.getText();
                System.out.println(metaData);
                System.out.println("   " + DIVIDER);
                String text = extractor.getText();
                System.out.println(text);
                System.out.println(DIVIDER);
                System.out.println("Had " + metaData.length() + " characters of metadata and " + text.length() + " characters of text");
            }
        }
    }
}
