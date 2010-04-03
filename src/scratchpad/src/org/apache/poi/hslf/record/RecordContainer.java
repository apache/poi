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

import org.apache.poi.util.ArrayUtil;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hslf.util.MutableByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Abstract class which all container records will extend. Providers
 *  helpful methods for writing child records out to disk
 *
 * @author Nick Burch
 */

public abstract class RecordContainer extends Record
{
	protected Record[] _children;
	private Boolean changingChildRecordsLock = Boolean.TRUE;

	/**
	 * Return any children
	 */
	public Record[] getChildRecords() { return _children; }

	/**
	 * We're not an atom
	 */
	public boolean isAnAtom() { return false; }


	/* ===============================================================
	 *                   Internal Move Helpers
	 * ===============================================================
	 */

	/**
	 * Finds the location of the given child record
	 */
	private int findChildLocation(Record child) {
		// Synchronized as we don't want things changing
		//  as we're doing our search
		synchronized(changingChildRecordsLock) {
			for(int i=0; i<_children.length; i++) {
				if(_children[i].equals(child)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Adds a child record, at the very end.
	 * @param newChild The child record to add
	 */
	private void appendChild(Record newChild) {
		synchronized(changingChildRecordsLock) {
			// Copy over, and pop the child in at the end
			Record[] nc = new Record[(_children.length + 1)];
			System.arraycopy(_children, 0, nc, 0, _children.length);
			// Switch the arrays
			nc[_children.length] = newChild;
			_children = nc;
		}
	}

	/**
	 * Adds the given new Child Record at the given location,
	 *  shuffling everything from there on down by one
	 * @param newChild
	 * @param position
	 */
	private void addChildAt(Record newChild, int position) {
		synchronized(changingChildRecordsLock) {
			// Firstly, have the child added in at the end
			appendChild(newChild);

			// Now, have them moved to the right place
			moveChildRecords( (_children.length-1), position, 1 );
		}
	}

	/**
	 * Moves <i>number</i> child records from <i>oldLoc</i>
	 *  to <i>newLoc</i>. Caller must have the changingChildRecordsLock
	 * @param oldLoc the current location of the records to move
	 * @param newLoc the new location for the records
	 * @param number the number of records to move
	 */
	private void moveChildRecords(int oldLoc, int newLoc, int number) {
		if(oldLoc == newLoc) { return; }
		if(number == 0) { return; }

		// Check that we're not asked to move too many
		if(oldLoc+number > _children.length) {
			throw new IllegalArgumentException("Asked to move more records than there are!");
		}

		// Do the move
		ArrayUtil.arrayMoveWithin(_children, oldLoc, newLoc, number);
	}


	/**
	 * Finds the first child record of the given type,
	 *  or null if none of the child records are of the
	 *  given type. Does not descend.
	 */
	public Record findFirstOfType(long type) {
		for(int i=0; i<_children.length; i++) {
			if(_children[i].getRecordType() == type) {
				return _children[i];
			}
		}
		return null;
	}

    /**
     * Remove a child record from this record container
     *
     * @param ch the child to remove
     * @return the removed record
     */
    public Record removeChild(Record ch) {
        Record rm = null;
        ArrayList<Record> lst = new ArrayList<Record>();
        for(Record r : _children) {
            if(r != ch) lst.add(r);
            else rm = r;
        }
        _children = lst.toArray(new Record[lst.size()]);
        return rm;
    }

    /* ===============================================================
	 *                   External Move Methods
	 * ===============================================================
	 */

	/**
	 * Add a new child record onto a record's list of children.
	 */
	public void appendChildRecord(Record newChild) {
		synchronized(changingChildRecordsLock) {
			appendChild(newChild);
		}
	}

	/**
	 * Adds the given Child Record after the supplied record
	 * @param newChild
	 * @param after
	 */
	public void addChildAfter(Record newChild, Record after) {
		synchronized(changingChildRecordsLock) {
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
	 * @param before
	 */
	public void addChildBefore(Record newChild, Record before) {
		synchronized(changingChildRecordsLock) {
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
	 * Moves the given Child Record to before the supplied record
	 */
	public void moveChildBefore(Record child, Record before) {
		moveChildrenBefore(child, 1, before);
	}

	/**
	 * Moves the given Child Records to before the supplied record
	 */
	public void moveChildrenBefore(Record firstChild, int number, Record before) {
		if(number < 1) { return; }

		synchronized(changingChildRecordsLock) {
			// Decide where we're going to put them
			int newLoc = findChildLocation(before);
			if(newLoc == -1) {
				throw new IllegalArgumentException("Asked to move children before another record, but that record wasn't one of our children!");
			}

			// Figure out where they are now
			int oldLoc = findChildLocation(firstChild);
			if(oldLoc == -1) {
				throw new IllegalArgumentException("Asked to move a record that wasn't a child!");
			}

			// Actually move
			moveChildRecords(oldLoc, newLoc, number);
		}
	}

	/**
	 * Moves the given Child Records to after the supplied record
	 */
	public void moveChildrenAfter(Record firstChild, int number, Record after) {
		if(number < 1) { return; }

		synchronized(changingChildRecordsLock) {
			// Decide where we're going to put them
			int newLoc = findChildLocation(after);
			if(newLoc == -1) {
				throw new IllegalArgumentException("Asked to move children before another record, but that record wasn't one of our children!");
			}
			// We actually want after this though
			newLoc++;

			// Figure out where they are now
			int oldLoc = findChildLocation(firstChild);
			if(oldLoc == -1) {
				throw new IllegalArgumentException("Asked to move a record that wasn't a child!");
			}

			// Actually move
			moveChildRecords(oldLoc, newLoc, number);
		}
	}

    /**
     * Set child records.
     *
     * @param records   the new child records
     */
    public void setChildRecord(Record[] records) {
        this._children = records;
    }

	/* ===============================================================
	 *                 External Serialisation Methods
	 * ===============================================================
	 */

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

    /**
     * Find the records that are parent-aware, and tell them who their parent is
     */
    public static void handleParentAwareRecords(RecordContainer br) {
        // Loop over child records, looking for interesting ones
        for (Record record : br.getChildRecords()) {
            // Tell parent aware records of their parent
            if (record instanceof ParentAwareRecord) {
                ((ParentAwareRecord) record).setParentRecord(br);
            }
            // Walk on down for the case of container records
            if (record instanceof RecordContainer) {
                handleParentAwareRecords((RecordContainer)record);
            }
        }
    }


}
