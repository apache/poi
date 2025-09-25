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

import org.apache.poi.EmptyFileException;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestPPTXMLDump extends BaseTestPPTIterating {
    static final Set<String> LOCAL_EXCLUDED = new HashSet<>();
    static {
        LOCAL_EXCLUDED.add("clusterfuzz-testcase-minimized-POIHSLFFuzzer-5306877435838464.ppt");
        LOCAL_EXCLUDED.add("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6032591399288832.ppt");
        LOCAL_EXCLUDED.add("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6360479850954752.ppt");
        LOCAL_EXCLUDED.add("ppt_with_png_encrypted.ppt");
        LOCAL_EXCLUDED.add("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6614960949821440.ppt");
    }

    @Test
    void testMain() throws Exception {
        PPTXMLDump.main(new String[0]);

        PPTXMLDump.main(new String[]{
                HSLFTestDataSamples.getSampleFile("slide_master.ppt").getAbsolutePath(),
                HSLFTestDataSamples.getSampleFile("pictures.ppt").getAbsolutePath()
        });

        assertThrows(EmptyFileException.class, () -> PPTXMLDump.main(new String[]{"invalidfile"}));
    }

    @Override
    void runOneFile(File pFile) throws Exception {
        try {
           PPTXMLDump.main(new String[]{pFile.getAbsolutePath()});
            if (LOCAL_EXCLUDED.contains(pFile.getName())) {
                throw new IllegalStateException("Expected failure for file " + pFile + ", but processing did not throw an exception");
            }
        } catch (IndexOutOfBoundsException | IOException e) {
            if (!LOCAL_EXCLUDED.contains(pFile.getName())) {
                throw e;
            }
        }

        // work around two files which works here but not in other tests
        if (pFile.getName().equals("clusterfuzz-testcase-minimized-POIFuzzer-5429732352851968.ppt") ||
                pFile.getName().equals("clusterfuzz-testcase-minimized-POIFuzzer-5681320547975168.ppt") ||
                pFile.getName().equals("clusterfuzz-testcase-minimized-POIHSLFFuzzer-5231088823566336.ppt") ||
                pFile.getName().equals("clusterfuzz-testcase-minimized-POIFuzzer-6411649193738240.ppt") ||
                pFile.getName().equals("clusterfuzz-testcase-minimized-POIHSLFFuzzer-4838893004128256.ppt") ||
                pFile.getName().equals("clusterfuzz-testcase-minimized-POIHSLFFuzzer-4624961081573376.ppt")) {
            throw new FileNotFoundException();
        }
    }

    @Override
    protected Set<String> getFailedEncryptedFiles() {
        return Collections.emptySet();
    }

    @Override
    protected Set<String> getFailedOldFiles() {
        return Collections.emptySet();
    }
}
