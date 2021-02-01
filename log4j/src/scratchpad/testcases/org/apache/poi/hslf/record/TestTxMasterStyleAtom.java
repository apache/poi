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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test <code>TestTxMasterStyleAtom</code> record.
 * Check master style for the empty ppt which is created
 * by the default constructor of <code>SlideShow</code>
 */
public final class TestTxMasterStyleAtom {
    private HSLFSlideShow _ppt;

    @BeforeEach
    void setUp() {
        _ppt = new HSLFSlideShow();
    }

    @Test
    void testDefaultStyles()  {
        TxMasterStyleAtom[] txmaster = getMasterStyles();
        for (final TxMasterStyleAtom atom : txmaster) {
            final int txtype = atom.getTextType();
            TextPlaceholder tp = TextPlaceholder.fromNativeId(txtype);
            assertNotNull(tp);
            switch (tp) {
                case TITLE:
                    checkTitleType(atom);
                    break;
                case BODY:
                    checkBodyType(atom);
                    break;
                case NOTES:
                    checkNotesType(atom);
                    break;
                case OTHER:
                    checkOtherType(atom);
                    break;
                case CENTER_BODY:
                case CENTER_TITLE:
                case HALF_BODY:
                case QUARTER_BODY:
                    break;
                default:
                    fail("Unknown text type: " + txtype);
            }

        }
    }



    /**
     * Test styles for type=TextHeaderAtom.TITLE_TYPE
     */
    private void checkTitleType(TxMasterStyleAtom txmaster){
        TextPropCollection props;
        TextProp prop;

        //paragraph styles
        props = txmaster.getParagraphStyles().get(0);

        prop = props.findByName("alignment");
        assertEquals(1, prop.getValue()); //title has center alignment

        //character styles
        props = txmaster.getCharacterStyles().get(0);

        prop = props.findByName("font.color");
        assertEquals(0x3000000, prop.getValue());

        prop = props.findByName("font.index");
        assertEquals(0, prop.getValue());

        prop = props.findByName("font.size");
        assertEquals(44, prop.getValue());

    }

    /**
     * Test styles for type=TextHeaderAtom.BODY_TYPE
     */
    private void checkBodyType(TxMasterStyleAtom txmaster){
        TextPropCollection props;
        TextProp prop;

        List<TextPropCollection> prstyles = txmaster.getParagraphStyles();
        List<TextPropCollection> chstyles = txmaster.getCharacterStyles();
        assertEquals(5, prstyles.size(), "TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE must contain styles for 5 indentation levels");
        assertEquals(5, chstyles.size(), "TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE must contain styles for 5 indentation levels");

        //paragraph styles
        props = prstyles.get(0);

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue());


        for (int i = 0; i < prstyles.size(); i++) {
            assertNotNull(prstyles.get(i).findByName("text.offset"), "text.offset is null for indentation level " + i);
            assertNotNull(prstyles.get(i).findByName("bullet.offset"), "bullet.offset is null for indentation level " + i);
        }

        //character styles
        props = chstyles.get(0);

        prop = props.findByName("font.color");
        assertEquals(0x1000000, prop.getValue());

        prop = props.findByName("font.index");
        assertEquals(0, prop.getValue());

        prop = props.findByName("font.size");
        assertEquals(32, prop.getValue());
    }

    /**
     * Test styles for type=TextHeaderAtom.OTHER_TYPE
     */
    private void checkOtherType(TxMasterStyleAtom txmaster){
        TextPropCollection props;
        TextProp prop;

        //paragraph styles
        props = txmaster.getParagraphStyles().get(0);

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue());

        //character styles
        props = txmaster.getCharacterStyles().get(0);

        prop = props.findByName("font.color");
        assertEquals(0x1000000, prop.getValue());

        prop = props.findByName("font.index");
        assertEquals(0, prop.getValue());

        prop = props.findByName("font.size");
        assertEquals(18, prop.getValue());
    }

    /**
     * Test styles for type=TextHeaderAtom.NOTES_TYPE
     */
    private void checkNotesType(TxMasterStyleAtom txmaster){
        TextPropCollection props;
        TextProp prop;

        //paragraph styles
        props = txmaster.getParagraphStyles().get(0);

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue()); //title has center alignment

        //character styles
        props = txmaster.getCharacterStyles().get(0);

        prop = props.findByName("font.color");
        assertEquals(0x1000000, prop.getValue());

        prop = props.findByName("font.index");
        assertEquals(0, prop.getValue());

        prop = props.findByName("font.size");
        assertEquals(12, prop.getValue());

    }

    /**
     * Collect all TxMasterStyleAtom records contained in the supplied slide show.
     * There must be a TxMasterStyleAtom per each type of text defined in TextHeaderAtom
     */
    private TxMasterStyleAtom[] getMasterStyles(){
        List<TxMasterStyleAtom> lst = new ArrayList<>();

        Record[] coreRecs = _ppt.getMostRecentCoreRecords();
        for (final Record coreRec : coreRecs) {
            if(coreRec.getRecordType() == RecordTypes.MainMaster.typeID){
                Record[] recs = coreRec.getChildRecords();
                int cnt = 0;
                for (final Record rec : recs) {
                    if (rec instanceof TxMasterStyleAtom) {
                        lst.add((TxMasterStyleAtom) rec);
                        cnt++;
                    }
                }
                assertEquals(7, cnt, "MainMaster must contain 7 TxMasterStyleAtoms ");
            } else if(coreRec.getRecordType() == RecordTypes.Document.typeID){
                TxMasterStyleAtom txstyle = null;
                Document doc = (Document)coreRec;
                Record[] rec = doc.getEnvironment().getChildRecords();
                for (final Record atom : rec) {
                    if (atom instanceof TxMasterStyleAtom) {
                        assertNull(txstyle, "Document.Environment must contain 1 TxMasterStyleAtom");
                        txstyle = (TxMasterStyleAtom)atom;
                    }
                }

                assertNotNull(txstyle, "TxMasterStyleAtom not found in Document.Environment");

                assertEquals(TextPlaceholder.OTHER.nativeId, txstyle.getTextType(),
                    "Document.Environment must contain TxMasterStyleAtom  with type=TextHeaderAtom.OTHER_TYPE");
                lst.add(txstyle);
            }
        }

        return lst.toArray(new TxMasterStyleAtom[0]);
    }
}
