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

import junit.framework.TestCase;
/**
 * 
 */
public final class TestStyleRecord extends TestCase {
    public void testUnicodeReadName() {
    	byte[] data = {
   			17, 0, 9, 0, 1, 56, 94, -60, -119, 95, 0, 83, 0, 104, 0, 101, 0, 101, 0, 116, 0, 49, 0, 92, 40, //92, 36    			
    	};
    	RecordInputStream in = TestcaseRecordInputStream.create(StyleRecord.sid, data);
    	StyleRecord sr = new StyleRecord(in);
    	assertEquals("\u5E38\u89C4_Sheet1", sr.getName()); // "<Conventional>_Sheet1"
    }
}
