
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
        

package org.apache.poi.hssf.record;

/**
 * Title: Record
 * Description: All HSSF Records inherit from this class.  It
 *              populates the fields common to all records (id, size and data).
 *              Subclasses should be sure to validate the id,
 * Company:
 * @author Andrew C. Oliver
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public abstract class Record
{

    /**
     * instantiates a blank record strictly for ID matching
     */

    public Record()
    {
    }

    /**
     * Constructor Record
     *
     * @param id record id
     * @param size record size
     * @param data raw data
     */

    public Record(short id, short size, byte [] data)
    {
        validateSid(id);
        fillFields(data, size);
    }

    /**
     * Constructor Record
     *
     * @param id record id
     * @param size record size
     * @param data raw data
     */

    public Record(short id, short size, byte [] data, int offset)
    {
        validateSid(id);
        fillFields(data, size, offset);
    }

    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */

    protected abstract void validateSid(short id);

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     */

    protected void fillFields(byte [] data, short size)
    {
        fillFields(data, size, 0);
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */

    protected abstract void fillFields(byte [] data, short size, int offset);

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @return byte array containing instance data
     */

    public byte [] serialize()
    {
        byte[] retval = new byte[ getRecordSize() ];

        serialize(0, retval);
        return retval;
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

    public abstract int serialize(int offset, byte [] data);

    /**
     * gives the current serialized size of the record. Should include the sid and reclength (4 bytes).
     */

    public int getRecordSize()
    {

        // this is kind od a stupid way to do it but for now we just serialize
        // the record and return the size of the byte array
        return serialize().length;
    }

    /**
     * tells whether this type of record contains a value
     */

    public boolean isValue()
    {
        return false;
    }

    /**
     * DBCELL, ROW, VALUES all say yes
     */

    public boolean isInValueSection()
    {
        return false;
    }

    /**
     * get a string representation of the record (for biffview/debugging)
     */

    public String toString()
    {
        return super.toString();
    }

    /**
     * Process a continuation record; default handling is to ignore
     * it -- TODO add logging
     *
     * @param record the continuation record's data
     */

    // made public to satisfy biffviewer

    /* protected */
    public void processContinueRecord(byte [] record)
    {

        // System.out.println("Got a continue record ... NOW what??");
    }

    /**
     * return the non static version of the id for this record.
     */

    public abstract short getSid();

    public Object clone() {
      throw new RuntimeException("The class "+getClass().getName()+" needs to define a clone method");
    }
}
