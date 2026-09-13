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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public final class TestPackagePartName {

    /**
     * Test method getExtension().
     */
    @Test
    void testGetExtension() throws Exception{
        PackagePartName name1 = PackagingURIHelper.createPartName("/doc/props/document.xml");
        PackagePartName name2 = PackagingURIHelper.createPartName("/root/document");
        assertEquals("xml", name1.getExtension());
        assertEquals("", name2.getExtension());
    }

    @Test
    void testGetRelationshipPartName() throws Exception {
        PackagePartName partName = PackagingURIHelper.createPartName("/word/document.xml");
        PackagePartName relName = PackagingURIHelper.getRelationshipPartName(partName);
        assertEquals("/word/_rels/document.xml.rels", relName.getName());
    }

    /**
     * URI.getPath() decodes percent-encoding, so a part name with spaces
     * (stored as %20) used to make createPartName throw and this method
     * return null. Keep the raw path so the relationship part name is valid.
     */
    @Test
    void testGetRelationshipPartNameWithPercentEncodedCharacters() throws Exception {
        PackagePartName partName = PackagingURIHelper.createPartName("/aasx/test%20document.txt");
        PackagePartName relName = PackagingURIHelper.getRelationshipPartName(partName);
        assertNotNull(relName);
        assertEquals("/aasx/_rels/test%20document.txt.rels", relName.getName());
    }
}
