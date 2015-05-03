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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.util.LittleEndian;

/**
 * For a given run of characters, holds the properties (which could
 *  be paragraph properties or character properties).
 * Used to hold the number of characters affected, the list of active
 *  properties, and the random reserved field if required.
 */
public class TextPropCollection {
	private int charactersCovered;
	private short reservedField;
	private List<TextProp> textPropList;
    private int maskSpecial = 0;
    
    public int getSpecialMask() { return maskSpecial; }

	/** Fetch the number of characters this styling applies to */
	public int getCharactersCovered() { return charactersCovered; }
	/** Fetch the TextProps that define this styling */
	public List<TextProp> getTextPropList() { return textPropList; }
	
	/** Fetch the TextProp with this name, or null if it isn't present */
	public TextProp findByName(String textPropName) {
		for(int i=0; i<textPropList.size(); i++) {
			TextProp prop = textPropList.get(i);
			if(prop.getName().equals(textPropName)) {
				return prop;
			}
		}
		return null;
	}
	
	/** Add the TextProp with this name to the list */
	public TextProp addWithName(String name) {
		// Find the base TextProp to base on
		TextProp base = null;
		for(int i=0; i < StyleTextPropAtom.characterTextPropTypes.length; i++) {
			if(StyleTextPropAtom.characterTextPropTypes[i].getName().equals(name)) {
				base = StyleTextPropAtom.characterTextPropTypes[i];
			}
		}
		for(int i=0; i < StyleTextPropAtom.paragraphTextPropTypes.length; i++) {
			if(StyleTextPropAtom.paragraphTextPropTypes[i].getName().equals(name)) {
				base = StyleTextPropAtom.paragraphTextPropTypes[i];
			}
		}
		if(base == null) {
			throw new IllegalArgumentException("No TextProp with name " + name + " is defined to add from");
		}
		
		// Add a copy of this property, in the right place to the list
		TextProp textProp = base.clone();
		int pos = 0;
		for(int i=0; i<textPropList.size(); i++) {
			TextProp curProp = textPropList.get(i);
			if(textProp.getMask() > curProp.getMask()) {
				pos++;
			}
		}
		textPropList.add(pos, textProp);
		return textProp;
	}

	/**
	 * For an existing set of text properties, build the list of 
	 *  properties coded for in a given run of properties.
	 * @return the number of bytes that were used encoding the properties list
	 */
	public int buildTextPropList(int containsField, TextProp[] potentialProperties, byte[] data, int dataOffset) {
		int bytesPassed = 0;

		// For each possible entry, see if we match the mask
		// If we do, decode that, save it, and shuffle on
		for(TextProp tp : potentialProperties) {
			// Check there's still data left to read

			// Check if this property is found in the mask
			if((containsField & tp.getMask()) != 0) {
                if(dataOffset+bytesPassed >= data.length) {
                    // Out of data, can't be any more properties to go
                    // remember the mask and return
                    maskSpecial |= tp.getMask();
                    return bytesPassed;
                }

				// Bingo, data contains this property
				TextProp prop = tp.clone();
				int val = 0;
				if (prop instanceof TabStopPropCollection) {
				    ((TabStopPropCollection)prop).parseProperty(data, dataOffset+bytesPassed);
				} else if (prop.getSize() == 2) {
					val = LittleEndian.getShort(data,dataOffset+bytesPassed);
				} else if(prop.getSize() == 4) {
					val = LittleEndian.getInt(data,dataOffset+bytesPassed);
				} else if (prop.getSize() == 0) {
                    //remember "special" bits.
                    maskSpecial |= tp.getMask();
                    continue;
                }
				prop.setValue(val);
				bytesPassed += prop.getSize();
				textPropList.add(prop);
			}
		}

		// Return how many bytes were used
		return bytesPassed;
	}

	/**
	 * Create a new collection of text properties (be they paragraph
	 *  or character) which will be groked via a subsequent call to
	 *  buildTextPropList().
	 */
	public TextPropCollection(int charactersCovered, short reservedField) {
		this.charactersCovered = charactersCovered;
		this.reservedField = reservedField;
		textPropList = new ArrayList<TextProp>();
	}

	/**
	 * Create a new collection of text properties (be they paragraph
	 *  or character) for a run of text without any
	 */
	public TextPropCollection(int textSize) {
		charactersCovered = textSize;
		reservedField = -1;
		textPropList = new ArrayList<TextProp>();
	}
	
    /**
     * Clones the given text properties
     */
	public void copy(TextPropCollection other) {
        this.charactersCovered = other.charactersCovered;
        this.reservedField = other.reservedField;
        this.textPropList.clear();
        for (TextProp tp : other.textPropList) {
            TextProp tpCopy = (tp instanceof BitMaskTextProp)
                ? ((BitMaskTextProp)tp).cloneAll()
                : tp.clone();
            this.textPropList.add(tpCopy);
        }
	}
	
	/**
	 * Update the size of the text that this set of properties
	 *  applies to 
	 */
	public void updateTextSize(int textSize) {
		charactersCovered = textSize;
	}

	/**
	 * Writes out to disk the header, and then all the properties
	 */
	public void writeOut(OutputStream o, TextProp[] potentialProperties) throws IOException {
		// First goes the number of characters we affect
		StyleTextPropAtom.writeLittleEndian(charactersCovered,o);

		// Then we have the reserved field if required
		if(reservedField > -1) {
			StyleTextPropAtom.writeLittleEndian(reservedField,o);
		}

		// Then the mask field
		int mask = maskSpecial;
		for(TextProp textProp : textPropList) {
            //sometimes header indicates that the bitmask is present but its value is 0
            if (textProp instanceof BitMaskTextProp) {
                if(mask == 0) mask |=  textProp.getWriteMask();
            }
            else {
                mask |= textProp.getWriteMask();
            }
        }
		StyleTextPropAtom.writeLittleEndian(mask,o);

		// Then the contents of all the properties
		for (TextProp potProp : potentialProperties) {
    		for(TextProp textProp : textPropList) {
    		    if (!textProp.getName().equals(potProp.getName())) continue;
                int val = textProp.getValue();
                if (textProp instanceof BitMaskTextProp && val == 0) {
                    // don't add empty properties, as they can't be recognized while reading
                    continue;
                } else if (textProp.getSize() == 2) {
    				StyleTextPropAtom.writeLittleEndian((short)val,o);
    			} else if (textProp.getSize() == 4) {
    				StyleTextPropAtom.writeLittleEndian(val,o);
    			}
    		}
		}
	}

    public short getReservedField(){
        return reservedField;
    }

    public void setReservedField(short val){
        reservedField = val;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + charactersCovered;
        result = prime * result + maskSpecial;
        result = prime * result + reservedField;
        result = prime * result + ((textPropList == null) ? 0 : textPropList.hashCode());
        return result;
    }
    /**
     * compares most properties apart of the covered characters length
     */
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        
        TextPropCollection o = (TextPropCollection)other;
        if (o.maskSpecial != this.maskSpecial || o.reservedField != this.reservedField) {
            return false;
        }

        if (textPropList == null) {
            return (o.textPropList == null);
        }        
        
        Map<String,TextProp> m = new HashMap<String,TextProp>();
        for (TextProp tp : o.textPropList) {
            m.put(tp.getName(), tp);
        }
        
        for (TextProp tp : this.textPropList) {
            TextProp otp = m.get(tp.getName());
            if (!tp.equals(otp)) return false;
        }
        
        return true;
    }

}
