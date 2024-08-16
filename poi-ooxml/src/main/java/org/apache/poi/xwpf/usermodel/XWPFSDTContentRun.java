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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRunTrackChange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSmartTagRun;


/**
 * Experimental class to offer rudimentary read-only processing of
 * the XWPFSDTContentRun.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContentRun implements ISDTContent, IRunBody {
    private final CTSdtContentRun sdtContentRun;
    protected List<XWPFRun> runs;
    protected List<IRunElement> iruns;

    private final StringBuilder footnoteText = new StringBuilder(64);
    private final XWPFSDTRun xwpfsdtRun;

    public XWPFSDTContentRun(CTSdtContentRun sdtContentRun, XWPFSDTRun xwpfsdtRun) {
        super();
        this.sdtContentRun = sdtContentRun;
        this.xwpfsdtRun = xwpfsdtRun;
        //sdtContentRun is allowed to be null:  minOccurs="0" maxOccurs="1"
        if (sdtContentRun == null) {
            return;
        }
        // Build up the character runs
        runs = new ArrayList<>();
        iruns = new ArrayList<>();
        buildRunsInOrderFromXml(sdtContentRun);

        // Look for bits associated with the runs
        for (XWPFRun run : runs) {
            CTR r = run.getCTR();

            // Check for bits that only apply when attached to a core document
            // TODO Make this nicer by tracking the XWPFFootnotes directly
            try (XmlCursor c = r.newCursor()) {
                c.selectPath("child::*");
                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTFtnEdnRef) {
                        CTFtnEdnRef ftn = (CTFtnEdnRef) o;
                        final BigInteger id = ftn.getId();
                        footnoteText.append(" [").append(id).append(": ");
                        XWPFAbstractFootnoteEndnote footnote =
                            ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                                xwpfsdtRun.getDocument().getFootnoteByID(id == null ? 0 : id.intValue()) :
                                xwpfsdtRun.getDocument().getEndnoteByID(id == null ? 0 : id.intValue());
                        if (null != footnote) {
                            boolean first = true;
                            for (XWPFParagraph p : footnote.getParagraphs()) {
                                if (!first) {
                                    footnoteText.append("\n");
                                }
                                first = false;
                                footnoteText.append(p.getText());
                            }
                        } else {
                            footnoteText.append("!!! End note with ID \"").append(id).append("\" not found in document.");
                        }
                        footnoteText.append("] ");

                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void buildRunsInOrderFromXml(XmlObject object) {
        try (XmlCursor c = object.newCursor()) {
            c.selectPath("child::*");
            while (c.toNextSelection()) {
                XmlObject o = c.getObject();
                if (o instanceof CTR) {
                    XWPFRun r = new XWPFRun((CTR) o, this);
                    runs.add(r);
                    iruns.add(r);
                }
                if (o instanceof CTHyperlink) {
                    CTHyperlink link = (CTHyperlink)o;
                    for (CTR r : link.getRArray()) {
                        XWPFHyperlinkRun hr = new XWPFHyperlinkRun(link, r, this);
                        runs.add(hr);
                        iruns.add(hr);
                    }
                }
                if (o instanceof CTSimpleField) {
                    CTSimpleField field = (CTSimpleField)o;
                    for (CTR r : field.getRArray()) {
                        XWPFFieldRun fr = new XWPFFieldRun(field, r, this);
                        runs.add(fr);
                        iruns.add(fr);
                    }
                }
                if (o instanceof CTSdtRun) {
                    XWPFSDTRun cc = new XWPFSDTRun((CTSdtRun) o, this);
                    iruns.add(cc);
                }
                if (o instanceof CTRunTrackChange) {
                    for (CTR r : ((CTRunTrackChange) o).getRArray()) {
                        XWPFRun cr = new XWPFRun(r, this);
                        runs.add(cr);
                        iruns.add(cr);
                    }
                }
                if (o instanceof CTSmartTagRun) {
                    // Smart Tags can be nested many times.
                    // This implementation does not preserve the tagging information
                    buildRunsInOrderFromXml(o);
                }
                if (o instanceof CTRunTrackChange) {
                    // add all the insertions as text
                    for (CTRunTrackChange change : ((CTRunTrackChange) o).getInsArray()) {
                        buildRunsInOrderFromXml(change);
                    }
                }
            }
        }
    }

    public CTSdtContentRun getSdtContentRun() {
        return sdtContentRun;
    }

    public XWPFSDTRun getSDT() {
        return xwpfsdtRun;
    }

    public List<XWPFRun> getRuns() {
        return Collections.unmodifiableList(runs);
    }

    public boolean runsIsEmpty() {
        return runs.isEmpty();
    }

    /**
     * Return literal runs and sdt/content control objects.
     */
    public List<IRunElement> getIRuns() {
        return Collections.unmodifiableList(iruns);
    }

    public String getText() {
        StringBuilder out = new StringBuilder(64);
        for (IRunElement run : iruns) {
            if (run instanceof XWPFRun) {
                XWPFRun xRun = (XWPFRun) run;
                // don't include the text if reviewing is enabled and this is a deleted run
                if (xRun.getCTR().getDelTextArray().length == 0) {
                    out.append(xRun);
                }
            } else if (run instanceof XWPFSDTRun) {
                out.append(((XWPFSDTRun) run).getContent().getText());
            } else {
                out.append(run);
            }
        }
        out.append(footnoteText);
        return out.toString();
    }

    public String toString() {
        return getText();
    }

    @Override
    public XWPFDocument getDocument() {
        return this.xwpfsdtRun.getDocument();
    }

    @Override
    public POIXMLDocumentPart getPart() {
        return this.xwpfsdtRun.getParent().getPart();
    }
}
