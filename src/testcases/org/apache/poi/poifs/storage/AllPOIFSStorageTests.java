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

package org.apache.poi.poifs.storage;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Tests for org.apache.poi.poifs.storage<br/>
 * 
 * @author Josh Micich
 */
public final class AllPOIFSStorageTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllPOIFSStorageTests.class.getName());
		result.addTestSuite(TestBATBlock.class);
		result.addTestSuite(TestBlockAllocationTableReader.class);
		result.addTestSuite(TestBlockAllocationTableWriter.class);
		result.addTestSuite(TestBlockListImpl.class);
		result.addTestSuite(TestDocumentBlock.class);
		result.addTestSuite(TestHeaderBlockReader.class);
		result.addTestSuite(TestHeaderBlockWriter.class);
		result.addTestSuite(TestPropertyBlock.class);
		result.addTestSuite(TestRawDataBlock.class);
		result.addTestSuite(TestRawDataBlockList.class);
		result.addTestSuite(TestSmallBlockTableReader.class);
		result.addTestSuite(TestSmallBlockTableWriter.class);
		result.addTestSuite(TestSmallDocumentBlock.class);
		result.addTestSuite(TestSmallDocumentBlockList.class);
		return result;
	}
}
