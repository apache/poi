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

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.usermodel.SlideShow;


/**
 * Test <code>TestTxMasterStyleAtom</code> record.
 * Check master style for the empty ppt which is created
 * by the default constructor of <code>SlideShow</code>
 *
 * @author Yegor Kozlov
 */
public final class TestTxMasterStyleAtom extends TestCase {
    protected SlideShow _ppt;

    public void setUp() {
        _ppt = new SlideShow();
    }

    public void testDefaultStyles()  {
        TxMasterStyleAtom[] txmaster = getMasterStyles();
        for (int i = 0; i < txmaster.length; i++) {
            int txtype = txmaster[i].getTextType();
            switch (txtype){
                case TextHeaderAtom.TITLE_TYPE:
                    checkTitleType(txmaster[i]);
                    break;
                case TextHeaderAtom.BODY_TYPE:
                    checkBodyType(txmaster[i]);
                    break;
                case TextHeaderAtom.NOTES_TYPE:
                    checkNotesType(txmaster[i]);
                    break;
                case TextHeaderAtom.OTHER_TYPE:
                    checkOtherType(txmaster[i]);
                    break;
                case TextHeaderAtom.CENTRE_BODY_TYPE:
                    break;
                case TextHeaderAtom.CENTER_TITLE_TYPE:
                    break;
                case TextHeaderAtom.HALF_BODY_TYPE:
                    break;
                case TextHeaderAtom.QUARTER_BODY_TYPE:
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
        props = txmaster.getParagraphStyles()[0];

        prop = props.findByName("alignment");
        assertEquals(1, prop.getValue()); //title has center alignment

        //character styles
        props = txmaster.getCharacterStyles()[0];

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

        TextPropCollection[] prstyles = txmaster.getParagraphStyles();
        TextPropCollection[] chstyles = txmaster.getCharacterStyles();
        assertEquals("TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE " +
                "must contain styles for 5 indentation levels", 5, prstyles.length);
        assertEquals("TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE " +
                "must contain styles for 5 indentation levels", 5, chstyles.length);

        //paragraph styles
        props = prstyles[0];

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue());


        for (int i = 0; i < prstyles.length; i++) {
            assertNotNull("text.offset is null for indentation level " + i, prstyles[i].findByName("text.offset"));
            assertNotNull("bullet.offset is null for indentation level " + i, prstyles[i].findByName("bullet.offset"));
        }

        //character styles
        props = chstyles[0];

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
        props = txmaster.getParagraphStyles()[0];

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue());

        //character styles
        props = txmaster.getCharacterStyles()[0];

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
        props = txmaster.getParagraphStyles()[0];

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue()); //title has center alignment

        //character styles
        props = txmaster.getCharacterStyles()[0];

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
    protected TxMasterStyleAtom[] getMasterStyles(){
        List<TxMasterStyleAtom> lst = new ArrayList<TxMasterStyleAtom>();

        Record[] coreRecs = _ppt.getMostRecentCoreRecords();
        for (int i = 0; i < coreRecs.length; i++) {
            Record coreRec = coreRecs[i];
            if(coreRec.getRecordType() == RecordTypes.MainMaster.typeID){
                Record[] recs = coreRec.getChildRecords();
                int cnt = 0;
                for (int j = 0; j < recs.length; j++) {
                    Record rec = recs[j];
                    if (rec instanceof TxMasterStyleAtom) {
                        lst.add((TxMasterStyleAtom) rec);
                        cnt++;
                    }
                }
                assertEquals("MainMaster must contain 7 TxMasterStyleAtoms ", 7, cnt);
            } else if(coreRec.getRecordType() == RecordTypes.Document.typeID){
                TxMasterStyleAtom txstyle = null;
                Document doc = (Document)coreRec;
                Record[] rec = doc.getEnvironment().getChildRecords();
                for (int j = 0; j < rec.length; j++) {
                    if (rec[j] instanceof TxMasterStyleAtom) {
                        if (txstyle != null)  fail("Document.Environment must contain 1 TxMasterStyleAtom");
                        txstyle = (TxMasterStyleAtom)rec[j];
                    }
                }
                if (txstyle == null) {
                    throw new AssertionFailedError("TxMasterStyleAtom not found in Document.Environment");
                }

                assertEquals("Document.Environment must contain TxMasterStyleAtom  with type=TextHeaderAtom.OTHER_TYPE",
                        TextHeaderAtom.OTHER_TYPE, txstyle.getTextType());
                lst.add(txstyle);
            }
        }

        return lst.toArray(new TxMasterStyleAtom[lst.size()]);
    }
}
