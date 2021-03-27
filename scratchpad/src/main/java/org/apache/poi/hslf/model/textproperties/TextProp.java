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

package org.apache.poi.hslf.model.textproperties;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Definition of a property of some text, or its paragraph. Defines
 * how to find out if it's present (via the mask on the paragraph or
 * character "contains" header field), how long the value of it is,
 * and how to get and set the value.
 *
 * As the exact form of these (such as mask value, size of data
 *  block etc) is different for StyleTextProps and
 *  TxMasterTextProps, the definitions of the standard
 *  TextProps is stored in the different record classes
 */
public class TextProp implements Duplicatable, GenericRecord {
	private int sizeOfDataBlock; // Number of bytes the data part uses
	private String propName;
	private int dataValue;
	private int maskInHeader;

	/**
	 * Generate the definition of a given type of text property.
	 */
	public TextProp(int sizeOfDataBlock, int maskInHeader, String propName) {
		this.sizeOfDataBlock = sizeOfDataBlock;
		this.maskInHeader = maskInHeader;
		this.propName = propName;
		this.dataValue = 0;
	}

	/**
	 * Clones the property
	 */
	public TextProp(TextProp other) {
	    this.sizeOfDataBlock = other.sizeOfDataBlock;
	    this.maskInHeader = other.maskInHeader;
	    this.propName = other.propName;
	    this.dataValue = other.dataValue;
	}

	/**
	 * Name of the text property
	 */
	public String getName() { return propName; }

	/**
	 * Size of the data section of the text property (2 or 4 bytes)
	 */
	public int getSize() { return sizeOfDataBlock; }

	/**
	 * Mask in the paragraph or character "contains" header field
	 *  that indicates that this text property is present.
	 */
	public int getMask() { return maskInHeader; }
	/**
	 * Get the mask that's used at write time. Only differs from
	 *  the result of getMask() for the mask based properties
	 */
	public int getWriteMask() { return getMask(); }

	/**
	 * Fetch the value of the text property (meaning is specific to
	 *  each different kind of text property)
	 */
	public int getValue() { return dataValue; }

	/**
	 * Set the value of the text property.
	 */
	public void setValue(int val) { dataValue = val; }

	/**
	 * Clone, eg when you want to actually make use of one of these.
	 */
	@Override
	public TextProp copy(){
		// subclasses need to override copy()
		assert(TextProp.class.equals(this.getClass()));
		return new TextProp(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataValue, maskInHeader, propName, sizeOfDataBlock);
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextProp other = (TextProp) obj;
        if (dataValue != other.dataValue) {
            return false;
        }
        if (maskInHeader != other.maskInHeader) {
            return false;
        }
        if (propName == null) {
            if (other.propName != null) {
                return false;
            }
        } else if (!propName.equals(other.propName)) {
            return false;
        }
        if (sizeOfDataBlock != other.sizeOfDataBlock) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        int len;
        switch (getSize()) {
        case 1: len = 4; break;
        case 2: len = 6; break;
        default: len = 10; break;
        }
        return String.format(Locale.ROOT, "%s = %d (%0#"+len+"X mask / %d bytes)", getName(), getValue(), getMask(), getSize());
    }

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"sizeOfDataBlock", this::getSize,
			"propName", this::getName,
			"dataValue", this::getValue,
			"maskInHeader", this::getMask
		);
	}
}