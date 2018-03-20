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
import java.util.List;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.NullEscherSerializationListener;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.util.LazilyConcatenatedByteArray;

/**
 * The escher container record is used to hold escher records.  It is abstract and
 * must be subclassed for maximum benefit.
 */
public abstract class AbstractEscherHolderRecord extends Record implements Cloneable {
    private static boolean DESERIALISE;
    static {
    try {
            DESERIALISE = (System.getProperty("poi.deserialize.escher") != null);
        } catch (SecurityException e) {
            DESERIALISE = false;
        }
    }

    private final List<EscherRecord> escherRecords;
    private final LazilyConcatenatedByteArray rawDataContainer = new LazilyConcatenatedByteArray();

    public AbstractEscherHolderRecord()
    {
        escherRecords = new ArrayList<>();
    }

    public AbstractEscherHolderRecord(RecordInputStream in)
    {
        escherRecords = new ArrayList<>();
        if (! DESERIALISE ) {
            rawDataContainer.concatenate(in.readRemainder());
        } else {
            byte[] data = in.readAllContinuedRemainder();
            convertToEscherRecords( 0, data.length, data );
        }
    }

    protected void convertRawBytesToEscherRecords() {
        if (! DESERIALISE ) {
            byte[] rawData = getRawData();
        	convertToEscherRecords(0, rawData.length, rawData);
        }
    }
    private void convertToEscherRecords( int offset, int size, byte[] data )
    {
         escherRecords.clear();
        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
        int pos = offset;
        while ( pos < offset + size )
        {
            EscherRecord r = recordFactory.createRecord(data, pos);
            int bytesRead = r.fillFields(data, pos, recordFactory );
            escherRecords.add(r);
            pos += bytesRead;
        }
    }

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        final String nl = System.getProperty("line.separator");
        buffer.append('[' + getRecordName() + ']' + nl);
        if (escherRecords.size() == 0)
            buffer.append("No Escher Records Decoded" + nl);
        for (EscherRecord r : escherRecords) {
            buffer.append(r);
        }
        buffer.append("[/" + getRecordName() + ']' + nl);

        return buffer.toString();
    }

    protected abstract String getRecordName();

    @Override
    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort( data, 0 + offset, getSid() );
        LittleEndian.putShort( data, 2 + offset, (short) ( getRecordSize() - 4 ) );
        byte[] rawData = getRawData();
        if ( escherRecords.size() == 0 && rawData != null )
        {
            LittleEndian.putShort(data, 0 + offset, getSid());
            LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
            System.arraycopy( rawData, 0, data, 4 + offset, rawData.length);
            return rawData.length + 4;
        }
        LittleEndian.putShort(data, 0 + offset, getSid());
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        int pos = offset + 4;
        for (EscherRecord r : escherRecords) {
            pos += r.serialize( pos, data, new NullEscherSerializationListener() );
        }
        return getRecordSize();
    }

    @Override
    public int getRecordSize() {
        byte[] rawData = getRawData();
        if (escherRecords.size() == 0 && rawData != null) {
            // XXX: It should be possible to derive this without concatenating the array, too.
            return rawData.length;
        }
        int size = 0;
        for (EscherRecord r : escherRecords) {
            size += r.getRecordSize();
        }
        return size;
    }



    @Override
    public abstract short getSid();

    @Override
    public AbstractEscherHolderRecord clone() {
    	return (AbstractEscherHolderRecord)cloneViaReserialise();
    }

    public void addEscherRecord(int index, EscherRecord element)
    {
        escherRecords.add( index, element );
    }

    public boolean addEscherRecord(EscherRecord element)
    {
        return escherRecords.add( element );
    }

    public List<EscherRecord> getEscherRecords()
    {
        return escherRecords;
    }

    public void clearEscherRecords()
    {
        escherRecords.clear();
    }

    /**
     * If we have a EscherContainerRecord as one of our
     *  children (and most top level escher holders do),
     *  then return that.
     * 
     * @return the EscherContainerRecord or {@code null} if no child is a container record
     */
    public EscherContainerRecord getEscherContainer() {
    	for (EscherRecord er : escherRecords) {
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
     *  
     * @param id the record to look for
     * 
     * @return the record or {@code null} if it can't be found
     */
    public EscherRecord findFirstWithId(short id) {
    	return findFirstWithId(id, getEscherRecords());
    }
    
    private EscherRecord findFirstWithId(short id, List<EscherRecord> records) {
    	// Check at our level
    	for (EscherRecord r : records) {
    		if(r.getRecordId() == id) {
    			return r;
    		}
    	}

    	// Then check our children in turn
    	for (EscherRecord r : records) {
    		if(r.isContainerRecord()) {
    			EscherRecord found = findFirstWithId(id, r.getChildRecords());
    			if(found != null) {
    				return found;
    			}
    		}
    	}

    	// Not found in this lot
    	return null;
    }


    public EscherRecord getEscherRecord(int index)
    {
        return escherRecords.get(index);
    }

    /**
     * Big drawing group records are split but it's easier to deal with them
     * as a whole group so we need to join them together.
     * 
     * @param record the record data to concatenate to the end
     */
    public void join( AbstractEscherHolderRecord record )
    {
        rawDataContainer.concatenate(record.getRawData());
    }

    public void processContinueRecord( byte[] record )
    {
        rawDataContainer.concatenate(record);
    }

    public byte[] getRawData()
    {
        return rawDataContainer.toArray();
    }

    public void setRawData( byte[] rawData )
    {
        rawDataContainer.clear();
        rawDataContainer.concatenate(rawData);
    }

    /**
     * Convert raw data to escher records.
     */
    public void decode()
    {
        if (null == escherRecords || 0 == escherRecords.size()){
            byte[] rawData = getRawData();
            convertToEscherRecords(0, rawData.length, rawData );
        }
    }
}
