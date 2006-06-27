
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        


package org.apache.poi.hslf.usermodel;


import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;

/**
 * Tests that SlideShow returns Sheets in the right order
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestSlideOrdering extends TestCase {
	// Simple slideshow, record order matches slide order
	private SlideShow ssA;
	// Complex slideshow, record order doesn't match slide order
	private SlideShow ssB;

    public TestSlideOrdering() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		
		String filenameA = dirname + "/basic_test_ppt_file.ppt";
		HSLFSlideShow hssA = new HSLFSlideShow(filenameA);
		ssA = new SlideShow(hssA);
		
		String filenameB = dirname + "/incorrect_slide_order.ppt";
		HSLFSlideShow hssB = new HSLFSlideShow(filenameB);
		ssB = new SlideShow(hssB);
    }

    /**
     * Test the simple case - record order matches slide order
     */
    public void testSimpleCase() throws Exception {
    	assertEquals(2, ssA.getSlides().length);
    	
    	Slide s1 = ssA.getSlides()[0];
    	Slide s2 = ssA.getSlides()[1];
    	
    	String[] firstTRs = new String[] {
    			"This is a test title",
    			"This is the title on page 2"
    	};
    	
    	assertEquals(firstTRs[0], s1.getTextRuns()[0].getText());
    	assertEquals(firstTRs[1], s2.getTextRuns()[0].getText());
    }

    /**
     * Test the complex case - record order differs from slide order
     */
    public void testComplexCase() throws Exception {
    	assertEquals(3, ssB.getSlides().length);
    	
    	Slide s1 = ssB.getSlides()[0];
    	Slide s2 = ssB.getSlides()[1];
    	Slide s3 = ssB.getSlides()[2];
    	
    	String[] firstTRs = new String[] {
    			"Slide 1",
    			"Slide 2",
    			"Slide 3"
    	};
    	
    	assertEquals(firstTRs[0], s1.getTextRuns()[0].getText());
    	assertEquals(firstTRs[1], s2.getTextRuns()[0].getText());
    	assertEquals(firstTRs[2], s3.getTextRuns()[0].getText());
    }
}
