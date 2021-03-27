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

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;

/**
 * The common object data record is used to store all common preferences for an excel object.
 */
public final class CommonObjectDataSubRecord extends SubRecord {
    public static final short sid = 0x0015;

    private static final BitField locked    = BitFieldFactory.getInstance(0x0001);
    private static final BitField printable = BitFieldFactory.getInstance(0x0010);
    private static final BitField autofill  = BitFieldFactory.getInstance(0x2000);
    private static final BitField autoline  = BitFieldFactory.getInstance(0x4000);

    public static final short OBJECT_TYPE_GROUP              = 0;
    public static final short OBJECT_TYPE_LINE               = 1;
    public static final short OBJECT_TYPE_RECTANGLE          = 2;
    public static final short OBJECT_TYPE_OVAL               = 3;
    public static final short OBJECT_TYPE_ARC                = 4;
    public static final short OBJECT_TYPE_CHART              = 5;
    public static final short OBJECT_TYPE_TEXT               = 6;
    public static final short OBJECT_TYPE_BUTTON             = 7;
    public static final short OBJECT_TYPE_PICTURE            = 8;
    public static final short OBJECT_TYPE_POLYGON            = 9;
    public static final short OBJECT_TYPE_RESERVED1          = 10;
    public static final short OBJECT_TYPE_CHECKBOX           = 11;
    public static final short OBJECT_TYPE_OPTION_BUTTON      = 12;
    public static final short OBJECT_TYPE_EDIT_BOX           = 13;
    public static final short OBJECT_TYPE_LABEL              = 14;
    public static final short OBJECT_TYPE_DIALOG_BOX         = 15;
    public static final short OBJECT_TYPE_SPINNER            = 16;
    public static final short OBJECT_TYPE_SCROLL_BAR         = 17;
    public static final short OBJECT_TYPE_LIST_BOX           = 18;
    public static final short OBJECT_TYPE_GROUP_BOX          = 19;
    public static final short OBJECT_TYPE_COMBO_BOX          = 20;
    public static final short OBJECT_TYPE_RESERVED2          = 21;
    public static final short OBJECT_TYPE_RESERVED3          = 22;
    public static final short OBJECT_TYPE_RESERVED4          = 23;
    public static final short OBJECT_TYPE_RESERVED5          = 24;
    public static final short OBJECT_TYPE_COMMENT            = 25;
    public static final short OBJECT_TYPE_RESERVED6          = 26;
    public static final short OBJECT_TYPE_RESERVED7          = 27;
    public static final short OBJECT_TYPE_RESERVED8          = 28;
    public static final short OBJECT_TYPE_RESERVED9          = 29;
    public static final short OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 30;

    private  short      field_1_objectType;
    private  int        field_2_objectId;
    private  short      field_3_option;
    private  int        field_4_reserved1;
    private  int        field_5_reserved2;
    private  int        field_6_reserved3;


    public CommonObjectDataSubRecord() {}

    public CommonObjectDataSubRecord(CommonObjectDataSubRecord other) {
        super(other);
        field_1_objectType = other.field_1_objectType;
        field_2_objectId = other.field_2_objectId;
        field_3_option = other.field_3_option;
        field_4_reserved1 = other.field_4_reserved1;
        field_5_reserved2 = other.field_5_reserved2;
        field_6_reserved3 = other.field_6_reserved3;
    }

    public CommonObjectDataSubRecord(LittleEndianInput in, int size) {
        this(in, size, -1);
    }

    CommonObjectDataSubRecord(LittleEndianInput in, int size, int cmoOt) {
        if (size != 18) {
            throw new RecordFormatException("Expected size 18 but got (" + size + ")");
        }
        field_1_objectType             = in.readShort();
        field_2_objectId               = in.readUShort();
        field_3_option                 = in.readShort();
        field_4_reserved1              = in.readInt();
        field_5_reserved2              = in.readInt();
        field_6_reserved3              = in.readInt();
    }

    @Override
    public void serialize(LittleEndianOutput out) {

        out.writeShort(sid);
        out.writeShort(getDataSize());

        out.writeShort(field_1_objectType);
        out.writeShort(field_2_objectId);
        out.writeShort(field_3_option);
        out.writeInt(field_4_reserved1);
        out.writeInt(field_5_reserved2);
        out.writeInt(field_6_reserved3);
    }

	@Override
    protected int getDataSize() {
        return 2 + 2 + 2 + 4 + 4 + 4;
    }

	/**
	 * @return the record sid
	 */
    public short getSid()
    {
        return sid;
    }

    @Override
    public CommonObjectDataSubRecord copy() {
        return new CommonObjectDataSubRecord(this);
    }


    /**
     * Get the object type field for the CommonObjectData record.
     *
     * @return  One of
     *        OBJECT_TYPE_GROUP
     *        OBJECT_TYPE_LINE
     *        OBJECT_TYPE_RECTANGLE
     *        OBJECT_TYPE_OVAL
     *        OBJECT_TYPE_ARC
     *        OBJECT_TYPE_CHART
     *        OBJECT_TYPE_TEXT
     *        OBJECT_TYPE_BUTTON
     *        OBJECT_TYPE_PICTURE
     *        OBJECT_TYPE_POLYGON
     *        OBJECT_TYPE_RESERVED1
     *        OBJECT_TYPE_CHECKBOX
     *        OBJECT_TYPE_OPTION_BUTTON
     *        OBJECT_TYPE_EDIT_BOX
     *        OBJECT_TYPE_LABEL
     *        OBJECT_TYPE_DIALOG_BOX
     *        OBJECT_TYPE_SPINNER
     *        OBJECT_TYPE_SCROLL_BAR
     *        OBJECT_TYPE_LIST_BOX
     *        OBJECT_TYPE_GROUP_BOX
     *        OBJECT_TYPE_COMBO_BOX
     *        OBJECT_TYPE_RESERVED2
     *        OBJECT_TYPE_RESERVED3
     *        OBJECT_TYPE_RESERVED4
     *        OBJECT_TYPE_RESERVED5
     *        OBJECT_TYPE_COMMENT
     *        OBJECT_TYPE_RESERVED6
     *        OBJECT_TYPE_RESERVED7
     *        OBJECT_TYPE_RESERVED8
     *        OBJECT_TYPE_RESERVED9
     *        OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
     */
    public short getObjectType()
    {
        return field_1_objectType;
    }

    /**
     * Set the object type field for the CommonObjectData record.
     *
     * @param field_1_objectType
     *        One of
     *        OBJECT_TYPE_GROUP
     *        OBJECT_TYPE_LINE
     *        OBJECT_TYPE_RECTANGLE
     *        OBJECT_TYPE_OVAL
     *        OBJECT_TYPE_ARC
     *        OBJECT_TYPE_CHART
     *        OBJECT_TYPE_TEXT
     *        OBJECT_TYPE_BUTTON
     *        OBJECT_TYPE_PICTURE
     *        OBJECT_TYPE_POLYGON
     *        OBJECT_TYPE_RESERVED1
     *        OBJECT_TYPE_CHECKBOX
     *        OBJECT_TYPE_OPTION_BUTTON
     *        OBJECT_TYPE_EDIT_BOX
     *        OBJECT_TYPE_LABEL
     *        OBJECT_TYPE_DIALOG_BOX
     *        OBJECT_TYPE_SPINNER
     *        OBJECT_TYPE_SCROLL_BAR
     *        OBJECT_TYPE_LIST_BOX
     *        OBJECT_TYPE_GROUP_BOX
     *        OBJECT_TYPE_COMBO_BOX
     *        OBJECT_TYPE_RESERVED2
     *        OBJECT_TYPE_RESERVED3
     *        OBJECT_TYPE_RESERVED4
     *        OBJECT_TYPE_RESERVED5
     *        OBJECT_TYPE_COMMENT
     *        OBJECT_TYPE_RESERVED6
     *        OBJECT_TYPE_RESERVED7
     *        OBJECT_TYPE_RESERVED8
     *        OBJECT_TYPE_RESERVED9
     *        OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
     */
    public void setObjectType(short field_1_objectType)
    {
        this.field_1_objectType = field_1_objectType;
    }

    /**
     * Get the object id field for the CommonObjectData record.
     *
     * @return the object id field
     */
    public int getObjectId()
    {
        return field_2_objectId;
    }

    /**
     * Set the object id field for the CommonObjectData record.
     *
     * @param field_2_objectId the object id field
     */
    public void setObjectId(int field_2_objectId)
    {
        this.field_2_objectId = field_2_objectId;
    }

    /**
     * Get the option field for the CommonObjectData record.
     *
     * @return the option field
     */
    public short getOption()
    {
        return field_3_option;
    }

    /**
     * Set the option field for the CommonObjectData record.
     *
     * @param field_3_option the option field
     */
    public void setOption(short field_3_option)
    {
        this.field_3_option = field_3_option;
    }

    /**
     * Get the reserved1 field for the CommonObjectData record.
     *
     * @return the reserved1 field
     */
    public int getReserved1()
    {
        return field_4_reserved1;
    }

    /**
     * Set the reserved1 field for the CommonObjectData record.
     *
     * @param field_4_reserved1 the reserved1 field
     */
    public void setReserved1(int field_4_reserved1)
    {
        this.field_4_reserved1 = field_4_reserved1;
    }

    /**
     * Get the reserved2 field for the CommonObjectData record.
     *
     * @return the reserved2 field
     */
    public int getReserved2()
    {
        return field_5_reserved2;
    }

    /**
     * Set the reserved2 field for the CommonObjectData record.
     *
     * @param field_5_reserved2 the reserved2 field
     */
    public void setReserved2(int field_5_reserved2)
    {
        this.field_5_reserved2 = field_5_reserved2;
    }

    /**
     * Get the reserved3 field for the CommonObjectData record.
     *
     * @return the reserved3 field
     */
    public int getReserved3()
    {
        return field_6_reserved3;
    }

    /**
     * Set the reserved3 field for the CommonObjectData record.
     *
     * @param field_6_reserved3 the reserved3 field
     */
    public void setReserved3(int field_6_reserved3)
    {
        this.field_6_reserved3 = field_6_reserved3;
    }

    /**
     * Sets the locked field value.
     * true if object is locked when sheet has been protected
     *
     * @param value {@code true} if object is locked when sheet has been protected
     */
    public void setLocked(boolean value)
    {
        field_3_option = locked.setShortBoolean(field_3_option, value);
    }

    /**
     * true if object is locked when sheet has been protected
     * @return  the locked field value.
     */
    public boolean isLocked()
    {
        return locked.isSet(field_3_option);
    }

    /**
     * Sets the printable field value.
     * object appears when printed
     *
     * @param value {@code true} if object appears when printed
     */
    public void setPrintable(boolean value)
    {
        field_3_option = printable.setShortBoolean(field_3_option, value);
    }

    /**
     * object appears when printed
     * @return  the printable field value.
     */
    public boolean isPrintable()
    {
        return printable.isSet(field_3_option);
    }

    /**
     * Sets the autofill field value.
     * whether object uses an automatic fill style
     *
     * @param value {@code true} if object uses an automatic fill style
     */
    public void setAutofill(boolean value)
    {
        field_3_option = autofill.setShortBoolean(field_3_option, value);
    }

    /**
     * whether object uses an automatic fill style
     * @return  the autofill field value.
     */
    public boolean isAutofill()
    {
        return autofill.isSet(field_3_option);
    }

    /**
     * Sets the autoline field value.
     * whether object uses an automatic line style
     *
     * @param value {@code true} if object uses an automatic line style
     */
    public void setAutoline(boolean value)
    {
        field_3_option = autoline.setShortBoolean(field_3_option, value);
    }

    /**
     * whether object uses an automatic line style
     * @return  the autoline field value.
     */
    public boolean isAutoline()
    {
        return autoline.isSet(field_3_option);
    }

    @Override
    public SubRecordTypes getGenericRecordType() {
        return SubRecordTypes.COMMON_OBJECT_DATA;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "objectType", this::getObjectType,
            "objectId", this::getObjectId,
            "option", getBitsAsString(this::getOption,
                new BitField[]{locked,printable,autofill,autoline},
                new String[]{"LOCKED","PRINTABLE","AUTOFILL","AUTOLINE"}),
            "reserved1", this::getReserved1,
            "reserved2", this::getReserved2,
            "reserved3", this::getReserved3
        );
    }
}
