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
package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.output.NullPrintStream;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock(Resources.SYSTEM_OUT)
class TestRecordLister extends BaseTestIteratingXLS {
    @Override
    void runOneFile(File fileIn) throws IOException {
        PrintStream save = System.out;
        try {
            // redirect standard out during the test to avoid spamming the console with output
            System.setOut(new NullPrintStream());

            RecordLister viewer = new RecordLister();
            viewer.setFile(fileIn.getAbsolutePath());
            viewer.run();
        } finally {
            System.setOut(save);
        }
    }

    //@Test
    void testFile() throws IOException {
        RecordLister viewer = new RecordLister();
        viewer.setFile(new File("test-data/spreadsheet/testEXCEL_95.xls").getAbsolutePath());
        viewer.run();
    }
}
