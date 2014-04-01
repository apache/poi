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
import org.apache.poi.hslf.model.textproperties.BitMaskTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.*;

import java.util.LinkedList;

/**
 * Uses record level code to locate StyleTextPropAtom entries.
 * Having found them, it shows the contents
 */
public final class TextStyleListing {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}

		HSLFSlideShow ss = new HSLFSlideShow(args[0]);

		// Find the documents, and then their SLWT
		Record[] records = ss.getRecords();
		for(int i=0; i<records.length; i++) {
			if(records[i].getRecordType() == 1000l) {
				Record docRecord = records[i];
				Record[] docChildren = docRecord.getChildRecords();
				for(int j=0; j<docChildren.length; j++) {
					if(docChildren[j] instanceof SlideListWithText) {
						Record[] slwtChildren = docChildren[j].getChildRecords();

						int lastTextLen = -1;
						for(int k=0; k<slwtChildren.length; k++) {
							if(slwtChildren[k] instanceof TextCharsAtom) {
								lastTextLen = ((TextCharsAtom)slwtChildren[k]).getText().length();
							}
							if(slwtChildren[k] instanceof TextBytesAtom) {
								lastTextLen = ((TextBytesAtom)slwtChildren[k]).getText().length();
							}

							if(slwtChildren[k] instanceof StyleTextPropAtom) {
								StyleTextPropAtom stpa = (StyleTextPropAtom)slwtChildren[k];
								stpa.setParentTextSize(lastTextLen);
								showStyleTextPropAtom(stpa);
							}
						}
					}
				}
			}
		}
	}

	public static void showStyleTextPropAtom(StyleTextPropAtom stpa) {
		System.out.println("\nFound a StyleTextPropAtom");

		LinkedList paragraphStyles = stpa.getParagraphStyles();
		System.out.println("Contains " + paragraphStyles.size() + " paragraph styles:");
		for(int i=0; i<paragraphStyles.size(); i++) {
			TextPropCollection tpc = (TextPropCollection)paragraphStyles.get(i);
			System.out.println(" In paragraph styling " + i + ":");
			System.out.println("  Characters covered is " + tpc.getCharactersCovered());
			showTextProps(tpc);
		}

		LinkedList charStyles = stpa.getCharacterStyles();
		System.out.println("Contains " + charStyles.size() + " character styles:");
		for(int i=0; i<charStyles.size(); i++) {
			TextPropCollection tpc = (TextPropCollection)charStyles.get(i);
			System.out.println("  In character styling " + i + ":");
			System.out.println("    Characters covered is " + tpc.getCharactersCovered());
			showTextProps(tpc);
		}
	}

	public static void showTextProps(TextPropCollection tpc) {
		LinkedList textProps = tpc.getTextPropList();
		System.out.println("    Contains " + textProps.size() + " TextProps");
		for(int i=0; i<textProps.size(); i++) {
			TextProp tp = (TextProp)textProps.get(i);
			System.out.println("      " + i + " - " + tp.getName());
			System.out.println("          = " + tp.getValue());
			System.out.println("          @ " + tp.getMask());

			if(tp instanceof BitMaskTextProp) {
				BitMaskTextProp bmtp = (BitMaskTextProp)tp;
				String[] subPropNames = bmtp.getSubPropNames();
				boolean[] subPropMatches = bmtp.getSubPropMatches();
				for(int j=0; j<subPropNames.length; j++) {
					System.out.println("            -> " + j + " - " + subPropNames[j]);
					System.out.println("               " + j + " = " + subPropMatches[j]);
				}
			}
		}
	}
}
