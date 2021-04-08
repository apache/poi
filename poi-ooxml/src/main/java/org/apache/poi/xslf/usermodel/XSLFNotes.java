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
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.NotesDocument;

@Beta
public final class XSLFNotes extends XSLFSheet
implements Notes<XSLFShape,XSLFTextParagraph> {
   private CTNotesSlide _notes;

    /**
     * Create a new notes
     */
    XSLFNotes() {
        _notes = prototype();
    }

    /**
     * Construct a SpreadsheetML notes from a package part
     *
     * @param part the package part holding the notes data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.notes+xml</code>
     *
     * @since POI 3.14-Beta1
     */
    XSLFNotes(PackagePart part) throws IOException, XmlException {
        super(part);

        NotesDocument doc =
            NotesDocument.Factory.parse(getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
        _notes = doc.getNotes();
    }

    private static CTNotesSlide prototype(){
        CTNotesSlide ctNotes = CTNotesSlide.Factory.newInstance();
        CTCommonSlideData cSld = ctNotes.addNewCSld();
        cSld.addNewSpTree();

        return ctNotes;
    }

    @Override
    public CTNotesSlide getXmlObject() {
       return _notes;
    }

    @Override
    protected String getRootElementName(){
        return "notes";
    }

    @Override
    public XSLFTheme getTheme(){
        final XSLFNotesMaster m = getMasterSheet();
    	return (m != null) ? m.getTheme() : null;
    }

    @Override
    public XSLFNotesMaster getMasterSheet() {
        for (POIXMLDocumentPart p : getRelations()) {
           if (p instanceof XSLFNotesMaster){
              return (XSLFNotesMaster)p;
           }
        }
        return null;
    }

    @Override
    public List<List<XSLFTextParagraph>> getTextParagraphs() {
        List<List<XSLFTextParagraph>> tp = new ArrayList<>();
        for (XSLFShape sh : super.getShapes()) {
            if (sh instanceof XSLFTextShape) {
                XSLFTextShape txt = (XSLFTextShape)sh;
                tp.add(txt.getTextParagraphs());
            }
        }
        return tp;
    }

    @Override
    String mapSchemeColor(String schemeColor) {
        return mapSchemeColor(_notes.getClrMapOvr(), schemeColor);
    }
}
