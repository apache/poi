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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute.Space;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtEndPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabs;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabTlc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTheme;

public class TOC {

    CTSdtBlock block;

    public TOC() {
        this(CTSdtBlock.Factory.newInstance());
    }

    public TOC(CTSdtBlock block) {
        this.block = block;
        CTSdtPr sdtPr = block.addNewSdtPr();
        CTDecimalNumber id = sdtPr.addNewId();
        id.setVal(BigInteger.valueOf(4844945));
        sdtPr.addNewDocPartObj().addNewDocPartGallery().setVal("Table of contents");
        CTSdtEndPr sdtEndPr = block.addNewSdtEndPr();
        CTRPr rPr = sdtEndPr.addNewRPr();
        CTFonts fonts = rPr.addNewRFonts();
        fonts.setAsciiTheme(STTheme.MINOR_H_ANSI);
        fonts.setEastAsiaTheme(STTheme.MINOR_H_ANSI);
        fonts.setHAnsiTheme(STTheme.MINOR_H_ANSI);
        fonts.setCstheme(STTheme.MINOR_BIDI);
        rPr.addNewB().setVal(STOnOff1.OFF);
        rPr.addNewBCs().setVal(STOnOff1.OFF);
        rPr.addNewColor().setVal("auto");
        rPr.addNewSz().setVal(BigInteger.valueOf(24));
        rPr.addNewSzCs().setVal(BigInteger.valueOf(24));
        CTSdtContentBlock content = block.addNewSdtContent();
        CTP p = content.addNewP();
        p.setRsidR("00EF7E24".getBytes(LocaleUtil.CHARSET_1252));
        p.setRsidRDefault("00EF7E24".getBytes(LocaleUtil.CHARSET_1252));
        p.addNewPPr().addNewPStyle().setVal("TOCHeading");
        p.addNewR().addNewT().setStringValue("Table of Contents");
    }

    @Internal
    public CTSdtBlock getBlock() {
        return this.block;
    }

    public void addRow(int level, String title, int page, String bookmarkRef) {
        CTSdtContentBlock contentBlock = this.block.getSdtContent();
        CTP p = contentBlock.addNewP();
        p.setRsidR("00EF7E24".getBytes(LocaleUtil.CHARSET_1252));
        p.setRsidRDefault("00EF7E24".getBytes(LocaleUtil.CHARSET_1252));
        CTPPr pPr = p.addNewPPr();
        pPr.addNewPStyle().setVal("TOC" + level);
        CTTabs tabs = pPr.addNewTabs();
        CTTabStop tab = tabs.addNewTab();
        tab.setVal(STTabJc.RIGHT);
        tab.setLeader(STTabTlc.DOT);
        tab.setPos(BigInteger.valueOf(8290));
        pPr.addNewRPr().addNewNoProof();
        CTR run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewT().setStringValue(title);
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewTab();
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewFldChar().setFldCharType(STFldCharType.BEGIN);
        // pageref run
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        CTText text = run.addNewInstrText();
        text.setSpace(Space.PRESERVE);
        // bookmark reference
        text.setStringValue(" PAGEREF _Toc" + bookmarkRef + " \\h ");
        p.addNewR().addNewRPr().addNewNoProof();
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewFldChar().setFldCharType(STFldCharType.SEPARATE);
        // page number run
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewT().setStringValue(Integer.toString(page));
        run = p.addNewR();
        run.addNewRPr().addNewNoProof();
        run.addNewFldChar().setFldCharType(STFldCharType.END);
    }
}
