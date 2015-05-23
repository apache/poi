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

/** 
 * Definition of a special kind of property of some text, or its 
 *  paragraph. For these properties, a flag in the "contains" header 
 *  field tells you the data property family will exist. The value
 *  of the property is itself a mask, encoding several different
 *  (but related) properties
 */
public abstract class BitMaskTextProp extends TextProp implements Cloneable {
	private String[] subPropNames;
	private int[] subPropMasks;
	private boolean[] subPropMatches;

	/** Fetch the list of the names of the sub properties */
	public String[] getSubPropNames() { return subPropNames; }
	/** Fetch the list of if the sub properties match or not */
	public boolean[] getSubPropMatches() { return subPropMatches; }

	public BitMaskTextProp(int sizeOfDataBlock, int maskInHeader, String overallName, String[] subPropNames) {
		super(sizeOfDataBlock,maskInHeader,"bitmask");
		this.subPropNames = subPropNames;
		this.propName = overallName;
		subPropMasks = new int[subPropNames.length];
		subPropMatches = new boolean[subPropNames.length];
		
		// Initialise the masks list
		for(int i=0; i<subPropMasks.length; i++) {
			subPropMasks[i] = (1 << i);
		}
	}
	
	/**
	 * Calculate mask from the subPropMatches.
	 */
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

	public void setWriteMask(int containsField) {
        int i = 0;
        for (int subMask : subPropMasks) {
            if ((containsField & subMask) != 0) subPropMatches[i] = true;
            i++;
        }
	}
	
	/**
	 * Set the value of the text property, and recompute the sub
	 * properties based on it, i.e. all unset subvalues won't be saved.
	 * Use {@link #setSubValue(boolean, int)} to explicitly set subvalues to {@code false}. 
	 */
	@Override
	public void setValue(int val) { 
		dataValue = val;

		// Figure out the values of the sub properties
		int i = 0;
		for(int mask : subPropMasks) {
		    subPropMatches[i++] = ((val & mask) != 0);
		}
	}

	/**
	 * Fetch the true/false status of the subproperty with the given index
	 */
	public boolean getSubValue(int idx) {
		return (dataValue & subPropMasks[idx]) != 0;
	}

	/**
	 * Set the true/false status of the subproperty with the given index
	 */
	public void setSubValue(boolean value, int idx) {
        subPropMatches[idx] = true;
        if (value) {
            dataValue |= subPropMasks[idx];
        } else {
            dataValue &= ~subPropMasks[idx];
        }
	}
	
	@Override
	public BitMaskTextProp clone(){
		BitMaskTextProp newObj = (BitMaskTextProp)super.clone();
		
		// Don't carry over matches, but keep everything 
		//  else as it was
		newObj.subPropMatches = new boolean[subPropMatches.length];
		
		return newObj;
	}
	
    public BitMaskTextProp cloneAll(){
        return (BitMaskTextProp)super.clone();
    }	
}