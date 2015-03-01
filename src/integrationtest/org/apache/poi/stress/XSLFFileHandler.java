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
package org.apache.poi.stress;

import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.junit.Test;

public class XSLFFileHandler extends AbstractFileHandler {
	@Override
    public void handleFile(InputStream stream) throws Exception {
        XSLFSlideShow slide = new XSLFSlideShow(OPCPackage.open(stream));
		assertNotNull(slide.getPresentation());
		assertNotNull(slide.getSlideMasterReferences());
		assertNotNull(slide.getSlideReferences());
		
		new POIXMLDocumentHandler().handlePOIXMLDocument(slide);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
		    slide.write(out);
		} finally {
		    out.close();
		}
		
        createBitmaps(out);		
	}

    private void createBitmaps(ByteArrayOutputStream out) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(out.toByteArray()));
        Dimension pgsize = ppt.getPageSize();
        XSLFSlide[] xmlSlide = ppt.getSlides();
        int slideSize = xmlSlide.length;
        for (int i = 0; i < slideSize; i++) {
//            System.out.println("slide-" + (i + 1));
//            System.out.println("" + xmlSlide[i].getTitle());

            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();

            // draw stuff
            xmlSlide[i].draw(graphics);

            // Also try to read notes
            XSLFNotes notes = xmlSlide[i].getNotes();
            if(notes != null) {
                for (XSLFShape note : notes) {
                    note.draw(graphics);
                    
                    if (note instanceof XSLFTextShape) {
                        XSLFTextShape txShape = (XSLFTextShape) note;
                        for (XSLFTextParagraph xslfParagraph : txShape.getTextParagraphs()) {
                            xslfParagraph.getText();
                        }
                    }
                }
            }
        }
    }

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
		InputStream stream = new FileInputStream("test-data/slideshow/pptx2svg.pptx");
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
	}


    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/slideshow/testPPT.thmx"));
    }
}