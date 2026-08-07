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

import org.apache.poi.util.RLEDecompressingInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Fuzz target for RLEDecompressingInputStream.
 * Used by Google's OSS-Fuzz for continuous security testing.
 */
public class POIRleFuzzer {
    public static void fuzzerTestOneInput(byte[] input) {
        try (RLEDecompressingInputStream rleStream =
                     new RLEDecompressingInputStream(new ByteArrayInputStream(input))) {

            byte[] buffer = new byte[1024];
            while (true) {
                // Trigger decompression logic
                int ret = rleStream.read(buffer);
                if (ret == -1) {
                    break;
                }

                if (ret < 0) {
                    throw new RuntimeException("Invalid return value while reading from stream: " + ret);
                }
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            // Expected exceptions on malformed input
        }
    }
}
