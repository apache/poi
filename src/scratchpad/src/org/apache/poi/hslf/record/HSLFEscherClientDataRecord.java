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

package org.apache.poi.hslf.record;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherSerializationListener;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies whether a shape is a placeholder shape.
 * The number, position, and type of placeholder shapes are determined by
 * the slide layout as specified in the SlideAtom record.
 * 
 * @since POI 3.14-Beta2
 */
public class HSLFEscherClientDataRecord extends EscherClientDataRecord {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    private final List<Record> _childRecords = new ArrayList<>();
    
    public List<? extends Record> getHSLFChildRecords() { 
        return _childRecords;
    }
    
    public void removeChild(Class<? extends Record> childClass) {
        _childRecords.removeIf(childClass::isInstance);
    }
    
    public void addChild(Record childRecord) {
        _childRecords.add(childRecord);
    }
    
    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        byte remainingData[] = IOUtils.safelyAllocate(bytesRemaining, MAX_RECORD_LENGTH);
        System.arraycopy(data, offset+8, remainingData, 0, bytesRemaining);
        setRemainingData(remainingData);
        return bytesRemaining + 8;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );
        
        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        
        byte childBytes[] = getRemainingData();
        
        LittleEndian.putInt(data, offset+4, childBytes.length);
        System.arraycopy(childBytes, 0, data, offset+8, childBytes.length);
        int recordSize = 8+childBytes.length;
        listener.afterRecordSerialize( offset+recordSize, getRecordId(), recordSize, this );
        return recordSize;
    }

    @Override
    public int getRecordSize() {
        return 8 + getRemainingData().length;
    }
    
    @Override
    public byte[] getRemainingData() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Record r : _childRecords) {
                r.writeOut(bos);
            }
        } catch (IOException e) {
            throw new HSLFException(e);
        }
        return bos.toByteArray();
    }

    @Override
    public void setRemainingData( byte[] remainingData ) {
        _childRecords.clear();
        int offset = 0;
        while (offset < remainingData.length) {
            final Record r = Record.buildRecordAtOffset(remainingData, offset);
            if (r != null) {
                _childRecords.add(r);
            }
            long rlen = LittleEndian.getUInt(remainingData,offset+4);
            offset += 8 + rlen;
        }
    }
    
    public String getRecordName() {
        return "HSLFClientData";
    }

    
}
