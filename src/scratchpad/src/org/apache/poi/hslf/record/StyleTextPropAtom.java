
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
 * A StyleTextPropAtom (type 4001). Holds character properties (font type,
 *  font size, colour, bold, italic etc) and paragraph properties
 *  (alignment, line spacing etc) for a block of text
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
		while(cpos <= len-8-10-8) { // Min size is 8, then 8+10 in
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
			cp.add(cs);
		}
		charStyles = new CharacterStyle[cp.size()];
		for(int i=0; i<charStyles.length; i++) {
			charStyles[i] = (CharacterStyle)cp.get(i);
		}

		// Chuck anything that doesn't fit somewhere for safe keeping
		reserved = new byte[len-8-10-cpos];
		System.arraycopy(source,start+8+10+cpos,reserved,0,reserved.length);
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

		/** Return the number of characters covered by these properties */
		public int getCharactersCoveredLength() {
			return styleLen;
		}

		/** Checks to see if the text is bold */
		public boolean isBold() {
			// style1 0x00010000
			if ((style1 & 65536) == 65536) { return true; }
			return false;
		}

		/** Checks to see if the text is italic */
		public boolean isItalic() {
			// style1 0x00020000
			if ((style1 & 131072) == 131072) { return true; }
			return false;
		}

		/** Checks to see if the text is underlined */
		public boolean isUnderlined() {
			// style1 0x00040000
			if ((style1 & 262144) == 262144) { return true; }
			return false;
		}
	}
}
