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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
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

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
		InputStream stream = new FileInputStream("test-data/slideshow/ae.ac.uaeu.faculty_nafaachbili_GeomLec1.pptx");
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
	}

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/slideshow/ae.ac.uaeu.faculty_nafaachbili_GeomLec1.pptx"));
    }
}