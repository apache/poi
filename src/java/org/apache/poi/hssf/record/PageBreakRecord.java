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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.LittleEndian;

/**
 * <p>Record that contains the functionality page breaks (horizontal and vertical)</p>
 * 
 * <p>The other two classes just specifically set the SIDS for record creation.</p>
 * 
 * <p>REFERENCE:  Microsoft Excel SDK page 322 and 420</p>
 * 
 * @see HorizontalPageBreakRecord
 * @see VerticalPageBreakRecord
 * @author Danny Mui (dmui at apache dot org)
 */
public abstract class PageBreakRecord extends Record {
	private static final boolean IS_EMPTY_RECORD_WRITTEN = false; //TODO - flip
    private static final int[] EMPTY_INT_ARRAY = { };
   
    private List _breaks;
    private Map _breakMap;
      
    /**
     * Since both records store 2byte integers (short), no point in 
     * differentiating it in the records.
     * <p>
     * The subs (rows or columns, don't seem to be able to set but excel sets
     * them automatically)
     */
    public class Break {

        public static final int ENCODED_SIZE = 6;
		public int main;
        public int subFrom;
        public int subTo;

        public Break(int main, int subFrom, int subTo)
        {
            this.main = main;
            this.subFrom = subFrom;
            this.subTo = subTo;
        }
        
        public Break(RecordInputStream in) {
        	main = in.readUShort() - 1;
        	subFrom = in.readUShort();
        	subTo = in.readUShort();
        }
        
        public int serialize(int offset, byte[] data) {
            LittleEndian.putUShort(data, offset + 0, main + 1);
            LittleEndian.putUShort(data, offset + 2, subFrom);
            LittleEndian.putUShort(data, offset + 4, subTo);
            return ENCODED_SIZE;
        }
    }

    protected PageBreakRecord() {
        _breaks = new ArrayList();
        _breakMap = new HashMap();
    }

    protected PageBreakRecord(RecordInputStream in) {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        int nBreaks = in.readShort();
        _breaks = new ArrayList(nBreaks + 2);
        _breakMap = new HashMap();
        
        for(int k = 0; k < nBreaks; k++) {
        	Break br = new Break(in);
			_breaks.add(br);
            _breakMap.put(new Integer(br.main), br);
        }

    }
    
    private int getDataSize() {
    	return 2 + _breaks.size() * Break.ENCODED_SIZE;
    }
    public int getRecordSize() {
        int nBreaks = _breaks.size();
        if (!IS_EMPTY_RECORD_WRITTEN && nBreaks < 1) {
        	return 0;
        }
        return 4 + getDataSize();
    }


    public final int serialize(int offset, byte data[]) {
        int nBreaks = _breaks.size();
        if (!IS_EMPTY_RECORD_WRITTEN && nBreaks < 1) {
        	return 0;
        }
    	int dataSize = getDataSize();
        LittleEndian.putUShort(data, offset + 0, getSid());
        LittleEndian.putUShort(data, offset + 2, dataSize);
        LittleEndian.putUShort(data, offset + 4, nBreaks);
        int pos = 6;
        for (int i=0; i<nBreaks; i++) {
            Break br = (Break)_breaks.get(i);
            pos += br.serialize(offset+pos, data);
        }

        return 4 + dataSize;
    }

    public int getNumBreaks() {
        return _breaks.size();
    }

    public final Iterator getBreaksIterator() {
        return _breaks.iterator();
    }

    public String toString()
    {
        StringBuffer retval = new StringBuffer();
        
       
        String label;
        String mainLabel;
        String subLabel;
        
        if (getSid() == HorizontalPageBreakRecord.sid) {
           label = "HORIZONTALPAGEBREAK";
           mainLabel = "row";
           subLabel = "col";
        } else {
           label = "VERTICALPAGEBREAK";
           mainLabel = "column";
           subLabel = "row";
        }
        
        retval.append("["+label+"]").append("\n");
        retval.append("     .sid        =").append(getSid()).append("\n");
        retval.append("     .numbreaks =").append(getNumBreaks()).append("\n");
        Iterator iterator = getBreaksIterator();
        for(int k = 0; k < getNumBreaks(); k++)
        {
            Break region = (Break)iterator.next();
            
            retval.append("     .").append(mainLabel).append(" (zero-based) =").append(region.main).append("\n");
            retval.append("     .").append(subLabel).append("From    =").append(region.subFrom).append("\n");
            retval.append("     .").append(subLabel).append("To      =").append(region.subTo).append("\n");
        }

        retval.append("["+label+"]").append("\n");
        return retval.toString();
    }

   /**
    * Adds the page break at the specified parameters
    * @param main Depending on sid, will determine row or column to put page break (zero-based)
    * @param subFrom No user-interface to set (defaults to minimum, 0)
    * @param subTo No user-interface to set
    */
    public void addBreak(int main, int subFrom, int subTo) {

        Integer key = new Integer(main);
        Break region = (Break)_breakMap.get(key);
        if(region == null) {
            region = new Break(main, subFrom, subTo);
            _breakMap.put(key, region);
            _breaks.add(region);
        } else {
            region.main = main;
            region.subFrom = subFrom;
            region.subTo = subTo;
        }
    }

    /**
     * Removes the break indicated by the parameter
     * @param main (zero-based)
     */
    public final void removeBreak(int main) {
        Integer rowKey = new Integer(main);
        Break region = (Break)_breakMap.get(rowKey);
        _breaks.remove(region);
        _breakMap.remove(rowKey);
    }

    /**
     * Retrieves the region at the row/column indicated
     * @param main FIXME: Document this!
     * @return The Break or null if no break exists at the row/col specified.
     */
    public final Break getBreak(int main) {
        Integer rowKey = new Integer(main);
        return (Break)_breakMap.get(rowKey);
    }


    public final int[] getBreaks() {
    	int count = getNumBreaks();
    	if (count < 1) {
    		return EMPTY_INT_ARRAY;
    	}
        int[] result = new int[count];
        for (int i=0; i<count; i++) {
            Break breakItem = (Break)_breaks.get(i);
            result[i] = breakItem.main;
        }
        return result;
    }
}
