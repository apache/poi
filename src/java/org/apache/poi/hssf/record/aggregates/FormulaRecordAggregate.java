/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.*;

/**
 * The formula record aggregate is used to join together the formula record and it's
 * (optional) string record and (optional) Shared Formula Record (template reads, excel optimization).
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class FormulaRecordAggregate
        extends Record
        implements CellValueRecordInterface, Comparable
{
    public final static short sid       = -2000;

    private FormulaRecord formulaRecord;
    private StringRecord stringRecord;
    
    /**
     * will only be set through the RecordFactory
     */
	 private SharedFormulaRecord sharedFormulaRecord;

    public FormulaRecordAggregate( FormulaRecord formulaRecord, StringRecord stringRecord )
    {
        this.formulaRecord = formulaRecord;
        this.stringRecord = stringRecord;
    }

	/**
	 * Used only in the clone
	 * @param formulaRecord
	 * @param stringRecord
	 * @param sharedRecord
	 */
	public FormulaRecordAggregate( FormulaRecord formulaRecord, StringRecord stringRecord, SharedFormulaRecord sharedRecord)
	{
		  this.formulaRecord = formulaRecord;
		  this.stringRecord = stringRecord;
		  this.sharedFormulaRecord = sharedRecord;
	}



    protected void validateSid( short id )
    {
    }

    protected void fillFields( byte[] data, short size, int offset )
    {
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset to begin writing at
     * @param data byte array containing instance data
     * @return number of bytes written
     */

    public int serialize( int offset, byte[] data )
    {
        int pos = offset;
        pos += formulaRecord.serialize(pos, data);
        if (this.getSharedFormulaRecord() != null) 
        {
        		pos += getSharedFormulaRecord().serialize(pos, data);
        }	
         if (stringRecord != null)
        {
            pos += stringRecord.serialize(pos, data);
        }
        return pos - offset;
        
    }

    /**
     * gives the current serialized size of the record. Should include the sid and reclength (4 bytes).
     */
    public int getRecordSize()
    {
        int size = formulaRecord.getRecordSize() + (stringRecord == null ? 0 : stringRecord.getRecordSize());
        size += (getSharedFormulaRecord() == null) ? 0 : getSharedFormulaRecord().getRecordSize();
        return size;
    }


    /**
     * return the non static version of the id for this record.
     */
    public short getSid()
    {
        return sid;
    }

    public void setStringRecord( StringRecord stringRecord )
    {
        this.stringRecord = stringRecord;
    }

    public void setFormulaRecord( FormulaRecord formulaRecord )
    {
        this.formulaRecord = formulaRecord;
    }

    public FormulaRecord getFormulaRecord()
    {
        return formulaRecord;
    }

    public StringRecord getStringRecord()
    {
        return stringRecord;
    }

    public boolean isEqual(CellValueRecordInterface i)
    {
        return formulaRecord.isEqual( i );
    }

    public boolean isAfter(CellValueRecordInterface i)
    {
        return formulaRecord.isAfter( i );
    }

    public boolean isBefore(CellValueRecordInterface i)
    {
        return formulaRecord.isBefore( i );
    }

    public short getXFIndex()
    {
        return formulaRecord.getXFIndex();
    }

    public void setXFIndex(short xf)
    {
        formulaRecord.setXFIndex( xf );
    }

    public void setColumn(short col)
    {
        formulaRecord.setColumn( col );
    }

    public void setRow(int row)
    {
        formulaRecord.setRow( row );
    }

    public short getColumn()
    {
        return formulaRecord.getColumn();
    }

    public int getRow()
    {
        return formulaRecord.getRow();
    }

    public int compareTo(Object o)
    {
        return formulaRecord.compareTo( o );
    }

    public boolean equals(Object obj)
    {
        return formulaRecord.equals( obj );
    }

    public String toString()
    {
        return formulaRecord.toString();
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
			StringRecord clonedString = (stringRecord == null) ? null : (StringRecord)stringRecord.clone();
    		SharedFormulaRecord clonedShared = (sharedFormulaRecord == null) ? null : (SharedFormulaRecord)sharedFormulaRecord.clone();
    		
        return new FormulaRecordAggregate((FormulaRecord) this.formulaRecord.clone(), clonedString, clonedShared);
    }



   /**
    * @return SharedFormulaRecord
    */
   public SharedFormulaRecord getSharedFormulaRecord() {
      return sharedFormulaRecord;
   }

   /**
    * Sets the sharedFormulaRecord, only set from RecordFactory since they are not generated by POI and are an Excel optimization
    * @param sharedFormulaRecord The sharedFormulaRecord to set
    */
   public void setSharedFormulaRecord(SharedFormulaRecord sharedFormulaRecord) {
      this.sharedFormulaRecord = sharedFormulaRecord;
   }

   /* 
    * Setting to true so that this value does not abort the whole ValueAggregation
    * (non-Javadoc)
    * @see org.apache.poi.hssf.record.Record#isInValueSection()
    */
   public boolean isInValueSection() {

      return true;
   }
   
   public String getStringValue() {
        if(stringRecord==null) return null;
        return stringRecord.getString();
   }

}
