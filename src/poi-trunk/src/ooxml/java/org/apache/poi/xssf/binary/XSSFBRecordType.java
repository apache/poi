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

package org.apache.poi.xssf.binary;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.util.Internal;

/**
 * @since 3.16-beta3
 */
@Internal
public enum XSSFBRecordType {

    BrtCellBlank(1),
    BrtCellRk(2),
    BrtCellError(3),
    BrtCellBool(4),
    BrtCellReal(5),
    BrtCellSt(6),
    BrtCellIsst(7),
    BrtFmlaString(8),
    BrtFmlaNum(9),
    BrtFmlaBool(10),
    BrtFmlaError(11),
    BrtRowHdr(0),
    BrtCellRString(62),
    BrtBeginSheet(129),
    BrtWsProp(147),
    BrtWsDim(148),
    BrtColInfo(60),
    BrtBeginSheetData(145),
    BrtEndSheetData(146),
    BrtHLink(494),
    BrtBeginHeaderFooter(479),

    //comments
    BrtBeginCommentAuthors(630),
    BrtEndCommentAuthors(631),
    BrtCommentAuthor(632),
    BrtBeginComment(635),
    BrtCommentText(637),
    BrtEndComment(636),
    //styles table
    BrtXf(47),
    BrtFmt(44),
    BrtBeginFmts(615),
    BrtEndFmts(616),
    BrtBeginCellXFs(617),
    BrtEndCellXFs(618),
    BrtBeginCellStyleXFS(626),
    BrtEndCellStyleXFS(627),

    //stored strings table
    BrtSstItem(19),   //stored strings items
    BrtBeginSst(159), //stored strings begin sst
    BrtEndSst(160),   //stored strings end sst

    BrtBundleSh(156), //defines worksheet in wb part

    BrtAbsPath15(2071), //Excel 2013 path where the file was stored in wbpart

    //TODO -- implement these as needed
    //BrtFileVersion(128), //file version
    //BrtWbProp(153), //Workbook prop contains 1904/1900-date based bit
    Unimplemented(-1);

    private static final Map<Integer, XSSFBRecordType> TYPE_MAP =
            new HashMap<>();

    static {
        for (XSSFBRecordType type : XSSFBRecordType.values()) {
            TYPE_MAP.put(type.getId(), type);
        }
    }

    private final int id;

    XSSFBRecordType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static XSSFBRecordType lookup(int id) {
        XSSFBRecordType type = TYPE_MAP.get(id);
        if (type == null) {
            return Unimplemented;
        }
        return type;
    }

}
