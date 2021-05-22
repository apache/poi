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

package org.apache.poi.hslf.model.textproperties;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hslf.record.TxMasterStyleAtom;
import org.apache.poi.util.GenericRecordUtil;

/** 
 * Definition of the indent level of some text. Defines how many
 *  characters it applies to, and what indent level they share.
 * 
 * This is defined by the slightly confusingly named MasterTextPropRun 
 */
public class IndentProp implements GenericRecord {
    private int charactersCovered;
    private short indentLevel;

    /** 
     * Generate the definition of a given text indent
     */
    public IndentProp(int charactersCovered, short indentLevel) {
        this.charactersCovered = charactersCovered;
        this.indentLevel = indentLevel;
    }

    /** Fetch the number of characters this styling applies to */
    public int getCharactersCovered() { return charactersCovered; }
    
    public int getIndentLevel() {
        return indentLevel;
    }
    
    /**
     * Sets the indent level, which can be between 0 and 4
     */
    public void setIndentLevel(int indentLevel) {
        if (indentLevel >= TxMasterStyleAtom.MAX_INDENT ||
            indentLevel < 0) {
            throw new IllegalArgumentException("Indent must be between 0 and 4");
        }
        this.indentLevel = (short)indentLevel;
    }

    /**
     * Update the size of the text that this set of properties
     *  applies to 
     */
    public void updateTextSize(int textSize) {
        charactersCovered = textSize;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "charactersCovered", this::getCharactersCovered,
            "indentLevel", this::getIndentLevel
        );
    }
}