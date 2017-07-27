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

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.IdentifierManager;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

/**
 * <p>High(ish) level class for working with .docx files.</p>
 * <p>
 * <p>This class tries to hide some of the complexity
 * of the underlying file format, but as it's not a
 * mature and stable API yet, certain parts of the
 * XML structure come through. You'll therefore almost
 * certainly need to refer to the OOXML specifications
 * from
 * http://www.ecma-international.org/publications/standards/Ecma-376.htm
 * at some point in your use.</p>
 */
public class XWPFDocument extends POIXMLDocument implements Document, IBody {
    private static final POILogger LOG = POILogFactory.getLogger(XWPFDocument.class);
    
    protected List<XWPFFooter> footers = new ArrayList<XWPFFooter>();
    protected List<XWPFHeader> headers = new ArrayList<XWPFHeader>();
    protected List<XWPFComment> comments = new ArrayList<XWPFComment>();
    protected List<XWPFHyperlink> hyperlinks = new ArrayList<XWPFHyperlink>();
    protected List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>();
    protected List<XWPFTable> tables = new ArrayList<XWPFTable>();
    protected List<XWPFSDT> contentControls = new ArrayList<XWPFSDT>();
    protected List<IBodyElement> bodyElements = new ArrayList<IBodyElement>();
    protected List<XWPFPictureData> pictures = new ArrayList<XWPFPictureData>();
    protected Map<Long, List<XWPFPictureData>> packagePictures = new HashMap<Long, List<XWPFPictureData>>();
    protected Map<Integer, XWPFFootnote> endnotes = new HashMap<Integer, XWPFFootnote>();
    protected XWPFNumbering numbering;
    protected XWPFStyles styles;
    protected XWPFFootnotes footnotes;
    private CTDocument1 ctDocument;
    private XWPFSettings settings;
    /**
     * Keeps track on all id-values used in this document and included parts, like headers, footers, etc.
     */
    private IdentifierManager drawingIdManager = new IdentifierManager(0L, 4294967295L);
    /**
     * Handles the joy of different headers/footers for different pages
     */
    private XWPFHeaderFooterPolicy headerFooterPolicy;

    public XWPFDocument(OPCPackage pkg) throws IOException {
        super(pkg);

        //build a tree of POIXMLDocumentParts, this document being the root
        load(XWPFFactory.getInstance());
    }

    public XWPFDocument(InputStream is) throws IOException {
        super(PackageHelper.open(is));

        //build a tree of POIXMLDocumentParts, this workbook being the root
        load(XWPFFactory.getInstance());
    }

    public XWPFDocument() {
        super(newPackage());
        onDocumentCreate();
    }

    /**
     * Create a new WordProcessingML package and setup the default minimal content
     */
    protected static OPCPackage newPackage() {
        try {
            OPCPackage pkg = OPCPackage.create(new ByteArrayOutputStream());
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(XWPFRelation.DOCUMENT.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT);
            // Create main document part
            pkg.createPart(corePartName, XWPFRelation.DOCUMENT.getContentType());

            pkg.getPackageProperties().setCreatorProperty(DOCUMENT_CREATOR);

            return pkg;
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDocumentRead() throws IOException {
        try {
            DocumentDocument doc = DocumentDocument.Factory.parse(getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
            ctDocument = doc.getDocument();

            initFootnotes();

            // parse the document with cursor and add
            // the XmlObject to its lists
            XmlCursor docCursor = ctDocument.newCursor();
            docCursor.selectPath("./*");
            while (docCursor.toNextSelection()) {
                XmlObject o = docCursor.getObject();
                if (o instanceof CTBody) {
                    XmlCursor bodyCursor = o.newCursor();
                    bodyCursor.selectPath("./*");
                    while (bodyCursor.toNextSelection()) {
                        XmlObject bodyObj = bodyCursor.getObject();
                        if (bodyObj instanceof CTP) {
                            XWPFParagraph p = new XWPFParagraph((CTP) bodyObj,
                                    this);
                            bodyElements.add(p);
                            paragraphs.add(p);
                        } else if (bodyObj instanceof CTTbl) {
                            XWPFTable t = new XWPFTable((CTTbl) bodyObj, this);
                            bodyElements.add(t);
                            tables.add(t);
                        } else if (bodyObj instanceof CTSdtBlock) {
                            XWPFSDT c = new XWPFSDT((CTSdtBlock) bodyObj, this);
                            bodyElements.add(c);
                            contentControls.add(c);
                        }
                    }
                    bodyCursor.dispose();
                }
            }
            docCursor.dispose();
            // Sort out headers and footers
            if (doc.getDocument().getBody().getSectPr() != null)
                headerFooterPolicy = new XWPFHeaderFooterPolicy(this);

            // Create for each XML-part in the Package a PartClass
            for (RelationPart rp : getRelationParts()) {
                POIXMLDocumentPart p = rp.getDocumentPart();
                String relation = rp.getRelationship().getRelationshipType();
                if (relation.equals(XWPFRelation.STYLES.getRelation())) {
                    this.styles = (XWPFStyles) p;
                    this.styles.onDocumentRead();
                } else if (relation.equals(XWPFRelation.NUMBERING.getRelation())) {
                    this.numbering = (XWPFNumbering) p;
                    this.numbering.onDocumentRead();
                } else if (relation.equals(XWPFRelation.FOOTER.getRelation())) {
                    XWPFFooter footer = (XWPFFooter) p;
                    footers.add(footer);
                    footer.onDocumentRead();
                } else if (relation.equals(XWPFRelation.HEADER.getRelation())) {
                    XWPFHeader header = (XWPFHeader) p;
                    headers.add(header);
                    header.onDocumentRead();
                } else if (relation.equals(XWPFRelation.COMMENT.getRelation())) {
                    // TODO Create according XWPFComment class, extending POIXMLDocumentPart
                    CommentsDocument cmntdoc = CommentsDocument.Factory.parse(p.getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
                    for (CTComment ctcomment : cmntdoc.getComments().getCommentArray()) {
                        comments.add(new XWPFComment(ctcomment, this));
                    }
                } else if (relation.equals(XWPFRelation.SETTINGS.getRelation())) {
                    settings = (XWPFSettings) p;
                    settings.onDocumentRead();
                } else if (relation.equals(XWPFRelation.IMAGES.getRelation())) {
                    XWPFPictureData picData = (XWPFPictureData) p;
                    picData.onDocumentRead();
                    registerPackagePictureData(picData);
                    pictures.add(picData);
                } else if (relation.equals(XWPFRelation.GLOSSARY_DOCUMENT.getRelation())) {
                    // We don't currently process the glossary itself
                    // Until we do, we do need to load the glossary child parts of it
                    for (POIXMLDocumentPart gp : p.getRelations()) {
                        // Trigger the onDocumentRead for all the child parts
                        // Otherwise we'll hit issues on Styles, Settings etc on save
                        // TODO: Refactor this to not need to access protected method
                        // from other package! Remove the static helper method once fixed!!!
                        POIXMLDocumentPart._invokeOnDocumentRead(gp);
                    }
                }
            }
            initHyperlinks();
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
    }

    private void initHyperlinks() {
        // Get the hyperlinks
        // TODO: make me optional/separated in private function
        try {
            Iterator<PackageRelationship> relIter =
                    getPackagePart().getRelationshipsByType(XWPFRelation.HYPERLINK.getRelation()).iterator();
            while (relIter.hasNext()) {
                PackageRelationship rel = relIter.next();
                hyperlinks.add(new XWPFHyperlink(rel.getId(), rel.getTargetURI().toString()));
            }
        } catch (InvalidFormatException e) {
            throw new POIXMLException(e);
        }
    }

    private void initFootnotes() throws XmlException, IOException {
        for (RelationPart rp : getRelationParts()) {
            POIXMLDocumentPart p = rp.getDocumentPart();
            String relation = rp.getRelationship().getRelationshipType();
            if (relation.equals(XWPFRelation.FOOTNOTE.getRelation())) {
                this.footnotes = (XWPFFootnotes) p;
                this.footnotes.onDocumentRead();
            } else if (relation.equals(XWPFRelation.ENDNOTE.getRelation())) {
                EndnotesDocument endnotesDocument = EndnotesDocument.Factory.parse(p.getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);

                for (CTFtnEdn ctFtnEdn : endnotesDocument.getEndnotes().getEndnoteArray()) {
                    endnotes.put(ctFtnEdn.getId().intValue(), new XWPFFootnote(this, ctFtnEdn));
                }
            }
        }
    }

    /**
     * Create a new CTWorkbook with all values set to default
     */
    @Override
    protected void onDocumentCreate() {
        ctDocument = CTDocument1.Factory.newInstance();
        ctDocument.addNewBody();

        settings = (XWPFSettings) createRelationship(XWPFRelation.SETTINGS, XWPFFactory.getInstance());

        POIXMLProperties.ExtendedProperties expProps = getProperties().getExtendedProperties();
        expProps.getUnderlyingProperties().setApplication(DOCUMENT_CREATOR);
    }

    /**
     * Returns the low level document base object
     */
    @Internal
    public CTDocument1 getDocument() {
        return ctDocument;
    }

    IdentifierManager getDrawingIdManager() {
        return drawingIdManager;
    }

    /**
     * returns an Iterator with paragraphs and tables
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getBodyElements()
     */
    @Override
    public List<IBodyElement> getBodyElements() {
        return Collections.unmodifiableList(bodyElements);
    }

    public Iterator<IBodyElement> getBodyElementsIterator() {
        return bodyElements.iterator();
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphs()
     */
    @Override
    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getTables()
     */
    @Override
    public List<XWPFTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getTableArray(int)
     */
    @Override
    public XWPFTable getTableArray(int pos) {
        if (pos >= 0 && pos < tables.size()) {
            return tables.get(pos);
        }
        return null;
    }

    /**
     * @return the list of footers
     */
    public List<XWPFFooter> getFooterList() {
        return Collections.unmodifiableList(footers);
    }

    public XWPFFooter getFooterArray(int pos) {
        if(pos >=0 && pos < footers.size()) {
            return footers.get(pos);
        }
        return null;
    }

    /**
     * @return the list of headers
     */
    public List<XWPFHeader> getHeaderList() {
        return Collections.unmodifiableList(headers);
    }

    public XWPFHeader getHeaderArray(int pos) {
        if(pos >=0 && pos < headers.size()) {
            return headers.get(pos);
        }
        return null;
    }

    public String getTblStyle(XWPFTable table) {
        return table.getStyleID();
    }

    public XWPFHyperlink getHyperlinkByID(String id) {
        for (XWPFHyperlink link : hyperlinks) {
            if (link.getId().equals(id))
                return link;
        }

        return null;
    }

    public XWPFFootnote getFootnoteByID(int id) {
        if (footnotes == null) return null;
        return footnotes.getFootnoteById(id);
    }

    public XWPFFootnote getEndnoteByID(int id) {
        if (endnotes == null) return null;
        return endnotes.get(id);
    }

    public List<XWPFFootnote> getFootnotes() {
        if (footnotes == null) {
            return Collections.emptyList();
        }
        return footnotes.getFootnotesList();
    }

    public XWPFHyperlink[] getHyperlinks() {
        return hyperlinks.toArray(new XWPFHyperlink[hyperlinks.size()]);
    }

    public XWPFComment getCommentByID(String id) {
        for (XWPFComment comment : comments) {
            if (comment.getId().equals(id))
                return comment;
        }

        return null;
    }

    public XWPFComment[] getComments() {
        return comments.toArray(new XWPFComment[comments.size()]);
    }

    /**
     * Get the document part that's defined as the
     * given relationship of the core document.
     */
    public PackagePart getPartById(String id) {
        try {
            PackagePart corePart = getCorePart();
            return corePart.getRelatedPart(corePart.getRelationship(id));
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the policy on headers and footers, which
     * also provides a way to get at them.
     */
    public XWPFHeaderFooterPolicy getHeaderFooterPolicy() {
        return headerFooterPolicy;
    }
    public XWPFHeaderFooterPolicy createHeaderFooterPolicy() {
        if (headerFooterPolicy == null) {
//            if (! ctDocument.getBody().isSetSectPr()) {
//                ctDocument.getBody().addNewSectPr();
//            }
            headerFooterPolicy = new XWPFHeaderFooterPolicy(this);
        }
        return headerFooterPolicy;
    }
    
    /**
     * Create a header of the given type
     *
     * @param type {@link HeaderFooterType} enum
     * @return object of type {@link XWPFHeader}
     */
    public XWPFHeader createHeader(HeaderFooterType type) {
        XWPFHeaderFooterPolicy hfPolicy = createHeaderFooterPolicy();
        // TODO this needs to be migrated out into section code
        if (type == HeaderFooterType.FIRST) {
            CTSectPr ctSectPr = getSection();
            if (ctSectPr.isSetTitlePg() == false) {
                CTOnOff titlePg = ctSectPr.addNewTitlePg();
                titlePg.setVal(STOnOff.ON);
            }
        // } else if (type == HeaderFooterType.EVEN) {
            // TODO Add support for Even/Odd headings and footers
        }
        return hfPolicy.createHeader(STHdrFtr.Enum.forInt(type.toInt()));
    }
    
    
    /**
     * Create a footer of the given type
     *
     * @param type {@link HeaderFooterType} enum
     * @return object of type {@link XWPFFooter}
     */
    public XWPFFooter createFooter(HeaderFooterType type) {
        XWPFHeaderFooterPolicy hfPolicy = createHeaderFooterPolicy();
        // TODO this needs to be migrated out into section code
        if (type == HeaderFooterType.FIRST) {
            CTSectPr ctSectPr = getSection();
            if (ctSectPr.isSetTitlePg() == false) {
                CTOnOff titlePg = ctSectPr.addNewTitlePg();
                titlePg.setVal(STOnOff.ON);
            }
        // } else if (type == HeaderFooterType.EVEN) {
            // TODO Add support for Even/Odd headings and footers
        }
        return hfPolicy.createFooter(STHdrFtr.Enum.forInt(type.toInt()));
    }

    /**
     * Return the {@link CTSectPr} object that corresponds with the
     * last section in this document.
     *
     * @return {@link CTSectPr} object
     */
    private CTSectPr getSection() {
        CTBody ctBody = getDocument().getBody();
        return (ctBody.isSetSectPr() ?
                ctBody.getSectPr() :
                ctBody.addNewSectPr());
    }

    /**
     * Returns the styles object used
     */
    @Internal
    public CTStyles getStyle() throws XmlException, IOException {
        PackagePart[] parts;
        try {
            parts = getRelatedByType(XWPFRelation.STYLES.getRelation());
        } catch (InvalidFormatException e) {
            throw new IllegalStateException(e);
        }
        if (parts.length != 1) {
            throw new IllegalStateException("Expecting one Styles document part, but found " + parts.length);
        }

        StylesDocument sd = StylesDocument.Factory.parse(parts[0].getInputStream(), DEFAULT_XML_OPTIONS);
        return sd.getStyles();
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<PackagePart>();

        // Get the embeddings for the workbook
        PackagePart part = getPackagePart();
        for (PackageRelationship rel : getPackagePart().getRelationshipsByType(OLE_OBJECT_REL_TYPE)) {
            embedds.add(part.getRelatedPart(rel));
        }

        for (PackageRelationship rel : getPackagePart().getRelationshipsByType(PACK_OBJECT_REL_TYPE)) {
            embedds.add(part.getRelatedPart(rel));
        }

        return embedds;
    }

    /**
     * Finds that for example the 2nd entry in the body list is the 1st paragraph
     */
    private int getBodyElementSpecificPos(int pos, List<? extends IBodyElement> list) {
        // If there's nothing to find, skip it
        if (list.size() == 0) {
            return -1;
        }

        if (pos >= 0 && pos < bodyElements.size()) {
            // Ensure the type is correct
            IBodyElement needle = bodyElements.get(pos);
            if (needle.getElementType() != list.get(0).getElementType()) {
                // Wrong type
                return -1;
            }

            // Work back until we find it
            int startPos = Math.min(pos, list.size() - 1);
            for (int i = startPos; i >= 0; i--) {
                if (list.get(i) == needle) {
                    return i;
                }
            }
        }

        // Couldn't be found
        return -1;
    }

    /**
     * Look up the paragraph at the specified position in the body elements list
     * and return this paragraphs position in the paragraphs list
     *
     * @param pos The position of the relevant paragraph in the body elements
     *            list
     * @return the position of the paragraph in the paragraphs list, if there is
     * a paragraph at the position in the bodyelements list. Else it
     * will return -1
     */
    public int getParagraphPos(int pos) {
        return getBodyElementSpecificPos(pos, paragraphs);
    }

    /**
     * get with the position of a table in the bodyelement array list
     * the position of this table in the table array list
     *
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getTablePos(int pos) {
        return getBodyElementSpecificPos(pos, tables);
    }

    /**
     * Add a new paragraph at position of the cursor. The cursor must be on the
     * {@link org.apache.xmlbeans.XmlCursor.TokenType#START} tag of an subelement
     * of the documents body. When this method is done, the cursor passed as
     * parameter points to the {@link org.apache.xmlbeans.XmlCursor.TokenType#END}
     * of the newly inserted paragraph.
     *
     * @param cursor
     * @return the {@link XWPFParagraph} object representing the newly inserted
     * CTP object
     */
    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (isCursorInBody(cursor)) {
            String uri = CTP.type.getName().getNamespaceURI();
            /*
             * TODO DO not use a coded constant, find the constant in the OOXML
             * classes instead, as the child of type CT_Paragraph is defined in the 
             * OOXML schema as 'p'
             */
            String localPart = "p";
            // creates a new Paragraph, cursor is positioned inside the new
            // element
            cursor.beginElement(localPart, uri);
            // move the cursor to the START token to the paragraph just created
            cursor.toParent();
            CTP p = (CTP) cursor.getObject();
            XWPFParagraph newP = new XWPFParagraph(p, this);
            XmlObject o = null;
            /*
             * move the cursor to the previous element until a) the next
             * paragraph is found or b) all elements have been passed
             */
            while (!(o instanceof CTP) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            /*
             * if the object that has been found is a) not a paragraph or b) is
             * the paragraph that has just been inserted, as the cursor in the
             * while loop above was not moved as there were no other siblings,
             * then the paragraph that was just inserted is the first paragraph
             * in the body. Otherwise, take the previous paragraph and calculate
             * the new index for the new paragraph.
             */
            if ((!(o instanceof CTP)) || (CTP) o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }

            /*
             * create a new cursor, that points to the START token of the just
             * inserted paragraph
             */
            XmlCursor newParaPos = p.newCursor();
            try {
                /*
                 * Calculate the paragraphs index in the list of all body
                 * elements
                 */
                int i = 0;
                cursor.toCursor(newParaPos);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl)
                        i++;
                }
                bodyElements.add(i, newP);
                cursor.toCursor(newParaPos);
                cursor.toEndToken();
                return newP;
            } finally {
                newParaPos.dispose();
            }
        }
        return null;
    }

    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInBody(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, this);
            XmlObject o = null;
            while (!(o instanceof CTTbl) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTTbl)) {
                tables.add(0, newT);
            } else {
                int pos = tables.indexOf(getTable((CTTbl) o)) + 1;
                tables.add(pos, newT);
            }
            int i = 0;
            XmlCursor tableCursor = t.newCursor();
            try {
                cursor.toCursor(tableCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl)
                        i++;
                }
                bodyElements.add(i, newT);
                cursor.toCursor(tableCursor);
                cursor.toEndToken();
                return newT;
            } finally {
                tableCursor.dispose();
            }
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     *
     * @param cursor
     */
    private boolean isCursorInBody(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        boolean result = (verify.getObject() == this.ctDocument.getBody());
        verify.dispose();
        return result;
    }

    private int getPosOfBodyElement(IBodyElement needle) {
        BodyElementType type = needle.getElementType();
        IBodyElement current;
        for (int i = 0; i < bodyElements.size(); i++) {
            current = bodyElements.get(i);
            if (current.getElementType() == type) {
                if (current.equals(needle)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get the position of the paragraph, within the list
     * of all the body elements.
     *
     * @param p The paragraph to find
     * @return The location, or -1 if the paragraph couldn't be found
     */
    public int getPosOfParagraph(XWPFParagraph p) {
        return getPosOfBodyElement(p);
    }

    /**
     * Get the position of the table, within the list of
     * all the body elements.
     *
     * @param t The table to find
     * @return The location, or -1 if the table couldn't be found
     */
    public int getPosOfTable(XWPFTable t) {
        return getPosOfBodyElement(t);
    }

    /**
     * commit and saves the document
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTDocument1.type.getName().getNamespaceURI(), "document"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctDocument.save(out, xmlOptions);
        out.close();
    }

    /**
     * Gets the index of the relation we're trying to create
     *
     * @param relation
     * @return i
     */
    private int getRelationIndex(XWPFRelation relation) {
        int i = 1;
        for (RelationPart rp : getRelationParts()) {
            if (rp.getRelationship().getRelationshipType().equals(relation.getRelation())) {
                i++;
            }
        }
        return i;
    }

    /**
     * Appends a new paragraph to this document
     *
     * @return a new paragraph
     */
    public XWPFParagraph createParagraph() {
        XWPFParagraph p = new XWPFParagraph(ctDocument.getBody().addNewP(), this);
        bodyElements.add(p);
        paragraphs.add(p);
        return p;
    }

    /**
     * Creates an empty numbering if one does not already exist and sets the numbering member
     *
     * @return numbering
     */
    public XWPFNumbering createNumbering() {
        if (numbering == null) {
            NumberingDocument numberingDoc = NumberingDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.NUMBERING;
            int i = getRelationIndex(relation);

            XWPFNumbering wrapper = (XWPFNumbering) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setNumbering(numberingDoc.addNewNumbering());
            numbering = wrapper;
        }

        return numbering;
    }

    /**
     * Creates an empty styles for the document if one does not already exist
     *
     * @return styles
     */
    public XWPFStyles createStyles() {
        if (styles == null) {
            StylesDocument stylesDoc = StylesDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.STYLES;
            int i = getRelationIndex(relation);

            XWPFStyles wrapper = (XWPFStyles) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setStyles(stylesDoc.addNewStyles());
            styles = wrapper;
        }

        return styles;
    }

    /**
     * Creates an empty footnotes element for the document if one does not already exist
     *
     * @return footnotes
     */
    public XWPFFootnotes createFootnotes() {
        if (footnotes == null) {
            FootnotesDocument footnotesDoc = FootnotesDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.FOOTNOTE;
            int i = getRelationIndex(relation);

            XWPFFootnotes wrapper = (XWPFFootnotes) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setFootnotes(footnotesDoc.addNewFootnotes());
            footnotes = wrapper;
        }

        return footnotes;
    }

    public XWPFFootnote addFootnote(CTFtnEdn note) {
        return footnotes.addFootnote(note);
    }

    public XWPFFootnote addEndnote(CTFtnEdn note) {
        XWPFFootnote endnote = new XWPFFootnote(this, note);
        endnotes.put(note.getId().intValue(), endnote);
        return endnote;
    }

    /**
     * remove a BodyElement from bodyElements array list
     *
     * @param pos
     * @return true if removing was successfully, else return false
     */
    public boolean removeBodyElement(int pos) {
        if (pos >= 0 && pos < bodyElements.size()) {
            BodyElementType type = bodyElements.get(pos).getElementType();
            if (type == BodyElementType.TABLE) {
                int tablePos = getTablePos(pos);
                tables.remove(tablePos);
                ctDocument.getBody().removeTbl(tablePos);
            }
            if (type == BodyElementType.PARAGRAPH) {
                int paraPos = getParagraphPos(pos);
                paragraphs.remove(paraPos);
                ctDocument.getBody().removeP(paraPos);
            }
            bodyElements.remove(pos);
            return true;
        }
        return false;
    }

    /**
     * copies content of a paragraph to a existing paragraph in the list paragraphs at position pos
     *
     * @param paragraph
     * @param pos
     */
    public void setParagraph(XWPFParagraph paragraph, int pos) {
        paragraphs.set(pos, paragraph);
        ctDocument.getBody().setPArray(pos, paragraph.getCTP());
        /* TODO update body element, update xwpf element, verify that
         * incoming paragraph belongs to this document or if not, XML was
         * copied properly (namespace-abbreviations, etc.)
         */
    }

    /**
     * @return the LastParagraph of the document
     */
    public XWPFParagraph getLastParagraph() {
        int lastPos = paragraphs.toArray().length - 1;
        return paragraphs.get(lastPos);
    }

    /**
     * Create an empty table with one row and one column as default.
     *
     * @return a new table
     */
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctDocument.getBody().addNewTbl(), this);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    /**
     * Create an empty table with a number of rows and cols specified
     *
     * @param rows
     * @param cols
     * @return table
     */
    public XWPFTable createTable(int rows, int cols) {
        XWPFTable table = new XWPFTable(ctDocument.getBody().addNewTbl(), this, rows, cols);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    /**
     *
     */
    public void createTOC() {
        CTSdtBlock block = this.getDocument().getBody().addNewSdt();
        TOC toc = new TOC(block);
        for (XWPFParagraph par : paragraphs) {
            String parStyle = par.getStyle();
            if (parStyle != null && parStyle.startsWith("Heading")) {
                try {
                    int level = Integer.parseInt(parStyle.substring("Heading".length()));
                    toc.addRow(level, par.getText(), 1, "112723803");
                } catch (NumberFormatException e) {
                    LOG.log(POILogger.ERROR, "can't format number in TOC heading", e);
                }
            }
        }
    }

    /**
     * Replace content of table in array tables at position pos with a
     *
     * @param pos
     * @param table
     */
    public void setTable(int pos, XWPFTable table) {
        tables.set(pos, table);
        ctDocument.getBody().setTblArray(pos, table.getCTTbl());
    }
	
	/**
     * Verifies that the documentProtection tag in settings.xml file <br>
     * specifies that the protection is enforced (w:enforcement="1") <br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option any
     */
    public boolean isEnforcedProtection() {
        return settings.isEnforcedWith();
    }

    /**
     * Verifies that the documentProtection tag in settings.xml file <br>
     * specifies that the protection is enforced (w:enforcement="1") <br>
     * and that the kind of protection is readOnly (w:edit="readOnly")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option readOnly
     */
    public boolean isEnforcedReadonlyProtection() {
        return settings.isEnforcedWith(STDocProtect.READ_ONLY);
    }

    /**
     * Verifies that the documentProtection tag in settings.xml file <br>
     * specifies that the protection is enforced (w:enforcement="1") <br>
     * and that the kind of protection is forms (w:edit="forms")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;forms&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option forms
     */
    public boolean isEnforcedFillingFormsProtection() {
        return settings.isEnforcedWith(STDocProtect.FORMS);
    }

    /**
     * Verifies that the documentProtection tag in settings.xml file <br>
     * specifies that the protection is enforced (w:enforcement="1") <br>
     * and that the kind of protection is comments (w:edit="comments")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;comments&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option comments
     */
    public boolean isEnforcedCommentsProtection() {
        return settings.isEnforcedWith(STDocProtect.COMMENTS);
    }

    /**
     * Verifies that the documentProtection tag in settings.xml file <br>
     * specifies that the protection is enforced (w:enforcement="1") <br>
     * and that the kind of protection is trackedChanges (w:edit="trackedChanges")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;trackedChanges&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option trackedChanges
     */
    public boolean isEnforcedTrackedChangesProtection() {
        return settings.isEnforcedWith(STDocProtect.TRACKED_CHANGES);
    }

    public boolean isEnforcedUpdateFields() {
        return settings.isUpdateFields();
    }

    /**
     * Enforces the readOnly protection.<br>
     * In the documentProtection tag inside settings.xml file, <br>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br>
     * and the value of edit to readOnly (w:edit="readOnly")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void enforceReadonlyProtection() {
        settings.setEnforcementEditValue(STDocProtect.READ_ONLY);
    }

    /**
     * Enforces the readOnly protection with a password.<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *   &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;
     *       w:cryptProviderType=&quot;rsaAES&quot; w:cryptAlgorithmClass=&quot;hash&quot;
     *       w:cryptAlgorithmType=&quot;typeAny&quot; w:cryptAlgorithmSid=&quot;14&quot;
     *       w:cryptSpinCount=&quot;100000&quot; w:hash=&quot;...&quot; w:salt=&quot;....&quot;
     *   /&gt;
     * </pre>
     *
     * @param password the plaintext password, if null no password will be applied
     * @param hashAlgo the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                 if null, it will default default to sha1
     */
    public void enforceReadonlyProtection(String password, HashAlgorithm hashAlgo) {
        settings.setEnforcementEditValue(STDocProtect.READ_ONLY, password, hashAlgo);
    }

    /**
     * Enforce the Filling Forms protection.<br>
     * In the documentProtection tag inside settings.xml file, <br>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br>
     * and the value of edit to forms (w:edit="forms")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;forms&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void enforceFillingFormsProtection() {
        settings.setEnforcementEditValue(STDocProtect.FORMS);
    }

    /**
     * Enforce the Filling Forms protection.<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *   &lt;w:documentProtection w:edit=&quot;forms&quot; w:enforcement=&quot;1&quot;
     *       w:cryptProviderType=&quot;rsaAES&quot; w:cryptAlgorithmClass=&quot;hash&quot;
     *       w:cryptAlgorithmType=&quot;typeAny&quot; w:cryptAlgorithmSid=&quot;14&quot;
     *       w:cryptSpinCount=&quot;100000&quot; w:hash=&quot;...&quot; w:salt=&quot;....&quot;
     *   /&gt;
     * </pre>
     *
     * @param password the plaintext password, if null no password will be applied
     * @param hashAlgo the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                 if null, it will default default to sha1
     */
    public void enforceFillingFormsProtection(String password, HashAlgorithm hashAlgo) {
        settings.setEnforcementEditValue(STDocProtect.FORMS, password, hashAlgo);
    }

    /**
     * Enforce the Comments protection.<br>
     * In the documentProtection tag inside settings.xml file,<br>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br>
     * and the value of edit to comments (w:edit="comments")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;comments&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void enforceCommentsProtection() {
        settings.setEnforcementEditValue(STDocProtect.COMMENTS);
    }

    /**
     * Enforce the Comments protection.<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *   &lt;w:documentProtection w:edit=&quot;comments&quot; w:enforcement=&quot;1&quot;
     *       w:cryptProviderType=&quot;rsaAES&quot; w:cryptAlgorithmClass=&quot;hash&quot;
     *       w:cryptAlgorithmType=&quot;typeAny&quot; w:cryptAlgorithmSid=&quot;14&quot;
     *       w:cryptSpinCount=&quot;100000&quot; w:hash=&quot;...&quot; w:salt=&quot;....&quot;
     *   /&gt;
     * </pre>
     *
     * @param password the plaintext password, if null no password will be applied
     * @param hashAlgo the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                 if null, it will default default to sha1
     */
    public void enforceCommentsProtection(String password, HashAlgorithm hashAlgo) {
        settings.setEnforcementEditValue(STDocProtect.COMMENTS, password, hashAlgo);
    }

    /**
     * Enforce the Tracked Changes protection.<br>
     * In the documentProtection tag inside settings.xml file, <br>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br>
     * and the value of edit to trackedChanges (w:edit="trackedChanges")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;trackedChanges&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void enforceTrackedChangesProtection() {
        settings.setEnforcementEditValue(STDocProtect.TRACKED_CHANGES);
    }

    /**
     * Enforce the Tracked Changes protection.<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *   &lt;w:documentProtection w:edit=&quot;trackedChanges&quot; w:enforcement=&quot;1&quot;
     *       w:cryptProviderType=&quot;rsaAES&quot; w:cryptAlgorithmClass=&quot;hash&quot;
     *       w:cryptAlgorithmType=&quot;typeAny&quot; w:cryptAlgorithmSid=&quot;14&quot;
     *       w:cryptSpinCount=&quot;100000&quot; w:hash=&quot;...&quot; w:salt=&quot;....&quot;
     *   /&gt;
     * </pre>
     *
     * @param password the plaintext password, if null no password will be applied
     * @param hashAlgo the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                 if null, it will default default to sha1
     */
    public void enforceTrackedChangesProtection(String password, HashAlgorithm hashAlgo) {
        settings.setEnforcementEditValue(STDocProtect.TRACKED_CHANGES, password, hashAlgo);
    }

    /**
     * Validates the existing password
     *
     * @param password
     * @return true, only if password was set and equals, false otherwise
     */
    public boolean validateProtectionPassword(String password) {
        return settings.validateProtectionPassword(password);
    }

    /**
     * Remove protection enforcement.<br>
     * In the documentProtection tag inside settings.xml file <br>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br>
     */
    public void removeProtectionEnforcement() {
        settings.removeEnforcement();
    }

    /**
     * Enforces fields update on document open (in Word).
     * In the settings.xml file <br>
     * sets the updateSettings value to true (w:updateSettings w:val="true")
     * <p>
     * NOTICES:
     * <ul>
     * <li>Causing Word to ask on open: "This document contains fields that may refer to other files. Do you want to update the fields in this document?"
     * (if "Update automatic links at open" is enabled)</li>
     * <li>Flag is removed after saving with changes in Word </li>
     * </ul>
     */
    public void enforceUpdateFields() {
        settings.setUpdateFields();
    }

    /**
     * Check if revision tracking is turned on.
     *
     * @return <code>true</code> if revision tracking is turned on
     */
    public boolean isTrackRevisions() {
        return settings.isTrackRevisions();
    }

    /**
     * Enable or disable revision tracking.
     *
     * @param enable <code>true</code> to turn on revision tracking, <code>false</code> to turn off revision tracking
     */
    public void setTrackRevisions(boolean enable) {
        settings.setTrackRevisions(enable);
    }


    /**
     * Returns the current zoom factor in percent values, i.e. 100 is normal zoom.
     *
     * @return A percent value denoting the current zoom setting of this document.
     */
    public long getZoomPercent() {
        return settings.getZoomPercent();
    }

    /**
     * Set the zoom setting as percent value, i.e. 100 is normal zoom.
     *
     * @param zoomPercent A percent value denoting the zoom setting for this document.
     */
    public void setZoomPercent(long zoomPercent) {
        settings.setZoomPercent(zoomPercent);
    }
    
    /**
     * inserts an existing XWPFTable to the arrays bodyElements and tables
     *
     * @param pos
     * @param table
     */
    @Override
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i = 0;
        for (CTTbl tbl : ctDocument.getBody().getTblArray()) {
            if (tbl == table.getCTTbl()) {
                break;
            }
            i++;
        }
        tables.add(i, table);
    }

    /**
     * Returns all Pictures, which are referenced from the document itself.
     *
     * @return a {@link List} of {@link XWPFPictureData}. The returned {@link List} is unmodifiable. Use #a
     */
    public List<XWPFPictureData> getAllPictures() {
        return Collections.unmodifiableList(pictures);
    }

    /**
     * @return all Pictures in this package
     */
    public List<XWPFPictureData> getAllPackagePictures() {
        List<XWPFPictureData> result = new ArrayList<XWPFPictureData>();
        Collection<List<XWPFPictureData>> values = packagePictures.values();
        for (List<XWPFPictureData> list : values) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    void registerPackagePictureData(XWPFPictureData picData) {
        List<XWPFPictureData> list = packagePictures.get(picData.getChecksum());
        if (list == null) {
            list = new ArrayList<XWPFPictureData>(1);
            packagePictures.put(picData.getChecksum(), list);
        }
        if (!list.contains(picData)) {
            list.add(picData);
        }
    }

    XWPFPictureData findPackagePictureData(byte[] pictureData, int format) {
        long checksum = IOUtils.calculateChecksum(pictureData);
        XWPFPictureData xwpfPicData = null;
        /*
         * Try to find PictureData with this checksum. Create new, if none
         * exists.
         */
        List<XWPFPictureData> xwpfPicDataList = packagePictures.get(checksum);
        if (xwpfPicDataList != null) {
            Iterator<XWPFPictureData> iter = xwpfPicDataList.iterator();
            while (iter.hasNext() && xwpfPicData == null) {
                XWPFPictureData curElem = iter.next();
                if (Arrays.equals(pictureData, curElem.getData())) {
                    xwpfPicData = curElem;
                }
            }
        }
        return xwpfPicData;
    }

    public String addPictureData(byte[] pictureData, int format) throws InvalidFormatException {
        XWPFPictureData xwpfPicData = findPackagePictureData(pictureData, format);
        POIXMLRelation relDesc = XWPFPictureData.RELATIONS[format];

        if (xwpfPicData == null) {
            /* Part doesn't exist, create a new one */
            int idx = getNextPicNameNumber(format);
            xwpfPicData = (XWPFPictureData) createRelationship(relDesc, XWPFFactory.getInstance(), idx);
            /* write bytes to new part */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            OutputStream out = null;
            try {
                out = picDataPart.getOutputStream();
                out.write(pictureData);
            } catch (IOException e) {
                throw new POIXMLException(e);
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            registerPackagePictureData(xwpfPicData);
            pictures.add(xwpfPicData);

            return getRelationId(xwpfPicData);
        } else if (!getRelations().contains(xwpfPicData)) {
            /*
             * Part already existed, but was not related so far. Create
             * relationship to the already existing part and update
             * POIXMLDocumentPart data.
             */
            // TODO add support for TargetMode.EXTERNAL relations.
            RelationPart rp = addRelation(null, XWPFRelation.IMAGES, xwpfPicData);
            return rp.getRelationship().getId();
        } else {
            /* Part already existed, get relation id and return it */
            return getRelationId(xwpfPicData);
        }
    }

    public String addPictureData(InputStream is, int format) throws InvalidFormatException {
        try {
            byte[] data = IOUtils.toByteArray(is);
            return addPictureData(data, format);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the next free ImageNumber
     *
     * @param format
     * @return the next free ImageNumber
     * @throws InvalidFormatException
     */
    public int getNextPicNameNumber(int format) throws InvalidFormatException {
        int img = getAllPackagePictures().size() + 1;
        String proposal = XWPFPictureData.RELATIONS[format].getFileName(img);
        PackagePartName createPartName = PackagingURIHelper.createPartName(proposal);
        while (this.getPackage().getPart(createPartName) != null) {
            img++;
            proposal = XWPFPictureData.RELATIONS[format].getFileName(img);
            createPartName = PackagingURIHelper.createPartName(proposal);
        }
        return img;
    }

    /**
     * returns the PictureData by blipID
     *
     * @param blipID
     * @return XWPFPictureData of a specificID
     */
    public XWPFPictureData getPictureDataByID(String blipID) {
        POIXMLDocumentPart relatedPart = getRelationById(blipID);
        if (relatedPart instanceof XWPFPictureData) {
            XWPFPictureData xwpfPicData = (XWPFPictureData) relatedPart;
            return xwpfPicData;
        }
        return null;
    }

    /**
     * getNumbering
     *
     * @return numbering
     */
    public XWPFNumbering getNumbering() {
        return numbering;
    }

    /**
     * get Styles
     *
     * @return styles for this document
     */
    public XWPFStyles getStyles() {
        return styles;
    }

    /**
     * get the paragraph with the CTP class p
     *
     * @param p
     * @return the paragraph with the CTP class p
     */
    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (int i = 0; i < getParagraphs().size(); i++) {
            if (getParagraphs().get(i).getCTP() == p) {
                return getParagraphs().get(i);
            }
        }
        return null;
    }

    /**
     * get a table by its CTTbl-Object
     *
     * @param ctTbl
     * @return a table by its CTTbl-Object or null
     * @see org.apache.poi.xwpf.usermodel.IBody#getTable(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl)
     */
    @Override
    public XWPFTable getTable(CTTbl ctTbl) {
        for (int i = 0; i < tables.size(); i++) {
            if (getTables().get(i).getCTTbl() == ctTbl) {
                return getTables().get(i);
            }
        }
        return null;
    }

    public Iterator<XWPFTable> getTablesIterator() {
        return tables.iterator();
    }

    public Iterator<XWPFParagraph> getParagraphsIterator() {
        return paragraphs.iterator();
    }

    /**
     * Returns the paragraph that of position pos
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int)
     */
    @Override
    public XWPFParagraph getParagraphArray(int pos) {
        if (pos >= 0 && pos < paragraphs.size()) {
            return paragraphs.get(pos);
        }
        return null;
    }

    /**
     * returns the Part, to which the body belongs, which you need for adding relationship to other parts
     * Actually it is needed of the class XWPFTableCell. Because you have to know to which part the tableCell
     * belongs.
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    @Override
    public POIXMLDocumentPart getPart() {
        return this;
    }


    /**
     * get the PartType of the body, for example
     * DOCUMENT, HEADER, FOOTER, FOOTNOTE,
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    @Override
    public BodyType getPartType() {
        return BodyType.DOCUMENT;
    }

    /**
     * get the TableCell which belongs to the TableCell
     *
     * @param cell
     */
    @Override
    public XWPFTableCell getTableCell(CTTc cell) {
        XmlCursor cursor = cell.newCursor();
        cursor.toParent();
        XmlObject o = cursor.getObject();
        if (!(o instanceof CTRow)) {
            return null;
        }
        CTRow row = (CTRow) o;
        cursor.toParent();
        o = cursor.getObject();
        cursor.dispose();
        if (!(o instanceof CTTbl)) {
            return null;
        }
        CTTbl tbl = (CTTbl) o;
        XWPFTable table = getTable(tbl);
        if (table == null) {
            return null;
        }
        XWPFTableRow tableRow = table.getRow(row);
        if (tableRow == null) {
            return null;
        }
        return tableRow.getTableCell(cell);
    }

    @Override
    public XWPFDocument getXWPFDocument() {
        return this;
    }
}
