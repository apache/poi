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
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.types.PICFAbstractType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

@Internal
public class PICFAndOfficeArtData {

    /**
     * Can contain either a {@link EscherBlipRecord} or a {@link EscherBSERecord}.
     * <p>
     * Should never contain more than 1 record.
     */
    private final List<EscherRecord> _blipRecords = new LinkedList<>();

    private final PICF _picf;

    /**
     * A {@link EscherRecordTypes#SP_CONTAINER}.
     */
    private final EscherContainerRecord _shape = new EscherContainerRecord();

    private byte[] _stPicName;

    public PICFAndOfficeArtData( byte[] dataStream, int startOffset )
    {
        int offset = startOffset;

        _picf = new PICF( dataStream, offset );
        offset += PICFAbstractType.getSize();

        if ( _picf.getMm() == 0x0066 )
        {
            short _cchPicName = LittleEndian.getUByte(dataStream, offset);
            offset += 1;

            _stPicName = IOUtils.safelyClone(dataStream, offset, _cchPicName, HWPFDocument.getMaxRecordLength());
            offset += _cchPicName;
        }

        final DefaultEscherRecordFactory escherRecordFactory = new DefaultEscherRecordFactory();
        int recordSize = _shape.fillFields( dataStream, offset,
                escherRecordFactory );
        offset += recordSize;

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

            // [MS-ODRAW] allows for multiple records in a OfficeArtInlineSpContainer, which is what we're parsing here.
            //   However, in the context of a HWPF document, there should be only 1.
            assert _blipRecords.size() == 1;
        }
    }

    /**
     * Contains {@link EscherBlipRecord}s and {@link EscherBSERecord}s.
     *
     * @return List of BLIP records. Never {@code null}.
     */
    public List<EscherRecord> getBlipRecords()
    {
        return _blipRecords;
    }

    public PICF getPicf()
    {
        return _picf;
    }

    /**
     * @return The {@link EscherRecordTypes#SP_CONTAINER}.
     */
    public EscherContainerRecord getShape()
    {
        return _shape;
    }

    public byte[] getStPicName()
    {
        return _stPicName;
    }
}
