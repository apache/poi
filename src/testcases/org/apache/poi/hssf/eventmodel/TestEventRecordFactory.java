package org.apache.poi.hssf.eventmodel;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.util.Iterator;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;

import junit.framework.TestCase;

/**
 * enclosing_type describe the purpose here
 * 
 * @author Andrew C. Oliver acoliver@apache.org
 */
public class TestEventRecordFactory extends TestCase
{
    boolean wascalled;

    private EventRecordFactory factory;
    /**
     * Constructor for TestEventRecordFactory.
     * @param arg0
     */
    public TestEventRecordFactory(String arg0)
    {
        super(arg0);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestEventRecordFactory.class);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        factory = new EventRecordFactory();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * tests that a listener can be registered and that once
     * registered can be returned as expected.
     */
    public void testRegisterListener()
    {        
        factory.registerListener(new ERFListener() {
            public boolean processRecord(Record rec) {
              return true;   
            }
        },null);
        
        Iterator i = factory.listeners();
        assertTrue("iterator must have one",i.hasNext());
       
        factory.registerListener(new ERFListener() {
            public boolean processRecord(Record rec) {
              return true;   
            }
        },null);
        
        i = factory.listeners();
        
        i.next();      
        assertTrue("iterator must have two",i.hasNext());         
        factory = new EventRecordFactory();
    }

    /**
     * tests that the records can be processed and properly return 
     * values.
     */
    public void testProcessRecords()
    {
        byte[] bytes = null;
        int offset = 0;
        //boolean wascalled = false;
        factory.registerListener(new ERFListener() {
            public boolean processRecord(Record rec) {
                wascalled = true;
                assertTrue("must be BOFRecord got SID="+rec.getSid(),
                           (rec.getSid() == BOFRecord.sid));                  
                return true;              
            }
        }, new short[] {BOFRecord.sid});
        
        BOFRecord bof = new BOFRecord();
        bof.setBuild((short)0);
        bof.setBuildYear((short)1999);
        bof.setRequiredVersion(123);
        bof.setType(BOFRecord.TYPE_WORKBOOK);
        bof.setVersion((short)0x06);
        bof.setHistoryBitMask(BOFRecord.HISTORY_MASK);
        
        EOFRecord eof = new EOFRecord();
        bytes = new byte[bof.getRecordSize() + eof.getRecordSize()];
        offset = bof.serialize(offset,bytes);
        offset = eof.serialize(offset,bytes);
                
        factory.processRecords(new ByteArrayInputStream(bytes));    
        assertTrue("The record listener must be called",wascalled);    
    }

    /**
     * tests that the create record function returns a properly 
     * constructed record in the simple case.
     */
    public void testCreateRecord()
    {
        byte[] bytes = null;
        byte[] nbytes = null;
        Record[] records = null;
        BOFRecord bof = new BOFRecord();
        bof.setBuild((short)0);
        bof.setBuildYear((short)1999);
        bof.setRequiredVersion(123);
        bof.setType(BOFRecord.TYPE_WORKBOOK);
        bof.setVersion((short)0x06);
        bof.setHistoryBitMask(BOFRecord.HISTORY_MASK);
        
        bytes = bof.serialize();
        nbytes = new byte[bytes.length - 4];
        System.arraycopy(bytes,4,nbytes,0,nbytes.length);
            
        records = factory.createRecord(bof.getSid(),(short)nbytes.length,nbytes);
        
        assertTrue("record.length must be 1, was ="+records.length,records.length == 1);
        assertTrue("record is the same", compareRec(bof,records[0]));
        
    }

    /**
     * Compare the serialized bytes of two records are equal
     * @param first the first record to compare
     * @param second the second record to compare
     * @return boolean whether or not the record where equal
     */
    private boolean compareRec(Record first, Record second)
    {
        boolean retval = true;
        byte[] rec1 = first.serialize();
        byte[] rec2 = second.serialize();
        
        if (rec1.length == rec2.length) {
            for (int k=0; k<rec1.length; k++) {
             if (rec1[k] != rec2[k]) {
              retval = false;
              break;  
             }   
            }
        } else {
            retval = false;   
        }
        
        return retval;
    }

    
    /**
     * tests that the create record function returns a properly 
     * constructed record in the case of a continued record.
     * @todo - need a real world example to put in a unit test
     */
    public void testCreateContinuedRecord()
    {
      //  fail("not implemented");
    }
    
}
