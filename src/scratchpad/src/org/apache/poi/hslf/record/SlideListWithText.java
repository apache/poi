
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
import org.apache.poi.hslf.model.Sheet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * These are tricky beasts. They contain the text of potentially
 *  many (normal) slides. They are made up of several sets of
 *  - SlidePersistAtom
 *  - TextHeaderAtom
 *  - TextBytesAtom / TextCharsAtom
 *  - StyleTextPropAtom (optional)
 *  - TextSpecInfoAtom (optional)
 *  - InteractiveInfo (optional)
 *  - TxInteractiveInfoAtom (optional)
 * and then the next SlidePersistAtom.
 *
 * Eventually, Slides will find the blocks that interest them from all
 *  the SlideListWithText entries, and refere to them
 *
 * For now, we scan through looking for interesting bits, then creating
 *  the helpful Sheet from model for them
 *
 * @author Nick Burch
 */

// For now, pretend to be an atom
public class SlideListWithText extends RecordContainer
{
	private byte[] _header;
	private static long _type = 4080;

	private SlideAtomsSet[] slideAtomsSets;

	/** 
	 * Create a new holder for slide records
	 */
	protected SlideListWithText(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);	

		// Group our children together into SlideAtomsSets
		// That way, model layer code can just grab the sets to use, 
		//  without having to try to match the children together
		Vector sets = new Vector();
		for(int i=0; i<_children.length; i++) {
			if(_children[i] instanceof SlidePersistAtom) {
				// Find where the next SlidePersistAtom is
				int endPos = i+1;
				while(endPos < _children.length && !(_children[endPos] instanceof SlidePersistAtom)) {
					endPos += 1;
				}

				// Now, if not empty, create a SlideAtomsSets
				int clen = endPos - i - 1;
				if(clen == 0) { continue; }
				Record[] spaChildren = new Record[clen];
				System.arraycopy(_children,i+1,spaChildren,0,clen);
				SlideAtomsSet set = new SlideAtomsSet((SlidePersistAtom)_children[i],spaChildren);
				sets.add(set);

				// Wind on
				i += clen;
			}
		}

		// Turn the vector into an array
		slideAtomsSets = new SlideAtomsSet[sets.size()];
		for(int i=0; i<slideAtomsSets.length; i++) {
			slideAtomsSets[i] = (SlideAtomsSet)sets.get(i);
		}
	}


	/**
	 * Get access to the SlideAtomsSets of the children of this record
	 */
	public SlideAtomsSet[] getSlideAtomsSets() { return slideAtomsSets; }

	/**
	 * Return the value we were given at creation
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}


	/** 
	 * Inner class to wrap up a matching set of records that hold the
	 *  text for a given sheet. Contains the leading SlidePersistAtom,
	 *  and all of the records until the next SlidePersistAtom. This 
	 *  includes sets of TextHeaderAtom and TextBytesAtom/TextCharsAtom,
	 *  along with some others.
	 */
	public class SlideAtomsSet {
		private SlidePersistAtom slidePersistAtom;
		private Record[] slideRecords;

		/** Get the SlidePersistAtom, which gives details on the Slide this text is associated with */
		public SlidePersistAtom getSlidePersistAtom() { return slidePersistAtom; }
		/** Get the Text related records for this slide */
		public Record[] getSlideRecords() { return slideRecords; }

		/** Create one to hold the Records for one Slide's text */
		public SlideAtomsSet(SlidePersistAtom s, Record[] r) {
			slidePersistAtom = s;
			slideRecords = r;
		}
	}
}
