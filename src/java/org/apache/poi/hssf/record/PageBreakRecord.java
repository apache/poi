/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.LittleEndian;

/**
 * Record that contains the functionality page breaks (horizontal and vertical)
 * <p>
 * The other two classes just specifically set the SIDS for record creation
 * @see HorizontalPageBreakRecord
 * @see VerticalPageBreakREcord
 * 
 * REFERENCE:  Microsoft Excel SDK page 322 and 420
 * @author Danny Mui (dmui at apache dot org)
 */
public class PageBreakRecord extends Record {
   public static final short HORIZONTAL_SID = (short)0x1B;
   public static final short VERTICAL_SID = (short)0x1A;
   public short sid;
   private short numBreaks;
   private List breaks;
   private Map BreakMap;
      
    /**
     * Since both records store 2byte integers (short), no point in 
     * differentiating it in the records.
     * <p>
     * The subs (rows or columns, don't seem to be able to set but excel sets
     * them automatically)
     */
    public class Break
    {

        public short main;
        public short subFrom;
        public short subTo;

        public Break(short main, short subFrom, short subTo)
        {
            this.main = main;
            this.subFrom = subFrom;
            this.subTo = subTo;
        }
    }

    public PageBreakRecord()
    {

    }

    /**
     * 
     * @param sid
     */
    public PageBreakRecord(short sid) {
       super();
       this.sid = sid;
    }

    public PageBreakRecord(short id, short size, byte data[])
    {
        super(id, size, data);
        this.sid = id;
    }

    public PageBreakRecord(short id, short size, byte data[], int offset)
    {
        super(id, size, data, offset);
        this.sid = id;
    }

    protected void fillFields(byte data[], short size, int offset)
    {
    	  short loadedBreaks = LittleEndian.getShort(data, 0 + offset); 
        setNumBreaks(loadedBreaks);
        int pos = 2;
        for(int k = 0; k < loadedBreaks; k++)
        {
            addBreak((short)(LittleEndian.getShort(data, pos + offset) - 1), LittleEndian.getShort(data, pos + 2 + offset), LittleEndian.getShort(data, pos + 4 + offset));
            pos += 6;
        }

    }

    public short getSid()
    {
        return sid;
    }

    public int serialize(int offset, byte data[])
    {
        int recordsize = getRecordSize();
        int pos = 6;
        LittleEndian.putShort(data, offset + 0, getSid());
        LittleEndian.putShort(data, offset + 2, (short)(recordsize - 4));
        LittleEndian.putShort(data, offset + 4, getNumBreaks());
        for(Iterator iterator = getBreaksIterator(); iterator.hasNext();)
        {
            Break Break = (Break)iterator.next();
            LittleEndian.putShort(data, offset + pos, (short)(Break.main + 1));
            pos += 2;
            LittleEndian.putShort(data, offset + pos, Break.subFrom);
            pos += 2;
            LittleEndian.putShort(data, offset + pos, Break.subTo);
            pos += 2;
        }

        return recordsize;
    }

    protected void validateSid(short id)
    {
        if(id != HORIZONTAL_SID && id != VERTICAL_SID)
            throw new RecordFormatException("NOT A HorizontalPageBreak or VerticalPageBreak RECORD!! " + id);
        else
            return;
    }

    public short getNumBreaks()
    {
        return breaks != null ? (short)breaks.size() : numBreaks;
    }

    public void setNumBreaks(short numBreaks)
    {
        this.numBreaks = numBreaks;
    }

    public Iterator getBreaksIterator()
    {
        if(breaks == null)
            return Collections.EMPTY_LIST.iterator();
        else
            return breaks.iterator();
    }

    public String toString()
    {
        StringBuffer retval = new StringBuffer();
        
        if (getSid() != HORIZONTAL_SID && getSid()!= VERTICAL_SID) 
            return "[INVALIDPAGEBREAK]\n     .sid ="+getSid()+"[INVALIDPAGEBREAK]";
        
        String label;
        String mainLabel;
        String subLabel;
        
        if (getSid() == HORIZONTAL_SID) {
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
    * @param subFrom No user-interface to set (defaults to minumum, 0)
    * @param subTo No user-interface to set
    */
    public void addBreak(short main, short subFrom, short subTo)
    {
        if(breaks == null)
        {
            breaks = new ArrayList(getNumBreaks() + 10);
            BreakMap = new HashMap();
        }
        Integer key = new Integer(main);
        Break region = (Break)BreakMap.get(key);
        if(region != null)
        {
            region.main = main;
            region.subFrom = subFrom;
            region.subTo = subTo;
        } else
        {
            region = new Break(main, subFrom, subTo);
            breaks.add(region);
        }
        BreakMap.put(key, region);
    }

    /**
     * Removes the break indicated by the parameter
     * @param main (zero-based)
     */
    public void removeBreak(short main)
    {
        Integer rowKey = new Integer(main);
        Break region = (Break)BreakMap.get(rowKey);
        breaks.remove(region);
        BreakMap.remove(rowKey);
    }

    public int getRecordSize()
    {
        return 6 + getNumBreaks() * 6;
    }

    /**
     * Retrieves the region at the row/column indicated
     * @param main
     * @return
     */
    public Break getBreak(short main)
    {
        Integer rowKey = new Integer(main);
        return (Break)BreakMap.get(rowKey);
    }

   /* Clones the page break record 
    * @see java.lang.Object#clone()
    */
   public Object clone() {
      PageBreakRecord record = new PageBreakRecord(getSid());      
      Iterator iterator = getBreaksIterator();
      while (iterator.hasNext()) {
         Break original = (Break)iterator.next();
         record.addBreak(original.main, original.subFrom, original.subTo);
      }
      return record;
   }

    
}
