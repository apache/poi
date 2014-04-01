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

package org.apache.poi.hslf.dev;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.*;


/**
 * Uses record level code to locate Notes and Slide records.
 * Having found them, it asks their SlideAtom or NotesAtom entries
 *  what they are all about. Useful for checking the matching between
 *  Slides, Master Slides and Notes
 */
public final class SlideAndNotesAtomListing {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}

		HSLFSlideShow ss = new HSLFSlideShow(args[0]);
		System.out.println("");

		// Find either Slides or Notes
		Record[] records = ss.getRecords();
		for(int i=0; i<records.length; i++) {
			Record r = records[i];

			// When we find them, print out their IDs
			if(r instanceof Slide) {
				Slide s = (Slide)r;
				SlideAtom sa = s.getSlideAtom();
				System.out.println("Found Slide at " + i);
				System.out.println("  Slide's master ID is " + sa.getMasterID());
				System.out.println("  Slide's notes ID is  " + sa.getNotesID());
				System.out.println("");
			}
			if(r instanceof Notes) {
				Notes n = (Notes)r;
				NotesAtom na = n.getNotesAtom();
				System.out.println("Found Notes at " + i);
				System.out.println("  Notes ID is " + na.getSlideID());
				System.out.println("");
			}
		}
	}
}
