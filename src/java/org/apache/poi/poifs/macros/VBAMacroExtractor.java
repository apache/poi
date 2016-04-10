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

package org.apache.poi.poifs.macros;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.poi.util.StringUtil;

/**
 * This class extracts out the source of all VBA Modules of an office file,
 *  both OOXML and OLE2/POIFS, eg XLSM or DOC
 */
public class VBAMacroExtractor {
    public static void main(String args[]) throws IOException {
        if (args.length == 0) {
            System.err.println("Use:");
            System.err.println("   VBAMacroExtractor <office.doc> [output]");
            System.err.println("");
            System.err.println("If an output directory is given, macros are written there");
            System.err.println("Otherwise they are output to the screen");
            System.exit(1);
        }
        
        File input = new File(args[0]);
        File output = null;
        if (args.length > 1) {
            output = new File(args[1]);
        }
        
        VBAMacroExtractor extract = new VBAMacroExtractor();
        extract.extract(input, output);
    }
    
    public void extract(File input, File outputDir) throws IOException {
        if (! input.exists()) throw new FileNotFoundException(input.toString());
        System.err.print("Extracting VBA Macros from " + input + " to ");
        if (outputDir != null) {
            if (! outputDir.exists()) outputDir.mkdir();
            System.err.println(outputDir);
        } else {
            System.err.println("STDOUT");
        }
        
        VBAMacroReader reader = new VBAMacroReader(input);
        Map<String,String> macros = reader.readMacros();
        reader.close();
        
        final String divider = "---------------------------------------";
        for (String macro : macros.keySet()) {
            if (outputDir == null) {
                System.out.println(divider);
                System.out.println(macro);
                System.out.println("");
                System.out.println(macros.get(macro));
            } else {
                File out = new File(outputDir, macro + ".vba");
                FileOutputStream fout = new FileOutputStream(out);
                OutputStreamWriter fwriter = new OutputStreamWriter(fout, StringUtil.UTF8);
                fwriter.write(macros.get(macro));
                fwriter.close();
                fout.close();
                System.out.println("Extracted " + out);
            }
        }
        if (outputDir == null) {
            System.out.println(divider);
        }
    }
}
