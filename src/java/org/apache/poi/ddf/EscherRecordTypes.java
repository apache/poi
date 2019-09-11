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

package org.apache.poi.ddf;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum EscherRecordTypes {
    // records greater then 0xF000 belong to with Microsoft Office Drawing format also known as Escher
    DGG_CONTAINER(0xF000, "DggContainer", null),
    BSTORE_CONTAINER(0xf001, "BStoreContainer", null),
    DG_CONTAINER(0xf002, "DgContainer", null),
    SPGR_CONTAINER(0xf003, "SpgrContainer", null),
    SP_CONTAINER(0xf004, "SpContainer", null),
    SOLVER_CONTAINER(0xf005, "SolverContainer", null),
    DGG(0xf006, "Dgg", "MsofbtDgg"),
    BSE(0xf007, "BSE", "MsofbtBSE"),
    DG(0xf008, "Dg", "MsofbtDg"),
    SPGR(0xf009, "Spgr", "MsofbtSpgr"),
    SP(0xf00a, "Sp", "MsofbtSp"),
    OPT(0xf00b, "Opt", "msofbtOPT"),
    TEXTBOX(0xf00c, null, null),
    CLIENT_TEXTBOX(0xf00d, "ClientTextbox", "msofbtClientTextbox"),
    ANCHOR(0xf00e, null, null),
    CHILD_ANCHOR(0xf00f, "ChildAnchor", "MsofbtChildAnchor"),
    CLIENT_ANCHOR(0xf010, "ClientAnchor", "MsofbtClientAnchor"),
    CLIENT_DATA(0xf011, "ClientData", "MsofbtClientData"),
    CONNECTOR_RULE(0xf012, null, null),
    ALIGN_RULE(0xf013, null, null),
    ARC_RULE(0xf014, null, null),
    CLIENT_RULE(0xf015, null, null),
    CLSID(0xf016, null, null),
    CALLOUT_RULE(0xf017, null, null),
    BLIP_START(0xf018, "Blip", "msofbtBlip"),
    BLIP_EMF(0xf018 + 2, "BlipEmf", null),
    BLIP_WMF(0xf018 + 3, "BlipWmf", null),
    BLIP_PICT(0xf018 + 4, "BlipPict", null),
    BLIP_JPEG(0xf018 + 5, "BlipJpeg", null),
    BLIP_PNG(0xf018 + 6, "BlipPng", null),
    BLIP_DIB(0xf018 + 7, "BlipDib", null),
    BLIP_END(0xf117, "Blip", "msofbtBlip"),
    REGROUP_ITEMS(0xf118, null, null),
    SELECTION(0xf119, null, null),
    COLOR_MRU(0xf11a, null, null),
    DELETED_PSPL(0xf11d, null, null),
    SPLIT_MENU_COLORS(0xf11e, "SplitMenuColors", "MsofbtSplitMenuColors"),
    OLE_OBJECT(0xf11f, null, null),
    COLOR_SCHEME(0xf120, null, null),
    // same as EscherTertiaryOptRecord.RECORD_ID
    USER_DEFINED(0xf122, "TertiaryOpt", null),
    UNKNOWN(0xffff, "unknown", "unknown");

    public final short typeID;
    public final String recordName;
    public final String description;

    EscherRecordTypes(int typeID, String recordName, String description) {
        this.typeID = (short) typeID;
        this.recordName = recordName;
        this.description = description;
    }

    private Short getTypeId() {
        return typeID;
    }

    private static final Map<Short, EscherRecordTypes> LOOKUP =
        Stream.of(values()).collect(Collectors.toMap(EscherRecordTypes::getTypeId, Function.identity()));

    public static EscherRecordTypes forTypeID(int typeID) {
        EscherRecordTypes rt = LOOKUP.get((short)typeID);
        return (rt != null) ? rt : EscherRecordTypes.UNKNOWN;
    }

}
