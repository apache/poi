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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;


/**
 * An XSSF only representation of a reference to a deleted area
 */
public final class Deleted3DPxg extends OperandPtg implements Pxg {
    private int externalWorkbookNumber = -1;
    private String sheetName;

    public Deleted3DPxg(int externalWorkbookNumber, String sheetName) {
        this.externalWorkbookNumber = externalWorkbookNumber;
        this.sheetName = sheetName;
    }

    public Deleted3DPxg(Deleted3DPxg other) {
        super(other);
        externalWorkbookNumber = other.externalWorkbookNumber;
        sheetName = other.sheetName;
    }

    public Deleted3DPxg(String sheetName) {
        this(-1, sheetName);
    }

    public int getExternalWorkbookNumber() {
        return externalWorkbookNumber;
    }
    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String toFormulaString() {
        StringBuilder sb = new StringBuilder(64);
        if (externalWorkbookNumber >= 0) {
            sb.append('[');
            sb.append(externalWorkbookNumber);
            sb.append(']');
        }
        if (sheetName != null) {
            SheetNameFormatter.appendFormat(sb, sheetName);
        }
        sb.append('!');
        sb.append(FormulaError.REF.getString());
        return sb.toString();
    }

    public byte getDefaultOperandClass() {
        return Ptg.CLASS_VALUE;
    }

    @Override
    public byte getSid() {
        return -1;
    }

    public int getSize() {
        return 1;
    }

    public void write(LittleEndianOutput out) {
        throw new IllegalStateException("XSSF-only Ptg, should not be serialised");
    }

    @Override
    public Deleted3DPxg copy() {
        return new Deleted3DPxg(this);
    }


    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "externalWorkbookNumber", this::getExternalWorkbookNumber,
            "sheetName", this::getSheetName,
            "formulaError", () -> FormulaError.REF
        );
    }
}
