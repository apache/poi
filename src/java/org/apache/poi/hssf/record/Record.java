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

import java.io.ByteArrayInputStream;

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
public abstract class Record extends RecordBase {

    /**
     * instantiates a blank record strictly for ID matching
     */

    protected Record()
    {
    }

    /**
     * Constructor Record
     *
     * @param in the RecordInputstream to read the record from
     */
    protected Record(RecordInputStream in)
    {
        validateSid(in.getSid());
        fillFields(in);
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
     * @param in the RecordInputstream to read the record from
     */

    protected abstract void fillFields(RecordInputStream in);

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
     * gives the current serialized size of the record. Should include the sid and reclength (4 bytes).
     */

    public int getRecordSize()
    {

        // this is kind od a stupid way to do it but for now we just serialize
        // the record and return the size of the byte array
        return serialize().length;
    }

    /**
     * get a string representation of the record (for biffview/debugging)
     */

    public String toString()
    {
        return super.toString();
    }

    /**
     * return the non static version of the id for this record.
     */

    public abstract short getSid();

    public Object clone() {
      throw new RuntimeException("The class "+getClass().getName()+" needs to define a clone method");
    }
    
    /**
     * Clone the current record, via a call to serialise
     *  it, and another to create a new record from the
     *  bytes.
     * May only be used for classes which don't have
     *  internal counts / ids in them. For those which
     *  do, a full record-aware serialise is needed, which
     *  allocates new ids / counts as needed.
     */
    public Record cloneViaReserialise()
    {
    	// Do it via a re-serialise
    	// It's a cheat, but it works...
    	byte[] b = serialize();
    	RecordInputStream rinp = new RecordInputStream(
    			new ByteArrayInputStream(b)
    	);
    	rinp.nextRecord();

    	Record[] r = RecordFactory.createRecord(rinp);
    	if(r.length != 1) {
    		throw new IllegalStateException("Re-serialised a record to clone it, but got " + r.length + " records back!");
    	}
    	return r[0];
    }
}
