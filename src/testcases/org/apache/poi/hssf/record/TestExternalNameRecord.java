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

package org.apache.poi.hssf.record;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 * 
 * @author Josh Micich
 */
public final class TestExternalNameRecord extends TestCase {

	private static final byte[] dataFDS = {
		0, 0, 0, 0, 0, 0, 3, 0, 70, 68, 83, 0, 0,
	};
	private static ExternalNameRecord createSimpleENR() {
		return new ExternalNameRecord(new TestcaseRecordInputStream((short)0x0023, dataFDS));
	}
	public void testBasicDeserializeReserialize() {
		
		ExternalNameRecord enr = createSimpleENR();
		assertEquals( "FDS", enr.getText());
	 
		try {
			TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataFDS, enr.serialize());
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage().equals("15")) {
				throw new AssertionFailedError("Identified bug 44695");
			}
		}
	}

	public void testBasicSize() {
		ExternalNameRecord enr = createSimpleENR();
		if(enr.getRecordSize() == 13) {
			throw new AssertionFailedError("Identified bug 44695");
		}
		assertEquals(17, enr.getRecordSize());
	}
}
