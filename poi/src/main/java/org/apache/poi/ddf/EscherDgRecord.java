
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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

/**
 * This record simply holds the number of shapes in the drawing group and the
 * last shape id used for this drawing group.
 */
public class EscherDgRecord extends EscherRecord {
    public static final short RECORD_ID = EscherRecordTypes.DG.typeID;

    private int field_1_numShapes;
    private int field_2_lastMSOSPID;

    public EscherDgRecord() {}

    public EscherDgRecord(EscherDgRecord other) {
        super(other);
        field_1_numShapes = other.field_1_numShapes;
        field_2_lastMSOSPID = other.field_2_lastMSOSPID;
    }


    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        field_1_numShapes   =  LittleEndian.getInt( data, pos + size );     size += 4;
        field_2_lastMSOSPID =  LittleEndian.getInt( data, pos + size );     size += 4;
        return getRecordSize();
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        LittleEndian.putInt( data, offset + 4, 8 );
        LittleEndian.putInt( data, offset + 8, field_1_numShapes );
        LittleEndian.putInt( data, offset + 12, field_2_lastMSOSPID );

        listener.afterRecordSerialize( offset + 16, getRecordId(), getRecordSize(), this );
        return getRecordSize();
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    @Override
    public int getRecordSize()
    {
        return 8 + 8;
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return EscherRecordTypes.DG.recordName;
    }

    /**
     * The number of shapes in this drawing group.
     *
     * @return the number of shapes
     */
    public int getNumShapes()
    {
        return field_1_numShapes;
    }

    /**
     * The number of shapes in this drawing group.
     *
     * @param field_1_numShapes the number of shapes
     */
    public void setNumShapes( int field_1_numShapes )
    {
        this.field_1_numShapes = field_1_numShapes;
    }

    /**
     * The last shape id used in this drawing group.
     *
     * @return the last shape id
     */
    public int getLastMSOSPID()
    {
        return field_2_lastMSOSPID;
    }

    /**
     * The last shape id used in this drawing group.
     *
     * @param field_2_lastMSOSPID the last shape id
     */
    public void setLastMSOSPID( int field_2_lastMSOSPID )
    {
        this.field_2_lastMSOSPID = field_2_lastMSOSPID;
    }

    /**
     * Gets the drawing group id for this record.  This is encoded in the
     * instance part of the option record.
     *
     * @return  a drawing group id.
     */
    public short getDrawingGroupId()
    {
        return (short) ( getOptions() >> 4 );
    }

    /**
     * Increments the number of shapes
     */
    public void incrementShapeCount()
    {
        this.field_1_numShapes++;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "numShapes", this::getNumShapes,
            "lastMSOSPID", this::getLastMSOSPID,
            "drawingGroupId", this::getDrawingGroupId
        );
    }

    @Override
    public Enum getGenericRecordType() {
        return EscherRecordTypes.DG;
    }

    @Override
    public EscherDgRecord copy() {
        return new EscherDgRecord(this);
    }
}
