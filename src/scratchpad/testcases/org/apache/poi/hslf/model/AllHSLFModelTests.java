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

package org.apache.poi.hslf.model;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests from the package <tt>org.apache.poi.hslf.model</tt>.
 * 
 * @author Josh Micich
 */
public class AllHSLFModelTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite(AllHSLFModelTests.class.getName());
		result.addTestSuite(TestBackground.class);
		result.addTestSuite(TestFreeform.class);
		result.addTestSuite(TestHeadersFooters.class);
		result.addTestSuite(TestHyperlink.class);
		result.addTestSuite(TestImagePainter.class);
		result.addTestSuite(TestLine.class);
		result.addTestSuite(TestMovieShape.class);
		result.addTestSuite(TestOleEmbedding.class);
		result.addTestSuite(TestPPFont.class);
		result.addTestSuite(TestPPGraphics2D.class);
		result.addTestSuite(TestPicture.class);
		result.addTestSuite(TestSetBoldItalic.class);
		result.addTestSuite(TestShapes.class);
		result.addTestSuite(TestSheet.class);
		result.addTestSuite(TestSlideChangeNotes.class);
		result.addTestSuite(TestSlideMaster.class);
		result.addTestSuite(TestSlides.class);
		result.addTestSuite(TestTable.class);
		result.addTestSuite(TestTextRun.class);
		result.addTestSuite(TestTextRunReWrite.class);
		result.addTestSuite(TestTextShape.class);
		return result;
	}
}
