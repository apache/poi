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

import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.SheetContainer;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.sl.usermodel.MasterSheet;

/**
 * The superclass of all master sheets - Slide masters, Notes masters, etc.
 */
public abstract class HSLFMasterSheet extends HSLFSheet implements MasterSheet<HSLFShape,HSLFTextParagraph> {
    public HSLFMasterSheet(SheetContainer container, int sheetNo){
        super(container, sheetNo);
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
    public abstract TextPropCollection getPropCollection(int txtype, int level, String name, boolean isCharacter);

}
