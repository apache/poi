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

import java.io.*;
import java.util.*;

import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.util.HexDump;
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
	private final List<TextProp> textPropList = new ArrayList<TextProp>();
    private int maskSpecial = 0;
    private final TextProp[] potentialPropList;
    
    public int getSpecialMask() { return maskSpecial; }

	/** Fetch the number of characters this styling applies to */
	public int getCharactersCovered() { return charactersCovered; }
	/** Fetch the TextProps that define this styling */
	public List<TextProp> getTextPropList() { return textPropList; }
	
	/** Fetch the TextProp with this name, or null if it isn't present */
	public TextProp findByName(String textPropName) {
		for(TextProp prop : textPropList) {
			if(prop.getName().equals(textPropName)) {
				return prop;
			}
		}
		return null;
	}
	
	/** Add the TextProp with this name to the list */
	public TextProp addWithName(String name) {
		// Find the base TextProp to base on
		TextProp existing = findByName(name);
		if (existing != null) return existing;
		
		TextProp base = null;
		for (TextProp tp : potentialPropList) {
		    if (tp.getName().equals(name)) {
		        base = tp;
		        break;
		    }
		}
		
		if(base == null) {
			throw new IllegalArgumentException("No TextProp with name " + name + " is defined to add from. "
		        + "Character and paragraphs have their own properties/names.");
		}
		
		// Add a copy of this property, in the right place to the list
		TextProp textProp = base.clone();
		addProp(textProp);
		return textProp;
	}
	
	/**
	 * Add the property at the correct position. Replaces an existing property with the same name.
	 *
	 * @param textProp the property to be added
	 */
	public void addProp(TextProp textProp) {
	    assert(textProp != null);
	    
        int pos = 0;
        boolean found = false;
        for (TextProp curProp : potentialPropList) {
            String potName = curProp.getName();
            if (pos == textPropList.size() || potName.equals(textProp.getName())) {
                if (textPropList.size() > pos && potName.equals(textPropList.get(pos).getName())) {
                    // replace existing prop (with same name)
                    textPropList.set(pos, textProp);
                } else {
                    textPropList.add(pos, textProp);
                }
                found = true;
                break;
            }
            
            if (potName.equals(textPropList.get(pos).getName())) {
                pos++;
            }
        }

        if(!found) {
            String err = "TextProp with name " + textProp.getName() + " doesn't belong to this collection.";
            throw new IllegalArgumentException(err);
        }
	}

	/**
	 * For an existing set of text properties, build the list of 
	 *  properties coded for in a given run of properties.
	 * @return the number of bytes that were used encoding the properties list
	 */
	public int buildTextPropList(int containsField, byte[] data, int dataOffset) {
		int bytesPassed = 0;

		// For each possible entry, see if we match the mask
		// If we do, decode that, save it, and shuffle on
		for(TextProp tp : potentialPropList) {
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
				addProp(prop);
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
	public TextPropCollection(int charactersCovered, short reservedField, TextProp[] potentialPropList) {
		this.charactersCovered = charactersCovered;
		this.reservedField = reservedField;
		this.potentialPropList = potentialPropList;
	}

	/**
	 * Create a new collection of text properties (be they paragraph
	 *  or character) for a run of text without any
	 */
	public TextPropCollection(int textSize, TextProp[] potentialPropList) {
	    this(textSize, (short)-1, potentialPropList);
	}
	
    /**
     * Clones the given text properties
     */
	public void copy(TextPropCollection other) {
        this.charactersCovered = other.charactersCovered;
        this.reservedField = other.reservedField;
        this.maskSpecial = other.maskSpecial;
        this.textPropList.clear();
        for (TextProp tp : other.textPropList) {
            TextProp tpCopy = (tp instanceof BitMaskTextProp)
                ? ((BitMaskTextProp)tp).cloneAll()
                : tp.clone();
            addProp(tpCopy);
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
	public void writeOut(OutputStream o) throws IOException {
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
		for (TextProp potProp : potentialPropList) {
    		for(TextProp textProp : textPropList) {
    		    if (!textProp.getName().equals(potProp.getName())) continue;
                int val = textProp.getValue();
                if (textProp instanceof BitMaskTextProp && val == 0
                    && !(textProp instanceof ParagraphFlagsTextProp)
//                    && !(textProp instanceof CharFlagsTextProp)
                ) {
                    // don't add empty properties, as they can't be recognized while reading
                    // strangely this doesn't apply for ParagraphFlagsTextProp in contrast
                    // to the documentation in 2.9.20 TextPFException
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

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("  chars covered: " + getCharactersCovered());
        out.append("  special mask flags: 0x" + HexDump.toHex(getSpecialMask()) + "\n");
        for(TextProp p : getTextPropList()) {
            out.append("    " + p.getName() + " = " + p.getValue() );
            out.append(" (0x" + HexDump.toHex(p.getValue()) + ")\n");
        }

        out.append("  bytes that would be written: \n");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeOut(baos);
            byte[] b = baos.toByteArray();
            out.append(HexDump.dump(b, 0, 0));
        } catch (Exception e ) {
            e.printStackTrace();
        }
        
        return out.toString();
    }
}
