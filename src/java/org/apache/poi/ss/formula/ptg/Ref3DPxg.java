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

import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.SheetRangeAndWorkbookIndexFormatter;
import org.apache.poi.ss.formula.SheetRangeIdentifier;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * XSSF 3D Reference<p>
 * Defines a cell in an external or different sheet.<p>
 *
 * This is XSSF only, as it stores the sheet / book references
 * in String form. The HSSF equivalent using indexes is {@link Ref3DPtg}
 */
public final class Ref3DPxg extends RefPtgBase implements Pxg3D {
    private int externalWorkbookNumber = -1;
    private String firstSheetName;
    private String lastSheetName;

    public Ref3DPxg(Ref3DPxg other) {
        super(other);
        externalWorkbookNumber = other.externalWorkbookNumber;
        firstSheetName = other.firstSheetName;
        lastSheetName = other.lastSheetName;
    }

    public Ref3DPxg(int externalWorkbookNumber, SheetIdentifier sheetName, String cellref) {
        this(externalWorkbookNumber, sheetName, new CellReference(cellref));
    }
    public Ref3DPxg(int externalWorkbookNumber, SheetIdentifier sheetName, CellReference c) {
        super(c);
        this.externalWorkbookNumber = externalWorkbookNumber;

        this.firstSheetName = sheetName.getSheetIdentifier().getName();
        if (sheetName instanceof SheetRangeIdentifier) {
            this.lastSheetName = ((SheetRangeIdentifier)sheetName).getLastSheetIdentifier().getName();
        } else {
            this.lastSheetName = null;
        }
    }

    public Ref3DPxg(SheetIdentifier sheetName, String cellref) {
        this(sheetName, new CellReference(cellref));
    }
    public Ref3DPxg(SheetIdentifier sheetName, CellReference c) {
        this(-1, sheetName, c);
    }

    public int getExternalWorkbookNumber() {
        return externalWorkbookNumber;
    }
    public String getSheetName() {
        return firstSheetName;
    }
    public String getLastSheetName() {
        return lastSheetName;
    }

    public void setSheetName(String sheetName) {
        this.firstSheetName = sheetName;
    }
    public void setLastSheetName(String sheetName) {
        this.lastSheetName = sheetName;
    }

    public String format2DRefAsString() {
        return formatReferenceAsString();
    }

    public String toFormulaString() {
        StringBuilder sb = new StringBuilder(64);

        SheetRangeAndWorkbookIndexFormatter.format(sb, externalWorkbookNumber, firstSheetName, lastSheetName);
        sb.append('!');
        sb.append(formatReferenceAsString());
        return sb.toString();
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
    public Ref3DPxg copy() {
        return new Ref3DPxg(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "externalWorkbookNumber", this::getExternalWorkbookNumber,
            "firstSheetName", this::getSheetName,
            "lastSheetName", this::getLastSheetName
        );
    }
}
