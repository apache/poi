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

/*
 * TestFormulaRecordAggregate.java
 *
 * Created on March 21, 2003, 12:32 AM
 */

package org.apache.poi.hssf.record.aggregates;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.StringRecord;

/**
 *
 * @author  avik
 */
public class TestFormulaRecordAggregate extends junit.framework.TestCase {
    
    /** Creates a new instance of TestFormulaRecordAggregate */
    public TestFormulaRecordAggregate(String arg) {
        super(arg);
    }
    
    public void testClone() {
        FormulaRecord f = new FormulaRecord();
        StringRecord s = new StringRecord();
        FormulaRecordAggregate fagg = new FormulaRecordAggregate(f,s);
        FormulaRecordAggregate newFagg = (FormulaRecordAggregate) fagg.clone();
        assertTrue("objects are different", fagg!=newFagg);
        assertTrue("deep clone", fagg.getFormulaRecord() != newFagg.getFormulaRecord());
        assertTrue("deep clone",  fagg.getStringRecord() != newFagg.getStringRecord());

        
    }
    
}
