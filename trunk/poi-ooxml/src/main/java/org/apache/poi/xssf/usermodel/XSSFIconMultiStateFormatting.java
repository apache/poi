/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfvo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIconSet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STIconSetType;

/**
 * High level representation for Icon / Multi-State Formatting
 *  component of Conditional Formatting settings
 */
public class XSSFIconMultiStateFormatting implements IconMultiStateFormatting {
    CTIconSet _iconset;

    /*package*/ XSSFIconMultiStateFormatting(CTIconSet iconset){
        _iconset = iconset;
    }

    @Override
    public IconSet getIconSet() {
        String set = _iconset.getIconSet().toString();
        return IconSet.byName(set);
    }
    @Override
    public void setIconSet(IconSet set) {
        STIconSetType.Enum xIconSet = STIconSetType.Enum.forString(set.name);
        _iconset.setIconSet(xIconSet);
    }

    @Override
    public boolean isIconOnly() {
        if (_iconset.isSetShowValue())
            return !_iconset.getShowValue();
        return false;
    }
    @Override
    public void setIconOnly(boolean only) {
        _iconset.setShowValue(!only);
    }

    @Override
    public boolean isReversed() {
        if (_iconset.isSetReverse())
            return _iconset.getReverse();
        return false;
    }
    @Override
    public void setReversed(boolean reversed) {
        _iconset.setReverse(reversed);
    }

    @Override
    public XSSFConditionalFormattingThreshold[] getThresholds() {
        CTCfvo[] cfvos = _iconset.getCfvoArray();
        XSSFConditionalFormattingThreshold[] t =
                new XSSFConditionalFormattingThreshold[cfvos.length];
        for (int i=0; i<cfvos.length; i++) {
            t[i] = new XSSFConditionalFormattingThreshold(cfvos[i]);
        }
        return t;
    }
    @Override
    public void setThresholds(ConditionalFormattingThreshold[] thresholds) {
        CTCfvo[] cfvos = new CTCfvo[thresholds.length];
        for (int i=0; i<thresholds.length; i++) {
            cfvos[i] = ((XSSFConditionalFormattingThreshold)thresholds[i]).getCTCfvo();
        }
        _iconset.setCfvoArray(cfvos);
    }
    @Override
    public XSSFConditionalFormattingThreshold createThreshold() {
        return new XSSFConditionalFormattingThreshold(_iconset.addNewCfvo());
    }
}
