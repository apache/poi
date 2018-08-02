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

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

/**
 * Represents a bottom-of-the-page footnote.
 * <p>Create a new footnote using {@link XWPFDocument#createFootnote()} or
 * {@link XWPFFootnotes#createFootnote()}.</p>
 * <p>The first body element of a footnote should (or possibly must) be a paragraph
 * with the first run containing a CTFtnEdnRef object. The {@link XWPFFootnote#createParagraph()}
 * and {@link XWPFFootnote#createTable()} methods do this for you.</p>
 * <p>Footnotes have IDs that are unique across all footnotes in the document. You use
 * the footnote ID to create a reference to a footnote from within a paragraph.</p>
 * <p>To create a reference to a footnote within a paragraph you create a run
 * with a CTFtnEdnRef that specifies the ID of the target paragraph. 
 * The {@link XWPFParagraph#addFootnoteReference(XWPFAbstractFootnoteEndnote)}
 * method does this for you.</p>
 */
public class XWPFFootnote extends XWPFAbstractFootnoteEndnote {
    
    @Internal
    public XWPFFootnote(CTFtnEdn note, XWPFAbstractFootnotesEndnotes xFootnotes) {
        super(note, xFootnotes);
    }

    @Internal
    public XWPFFootnote(XWPFDocument document, CTFtnEdn body) {
        super(document, body);
    }
    
    /**
     * Ensure that the specified paragraph has a reference marker for this
     * footnote by adding a footnote reference if one is not found.
     * <p>This method is for the first paragraph in the footnote, not 
     * paragraphs that will refer to the footnote. For references to
     * the footnote, use {@link XWPFParagraph#addFootnoteReference(XWPFFootnote)}.
     * </p>
     * <p>The first run of the first paragraph in a footnote should
     * contain a {@link CTFtnEdnRef} object.</p>
     *
     * @param p The {@link XWPFParagraph} to ensure
     * @since 4.0.0
       */
    public void ensureFootnoteRef(XWPFParagraph p) {
        
        XWPFRun r = null;
        if (p.getRuns().size() > 0) {
            r = p.getRuns().get(0);
        }
        if (r == null) {
            r = p.createRun();
        }
        CTR ctr = r.getCTR();
        boolean foundRef = false;
        for (CTFtnEdnRef ref : ctr.getFootnoteReferenceList()) {
            if (getId().equals(ref.getId())) {
                foundRef = true;
                break;
            }
        }
        if (!foundRef) {
            ctr.addNewRPr().addNewRStyle().setVal("FootnoteReference");
            ctr.addNewFootnoteRef();
        }
        
    }
}
