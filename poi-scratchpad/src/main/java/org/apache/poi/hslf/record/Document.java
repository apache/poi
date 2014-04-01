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

package org.apache.poi.hslf.record;

import org.apache.poi.util.POILogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Master container for Document. There is one of these for every
 *  slideshow, and it holds lots of definitions, and some summaries.
 *
 * @author Nick Burch
 */

public final class Document extends PositionDependentRecordContainer
{
	private byte[] _header;
	private static long _type = 1000;

	// Links to our more interesting children
	private DocumentAtom documentAtom;
	private Environment environment;
	private PPDrawingGroup ppDrawing;
	private SlideListWithText[] slwts;
	private ExObjList exObjList; // Can be null

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
	 * Returns the ExObjList, which holds the references to
	 *  external objects used in the slides. This may be null, if
	 *  there are no external references.
	 */
	public ExObjList getExObjList() { return exObjList; }

	/**
	 * Returns all the SlideListWithTexts that are defined for
	 *  this Document. They hold the text, and some of the text
	 *  properties, which are referred to by the slides.
	 * This will normally return an array of size 2 or 3
	 */
	public SlideListWithText[] getSlideListWithTexts() { return slwts; }

    /**
	 * Returns the SlideListWithText that deals with the
	 *  Master Slides
	 */
	public SlideListWithText getMasterSlideListWithText() {
        for (int i = 0; i < slwts.length; i++) {
            if(slwts[i].getInstance() == SlideListWithText.MASTER) {
                return slwts[i];
            }
        }
        return null;
    }

	/**
	 * Returns the SlideListWithText that deals with the
	 *  Slides, or null if there isn't one
	 */
	public SlideListWithText getSlideSlideListWithText() {
        for (int i = 0; i < slwts.length; i++) {
            if(slwts[i].getInstance() == SlideListWithText.SLIDES) {
                return slwts[i];
            }
        }
		return null;
    }
	/**
	 * Returns the SlideListWithText that deals with the
	 *  notes, or null if there isn't one
	 */
	public SlideListWithText getNotesSlideListWithText() {
        for (int i = 0; i < slwts.length; i++) {
            if(slwts[i].getInstance() == SlideListWithText.NOTES) {
                return slwts[i];
            }
        }
		return null;
    }


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
		// Also, grab the Environment and PPDrawing records
		//  on our way past
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
			if(_children[i] instanceof ExObjList) {
				exObjList = (ExObjList)_children[i];
			}
		}

		// You should only every have 1, 2 or 3 SLWTs
		//  (normally it's 2, or 3 if you have notes)
		// Complain if it's not
		if(slwtcount == 0) {
			logger.log(POILogger.WARN, "No SlideListWithText's found - there should normally be at least one!");
		}
		if(slwtcount > 3) {
			logger.log(POILogger.WARN, "Found " + slwtcount + " SlideListWithTexts - normally there should only be three!");
		}

		// Now grab all the SLWTs
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
	 * Adds a new SlideListWithText record, at the appropriate
	 *  point in the child records.
	 */
	public void addSlideListWithText(SlideListWithText slwt) {
		// The new SlideListWithText should go in
		//  just before the EndDocumentRecord
		Record endDoc = _children[_children.length - 1];
		if(endDoc.getRecordType() != RecordTypes.EndDocument.typeID) {
			throw new IllegalStateException("The last child record of a Document should be EndDocument, but it was " + endDoc);
		}

		// Add in the record
		addChildBefore(slwt, endDoc);

		// Updated our cached list of SlideListWithText records
		int newSize = slwts.length + 1;
		SlideListWithText[] nl = new SlideListWithText[newSize];
		System.arraycopy(slwts, 0, nl, 0, slwts.length);
		nl[nl.length-1] = slwt;
		slwts = nl;
	}

    public void removeSlideListWithText(SlideListWithText slwt) {
        ArrayList<SlideListWithText> lst = new ArrayList<SlideListWithText>();
        for(SlideListWithText s : slwts) {
            if(s != slwt) lst.add(s);
            else {
                removeChild(slwt);
            }
        }
        slwts = lst.toArray(new SlideListWithText[lst.size()]);
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
