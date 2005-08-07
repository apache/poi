
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
import java.util.Vector;

/**
 * A StyleTextPropAtom (type 4001). Holds basic character properties 
 *  (bold, italic, underline, possibly more?) and paragraph properties
 *  (alignment, line spacing etc) for the block of text (TextBytesAtom
 *  or TextCharsAtom) that this record follows
 *
 * @author Nick Burch
 */

public class StyleTextPropAtom extends RecordAtom
{
	private byte[] _header;
	private static long _type = 4001l;
	private byte[] reserved;

	private short paraStyleLen;
	private int paraStyle1;
	private int paraStyle2;

	private CharacterStyle[] charStyles;

	
	/** Get the number of characters covered by these text styles */
	public int getParagraphStyleCharactersCoveredLength() {
		return paraStyleLen;
	}

	/** Get the individual character stylings for this paragraph */
	public CharacterStyle[] getCharacterStyles() {
		return charStyles;
	}

	/** 
	 * Set the number of characters covered by these text styles.
	 * This must equal the number of characters in the Text record
	 *  that precedes this record, or things won't behave properly
	 */
	public void setParagraphStyleCharactersCoveredLength(int len) {
		paraStyleLen = (short)len;
	}


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

		// Grab the paragraph style stuff
		paraStyleLen = (short)LittleEndian.getShort(source,start+8+0);
		paraStyle1 = (int)LittleEndian.getInt(source,start+8+2);
		paraStyle2 = (int)LittleEndian.getInt(source,start+8+6);

		// While we have the data, grab the character styles
		Vector cp = new Vector();
		int cpos = 0;
		int oldCpos = 0;
		boolean overshot = false;

		// Min size is 8, everything starts 8+10 in to the record
		while((cpos <= len-8-10-8) && !overshot) { 
			CharacterStyle cs;
			
			short clen = LittleEndian.getShort(source,start+8+10+cpos);
			cpos += 2;
			int s1 = (int)LittleEndian.getInt(source,start+8+10+cpos);
			cpos += 4;
			if(s1 == 0) {
				short s3 = LittleEndian.getShort(source,start+8+10+cpos);
				cpos += 2;
				cs = new CharacterStyle(clen,s1,0,s3);
			} else {
				int s2 = (int)LittleEndian.getInt(source,start+8+10+cpos);
				cpos += 4;
				cs = new CharacterStyle(clen,s1,s2,(short)0);
			}

			// Only add if it won't push us past the end of the record
			if(cpos <= (len-8-10)) {
				cp.add(cs);
				oldCpos = cpos;
			} else {
				// Long CharacterStyle, but only enough data for a short one!
				// Rewind back to the end of the last CharacterStyle
				cpos = oldCpos;
				overshot = true;
			}
		}
		charStyles = new CharacterStyle[cp.size()];
		for(int i=0; i<charStyles.length; i++) {
			charStyles[i] = (CharacterStyle)cp.get(i);
		}

		// Chuck anything that doesn't make a complete CharacterStyle 
		// somewhere for safe keeping
		reserved = new byte[len-8-10-cpos];
		System.arraycopy(source,start+8+10+cpos,reserved,0,reserved.length);
	}

	/** 
	 * A new set of text style properties for some text without any
	 */
	public StyleTextPropAtom() {
		_header = new byte[8];
		reserved = new byte[0];
		charStyles = new CharacterStyle[0];

		// Set our type
		LittleEndian.putInt(_header,2,(short)_type);
		// Our initial size is 10
		LittleEndian.putInt(_header,4,10);

		// Blank paragraph style
		paraStyleLen = 0;
		paraStyle1 = 0;
		paraStyle2 = 0;
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
		// Grab the size of the character styles
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int i=0; i<charStyles.length; i++) {
			charStyles[i].writeOut(baos);
		}

		// Figure out the new size
		//    Para->10 + Chars + Reserved
		int newSize = 10 + baos.size() + reserved.length;
		// Update the size (header bytes 5-8)
		LittleEndian.putInt(_header,4,newSize);
	

		// Write out the (new) header
		out.write(_header);

		// Write out the paragraph bits
		writeLittleEndian(paraStyleLen,out);
		writeLittleEndian(paraStyle1,out);
		writeLittleEndian(paraStyle2,out);

		// Write out the character bits
		out.write(baos.toByteArray());

		// Write out any extra bits
		out.write(reserved);
	}


	/**
	 * Class to handle character styles
	 */
	public static class CharacterStyle {
		private short styleLen;
		private int style1;
		private int style2;
		private short style3;

		// style1 0x00010000
		private static final int BOLD_STYLE = 65536;
		// style1 0x00020000
		private static final int ITALIC_STYLE = 131072;
		// style1 0x00040000
		private static final int UNDERLINED_STYLE = 262144;

		/** Create a new Character Style from on-disk data */
		protected CharacterStyle(short len, int s1, int s2, short s3) {
			styleLen = len;
			style1 = s1;
			style2 = s2;
			style3 = s3;
		}

		/** Create a new Character Style for text without one */
		protected CharacterStyle() {
			new CharacterStyle((short)0,0,0,(short)0);
		}

		/** Write the character style out */
		protected void writeOut(OutputStream out) throws IOException {
			writeLittleEndian(styleLen,out);
			writeLittleEndian(style1,out);
			if(style1 == 0) {
				writeLittleEndian(style3,out);
			} else {
				writeLittleEndian(style2,out);
			}
		}


		/** 
		 * Return the number of characters covered by these properties. 
		 * If it's the last CharacterStyle of a StyleTextPropAtom, it 
		 *  will normally be 0, indicating it applies to all the remaining
		 *  text.
		 */
		public int getCharactersCoveredLength() {
			return styleLen;
		}

		/** 
		 * Set the number of characters covered by these properties.
		 * If this is the last CharacterStyle of a StyleTextPropAtom, then
		 *  a value of 0 should be used
		 */
		public void setCharactersCoveredLength(int len) {
			styleLen = (short)len;
		}


		/** Checks to see if the text is bold */
		public boolean isBold() {
			if ((style1 & BOLD_STYLE) == BOLD_STYLE) { return true; }
			return false;
		}

		/** Checks to see if the text is italic */
		public boolean isItalic() {
			if ((style1 & ITALIC_STYLE) == ITALIC_STYLE) { return true; }
			return false;
		}

		/** Checks to see if the text is underlined */
		public boolean isUnderlined() {
			if ((style1 & UNDERLINED_STYLE) == UNDERLINED_STYLE) { return true; }
			return false;
		}

		/** Sets the text to be bold/not bold */
		public void setBold(boolean bold) {
			if(bold == isBold()) { return; }
			if(bold) {
				style1 += BOLD_STYLE;
			} else {
				style1 -= BOLD_STYLE;
			}
		}

		/** Sets the text to be italic/not italic */
		public void setItalic(boolean italic) {
			if(italic == isItalic()) { return; }
			if(italic) {
				style1 += ITALIC_STYLE;
			} else {
				style1 -= ITALIC_STYLE;
			}
		}

		/** Sets the text to be underlined/not underlined */
		public void setUnderlined(boolean underlined) {
			if(underlined == isUnderlined()) { return; }
			if(underlined) {
				style1 += UNDERLINED_STYLE;
			} else {
				style1 -= UNDERLINED_STYLE;
			}
		}
	}
}
