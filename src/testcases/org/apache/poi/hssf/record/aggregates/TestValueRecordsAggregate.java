
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;

public class TestValueRecordsAggregate extends junit.framework.TestCase {
    public TestValueRecordsAggregate(String name) {
        super (name);
    }
    
    /**
     * Make sure the shared formula makes it to the FormulaRecordAggregate when being parsed
     * as part of the value records
     *
     */
    public void testSharedFormula() {
			List records = new ArrayList();
			records.add(new FormulaRecord());
			records.add(new SharedFormulaRecord());
			
			ValueRecordsAggregate valueRecord = new ValueRecordsAggregate();
			valueRecord.construct(0, records); 
			Iterator iterator = valueRecord.getIterator();			
			Record record = (Record)iterator.next();
			assertNotNull("Row contains a value", record);
			assertTrue("First record is a FormulaRecordsAggregate", (record instanceof FormulaRecordAggregate));
			FormulaRecordAggregate aggregate = (FormulaRecordAggregate)record;
			assertNotNull("SharedFormulaRecord is null", aggregate.getSharedFormulaRecord());
				
    }
    
     public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.record.aggregates.TestValueRecordAggregate");
        junit.textui.TestRunner.run(TestValueRecordsAggregate.class);
    }
}
