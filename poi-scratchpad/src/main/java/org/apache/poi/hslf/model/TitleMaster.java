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
import org.apache.poi.hslf.record.*;

/**
 * Title masters define the design template for slides with a Title Slide layout.
 *
 * @author Yegor Kozlov
 */
public final class TitleMaster extends MasterSheet {
    private TextRun[] _runs;

    /**
     * Constructs a TitleMaster
     *
     */
    public TitleMaster(org.apache.poi.hslf.record.Slide record, int sheetNo) {
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
     * Delegate the call to the underlying slide master.
     */
    public TextProp getStyleAttribute(int txtype, int level, String name, boolean isCharacter) {
        MasterSheet master = getMasterSheet();
        return master == null ? null : master.getStyleAttribute(txtype, level, name, isCharacter);
    }

    /**
     * Returns the slide master for this title master.
     */
    public MasterSheet getMasterSheet(){
        SlideMaster[] master = getSlideShow().getSlidesMasters();
        SlideAtom sa = ((org.apache.poi.hslf.record.Slide)getSheetContainer()).getSlideAtom();
        int masterId = sa.getMasterID();
        for (int i = 0; i < master.length; i++) {
            if (masterId == master[i]._getSheetNumber()) return master[i];
        }
        return null;
    }
}
