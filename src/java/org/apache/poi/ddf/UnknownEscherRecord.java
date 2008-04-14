
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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * This record is used whenever a escher record is encountered that
 * we do not explicitly support.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class UnknownEscherRecord extends EscherRecord
{
    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the the 8 byte header */
    private byte[] thedata = NO_BYTES;
    private List childRecords = new ArrayList();

    public UnknownEscherRecord()
    {
    }

    /**
     * This method deserializes the record from a byte array.
     *
     * @param data          The byte array containing the escher record information
     * @param offset        The starting offset into <code>data</code>.
     * @param recordFactory May be null since this is not a container record.
     * @return The number of bytes read from the byte array.
     */
    public int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        if ( isContainerRecord() )
        {
            int bytesWritten = 0;
            thedata = new byte[0];
            offset += 8;
            bytesWritten += 8;
            while ( bytesRemaining > 0 )
            {
                EscherRecord child = recordFactory.createRecord( data, offset );
                int childBytesWritten = child.fillFields( data, offset, recordFactory );
                bytesWritten += childBytesWritten;
                offset += childBytesWritten;
                bytesRemaining -= childBytesWritten;
                getChildRecords().add( child );
            }
            return bytesWritten;
        }
        else
        {
            thedata = new byte[bytesRemaining];
            System.arraycopy( data, offset + 8, thedata, 0, bytesRemaining );
            return bytesRemaining + 8;
        }
    }

    /**
     * Writes this record and any contained records to the supplied byte
     * array.
     *
     * @return  the number of bytes written.
     */
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = thedata.length;
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            remainingBytes += r.getRecordSize();
        }
        LittleEndian.putInt(data, offset+4, remainingBytes);
        System.arraycopy(thedata, 0, data, offset+8, thedata.length);
        int pos = offset+8+thedata.length;
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    public byte[] getData()
    {
        return thedata;
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    public int getRecordSize()
    {
        return 8 + thedata.length;
    }

    public List getChildRecords()
    {
        return childRecords;
    }

    public void setChildRecords( List childRecords )
    {
        this.childRecords = childRecords;
    }

    public Object clone()
    {
        // shallow clone
        return super.clone();
    }

    /**
     * The short name for this record
     */
    public String getRecordName()
    {
        return "Unknown 0x" + HexDump.toHex(getRecordId());
    }

    public String toString()
    {
        String nl = System.getProperty( "line.separator" );

        StringBuffer children = new StringBuffer();
        if ( getChildRecords().size() > 0 )
        {
            children.append( "  children: " + nl );
            for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
            {
                EscherRecord record = (EscherRecord) iterator.next();
                children.append( record.toString() );
                children.append( nl );
            }
        }

        String theDumpHex = "";
        try
        {
            if (thedata.length != 0)
            {
                theDumpHex = "  Extra Data("+thedata.length+"):" + nl;
                theDumpHex += HexDump.dump(thedata, 0, 0);
            }
        }
        catch ( Exception e )
        {
            theDumpHex = "Error!!";
        }

        return getClass().getName() + ":" + nl +
                "  isContainer: " + isContainerRecord() + nl +
                "  options: 0x" + HexDump.toHex( getOptions() ) + nl +
                "  recordId: 0x" + HexDump.toHex( getRecordId() ) + nl +
                "  numchildren: " + getChildRecords().size() + nl +
                theDumpHex +
                children.toString();
    }

    public void addChildRecord( EscherRecord childRecord )
    {
        getChildRecords().add( childRecord );
    }

}



