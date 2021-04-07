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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.ArrayUtil;
import org.apache.poi.util.LittleEndian;

/**
 * Abstract class which all container records will extend. Providers
 *  helpful methods for writing child records out to disk
 */

public abstract class RecordContainer extends Record
{
	protected Record[] _children;

	/**
	 * Return any children
	 */
	@Override
    public org.apache.poi.hslf.record.Record[] getChildRecords() { return _children; }

	/**
	 * We're not an atom
	 */
	@Override
    public boolean isAnAtom() { return false; }


	/* ===============================================================
	 *                   Internal Move Helpers
	 * ===============================================================
	 */

	/**
	 * Finds the location of the given child record
	 */
	private int findChildLocation(Record child) {
	    int i=0;
		for(org.apache.poi.hslf.record.Record r : _children) {
			if (r.equals(child)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Adds a child record, at the very end.
	 * @param newChild The child record to add
	 * @return the position of the added child
	 */
	private int appendChild(Record newChild) {
		// Copy over, and pop the child in at the end
		Record[] nc = Arrays.copyOf(_children, _children.length+1, org.apache.poi.hslf.record.Record[].class);
		// Switch the arrays
		nc[_children.length] = newChild;
		_children = nc;
		return _children.length;
	}

	/**
	 * Adds the given new Child Record at the given location,
	 *  shuffling everything from there on down by one
	 *
	 * @param newChild The record to be added as child-record.
	 * @param position The index where the child should be added, 0-based
	 */
	private void addChildAt(Record newChild, int position) {
		// Firstly, have the child added in at the end
		appendChild(newChild);

		// Now, have them moved to the right place
		moveChildRecords( (_children.length-1), position, 1 );
	}

	/**
	 * Moves {@code number} child records from {@code oldLoc} to {@code newLoc}.
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
	public org.apache.poi.hslf.record.Record findFirstOfType(long type) {
		for (org.apache.poi.hslf.record.Record r : _children) {
			if (r.getRecordType() == type) {
				return r;
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
    public org.apache.poi.hslf.record.Record removeChild(Record ch) {
        org.apache.poi.hslf.record.Record rm = null;
        ArrayList<org.apache.poi.hslf.record.Record> lst = new ArrayList<>();
        for(org.apache.poi.hslf.record.Record r : _children) {
            if(r != ch) {
                lst.add(r);
            } else {
                rm = r;
            }
        }
        _children = lst.toArray(new org.apache.poi.hslf.record.Record[0]);
        return rm;
    }

    /* ===============================================================
	 *                   External Move Methods
	 * ===============================================================
	 */

	/**
	 * Add a new child record onto a record's list of children.
	 *
	 * @param newChild the child record to be added
	 * @return the position of the added child within the list, i.e. the last index
	 */
	public int appendChildRecord(Record newChild) {
		return appendChild(newChild);
	}

	/**
	 * Adds the given Child Record after the supplied record
	 * @param newChild The record to add as new child.
	 * @param after The record after which the given record should be added.
	 * @return the position of the added child within the list
	 */
	public int addChildAfter(Record newChild, Record after) {
		// Decide where we're going to put it
		int loc = findChildLocation(after);
		if(loc == -1) {
			throw new IllegalArgumentException("Asked to add a new child after another record, but that record wasn't one of our children!");
		}

		// Add one place after the supplied record
		addChildAt(newChild, loc+1);
		return loc+1;
	}

	/**
	 * Adds the given Child Record before the supplied record
	 * @param newChild The record to add as new child.
	 * @param before The record before which the given record should be added.
     * @return the position of the added child within the list
	 */
	public int addChildBefore(Record newChild, Record before) {
		// Decide where we're going to put it
		int loc = findChildLocation(before);
		if(loc == -1) {
			throw new IllegalArgumentException("Asked to add a new child before another record, but that record wasn't one of our children!");
		}

		// Add at the place of the supplied record
		addChildAt(newChild, loc);
		return loc;
	}

    /**
     * Set child records.
     *
     * @param records   the new child records
     */
    public void setChildRecord(org.apache.poi.hslf.record.Record[] records) {
        this._children = records.clone();
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
		// Create a ByteArrayOutputStream to hold everything in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Write out our header, less the size
		baos.write(new byte[] {headerA,headerB});
		byte[] typeB = new byte[2];
		LittleEndian.putShort(typeB,0,(short)type);
		baos.write(typeB);
		baos.write(new byte[] {0,0,0,0});

		// Write out our children
		for (Record aChildren : children) {
			aChildren.writeOut(baos);
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

    /**
     * Find the records that are parent-aware, and tell them who their parent is
     */
    public static void handleParentAwareRecords(RecordContainer br) {
        // Loop over child records, looking for interesting ones
        for (org.apache.poi.hslf.record.Record record : br.getChildRecords()) {
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

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return null;
	}
}
