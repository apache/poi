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

package org.apache.poi.xwpf.usermodel;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;

public class TestXWPFStyles extends TestCase {

//	protected void setUp() throws Exception {
//		super.setUp();
//	}
	
	public void testGetUsedStyles(){
		XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("Styles.docx");
		List<XWPFStyle> testUsedStyleList = new ArrayList<XWPFStyle>();
		XWPFStyles styles = sampleDoc.getStyles();
		XWPFStyle style = styles.getStyle("berschrift1");
		testUsedStyleList.add(style);
		testUsedStyleList.add(styles.getStyle("Standard"));
		testUsedStyleList.add(styles.getStyle("berschrift1Zchn"));
		testUsedStyleList.add(styles.getStyle("Absatz-Standardschriftart"));
		style.hasSameName(style);
		
		List<XWPFStyle> usedStyleList = styles.getUsedStyleList(style);
		assertEquals(usedStyleList, testUsedStyleList);
		
		
	}

//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}

}
