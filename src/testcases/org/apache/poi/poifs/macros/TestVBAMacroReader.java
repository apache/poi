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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;
import org.junit.Test;

public class TestVBAMacroReader {
    private final String testMacroContents;
    private final String testMacroNoSub;
    public TestVBAMacroReader() throws Exception {
        File macro = HSSFTestDataSamples.getSampleFile("SimpleMacro.vba");
        testMacroContents = new String(
                IOUtils.toByteArray(new FileInputStream(macro)),
                StringUtil.UTF8
        );
        
        if (! testMacroContents.startsWith("Sub ")) {
            throw new IllegalArgumentException("Not a macro");
        }
        testMacroNoSub = testMacroContents.substring(testMacroContents.indexOf("()")+3);
    }
    
    @Test
    public void HSSFfromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    public void XSSFfromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }

    @Test
    public void HSSFfromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    public void XSSFfromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }

    @Test
    public void fromNPOIFS() throws Exception {
        NPOIFSFileSystem fs = new NPOIFSFileSystem(
                HSSFTestDataSamples.getSampleFile("SimpleMacro.xls"));
        VBAMacroReader r = new VBAMacroReader(fs);
        assertMacroContents(r);
        r.close();
    }

    protected void fromFile(POIDataSamples poiDataSamples, String filename) {   
        File f = poiDataSamples.getSampleFile(filename);
        VBAMacroReader r = new VBAMacroReader(f);
        try {
            assertMacroContents(r);
        } finally {
            r.close();
        }
    }

    protected void fromStream(POIDataSamples poiDataSamples, String filename) {   
        InputStream fis = poiDataSamples.openSampleFileStream(filename);
        try {
            VBAMacroReader r = new VBAMacroReader(fis);
            try {
                assertMacroContents(r);
            } finally {
                r.close();
            }
        }
    }
    
    protected void assertMacroContents(VBAMacroReader r) throws Exception {
        Map<String,String> contents = r.readMacros();
        
        assertFalse(contents.isEmpty());
        assertEquals(5, contents.size());
        
        // Check the ones without scripts
        String[] noScripts = new String[] { "ThisWorkbook",
                "Sheet1", "Sheet2", "Sheet3" };
        for (String entry : noScripts) {
            assertTrue(entry, contents.containsKey(entry));
            
            String content = contents.get(entry);
            assertContains(content, "Attribute VB_Exposed = True");
            assertContains(content, "Attribute VB_Customizable = True");
            assertContains(content, "Attribute VB_TemplateDerived = False");
            assertContains(content, "Attribute VB_GlobalNameSpace = False");
            assertContains(content, "Attribute VB_Exposed = True");
        }
        
        // Check the script one
        String content = contents.get("Module1");
        assertContains(content, "Attribute VB_Name = \"Module1\"");
        assertContains(content, "Attribute TestMacro.VB_Description = \"This is a test macro\"");

        // And the macro itself
        assertContains(content, testMacroNoSub);
    }
}
