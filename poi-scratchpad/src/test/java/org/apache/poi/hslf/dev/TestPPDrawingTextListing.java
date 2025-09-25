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
package org.apache.poi.hslf.dev;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.EmptyFileException;
import org.junit.jupiter.api.Test;

public class TestPPDrawingTextListing extends BaseTestPPTIterating {
    static final Set<String> LOCAL_EXCLUDED = new HashSet<>();
    static {
        LOCAL_EXCLUDED.add("clusterfuzz-testcase-minimized-POIFuzzer-5681320547975168.ppt");
    }

    @Test
    void testMain() throws IOException {
        // calls System.exit(): PPDrawingTextListing.main(new String[0]);
        assertThrows(EmptyFileException.class, () -> PPDrawingTextListing.main(new String[]{"invalidfile"}));
    }

    @Override
    void runOneFile(File pFile) throws Exception {
        try {
            PPDrawingTextListing.main(new String[]{pFile.getAbsolutePath()});
        } catch (IndexOutOfBoundsException | IOException e) {
            if (!LOCAL_EXCLUDED.contains(pFile.getName())) {
                throw e;
            }
        }

        // work around one file which works here but not in other tests
        if (pFile.getName().equals("clusterfuzz-testcase-minimized-POIFuzzer-5681320547975168.ppt")) {
            throw new FileNotFoundException();
        }
    }
}
