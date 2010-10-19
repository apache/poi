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
import java.util.Hashtable;

/**
 * A special (and dangerous) kind of Record Atom that cares about where
 *  it lives on the disk, or who has other Atoms that care about where
 *  this is on the disk.
 *
 * @author Nick Burch
 */

public abstract class PositionDependentRecordAtom extends RecordAtom implements PositionDependentRecord
{
	/** Our location on the disk, as of the last write out */
	protected int myLastOnDiskOffset;

	/** Fetch our location on the disk, as of the last write out */
	public int getLastOnDiskOffset() { return myLastOnDiskOffset; }

	/**
	 * Update the Record's idea of where on disk it lives, after a write out.
	 * Use with care...
	 */
	public void setLastOnDiskOffset(int offset) {
		myLastOnDiskOffset = offset;
	}

	/**
	 * Offer the record the list of records that have changed their
	 *  location as part of the writeout.
	 * Allows records to update their internal pointers to other records
	 *  locations
	 */
	public abstract void updateOtherRecordReferences(Hashtable<Integer,Integer> oldToNewReferencesLookup);
}
