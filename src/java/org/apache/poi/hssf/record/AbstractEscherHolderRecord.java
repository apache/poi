/* ====================================================================
   Copyright 2004   Apache Software Foundation

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

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.NullEscherSerializationListener;
import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The escher container record is used to hold escher records.  It is abstract and
 * must be subclassed for maximum benefit.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Michael Zalewski (zalewski at optonline.net)
 */
public abstract class AbstractEscherHolderRecord
    extends Record
{
    private static final boolean DESERIALISE = System.getProperty("poi.deserialize.escher") != null;

    private List escherRecords;
    private byte[] rawData;


    public AbstractEscherHolderRecord()
    {
        escherRecords = new ArrayList();
    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public AbstractEscherHolderRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public AbstractEscherHolderRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != getSid())
        {
            throw new RecordFormatException("Not a Bar record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        escherRecords = new ArrayList();
        if (! DESERIALISE )
        {
            rawData = new byte[size];
            System.arraycopy(data, offset, rawData, 0, size);
        }
        else
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
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        final String nl = System.getProperty("line.separator");
        buffer.append('[' + getRecordName() + ']' + nl);
        for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
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
        if ( escherRecords.size() == 0 && rawData != null )
        {
            System.arraycopy( rawData, 0, data, offset + 4, rawData.length );
        }
        else
        {
            int pos = offset + 4;
            for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
            {
                EscherRecord r = (EscherRecord) iterator.next();
                pos += r.serialize( pos, data, new NullEscherSerializationListener() );
            }
        }
        return getRecordSize();
    }

//    public int serialize(int offset, byte[] data)
//    {
//        if (escherRecords.size() == 0 && rawData != null)
//        {
//            System.arraycopy( rawData, 0, data, offset, rawData.length);
//            return rawData.length;
//        }
//        else
//        {
//            collapseShapeInformation();
//
//            LittleEndian.putShort(data, 0 + offset, getSid());
//            LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
//
//            int pos = offset + 4;
//            for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
//            {
//                EscherRecord r = (EscherRecord) iterator.next();
//                pos += r.serialize(pos, data, new NullEscherSerializationListener() );
//            }
//
//            return getRecordSize();
//        }
//    }

    /**
     * Size of record (including 4 byte header)
     */
    public int getRecordSize()
    {
        if (escherRecords.size() == 0 && rawData != null)
        {
            return rawData.length + 4;
        }
        else
        {
            int size = 4;
            for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
            {
                EscherRecord r = (EscherRecord) iterator.next();
                size += r.getRecordSize();
            }
            return size;
        }
    }

//
//    /**
//     * Size of record (including 4 byte header)
//     */
//    public int getRecordSize()
//    {
//        if (escherRecords.size() == 0 && rawData != null)
//        {
//            return rawData.length;
//        }
//        else
//        {
//            collapseShapeInformation();
//
//            int size = 4;
//            for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
//            {
//                EscherRecord r = (EscherRecord) iterator.next();
//                size += r.getRecordSize();
//            }
//            return size;
//        }
//    }

    public abstract short getSid();

    public Object clone()
    {
        throw new IllegalStateException("Not implemented yet.");
    }

    public void addEscherRecord(int index, EscherRecord element)
    {
        escherRecords.add( index, element );
    }

    public boolean addEscherRecord(EscherRecord element)
    {
        return escherRecords.add( element );
    }

    public List getEscherRecords()
    {
        return escherRecords;
    }

    public void clearEscherRecords()
    {
        escherRecords.clear();
    }


    public EscherRecord getEscherRecord(int index)
    {
        return (EscherRecord) escherRecords.get(index);
    }


}  // END OF CLASS




