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

import java.io.File;

import org.apache.poi.POITextExtractor;

/**
 * A command line wrapper around {@link ExtractorFactory}, useful
 *  for when debugging.
 */
public class CommandLineTextExtractor {
   public static String DIVIDER = "=======================";
   
   public static void main(String[] args) throws Exception {
      if(args.length < 1) {
         System.err.println("Use:");
         System.err.println("   CommandLineTextExtractor <filename> [filename] [filename]");
         System.exit(1);
      }
      
      for(int i=0; i<args.length; i++) {
         System.out.println(DIVIDER);
         
         File f = new File(args[i]);
         System.out.println(f);
         
         POITextExtractor extractor = 
            ExtractorFactory.createExtractor(f);
         POITextExtractor metadataExtractor =
            extractor.getMetadataTextExtractor();
         
         System.out.println("   " + DIVIDER);
         System.out.println(metadataExtractor.getText());
         System.out.println("   " + DIVIDER);
         System.out.println(extractor.getText());
         System.out.println(DIVIDER);
      }
   }
}
