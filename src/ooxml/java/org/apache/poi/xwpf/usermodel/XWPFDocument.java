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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.*;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.xml.namespace.QName;

/**
 * Experimental class to do low level processing
 *  of docx files.
 *
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 *
 * WARNING - APIs expected to change rapidly
 */
public class XWPFDocument extends POIXMLDocument {

    private CTDocument1 ctDocument;
    protected List<XWPFComment> comments;
    protected List<XWPFHyperlink> hyperlinks;
    protected List<XWPFParagraph> paragraphs;
    protected List<XWPFTable> tables;
    protected Map<Integer, XWPFFootnote> footnotes;
    protected Map<Integer, XWPFFootnote> endnotes;

    /** Handles the joy of different headers/footers for different pages */
    private XWPFHeaderFooterPolicy headerFooterPolicy;

    public XWPFDocument(OPCPackage pkg) throws IOException {
        super(ensureWriteAccess(pkg));

        //build a tree of POIXMLDocumentParts, this document being the root
        load(XWPFFactory.getInstance());
    }
    
    public XWPFDocument(InputStream is) throws IOException {
        super(PackageHelper.open(is));

        //build a tree of POIXMLDocumentParts, this workbook being the root
        load(XWPFFactory.getInstance());
    }

    public XWPFDocument(){
        super(newPackage());
        onDocumentCreate();
    }

    @Override
    protected void onDocumentRead() throws IOException {
        hyperlinks = new ArrayList<XWPFHyperlink>();
        comments = new ArrayList<XWPFComment>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables= new ArrayList<XWPFTable>();
        footnotes = new HashMap<Integer, XWPFFootnote>();
        endnotes = new HashMap<Integer, XWPFFootnote>();

        try {
            DocumentDocument doc = DocumentDocument.Factory.parse(getPackagePart().getInputStream());
            ctDocument = doc.getDocument();

            CTBody body = ctDocument.getBody();

            initFootnotes();

            // filling paragraph list
            for (CTP p : body.getPArray())	{
                paragraphs.add(new XWPFParagraph(p, this));
            }

            // Get any tables
            for(CTTbl table : body.getTblArray()) {
                tables.add(new XWPFTable(this, table));
            }

            // Sort out headers and footers
            headerFooterPolicy = new XWPFHeaderFooterPolicy(this);

            for(POIXMLDocumentPart p : getRelations()){
                String relation = p.getPackageRelationship().getRelationshipType();
                if(relation.equals(XWPFRelation.COMMENT.getRelation())){
                    CommentsDocument cmntdoc = CommentsDocument.Factory.parse(p.getPackagePart().getInputStream());
                    for(CTComment ctcomment : cmntdoc.getComments().getCommentArray()) {
                        comments.add(new XWPFComment(ctcomment));
                    }
                }
            }

            initHyperlinks();
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
    }

    private void initHyperlinks(){
        // Get the hyperlinks
        // TODO: make me optional/separated in private function
        try	{
            Iterator <PackageRelationship> relIter =
                getPackagePart().getRelationshipsByType(XWPFRelation.HYPERLINK.getRelation()).iterator();
            while(relIter.hasNext()) {
                PackageRelationship rel = relIter.next();
                hyperlinks.add(new XWPFHyperlink(rel.getId(), rel.getTargetURI().toString()));
            }
        } catch (InvalidFormatException e){
            throw new POIXMLException(e);
        }
    }

    private void initFootnotes() throws XmlException, IOException {
        for(POIXMLDocumentPart p : getRelations()){
            String relation = p.getPackageRelationship().getRelationshipType();
            if(relation.equals(XWPFRelation.FOOTNOTE.getRelation())){
                FootnotesDocument footnotesDocument = FootnotesDocument.Factory.parse(p.getPackagePart().getInputStream());

                for(CTFtnEdn ctFtnEdn : footnotesDocument.getFootnotes().getFootnoteArray()) {
                    footnotes.put(ctFtnEdn.getId().intValue(), new XWPFFootnote(this, ctFtnEdn));
                }
            } else if (relation.equals(XWPFRelation.ENDNOTE.getRelation())){
                EndnotesDocument endnotesDocument = EndnotesDocument.Factory.parse(p.getPackagePart().getInputStream());

                for(CTFtnEdn ctFtnEdn : endnotesDocument.getEndnotes().getEndnoteArray()) {
                    endnotes.put(ctFtnEdn.getId().intValue(), new XWPFFootnote(this, ctFtnEdn));
                }
            }
        }
    }

    /**
     * Create a new SpreadsheetML package and setup the default minimal content
     */
    protected static OPCPackage newPackage() {
        try {
        	OPCPackage pkg = OPCPackage.create(PackageHelper.createTempFile());
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(XWPFRelation.DOCUMENT.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT);
            // Create main document part
            pkg.createPart(corePartName, XWPFRelation.DOCUMENT.getContentType());

            pkg.getPackageProperties().setCreatorProperty(DOCUMENT_CREATOR);

            return pkg;
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Create a new CTWorkbook with all values set to default
     */
    protected void onDocumentCreate() {
        hyperlinks = new ArrayList<XWPFHyperlink>();
        comments = new ArrayList<XWPFComment>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables= new ArrayList<XWPFTable>();

        ctDocument = CTDocument1.Factory.newInstance();
        ctDocument.addNewBody();

        POIXMLProperties.ExtendedProperties expProps = getProperties().getExtendedProperties();
        expProps.getUnderlyingProperties().setApplication(DOCUMENT_CREATOR);
    }

    /**
     * Returns the low level document base object
     */
    public CTDocument1 getDocument() {
        return ctDocument;
    }

    public Iterator<XWPFParagraph> getParagraphsIterator() {
        return paragraphs.iterator();
    }
    public XWPFParagraph[] getParagraphs() {
        return paragraphs.toArray(
            new XWPFParagraph[paragraphs.size()]
        );
    }

    public Iterator<XWPFTable> getTablesIterator()
    {
        return tables.iterator();
    }

    public XWPFHyperlink getHyperlinkByID(String id) {
        Iterator<XWPFHyperlink> iter = hyperlinks.iterator();
        while(iter.hasNext())
        {
            XWPFHyperlink link = iter.next();
            if(link.getId().equals(id))
                return link;
        }

        return null;
    }

    public XWPFFootnote getFootnoteByID(int id) {
        return footnotes.get(id);
    }

    public XWPFFootnote getEndnoteByID(int id) {
        return endnotes.get(id);
    }

    public Collection<XWPFFootnote> getFootnotes() {
        return footnotes == null ? new ArrayList<XWPFFootnote>() : footnotes.values();
    }

    public XWPFHyperlink[] getHyperlinks() {
        return hyperlinks.toArray(
                new XWPFHyperlink[hyperlinks.size()]
        );
    }

    public XWPFComment getCommentByID(String id) {
        Iterator<XWPFComment> iter = comments.iterator();
        while(iter.hasNext())
        {
            XWPFComment comment = iter.next();
            if(comment.getId().equals(id))
                return comment;
        }

        return null;
    }
    public XWPFComment[] getComments() {
        return comments.toArray(
                new XWPFComment[comments.size()]
        );
    }

    /**
     * Get the document part that's defined as the
     *  given relationship of the core document.
     */
    public PackagePart getPartById(String id) {
        try {
            return getTargetPart(
                    getCorePart().getRelationship(id)
            );
        } catch(InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the policy on headers and footers, which
     *  also provides a way to get at them.
     */
    public XWPFHeaderFooterPolicy getHeaderFooterPolicy() {
        return headerFooterPolicy;
    }

    /**
     * Returns the styles object used
     */
    public CTStyles getStyle() throws XmlException, IOException {
        PackagePart[] parts;
        try {
            parts = getRelatedByType(XWPFRelation.STYLES.getRelation());
        } catch(InvalidFormatException e) {
            throw new IllegalStateException(e);
        }
        if(parts.length != 1) {
            throw new IllegalStateException("Expecting one Styles document part, but found " + parts.length);
        }

        StylesDocument sd =
            StylesDocument.Factory.parse(parts[0].getInputStream());
        return sd.getStyles();
    }

    /**
     * Get the document's embedded files.
     */
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<PackagePart>();

        // Get the embeddings for the workbook
        for(PackageRelationship rel : getPackagePart().getRelationshipsByType(OLE_OBJECT_REL_TYPE))
            embedds.add(getTargetPart(rel));

        for(PackageRelationship rel : getPackagePart().getRelationshipsByType(PACK_OBJECT_REL_TYPE))
            embedds.add(getTargetPart(rel));

        return embedds;
    }

    @Override
    protected void commit() throws IOException {

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTDocument1.type.getName().getNamespaceURI(), "document"));
        Map map = new HashMap();
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("urn:schemas-microsoft-com:vml", "v");
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctDocument.save(out, xmlOptions);
        out.close();
    }

    /**
     * Appends a new paragraph to this document
     *
     * @return a new paragraph
     */
    public XWPFParagraph createParagraph(){
        return new XWPFParagraph(ctDocument.getBody().addNewP(), this);
    }

    /**
     * Create an empty table with one row and one column as default.
     * 
     * @return a new table
     */
    public XWPFTable createTable(){
        return new XWPFTable(this, ctDocument.getBody().addNewTbl());
    }
    
    /**
     * Create an empty table with a number of rows and cols specified
     * @param rows
     * @param cols
     * @return table
     */
    public XWPFTable createTable(int rows, int cols) {
	return new XWPFTable(this, ctDocument.getBody().addNewTbl(), rows, cols);
    }
}

