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

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SlideListWithText;

/**
 * Uses record level code to Documents.
 * Having found them, it sees if they have any SlideListWithTexts,
 *  and reports how many, and what sorts of things they contain
 */
public final class SLWTListing {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}

		HSLFSlideShow ss = new HSLFSlideShow(args[0]);

		// Find the documents, and then their SLWT
		Record[] records = ss.getRecords();
		for(int i=0; i<records.length; i++) {
			if(records[i] instanceof Document) {
				Document doc = (Document)records[i];
				SlideListWithText[] slwts = doc.getSlideListWithTexts();

				System.out.println("Document at " + i + " had " + slwts.length + " SlideListWithTexts");
				if(slwts.length == 0) {
					System.err.println("** Warning: Should have had at least 1! **");
				}
				if(slwts.length > 3) {
					System.err.println("** Warning: Shouldn't have more than 3!");
				}

				// Check the SLWTs contain what we'd expect
				for(int j=0; j<slwts.length; j++) {
					SlideListWithText slwt = slwts[j];
					Record[] children = slwt.getChildRecords();

					System.out.println(" - SLWT at " + j + " had " + children.length + " children:");

					// Should only have SlideAtomSets if the second one
					int numSAS = slwt.getSlideAtomsSets().length;
					if(j == 1) {
						if(numSAS == 0) {
							System.err.println("  ** 2nd SLWT didn't have any SlideAtomSets!");
						} else {
							System.out.println("  - Contains " + numSAS + " SlideAtomSets");
						}
					} else {
						if(numSAS > 0) {
							System.err.println("  ** SLWT " + j + " had " + numSAS + " SlideAtomSets! (expected 0)");
						}
					}

					// Report the first 5 children, to give a flavour
					int upTo = 5;
					if(children.length < 5) { upTo = children.length; }
					for(int k=0; k<upTo; k++) {
						Record r = children[k];
						int typeID = (int)r.getRecordType();
						String typeName = RecordTypes.recordName(typeID);
						System.out.println("   - " + typeID + " (" + typeName + ")");
					}
				}
			}
		}
	}
}
