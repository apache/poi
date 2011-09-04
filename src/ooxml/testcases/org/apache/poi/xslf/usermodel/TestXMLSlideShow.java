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
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;

public class TestXMLSlideShow extends TestCase {
   private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
   private OPCPackage pack;

   protected void setUp() throws Exception {
      pack = OPCPackage.open(slTests.openResourceAsStream("sample.pptx"));
   }

   public void testContainsMainContentType() throws Exception {
      boolean found = false;
      for(PackagePart part : pack.getParts()) {
         if(part.getContentType().equals(XSLFRelation.MAIN.getContentType())) {
            found = true;
         }
         //System.out.println(part);
      }
      assertTrue(found);
   }

   public void testOpen() throws Exception {
      XMLSlideShow xml;

      // With the finalised uri, should be fine
      xml = new XMLSlideShow(pack);
      // Check the core
      assertNotNull(xml.getCTPresentation());

      // Check it has some slides
      assertNotNull(xml.getSlides().length);
      assertTrue(xml.getSlides().length > 0);

      assertNotNull(xml.getSlideMasters().length);
      assertTrue(xml.getSlideMasters().length > 0);
   }

   public void testSlideBasics() throws Exception {
      XMLSlideShow xml = new XMLSlideShow(pack);

      // Should have 1 master
      assertEquals(1, xml.getSlideMasters().length);

      // Should have two sheets
      assertEquals(2, xml.getSlides().length);

      // Check they're as expected
      CTSlideIdListEntry[] slides = new CTSlideIdListEntry[
         xml.getCTPresentation().getSldIdLst().getSldIdList().size()];
      xml.getCTPresentation().getSldIdLst().getSldIdList().toArray(slides);

      assertEquals(256, slides[0].getId());
      assertEquals(257, slides[1].getId());
      assertEquals("rId2", slides[0].getId2());
      assertEquals("rId3", slides[1].getId2());

      // Now get those objects
      assertNotNull(xml.getSlides()[0]);
      assertNotNull(xml.getSlides()[1]);

      // And check they have notes as expected
      assertNotNull(xml.getSlides()[0].getNotes());
      assertNotNull(xml.getSlides()[1].getNotes());

      // Next up look for the slide master
      CTSlideMasterIdListEntry[] masters = new CTSlideMasterIdListEntry[
         xml.getCTPresentation().getSldMasterIdLst().getSldMasterIdList().size()];
      xml.getCTPresentation().getSldMasterIdLst().getSldMasterIdList().toArray(masters);

      assertEquals(2147483648l, masters[0].getId());
      assertEquals("rId1", masters[0].getId2());
      assertNotNull(xml.getSlideMasters()[0]);

      // Finally look for the notes master
      CTNotesMasterIdListEntry notesMaster =
         xml.getCTPresentation().getNotesMasterIdLst().getNotesMasterId();
      assertNotNull(notesMaster);

      assertNotNull(xml.getNotesMaster());
   }
	
   public void testMetadataBasics() throws Exception {
      XMLSlideShow xml = new XMLSlideShow(pack);

      assertNotNull(xml.getProperties().getCoreProperties());
      assertNotNull(xml.getProperties().getExtendedProperties());

      assertEquals("Microsoft Office PowerPoint", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

      assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
      assertEquals(null, xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
   }
}
