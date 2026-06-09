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

package org.apache.poi.fuzz;

import org.apache.poi.fuzz.POIFileHandlerFuzzer;
import java.io.File;
import java.nio.file.Files;

public class FuzzerRunner {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: FuzzerRunner <file-to-fuzz>");
            System.exit(1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("File not found: " + args[0]);
            System.exit(1);
        }
        byte[] input = Files.readAllBytes(f.toPath());
        System.out.println("Running fuzzer for file: " + args[0] + " (" + input.length + " bytes)");
        POIFileHandlerFuzzer.fuzzerInitialize();
        POIFileHandlerFuzzer.fuzzerTestOneInput(input);
        System.out.println("Success!");
    }
}
