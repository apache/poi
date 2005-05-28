
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
        


package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.*;
import org.apache.poi.util.StringUtil;

/**
 * This class represents a run of text in a powerpoint document. That
 *  run could be text on a sheet, or text in a note.
 *  It is only a very basic class for now
 *
 * @author Nick Burch
 */

public class TextRun
{
	private TextHeaderAtom _headerAtom;
	private TextBytesAtom  _byteAtom;
	private TextCharsAtom  _charAtom;
	private boolean _isUnicode;

	/**
	* Constructs a Text Run from a Unicode text block
	*
	* @param tha the TextHeaderAtom that defines what's what
	* @param tca the TextCharsAtom containing the text
	*/
	public TextRun(TextHeaderAtom tha, TextCharsAtom tca) {
		_headerAtom = tha;
		_charAtom = tca;
		_isUnicode = true;
	}

	/**
	* Constructs a Text Run from a Ascii text block
	*
	* @param tha the TextHeaderAtom that defines what's what
	* @param tba the TextBytesAtom containing the text
	*/
	public TextRun(TextHeaderAtom tha, TextBytesAtom tba) {
		_headerAtom = tha;
		_byteAtom = tba;
		_isUnicode = false;
	}


	// Accesser methods follow

	/**
	 * Returns the text content of the run, which has been made safe
	 * for printing and other use.
	 */
	public String getText() {
		String rawText = getRawText();

		// PowerPoint seems to store files with \r as the line break
		// The messes things up on everything but a Mac, so translate
		//  them to \n
		String text = rawText.replace('\r','\n');
		return text;
	}

	/**
	* Returns the raw text content of the run. This hasn't had any
	*  changes applied to it, and so is probably unlikely to print
	*  out nicely.
	*/
	public String getRawText() {
		if(_isUnicode) {
			return _charAtom.getText();
		} else {
			return _byteAtom.getText();
		}
	}

	/**
	 * Changes the text. Chance are, this won't work just yet, because
	 *  we also need to update some other bits of the powerpoint file
	 *  to match the change in the Text Atom, especially byte offsets
	 */
	public void setText(String s) {
		// If size changed, warn
		if(s.length() != getText().length()) {
			System.err.println("Warning: Your powerpoint file is probably no longer readable by powerpoint, as the text run has changed size!");
		}

		if(_isUnicode) {
			// The atom can safely convert to unicode
			_charAtom.setText(s);
		} else {
			// Will it fit in a 8 bit atom?
			boolean hasMultibyte = StringUtil.hasMultibyte(s);
			if(! hasMultibyte) {
				// Fine to go into 8 bit atom
				byte[] text = new byte[s.length()];
				StringUtil.putCompressedUnicode(s,text,0);
				_byteAtom.setText(text);
			} else {
				throw new RuntimeException("Setting of unicode text is currently only possible for Text Runs that are Unicode in the file, sorry. For now, please convert that text to us-ascii and re-try it");
			}
		}
		
	}

	/**
	* Returns the type of the text, from the TextHeaderAtom.
	* Possible values can be seen from TextHeaderAtom
	* @see org.apache.poi.hslf.record.TextHeaderAtom
	*/
	public int getRunType() { 
		return _headerAtom.getTextType();
	}

	/**
	* Changes the type of the text. Values should be taken
	*  from TextHeaderAtom. No checking is done to ensure you
	*  set this to a valid value!
	* @see org.apache.poi.hslf.record.TextHeaderAtom
	*/
	public void setRunType(int type) {
		_headerAtom.setTextType(type);
	}
} 
