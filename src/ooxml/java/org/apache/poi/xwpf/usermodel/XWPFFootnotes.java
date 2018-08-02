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
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFootnotes;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FootnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn;

/**
 * Looks after the collection of Footnotes for a document.
 * Manages bottom-of-the-page footnotes ({@link XWPFFootnote}).
 */
public class XWPFFootnotes extends XWPFAbstractFootnotesEndnotes {
    protected CTFootnotes ctFootnotes;

    /**
     * Construct XWPFFootnotes from a package part
     *
     * @param part the package part holding the data of the footnotes,
     * 
     * @since POI 3.14-Beta1
     */
    public XWPFFootnotes(PackagePart part) throws IOException, OpenXML4JException {
        super(part);
    }
    
    /**
     * Construct XWPFFootnotes from scratch for a new document.
     */
    public XWPFFootnotes() {
    }

    /**
     * Sets the ctFootnotes
     *
     * @param footnotes Collection of CTFntEdn objects.
     */
    @Internal
    public void setFootnotes(CTFootnotes footnotes) {
        ctFootnotes = footnotes;
    }

    /**
     * Create a new footnote and add it to the document. 
     *
     * @return New {@link XWPFFootnote}
     * @since 4.0.0
     */
    public XWPFFootnote createFootnote() {
        CTFtnEdn newNote = CTFtnEdn.Factory.newInstance(); 
        newNote.setType(STFtnEdn.NORMAL);

        XWPFFootnote footnote = addFootnote(newNote);
        footnote.getCTFtnEdn().setId(getIdManager().nextId());
        return footnote;
        
    }

    /**
     * Remove the specified footnote if present.
     *
     * @param pos Array position of the footnote to be removed
     * @return True if the footnote was removed.
     * @since 4.0.0
     */
    public boolean removeFootnote(int pos) {
        if (ctFootnotes.sizeOfFootnoteArray() >= pos - 1) {
            ctFootnotes.removeFootnote(pos);
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
        FootnotesDocument notesDoc;
        InputStream is = null;
        try {
            is = getPackagePart().getInputStream();
            notesDoc = FootnotesDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctFootnotes = notesDoc.getFootnotes();
        } catch (XmlException e) {
            throw new POIXMLException();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    
        for (CTFtnEdn note : ctFootnotes.getFootnoteList()) {
            listFootnote.add(new XWPFFootnote(note, this));
        }
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTFootnotes.type.getName().getNamespaceURI(), "footnotes"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctFootnotes.save(out, xmlOptions);
        out.close();
    }

    /**
     * Add an {@link XWPFFootnote} to the document
     *
     * @param footnote Footnote to add
     * @throws IOException
     */
    public void addFootnote(XWPFFootnote footnote) {
        listFootnote.add(footnote);
        ctFootnotes.addNewFootnote().set(footnote.getCTFtnEdn());
    }

    /**
     * Add a CT footnote to the document
     *
     * @param note CTFtnEdn to add.
     * @throws IOException
     */
    @Internal
    public XWPFFootnote addFootnote(CTFtnEdn note) {
        CTFtnEdn newNote = ctFootnotes.addNewFootnote();
        newNote.set(note);
        XWPFFootnote xNote = new XWPFFootnote(newNote, this);
        listFootnote.add(xNote);
        return xNote;
    }

    /**
     * Get the list of {@link XWPFFootnote} in the Footnotes part.
     *
     * @return List, possibly empty, of footnotes.
     */
    public List<XWPFFootnote> getFootnotesList() {
        List<XWPFFootnote> resultList = new ArrayList<XWPFFootnote>();
        for (XWPFAbstractFootnoteEndnote note : listFootnote) {
            resultList.add((XWPFFootnote)note);
        }
        return resultList;
    }
    
    

}
