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

package org.apache.poi.hslf.model;

import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * SlideMaster determines the graphics, layout, and formatting for all the slides in a given presentation.
 * It stores information about default font styles, placeholder sizes and positions,
 * background design, and color schemes.
 *
 * @author Yegor Kozlov
 */
public class SlideMaster extends MasterSheet {
    private TextRun[] _runs;

    /**
     * all TxMasterStyleAtoms available in this master
     */
    private TxMasterStyleAtom[] _txmaster;

    /**
     * Constructs a SlideMaster from the MainMaster record,
     *
     */
    public SlideMaster(MainMaster record, int sheetNo) {
        super(record, sheetNo);

        _runs = findTextRuns(getPPDrawing());
        for (int i = 0; i < _runs.length; i++) _runs[i].setSheet(this);
    }

    /**
     * Returns an array of all the TextRuns found
     */
    public TextRun[] getTextRuns() {
        return _runs;
    }

    /**
     * Returns <code>null</code> since SlideMasters doen't have master sheet.
     */
    public MasterSheet getMasterSheet() {
        return null;
    }

    /**
     * Pickup a style attribute from the master.
     * This is the "workhorse" which returns the default style attrubutes. 
     */
    public TextProp getStyleAttribute(int txtype, int level, String name, boolean isCharacter) {

        TextProp prop = null;
        for (int i = level; i >= 0; i--) {
            TextPropCollection[] styles =
                    isCharacter ? _txmaster[txtype].getCharacterStyles() : _txmaster[txtype].getParagraphStyles();
            if (i < styles.length) prop = styles[i].findByName(name);
            if (prop != null) break;
        }
        if (prop == null) {
            if(isCharacter) {
                switch (txtype) {
                    case TextHeaderAtom.CENTRE_BODY_TYPE:
                    case TextHeaderAtom.HALF_BODY_TYPE:
                    case TextHeaderAtom.QUARTER_BODY_TYPE:
                        txtype = TextHeaderAtom.BODY_TYPE;
                        break;
                    case TextHeaderAtom.CENTER_TITLE_TYPE:
                        txtype = TextHeaderAtom.TITLE_TYPE;
                        break;
                    default:
                        return null;
                }
            } else {
                switch (txtype) {
                    case TextHeaderAtom.CENTRE_BODY_TYPE:
                    case TextHeaderAtom.QUARTER_BODY_TYPE:
                        txtype = TextHeaderAtom.BODY_TYPE;
                        break;
                    case TextHeaderAtom.CENTER_TITLE_TYPE:
                        txtype = TextHeaderAtom.TITLE_TYPE;
                        break;
                    default:
                        return null;
                }
                return null;

            }
            prop = getStyleAttribute(txtype, level, name, isCharacter);
        }
        return prop;
    }

    /**
     * Assign SlideShow for this slide master.
     * (Used interanlly)
     */ 
    public void setSlideShow(SlideShow ss) {
        super.setSlideShow(ss);

        //after the slide show is assigned collect all available style records
        if (_txmaster == null) {
            _txmaster = new TxMasterStyleAtom[9];

            TxMasterStyleAtom txdoc = getSlideShow().getDocumentRecord().getEnvironment().getTxMasterStyleAtom();
            _txmaster[txdoc.getTextType()] = txdoc;

            TxMasterStyleAtom[] txrec = ((MainMaster)getSheetContainer()).getTxMasterStyleAtoms();
            for (int i = 0; i < txrec.length; i++) {
                _txmaster[txrec[i].getTextType()] = txrec[i];
            }
        }
    }

    /**
     * Checks if the shape is a placeholder.
     * (placeholders aren't normal shapes, they are visible only in the Edit Master mode)
     *
     *
     * @return true if the shape is a placeholder
     */
    public static boolean isPlaceholder(Shape shape){
        if(!(shape instanceof TextShape)) return false;

        TextShape tx = (TextShape)shape;
        TextRun run = tx.getTextRun();
        if(run == null) return false;

        Record[] records = run._records;
        for (int i = 0; i < records.length; i++) {
            int type = (int)records[i].getRecordType();
            if (type == RecordTypes.OEPlaceholderAtom.typeID ||
                    type == RecordTypes.SlideNumberMCAtom.typeID ||
                    type == RecordTypes.DateTimeMCAtom.typeID ||
                    type == RecordTypes.GenericDateMCAtom.typeID ||
                    type == RecordTypes.FooterMCAtom.typeID ){
                return true;

            }

        }
        return false;
    }
}
