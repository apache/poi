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
import org.apache.poi.hslf.record.StyleTextPropAtom.*;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;

import java.util.List;
import java.util.Iterator;

/**
 * SlideMaster determines the graphics, layout, and formatting for all the slides in a given presentation.
 * It stores information about default font styles, placeholder sizes and positions,
 * background design, and color schemes.
 *
 * @author Yegor Kozlov
 */
public class SlideMaster extends MasterSheet {
    private int _refSheetNo;
    private int _sheetNo;
    private MainMaster _master;
    private TextRun[] _runs;
    private Background _background;

    /**
     * all TxMasterStyleAtoms available in this master
     */
    private TxMasterStyleAtom[] _txmaster;

    /**
     * Constructs a SlideMaster from the MainMaster record,
     *
     */
    public SlideMaster(org.apache.poi.hslf.record.MainMaster rec, int slideId) {
        _master = rec;

        // Grab our internal sheet ID
        _refSheetNo = rec.getSheetId();

        // Grab the number of the slide we're for, via the NotesAtom
        _sheetNo = slideId;

        _runs = findTextRuns(_master.getPPDrawing());
    }

    /**
     * Returns an array of all the TextRuns found
     */
    public TextRun[] getTextRuns() {
        return _runs;
    }

    /**
     * Returns the (internal, RefID based) sheet number, as used
     * to in PersistPtr stuff.
     */
    public int _getSheetRefId() {
        return _refSheetNo;
    }

    /**
     * Returns the (internal, SlideIdentifer based) number of the
     * slide we're attached to
     */
    public int _getSheetNumber() {
        return _sheetNo;
    }

    /**
     * Returns the PPDrawing associated with this slide master
     */
    protected PPDrawing getPPDrawing() {
        return _master.getPPDrawing();
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

            TxMasterStyleAtom[] txrec = _master.getTxMasterStyleAtoms();
            for (int i = 0; i < txrec.length; i++) {
                _txmaster[txrec[i].getTextType()] = txrec[i];
            }
        }
    }

    /**
     * Returns the ColorSchemeAtom associated with this slide master
     */
    public ColorSchemeAtom getColorScheme(){
        return _master.getColorScheme();
    }

    /**
     * Returns the background shape for this sheet.
     *
     * @return the background shape for this sheet.
     */
    public Background getBackground(){
        if (_background == null){
            PPDrawing ppdrawing = getPPDrawing();

            EscherContainerRecord dg = (EscherContainerRecord)ppdrawing.getEscherRecords()[0];
            EscherContainerRecord spContainer = null;
            List ch = dg.getChildRecords();

            for (Iterator it = ch.iterator(); it.hasNext();) {
                EscherRecord rec = (EscherRecord)it.next();
                if (rec.getRecordId() == EscherContainerRecord.SP_CONTAINER){
                        spContainer = (EscherContainerRecord)rec;
                        break;
                }
            }
            _background = new Background(spContainer, null);
            _background.setSheet(this);
        }
        return _background;
    }

}
