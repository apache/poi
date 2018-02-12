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

package org.apache.poi.hslf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.MainMaster;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.record.TxMasterStyleAtom;
import org.apache.poi.util.Internal;

/**
 * SlideMaster determines the graphics, layout, and formatting for all the slides in a given presentation.
 * It stores information about default font styles, placeholder sizes and positions,
 * background design, and color schemes.
 *
 * @author Yegor Kozlov
 */
public final class HSLFSlideMaster extends HSLFMasterSheet {
    private final List<List<HSLFTextParagraph>> _paragraphs = new ArrayList<>();

    /**
     * all TxMasterStyleAtoms available in this master
     */
    private TxMasterStyleAtom[] _txmaster;

    /**
     * Constructs a SlideMaster from the MainMaster record,
     *
     */
    public HSLFSlideMaster(MainMaster record, int sheetNo) {
        super(record, sheetNo);

        for (List<HSLFTextParagraph> l : HSLFTextParagraph.findTextParagraphs(getPPDrawing(), this)) {
            if (!_paragraphs.contains(l)) {
                _paragraphs.add(l);
            }
        }
    }

    /**
     * Returns an array of all the TextRuns found
     */
    @Override
    public List<List<HSLFTextParagraph>> getTextParagraphs() {
        return _paragraphs;
    }

    /**
     * Returns <code>null</code> since SlideMasters doen't have master sheet.
     */
    @Override
    public HSLFMasterSheet getMasterSheet() {
        return null;
    }

    /**
     * Find the master collection for the given txtype/level/name.
     * This is the "workhorse" which returns the default style attributes.
     * If {@code name = "*"} return the current collection, otherwise if the name is not found
     * in the current selection of txtype/level/name, first try lower levels then try parent types,
     * if it wasn't found there return {@code null}.
     * 
     * @param txtype the {@link TextHeaderAtom} type
     * @param level the indent level of the paragraph, if the level is not defined for the found
     *      collection, the highest existing level will be used
     * @param name the property name, 
     * @param isCharacter if {@code true} use character styles, otherwise use paragraph styles
     */
    @Override
    public TextPropCollection getPropCollection(final int txtype, final int level, final String name, final boolean isCharacter) {
        if (txtype < _txmaster.length) {
            final TxMasterStyleAtom t = _txmaster[txtype];
            final List<TextPropCollection> styles = isCharacter ? t.getCharacterStyles() : t.getParagraphStyles();
            // TODO: what is the reaction for readOnly=false and styles.isEmpty()?
            final int minLevel = Math.min(level, styles.size()-1);
            if ("*".equals(name)) {
                return styles.get(minLevel);
            }
            
            for (int i=minLevel; i >= 0; i--) {
                final TextPropCollection col = styles.get(i);
                final TextProp tp = col.findByName(name);
                if (tp != null) {
                    return col;
                }
            }
        }

        switch (txtype) {
            case TextHeaderAtom.CENTRE_BODY_TYPE:
            case TextHeaderAtom.HALF_BODY_TYPE:
            case TextHeaderAtom.QUARTER_BODY_TYPE:
                return getPropCollection(TextHeaderAtom.BODY_TYPE, level, name, isCharacter);
            case TextHeaderAtom.CENTER_TITLE_TYPE:
                return getPropCollection(TextHeaderAtom.TITLE_TYPE, level, name, isCharacter);
            default:
                return null;
        }
    }

    /**
     * Assign SlideShow for this slide master.
     */
    @Internal
    @Override
    protected void setSlideShow(HSLFSlideShow ss) {
        super.setSlideShow(ss);

        //after the slide show is assigned collect all available style records
        assert (_txmaster == null);
        _txmaster = new TxMasterStyleAtom[9];

        TxMasterStyleAtom txdoc = getSlideShow().getDocumentRecord().getEnvironment().getTxMasterStyleAtom();
        _txmaster[txdoc.getTextType()] = txdoc;

        TxMasterStyleAtom[] txrec = ((MainMaster)getSheetContainer()).getTxMasterStyleAtoms();
        for (int i = 0; i < txrec.length; i++) {
            int txType = txrec[i].getTextType();
            if (txType < _txmaster.length && _txmaster[txType] == null) {
                _txmaster[txType] = txrec[i];
            }
        }

        for (List<HSLFTextParagraph> paras : getTextParagraphs()) {
            for (HSLFTextParagraph htp : paras) {
                int txType = htp.getRunType();
                if (txType >= _txmaster.length || _txmaster[txType] == null) {
                    throw new HSLFException("Master styles not initialized");
                }

                int level = htp.getIndentLevel();

                List<TextPropCollection> charStyles = _txmaster[txType].getCharacterStyles();
                List<TextPropCollection> paragraphStyles = _txmaster[txType].getParagraphStyles();
                if (charStyles == null || paragraphStyles == null ||
                    charStyles.size() <= level || paragraphStyles.size() <= level) {
                    throw new HSLFException("Master styles not initialized");
                }
            }
        }
    }

    @Override
    protected void onAddTextShape(HSLFTextShape shape) {
        List<HSLFTextParagraph> runs = shape.getTextParagraphs();
        _paragraphs.add(runs);
    }

    public TxMasterStyleAtom[] getTxMasterStyleAtoms(){
        return _txmaster;
    }
}
