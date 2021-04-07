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
import org.apache.poi.util.RecordFormatException;

/**
 * The spgr record defines information about a shape group.  Groups in escher
 * are simply another form of shape that you can't physically see.
 */
public class EscherSpgrRecord extends EscherRecord {
    public static final short RECORD_ID = EscherRecordTypes.SPGR.typeID;

    private int field_1_rectX1;
    private int field_2_rectY1;
    private int field_3_rectX2;
    private int field_4_rectY2;

    public EscherSpgrRecord() {}

    public EscherSpgrRecord(EscherSpgrRecord other) {
        super(other);
        field_1_rectX1 = other.field_1_rectX1;
        field_2_rectY1 = other.field_2_rectY1;
        field_3_rectX2 = other.field_3_rectX2;
        field_4_rectY2 = other.field_4_rectY2;
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        field_1_rectX1 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_2_rectY1 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_3_rectX2 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_rectY2 =  LittleEndian.getInt( data, pos + size );size+=4;
        bytesRemaining -= size;
        if (bytesRemaining != 0) {
            throw new RecordFormatException("Expected no remaining bytes but got " + bytesRemaining);
        }
        return 8 + size + bytesRemaining;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );
        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int remainingBytes = 16;
        LittleEndian.putInt( data, offset + 4, remainingBytes );
        LittleEndian.putInt( data, offset + 8, field_1_rectX1 );
        LittleEndian.putInt( data, offset + 12, field_2_rectY1 );
        LittleEndian.putInt( data, offset + 16, field_3_rectX2 );
        LittleEndian.putInt( data, offset + 20, field_4_rectY2 );
        listener.afterRecordSerialize( offset + getRecordSize(), getRecordId(), offset + getRecordSize(), this );
        return 8 + 16;
    }

    @Override
    public int getRecordSize()
    {
        return 8 + 16;
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return EscherRecordTypes.SPGR.recordName;
    }

    /**
     * The starting top-left coordinate of child records.
     *
     * @return the top-left x coordinate
     */
    public int getRectX1()
    {
        return field_1_rectX1;
    }

    /**
     * The top-left coordinate of child records.
     *
     * @param x1 the top-left x coordinate
     */
    public void setRectX1( int x1 )
    {
        this.field_1_rectX1 = x1;
    }

    /**
     * The top-left coordinate of child records.
     *
     * @return the top-left y coordinate
     */
    public int getRectY1()
    {
        return field_2_rectY1;
    }

    /**
     * The top-left y coordinate of child records.
     *
     * @param y1 the top-left y coordinate
     */
    public void setRectY1( int y1 )
    {
        this.field_2_rectY1 = y1;
    }

    /**
     * The bottom-right x coordinate of child records.
     *
     * @return the bottom-right x coordinate
     */
    public int getRectX2()
    {
        return field_3_rectX2;
    }

    /**
     * The bottom-right x coordinate of child records.
     *
     * @param x2 the bottom-right x coordinate
     */
    public void setRectX2( int x2 )
    {
        this.field_3_rectX2 = x2;
    }

    /**
     * The bottom-right y coordinate of child records.
     *
     * @return the bottom-right y coordinate
     */
    public int getRectY2()
    {
        return field_4_rectY2;
    }

    /**
     * The bottom-right y coordinate of child records.
     *
     * @param rectY2 the bottom-right y coordinate
     */
    public void setRectY2(int rectY2) {
        this.field_4_rectY2 = rectY2;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "rectX1", this::getRectX1,
            "rectY1", this::getRectY1,
            "rectX2", this::getRectX2,
            "rectY2", this::getRectY2
        );
    }

    @Override
    public Enum getGenericRecordType() {
        return EscherRecordTypes.SPGR;
    }

    @Override
    public EscherSpgrRecord copy() {
        return new EscherSpgrRecord(this);
    }
}
