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
 * All HSSF Records inherit from this class.
 */
public abstract class Record extends RecordBase {

    protected Record() {
        // no fields to initialise
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @return byte array containing instance data
     */
    public final byte[] serialize() {
        byte[] retval = new byte[ getRecordSize() ];

        serialize(0, retval);
        return retval;
    }

    /**
     * get a string representation of the record (for biffview/debugging)
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * return the non static version of the id for this record.
     * 
     * @return he id for this record
     */
    public abstract short getSid();

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("The class "+getClass().getName()+" needs to define a clone method");
    }

    /**
     * Clone the current record, via a call to serialize
     *  it, and another to create a new record from the
     *  bytes.
     * May only be used for classes which don't have
     *  internal counts / ids in them. For those which
     *  do, a full model-aware cloning is needed, which
     *  allocates new ids / counts as needed.
     * 
     * @return the cloned current record
     */
    public Record cloneViaReserialise() {
        // Do it via a re-serialization
        // It's a cheat, but it works...
        byte[] b = serialize();
        RecordInputStream rinp = new RecordInputStream(new ByteArrayInputStream(b));
        rinp.nextRecord();

        Record[] r = RecordFactory.createRecord(rinp);
        if(r.length != 1) {
            throw new IllegalStateException("Re-serialised a record to clone it, but got " + r.length + " records back!");
        }
        return r[0];
    }
}
