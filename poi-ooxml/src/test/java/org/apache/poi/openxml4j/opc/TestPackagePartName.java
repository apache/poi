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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

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
        assertEquals(partName.getURI(),
                PackagingURIHelper.getSourcePartUriFromRelationshipPartUri(relName.getURI()));
    }

    @Test
    void testIsRelationshipPartURI() throws Exception {
        assertTrue(PackagingURIHelper.createPartName("/_rels/.rels").isRelationshipPartURI());
        assertTrue(PackagingURIHelper.createPartName("/word/_rels/document.xml.rels").isRelationshipPartURI());
        assertFalse(PackagingURIHelper.createPartName("/word/document.xml").isRelationshipPartURI());
        assertFalse(PackagingURIHelper.createPartName("/word/_rels/document.xml").isRelationshipPartURI());
        assertFalse(PackagingURIHelper.createPartName("/word/_rels.rels").isRelationshipPartURI());
    }

    /**
     * The regex this used to be checked with backtracked quadratically on "/_rels" repeated
     */
    @Test
    void testIsRelationshipPartURISlowness() throws Exception {
        String name = "/_rels".repeat(40_000) + "/x";
        Boolean result = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> PackagingURIHelper.createPartName(name).isRelationshipPartURI());
        assertFalse(result);
    }

    @Test
    void testCompareNumbers() {
        assertThat(PackagePartName.compare("/f2.png", "/f10.png"), lessThan(0));
        assertThat(PackagePartName.compare("/f10.png", "/f2.png"), greaterThan(0));
        assertThat(PackagePartName.compare("/f007.xml", "/f7.xml"), greaterThan(0));
        assertThat(PackagePartName.compare("/f00.xml", "/f0.xml"), greaterThan(0));
        assertEquals(0, PackagePartName.compare("/a01b1.xml", "/a1b01.xml"));
        // more digits than fit in a long
        assertThat(PackagePartName.compare("/f" + "9".repeat(30), "/f1" + "0".repeat(30)), lessThan(0));
        assertThat(PackagePartName.compare("/f" + "2".repeat(30), "/f" + "1".repeat(30)), greaterThan(0));
    }

    /**
     * Parsing the digits as a BigInteger is quadratic in their number
     */
    @Test
    void testCompareSlowness() {
        String name1 = "/" + "1".repeat(65_000) + "2.xml";
        String name2 = "/" + "1".repeat(65_000) + "3.xml";
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int i = 0; i < 1_000; i++) {
                assertThat(PackagePartName.compare(name1, name2), lessThan(0));
            }
        });
    }
}
