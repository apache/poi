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
 * Uses record level code to locate PPDrawing entries.
 * Having found them, it sees if they have DDF Textbox records, and if so,
 *  searches those for text. Prints out any text it finds
 */
public final class PPDrawingTextListing {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}

		HSLFSlideShow ss = new HSLFSlideShow(args[0]);

		// Find PPDrawings at any second level position
		Record[] records = ss.getRecords();
		for(int i=0; i<records.length; i++) {
			Record[] children = records[i].getChildRecords();
			if(children != null && children.length != 0) {
				for(int j=0; j<children.length; j++) {
					if(children[j] instanceof PPDrawing) {
						System.out.println("Found PPDrawing at " + j + " in top level record " + i + " (" + records[i].getRecordType() + ")" );

						// Look for EscherTextboxWrapper's
						PPDrawing ppd = (PPDrawing)children[j];
						EscherTextboxWrapper[] wrappers = ppd.getTextboxWrappers();
						System.out.println("  Has " + wrappers.length + " textbox wrappers within");

						// Loop over the wrappers, showing what they contain
						for(int k=0; k<wrappers.length; k++) {
							EscherTextboxWrapper tbw = wrappers[k];
							System.out.println("    " + k + " has " + tbw.getChildRecords().length + " PPT atoms within");

							// Loop over the records, printing the text
							Record[] pptatoms = tbw.getChildRecords();
							for(int l=0; l<pptatoms.length; l++) {
								String text = null;
								if(pptatoms[l] instanceof TextBytesAtom) {
									TextBytesAtom tba = (TextBytesAtom)pptatoms[l];
									text = tba.getText();
								}
								if(pptatoms[l] instanceof TextCharsAtom) {
									TextCharsAtom tca = (TextCharsAtom)pptatoms[l];
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
