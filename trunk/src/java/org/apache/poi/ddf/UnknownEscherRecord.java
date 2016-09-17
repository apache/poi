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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * This record is used whenever a escher record is encountered that
 * we do not explicitly support.
 */
public final class UnknownEscherRecord extends EscherRecord implements Cloneable {
    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the the 8 byte header */
    private byte[] thedata = NO_BYTES;
    private List<EscherRecord> _childRecords;

    public UnknownEscherRecord() {
        _childRecords = new ArrayList<EscherRecord>();
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
		/*
		 * Have a check between avaliable bytes and bytesRemaining, 
		 * take the avaliable length if the bytesRemaining out of range.
		 */
		int avaliable = data.length - (offset + 8);
		if (bytesRemaining > avaliable) {
			bytesRemaining = avaliable;
		}

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

    @Override
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

    /**
     * @return the data which makes up this record
     */
    public byte[] getData() {
        return thedata;
    }

    @Override
    public int getRecordSize() {
        return 8 + thedata.length;
    }

    @Override
    public List<EscherRecord> getChildRecords() {
        return _childRecords;
    }

    @Override
    public void setChildRecords(List<EscherRecord> childRecords) {
        _childRecords = childRecords;
    }

    @Override
    public UnknownEscherRecord clone() {
        UnknownEscherRecord uer = new UnknownEscherRecord();
        uer.thedata = this.thedata.clone();
        uer.setOptions(this.getOptions());
        uer.setRecordId(this.getRecordId());
        return uer;
    }

    @Override
    public String getRecordName() {
        return "Unknown 0x" + HexDump.toHex(getRecordId());
    }

    @Override
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
                "  version: 0x" + HexDump.toHex( getVersion() ) + '\n' +
                "  instance: 0x" + HexDump.toHex( getInstance() ) + '\n' +
                "  recordId: 0x" + HexDump.toHex( getRecordId() ) + '\n' +
                "  numchildren: " + getChildRecords().size() + '\n' +
                theDumpHex +
                children.toString();
    }

    @Override
    public String toXml(String tab) {
        String theDumpHex = HexDump.toHex(thedata, 32);
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append(formatXmlRecordHeader(getClass().getSimpleName(), HexDump.toHex(getRecordId()), HexDump.toHex(getVersion()), HexDump.toHex(getInstance())))
                .append(tab).append("\t").append("<IsContainer>").append(isContainerRecord()).append("</IsContainer>\n")
                .append(tab).append("\t").append("<Numchildren>").append(HexDump.toHex(_childRecords.size())).append("</Numchildren>\n");
        for ( Iterator<EscherRecord> iterator = _childRecords.iterator(); iterator
                .hasNext(); )
        {
            EscherRecord record = iterator.next();
            builder.append(record.toXml(tab+"\t"));
        }
        builder.append(theDumpHex).append("\n");
        builder.append(tab).append("</").append(getClass().getSimpleName()).append(">\n");
        return builder.toString();
    }

    public void addChildRecord(EscherRecord childRecord) {
        getChildRecords().add( childRecord );
    }
}
