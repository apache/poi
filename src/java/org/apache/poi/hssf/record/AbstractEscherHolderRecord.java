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
import java.util.Iterator;
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
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Michael Zalewski (zalewski at optonline.net)
 */
public abstract class AbstractEscherHolderRecord extends Record {
    private static boolean DESERIALISE;
    static {
    try {
            DESERIALISE = (System.getProperty("poi.deserialize.escher") != null);
        } catch (SecurityException e) {
            DESERIALISE = false;
        }
    }

    private List<EscherRecord> escherRecords;
    private LazilyConcatenatedByteArray rawDataContainer = new LazilyConcatenatedByteArray();

    public AbstractEscherHolderRecord()
    {
        escherRecords = new ArrayList<EscherRecord>();
    }

    public AbstractEscherHolderRecord(RecordInputStream in)
    {
        escherRecords = new ArrayList<EscherRecord>();
        if (! DESERIALISE )
        {
            rawDataContainer.concatenate(in.readRemainder());
        }
        else
        {
            byte[] data = in.readAllContinuedRemainder();
            convertToEscherRecords( 0, data.length, data );
        }
    }

    protected void convertRawBytesToEscherRecords() {
        byte[] rawData = getRawData();
    	convertToEscherRecords(0, rawData.length, rawData);
    }
    private void convertToEscherRecords( int offset, int size, byte[] data )
    {
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

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        final String nl = System.getProperty("line.separator");
        buffer.append('[' + getRecordName() + ']' + nl);
        if (escherRecords.size() == 0)
            buffer.append("No Escher Records Decoded" + nl);
        for ( Iterator<EscherRecord> iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord r = iterator.next();
            buffer.append(r.toString());
        }
        buffer.append("[/" + getRecordName() + ']' + nl);

        return buffer.toString();
    }

    protected abstract String getRecordName();

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
        for ( Iterator<EscherRecord> iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord r = iterator.next();
            pos += r.serialize( pos, data, new NullEscherSerializationListener() );
        }
        return getRecordSize();
    }

    public int getRecordSize() {
        byte[] rawData = getRawData();
        if (escherRecords.size() == 0 && rawData != null) {
            // XXX: It should be possible to derive this without concatenating the array, too.
            return rawData.length;
        }
        int size = 0;
        for ( Iterator<EscherRecord> iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord r = iterator.next();
            size += r.getRecordSize();
        }
        return size;
    }



    public abstract short getSid();

    public Object clone()
    {
    	return cloneViaReserialise();
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
     */
    public EscherContainerRecord getEscherContainer() {
    	for(Iterator<EscherRecord> it = escherRecords.iterator(); it.hasNext();) {
    		EscherRecord er = it.next();
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
     */
    public EscherRecord findFirstWithId(short id) {
    	return findFirstWithId(id, getEscherRecords());
    }
    private EscherRecord findFirstWithId(short id, List<EscherRecord> records) {
    	// Check at our level
    	for(Iterator<EscherRecord> it = records.iterator(); it.hasNext();) {
    		EscherRecord r = it.next();
    		if(r.getRecordId() == id) {
    			return r;
    		}
    	}

    	// Then check our children in turn
    	for(Iterator<EscherRecord> it = records.iterator(); it.hasNext();) {
    		EscherRecord r = it.next();
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
        return (EscherRecord) escherRecords.get(index);
    }

    /**
     * Big drawing group records are split but it's easier to deal with them
     * as a whole group so we need to join them together.
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
        byte[] rawData = getRawData();
        convertToEscherRecords(0, rawData.length, rawData );
    }
}
