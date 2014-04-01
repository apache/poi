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
 * Uses record level code to locate SlideListWithText entries.
 * Having found them, it sees if they have any text, and prints out
 *  what it finds.
 */
public final class SLWTTextListing {
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
				Record docRecord = records[i];
				Record[] docChildren = docRecord.getChildRecords();
				for(int j=0; j<docChildren.length; j++) {
					if(docChildren[j] instanceof SlideListWithText) {
						System.out.println("Found SLWT at pos " + j + " in the Document at " + i);
						System.out.println("  Has " + docChildren[j].getChildRecords().length + " children");

						// Grab the SlideAtomSet's, which contain
						//  a SlidePersistAtom and then a bunch of text
						//  + related records
						SlideListWithText slwt = (SlideListWithText)docChildren[j];
						SlideListWithText.SlideAtomsSet[] thisSets = slwt.getSlideAtomsSets();
						System.out.println("  Has " + thisSets.length + " AtomSets in it");

						// Loop over the sets, showing what they contain
						for(int k=0; k<thisSets.length; k++) {
							SlidePersistAtom spa = thisSets[k].getSlidePersistAtom();
							System.out.println("    " + k + " has slide id " + spa.getSlideIdentifier() );
							System.out.println("    " + k + " has ref id " + spa.getRefID() );

							// Loop over the records, printing the text
							Record[] slwtc = thisSets[k].getSlideRecords();
							for(int l=0; l<slwtc.length; l++) {
								String text = null;
								if(slwtc[l] instanceof TextBytesAtom) {
									TextBytesAtom tba = (TextBytesAtom)slwtc[l];
									text = tba.getText();
								}
								if(slwtc[l] instanceof TextCharsAtom) {
									TextCharsAtom tca = (TextCharsAtom)slwtc[l];
									text = tca.getText();
								}

								if(text != null) {
									text = text.replace('\r','\n');
									System.out.println("        ''" + text + "''");
								}
							}
						}
					}
				}
			}
		}
	}
}
