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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import javax.xml.namespace.QName;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.ooxml.util.IdentifierManager;
import org.apache.poi.ooxml.util.PackageHelper;
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
import org.apache.poi.util.Internal;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.EndnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FootnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.NumberingDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;

/**
 * High(ish) level class for working with .docx files.
 * <p>
 * This class tries to hide some of the complexity
 * of the underlying file format, but as it's not a
 * mature and stable API yet, certain parts of the
 * XML structure come through. You'll therefore almost
 * certainly need to refer to the OOXML specifications
 * from
 * https://www.ecma-international.org/publications/standards/Ecma-376.htm
 * at some point in your use.
 */
@SuppressWarnings("unused")
public class XWPFDocument extends POIXMLDocument implements Document, IBody {
    private static final Logger LOG = LogManager.getLogger(XWPFDocument.class);

    protected List<XWPFFooter> footers = new ArrayList<>();
    protected List<XWPFHeader> headers = new ArrayList<>();
    protected List<XWPFHyperlink> hyperlinks = new ArrayList<>();
    protected List<XWPFParagraph> paragraphs = new ArrayList<>();
    protected List<XWPFTable> tables = new ArrayList<>();
    protected List<XWPFSDT> contentControls = new ArrayList<>();
    protected List<IBodyElement> bodyElements = new ArrayList<>();
    protected List<XWPFPictureData> pictures = new ArrayList<>();
    protected Map<Long, List<XWPFPictureData>> packagePictures = new HashMap<>();
    protected XWPFEndnotes endnotes;
    protected XWPFNumbering numbering;
    protected XWPFStyles styles;
    protected XWPFTheme theme;
    protected XWPFFootnotes footnotes;
    private CTDocument1 ctDocument;
    private XWPFSettings settings;
    private XWPFComments comments;
    protected final List<XWPFChart> charts = new ArrayList<>();
    /**
     * Keeps track on all id-values used in this document and included parts, like headers, footers, etc.
     */
    private final IdentifierManager drawingIdManager = new IdentifierManager(0L, 4294967295L);

    private final FootnoteEndnoteIdManager footnoteIdManager = new FootnoteEndnoteIdManager(this);

    /**
     * Handles the joy of different headers/footers for different pages
     */
    private XWPFHeaderFooterPolicy headerFooterPolicy;

    /**
     * @param pkg OPC package
     * @throws IOException If reading data from the package fails
     * @throws POIXMLException a RuntimeException that can be caused by invalid OOXML data
     * @throws IllegalStateException a number of other runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
    public XWPFDocument(OPCPackage pkg) throws IOException {
        super(pkg);

        //build a tree of POIXMLDocumentParts, this document being the root
        load(XWPFFactory.getInstance());
    }

    /**
     * @param is The InputStream to read data from
     * @throws IOException If reading data from the stream fails
     * @throws POIXMLException a RuntimeException that can be caused by invalid OOXML data
     * @throws IllegalStateException a number of other runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
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
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.create(UnsynchronizedByteArrayOutputStream.builder().get());    // NOSONAR - we do not want to close this here
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(XWPFRelation.DOCUMENT.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT);
            // Create main document part
            pkg.createPart(corePartName, XWPFRelation.DOCUMENT.getContentType());

            pkg.getPackageProperties().setCreatorProperty(DOCUMENT_CREATOR);

            return pkg;
        } catch (Exception e) {
            IOUtils.closeQuietly(pkg);
            throw new POIXMLException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDocumentRead() throws IOException {
        try {
            DocumentDocument doc;
            try (InputStream stream = getPackagePart().getInputStream()) {
                doc = DocumentDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
                ctDocument = doc.getDocument();
            }

            initFootnotes();

            // parse the document with cursor and add
            // the XmlObject to its lists
            try (XmlCursor docCursor = ctDocument.newCursor()) {
                docCursor.selectPath("./*");
                while (docCursor.toNextSelection()) {
                    XmlObject o = docCursor.getObject();
                    if (o instanceof CTBody) {
                        try (XmlCursor bodyCursor = o.newCursor()) {
                            bodyCursor.selectPath("./*");
                            while (bodyCursor.toNextSelection()) {
                                XmlObject bodyObj = bodyCursor.getObject();
                                if (bodyObj instanceof CTP) {
                                    XWPFParagraph p = new XWPFParagraph((CTP) bodyObj, this);
                                    bodyElements.add(p);
                                    paragraphs.add(p);
                                } else if (bodyObj instanceof CTTbl) {
                                    XWPFTable t = new XWPFTable((CTTbl) bodyObj, this, false);
                                    bodyElements.add(t);
                                    tables.add(t);
                                } else if (bodyObj instanceof CTSdtBlock) {
                                    XWPFSDT c = new XWPFSDT((CTSdtBlock) bodyObj, this);
                                    bodyElements.add(c);
                                    contentControls.add(c);
                                }
                            }
                        }
                    }
                }
            }
            // Sort out headers and footers
            if (doc.getDocument().getBody() != null && doc.getDocument().getBody().getSectPr() != null) {
                headerFooterPolicy = new XWPFHeaderFooterPolicy(this);
            }

            // Create for each XML-part in the Package a PartClass
            for (RelationPart rp : getRelationParts()) {
                POIXMLDocumentPart p = rp.getDocumentPart();
                String relation = rp.getRelationship().getRelationshipType();
                try {
                    if (relation.equals(XWPFRelation.STYLES.getRelation())) {
                        this.styles = (XWPFStyles) p;
                        this.styles.onDocumentRead();
                    } else if (relation.equals(XWPFRelation.THEME.getRelation())) {
                        this.theme = (XWPFTheme) p;
                        this.theme.onDocumentRead();
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
                        this.comments = (XWPFComments) p;
                        this.comments.onDocumentRead();
                    } else if (relation.equals(XWPFRelation.SETTINGS.getRelation())) {
                        settings = (XWPFSettings) p;
                        settings.onDocumentRead();
                    } else if (relation.equals(XWPFRelation.IMAGES.getRelation())) {
                        XWPFPictureData picData = (XWPFPictureData) p;
                        picData.onDocumentRead();
                        registerPackagePictureData(picData);
                        pictures.add(picData);
                    } else if (relation.equals(XWPFRelation.CHART.getRelation())) {
                        //now we can use all methods to modify charts in XWPFDocument
                        XWPFChart chartData = (XWPFChart) p;
                        charts.add(chartData);
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
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Relation and type of document-part did not match, had relation " + relation + " and type of document-part: " + p.getClass(), e);
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
            hyperlinks = new ArrayList<>();
            for (PackageRelationship rel : getPackagePart().getRelationshipsByType(XWPFRelation.HYPERLINK.getRelation())) {
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
            if (relation.equals(XWPFRelation.FOOTNOTE.getRelation()) && p instanceof XWPFFootnotes) {
                this.footnotes = (XWPFFootnotes) p;
                this.footnotes.onDocumentRead();
                this.footnotes.setIdManager(footnoteIdManager);
            } else if (relation.equals(XWPFRelation.ENDNOTE.getRelation()) && p instanceof XWPFEndnotes) {
                this.endnotes = (XWPFEndnotes) p;
                this.endnotes.onDocumentRead();
                this.endnotes.setIdManager(footnoteIdManager);
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
     */
    @Override
    public List<IBodyElement> getBodyElements() {
        return Collections.unmodifiableList(bodyElements);
    }

    public Iterator<IBodyElement> getBodyElementsIterator() {
        return bodyElements.iterator();
    }

    /**
     * returns a Spliterator with paragraphs and tables
     *
     * @since POI 5.2.0
     */
    public Spliterator<IBodyElement> getBodyElementsSpliterator() {
        return bodyElements.spliterator();
    }

    @Override
    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    @Override
    public List<XWPFTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    /**
     * @return list of XWPFCharts in this document
     */
    public List<XWPFChart> getCharts() {
        return Collections.unmodifiableList(charts);
    }

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
        if (pos >= 0 && pos < footers.size()) {
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
        if (pos >= 0 && pos < headers.size()) {
            return headers.get(pos);
        }
        return null;
    }

    public String getTblStyle(XWPFTable table) {
        return table.getStyleID();
    }

    public XWPFHyperlink getHyperlinkByID(String id) {
        for (XWPFHyperlink link : hyperlinks) {
            if (link.getId().equals(id)) {
                return link;
            }
        }

        // If the link was not found, rebuild the list (maybe a new link was added into the document) and check again.
        initHyperlinks();
        for (XWPFHyperlink link : hyperlinks) {
            if (link.getId().equals(id)) {
                return link;
            }
        }

        // Link still not there? Giving up.
        return null;
    }

    public XWPFFootnote getFootnoteByID(int id) {
        if (footnotes == null) {
            return null;
        }
        return (XWPFFootnote)footnotes.getFootnoteById(id);
    }

    public XWPFEndnote getEndnoteByID(int id) {
        if (endnotes == null) {
            return null;
        }
        return endnotes.getFootnoteById(id);
    }

    public List<XWPFFootnote> getFootnotes() {
        if (footnotes == null) {
            return Collections.emptyList();
        }
        return footnotes.getFootnotesList();
    }

    /**
     * @return Theme document (can be null)
     * @since POI 5.2.4
     */
    public XWPFTheme getTheme() {
        return theme;
    }

    public XWPFHyperlink[] getHyperlinks() {
        return hyperlinks.toArray(new XWPFHyperlink[0]);
    }

    /**
     * Get Comments
     *
     * @return comments
     */
    public XWPFComments getDocComments() {
        return comments;
    }

    public XWPFComment getCommentByID(String id) {
        if (null == comments) {
            return null;
        }
        return comments.getCommentByID(id);
    }

    public XWPFComment[] getComments() {
        if (null == comments) {
            return null;
        }
        return comments.getComments().toArray(new XWPFComment[0]);
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
            if (!ctSectPr.isSetTitlePg()) {
                CTOnOff titlePg = ctSectPr.addNewTitlePg();
                titlePg.setVal(STOnOff1.ON);
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
            if (!ctSectPr.isSetTitlePg()) {
                CTOnOff titlePg = ctSectPr.addNewTitlePg();
                titlePg.setVal(STOnOff1.ON);
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
            throw new IOException(e);
        }
        if (parts.length != 1) {
            throw new IOException("Expecting one Styles document part, but found " + parts.length);
        }

        try (InputStream stream = parts[0].getInputStream()) {
            StylesDocument sd = StylesDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            return sd.getStyles();
        }
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbeddedParts() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<>();

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
        if (list.isEmpty()) {
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
     * {@link XmlCursor.TokenType#START} tag of an subelement
     * of the documents body. When this method is done, the cursor passed as
     * parameter points to the {@link XmlCursor.TokenType#END}
     * of the newly inserted paragraph.
     *
     * @param cursor The cursor-position where the new paragraph should be added.
     * @return the {@link XWPFParagraph} object representing the newly inserted
     * CTP object.
     */
    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        Deque<XmlObject> path = getPathToObject(cursor);
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
        insertIntoParentElement(newP, path);
        cursor.toCursor(newP.getCTP().newCursor());
        cursor.toEndToken();
        return newP;
    }

    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        Deque<XmlObject> path = getPathToObject(cursor);
        String uri = CTTbl.type.getName().getNamespaceURI();
        String localPart = "tbl";
        cursor.beginElement(localPart, uri);
        cursor.toParent();
        CTTbl t = (CTTbl) cursor.getObject();
        XWPFTable newT = new XWPFTable(t, this);
        insertIntoParentElement(newT, path);
        cursor.toCursor(newT.getCTTbl().newCursor());
        cursor.toEndToken();
        return newT;
    }

    private Deque<XmlObject> getPathToObject(XmlCursor cursor) {
        Deque<XmlObject> searchPath = new LinkedList<>();
        try (XmlCursor verify = cursor.newCursor()) {
            while (verify.toParent() && searchPath.peekFirst() != this.ctDocument.getBody()) {
                searchPath.addFirst(verify.getObject());
            }
        }
        return searchPath;
    }

    private void insertIntoParentElement(IBodyElement iBodyElement, Deque<XmlObject> path) {
        XmlObject firstObject = path.pop();
        if (path.isEmpty()) {
            if (iBodyElement instanceof XWPFParagraph) {
                insertIntoParagraphsAndElements((XWPFParagraph) iBodyElement, paragraphs, bodyElements);
            } else if (iBodyElement instanceof XWPFTable) {
                insertIntoTablesAndElements((XWPFTable) iBodyElement, tables, bodyElements);
            }
        } else {
            CTTbl ctTbl = (CTTbl) path.pop(); //first object is always the body, we want the second one
            for (XWPFTable xwpfTable : tables) {
                if (ctTbl == xwpfTable.getCTTbl()) {
                    insertElementIntoTable(xwpfTable, iBodyElement, path);
                }
            }
        }
    }

    private void insertIntoParagraphsAndElements(XWPFParagraph newP, List<XWPFParagraph> paragraphs, List<IBodyElement> bodyElements) {
        insertIntoParagraphs(newP, paragraphs);
        insertIntoBodyElements(newP, bodyElements);
    }

    private void insertIntoParagraphs(XWPFParagraph newP, List<XWPFParagraph> paragraphs) {
        try (XmlCursor cursor = newP.getCTP().newCursor()) {
            XmlObject p = cursor.getObject();
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
            if ((!(o instanceof CTP)) || o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }
        }
    }

    private void insertIntoTablesAndElements(XWPFTable newT, List<XWPFTable> tables, List<IBodyElement> bodyElements) {
        insertIntoTables(newT, tables);
        insertIntoBodyElements(newT, bodyElements);
    }

    private void insertIntoTables(XWPFTable newT, List<XWPFTable> tables) {
        try (XmlCursor cursor = newT.getCTTbl().newCursor()) {
            XmlObject p = cursor.getObject();
            XmlObject o = null;
            /*
             * move the cursor to the previous element until a) the next
             * paragraph is found or b) all elements have been passed
             */
            while (!(o instanceof CTTbl) && (cursor.toPrevSibling())) {
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
            if (!(o instanceof CTTbl)) {
                tables.add(0, newT);
            } else {
                int pos = tables.indexOf(getTable((CTTbl) o)) + 1;
                tables.add(pos, newT);
            }
        }
    }

    private void insertIntoBodyElements(IBodyElement iBodyElement, List<IBodyElement> bodyElements) {
        /*
         * create a new cursor, that points to the START token of the just
         * inserted paragraph
         */
        try (XmlCursor cursor = getNewCursor(iBodyElement).orElseThrow(NoSuchElementException::new);
             XmlCursor newParaPos = getNewCursor(iBodyElement).orElseThrow(NoSuchElementException::new)) {
            XmlObject o;
            /*
             * Calculate the paragraphs index in the list of all body
             * elements
             */
            int i = 0;
            cursor.toCursor(newParaPos);
            while (cursor.toPrevSibling()) {
                o = cursor.getObject();
                if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                    i++;
                }
            }
            bodyElements.add(i, iBodyElement);
            cursor.toCursor(newParaPos);
            cursor.toEndToken();
        } catch (NoSuchElementException ignored) {
            //We could not open a cursor to the ibody element
        }
    }

    private Optional<XmlCursor> getNewCursor(IBodyElement iBodyElement) {
        if (iBodyElement instanceof XWPFParagraph) {
            return Optional.ofNullable(((XWPFParagraph) iBodyElement).getCTP().newCursor());
        } else if (iBodyElement instanceof XWPFTable) {
            return Optional.ofNullable(((XWPFTable) iBodyElement).getCTTbl().newCursor());
        }
        return Optional.empty();
    }


    private void insertElementIntoTable(XWPFTable xwpfTable, IBodyElement iBodyElement, Deque<XmlObject> path) {
        CTRow row = (CTRow) path.pop();
        for (XWPFTableRow tableRow : xwpfTable.getRows()) {
            if (tableRow.getCtRow() == row) {
                insertElementIntoRow(tableRow, iBodyElement, path);
            }
        }
    }

    private void insertElementIntoRow(XWPFTableRow tableRow, IBodyElement iBodyElement, Deque<XmlObject> path) {
        CTTc cell = (CTTc) path.pop();
        for (XWPFTableCell tableCell : tableRow.getTableCells()) {
            if (tableCell.getCTTc() == cell) {
                insertElementIntoCell(tableCell, iBodyElement, path);
            }
        }
    }

    private void insertElementIntoCell(XWPFTableCell tableCell, IBodyElement iBodyElement, Deque<XmlObject> path) {
        if (path.isEmpty()) {
            if (iBodyElement instanceof XWPFParagraph) {
                insertIntoParagraphsAndElements((XWPFParagraph) iBodyElement, tableCell.paragraphs, tableCell.bodyElements);
            }  else if (iBodyElement instanceof XWPFTable) {
                insertIntoTablesAndElements((XWPFTable) iBodyElement, tableCell.tables, tableCell.bodyElements);
            }
        } else {
            // another table
            insertElementIntoTable((XWPFTable) path.pop(), iBodyElement, path);
        }
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
        try (OutputStream out = part.getOutputStream()) {
            ctDocument.save(out, xmlOptions);
        }
    }

    /**
     * Gets the index of the relation we're trying to create
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
     * Creates an empty comments for the document if one does not already exist
     *
     * @return comments
     */
    public XWPFComments createComments() {
        if (comments == null) {
            CommentsDocument commentsDoc = CommentsDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.COMMENT;
            int i = getRelationIndex(relation);

            XWPFComments wrapper = (XWPFComments) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setCtComments(commentsDoc.addNewComments());
            wrapper.setXWPFDocument(getXWPFDocument());
            comments = wrapper;
        }
        return comments;
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
     * Creates an empty styles for the document if one does not already exist
     *
     * @return styles
     * @since POI 5.2.4
     */
    public XWPFTheme createTheme() {
        if (theme == null) {
            ThemeDocument themeDoc = ThemeDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.THEME;
            int i = getRelationIndex(relation);

            XWPFTheme wrapper = (XWPFTheme) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setTheme(themeDoc.addNewTheme());
            theme = wrapper;
        }

        return theme;
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
            wrapper.setIdManager(this.footnoteIdManager);
            footnotes = wrapper;
        }

        return footnotes;
    }

    /**
     * Add a CTFtnEdn footnote to the document.
     *
     * @param note CTFtnEnd to be added.
     * @return New {@link XWPFFootnote}
     */
    @Internal
    public XWPFFootnote addFootnote(CTFtnEdn note) {
        return footnotes.addFootnote(note);
    }

    /**
     * Add a CTFtnEdn endnote to the document.
     *
     * @param note CTFtnEnd to be added.
     * @return New {@link XWPFEndnote}
     */
    @Internal
    public XWPFEndnote addEndnote(CTFtnEdn note) {
        XWPFEndnote endnote = new XWPFEndnote(this, note);
        endnotes.addEndnote(note);
        return endnote;
    }

    /**
     * remove a BodyElement from bodyElements array list
     *
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
     * copies content of a paragraph to an existing paragraph in the list paragraphs at position pos
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
                    LOG.atError().withThrowable(e).log("can't format number in TOC heading");
                }
            }
        }
    }

    /**
     * Replace content of table in array tables at position pos with a
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
     *                 if null, it will default to sha1
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
     *                 if null, it will default to sha1
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
     *                 if null, it will default to sha1
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
     *                 if null, it will default to sha1
     */
    public void enforceTrackedChangesProtection(String password, HashAlgorithm hashAlgo) {
        settings.setEnforcementEditValue(STDocProtect.TRACKED_CHANGES, password, hashAlgo);
    }

    /**
     * Validates the existing password
     *
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
     * @return {@code true} if revision tracking is turned on
     */
    public boolean isTrackRevisions() {
        return settings.isTrackRevisions();
    }

    /**
     * Enable or disable revision tracking.
     *
     * @param enable {@code true} to turn on revision tracking, {@code false} to turn off revision tracking
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
     * Returns the even-and-odd-headings setting
     *
     * @return True or false indicating whether or not separate even and odd headings is turned on.
     */
    public boolean getEvenAndOddHeadings() {
        return settings.getEvenAndOddHeadings();
    }

    /**
     * Sets the even-and-odd-headings setting
     * @param enable Set to true to turn on separate even and odd headings.
     */
    public void setEvenAndOddHeadings(boolean enable) {
        settings.setEvenAndOddHeadings(enable);
    }

    /**
     * Returns the mirror margins setting
     *
     * @return True or false indicating whether or not mirror margins is turned on.
     */
    public boolean getMirrorMargins() {
        return settings.getMirrorMargins();
    }

    /**
     * Sets the mirror margins setting
     * @param enable Set to true to turn on mirror margins.
     */
    public void setMirrorMargins(boolean enable) {
        settings.setMirrorMargins(enable);
    }

    /**
     * inserts an existing XWPFTable to the arrays bodyElements and tables
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
        List<XWPFPictureData> result = new ArrayList<>();
        Collection<List<XWPFPictureData>> values = packagePictures.values();
        for (List<XWPFPictureData> list : values) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * @return document level settings
     * @since POI 5.2.1
     */
    public XWPFSettings getSettings() {
        return settings;
    }

    void registerPackagePictureData(XWPFPictureData picData) {
        List<XWPFPictureData> list = packagePictures.computeIfAbsent(picData.getChecksum(), k -> new ArrayList<>(1));
        if (!list.contains(picData)) {
            list.add(picData);
        }
    }

    XWPFPictureData findPackagePictureData(byte[] pictureData) {
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

    /**
     * Adds a picture to the document.
     *
     * @param pictureData The picture data
     * @param format the format of the picture, see constants in {@link Document}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException if the format is not known
     * @see #addPictureData(byte[], PictureType)
     */
    public String addPictureData(byte[] pictureData, int format) throws InvalidFormatException {
        return addPictureData(pictureData, PictureType.findByOoxmlId(format));
    }

    /**
     * Adds a picture to the document.
     *
     * @param pictureData The picture data
     * @param pictureType the {@link PictureType}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException if the format is not known
     * @since POI 5.2.3
     */
    public String addPictureData(byte[] pictureData, PictureType pictureType) throws InvalidFormatException {
        if (pictureType == null) {
            throw new InvalidFormatException("pictureType is not supported");
        }
        XWPFPictureData xwpfPicData = findPackagePictureData(pictureData);
        POIXMLRelation relDesc = XWPFPictureData.RELATIONS[pictureType.ooxmlId];

        if (xwpfPicData == null) {
            /* Part doesn't exist, create a new one */
            int idx = getNextPicNameNumber(pictureType);
            xwpfPicData = (XWPFPictureData) createRelationship(relDesc, XWPFFactory.getInstance(), idx);
            /* write bytes to new part */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            try (OutputStream out = picDataPart.getOutputStream()) {
                out.write(pictureData);
            } catch (IOException e) {
                throw new POIXMLException(e);
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

    /**
     * Adds a picture to the document.
     *
     * @param is The picture data
     * @param format the format of the picture, see constants in {@link Document}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException if the format is not known
     * @see #addPictureData(InputStream, PictureType)
     */
    public String addPictureData(InputStream is, int format) throws InvalidFormatException {
        try {
            byte[] data = IOUtils.toByteArrayWithMaxLength(is, XWPFPictureData.getMaxImageSize());
            return addPictureData(data, format);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * Adds a picture to the document.
     *
     * @param is The picture data
     * @param pictureType the {@link PictureType}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException if the pictureType is not known
     * @since POI 5.2.3
     */
    public String addPictureData(InputStream is, PictureType pictureType) throws InvalidFormatException {
        try {
            byte[] data = IOUtils.toByteArrayWithMaxLength(is, XWPFPictureData.getMaxImageSize());
            return addPictureData(data, pictureType);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the next free ImageNumber
     *
     * @param format the format of the picture, see constants in {@link Document}
     * @return the next free ImageNumber
     * @throws InvalidFormatException If the format of the picture is not known.
     * @see #getNextPicNameNumber(PictureType)
     */
    public int getNextPicNameNumber(int format) throws InvalidFormatException {
        return getNextPicNameNumber(PictureType.findByOoxmlId(format));
    }

    /**
     * get the next free ImageNumber
     *
     * @param pictureType the {@link PictureType}
     * @return the next free ImageNumber
     * @throws InvalidFormatException If the pictureType of the picture is not known.
     * @since POI 5.2.3
     */
    public int getNextPicNameNumber(PictureType pictureType) throws InvalidFormatException {
        if (pictureType == null) {
            throw new InvalidFormatException("pictureType is not supported");
        }
        int img = getAllPackagePictures().size() + 1;
        String proposal = XWPFPictureData.RELATIONS[pictureType.ooxmlId].getFileName(img);
        PackagePartName createPartName = PackagingURIHelper.createPartName(proposal);
        while (this.getPackage().getPart(createPartName) != null) {
            img++;
            proposal = XWPFPictureData.RELATIONS[pictureType.ooxmlId].getFileName(img);
            createPartName = PackagingURIHelper.createPartName(proposal);
        }
        return img;
    }

    /**
     * returns the PictureData by blipID
     *
     * @return XWPFPictureData of a specificID
     */
    public XWPFPictureData getPictureDataByID(String blipID) {
        POIXMLDocumentPart relatedPart = getRelationById(blipID);
        if (relatedPart instanceof XWPFPictureData) {
            return (XWPFPictureData) relatedPart;
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

    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (paragraph.getCTP() == p) {
                return paragraph;
            }
        }
        return null;
    }

    /**
     * get a table by its CTTbl-Object
     *
     * @return a table by its CTTbl-Object or null
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

    /**
     * @since POI 5.2.0
     */
    public Spliterator<XWPFTable> getTablesSpliterator() {
        return tables.spliterator();
    }

    public Iterator<XWPFParagraph> getParagraphsIterator() {
        return paragraphs.iterator();
    }

    /**
     * @since POI 5.2.0
     */
    public Spliterator<XWPFParagraph> getParagraphsSpliterator() {
        return paragraphs.spliterator();
    }

    /**
     * Returns the paragraph that of position pos
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
     */
    @Override
    public POIXMLDocumentPart getPart() {
        return this;
    }


    /**
     * get the PartType of the body, for example
     * DOCUMENT, HEADER, FOOTER, FOOTNOTE,
     */
    @Override
    public BodyType getPartType() {
        return BodyType.DOCUMENT;
    }

    /**
     * get the TableCell which belongs to the TableCell
     */
    @Override
    public XWPFTableCell getTableCell(CTTc cell) {
        XmlObject o;
        CTRow row;
        try (final XmlCursor cursor = cell.newCursor()) {
            cursor.toParent();
            o = cursor.getObject();
            if (!(o instanceof CTRow)) {
                return null;
            }
            row = (CTRow) o;
            cursor.toParent();
            o = cursor.getObject();
        }
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

    /**
     * This method is used to create template for chart XML
     * no need to read MS-Word file and modify charts
     *
     * @return This method return object of XWPFChart Object with default height and width
     * @since POI 4.0.0
     */
    public XWPFChart createChart() throws InvalidFormatException, IOException {
        return createChart(XDDFChart.DEFAULT_WIDTH, XDDFChart.DEFAULT_HEIGHT);
    }

    /**
     * This method is used to create template for chart XML
     * no need to read MS-Word file and modify charts
     *
     * @param width  width of chart in document
     * @param height height of chart in document
     * @return This method return object of XWPFChart
     * @since POI 4.0.0
     */
    public XWPFChart createChart(int width, int height) throws InvalidFormatException, IOException {
        return createChart(createParagraph().createRun(), width, height);
    }

    /**
     *
     * @param run in which the chart will be attached.
     * @param width in EMU.
     * @param height in EMU.
     * @return the new chart.
     * @since POI 4.1.2
     */
    public XWPFChart createChart(XWPFRun run, int width, int height) throws InvalidFormatException, IOException {
        //get chart number
        int chartNumber = getNextPartNumber(XWPFRelation.CHART, charts.size() + 1);

        //create relationship in document for new chart
        RelationPart rp = createRelationship(
            XWPFRelation.CHART, XWPFFactory.getInstance(), chartNumber, false);

        // initialize xwpfchart object
        XWPFChart xwpfChart = rp.getDocumentPart();
        xwpfChart.setChartIndex(chartNumber);
        xwpfChart.attach(rp.getRelationship().getId(), run);
        xwpfChart.setChartBoundingBox(width, height);

        //add chart object to chart list
        charts.add(xwpfChart);
        return xwpfChart;
    }

    /**
     * Create a new footnote and add it to the document.
     *
     * @return New XWPFFootnote.
     * @since 4.0.0
     */
    public XWPFFootnote createFootnote() {
        XWPFFootnotes footnotes = this.createFootnotes();

        return footnotes.createFootnote();
    }

    /**
     * Remove the specified footnote if present.
     *
     * @param pos Array position of the footnote to be removed.
     * @return True if the footnote was removed.
     * @since 4.0.0
     */
    public boolean removeFootnote(int pos) {
        if (null != footnotes) {
            return footnotes.removeFootnote(pos);
        } else {
            return false;
        }
    }

    /**
     * Create a new end note and add it to the document.
     *
     * @return New {@link XWPFEndnote}.
     * @since 4.0.0
     */
    public XWPFEndnote createEndnote() {
        XWPFEndnotes endnotes = this.createEndnotes();

        return endnotes.createEndnote();

    }

    public XWPFEndnotes createEndnotes() {
        if (endnotes == null) {
            EndnotesDocument endnotesDoc = EndnotesDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.ENDNOTE;
            int i = getRelationIndex(relation);

            XWPFEndnotes wrapper = (XWPFEndnotes) createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setEndnotes(endnotesDoc.addNewEndnotes());
            wrapper.setIdManager(footnoteIdManager);
            endnotes = wrapper;
        }

        return endnotes;

    }

    /**
     * Gets the list of end notes for the document.
     *
     * @return List, possibly empty, of {@link XWPFEndnote}s.
     */
    public List<XWPFEndnote> getEndnotes() {
        if (endnotes == null) {
            return Collections.emptyList();
        }
        return endnotes.getEndnotesList();
    }

    /**
     * Remove the specified end note if present.
     *
     * @param pos Array position of the end note to be removed.
     * @return True if the end note was removed.
     * @since 4.0.0
     */
    public boolean removeEndnote(int pos) {
        if (null != endnotes) {
            return endnotes.removeEndnote(pos);
        } else {
            return false;
        }
    }

}
