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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * This record is used whenever a escher record is encountered that
 * we do not explicitly support.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class UnknownEscherRecord extends EscherRecord {
    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the the 8 byte header */
    private byte[] thedata = NO_BYTES;
    private List<EscherRecord> _childRecords;

    public UnknownEscherRecord() {
        _childRecords = new ArrayList<EscherRecord>();
    }

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        if (isContainerRecord()) {
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
        thedata = new byte[bytesRemaining];
        System.arraycopy( data, offset + 8, thedata, 0, bytesRemaining );
        return bytesRemaining + 8;
    }

    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = thedata.length;
        for (EscherRecord r : _childRecords) {
            remainingBytes += r.getRecordSize();
        }
        LittleEndian.putInt(data, offset+4, remainingBytes);
        System.arraycopy(thedata, 0, data, offset+8, thedata.length);
        int pos = offset+8+thedata.length;
        for (EscherRecord r : _childRecords) {
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    public byte[] getData() {
        return thedata;
    }

    public int getRecordSize() {
        return 8 + thedata.length;
    }

    public List<EscherRecord> getChildRecords() {
        return _childRecords;
    }

    public void setChildRecords(List<EscherRecord> childRecords) {
        _childRecords = childRecords;
    }

    public Object clone() {
        // shallow clone
        return super.clone();
    }

    public String getRecordName() {
        return "Unknown 0x" + HexDump.toHex(getRecordId());
    }

    public String toString() {
        StringBuffer children = new StringBuffer();
        if (getChildRecords().size() > 0) {
            children.append( "  children: " + '\n' );
            for (EscherRecord record : _childRecords) {
                children.append( record.toString() );
                children.append( '\n' );
            }
        }

        String theDumpHex = HexDump.toHex(thedata, 32);

        return getClass().getName() + ":" + '\n' +
                "  isContainer: " + isContainerRecord() + '\n' +
                "  options: 0x" + HexDump.toHex( getOptions() ) + '\n' +
                "  recordId: 0x" + HexDump.toHex( getRecordId() ) + '\n' +
                "  numchildren: " + getChildRecords().size() + '\n' +
                theDumpHex +
                children.toString();
    }

    public void addChildRecord(EscherRecord childRecord) {
        getChildRecords().add( childRecord );
    }
}
