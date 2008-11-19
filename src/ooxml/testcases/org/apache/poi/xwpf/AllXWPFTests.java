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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.xwpf.extractor.TestXWPFWordExtractor;
import org.apache.poi.xwpf.model.TestXWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.TestXWPFParagraph;
import org.apache.poi.xwpf.usermodel.TestXWPFRun;

/**
 * Collects all tests for <tt>org.apache.poi.xwpf</tt> and sub-packages.
 * 
 * @author Josh Micich
 */
public final class AllXWPFTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllXWPFTests.class.getName());
		result.addTestSuite(TestXWPFDocument.class);
		result.addTestSuite(TestXWPFHeaderFooterPolicy.class);
		result.addTestSuite(TestXWPFParagraph.class);
		result.addTestSuite(TestXWPFRun.class);
		result.addTestSuite(TestXWPFWordExtractor.class);
		return result;
	}
}
