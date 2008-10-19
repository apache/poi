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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;

import java.io.IOException;

public class XSSFDialogsheet extends XSSFSheet implements Sheet{

    public XSSFDialogsheet(XSSFSheet sheet) {
        this.packagePart = sheet.getPackagePart();
        this.packageRel = sheet.getPackageRelationship();
        this.dialogsheet = CTDialogsheet.Factory.newInstance();
    }

    public XSSFRow createRow(int rowNum) {
        return null;
    }

    protected CTHeaderFooter getSheetTypeHeaderFooter() {
        if (dialogsheet.getHeaderFooter() == null) {
            dialogsheet.setHeaderFooter(CTHeaderFooter.Factory.newInstance());
        }
        return dialogsheet.getHeaderFooter();
    }

    protected CTSheetPr getSheetTypeSheetPr() {
        if (dialogsheet.getSheetPr() == null) {
            dialogsheet.setSheetPr(CTSheetPr.Factory.newInstance());
        }
        return dialogsheet.getSheetPr();
    }

    protected CTPageBreak getSheetTypeColumnBreaks() {
        return null;
    }

    protected CTSheetFormatPr getSheetTypeSheetFormatPr() {
        if (dialogsheet.getSheetFormatPr() == null) {
            dialogsheet.setSheetFormatPr(CTSheetFormatPr.Factory.newInstance());
        }
        return dialogsheet.getSheetFormatPr();
    }

    protected CTPageMargins getSheetTypePageMargins() {
        if (dialogsheet.getPageMargins() == null) {
            dialogsheet.setPageMargins(CTPageMargins.Factory.newInstance());
        }
        return dialogsheet.getPageMargins();
    }

    protected CTPageBreak getSheetTypeRowBreaks() {
        return null;
    }

    protected CTSheetViews getSheetTypeSheetViews() {
        if (dialogsheet.getSheetViews() == null) {
            dialogsheet.setSheetViews(CTSheetViews.Factory.newInstance());
            dialogsheet.getSheetViews().addNewSheetView();
        }
        return dialogsheet.getSheetViews();
    }

    protected CTPrintOptions getSheetTypePrintOptions() {
        if (dialogsheet.getPrintOptions() == null) {
            dialogsheet.setPrintOptions(CTPrintOptions.Factory.newInstance());
        }
        return dialogsheet.getPrintOptions();
    }

    protected CTSheetProtection getSheetTypeProtection() {
        if (dialogsheet.getSheetProtection() == null) {
            dialogsheet.setSheetProtection(CTSheetProtection.Factory.newInstance());
        }
        return dialogsheet.getSheetProtection();
    }

}
