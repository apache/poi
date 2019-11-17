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
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;


/**
 * Test <code>TestTxMasterStyleAtom</code> record.
 * Check master style for the empty ppt which is created
 * by the default constructor of <code>SlideShow</code>
 *
 * @author Yegor Kozlov
 */
public final class TestTxMasterStyleAtom extends TestCase {
    protected HSLFSlideShow _ppt;

    @Override
    public void setUp() {
        _ppt = new HSLFSlideShow();
    }

    public void testDefaultStyles()  {
        TxMasterStyleAtom[] txmaster = getMasterStyles();
        for (final TxMasterStyleAtom atom : txmaster) {
            final int txtype = atom.getTextType();
            switch (TextPlaceholder.fromNativeId(txtype)){
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
                    break;
                case CENTER_TITLE:
                    break;
                case HALF_BODY:
                    break;
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
        assertEquals("TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE " +
                "must contain styles for 5 indentation levels", 5, prstyles.size());
        assertEquals("TxMasterStyleAtom for TextHeaderAtom.BODY_TYPE " +
                "must contain styles for 5 indentation levels", 5, chstyles.size());

        //paragraph styles
        props = prstyles.get(0);

        prop = props.findByName("alignment");
        assertEquals(0, prop.getValue());


        for (int i = 0; i < prstyles.size(); i++) {
            assertNotNull("text.offset is null for indentation level " + i, prstyles.get(i).findByName("text.offset"));
            assertNotNull("bullet.offset is null for indentation level " + i, prstyles.get(i).findByName("bullet.offset"));
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
    protected TxMasterStyleAtom[] getMasterStyles(){
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
                assertEquals("MainMaster must contain 7 TxMasterStyleAtoms ", 7, cnt);
            } else if(coreRec.getRecordType() == RecordTypes.Document.typeID){
                TxMasterStyleAtom txstyle = null;
                Document doc = (Document)coreRec;
                Record[] rec = doc.getEnvironment().getChildRecords();
                for (final Record atom : rec) {
                    if (atom instanceof TxMasterStyleAtom) {
                        if (txstyle != null)  fail("Document.Environment must contain 1 TxMasterStyleAtom");
                        txstyle = (TxMasterStyleAtom)atom;
                    }
                }
                if (txstyle == null) {
                    throw new AssertionFailedError("TxMasterStyleAtom not found in Document.Environment");
                }

                assertEquals("Document.Environment must contain TxMasterStyleAtom  with type=TextHeaderAtom.OTHER_TYPE",
                        TextPlaceholder.OTHER.nativeId, txstyle.getTextType());
                lst.add(txstyle);
            }
        }

        return lst.toArray(new TxMasterStyleAtom[0]);
    }
}
