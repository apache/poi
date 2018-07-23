package org.apache.poi.xwpf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTEndnotes;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.EndnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn;


/**
 * Looks after the collection of end notes for a document.
 * Managed end notes ({@link XWPFEndnote}).
 * @since 4.0.0
 */
public class XWPFEndnotes extends AbstractXWPFFootnotesEndnotes {

    protected CTEndnotes ctEndnotes;

    public XWPFEndnotes() {
        super();
    }

    /**
     * Construct XWPFEndnotes from a package part
     *
     * @param part the package part holding the data of the footnotes,
     * 
     * @since POI 3.14-Beta1
     */
    public XWPFEndnotes(PackagePart part) throws IOException, OpenXML4JException {
        super(part);
    }
    
    /**
     * Set the end notes for this part.
     *
     * @param endnotes The endnotes to be added.
     */
    public void setEndnotes(CTEndnotes endnotes) {
        ctEndnotes = endnotes;

    }

    /**
     * Create a new end note and add it to the document. 
     *
     * @return New XWPFEndnote
     * @since 4.0.0
     */
    public XWPFEndnote createEndnote() {
        CTFtnEdn newNote = CTFtnEdn.Factory.newInstance(); 
        newNote.setType(STFtnEdn.NORMAL);

        XWPFEndnote footnote = addEndnote(newNote);
        footnote.getCTFtnEdn().setId(getIdManager().nextId());
        return footnote;
        
    }

    /**
     * Remove the specified footnote if present.
     *
     * @param pos 
     * @return True if the footnote was removed.
     * @since 4.0.0
     */
    public boolean removeFootnote(int pos) {
        if (ctEndnotes.sizeOfEndnoteArray() >= pos - 1) {
            ctEndnotes.removeEndnote(pos);
            listFootnote.remove(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Read document
     */
    @Override
    protected void onDocumentRead() throws IOException {
        EndnotesDocument notesDoc;
        InputStream is = null;
        try {
            is = getPackagePart().getInputStream();            
            notesDoc = EndnotesDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctEndnotes = notesDoc.getEndnotes();
        } catch (XmlException e) {
            throw new POIXMLException();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    
        for (CTFtnEdn note : ctEndnotes.getEndnoteList()) {
            listFootnote.add(new XWPFEndnote(note, this));
        }
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTEndnotes.type.getName().getNamespaceURI(), "endnotes"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctEndnotes.save(out, xmlOptions);
        out.close();
    }

    /**
     * add an {@link XWPFEndnote} to the document
     *
     * @param endnote
     * @throws IOException
     */
    public void addEndnote(XWPFEndnote endnote) {
        listFootnote.add(endnote);
        ctEndnotes.addNewEndnote().set(endnote.getCTFtnEdn());
    }

    /**
     * Add an endnote to the document
     *
     * @param note Note to add
     * @return New {@link XWPFEndnote}
     * @throws IOException
     */
    public XWPFEndnote addEndnote(CTFtnEdn note) {
        CTFtnEdn newNote = ctEndnotes.addNewEndnote();
        newNote.set(note);
        XWPFEndnote xNote = new XWPFEndnote(newNote, this);
        listFootnote.add(xNote);
        return xNote;
    }

    /**
     * Get the end note with the specified ID, if any.
     * @param id End note ID.
     * @return The end note or null if not found.
     */
    public XWPFEndnote getFootnoteById(int id) {
        return (XWPFEndnote)super.getFootnoteById(id);
    }

    /**
     * Get the list of {@link XWPFEndnote} in the Endnotes part.
     *
     * @return List, possibly empty, of end notes.
     */
    public List<XWPFEndnote> getEndnotesList() {
        List<XWPFEndnote> resultList = new ArrayList<XWPFEndnote>();
        for (AbstractXWPFFootnoteEndnote note : listFootnote) {
            resultList.add((XWPFEndnote)note);
        }
        return resultList;
    }

    /**
     * Remove the specified end note if present.
     *
     * @param pos Array position of the endnote to be removed
     * @return True if the end note was removed.
     * @since 4.0.0
     */
    public boolean removeEndnote(int pos) {
        if (ctEndnotes.sizeOfEndnoteArray() >= pos - 1) {
            ctEndnotes.removeEndnote(pos);
            listFootnote.remove(pos);
            return true;
        } else {
            return false;
        }
    }

    

}
