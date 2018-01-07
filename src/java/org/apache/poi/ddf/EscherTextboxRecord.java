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

package org.apache.poi.ddf;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;

/**
 * Holds data from the parent application. Most commonly used to store
 *  text in the format of the parent application, rather than in
 *  Escher format. We don't attempt to understand the contents, since
 *  they will be in the parent's format, not Escher format.
 */
public final class EscherTextboxRecord extends EscherRecord implements Cloneable {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    public static final short RECORD_ID = (short)0xF00D;
    public static final String RECORD_DESCRIPTION = "msofbtClientTextbox";

    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the the 8 byte header */
    private byte[] thedata = NO_BYTES;

    public EscherTextboxRecord()
    {
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );

        // Save the data, ready for the calling code to do something
        //  useful with it
        thedata = IOUtils.safelyAllocate(bytesRemaining, MAX_RECORD_LENGTH);
        System.arraycopy( data, offset + 8, thedata, 0, bytesRemaining );
        return bytesRemaining + 8;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = thedata.length;
        LittleEndian.putInt(data, offset+4, remainingBytes);
        System.arraycopy(thedata, 0, data, offset+8, thedata.length);
        int pos = offset+8+thedata.length;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        int size = pos - offset;
        if (size != getRecordSize()) {
            throw new RecordFormatException(size + " bytes written but getRecordSize() reports " + getRecordSize());
        }
        return size;
    }

    /**
     * Returns any extra data associated with this record.  In practice excel
     * does not seem to put anything here, but with PowerPoint this will
     * contain the bytes that make up a TextHeaderAtom followed by a
     * TextBytesAtom/TextCharsAtom
     * 
     * @return the extra data
     */
    public byte[] getData()
    {
        return thedata;
    }

    /**
     * Sets the extra data (in the parent application's format) to be
     * contained by the record. Used when the parent application changes
     * the contents.
     * 
     * @param b the buffer which contains the data
     * @param start the start position in the buffer
     * @param length the length of the block
     */
    public void setData(byte[] b, int start, int length)
    {
        thedata = IOUtils.safelyAllocate(length, MAX_RECORD_LENGTH);
        System.arraycopy(b,start,thedata,0,length);
    }
    
    /**
     * Sets the extra data (in the parent application's format) to be
     * contained by the record. Used when the parent application changes
     * the contents.
     * 
     * @param b the data
     */
    public void setData(byte[] b) {
        setData(b,0,b.length);
    }

    @Override
    public int getRecordSize()
    {
        return 8 + thedata.length;
    }

    @Override
    public EscherTextboxRecord clone() {
        EscherTextboxRecord etr = new EscherTextboxRecord();
        etr.setOptions(this.getOptions());
        etr.setRecordId(this.getRecordId());
        etr.thedata = this.thedata.clone();
        return etr;
    }

    @Override
    public String getRecordName() {
        return "ClientTextbox";
    }

    @Override
    protected Object[][] getAttributeMap() {
        int numCh = getChildRecords().size();
        List<Object> chLst = new ArrayList<>(numCh * 2 + 2);
        chLst.add("children");
        chLst.add(numCh);
        for (EscherRecord er : getChildRecords()) {
            chLst.add(er.getRecordName());
            chLst.add(er);
        }
        
        return new Object[][] {
            { "isContainer", isContainerRecord() },
            chLst.toArray(),
            { "Extra Data", thedata }
        };
    }
}
