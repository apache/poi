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
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.IdentifierManager;
import org.apache.poi.util.Internal;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.NumberingDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.EndnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FootnotesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;

/**
 * Experimental class to do low level processing
 *  of docx files.
 *
 * If you're using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 *
 * WARNING - APIs expected to change rapidly
 */
public class XWPFDocument extends POIXMLDocument implements Document, IBody {

    private CTDocument1 ctDocument;
    private XWPFSettings settings;
    /**
     * Keeps track on all id-values used in this document and included parts, like headers, footers, etc.
     */
    private IdentifierManager drawingIdManager = new IdentifierManager(1L,4294967295L);
    protected List<XWPFFooter> footers = new ArrayList<XWPFFooter>();
    protected List<XWPFHeader> headers = new ArrayList<XWPFHeader>();
    protected List<XWPFComment> comments = new ArrayList<XWPFComment>();
    protected List<XWPFHyperlink> hyperlinks = new ArrayList<XWPFHyperlink>();
    protected List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>();
    protected List<XWPFTable> tables = new ArrayList<XWPFTable>();
    protected List<IBodyElement> bodyElements = new ArrayList<IBodyElement>();
    protected List<XWPFPictureData> pictures = new ArrayList<XWPFPictureData>();
    protected Map<Long, List<XWPFPictureData>> packagePictures = new HashMap<Long, List<XWPFPictureData>>();
    protected Map<Integer, XWPFFootnote> endnotes = new HashMap<Integer, XWPFFootnote>();
    protected XWPFNumbering numbering;
    protected XWPFStyles styles;
    protected XWPFFootnotes footnotes;

    /** Handles the joy of different headers/footers for different pages */
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

    public XWPFDocument(){
        super(newPackage());
        onDocumentCreate();
    }

    @Override
    protected void onDocumentRead() throws IOException {
        try {
            DocumentDocument doc = DocumentDocument.Factory.parse(getPackagePart().getInputStream());
            ctDocument = doc.getDocument();

            initFootnotes();

            // parse the document with cursor and add
            // the XmlObject to its lists
            XmlCursor cursor = ctDocument.getBody().newCursor();
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                    bodyElements.add(p);
                    paragraphs.add(p);
                } else if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl) o, this);
                    bodyElements.add(t);
                    tables.add(t);
                }
            }
            cursor.dispose();

            // Sort out headers and footers
            if (doc.getDocument().getBody().getSectPr() != null)
                headerFooterPolicy = new XWPFHeaderFooterPolicy(this);

            // Create for each XML-part in the Package a PartClass
            for (POIXMLDocumentPart p : getRelations()) {
                String relation = p.getPackageRelationship().getRelationshipType();
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
                    CommentsDocument cmntdoc = CommentsDocument.Factory.parse(p.getPackagePart().getInputStream());
                    for (CTComment ctcomment : cmntdoc.getComments().getCommentList()) {
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
        try {
            Iterator<PackageRelationship> relIter =
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
          if (relation.equals(XWPFRelation.FOOTNOTE.getRelation())) {
             FootnotesDocument footnotesDocument = FootnotesDocument.Factory.parse(p.getPackagePart().getInputStream());
             this.footnotes = (XWPFFootnotes)p;
             this.footnotes.onDocumentRead();

             for(CTFtnEdn ctFtnEdn : footnotesDocument.getFootnotes().getFootnoteList()) {
                footnotes.addFootnote(ctFtnEdn);
             }
          } else if (relation.equals(XWPFRelation.ENDNOTE.getRelation())){
             EndnotesDocument endnotesDocument = EndnotesDocument.Factory.parse(p.getPackagePart().getInputStream());

             for(CTFtnEdn ctFtnEdn : endnotesDocument.getEndnotes().getEndnoteList()) {
                endnotes.put(ctFtnEdn.getId().intValue(), new XWPFFootnote(this, ctFtnEdn));
             }
          }
       }
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
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Create a new CTWorkbook with all values set to default
     */
    @Override
    protected void onDocumentCreate() {
        ctDocument = CTDocument1.Factory.newInstance();
        ctDocument.addNewBody();

        settings = (XWPFSettings) createRelationship(XWPFRelation.SETTINGS,XWPFFactory.getInstance());

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
     * @see org.apache.poi.xwpf.usermodel.IBody#getBodyElements()
     */
    public List<IBodyElement> getBodyElements(){
    	return Collections.unmodifiableList(bodyElements);
    }
    
    /**
	 * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphs()
     */
  	public List<XWPFParagraph> getParagraphs(){
    	return Collections.unmodifiableList(paragraphs);
    }
    
 	/**
 	 * @see org.apache.poi.xwpf.usermodel.IBody#getTables()
 	 */
 	public List<XWPFTable> getTables(){
 		return Collections.unmodifiableList(tables);
 	}
 	
	/**
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTableArray(int)
	 */
	public XWPFTable getTableArray(int pos) {
		if(pos > 0 && pos < tables.size()){
			return tables.get(pos);
		}
		return null;
	}
 	
 	/**
 	 * 
 	 * @return  the list of footers
 	 */
 	public List<XWPFFooter> getFooterList(){
 		return Collections.unmodifiableList(footers);
 	}
 	
 	public XWPFFooter getFooterArray(int pos){
 		return footers.get(pos);
 	}
 	
 	/**
 	 * 
 	 * @return  the list of headers
 	 */
 	public List<XWPFHeader> getHeaderList(){
 		return Collections.unmodifiableList(headers);
 	}
 	
 	public XWPFHeader getHeaderArray(int pos){
 		return headers.get(pos);
 	}
 	
    public String getTblStyle(XWPFTable table){
    	return table.getStyleID();
    }

    public XWPFHyperlink getHyperlinkByID(String id) {
        Iterator<XWPFHyperlink> iter = hyperlinks.iterator();
        while (iter.hasNext()) {
            XWPFHyperlink link = iter.next();
            if(link.getId().equals(id))
                return link;
        }

        return null;
    }

    public XWPFFootnote getFootnoteByID(int id) {
        return footnotes.getFootnoteById(id);
    }

    public XWPFFootnote getEndnoteByID(int id) {
        return endnotes.get(id);
    }

    public List<XWPFFootnote> getFootnotes() {
		return footnotes.getFootnotesList();
    }

    public XWPFHyperlink[] getHyperlinks() {
        return hyperlinks.toArray(new XWPFHyperlink[hyperlinks.size()]);
    }

    public XWPFComment getCommentByID(String id) {
        Iterator<XWPFComment> iter = comments.iterator();
        while (iter.hasNext()) {
            XWPFComment comment = iter.next();
            if(comment.getId().equals(id))
                return comment;
        }

        return null;
    }

    public XWPFComment[] getComments() {
        return comments.toArray(new XWPFComment[comments.size()]);
    }

    /**
     * Get the document part that's defined as the
     *  given relationship of the core document.
     */
    public PackagePart getPartById(String id) {
        try {
            return getTargetPart(getCorePart().getRelationship(id));
        } catch (InvalidFormatException e) {
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
    @Internal
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

        StylesDocument sd = StylesDocument.Factory.parse(parts[0].getInputStream());
        return sd.getStyles();
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<PackagePart>();

        // Get the embeddings for the workbook
        for (PackageRelationship rel : getPackagePart().getRelationshipsByType(OLE_OBJECT_REL_TYPE)) {
            embedds.add(getTargetPart(rel));
        }

        for (PackageRelationship rel : getPackagePart().getRelationshipsByType(PACK_OBJECT_REL_TYPE)) {
            embedds.add(getTargetPart(rel));
        }

        return embedds;
    }

    /**
     * Finds that for example the 2nd entry in the body list is the 1st paragraph
     */
    private int getBodyElementSpecificPos(int pos, List<? extends IBodyElement> list) {
       // If there's nothing to find, skip it
       if(list.size() == 0) {
          return -1;
       }

       if(pos >= 0 && pos < bodyElements.size()) {
          // Ensure the type is correct
          IBodyElement needle = bodyElements.get(pos);
          if(needle.getElementType() != list.get(0).getElementType()) {
             // Wrong type
             return -1;
          }

          // Work back until we find it
          int startPos = Math.min(pos, list.size()-1);
          for(int i=startPos; i>=0; i--) {
             if(list.get(i) == needle) {
                return i;
             }
          }
       }

       // Couldn't be found
       return -1;
    }
    
    /**
     * Look up the paragraph at the specified position in the body elemnts list
     * and return this paragraphs position in the paragraphs list
     * 
     * @param pos
     *            The position of the relevant paragraph in the body elements
     *            list
     * @return the position of the paragraph in the paragraphs list, if there is
     *         a paragraph at the position in the bodyelements list. Else it
     *         will return -1
     * 
     */
    public int getParagraphPos(int pos) {
       return getBodyElementSpecificPos(pos, paragraphs);
    }

    /**
     * get with the position of a table in the bodyelement array list 
     * the position of this table in the table array list
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * 		   else it will return null. 
     */
    public int getTablePos(int pos) {
       return getBodyElementSpecificPos(pos, tables);
    }

    /**
     * Add a new paragraph at position of the cursor. The cursor must be on the
     * {@link TokenType#START} tag of an subelement of the documents body. When
     * this method is done, the cursor passed as parameter points to the
     * {@link TokenType#END} of the newly inserted paragraph.
     * 
     * @param cursor
     * @return the {@link XWPFParagraph} object representing the newly inserted
     *         CTP object
     */
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

    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInBody(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, this);
            cursor.removeXmlContents();
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
            cursor = t.newCursor();
            while (cursor.toPrevSibling()) {
                o = cursor.getObject();
                if (o instanceof CTP || o instanceof CTTbl)
                    i++;
            }
            bodyElements.add(i, newT);
            cursor = t.newCursor();
            cursor.toEndToken();
            return newT;
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     * @param cursor
     */
    private boolean isCursorInBody(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        try {
            return (verify.getObject() == this.ctDocument.getBody());
        } finally {
            verify.dispose();
        }
    }

	private int getPosOfBodyElement(IBodyElement needle) {
	   BodyElementType type = needle.getElementType();
      IBodyElement current; 
	   for(int i=0; i<bodyElements.size(); i++) {
	      current = bodyElements.get(i);
	      if(current.getElementType() == type) {
	         if(current.equals(needle)) {
	            return i;
	         }
	      }
	   }
	   return -1;
	}

	/**
	 * Get the position of the paragraph, within the list
	 *  of all the body elements.
	 * @param p The paragraph to find
	 * @return The location, or -1 if the paragraph couldn't be found 
	 */
	public int getPosOfParagraph(XWPFParagraph p){
	   return getPosOfBodyElement(p);
    }

	/**
	 * Get the position of the table, within the list of
	 *  all the body elements.
	 * @param t The table to find
	 * @return The location, or -1 if the table couldn't be found
	 */
	public int getPosOfTable(XWPFTable t){
      return getPosOfBodyElement(t);
	}

    /**
     * commit and saves the document
     */
    @Override
    protected void commit() throws IOException {

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTDocument1.type.getName().getNamespaceURI(), "document"));
        Map<String, String> map = new HashMap<String, String>();
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
     * Gets the index of the relation we're trying to create
     * @param relation
     * @return i
     */
    private int getRelationIndex(XWPFRelation relation) {
        List<POIXMLDocumentPart> relations = getRelations();
        int i = 1;
        for (Iterator<POIXMLDocumentPart> it = relations.iterator(); it.hasNext() ; ) {
           POIXMLDocumentPart item = it.next();
           if (item.getPackageRelationship().getRelationshipType().equals(relation.getRelation())) {
              i++;
           }
        }
        return i;
    }

    /**
     * Appends a new paragraph to this document
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
     * @return numbering
     */
    public XWPFNumbering createNumbering() {
        if(numbering == null) {
            NumberingDocument numberingDoc = NumberingDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.NUMBERING;
            int i = getRelationIndex(relation);

            XWPFNumbering wrapper = (XWPFNumbering)createRelationship(relation, XWPFFactory.getInstance(), i);
            wrapper.setNumbering(numberingDoc.addNewNumbering());
            numbering = wrapper;
        }

        return numbering;
    }

    /**
     * Creates an empty styles for the document if one does not already exist
     * @return styles
     */
    public XWPFStyles createStyles() {
       if(styles == null) {
          StylesDocument stylesDoc = StylesDocument.Factory.newInstance();

          XWPFRelation relation = XWPFRelation.STYLES;
          int i = getRelationIndex(relation);

          XWPFStyles wrapper = (XWPFStyles)createRelationship(relation, XWPFFactory.getInstance(), i);
          wrapper.setStyles(stylesDoc.addNewStyles());
          styles = wrapper;
       }

       return styles;
    }

    /**
     * Creates an empty footnotes element for the document if one does not already exist
     * @return footnotes
     */
    public XWPFFootnotes createFootnotes() {
       if(footnotes == null) {
          FootnotesDocument footnotesDoc = FootnotesDocument.Factory.newInstance();

          XWPFRelation relation = XWPFRelation.FOOTNOTE;
          int i = getRelationIndex(relation);

          XWPFFootnotes wrapper = (XWPFFootnotes)createRelationship(relation, XWPFFactory.getInstance(), i);
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
     * @param pos
     * @return true if removing was successfully, else return false
     */
    public boolean removeBodyElement(int pos){
    	if(pos >= 0 && pos < bodyElements.size()) {
    	   BodyElementType type = bodyElements.get(pos).getElementType(); 
    		if(type == BodyElementType.TABLE){
    			int tablePos = getTablePos(pos);
    			tables.remove(tablePos);
    			ctDocument.getBody().removeTbl(tablePos);
    		}
    		if(type == BodyElementType.PARAGRAPH){
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
            if (parStyle != null && parStyle.substring(0, 7).equals("Heading")) {
                try {
                    int level = Integer.valueOf(parStyle.substring("Heading".length())).intValue();
                    toc.addRow(level, par.getText(), 1, "112723803");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**Replace content of table in array tables at position pos with a
     * @param pos
     * @param table
     */
    public void setTable(int pos, XWPFTable table) {
        tables.set(pos, table);
        ctDocument.getBody().setTblArray(pos, table.getCTTbl());
    }

    /**
     * Verifies that the documentProtection tag in settings.xml file <br/>
     * specifies that the protection is enforced (w:enforcement="1") <br/>
     * and that the kind of protection is readOnly (w:edit="readOnly")<br/>
     * <br/>
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
     * Verifies that the documentProtection tag in settings.xml file <br/>
     * specifies that the protection is enforced (w:enforcement="1") <br/>
     * and that the kind of protection is forms (w:edit="forms")<br/>
     * <br/>
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
     * Verifies that the documentProtection tag in settings.xml file <br/>
     * specifies that the protection is enforced (w:enforcement="1") <br/>
     * and that the kind of protection is comments (w:edit="comments")<br/>
     * <br/>
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
     * Verifies that the documentProtection tag in settings.xml file <br/>
     * specifies that the protection is enforced (w:enforcement="1") <br/>
     * and that the kind of protection is trackedChanges (w:edit="trackedChanges")<br/>
     * <br/>
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

    /**
     * Enforces the readOnly protection.<br/>
     * In the documentProtection tag inside settings.xml file, <br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to readOnly (w:edit="readOnly")<br/>
     * <br/>
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
     * Enforce the Filling Forms protection.<br/>
     * In the documentProtection tag inside settings.xml file, <br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to forms (w:edit="forms")<br/>
     * <br/>
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
     * Enforce the Comments protection.<br/>
     * In the documentProtection tag inside settings.xml file,<br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to comments (w:edit="comments")<br/>
     * <br/>
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
     * Enforce the Tracked Changes protection.<br/>
     * In the documentProtection tag inside settings.xml file, <br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to trackedChanges (w:edit="trackedChanges")<br/>
     * <br/>
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
     * Remove protection enforcement.<br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br/>
     */
    public void removeProtectionEnforcement() {
        settings.removeEnforcement();
    }

    /**
     * inserts an existing XWPFTable to the arrays bodyElements and tables
     * @param pos
     * @param table
     */
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i;
        for (i = 0; i < ctDocument.getBody().getTblList().size(); i++) {
            CTTbl tbl = ctDocument.getBody().getTblArray(i);
            if (tbl == table.getCTTbl()) {
                break;
            }
        }
        tables.add(i, table);
    }

    /**
     * Returns all Pictures, which are referenced from the document itself.
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
        if (!list.contains(picData))
        {
            list.add(picData);
        }
    }
    
    XWPFPictureData findPackagePictureData(byte[] pictureData, int format)
    {
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

    public String addPictureData(byte[] pictureData,int format) throws InvalidFormatException
    {
        XWPFPictureData xwpfPicData = findPackagePictureData(pictureData, format);
        POIXMLRelation relDesc = XWPFPictureData.RELATIONS[format];
        
        if (xwpfPicData == null)
        {
            /* Part doesn't exist, create a new one */
            int idx = getNextPicNameNumber(format);
            xwpfPicData = (XWPFPictureData) createRelationship(relDesc, XWPFFactory.getInstance(),idx);
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
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            registerPackagePictureData(xwpfPicData);
            pictures.add(xwpfPicData);
            
            return getRelationId(xwpfPicData);
        }
        else if (!getRelations().contains(xwpfPicData))
        {
            /*
             * Part already existed, but was not related so far. Create
             * relationship to the already existing part and update
             * POIXMLDocumentPart data.
             */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            // TODO add support for TargetMode.EXTERNAL relations.
            TargetMode targetMode = TargetMode.INTERNAL;
            PackagePartName partName = picDataPart.getPartName();
            String relation = relDesc.getRelation();
            PackageRelationship relShip = getPackagePart().addRelationship(partName,targetMode,relation);
            String id = relShip.getId();
            addRelation(id,xwpfPicData);
            pictures.add(xwpfPicData);
            return id;
        }
        else 
        {
            /* Part already existed, get relation id and return it */
            return getRelationId(xwpfPicData);
        }
    }
    
    public String addPictureData(InputStream is,int format) throws InvalidFormatException
    {
        try {
            byte[] data = IOUtils.toByteArray(is);
            return addPictureData(data, format);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the next free ImageNumber
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
     * @return numbering
     */
    public XWPFNumbering getNumbering() {
        return numbering;
    }

    /**
     * get Styles
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
     * @param ctTbl
     * @see org.apache.poi.xwpf.usermodel.IBody#getTable(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl)
     * @return a table by its CTTbl-Object or null
     */
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
	 * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int)
	 */
	public XWPFParagraph getParagraphArray(int pos) {
		if(pos >= 0 && pos < paragraphs.size()){		
			return paragraphs.get(pos);
		}
		return null;
	}

    /**
     * returns the Part, to which the body belongs, which you need for adding relationship to other parts
     * Actually it is needed of the class XWPFTableCell. Because you have to know to which part the tableCell
     * belongs.
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        return this;
    }


	/**
	 * get the PartType of the body, for example
	 * DOCUMENT, HEADER, FOOTER,	FOOTNOTE,
     *
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
	 */
	public BodyType getPartType() {
		return BodyType.DOCUMENT;
	}

	/**
	 * get the TableCell which belongs to the TableCell
	 * @param cell
	 */
	public XWPFTableCell getTableCell(CTTc cell) {
		XmlCursor cursor = cell.newCursor();
		cursor.toParent();
		XmlObject o = cursor.getObject();
		if(!(o instanceof CTRow)){
			return null;
		}
		CTRow row = (CTRow)o;
		cursor.toParent();
		o = cursor.getObject();
        cursor.dispose();
		if(! (o instanceof CTTbl)){
			return null;
		}
		CTTbl tbl = (CTTbl) o;
		XWPFTable table = getTable(tbl);
		if(table == null){
			return null;
		}
		XWPFTableRow tableRow = table.getRow(row);
		if(row == null){
			return null;
		}
		return tableRow.getTableCell(cell);
	}

    public XWPFDocument getXWPFDocument() {
        return this;
    }
} // end class
