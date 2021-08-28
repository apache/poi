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

package org.apache.poi.hssf.usermodel;

import static org.apache.poi.hssf.record.CFRuleBase.parseFormula;
import static org.apache.poi.hssf.usermodel.HSSFConditionalFormattingRule.toFormulaString;

import org.apache.poi.hssf.record.cf.Threshold;

/**
 * High level representation for Icon / Multi-State / Databar /
 *  Colour Scale change thresholds
 */
public final class HSSFConditionalFormattingThreshold implements org.apache.poi.ss.usermodel.ConditionalFormattingThreshold {
    private final Threshold threshold;
    private final HSSFSheet sheet;
    private final HSSFWorkbook workbook;

    HSSFConditionalFormattingThreshold(Threshold threshold, HSSFSheet sheet) {
        this.threshold = threshold;
        this.sheet = sheet;
        this.workbook = sheet.getWorkbook();
    }

    Threshold getThreshold() {
        return threshold;
    }

    @Override
    public RangeType getRangeType() {
        return RangeType.byId(threshold.getType());
    }
    @Override
    public void setRangeType(RangeType type) {
        threshold.setType((byte)type.id);
    }

    @Override
    public String getFormula() {
        return toFormulaString(threshold.getParsedExpression(), workbook);
    }
    @Override
    public void setFormula(String formula) {
        threshold.setParsedExpression(parseFormula(formula, sheet));
    }

    @Override
    public Double getValue() {
        return threshold.getValue();
    }

    @Override
    public void setValue(Double value) {
        threshold.setValue(value);
    }
}
