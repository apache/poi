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