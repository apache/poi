/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;

/**
 * Based on AbstractEscherRecordHolder fomr HSSF.
 * 
 * @author Squeeself
 */
public class EscherRecordHolder 
{
    protected ArrayList escherRecords = new ArrayList();
    
    public EscherRecordHolder()
    {
        
    }
    
    public EscherRecordHolder(byte[] data, int offset, int size)
    {
        fillEscherRecords(data, offset, size);
    }
    
    private void fillEscherRecords(byte[] data, int offset, int size)
    {
        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
        int pos = offset;
        while ( pos < offset + size)
        {
            EscherRecord r = recordFactory.createRecord(data, pos);
            escherRecords.add(r);
            int bytesRead = r.fillFields(data, pos, recordFactory);
            pos += bytesRead + 1; // There is an empty byte between each top-level record in a Word doc
        }
    }
    
    public List getEscherRecords()
    {
        return escherRecords;
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        final String nl = System.getProperty("line.separator");
        if (escherRecords.size() == 0)
            buffer.append("No Escher Records Decoded" + nl);
        for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            buffer.append(r.toString());
        }

        return buffer.toString();
    }
    
    /**
     * If we have a EscherContainerRecord as one of our
     *  children (and most top level escher holders do),
     *  then return that.
     */
    public EscherContainerRecord getEscherContainer() {
    	for(Iterator it = escherRecords.iterator(); it.hasNext();) {
    		Object er = it.next();
    		if(er instanceof EscherContainerRecord) {
    			return (EscherContainerRecord)er;
    		}
    	}
    	return null;
    }

    /**
     * Descends into all our children, returning the
     *  first EscherRecord with the given id, or null
     *  if none found
     */
    public EscherRecord findFirstWithId(short id) {
    	return findFirstWithId(id, getEscherRecords());
    }
    private EscherRecord findFirstWithId(short id, List records) {
    	// Check at our level
    	for(Iterator it = records.iterator(); it.hasNext();) {
    		EscherRecord r = (EscherRecord)it.next();
    		if(r.getRecordId() == id) {
    			return r;
    		}
    	}
    	
    	// Then check our children in turn
    	for(Iterator it = records.iterator(); it.hasNext();) {
    		EscherRecord r = (EscherRecord)it.next();
    		if(r.isContainerRecord()) {
    			EscherRecord found =
    				findFirstWithId(id, r.getChildRecords());
    			if(found != null) {
    				return found;
    			}
    		}
    	}
    	
    	// Not found in this lot
    	return null;
    }
}
