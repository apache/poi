
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

/**
 * Title: Record
 * Description: All HSSF Records inherit from this class.  It
 *              populates the fields common to all records (id, size and data).
 *              Subclasses should be sure to validate the id,
 * Company:
 * @author Andrew C. Oliver
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version 2.0-pre
 */

public abstract class Record
{

    /**
     * The static ID, subclasses should override this value with the id for the
     * record type they handle.
     */

    public short   sid  = 0;
    private short  id   = 0;
    private short  size = 0;
    private byte[] data = null;

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
        this.id   = id;
        this.size = size;
        this.data = data;
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
        this.id   = id;
        this.size = size;
        this.data = data;
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
     * gives the current serialized size of the record.
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
}
