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
