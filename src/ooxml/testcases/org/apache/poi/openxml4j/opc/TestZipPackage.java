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

package org.apache.poi.openxml4j.opc;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.junit.Test;

public class TestZipPackage {
    @Test
    public void testBug56479() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("dcterms_bug_56479.zip");
        OPCPackage p = OPCPackage.open(is);
        
        // Check we found the contents of it
        boolean foundCoreProps = false, foundDocument = false, foundTheme1 = false;
        for (PackagePart part : p.getParts()) {
            if (part.getPartName().toString().equals("/docProps/core.xml")) {
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, part.getContentType());
                foundCoreProps = true;
            }
            if (part.getPartName().toString().equals("/word/document.xml")) {
                assertEquals(XWPFRelation.DOCUMENT.getContentType(), part.getContentType());
                foundDocument = true;
            }
            if (part.getPartName().toString().equals("/word/theme/theme1.xml")) {
                assertEquals(XWPFRelation.THEME.getContentType(), part.getContentType());
                foundTheme1 = true;
            }
        }
        assertTrue("Core not found in " + p.getParts(), foundCoreProps);
        assertFalse("Document should not be found in " + p.getParts(), foundDocument);
        assertFalse("Theme1 should not found in " + p.getParts(), foundTheme1);
    }
}
