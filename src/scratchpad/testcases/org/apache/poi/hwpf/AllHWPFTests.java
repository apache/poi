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

package org.apache.poi.hwpf;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hwpf.model.*;

public final class AllHWPFTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllHWPFTests.class.getName());
		suite.addTestSuite(TestCHPBinTable.class);
		suite.addTestSuite(TestDocumentProperties.class);
		suite.addTestSuite(TestFileInformationBlock.class);
		suite.addTestSuite(TestFontTable.class);
		suite.addTestSuite(TestPAPBinTable.class);
		suite.addTestSuite(TestPlexOfCps.class);
		suite.addTestSuite(TestSectionTable.class);
		suite.addTestSuite(TestStyleSheet.class);
		suite.addTestSuite(TestTextPieceTable.class);
		suite.addTestSuite(TestListTables.class);
		return suite;
	}
}
