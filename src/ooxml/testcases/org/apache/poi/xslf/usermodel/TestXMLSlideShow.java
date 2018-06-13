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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.BaseTestSlideShow;
import org.apache.poi.sl.usermodel.SlideShow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;

public class TestXMLSlideShow extends BaseTestSlideShow {
   private OPCPackage pack;
   
   @Override
   public XMLSlideShow createSlideShow() {
       return new XMLSlideShow();
   }

   @Before
   public void setUp() throws Exception {
      pack = OPCPackage.open(slTests.openResourceAsStream("sample.pptx"));
   }
   
   @After
   public void tearDown() {
       pack.revert();
   }

   @Test
   public void testContainsMainContentType() throws Exception {
      boolean found = false;
      for(PackagePart part : pack.getParts()) {
         if(part.getContentType().equals(XSLFRelation.MAIN.getContentType())) {
            found = true;
         }
      }
      assertTrue(found);
   }

   @Test
   public void testOpen() throws Exception {
      // With the finalised uri, should be fine
       XMLSlideShow xml = new XMLSlideShow(pack);
      // Check the core
      assertNotNull(xml.getCTPresentation());

      // Check it has some slides
      assertFalse(xml.getSlides().isEmpty());
      assertFalse(xml.getSlideMasters().isEmpty());
      
      xml.close();
   }

   @Test
   public void testSlideBasics() throws IOException {
      XMLSlideShow xml = new XMLSlideShow(pack);

      // Should have 1 master
      assertEquals(1, xml.getSlideMasters().size());

      // Should have two sheets
      assertEquals(2, xml.getSlides().size());

      // Check they're as expected
      CTSlideIdListEntry[] slides = xml.getCTPresentation().getSldIdLst().getSldIdArray();

      assertEquals(256, slides[0].getId());
      assertEquals(257, slides[1].getId());
      assertEquals("rId2", slides[0].getId2());
      assertEquals("rId3", slides[1].getId2());

      // Now get those objects
      assertNotNull(xml.getSlides().get(0));
      assertNotNull(xml.getSlides().get(1));

      // And check they have notes as expected
      assertNotNull(xml.getSlides().get(0).getNotes());
      assertNotNull(xml.getSlides().get(1).getNotes());

      // Next up look for the slide master
      CTSlideMasterIdListEntry[] masters = xml.getCTPresentation().getSldMasterIdLst().getSldMasterIdArray();

      // see SlideAtom.USES_MASTER_SLIDE_ID
      assertEquals(0x80000000L, masters[0].getId());
      assertEquals("rId1", masters[0].getId2());
      assertNotNull(xml.getSlideMasters().get(0));

      // Finally look for the notes master
      CTNotesMasterIdListEntry notesMaster =
         xml.getCTPresentation().getNotesMasterIdLst().getNotesMasterId();
      assertNotNull(notesMaster);

      assertNotNull(xml.getNotesMaster());
      
      xml.close();
   }
	
   @Test
   public void testMetadataBasics() throws IOException {
      XMLSlideShow xml = new XMLSlideShow(pack);

      assertNotNull(xml.getProperties().getCoreProperties());
      assertNotNull(xml.getProperties().getExtendedProperties());

      assertEquals("Microsoft Office PowerPoint", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

      assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
      assertFalse(xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().isPresent());
      
      xml.close();
   }
   
   @Test
   public void testComments() throws Exception {
      // Default sample file has none
      XMLSlideShow xml = new XMLSlideShow(pack);
      
      assertEquals(null, xml.getCommentAuthors());
      
      for (XSLFSlide slide : xml.getSlides()) {
         assertTrue(slide.getComments().isEmpty());
      }
      
      // Try another with comments
      XMLSlideShow xmlComments = new XMLSlideShow(slTests.openResourceAsStream("45545_Comment.pptx"));
      
      // Has one author
      assertNotNull(xmlComments.getCommentAuthors());
      assertEquals(1, xmlComments.getCommentAuthors().getCTCommentAuthorsList().sizeOfCmAuthorArray());
      assertEquals("XPVMWARE01", xmlComments.getCommentAuthors().getAuthorById(0).getName());
      
      // First two slides have comments
      int i = -1;
      for (XSLFSlide slide : xmlComments.getSlides()) {
         i++;
         
         if(i == 0) {
            assertNotNull(slide.getCommentsPart());
            assertEquals(1, slide.getCommentsPart().getNumberOfComments());
            assertEquals("testdoc", slide.getCommentsPart().getCommentAt(0).getText());
            assertEquals(0, slide.getCommentsPart().getCommentAt(0).getAuthorId());
         } else if (i == 1) {
            assertNotNull(slide.getComments());
            assertEquals(1, slide.getCommentsPart().getNumberOfComments());
            assertEquals("test phrase", slide.getCommentsPart().getCommentAt(0).getText());
            assertEquals(0, slide.getCommentsPart().getCommentAt(0).getAuthorId());
         } else {
            assertNull(slide.getCommentsPart());
            assertTrue(slide.getComments().isEmpty());
         }
      }
      
      xmlComments.close();
      xml.close();
   }
   
   public SlideShow<?, ?> reopen(SlideShow<?, ?> show) {
       return reopen((XMLSlideShow)show);
   }

   private static XMLSlideShow reopen(XMLSlideShow show) {
       try {
           BufAccessBAOS bos = new BufAccessBAOS();
           show.write(bos);
           return new XMLSlideShow(new ByteArrayInputStream(bos.getBuf()));
       } catch (IOException e) {
           fail(e.getMessage());
           return null;
       }
   }

   private static class BufAccessBAOS extends ByteArrayOutputStream {
       byte[] getBuf() {
           return buf;
       }
   }
}
