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

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.apache.poi.xwpf.usermodel.XWPFRelation;

/**
 * Tests for content type (ContentType class).
 *
 * @author Julien Chable
 */
public final class TestContentType extends TestCase {

	/**
	 * Check rule M1.13: Package implementers shall only create and only
	 * recognize parts with a content type; format designers shall specify a
	 * content type for each part included in the format. Content types for
	 * package parts shall fit the definition and syntax for media types as
	 * specified in RFC 2616, \u00A73.7.
	 */
	public void testContentTypeValidation() throws InvalidFormatException {
		String[] contentTypesToTest = new String[] { "text/xml",
				"application/pgp-key", "application/vnd.hp-PCLXL",
				"application/vnd.lotus-1-2-3" };
		for (String contentType : contentTypesToTest) {
			new ContentType(contentType);
		}
	}

	/**
	 * Check rule M1.13 : Package implementers shall only create and only
	 * recognize parts with a content type; format designers shall specify a
	 * content type for each part included in the format. Content types for
	 * package parts shall fit the definition and syntax for media types as
	 * specified in RFC 2616, \u00A3.7.
	 *
	 * Check rule M1.14: Content types shall not use linear white space either
	 * between the type and subtype or between an attribute and its value.
	 * Content types also shall not have leading or trailing white spaces.
	 * Package implementers shall create only such content types and shall
	 * require such content types when retrieving a part from a package; format
	 * designers shall specify only such content types for inclusion in the
	 * format.
	 */
	public void testContentTypeValidationFailure() {
		String[] contentTypesToTest = new String[] { "text/xml/app", "",
				"test", "text(xml/xml", "text)xml/xml", "text<xml/xml",
				"text>/xml", "text@/xml", "text,/xml", "text;/xml",
				"text:/xml", "text\\/xml", "t/ext/xml", "t\"ext/xml",
				"text[/xml", "text]/xml", "text?/xml", "tex=t/xml",
				"te{xt/xml", "tex}t/xml", "te xt/xml",
				"text" + (char) 9 + "/xml", "text xml", " text/xml " };
		for (String contentType : contentTypesToTest) {
			try {
				new ContentType(contentType);
			} catch (InvalidFormatException e) {
				continue;
			}
			fail("Must have fail for content type: '" + contentType + "' !");
		}
	}

   /**
    * Parameters are allowed, provides that they meet the
    *  criteria of rule [01.2]
    * Invalid parameters are verified as incorrect in 
    *  {@link #testContentTypeParameterFailure()}
    */
   public void testContentTypeParam() throws InvalidFormatException {
      String[] contentTypesToTest = new String[] { "mail/toto;titi=tata",
               "text/xml;a=b;c=d", "text/xml;key1=param1;key2=param2",
               "application/pgp-key;version=\"2\"", 
               "application/x-resqml+xml;version=2.0;type=obj_global2dCrs"
      };
      for (String contentType : contentTypesToTest) {
          new ContentType(contentType);
      }
   }
   
	/**
	 * Check rule [O1.2]: Format designers might restrict the usage of
	 * parameters for content types.
	 */
	public void testContentTypeParameterFailure() {
		String[] contentTypesToTest = new String[] { 
		        "mail/toto;\"titi=tata\"", // quotes not allowed like that
                "mail/toto;titi = tata", // spaces not allowed
                "text/\u0080" // characters above ASCII are not allowed
        };
		for (String contentType : contentTypesToTest) {
			try {
				new ContentType(contentType);
			} catch (InvalidFormatException e) {
				continue;
			}
			fail("Must have fail for content type: '" + contentType + "' !");
		}
	}

	/**
	 * Check rule M1.15: The package implementer shall require a content type
	 * that does not include comments and the format designer shall specify such
	 * a content type.
	 */
	public void testContentTypeCommentFailure() {
		String[] contentTypesToTest = new String[] { "text/xml(comment)" };
		for (String contentType : contentTypesToTest) {
			try {
				new ContentType(contentType);
			} catch (InvalidFormatException e) {
				continue;
			}
			fail("Must have fail for content type: '" + contentType + "' !");
		}
	}
	
	/**
	 * OOXML content types don't need entities, but we shouldn't
	 * barf if we get one from a third party system that added them
	 */
	public void testFileWithContentTypeEntities() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("ContentTypeHasEntities.ooxml");
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
        assertTrue("Document not found in " + p.getParts(), foundDocument);
        assertTrue("Theme1 not found in " + p.getParts(), foundTheme1);
	}
	
	/**
	 * Check that we can open a file where there are valid
	 *  parameters on a content type
	 */
	public void testFileWithContentTypeParams() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("ContentTypeHasParameters.ooxml");

        OPCPackage p = OPCPackage.open(is);
        
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
                assertEquals(false, hasParameters);
                assertEquals(0, length);
            }
            // Core type doesn't have any params
            else if (part.getPartName().toString().equals("/docProps/core.xml")) {
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, contentType);
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, details.toString());
                assertEquals(false, hasParameters);
                assertEquals(0, length);
            }
            // Global Crs types do have params
            else if (part.getPartName().toString().equals("/global1dCrs.xml")) {
                assertTrue(part.getContentType().startsWith(typeResqml));
                assertEquals(typeResqml, details.toString(false));
                assertEquals(true, hasParameters);
                assertContains("version=2.0", details.toString());
                assertContains("type=obj_global1dCrs", details.toString());
                assertEquals(2, length);
                assertEquals("2.0", details.getParameter("version"));
                assertEquals("obj_global1dCrs", details.getParameter("type"));
            }
            else if (part.getPartName().toString().equals("/global2dCrs.xml")) {
                assertTrue(part.getContentType().startsWith(typeResqml));
                assertEquals(typeResqml, details.toString(false));
                assertEquals(true, hasParameters);
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
                assertEquals(true, hasParameters);
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

    private static void assertContains(String needle, String haystack) {
        assertTrue(haystack.contains(needle));
    }
}
