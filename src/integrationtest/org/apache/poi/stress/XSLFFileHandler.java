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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.junit.Test;

public class XSLFFileHandler extends SlideShowHandler {
	@Override
    public void handleFile(InputStream stream) throws Exception {
	    XMLSlideShow slide = new XMLSlideShow(stream);
	    XSLFSlideShow slideInner = new XSLFSlideShow(slide.getPackage());
		assertNotNull(slideInner.getPresentation());
		assertNotNull(slideInner.getSlideMasterReferences());
		assertNotNull(slideInner.getSlideReferences());
		
		new POIXMLDocumentHandler().handlePOIXMLDocument(slide);

		handleSlideShow(slide);
		
		slideInner.close();
		slide.close();
	}

	@Override
    public void handleExtracting(File file) throws Exception {
        super.handleExtracting(file);
        
        
        // additionally try the other getText() methods
        
        XSLFPowerPointExtractor extractor = (XSLFPowerPointExtractor) ExtractorFactory.createExtractor(file);
        try  {
            assertNotNull(extractor);

            assertNotNull(extractor.getText(true, true, true));
            assertEquals("With all options disabled we should not get text", 
                    "", extractor.getText(false, false, false));
        } finally {
            extractor.close();
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
	@Override
    @Test
	public void test() throws Exception {
		File file = new File("test-data/slideshow/ae.ac.uaeu.faculty_nafaachbili_GeomLec1.pptx");
		InputStream stream = new FileInputStream(file);
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}

        handleExtracting(file);
   }
}
