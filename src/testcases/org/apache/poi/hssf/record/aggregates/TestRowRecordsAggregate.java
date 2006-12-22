
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
        
package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.*;

public class TestRowRecordsAggregate extends junit.framework.TestCase {
    public TestRowRecordsAggregate(String name) {
        super (name);
    }
    
    public void testRowGet() {
        RowRecordsAggregate rra = new RowRecordsAggregate();
        RowRecord rr = new RowRecord();
        rr.setRowNumber(( short ) 4);
        rra.insertRow(rr);
        RowRecord rr2 = new RowRecord(); rr2.setRowNumber((short) 1);
        rra.insertRow(rr2);
        
        RowRecord rr1 = rra.getRow(4);
        
        assertTrue("Row Record should not be null", rr1!=null);
        assertTrue("Row number is 1",rr1.getRowNumber() == 4);
        assertTrue("Row record retrieved is identical ", rr1 == rr);
    }
    
     public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.record.aggregates.RowRecordAggregate");
        junit.textui.TestRunner.run(TestRowRecordsAggregate.class);
    }
}
