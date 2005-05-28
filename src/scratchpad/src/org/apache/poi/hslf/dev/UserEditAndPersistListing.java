
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



package org.apache.poi.hslf.dev;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.*;

import java.io.*;

/**
 * Uses record level code to locate UserEditAtom records, and other
 *  persistence related atoms. Tries to match them together, to help
 *  illuminate quite what all the offsets mean
 */
public class UserEditAndPersistListing {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}

		HSLFSlideShow ss = new HSLFSlideShow(args[0]);
		System.out.println("");

		// Find any persist ones first
		Record[] records = ss.getRecords();
		int pos = 0;
		for(int i=0; i<records.length; i++) {
			Record r = records[i];

			if(r.getRecordType() == 6001l) {
				// PersistPtrFullBlock
				System.out.println("Found PersistPtrFullBlock at " + pos + " (" + Integer.toHexString(pos) + ")");
			}
			if(r.getRecordType() == 6002l) {
				// PersistPtrIncrementalBlock
				System.out.println("Found PersistPtrIncrementalBlock at " + pos + " (" + Integer.toHexString(pos) + ")");
			}

			// Increase the position by the on disk size
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			r.writeOut(baos);
			pos += baos.size();
		}

		System.out.println("");

		pos = 0;
		// Now look for UserEditAtoms
		for(int i=0; i<records.length; i++) {
			Record r = records[i];

			if(r instanceof UserEditAtom) {
				UserEditAtom uea = (UserEditAtom)r;
				System.out.println("Found UserEditAtom at " + pos + " (" + Integer.toHexString(pos) + ")");
				System.out.println("  lastUserEditAtomOffset = " + uea.getLastUserEditAtomOffset() );
				System.out.println("  persistPointersOffset  = " + uea.getPersistPointersOffset() );
				System.out.println("  docPersistRef          = " + uea.getDocPersistRef() );
				System.out.println("  maxPersistWritten      = " + uea.getMaxPersistWritten() );
			}

			// Increase the position by the on disk size
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			r.writeOut(baos);
			pos += baos.size();
		}

		System.out.println("");


		// Query the CurrentUserAtom
		CurrentUserAtom cua = ss.getCurrentUserAtom();
		System.out.println("Checking Current User Atom");
		System.out.println("  Thinks the CurrentEditOffset is " + cua.getCurrentEditOffset());
		
		System.out.println("");
	}
}
