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

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.model.textproperties.BitMaskTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;

/**
 * Uses record level code to locate StyleTextPropAtom entries.
 * Having found them, it shows the contents
 */
public final class TextStyleListing {
    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            System.err.println("Need to give a filename");
            System.exit(1);
        }

        try (HSLFSlideShowImpl ss = new HSLFSlideShowImpl(args[0])) {
            // Find the documents, and then their SLWT
            Record[] records = ss.getRecords();
            for (org.apache.poi.hslf.record.Record record : records) {
                if (record.getRecordType() == 1000L) {
                    Record[] docChildren = record.getChildRecords();
                    for (Record docChild : docChildren) {
                        if (docChild instanceof SlideListWithText) {
                            Record[] slwtChildren = docChild.getChildRecords();

                            int lastTextLen = -1;
                            for (Record slwtChild : slwtChildren) {
                                if (slwtChild instanceof TextCharsAtom) {
                                    lastTextLen = ((TextCharsAtom) slwtChild).getText().length();
                                }
                                if (slwtChild instanceof TextBytesAtom) {
                                    lastTextLen = ((TextBytesAtom) slwtChild).getText().length();
                                }

                                if (slwtChild instanceof StyleTextPropAtom) {
                                    StyleTextPropAtom stpa = (StyleTextPropAtom) slwtChild;
                                    stpa.setParentTextSize(lastTextLen);
                                    showStyleTextPropAtom(stpa);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void showStyleTextPropAtom(StyleTextPropAtom stpa) {
        System.out.println("\nFound a StyleTextPropAtom");

        List<TextPropCollection> paragraphStyles = stpa.getParagraphStyles();
        System.out.println("Contains " + paragraphStyles.size() + " paragraph styles:");
        for(int i=0; i<paragraphStyles.size(); i++) {
            TextPropCollection tpc = paragraphStyles.get(i);
            System.out.println(" In paragraph styling " + i + ":");
            System.out.println("  Characters covered is " + tpc.getCharactersCovered());
            showTextProps(tpc);
        }

        List<TextPropCollection> charStyles = stpa.getCharacterStyles();
        System.out.println("Contains " + charStyles.size() + " character styles:");
        for(int i=0; i<charStyles.size(); i++) {
            TextPropCollection tpc = charStyles.get(i);
            System.out.println("  In character styling " + i + ":");
            System.out.println("    Characters covered is " + tpc.getCharactersCovered());
            showTextProps(tpc);
        }
    }

    public static void showTextProps(TextPropCollection tpc) {
        List<TextProp> textProps = tpc.getTextPropList();
        System.out.println("    Contains " + textProps.size() + " TextProps");
        for(int i=0; i<textProps.size(); i++) {
            TextProp tp = textProps.get(i);
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
