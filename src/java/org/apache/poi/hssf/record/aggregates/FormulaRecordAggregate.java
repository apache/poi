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
