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

import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

import junit.framework.TestCase;

public final class TestPackagePartName extends TestCase {

	/**
	 * Test method getExtension().
	 */
	public void testGetExtension() throws Exception{
		PackagePartName name1 = PackagingURIHelper.createPartName("/doc/props/document.xml");
		PackagePartName name2 = PackagingURIHelper.createPartName("/root/document");
		assertEquals("xml", name1.getExtension());
		assertEquals("", name2.getExtension());
	}
}
