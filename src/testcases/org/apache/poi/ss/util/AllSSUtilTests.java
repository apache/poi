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

package org.apache.poi.ss.util;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Test suite for <tt>org.apache.poi.ss.util</tt>
 *
 * @author Josh Micich
 */
public final class AllSSUtilTests {
	public static Test suite() {
		TestSuite result = new TestSuite(AllSSUtilTests.class.getName());
		result.addTestSuite(TestCellRangeAddress.class);
		result.addTestSuite(TestCellReference.class);
		result.addTestSuite(TestExpandedDouble.class);
		result.addTestSuite(TestNumberComparer.class);
		result.addTestSuite(TestNumberToTextConverter.class);
		result.addTestSuite(TestRegion.class);
		return result;
	}
}
