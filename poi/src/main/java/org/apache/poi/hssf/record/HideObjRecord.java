
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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Flag defines whether to hide placeholders and object
 *
 * @version 2.0-pre
 */
public final class HideObjRecord extends StandardRecord {
    public static final short sid               = 0x8d;
    public static final short HIDE_ALL          = 2;
    public static final short SHOW_PLACEHOLDERS = 1;
    public static final short SHOW_ALL          = 0;

    private short field_1_hide_obj;

    public HideObjRecord() {}

    public HideObjRecord(HideObjRecord other) {
        super(other);
        field_1_hide_obj = other.field_1_hide_obj;
    }

    public HideObjRecord(RecordInputStream in) {
        field_1_hide_obj = in.readShort();
    }

    /**
     * set hide object options
     *
     * @param hide options
     * @see #HIDE_ALL
     * @see #SHOW_PLACEHOLDERS
     * @see #SHOW_ALL
     */

    public void setHideObj(short hide)
    {
        field_1_hide_obj = hide;
    }

    /**
     * get hide object options
     *
     * @return hide options
     * @see #HIDE_ALL
     * @see #SHOW_PLACEHOLDERS
     * @see #SHOW_ALL
     */

    public short getHideObj()
    {
        return field_1_hide_obj;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getHideObj());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public HideObjRecord copy() {
        return new HideObjRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.HIDE_OBJ;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("hideObj", this::getHideObj);
    }
}
