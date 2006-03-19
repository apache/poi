
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
import org.apache.poi.hslf.util.MutableByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 * Abstract class which all container records will extend. Providers
 *  helpful methods for writing child records out to disk
 *
 * @author Nick Burch
 */

public abstract class RecordContainer extends Record
{
	protected Record[] _children;
	private Boolean addingChildRecordLock = new Boolean(true);
	
	/** 
	 * Return any children 
	 */
	public Record[] getChildRecords() { return _children; }

	/** 
	 * We're not an atom
	 */
	public boolean isAnAtom() { return false; }

	/**
	 * Add a new child record onto a record's list of children, and 
	 *  return the new list.
	 */
	public Record[] appendChildRecord(Record newChild, Record[] children) {
		Record[] r;
		synchronized(addingChildRecordLock) {
			r = new Record[children.length + 1];
			System.arraycopy(children,0,r,0,children.length);
			r[r.length-1] = newChild;
		}
		return r;
	}
	
	/**
	 * Finds the location of the given child record
	 */
	private int findChildLocation(Record child) {
		// Synchronized as we don't want things changing 
		//  as we're doing our search
		synchronized(addingChildRecordLock) {
			for(int i=0; i<_children.length; i++) {
				if(_children[i].equals(child)) {
					return i;
				}
			}
		}	
		return -1;
	}
	
	/**
	 * Adds the given new Child Record at the given location,
	 *  shuffling everything from there on down by one
	 * @param newChild
	 * @param position
	 */
	private void addChildAt(Record newChild, int position) {
		synchronized(addingChildRecordLock) {
			Record[] newChildren = new Record[_children.length+1];
			// Move over to the new array, shuffling on by one after
			//  the addition point
			for(int i=0; i<_children.length; i++) {
				if(i == position) {
					newChildren[i] = newChild;
				}

				if(i >= position) {
					newChildren[i+1] = _children[i];
				}
				if(i < position) {
					newChildren[i] = _children[i];
				}
			}
			
			// Special case - new record goes at the end
			if(position == _children.length) {
				newChildren[position] = newChild;
			}
			
			// All done, replace our child list
			_children = newChildren;
		}
	}
	
	/**
	 * Adds the given Child Record after the supplied record
	 * @param newChild
	 * @param after
	 */
	public void addChildAfter(Record newChild, Record after) {
		synchronized(addingChildRecordLock) {
			// Decide where we're going to put it
			int loc = findChildLocation(after);
			if(loc == -1) {
				throw new IllegalArgumentException("Asked to add a new child after another record, but that record wasn't one of our children!");
			}
				
			// Add one place after the supplied record
			addChildAt(newChild, loc+1);
		}
	}
	
	/**
	 * Adds the given Child Record before the supplied record
	 * @param newChild
	 * @param after
	 */
	public void addChildBefore(Record newChild, Record before) {
		synchronized(addingChildRecordLock) {
			// Decide where we're going to put it
			int loc = findChildLocation(before);
			if(loc == -1) {
				throw new IllegalArgumentException("Asked to add a new child before another record, but that record wasn't one of our children!");
			}
				
			// Add at the place of the supplied record
			addChildAt(newChild, loc);
		}
	}
	
	/**
	 * Write out our header, and our children.
	 * @param headerA the first byte of the header
	 * @param headerB the second byte of the header
	 * @param type the record type
	 * @param children our child records
	 * @param out the stream to write to
	 */
	public void writeOut(byte headerA, byte headerB, long type, Record[] children, OutputStream out) throws IOException {
		// If we have a mutable output stream, take advantage of that
		if(out instanceof MutableByteArrayOutputStream) {
			MutableByteArrayOutputStream mout = 
				(MutableByteArrayOutputStream)out;

			// Grab current size
			int oldSize = mout.getBytesWritten();

			// Write out our header, less the size
			mout.write(new byte[] {headerA,headerB});
			byte[] typeB = new byte[2];
			LittleEndian.putShort(typeB,(short)type);
			mout.write(typeB);
			mout.write(new byte[4]);

			// Write out the children
			for(int i=0; i<children.length; i++) {
				children[i].writeOut(mout);
			}

			// Update our header with the size
			// Don't forget to knock 8 more off, since we don't include the
			//  header in the size
			int length = mout.getBytesWritten() - oldSize - 8;
			byte[] size = new byte[4];
			LittleEndian.putInt(size,0,length);
			mout.overwrite(size, oldSize+4);
		} else {
			// Going to have to do it a slower way, because we have
			// to update the length come the end

			// Create a ByteArrayOutputStream to hold everything in
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Write out our header, less the size
			baos.write(new byte[] {headerA,headerB});
			byte[] typeB = new byte[2];
			LittleEndian.putShort(typeB,(short)type);
			baos.write(typeB);
			baos.write(new byte[] {0,0,0,0});

			// Write out our children
			for(int i=0; i<children.length; i++) {
				children[i].writeOut(baos);
			}

			// Grab the bytes back
			byte[] toWrite = baos.toByteArray();

			// Update our header with the size
			// Don't forget to knock 8 more off, since we don't include the
			//  header in the size
			LittleEndian.putInt(toWrite,4,(toWrite.length-8));

			// Write out the bytes
			out.write(toWrite);
		}
	}
}
