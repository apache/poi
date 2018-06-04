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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src
==================================================================== */
package org.apache.poi.dev;

import org.apache.poi.ooxml.dev.OOXMLPrettyPrint;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestOOXMLPrettyPrint {

    private static PrintStream SYSTEM_OUT;

    @BeforeClass
    public static void setUp() throws UnsupportedEncodingException {
        SYSTEM_OUT = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }, false, "UTF-8"));
    }

    @AfterClass
    public static void tearDown() {
        System.setOut(SYSTEM_OUT);
    }

    @Test
    public void testMain() throws Exception {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        File outFile = TempFile.createTempFile("Formatting", "-pretty.xlsx");

        assertTrue(outFile.delete());
        assertFalse(outFile.exists());

        OOXMLPrettyPrint.main(new String[] {
            file.getAbsolutePath(), outFile.getAbsolutePath()
        });

        assertTrue(outFile.exists());
        assertTrue(outFile.delete());
    }
}