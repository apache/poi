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
package org.apache.poi.hwpf.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

@Internal
public class PICFAndOfficeArtData
{

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private List<EscherRecord> _blipRecords;

    private short _cchPicName;

    private PICF _picf;

    private EscherContainerRecord _shape;

    private byte[] _stPicName;

    public PICFAndOfficeArtData( byte[] dataStream, int startOffset )
    {
        int offset = startOffset;

        _picf = new PICF( dataStream, offset );
        offset += PICF.getSize();

        if ( _picf.getMm() == 0x0066 )
        {
            _cchPicName = LittleEndian.getUByte( dataStream, offset );
            offset += 1;

            _stPicName = LittleEndian.getByteArray( dataStream, offset,
                    _cchPicName, MAX_RECORD_LENGTH);
            offset += _cchPicName;
        }

        final DefaultEscherRecordFactory escherRecordFactory = new DefaultEscherRecordFactory();
        _shape = new EscherContainerRecord();
        int recordSize = _shape.fillFields( dataStream, offset,
                escherRecordFactory );
        offset += recordSize;

        _blipRecords = new LinkedList<>();
        while ( ( offset - startOffset ) < _picf.getLcb() )
        {
            EscherRecord nextRecord = escherRecordFactory.createRecord(
                    dataStream, offset );
            if ( nextRecord.getRecordId() != (short) 0xF007
                    && ( nextRecord.getRecordId() < (short) 0xF018 || nextRecord
                            .getRecordId() > (short) 0xF117 ) )
                break;

            int blipRecordSize = nextRecord.fillFields( dataStream, offset,
                    escherRecordFactory );
            offset += blipRecordSize;

            _blipRecords.add( nextRecord );
        }
    }

    public List<EscherRecord> getBlipRecords()
    {
        return _blipRecords;
    }

    public PICF getPicf()
    {
        return _picf;
    }

    public EscherContainerRecord getShape()
    {
        return _shape;
    }

    public byte[] getStPicName()
    {
        return _stPicName;
    }
}
