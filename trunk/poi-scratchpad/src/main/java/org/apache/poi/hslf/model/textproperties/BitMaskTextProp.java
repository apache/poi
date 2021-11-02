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

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Definition of a special kind of property of some text, or its
 *  paragraph. For these properties, a flag in the "contains" header
 *  field tells you the data property family will exist. The value
 *  of the property is itself a mask, encoding several different
 *  (but related) properties
 */
public abstract class BitMaskTextProp extends TextProp {
    protected static final Logger LOG = LogManager.getLogger(BitMaskTextProp.class);

    private final String[] subPropNames;
    private final int[] subPropMasks;
    private final boolean[] subPropMatches;

    /** Fetch the list of the names of the sub properties */
    public String[] getSubPropNames() { return subPropNames; }
    /** Fetch the list of if the sub properties match or not */
    public boolean[] getSubPropMatches() { return subPropMatches; }


    protected BitMaskTextProp(BitMaskTextProp other) {
        super(other);
        subPropNames = (other.subPropNames == null) ? null : other.subPropNames.clone();
        subPropMasks = (other.subPropMasks == null) ? null : other.subPropMasks.clone();

        // The old clone implementation didn't carry over matches, but keep everything else as it was
        // this is failing unit tests
        // subPropMatches = (other.subPropMatches == null) ? null : new boolean[other.subPropMatches.length];
        subPropMatches = (other.subPropMatches == null) ? null : other.subPropMatches.clone();
    }


    protected BitMaskTextProp(int sizeOfDataBlock, int maskInHeader, String overallName, String... subPropNames) {
        super(sizeOfDataBlock,maskInHeader,overallName);
        this.subPropNames = subPropNames;
        subPropMasks = new int[subPropNames.length];
        subPropMatches = new boolean[subPropNames.length];

        int LSB = Integer.lowestOneBit(maskInHeader);

        // Initialise the masks list
        for(int i=0; i<subPropMasks.length; i++) {
            subPropMasks[i] = (LSB << i);
        }
    }

    /**
     * Calculate mask from the subPropMatches.
     */
    @Override
    public int getWriteMask() {
        /*
         * The dataValue can't be taken as a mask, as sometimes certain properties
         * are explicitly set to false, i.e. the mask says the property is defined
         * but in the actually nibble the property is set to false
         */
        int mask = 0, i = 0;
        for (int subMask : subPropMasks) {
            if (subPropMatches[i++]) mask |= subMask;
        }
        return mask;
    }

    /**
     * Sets the write mask, i.e. which defines the text properties to be considered
     *
     * @param writeMask the mask, bit values outside the property mask range will be ignored
     */
    public void setWriteMask(int writeMask) {
        int i = 0;
        for (int subMask : subPropMasks) {
            subPropMatches[i++] = ((writeMask & subMask) != 0);
        }
    }

    /**
     * Return the text property value.
     * Clears all bits of the value, which are marked as unset.
     *
     * @return the text property value.
     */
    @Override
    public int getValue() {
        return maskValue(super.getValue());
    }

    private int maskValue(int pVal) {
        int val = pVal, i = 0;
        for (int mask : subPropMasks) {
            if (!subPropMatches[i++]) {
                val &= ~mask;
            }
        }
        return val;
    }

    /**
     * Set the value of the text property, and recompute the sub
     * properties based on it, i.e. all unset subvalues will be cleared.
     * Use {@link #setSubValue(boolean, int)} to explicitly set subvalues to {@code false}.
     */
    @Override
    public void setValue(int val) {
        super.setValue(val);

        // Figure out the values of the sub properties
        int i = 0;
        for(int mask : subPropMasks) {
            subPropMatches[i++] = ((val & mask) != 0);
        }
    }

    /**
     * Convenience method to set a value with mask, without splitting it into the subvalues
     */
    public void setValueWithMask(int val, int writeMask) {
        setWriteMask(writeMask);
        super.setValue(maskValue(val));
        if (val != super.getValue()) {
            LOG.atWarn().log("Style properties of '{}' don't match mask - output will be sanitized", getName());
            LOG.atDebug().log(() -> {
                StringBuilder sb = new StringBuilder("The following style attributes of the '")
                        .append(getName()).append("' property will be ignored:\n");
                int i=0;
                for (int mask : subPropMasks) {
                    if (!subPropMatches[i] && (val & mask) != 0) {
                        sb.append(subPropNames[i]).append(",");
                    }
                    i++;
                }
                return new SimpleMessage(sb);
            });
        }
    }

    /**
     * Fetch the true/false status of the subproperty with the given index
     */
    public boolean getSubValue(int idx) {
        return subPropMatches[idx] && ((super.getValue() & subPropMasks[idx]) != 0);
    }

    /**
     * Set the true/false status of the subproperty with the given index
     */
    public void setSubValue(boolean value, int idx) {
        subPropMatches[idx] = true;
        int newVal = super.getValue();
        if (value) {
            newVal |= subPropMasks[idx];
        } else {
            newVal &= ~subPropMasks[idx];
        }
        super.setValue(newVal);
    }

    /**
     * @return an identical copy of this, i.e. also the subPropMatches are copied
     */
    public BitMaskTextProp cloneAll(){
        BitMaskTextProp bmtp = copy();
        if (subPropMatches != null) {
            System.arraycopy(subPropMatches, 0, bmtp.subPropMatches, 0, subPropMatches.length);
        }
        return bmtp;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "flags", getBitsAsString(this::getValue, subPropMasks, subPropNames)
        );
    }

    @Override
    public abstract BitMaskTextProp copy();
}