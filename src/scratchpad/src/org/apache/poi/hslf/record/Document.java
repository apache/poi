
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Master container for Document. There is one of these for every 
 *  slideshow, and it holds lots of definitions, and some summaries.
 *
 * @author Nick Burch
 */

public class Document extends PositionDependentRecordContainer
{
	private byte[] _header;
	private static long _type = 1000;

	// Links to our more interesting children
	private DocumentAtom documentAtom;
	private Environment environment;
	private PPDrawingGroup ppDrawing;
	private SlideListWithText[] slwts;

	/**
	 * Returns the DocumentAtom of this Document
	 */
	public DocumentAtom getDocumentAtom() { return documentAtom; }
	/**
	 * Returns the Environment of this Notes, which lots of
	 *  settings for the document in it
	 */
	public Environment getEnvironment() { return environment; }
	/**
	 * Returns the PPDrawingGroup, which holds an Escher Structure
	 *  that contains information on pictures in the slides.
	 */
	public PPDrawingGroup getPPDrawingGroup() { return ppDrawing; }
	/**
	 * Returns all the SlideListWithTexts that are defined for
	 *  this Document. They hold the text, and some of the text
	 *  properties, which are referred to by the slides.
	 */
	public SlideListWithText[] getSlideListWithTexts() { return slwts; }


	/** 
	 * Set things up, and find our more interesting children
	 */
	protected Document(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);
		
		// Our first one should be a document atom
		if(! (_children[0] instanceof DocumentAtom)) {
			throw new IllegalStateException("The first child of a Document must be a DocumentAtom");
		}
		documentAtom = (DocumentAtom)_children[0];
		
		// Find how many SlideListWithTexts we have
		// Also, grab the Environment record on our way past
		int slwtcount = 0;
		for(int i=1; i<_children.length; i++) {
			if(_children[i] instanceof SlideListWithText) {
				slwtcount++;
			}
			if(_children[i] instanceof Environment) {
				environment = (Environment)_children[i];
			}
			if(_children[i] instanceof PPDrawingGroup) {
				ppDrawing = (PPDrawingGroup)_children[i];
			}
		}
		// Now grab them all
		slwts = new SlideListWithText[slwtcount];
		slwtcount = 0;
		for(int i=1; i<_children.length; i++) {
			if(_children[i] instanceof SlideListWithText) {
				slwts[slwtcount] = (SlideListWithText)_children[i];
				slwtcount++;
			}
		}
	}


	/**
	 * We are of type 1000
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}
}
