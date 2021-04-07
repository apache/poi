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
package org.apache.poi.xwpf.extractor;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.model.XWPFCommentsDecorator;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ICell;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlink;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFSDTCell;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

/**
 * Helper class to extract text from an OOXML Word file
 */
public class XWPFWordExtractor implements POIXMLTextExtractor {
    public static final XWPFRelation[] SUPPORTED_TYPES = {
            XWPFRelation.DOCUMENT, XWPFRelation.TEMPLATE,
            XWPFRelation.MACRO_DOCUMENT,
            XWPFRelation.MACRO_TEMPLATE_DOCUMENT
    };

    private final XWPFDocument document;
    private boolean fetchHyperlinks;
    private boolean concatenatePhoneticRuns = true;
    private boolean doCloseFilesystem = true;

    public XWPFWordExtractor(OPCPackage container) throws IOException {
        this(new XWPFDocument(container));
    }

    public XWPFWordExtractor(XWPFDocument document) {
        this.document = document;
    }

    /**
     * Should we also fetch the hyperlinks, when fetching
     * the text content? Default is to only output the
     * hyperlink label, and not the contents
     */
    public void setFetchHyperlinks(boolean fetch) {
        fetchHyperlinks = fetch;
    }

    /**
     * Should we concatenate phonetic runs in extraction.  Default is <code>true</code>
     * @param concatenatePhoneticRuns If phonetic runs should be concatenated
     */
    public void setConcatenatePhoneticRuns(boolean concatenatePhoneticRuns) {
        this.concatenatePhoneticRuns = concatenatePhoneticRuns;
    }

    public String getText() {
        StringBuilder text = new StringBuilder(64);
        XWPFHeaderFooterPolicy hfPolicy = document.getHeaderFooterPolicy();

        // Start out with all headers
        extractHeaders(text, hfPolicy);

        // Process all body elements
        for (IBodyElement e : document.getBodyElements()) {
            appendBodyElementText(text, e);
            text.append('\n');
        }

        // Finish up with all the footers
        extractFooters(text, hfPolicy);

        return text.toString();
    }

    public void appendBodyElementText(StringBuilder text, IBodyElement e) {
        if (e instanceof XWPFParagraph) {
            appendParagraphText(text, (XWPFParagraph) e);
        } else if (e instanceof XWPFTable) {
            appendTableText(text, (XWPFTable) e);
        } else if (e instanceof XWPFSDT) {
            text.append(((XWPFSDT) e).getContent().getText());
        }
    }

    public void appendParagraphText(StringBuilder text, XWPFParagraph paragraph) {
        CTSectPr ctSectPr = null;
        if (paragraph.getCTP().getPPr() != null) {
            ctSectPr = paragraph.getCTP().getPPr().getSectPr();
        }

        XWPFHeaderFooterPolicy headerFooterPolicy = null;

        if (ctSectPr != null) {
            headerFooterPolicy = new XWPFHeaderFooterPolicy(document, ctSectPr);
            extractHeaders(text, headerFooterPolicy);
        }

        for (IRunElement run : paragraph.getIRuns()) {
            if (run instanceof XWPFSDT) {
                text.append(((XWPFSDT) run).getContent().getText());
            } else if (! concatenatePhoneticRuns && run instanceof XWPFRun) {
                text.append(((XWPFRun)run).text());
            } else {
                text.append(run);
            }
            if (run instanceof XWPFHyperlinkRun && fetchHyperlinks) {
                XWPFHyperlink link = ((XWPFHyperlinkRun) run).getHyperlink(document);
                if (link != null)
                    text.append(" <").append(link.getURL()).append(">");
            }
        }

        // Add comments
        XWPFCommentsDecorator decorator = new XWPFCommentsDecorator(paragraph, null);
        String commentText = decorator.getCommentText();
        if (commentText.length() > 0) {
            text.append(commentText).append('\n');
        }

        // Do endnotes and footnotes
        String footnameText = paragraph.getFootnoteText();
        if (footnameText != null && footnameText.length() > 0) {
            text.append(footnameText).append('\n');
        }

        if (ctSectPr != null) {
            extractFooters(text, headerFooterPolicy);
        }
    }

    private void appendTableText(StringBuilder text, XWPFTable table) {
        //this works recursively to pull embedded tables from tables
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

    private void extractFooters(StringBuilder text, XWPFHeaderFooterPolicy hfPolicy) {
        if (hfPolicy == null) return;

        if (hfPolicy.getFirstPageFooter() != null) {
            text.append(hfPolicy.getFirstPageFooter().getText());
        }
        if (hfPolicy.getEvenPageFooter() != null) {
            text.append(hfPolicy.getEvenPageFooter().getText());
        }
        if (hfPolicy.getDefaultFooter() != null) {
            text.append(hfPolicy.getDefaultFooter().getText());
        }
    }

    private void extractHeaders(StringBuilder text, XWPFHeaderFooterPolicy hfPolicy) {
        if (hfPolicy == null) return;

        if (hfPolicy.getFirstPageHeader() != null) {
            text.append(hfPolicy.getFirstPageHeader().getText());
        }
        if (hfPolicy.getEvenPageHeader() != null) {
            text.append(hfPolicy.getEvenPageHeader().getText());
        }
        if (hfPolicy.getDefaultHeader() != null) {
            text.append(hfPolicy.getDefaultHeader().getText());
        }
    }

    @Override
    public XWPFDocument getDocument() {
        return document;
    }

    @Override
    public void setCloseFilesystem(boolean doCloseFilesystem) {
        this.doCloseFilesystem = doCloseFilesystem;
    }

    @Override
    public boolean isCloseFilesystem() {
        return doCloseFilesystem;
    }

    @Override
    public XWPFDocument getFilesystem() {
        return document;
    }
}
