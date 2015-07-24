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
 *  properties, and the indent level if required.
 */
public class TextPropCollection {
    /** All the different kinds of paragraph properties we might handle */
    public static final TextProp[] paragraphTextPropTypes = {
        // TextProp order is according to 2.9.20 TextPFException,
        // bitmask order can be different
        new ParagraphFlagsTextProp(),
        new TextProp(2, 0x80, "bullet.char"),
        new TextProp(2, 0x10, "bullet.font"),
        new TextProp(2, 0x40, "bullet.size"),
        new TextProp(4, 0x20, "bullet.color"),
        new TextAlignmentProp(),
        new TextProp(2, 0x1000, "linespacing"),
        new TextProp(2, 0x2000, "spacebefore"),
        new TextProp(2, 0x4000, "spaceafter"),
        new TextProp(2, 0x100, "text.offset"), // left margin
        // 0x200 - Undefined and MUST be ignored
        new TextProp(2, 0x400, "bullet.offset"), // indent
        new TextProp(2, 0x8000, "defaultTabSize"),
        new TabStopPropCollection(), // tabstops size is variable!
        new FontAlignmentProp(),
        new WrapFlagsTextProp(),
        new TextProp(2, 0x200000, "textDirection"),
        // 0x400000 MUST be zero and MUST be ignored
        new TextProp(0, 0x800000, "bullet.blip"), // TODO: check size
        new TextProp(0, 0x1000000, "bullet.scheme"), // TODO: check size
        new TextProp(0, 0x2000000, "hasBulletScheme"), // TODO: check size
        // 0xFC000000 MUST be zero and MUST be ignored
    };
    
    /** All the different kinds of character properties we might handle */
    public static final TextProp[] characterTextPropTypes = new TextProp[] {
        new TextProp(0, 0x100000, "pp10ext"),
        new TextProp(0, 0x1000000, "newAsian.font.index"), // A bit that specifies whether the newEAFontRef field of the TextCFException10 structure that contains this CFMasks exists.
        new TextProp(0, 0x2000000, "cs.font.index"), // A bit that specifies whether the csFontRef field of the TextCFException10 structure that contains this CFMasks exists.
        new TextProp(0, 0x4000000, "pp11ext"), // A bit that specifies whether the pp11ext field of the TextCFException10 structure that contains this CFMasks exists.
        new CharFlagsTextProp(),
        new TextProp(2, 0x10000, "font.index"),
        new TextProp(2, 0x200000, "asian.font.index"),
        new TextProp(2, 0x400000, "ansi.font.index"),
        new TextProp(2, 0x800000, "symbol.font.index"),
        new TextProp(2, 0x20000, "font.size"),
        new TextProp(4, 0x40000, "font.color"),
        new TextProp(2, 0x80000, "superscript")
    };

    public enum TextPropType {
        paragraph, character;
    }
    
    private int charactersCovered;
	
    // indentLevel is only valid for paragraph collection
    // if it's set to -1, it must be omitted - see 2.9.36 TextMasterStyleLevel
    private short indentLevel = 0;
	private final List<TextProp> textPropList = new ArrayList<TextProp>();
    private int maskSpecial = 0;
    private final TextPropType textPropType;
    
    /**
     * Create a new collection of text properties (be they paragraph
     *  or character) which will be groked via a subsequent call to
     *  buildTextPropList().
     */
    public TextPropCollection(int charactersCovered, TextPropType textPropType) {
        this.charactersCovered = charactersCovered;
        this.textPropType = textPropType;
    }

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

	public TextProp removeByName(String name) {
	    Iterator<TextProp> iter = textPropList.iterator();
	    TextProp tp = null;
	    while (iter.hasNext()) {
	        tp = iter.next();
	        if (tp.getName().equals(name)){
	            iter.remove();
	            break;
	        }
	    }
	    return tp;
	}
	
	/** Add the TextProp with this name to the list */
	public TextProp addWithName(String name) {
		// Find the base TextProp to base on
		TextProp existing = findByName(name);
		if (existing != null) return existing;
		
		TextProp base = null;
		for (TextProp tp : getPotentialProperties()) {
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

	public TextPropType getTextPropType() {
	    return textPropType;
	}
	
	private TextProp[] getPotentialProperties() {
	    return (textPropType == TextPropType.paragraph) ? paragraphTextPropTypes : characterTextPropTypes;
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
        for (TextProp curProp : getPotentialProperties()) {
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
		for(TextProp tp : getPotentialProperties()) {
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
				
				if (prop instanceof BitMaskTextProp) {
				    ((BitMaskTextProp)prop).setValueWithMask(val, containsField);
				} else {
				    prop.setValue(val);
				}
				bytesPassed += prop.getSize();
				addProp(prop);
			}
		}

		// Return how many bytes were used
		return bytesPassed;
	}

    /**
     * Clones the given text properties
     */
	public void copy(TextPropCollection other) {
	    if (this == other) return;
        this.charactersCovered = other.charactersCovered;
        this.indentLevel = other.indentLevel;
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

		// Then we have the indentLevel field if it's a paragraph collection
		if (textPropType == TextPropType.paragraph && indentLevel > -1) {
			StyleTextPropAtom.writeLittleEndian(indentLevel, o);
		}

		// Then the mask field
		int mask = maskSpecial;
		for (TextProp textProp : textPropList) {
            mask |= textProp.getWriteMask();
        }
		StyleTextPropAtom.writeLittleEndian(mask,o);

		// Then the contents of all the properties
		for (TextProp potProp : getPotentialProperties()) {
    		for(TextProp textProp : textPropList) {
    		    if (!textProp.getName().equals(potProp.getName())) continue;
                int val = textProp.getValue();
                if (textProp instanceof BitMaskTextProp && textProp.getWriteMask() == 0) {
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

    public short getIndentLevel(){
        return indentLevel;
    }

    public void setIndentLevel(short indentLevel) {
        if (textPropType == TextPropType.character) {
            throw new RuntimeException("trying to set an indent on a character collection.");
        }
        this.indentLevel = indentLevel;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + charactersCovered;
        result = prime * result + maskSpecial;
        result = prime * result + indentLevel;
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
        if (o.maskSpecial != this.maskSpecial || o.indentLevel != this.indentLevel) {
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
        if (textPropType == TextPropType.paragraph) {
            out.append("  indent level: "+getIndentLevel()+"\n");
        }
        for(TextProp p : getTextPropList()) {
            out.append("    " + p.getName() + " = " + p.getValue() );
            out.append(" (0x" + HexDump.toHex(p.getValue()) + ")\n");
            if (p instanceof BitMaskTextProp) {
                BitMaskTextProp bm = (BitMaskTextProp)p;
                int i = 0;
                for (String s : bm.getSubPropNames()) {
                    if (bm.getSubPropMatches()[i]) {
                        out.append("          " + s + " = " + bm.getSubValue(i) + "\n");
                    }
                    i++;
                }
            }
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
