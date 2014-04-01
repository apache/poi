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
public class BitMaskTextProp extends TextProp implements Cloneable {
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
	 * As we're purely mask based, just set flags for stuff
	 *  that is set
	 */
	public int getWriteMask() {
		return dataValue;
	}
	
	/**
	 * Set the value of the text property, and recompute the sub
	 *  properties based on it
	 */
	public void setValue(int val) { 
		dataValue = val;

		// Figure out the values of the sub properties
		for(int i=0; i< subPropMatches.length; i++) {
			subPropMatches[i] = false;
			if((dataValue & subPropMasks[i]) != 0) {
				subPropMatches[i] = true;
			}
		}
	}

	/**
	 * Fetch the true/false status of the subproperty with the given index
	 */
	public boolean getSubValue(int idx) {
		return subPropMatches[idx];
	}

	/**
	 * Set the true/false status of the subproperty with the given index
	 */
	public void setSubValue(boolean value, int idx) {
		if(subPropMatches[idx] == value) { return; }
		if(value) {
			dataValue += subPropMasks[idx];
		} else {
			dataValue -= subPropMasks[idx];
		}
		subPropMatches[idx] = value;
	}
	
	public Object clone(){
		BitMaskTextProp newObj = (BitMaskTextProp)super.clone();
		
		// Don't carry over matches, but keep everything 
		//  else as it was
		newObj.subPropMatches = new boolean[subPropMatches.length];
		
		return newObj;
	}
}