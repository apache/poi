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

package org.apache.poi.hssf.record.formula;

import java.util.Arrays;

import junit.framework.AssertionFailedError;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianInput;

/**
 * Tests for {@link AttrPtg}.
 * 
 * @author Josh Micich
 */
public final class TestAttrPtg extends AbstractPtgTestCase {

	/**
	 * Fix for bug visible around svn r706772.
	 */
	public void testReserializeAttrChoose() {
		byte[] data = HexRead.readFromString("19, 04, 03, 00, 08, 00, 11, 00, 1A, 00, 23, 00");
		LittleEndianInput in = TestcaseRecordInputStream.createLittleEndian(data);
		Ptg[] ptgs = Ptg.readTokens(data.length, in);
		byte[] data2 = new byte[data.length];
		try {
			Ptg.serializePtgs(ptgs, data2, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new AssertionFailedError("incorrect re-serialization of tAttrChoose");
		}
		assertTrue(Arrays.equals(data, data2));
	}
}
