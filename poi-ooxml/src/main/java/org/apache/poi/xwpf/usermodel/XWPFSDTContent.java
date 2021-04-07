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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

/**
 * Experimental class to offer rudimentary read-only processing of
 * of the contentblock of an SDT/ContentControl.
 * <p>
 * <p>
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContent implements ISDTContent {

    // private final IBody part;
    // private final XWPFDocument document;
    // private List<XWPFParagraph> paragraphs = new ArrayList<>();
    // private List<XWPFTable> tables = new ArrayList<>();
    // private List<XWPFRun> runs = new ArrayList<>();
    // private List<XWPFSDT> contentControls = new ArrayList<>();
    private List<ISDTContents> bodyElements = new ArrayList<>();

    public XWPFSDTContent(CTSdtContentRun sdtRun, IBody part, IRunBody parent) {
        if (sdtRun == null) {
            return;
        }
        XmlCursor cursor = sdtRun.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTR) {
                XWPFRun run = new XWPFRun((CTR) o, parent);
                bodyElements.add(run);
            } else if (o instanceof CTSdtRun) {
                XWPFSDT c = new XWPFSDT(((CTSdtRun) o), part);
                bodyElements.add(c);
            }
        }
        cursor.dispose();
    }

    public XWPFSDTContent(CTSdtContentBlock block, IBody part, IRunBody parent) {
        if (block == null) {
            return;
        }
        XmlCursor cursor = block.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP) o, part);
                bodyElements.add(p);
                // paragraphs.add(p);
            } else if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl) o, part);
                bodyElements.add(t);
                // tables.add(t);
            } else if (o instanceof CTSdtBlock) {
                XWPFSDT c = new XWPFSDT(((CTSdtBlock) o), part);
                bodyElements.add(c);
                // contentControls.add(c);
            } else if (o instanceof CTR) {
                XWPFRun run = new XWPFRun((CTR) o, parent);
                // runs.add(run);
                bodyElements.add(run);
            }
        }
        cursor.dispose();
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        boolean addNewLine = false;
        for (int i = 0; i < bodyElements.size(); i++) {
            Object o = bodyElements.get(i);
            if (o instanceof XWPFParagraph) {
                appendParagraph((XWPFParagraph) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFTable) {
                appendTable((XWPFTable) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFSDT) {
                text.append(((XWPFSDT) o).getContent().getText());
                addNewLine = true;
            } else if (o instanceof XWPFRun) {
                text.append(o);
                addNewLine = false;
            }
            if (addNewLine && i < bodyElements.size() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }

    private void appendTable(XWPFTable table, StringBuilder text) {
        //this works recursively to pull embedded tables from within cells
        for (XWPFTableRow row : table.getRows()) {
            List<ICell> cells = row.getTableICells();
            for (int i = 0; i < cells.size(); i++) {
                ICell cell = cells.get(i);
                if (cell instanceof XWPFTableCell) {
                    text.append(((XWPFTableCell) cell).getTextRecursively());
                } else if (cell instanceof XWPFSDTCell) {
                    text.append(((XWPFSDTCell) cell).getContent().getText());
                }
                if (i < cells.size() - 1) {
                    text.append("\t");
                }
            }
            text.append('\n');
        }
    }

    private void appendParagraph(XWPFParagraph paragraph, StringBuilder text) {
        for (IRunElement run : paragraph.getRuns()) {
            text.append(run);
        }
    }

    @Override
    public String toString() {
        return getText();
    }
}
