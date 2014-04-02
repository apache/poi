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

package org.apache.poi.openxml4j.opc;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.openxml4j.opc.compliance.AllOpenXML4JComplianceTests;
import org.apache.poi.openxml4j.opc.internal.AllOpenXML4JInternalTests;

public final class AllOpenXML4JTests {

	public static Test suite() {

		TestSuite suite = new TestSuite(AllOpenXML4JTests.class.getName());
		suite.addTestSuite(TestContentType.class);
		suite.addTestSuite(TestFileHelper.class);
		suite.addTestSuite(TestListParts.class);
		suite.addTestSuite(TestPackage.class);
		suite.addTestSuite(TestPackageCoreProperties.class);
		suite.addTestSuite(TestPackagePartName.class);
		suite.addTestSuite(TestPackageThumbnail.class);
		suite.addTestSuite(TestPackagingURIHelper.class);
		suite.addTestSuite(TestRelationships.class);
		suite.addTest(AllOpenXML4JComplianceTests.suite());
		suite.addTest(AllOpenXML4JInternalTests.suite());
		return suite;
	}
}
