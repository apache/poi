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

package org.apache.poi.xwpf;

import junit.framework.TestCase;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTDigSigBlob;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTVectorLpstr;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTVectorVariant;

/**
 * Tests if the {@link CoreProperties#getKeywords()} method. This test has been
 * submitted because even though the
 * {@link PackageProperties#getKeywordsProperty()} had been present before, the
 * {@link CoreProperties#getKeywords()} had been missing.
 * 
 * The author of this has added {@link CoreProperties#getKeywords()} and
 * {@link CoreProperties#setKeywords(String)} and this test is supposed to test
 * them.
 * 
 * @author Antoni Mylka
 * 
 */
public final class TestAllExtendedProperties extends TestCase {
	public void testGetAllExtendedProperties() {
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestPoiXMLDocumentCorePropertiesGetKeywords.docx");
		CTProperties ctProps = doc.getProperties().getExtendedProperties().getUnderlyingProperties();
		assertEquals("Microsoft Office Word",ctProps.getApplication());
		assertEquals("14.0000",ctProps.getAppVersion());
		assertEquals(57,ctProps.getCharacters());
		assertEquals(66,ctProps.getCharactersWithSpaces());
		assertEquals("",ctProps.getCompany());
		assertNull(ctProps.getDigSig());
		assertEquals(0,ctProps.getDocSecurity());
		assertNotNull(ctProps.getDomNode());
		
		CTVectorVariant vec = ctProps.getHeadingPairs();
		assertEquals(2,vec.getVector().sizeOfVariantArray());
		assertEquals("Title",vec.getVector().getVariantArray(0).getLpstr());
		assertEquals(1,vec.getVector().getVariantArray(1).getI4());
		
		assertFalse(ctProps.isSetHiddenSlides());
		assertEquals(0,ctProps.getHiddenSlides());
		assertFalse(ctProps.isSetHLinks());
		assertNull(ctProps.getHLinks());
		assertNull(ctProps.getHyperlinkBase());
		assertTrue(ctProps.isSetHyperlinksChanged());
		assertFalse(ctProps.getHyperlinksChanged());
		assertEquals(1,ctProps.getLines());
		assertTrue(ctProps.isSetLinksUpToDate());
		assertFalse(ctProps.getLinksUpToDate());
		assertNull(ctProps.getManager());
		assertFalse(ctProps.isSetMMClips());
		assertEquals(0,ctProps.getMMClips());
		assertFalse(ctProps.isSetNotes());
		assertEquals(0,ctProps.getNotes());
		assertEquals(1,ctProps.getPages());
		assertEquals(1,ctProps.getParagraphs());
		assertNull(ctProps.getPresentationFormat());
		assertTrue(ctProps.isSetScaleCrop());
		assertFalse(ctProps.getScaleCrop());
		assertTrue(ctProps.isSetSharedDoc());
		assertFalse(ctProps.getSharedDoc());
		assertFalse(ctProps.isSetSlides());
		assertEquals(0,ctProps.getSlides());
		assertEquals("Normal.dotm",ctProps.getTemplate());
		
		CTVectorLpstr vec2 = ctProps.getTitlesOfParts();
		assertEquals(1,vec2.getVector().sizeOfLpstrArray());
		assertEquals("Example Word 2010 Document",vec2.getVector().getLpstrArray(0));
		
		assertEquals(3,ctProps.getTotalTime());
		assertEquals(10,ctProps.getWords());
		
		// Check the digital signature part
		// Won't be there in this file, but we
		//  need to do this check so that the
		//  appropriate parts end up in the
		//  smaller ooxml schemas file
		CTDigSigBlob blob = ctProps.getDigSig();
		assertNull(blob);
		
		blob = CTDigSigBlob.Factory.newInstance();
		blob.setBlob(new byte [] {2,6,7,2,3,4,5,1,2,3});
	}
}
