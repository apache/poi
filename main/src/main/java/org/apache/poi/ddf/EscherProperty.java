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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.GenericRecordXmlWriter;

/**
 * This is the abstract base class for all escher properties.
 *
 * @see EscherOptRecord
 */
public abstract class EscherProperty implements GenericRecord {
    private final short id;

    static final int IS_BLIP = 0x4000;
    static final int IS_COMPLEX = 0x8000;

    private static final int[] FLAG_MASK = { IS_BLIP, IS_COMPLEX };
    private static final String[] FLAG_NAMES = { "IS_BLIP", "IS_COMPLEX" };

    /**
     * The id is distinct from the actual property number.  The id includes the property number the blip id
     * flag and an indicator whether the property is complex or not.
     *
     * @param id the combined id
     */
    protected EscherProperty(short id) {
        this.id  = id;
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property
     * id.
     *
     * @param propertyNumber the property number
     * @param isComplex true, if this is a complex property
     * @param isBlipId true, if this property is a blip id
     */
    protected EscherProperty(short propertyNumber, boolean isComplex, boolean isBlipId) {
        this((short)(propertyNumber |
            (isComplex ? IS_COMPLEX : 0x0) |
            (isBlipId ? IS_BLIP : 0x0)));
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property
     * id.
     *
     * @param type one of the defined property types
     * @param isComplex true, if this is a complex property
     * @param isBlipId true, if this property is a blip id
     */
    protected EscherProperty(EscherPropertyTypes type, boolean isComplex, boolean isBlipId) {
        this((short)(type.propNumber |
            (isComplex ? IS_COMPLEX : 0) |
            (isBlipId ? IS_BLIP : 0)));
    }

    public short getId() {
        return id;
    }

    public short getPropertyNumber() {
        return (short) (id & 0x3FFF);
    }

    public boolean isComplex() {
        return (id & IS_COMPLEX) != 0;
    }

    public boolean isBlipId() {
        return (id & IS_BLIP) != 0;
    }

    public String getName() {
        return EscherPropertyTypes.forPropertyID(getPropertyNumber()).propName;
    }

    /**
     * Most properties are just 6 bytes in length.  Override this if we're
     * dealing with complex properties.
     *
     * @return size of this property (in bytes)
     */
    public int getPropertySize() {
        return 6;
    }

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     *
     * @param data the buffer to write to
     * @param pos the starting position
     *
     * @return the length of the part
     */
    abstract public int serializeSimplePart( byte[] data, int pos );

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     *
     * @param data the buffer to write to
     * @param pos the starting position
     *
     * @return the length of the part
     */
    abstract public int serializeComplexPart( byte[] data, int pos );


    @Override
    public final String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    public final String toXml(String tab){
        return GenericRecordXmlWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "id", this::getId,
            "name", this::getName,
            "propertyNumber", this::getPropertyNumber,
            "propertySize", this::getPropertySize,
            "flags", GenericRecordUtil.getBitsAsString(this::getId, FLAG_MASK, FLAG_NAMES)
        );
    }

    @Override
    public List<? extends GenericRecord> getGenericChildren() {
        return null;
    }

    @Override
    public EscherPropertyTypes getGenericRecordType() {
        return EscherPropertyTypes.forPropertyID(id);
    }
}