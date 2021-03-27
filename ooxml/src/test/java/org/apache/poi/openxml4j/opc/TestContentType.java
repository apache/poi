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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for content type (ContentType class).
 */
public final class TestContentType {

    private static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * Check rule M1.13: Package implementers shall only create and only
     * recognize parts with a content type; format designers shall specify a
     * content type for each part included in the format. Content types for
     * package parts shall fit the definition and syntax for media types as
     * specified in RFC 2616, \u00A73.7.
     */
    @ParameterizedTest
    @ValueSource(strings = {"text/xml", "application/pgp-key", "application/vnd.hp-PCLXL", "application/vnd.lotus-1-2-3"})
    void testContentTypeValidation(String contentType) throws InvalidFormatException {
        assertDoesNotThrow(() -> new ContentType(contentType));
    }

    /**
     * Check rule M1.13 : Package implementers shall only create and only
     * recognize parts with a content type; format designers shall specify a
     * content type for each part included in the format. Content types for
     * package parts shall fit the definition and syntax for media types as
     * specified in RFC 2616, \u00A3.7.
     * <p>
     * Check rule M1.14: Content types shall not use linear white space either
     * between the type and subtype or between an attribute and its value.
     * Content types also shall not have leading or trailing white spaces.
     * Package implementers shall create only such content types and shall
     * require such content types when retrieving a part from a package; format
     * designers shall specify only such content types for inclusion in the
     * format.
     */
    @ParameterizedTest
    @ValueSource(strings = {"text/xml/app", "",
        "test", "text(xml/xml", "text)xml/xml", "text<xml/xml",
        "text>/xml", "text@/xml", "text,/xml", "text;/xml",
        "text:/xml", "text\\/xml", "t/ext/xml", "t\"ext/xml",
        "text[/xml", "text]/xml", "text?/xml", "tex=t/xml",
        "te{xt/xml", "tex}t/xml", "te xt/xml",
        "text\u0009/xml", "text xml", " text/xml "})
    void testContentTypeValidationFailure(String contentType) {
        assertThrows(InvalidFormatException.class, () -> new ContentType(contentType),
            "Must have fail for content type: '" + contentType + "' !");
    }

    /**
     * Parameters are allowed, provides that they meet the
     * criteria of rule [01.2]
     * Invalid parameters are verified as incorrect in
     * {@link #testContentTypeParameterFailure()}
     */
    @ParameterizedTest
    @ValueSource(strings = {"mail/toto;titi=tata",
        "text/xml;a=b;c=d", "text/xml;key1=param1;key2=param2",
        "application/pgp-key;version=\"2\"",
        "application/x-resqml+xml;version=2.0;type=obj_global2dCrs"})
    void testContentTypeParam(String contentType) {
        assertDoesNotThrow(() -> new ContentType(contentType));
    }

    /**
     * Check rule [O1.2]: Format designers might restrict the usage of
     * parameters for content types.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "mail/toto;\"titi=tata\"", // quotes not allowed like that
        "mail/toto;titi = tata", // spaces not allowed
        "text/\u0080" // characters above ASCII are not allowed
    })
    void testContentTypeParameterFailure(String contentType) {
        assertThrows(InvalidFormatException.class, () -> new ContentType(contentType),
            "Must have fail for content type: '" + contentType + "' !");
    }

    /**
     * Check rule M1.15: The package implementer shall require a content type
     * that does not include comments and the format designer shall specify such
     * a content type.
     */
    @ParameterizedTest
    @ValueSource(strings = {"text/xml(comment)"})
    void testContentTypeCommentFailure(String contentType) {
        assertThrows(InvalidFormatException.class, () -> new ContentType(contentType),
            "Must have fail for content type: '" + contentType + "' !");
    }

    /**
     * OOXML content types don't need entities and we shouldn't
     * barf if we get one from a third party system that added them
     * (expected = InvalidFormatException.class)
     */
    @Test
    void testFileWithContentTypeEntities() throws Exception {
        try (InputStream is = OpenXML4JTestDataSamples.openSampleStream("ContentTypeHasEntities.ooxml")) {
            if (isOldXercesActive()) {
                OPCPackage.open(is);
            } else {
                assertThrows(InvalidFormatException.class, () -> OPCPackage.open(is));
            }
        }
    }

    /**
     * Check that we can open a file where there are valid
     * parameters on a content type
     */
    @Test
    void testFileWithContentTypeParams() throws Exception {
        try (InputStream is = OpenXML4JTestDataSamples.openSampleStream("ContentTypeHasParameters.ooxml");
             OPCPackage p = OPCPackage.open(is)) {

            final String typeResqml = "application/x-resqml+xml";

            // Check the types on everything
            for (PackagePart part : p.getParts()) {
                final String contentType = part.getContentType();
                final ContentType details = part.getContentTypeDetails();
                final int length = details.getParameterKeys().length;
                final boolean hasParameters = details.hasParameters();

                // _rels type doesn't have any params
                if (part.isRelationshipPart()) {
                    assertEquals(ContentTypes.RELATIONSHIPS_PART, contentType);
                    assertEquals(ContentTypes.RELATIONSHIPS_PART, details.toString());
                    assertFalse(hasParameters);
                    assertEquals(0, length);
                }
                // Core type doesn't have any params
                else if (part.getPartName().toString().equals("/docProps/core.xml")) {
                    assertEquals(ContentTypes.CORE_PROPERTIES_PART, contentType);
                    assertEquals(ContentTypes.CORE_PROPERTIES_PART, details.toString());
                    assertFalse(hasParameters);
                    assertEquals(0, length);
                }
                // Global Crs types do have params
                else if (part.getPartName().toString().equals("/global1dCrs.xml")) {
                    assertTrue(part.getContentType().startsWith(typeResqml));
                    assertEquals(typeResqml, details.toString(false));
                    assertTrue(hasParameters);
                    assertContains("version=2.0", details.toString());
                    assertContains("type=obj_global1dCrs", details.toString());
                    assertEquals(2, length);
                    assertEquals("2.0", details.getParameter("version"));
                    assertEquals("obj_global1dCrs", details.getParameter("type"));
                } else if (part.getPartName().toString().equals("/global2dCrs.xml")) {
                    assertTrue(part.getContentType().startsWith(typeResqml));
                    assertEquals(typeResqml, details.toString(false));
                    assertTrue(hasParameters);
                    assertContains("version=2.0", details.toString());
                    assertContains("type=obj_global2dCrs", details.toString());
                    assertEquals(2, length);
                    assertEquals("2.0", details.getParameter("version"));
                    assertEquals("obj_global2dCrs", details.getParameter("type"));
                }
                // Other thingy
                else if (part.getPartName().toString().equals("/myTestingGuid.xml")) {
                    assertTrue(part.getContentType().startsWith(typeResqml));
                    assertEquals(typeResqml, details.toString(false));
                    assertTrue(hasParameters);
                    assertContains("version=2.0", details.toString());
                    assertContains("type=obj_tectonicBoundaryFeature", details.toString());
                    assertEquals(2, length);
                    assertEquals("2.0", details.getParameter("version"));
                    assertEquals("obj_tectonicBoundaryFeature", details.getParameter("type"));
                }
                // That should be it!
                else {
                    fail("Unexpected part " + part);
                }
            }
        }
    }

    private static void assertContains(String needle, String haystack) {
        assertTrue(haystack.contains(needle));
    }

    public static boolean isOldXercesActive() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(FEATURE_DISALLOW_DOCTYPE_DECL, true);
            return false;
        } catch (Exception|AbstractMethodError ignored) {}
        return true;
    }
}
