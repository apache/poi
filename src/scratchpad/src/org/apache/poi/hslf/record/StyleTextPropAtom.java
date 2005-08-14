
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hslf.record;

import org.apache.poi.util.LittleEndian;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Vector;

/**
 * A StyleTextPropAtom (type 4001). Holds basic character properties 
 *  (bold, italic, underline, possibly more?) and paragraph properties
 *  (alignment, line spacing etc) for the block of text (TextBytesAtom
 *  or TextCharsAtom) that this record follows.
 * You will find two lists within this class.
 *  1 - Paragraph style list (paragraphStyles)
 *  2 - Character style list (charStyles)
 * Both are lists of TextPropCollections. These define how many characters
 *  the style applies to, and what style elements make up the style (another
 *  list, this time of TextProps). Each TextProp has a value, which somehow
 *  encapsulates a property of the style
 *
 * @author Nick Burch
 */

public class StyleTextPropAtom extends RecordAtom
{
	private byte[] _header;
	private static long _type = 4001l;
	private byte[] reserved;

	private byte[] rawContents; // Holds the contents between write-outs

	/** 
	 * Only set to true once setParentTextSize(int) is called.
	 * Until then, no stylings will have been decoded
	 */
	private boolean initialised = false;

	/** 
	 * The list of all the different paragraph stylings we code for.
	 * Each entry is a TextPropCollection, which tells you how many
	 *  Characters the paragraph covers, and also contains the TextProps
	 *  that actually define the styling of the paragraph.
	 */
	private LinkedList paragraphStyles;
	public LinkedList getParagraphStyles() { return paragraphStyles; }
	/** 
	 * The list of all the different character stylings we code for.
	 * Each entry is a TextPropCollection, which tells you how many
	 *  Characters the character styling covers, and also contains the 
	 *  TextProps that actually define the styling of the characters.
	 */
	private LinkedList charStyles;
	public LinkedList getCharacterStyles() { return charStyles; }

	/** All the different kinds of paragraph properties we might handle */
	public TextProp[] paragraphTextPropTypes = new TextProp[] {
				new BitMaskTextProp(2,  0xF, new String[] {
					"bullet", "bullet.hardfont", 
					"bullet.hardcolor", "bullet.hardsize"}
				),
				new TextProp(2, 0x10, "bullet.font"),
				new TextProp(4, 0x20, "bullet.color"),
				new TextProp(2, 0x40, "bullet.size"),
				new TextProp(2, 0x80, "bullet.char"),
				new TextProp(2, 0x100, "para_unknown_1"),
				new TextProp(2, 0x200, "para_unknown_2"),
				new TextProp(2, 0x400, "para_unknown_3"),
				new TextProp(2, 0x800, "alignment"),
				new TextProp(2, 0x1000, "linespacing"),
				new TextProp(2, 0x2000, "spacebefore"),
				new TextProp(2, 0x4000, "spaceafter"),
				new TextProp(2, 0x8000, "para_unknown_4"),
				new TextProp(2, 0x10000, "para_unknown_5"),
				new TextProp(2, 0xA0000, "para_unknown_6")
	};
	/** All the different kinds of character properties we might handle */
	public TextProp[] characterTextPropTypes = new TextProp[] {
				new CharFlagsTextProp(),
				new TextProp(2, 0x10000, "font.index"),
				new TextProp(2, 0x20000, "font.size"),
				new TextProp(4, 0x40000, "font.color"),
				new TextProp(2, 0x80000, "offset"),
				new TextProp(2, 0x100000, "char_unknown_1"),
				new TextProp(2, 0x200000, "asian_or_complex"),
				new TextProp(2, 0x400000, "char_unknown_2"),
				new TextProp(2, 0x800000, "symbol"),
				new TextProp(2, 0x1000000, "char_unknown_3"),
				new TextProp(2, 0x2000000, "char_unknown_4"),
				new TextProp(2, 0x4000000, "char_unknown_5"),
				new TextProp(2, 0x8000000, "char_unknown_6"),
				new TextProp(2, 0x10000000, "char_unknown_7"),
				new TextProp(2, 0x20000000, "char_unknown_8"),
				new TextProp(2, 0x40000000, "char_unknown_9"),
				new TextProp(2, 0x80000000, "char_unknown_10"),
	};

	
	/* *************** record code follows ********************** */

	/** 
	 * For the Text Style Properties (StyleTextProp) Atom
	 */
	protected StyleTextPropAtom(byte[] source, int start, int len) {
		// Sanity Checking - we're always at least 8+10 bytes long
		if(len < 18) {
			len = 18;
			if(source.length - start < 18) {
				throw new RuntimeException("Not enough data to form a StyleTextPropAtom (min size 18 bytes long) - found " + (source.length - start));
			}
		}

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Save the contents of the atom, until we're asked to go and
		//  decode them (via a call to setParentTextSize(int)
		rawContents = new byte[len-8];
		System.arraycopy(source,start+8,rawContents,0,rawContents.length);
		reserved = new byte[0];

		// Set empty linked lists, ready for when they call setParentTextSize
		paragraphStyles = new LinkedList();
		charStyles = new LinkedList();
	}


	/** 
	 * A new set of text style properties for some text without any.
	 */
	public StyleTextPropAtom(int parentTextSize) {
		_header = new byte[8];
		rawContents = new byte[0];
		reserved = new byte[0];

		// Set our type
		LittleEndian.putInt(_header,2,(short)_type);
		// Our initial size is 10
		LittleEndian.putInt(_header,4,10);

		// Set empty paragraph and character styles
		paragraphStyles = new LinkedList();
		charStyles = new LinkedList();

		TextPropCollection defaultParagraphTextProps = new TextPropCollection(parentTextSize);
		paragraphStyles.add(defaultParagraphTextProps);

		TextPropCollection defaultCharacterTextProps = new TextPropCollection(parentTextSize);
		charStyles.add(defaultCharacterTextProps);

		// Set us as now initialised
		initialised = true;
	}


	/**
	 * We are of type 4001
	 */
	public long getRecordType() { return _type; }


	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// First thing to do is update the raw bytes of the contents, based
		//  on the properties
		updateRawContents();

		// Now ensure that the header size is correct
		int newSize = rawContents.length + reserved.length;
		LittleEndian.putInt(_header,4,newSize);

		// Write out the (new) header
		out.write(_header);

		// Write out the styles
		out.write(rawContents);

		// Write out any extra bits
		out.write(reserved);
	}


	/**
	 * Tell us how much text the parent TextCharsAtom or TextBytesAtom
	 *  contains, so we can go ahead and initialise ourselves.
	 */
	public void setParentTextSize(int size) {
		int pos = 0;
		int textHandled = 0;

		// While we have text in need of paragraph stylings, go ahead and
		// grok the contents as paragraph formatting data
		while(pos < rawContents.length && textHandled < size) {
			// First up, fetch the number of characters this applies to
			int textLen = LittleEndian.getInt(rawContents,pos);
			textHandled += textLen;
			pos += 4;

			// Fetch the 2 byte value that is safe to ignore as 0
			short paraIgn = LittleEndian.getShort(rawContents,pos);
			pos += 2;

			// Grab the 4 byte value that tells us what properties follow
			int paraFlags = LittleEndian.getInt(rawContents,pos);
			pos += 4;

			// Now make sense of those properties
			TextPropCollection thisCollection = new TextPropCollection(textLen, paraIgn);
			int plSize = thisCollection.buildTextPropList(
					paraFlags, paragraphTextPropTypes, rawContents, pos);
			pos += plSize;

			// Save this properties set
			paragraphStyles.add(thisCollection);
		}

		// Now do the character stylings
		textHandled = 0;
		while(pos < rawContents.length && textHandled < size) {
			// First up, fetch the number of characters this applies to
			int textLen = LittleEndian.getInt(rawContents,pos);
			textHandled += textLen;
			pos += 4;

			// There is no 2 byte value
			short no_val = -1;

			// Grab the 4 byte value that tells us what properties follow
			int charFlags = LittleEndian.getInt(rawContents,pos);
			pos += 4;

			// Now make sense of those properties
			TextPropCollection thisCollection = new TextPropCollection(textLen, no_val);
			int chSize = thisCollection.buildTextPropList(
					charFlags, characterTextPropTypes, rawContents, pos);
			pos += chSize;

			// Save this properties set
			charStyles.add(thisCollection);
		}

		// Handle anything left over
		if(pos < rawContents.length) {
			reserved = new byte[rawContents.length-pos];
			System.arraycopy(rawContents,pos,reserved,0,reserved.length);
		}

		initialised = true;
	}


	/**
	 * Updates the cache of the raw contents. Serialised the styles out.
	 */
	private void updateRawContents() throws IOException {
		if(!initialised) {
			// We haven't groked the styles since creation, so just stick
			// with what we found
			return;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// First up, we need to serialise the paragraph properties
		for(int i=0; i<paragraphStyles.size(); i++) {
			TextPropCollection tpc = (TextPropCollection)paragraphStyles.get(i);
			tpc.writeOut(baos);
		}

		// Now, we do the character ones
		for(int i=0; i<charStyles.size(); i++) {
			TextPropCollection tpc = (TextPropCollection)charStyles.get(i);
			tpc.writeOut(baos);
		}

		rawContents	= baos.toByteArray();
	}


/* ************************************************************************ */


	/**
	 * For a given run of characters, holds the properties (which could
	 *  be paragraph properties or character properties).
	 * Used to hold the number of characters affected, the list of active
	 *  properties, and the random reserved field if required.
	 */
	public static class TextPropCollection {
		private int charactersCovered;
		private short reservedField;
		private LinkedList textPropList;

		/** Fetch the number of characters this styling applies to */
		public int getCharactersCovered() { return charactersCovered; }
		/** Fetch the TextProps that define this styling */
		public LinkedList getTextPropList() { return textPropList; }

		/**
		 * Create a new collection of text properties (be they paragraph
		 *  or character) which will be groked via a subsequent call to
		 *  buildTextPropList().
		 */
		public TextPropCollection(int charactersCovered, short reservedField) {
			this.charactersCovered = charactersCovered;
			this.reservedField = reservedField;
			textPropList = new LinkedList();
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
			for(int i=0; i<potentialProperties.length; i++) {
				if((containsField & potentialProperties[i].getMask()) != 0) {
					// Bingo, contained
					TextProp prop = (TextProp)potentialProperties[i].clone();
					int val = 0;
					if(prop.getSize() == 2) {
						val = LittleEndian.getShort(data,dataOffset+bytesPassed);
					} else {
						val = LittleEndian.getInt(data,dataOffset+bytesPassed);
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
		 *  or character) for a run of text without any
		 */
		public TextPropCollection(int textSize) {
			charactersCovered = textSize;
			reservedField = -1;
			textPropList = new LinkedList();
		}

		/**
		 * Writes out to disk the header, and then all the properties
		 */
		private void writeOut(OutputStream o) throws IOException {
			// First goes the number of characters we affect
			writeLittleEndian(charactersCovered,o);

			// Then we have the reserved field if required
			if(reservedField > -1) {
				writeLittleEndian(reservedField,o);
			}

			// The the mask field
			int mask = 0;
			for(int i=0; i<textPropList.size(); i++) {
				TextProp textProp = (TextProp)textPropList.get(i);
				mask += textProp.getMask();
			}
			writeLittleEndian(mask,o);

			// Then the contents of all the properties
			for(int i=0; i<textPropList.size(); i++) {
				TextProp textProp = (TextProp)textPropList.get(i);
				int val = textProp.getValue();
				if(textProp.getSize() == 2) {
					writeLittleEndian((short)val,o);
				} else {
					writeLittleEndian(val,o);
				}
			}
		}
	}


/* ************************************************************************ */


	/** 
	 * Definition of a property of some text, or its paragraph. Defines 
	 * how to find out if it's present (via the mask on the paragraph or 
	 * character "contains" header field), how long the value of it is, 
	 * and how to get and set the value.
	 */
	public static class TextProp implements Cloneable {
		protected int sizeOfDataBlock; // Number of bytes the data part uses
		protected String propName;
		protected int dataValue;
		protected int maskInHeader;

		/** 
		 * Generate the definition of a given type of text property.
		 */
		private TextProp(int sizeOfDataBlock, int maskInHeader, String propName) {
			this.sizeOfDataBlock = sizeOfDataBlock;
			this.maskInHeader = maskInHeader;
			this.propName = propName;
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
		public Object clone(){
			try {
				return super.clone();
			} catch(CloneNotSupportedException e) {
				throw new InternalError(e.getMessage());
			}
		}
	}


	/** 
	 * Definition of a special kind of property of some text, or its 
	 *  paragraph. For these properties, a flag in the "contains" header 
	 *  field tells you the data property family will exist. The value
	 *  of the property is itself a mask, encoding several different
	 *  (but related) properties
	 */
	public static class BitMaskTextProp extends TextProp {
		private String[] subPropNames;
		private int[] subPropMasks;
		private boolean[] subPropMatches;

		/** Fetch the list of the names of the sub properties */
		public String[] getSubPropNames() { return subPropNames; }
		/** Fetch the list of if the sub properties match or not */
		public boolean[] getSubPropMatches() { return subPropMatches; }

		private BitMaskTextProp(int sizeOfDataBlock, int maskInHeader, String[] subPropNames) {
			super(sizeOfDataBlock,maskInHeader,"bitmask");
			this.subPropNames = subPropNames;
			subPropMasks = new int[subPropNames.length];
			subPropMatches = new boolean[subPropNames.length];
		}

		/**
		 * Set the value of the text property, and recompute the sub
		 *  properties based on it
		 */
		public void setValue(int val) { 
			dataValue = val;

			// Figure out the values of the sub properties
			for(int i=0; i< subPropMatches.length; i++) {
				subPropMasks[i] = (1 << i);
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
		}
	}


	/** 
	 * Definition for the common character text property bitset, which
	 *  handles bold/italic/underline etc.
	 */
	public static class CharFlagsTextProp extends BitMaskTextProp {
		public static final int BOLD_IDX = 0;
		public static final int ITALIC_IDX = 1;
		public static final int UNDERLINE_IDX = 2;
		public static final int SHADOW_IDX = 4;
		public static final int STRIKETHROUGH_IDX = 8;
		public static final int RELIEF_IDX = 9;
		public static final int RESET_NUMBERING_IDX = 10;
		public static final int ENABLE_NUMBERING_1_IDX = 11;
		public static final int ENABLE_NUMBERING_2_IDX = 12;

		private CharFlagsTextProp() {
			super(2,0xffff, new String[] {
					"bold",          // 0x0001
					"italic",        // 0x0002
					"underline",     // 0x0004
					"char_unknown_1",// 0x0008
					"shadow",        // 0x0010
					"char_unknown_2",// 0x0020
					"char_unknown_3",// 0x0040
					"char_unknown_4",// 0x0080
					"strikethrough", // 0x0100
					"relief",        // 0x0200
					"reset_numbering",    // 0x0400
					"enable_numbering_1", // 0x0800
					"enable_numbering_2", // 0x1000
				}
			);
		}
	}
}
