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

import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.cf.IconMultiStateFormatting;
import org.apache.poi.hssf.record.cf.IconMultiStateThreshold;
import org.apache.poi.hssf.record.cf.Threshold;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;

/**
 * High level representation for Icon / Multi-State Formatting
 *  component of Conditional Formatting settings
 */
public final class HSSFIconMultiStateFormatting implements org.apache.poi.ss.usermodel.IconMultiStateFormatting {
    private final HSSFSheet sheet;
    private final IconMultiStateFormatting iconFormatting;

    HSSFIconMultiStateFormatting(CFRule12Record cfRule12Record, HSSFSheet sheet) {
        this.sheet = sheet;
        this.iconFormatting = cfRule12Record.getMultiStateFormatting();
    }

    @Override
    public IconSet getIconSet() {
        return iconFormatting.getIconSet();
    }
    @Override
    public void setIconSet(IconSet set) {
        iconFormatting.setIconSet(set);
    }

    @Override
    public boolean isIconOnly() {
        return iconFormatting.isIconOnly();
    }
    @Override
    public void setIconOnly(boolean only) {
        iconFormatting.setIconOnly(only);
    }

    @Override
    public boolean isReversed() {
        return iconFormatting.isReversed();
    }
    @Override
    public void setReversed(boolean reversed) {
        iconFormatting.setReversed(reversed);
    }

    @Override
    public HSSFConditionalFormattingThreshold[] getThresholds() {
        Threshold[] t = iconFormatting.getThresholds();
        HSSFConditionalFormattingThreshold[] ht = new HSSFConditionalFormattingThreshold[t.length];
        for (int i=0; i<t.length; i++) {
            ht[i] = new HSSFConditionalFormattingThreshold(t[i], sheet);
        }
        return ht;
    }

    @Override
    public void setThresholds(ConditionalFormattingThreshold[] thresholds) {
        Threshold[] t = new Threshold[thresholds.length];
        for (int i=0; i<t.length; i++) {
            t[i] = ((HSSFConditionalFormattingThreshold)thresholds[i]).getThreshold();
        }
        iconFormatting.setThresholds(t);
    }

    @Override
    public HSSFConditionalFormattingThreshold createThreshold() {
        return new HSSFConditionalFormattingThreshold(new IconMultiStateThreshold(), sheet);
    }
}
