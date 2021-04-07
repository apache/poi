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

import java.net.URI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

class TestPackagingURIHelper {

    /**
     * Test relativizePartName() method.
     */
    @Test
    void testRelativizeURI() throws Exception {
        URI uri1 = new URI("/word/document.xml");
        URI uri2 = new URI("/word/media/image1.gif");
        URI uri3 = new URI("/word/media/image1.gif#Sheet1!A1");
        URI uri4 = new URI("#'My%20Sheet1'!A1");

        // Document to image is down a directory
        URI retURI1to2 = PackagingURIHelper.relativizeURI(uri1, uri2);
        assertEquals("media/image1.gif", retURI1to2.getPath());
        // Image to document is up a directory
        URI retURI2to1 = PackagingURIHelper.relativizeURI(uri2, uri1);
        assertEquals("../document.xml", retURI2to1.getPath());

        // Document and CustomXML parts totally different [Julien C.]
        URI uriCustomXml = new URI("/customXml/item1.xml");
        URI uriRes = PackagingURIHelper.relativizeURI(uri1, uriCustomXml);
        assertEquals("../customXml/item1.xml", uriRes.toString());

        // Document to itself is the same place (empty URI)
        URI retURI2 = PackagingURIHelper.relativizeURI(uri1, uri1);
        // YK: the line below used to assert empty string which is wrong
        // if source and target are the same they should be relaitivized as the last segment,
        // see Bugzilla 51187
        assertEquals("document.xml", retURI2.getPath());

        // relativization against root
        URI root = new URI("/");
        uriRes = PackagingURIHelper.relativizeURI(root, uri1);
        assertEquals("/word/document.xml", uriRes.toString());

        //URI compatible with MS Office and OpenOffice: leading slash is removed
        uriRes = PackagingURIHelper.relativizeURI(root, uri1, true);
        assertNotNull(uriRes);
        assertEquals("word/document.xml", uriRes.toString());

        //preserve URI fragments
        uriRes = PackagingURIHelper.relativizeURI(uri1, uri3, true);
        assertNotNull(uriRes);
        assertEquals("media/image1.gif#Sheet1!A1", uriRes.toString());
        assertNotNull(uriRes);
        uriRes = PackagingURIHelper.relativizeURI(root, uri4, true);
        assertNotNull(uriRes);
        assertEquals("#'My%20Sheet1'!A1", uriRes.toString());
    }

    /**
     * Test createPartName(String, y)
     */
    @Test
    void testCreatePartNameRelativeString()
            throws InvalidFormatException {
        PackagePartName partNameToValid = PackagingURIHelper
                .createPartName("/word/media/image1.gif");

        OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
        // Base part
        PackagePartName nameBase = PackagingURIHelper
                .createPartName("/word/document.xml");
        PackagePart partBase = pkg.createPart(nameBase, ContentTypes.XML);
        // Relative part name
        PackagePartName relativeName = PackagingURIHelper.createPartName(
                "media/image1.gif", partBase);
        assertEquals(partNameToValid, relativeName, "The part name must be equal to " + partNameToValid.getName());
        pkg.revert();
    }

    /**
     * Test createPartName(URI, y)
     */
    @Test
    void testCreatePartNameRelativeURI() throws Exception {
        PackagePartName partNameToValid = PackagingURIHelper
                .createPartName("/word/media/image1.gif");

        OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
        // Base part
        PackagePartName nameBase = PackagingURIHelper
                .createPartName("/word/document.xml");
        PackagePart partBase = pkg.createPart(nameBase, ContentTypes.XML);
        // Relative part name
        PackagePartName relativeName = PackagingURIHelper.createPartName(
                new URI("media/image1.gif"), partBase);
        assertEquals(partNameToValid, relativeName, "The part name must be equal to " + partNameToValid.getName());
        pkg.revert();
    }

    @Test
    void testCreateURIFromString() throws Exception {
        String[] href = {
                "..\\\\\\cygwin\\home\\yegor\\.vim\\filetype.vim",
                "..\\Program%20Files\\AGEIA%20Technologies\\v2.3.3\\NxCooking.dll",
                "file:///D:\\seva\\1981\\r810102ns.mp3",
                "..\\cygwin\\home\\yegor\\dinom\\%5baccess%5d.2010-10-26.log",
                "#'Instructions (Text)'!B21",
                "#'性'!B21",
                "javascript://",
                "#ctl||rId16||cmdAddAction||_x0000_i1029"
        };

        for(String s : href){
            URI uri = PackagingURIHelper.toURI(s);
            assertNotNull(uri);
        }
    }

    @Test
    void test53734() throws Exception {
        URI uri = PackagingURIHelper.toURI("javascript://");
        // POI appends a trailing slash tpo avoid "Expected authority at index 13: javascript://"
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=53734
        assertEquals("javascript:///", uri.toASCIIString());
    }

}
