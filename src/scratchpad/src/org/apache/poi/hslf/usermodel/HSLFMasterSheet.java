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

import org.apache.poi.hslf.record.SheetContainer;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.sl.usermodel.MasterSheet;

/**
 * The superclass of all master sheets - Slide masters, Notes masters, etc.
 *
 * For now it's empty. When we understand more about masters in ppt we will add the common functionality here.
 *
 * @author Yegor Kozlov
 */
public abstract class HSLFMasterSheet extends HSLFSheet implements MasterSheet<HSLFShape,HSLFTextParagraph> {
    public HSLFMasterSheet(SheetContainer container, int sheetNo){
        super(container, sheetNo);
    }

    /**
     * Pickup a style attribute from the master.
     * This is the "workhorse" which returns the default style attrubutes.
     */
    public abstract TextProp getStyleAttribute(int txtype, int level, String name, boolean isCharacter) ;


    /**
     * Checks if the shape is a placeholder.
     * (placeholders aren't normal shapes, they are visible only in the Edit Master mode)
     *
     *
     * @return true if the shape is a placeholder
     */
    public static boolean isPlaceholder(HSLFShape shape){
        if(!(shape instanceof HSLFTextShape)) return false;

        HSLFTextShape tx = (HSLFTextShape)shape;
        return tx.getPlaceholderAtom() != null;
    }
}
