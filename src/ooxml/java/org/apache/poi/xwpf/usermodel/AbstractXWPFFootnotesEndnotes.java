package org.apache.poi.xwpf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * Base class for the Footnotes and Endnotes part implementations.
 * @since 4.0.0
 */
public abstract class AbstractXWPFFootnotesEndnotes extends POIXMLDocumentPart {

    protected XWPFDocument document;
    protected List<AbstractXWPFFootnoteEndnote> listFootnote = new ArrayList<>();
    private FootnoteEndnoteIdManager idManager;
    
    public AbstractXWPFFootnotesEndnotes(OPCPackage pkg) {
        super(pkg);
    }

    public AbstractXWPFFootnotesEndnotes(OPCPackage pkg,
            String coreDocumentRel) {
        super(pkg, coreDocumentRel);
    }

    public AbstractXWPFFootnotesEndnotes() {
        super();
    }

    public AbstractXWPFFootnotesEndnotes(PackagePart part) {
        super(part);
    }

    public AbstractXWPFFootnotesEndnotes(POIXMLDocumentPart parent, PackagePart part) {
        super(parent, part);
    }


    public AbstractXWPFFootnoteEndnote getFootnoteById(int id) {
        for (AbstractXWPFFootnoteEndnote note : listFootnote) {
            if (note.getCTFtnEdn().getId().intValue() == id)
                return note;
        }
        return null;
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public XWPFDocument getXWPFDocument() {
        if (document != null) {
            return document;
        } else {
            return (XWPFDocument) getParent();
        }
    }

    public void setXWPFDocument(XWPFDocument doc) {
        document = doc;
    }

    public void setIdManager(FootnoteEndnoteIdManager footnoteIdManager) {
       this.idManager = footnoteIdManager;
        
    }
    
    public FootnoteEndnoteIdManager getIdManager() {
        return this.idManager;
    }

}