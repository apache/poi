
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

import org.apache.poi.ddf.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

/**
 * These are actually wrappers onto Escher drawings. Make use of
 *  the DDF classes to do useful things with them.
 * For now, creates a tree of the Escher records, and then creates any
 *  PowerPoint (hslf) records found within the EscherTextboxRecord 
 *  (msofbtClientTextbox) records.
 * Also provides easy access to the EscherTextboxRecords, so that their
 *  text may be extracted and used in Sheets
 *
 * @author Nick Burch
 */

// For now, pretending to be an atom. Might not always be, but that
//  would require a wrapping class
public class PPDrawing extends RecordAtom
{
	private byte[] _header;
	private long _type;

	private EscherRecord[] childRecords;
	private EscherTextboxWrapper[] textboxWrappers;


	/**
	 * Get access to the underlying Escher Records
	 */
	public EscherRecord[] getEscherRecords() { return childRecords; }

	/**
	 * Get access to the atoms inside Textboxes
	 */
	public EscherTextboxWrapper[] getTextboxWrappers() { return textboxWrappers; }


	/* ******************** record stuff follows ********************** */

	/** 
	 * Sets everything up, groks the escher etc
	 */
	protected PPDrawing(byte[] source, int start, int len) {
		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Get the type
		_type = LittleEndian.getUShort(_header,2);

		// Get the contents for now
		byte[] contents = new byte[len];
		System.arraycopy(source,start,contents,0,len);


		// Build up a tree of Escher records contained within
		DefaultEscherRecordFactory erf = new DefaultEscherRecordFactory();
		Vector escherChildren = new Vector();
		findEscherChildren(erf,contents,8,len-8,escherChildren);

		childRecords = new EscherRecord[escherChildren.size()];
		for(int i=0; i<childRecords.length; i++) {
			childRecords[i] = (EscherRecord)escherChildren.get(i);
		}

		// Find and EscherTextboxRecord's, and wrap them up
		Vector textboxes = new Vector();
		findEscherTextboxRecord(childRecords, textboxes);
		textboxWrappers = new EscherTextboxWrapper[textboxes.size()];
		for(int i=0; i<textboxWrappers.length; i++) {
			textboxWrappers[i] = (EscherTextboxWrapper)textboxes.get(i);
		}
	}

	/** 
	 * Tree walking way of finding Escher Child Records
	 */
	private void findEscherChildren(DefaultEscherRecordFactory erf, byte[] source, int startPos, int lenToGo, Vector found) {
		// Find the record
		EscherRecord r = erf.createRecord(source,startPos);
		// Fill it in
		r.fillFields( source, startPos, erf );
		// Save it
		found.add(r);

		// Wind on
		int size = r.getRecordSize();
		if(size < 8) {
			System.err.println("Hit short DDF record at " + startPos + " - " + size);
		}
		startPos += size;
		lenToGo -= size;
		if(lenToGo >= 8) {
			findEscherChildren(erf, source, startPos, lenToGo, found);
		}
	}

	/** 
	 * Look for EscherTextboxRecords
	 */
	private void findEscherTextboxRecord(EscherRecord[] toSearch, Vector found) {
		for(int i=0; i<toSearch.length; i++) {
			if(toSearch[i] instanceof EscherTextboxRecord) {
				EscherTextboxRecord tbr = (EscherTextboxRecord)toSearch[i];
				EscherTextboxWrapper w = new EscherTextboxWrapper(tbr);
				found.add(w);
			} else {
				// If it has children, walk them
				if(toSearch[i].isContainerRecord()) {
					List childrenL = toSearch[i].getChildRecords();
					EscherRecord[] children = new EscherRecord[childrenL.size()];
					for(int j=0; j< children.length; j++) {
						children[j] = (EscherRecord)childrenL.get(j);
					}
					findEscherTextboxRecord(children,found);
				}
			}
		}
	}

	/**
	 * We are type 1036
	 */
	public long getRecordType() { return _type; }

	/** 
	 * We're pretending to be an atom, so return null
	 */
	public Record[] getChildRecords() { return null; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 * Walks the escher layer to get the contents
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Ensure the escher layer reflects the text changes
		for(int i=0; i<textboxWrappers.length; i++) {
			textboxWrappers[i].writeOut(null);
		}

		// Find the new size of the escher children;
		int newSize = 0;
		for(int i=0; i<childRecords.length; i++) {
			newSize += childRecords[i].getRecordSize();
		}

		// Update the size (header bytes 5-8)
		LittleEndian.putInt(_header,4,newSize);

		// Write out our header
		out.write(_header);

		// Now grab the children's data
		byte[] b = new byte[newSize];
		int done = 0;
		for(int i=0; i<childRecords.length; i++) {
			int written = childRecords[i].serialize( done, b );
			done += written;
		}

		// Finally, write out the children
		out.write(b);
	}
}
