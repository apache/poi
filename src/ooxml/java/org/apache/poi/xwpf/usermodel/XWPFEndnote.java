package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

/**
 * Represents an end note footnote.
 * <p>End notes are collected at the end of a document or section rather than
 * at the bottom of a page.</p>
 * <p>Create a new footnote using {@link XWPFDocument#createEndnote()} or
 * {@link XWPFEndnotes#createFootnote()}.</p>
 * <p>The first body element of a footnote should (or possibly must) be a paragraph
 * with the first run containing a CTFtnEdnRef object. The {@link XWPFFootnote#createParagraph()}
 * and {@link XWPFFootnote#createTable()} methods do this for you.</p>
 * <p>Footnotes have IDs that are unique across all footnotes in the document. You use
 * the footnote ID to create a reference to a footnote from within a paragraph.</p>
 * <p>To create a reference to a footnote within a paragraph you create a run
 * with a CTFtnEdnRef that specifies the ID of the target paragraph. 
 * The {@link XWPFParagraph#addFootnoteReference(AbstractXWPFFootnoteEndnote)}
 * method does this for you.</p>
 * @since 4.0.0
 */
public class XWPFEndnote extends AbstractXWPFFootnoteEndnote {

    public XWPFEndnote() {
        // TODO Auto-generated constructor stub
    }

    public XWPFEndnote(XWPFDocument document, CTFtnEdn body) {
        super(document, body);
    }

    public XWPFEndnote(CTFtnEdn note, AbstractXWPFFootnotesEndnotes footnotes) {
        super(note, footnotes);
    }

    /**
     * Ensure that the specified paragraph has a reference marker for this
     * end note by adding a footnote reference if one is not found.
     * <p>This method is for the first paragraph in the footnote, not 
     * paragraphs that will refer to the footnote. For references to
     * the footnote, use {@link XWPFParagraph#addFootnoteReference(AbstractXWPFFootnoteEndnote))}.
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
        for (CTFtnEdnRef ref : ctr.getEndnoteReferenceList()) {
            if (getId().equals(ref.getId())) {
                foundRef = true;
                break;
            }
        }
        if (!foundRef) {
            ctr.addNewRPr().addNewRStyle().setVal("FootnoteReference");
            ctr.addNewEndnoteRef();
        }
        
    }

}
