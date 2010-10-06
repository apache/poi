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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
    protected List<XWPFFooter> footers;
    protected List <XWPFHeader> headers;
    protected List<XWPFComment> comments;
    protected List<XWPFHyperlink> hyperlinks;
    protected List<XWPFParagraph> paragraphs;
    protected List<XWPFTable> tables;
    protected List<IBodyElement> bodyElements;
    protected List<XWPFPictureData> pictures;
    protected Map<Integer, XWPFFootnote> footnotes;
    protected Map<Integer, XWPFFootnote> endnotes;
    protected XWPFNumbering numbering;
    protected XWPFStyles styles;

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
        hyperlinks = new ArrayList<XWPFHyperlink>();
        comments = new ArrayList<XWPFComment>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables= new ArrayList<XWPFTable>();
        bodyElements = new ArrayList<IBodyElement>();
        footers = new ArrayList<XWPFFooter>();
        headers = new ArrayList<XWPFHeader>();
        footnotes = new HashMap<Integer, XWPFFootnote>();
        endnotes = new HashMap<Integer, XWPFFootnote>();

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
                	XWPFParagraph p = new XWPFParagraph((CTP)o, this);
                	bodyElements.add(p);
                	paragraphs.add(p);
                }
                if (o instanceof CTTbl) {
                	XWPFTable t = new XWPFTable((CTTbl)o, this);
                	bodyElements.add(t);
                	tables.add(t);
                }
            }
			
            // Sort out headers and footers
			if (doc.getDocument().getBody().getSectPr() != null)
				headerFooterPolicy = new XWPFHeaderFooterPolicy(this);
				
			// Create for each XML-part in the Package a PartClass
            for(POIXMLDocumentPart p : getRelations()){
                String relation = p.getPackageRelationship().getRelationshipType();
                if(relation.equals(XWPFRelation.STYLES.getRelation())){
                	this.styles = (XWPFStyles) p;
                }
                else if(relation.equals(XWPFRelation.NUMBERING.getRelation())){
                	this.numbering = (XWPFNumbering) p;

                }
                else if(relation.equals(XWPFRelation.FOOTER.getRelation())){
                	footers.add((XWPFFooter)p);
                }
                else if(relation.equals(XWPFRelation.HEADER.getRelation())){
                	headers.add((XWPFHeader)p);
                }

                else if(relation.equals(XWPFRelation.COMMENT.getRelation())){
                    CommentsDocument cmntdoc = CommentsDocument.Factory.parse(p.getPackagePart().getInputStream());
                    for(CTComment ctcomment : cmntdoc.getComments().getCommentList()) {
                        comments.add(new XWPFComment(ctcomment));
                    }
                }
                else if(relation.equals(XWPFRelation.SETTINGS.getRelation())){
                	settings = (XWPFSettings)p;
                }
            }

            initHyperlinks();
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
        // create for every Graphic-Part in Package a new XWPFGraphic
        getAllPictures();
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

                for(CTFtnEdn ctFtnEdn : footnotesDocument.getFootnotes().getFootnoteList()) {
                    footnotes.put(ctFtnEdn.getId().intValue(), new XWPFFootnote(this, ctFtnEdn));
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
    protected void onDocumentCreate() {
        hyperlinks = new ArrayList<XWPFHyperlink>();
        comments = new ArrayList<XWPFComment>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables= new ArrayList<XWPFTable>();

        ctDocument = CTDocument1.Factory.newInstance();
        ctDocument.addNewBody();
        
        settings =  (XWPFSettings) createRelationship(XWPFRelation.SETTINGS, XWPFFactory.getInstance());

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
        return Collections.unmodifiableCollection(footnotes == null ? new ArrayList<XWPFFootnote>() : footnotes.values());
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
    
    /**
     * get with the position of a Paragraph in the bodyelement array list 
     * the position of this paragraph in the paragraph array list
     * @param pos position of the paragraph in the bodyelement array list
     * @return if there is a paragraph at the position in the bodyelement array list,
     * 			else it will return -1 
     * 			
     */
    public int getParagraphPos(int pos){
    	if(pos >= 0 && pos < bodyElements.size()){
    		if(bodyElements.get(pos).getElementType() == BodyElementType.PARAGRAPH){
	    		int startPos;
	    		//find the startpoint for searching
	    		if(pos < paragraphs.size()){
	    			startPos = pos;
	    		}
	    		else{
	    			startPos = (paragraphs.size());
	    		}
	    		for(int i = startPos; i < 0; i--){
	    			if(paragraphs.get(i) == bodyElements.get(pos))
	    				return i;
	    		}
	    	}
    	}
    	if(paragraphs.size() == 0){
    		return 0;
    	}
    	return -1;
    }
    
    /**
     * get with the position of a table in the bodyelement array list 
     * the position of this table in the table array list
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * 		   else it will return null. 
     */
    public int getTablePos(int pos){
    	if(pos >= 0 && pos < bodyElements.size()){
    		if(bodyElements.get(pos).getElementType() == BodyElementType.TABLE){
	    		int startPos;
	    		//find the startpoint for searching
	    		if(pos < tables.size()){
	    			startPos = pos;
	    		}
	    		else{
	    			startPos = (tables.size());
	    		}
	    		for(int i = startPos; i > 0; i--){
	    			if(tables.get(i) == bodyElements.get(pos))
	    				return i;
	    		}
	    	}
    	}
    	if(tables.size() == 0){
    		return 0;
    	}
    	else
    		return -1;
    }
    
    /**
     * add a new paragraph at position of the cursor
     * @param cursor
     */
    public XWPFParagraph insertNewParagraph(XmlCursor cursor){
    	if(isCursorInBody(cursor)){
    		String uri = CTP.type.getName().getNamespaceURI();
    		String localPart = "p";
    		cursor.beginElement(localPart,uri);
    		cursor.toParent();
    		CTP p = (CTP)cursor.getObject();
    		XWPFParagraph newP = new XWPFParagraph(p, this);
    		XmlObject o = null;
	    	while(!(o instanceof CTP)&&(cursor.toPrevSibling())){
	    		o = cursor.getObject();
	    	}
	    	if((!(o instanceof CTP)) || (CTP)o == p){
	    		paragraphs.add(0, newP);
	    	}
	    	else{
	    		int pos = paragraphs.indexOf(getParagraph((CTP)o))+1;
	    		paragraphs.add(pos,newP);
	    	}
	    	int i=0;
	    	cursor.toCursor(p.newCursor());
			while(cursor.toPrevSibling()){
				o =cursor.getObject();
				if(o instanceof CTP || o instanceof CTTbl)
					i++;
			}
			bodyElements.add(i, newP);
	    	cursor.toCursor(p.newCursor());
	    	cursor.toEndToken();
	    	return newP;
    	}
    	return null;
    }

	public XWPFTable insertNewTbl(XmlCursor cursor) {
		if(isCursorInBody(cursor)){
			String uri = CTTbl.type.getName().getNamespaceURI();
			String localPart ="tbl";
    		cursor.beginElement(localPart,uri);
			cursor.toParent();
			CTTbl t = (CTTbl)cursor.getObject();
			XWPFTable newT = new XWPFTable(t, this);
			cursor.removeXmlContents();
			XmlObject o = null;
			while(!(o instanceof CTTbl)&&(cursor.toPrevSibling())){
				o = cursor.getObject();
			}
			if(!(o instanceof CTTbl)){
				tables.add(0, newT);
			}
			else{
				int pos = tables.indexOf(getTable((CTTbl)o))+1;
				tables.add(pos,newT);
			}
			int i=0;
			cursor = t.newCursor();
			while(cursor.toPrevSibling()){
				o =cursor.getObject();
				if(o instanceof CTP || o instanceof CTTbl)
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
		if(verify.getObject() == this.ctDocument.getBody()){
			return true;
		}
		XmlObject o = verify.getObject();
		return false;
		
	}

	/**
	 * get position of the paragraph
	 * @param p
	 */
	public Integer getPosOfParagraph(XWPFParagraph p){
    	int i, pos = 0;
    	for (i = 0 ; i < bodyElements.size() ; i++) {
    		if (bodyElements.get(i) instanceof XWPFParagraph){
    			if (bodyElements.get(i).equals(p)){
    				return pos;
    			}
    			pos++;
    		}
		}
    	return null;
    }
	
	public Integer getPosOfTable(XWPFTable t){
		int i, pos = 0;
		for(i = 0; i < bodyElements.size(); i++){
			if(bodyElements.get(i).getElementType() == BodyElementType.TABLE){
				if (bodyElements.get(i) == t){
					return pos;
				}
				pos++;
			}
		}
		return null;
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
     * Appends a new paragraph to this document
     * @return a new paragraph
     */
    public XWPFParagraph createParagraph(){
        return new XWPFParagraph(ctDocument.getBody().addNewP(), this);
    }
    
    /**
     * remove a BodyElement from bodyElements array list 
     * @param pos
     * @return true if removing was successfully, else return false
     */
    public boolean removeBodyElement(int pos){
    	if(pos >= 0 && pos < bodyElements.size()){
    		if(bodyElements.get(pos).getElementType() == BodyElementType.TABLE){
    			bodyElements.remove(pos);
    			Integer tablePos = getTablePos(pos);
    			tables.remove(tablePos);
    			ctDocument.getBody().removeTbl(tablePos);
    			return true;    			
    		}
    		if(bodyElements.get(pos).getElementType() == BodyElementType.PARAGRAPH){
    			bodyElements.remove(pos);
    			Integer paraPos = getParagraphPos(pos);
    			paragraphs.remove(paraPos);
    			ctDocument.getBody().removeP(paraPos);
    			return true;    			
    		}
    	}
    	return false;
    }

    /**
     * copies content of a paragraph to a existing paragraph in the list paragraphs at position pos
     * @param paragraph
     * @param pos
     */
    public void setParagraph(XWPFParagraph paragraph, int pos){
    	paragraphs.set(pos, paragraph);
    	ctDocument.getBody().setPArray(pos, paragraph.getCTP());
    }
    
    /**
     * @return the LastParagraph of the document
     */
    public XWPFParagraph getLastParagraph(){
    	int lastPos = paragraphs.toArray().length - 1;
    	return paragraphs.get(lastPos);
    }

    /**
     * Create an empty table with one row and one column as default.
     * @return a new table
     */
    public XWPFTable createTable(){
        return new XWPFTable(ctDocument.getBody().addNewTbl(), this);
    }

    /**
     * Create an empty table with a number of rows and cols specified
     * @param rows
     * @param cols
     * @return table
     */
    public XWPFTable createTable(int rows, int cols) {
    	return new XWPFTable(ctDocument.getBody().addNewTbl(), this, rows, cols);
    }
    
    
    /**
     * 
     */
    public void createTOC() {
        CTSdtBlock block = this.getDocument().getBody().addNewSdt();
        TOC toc = new TOC(block);
        for (XWPFParagraph par: paragraphs ) {
            String parStyle = par.getStyle();
            if (parStyle != null && parStyle.substring(0, 7).equals("Heading")) {
                try {
                    int level = Integer.valueOf(parStyle.substring("Heading".length()));
                    toc.addRow(level, par.getText(), 1, "112723803");
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**Replace content of table in array tables at position pos with a
     * @param pos
     * @param table
     */
    public void setTable(int pos, XWPFTable table){
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
			if(tbl == table.getCTTbl()){
				break;
			}
		}
		tables.add(i, table);
	}
    
    public List<XWPFPictureData> getAllPictures() {
    	if(pictures == null){
    		pictures = new ArrayList<XWPFPictureData>();
    		for (POIXMLDocumentPart poixmlDocumentPart : getRelations()){
    			if(poixmlDocumentPart instanceof XWPFPictureData){
    				pictures.add((XWPFPictureData)poixmlDocumentPart);
    			}
    		}
    	}
    return pictures;
    }
    
    /**
     * @return  all Pictures in this package
     */
    public List<XWPFPictureData> getAllPackagePictures(){
    	List<XWPFPictureData> pkgpictures = new ArrayList<XWPFPictureData>();
    	pkgpictures.addAll(getAllPictures());
    	for (POIXMLDocumentPart poixmlDocumentPart : getRelations()){
			if(poixmlDocumentPart instanceof XWPFHeaderFooter){
				pkgpictures.addAll(((XWPFHeaderFooter)poixmlDocumentPart).getAllPictures());
			}
		}
    	return pkgpictures;
    }
   
     /**
     * Adds a picture to the document.
     *
     * @param is                The stream to read image from
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException 
     */
    public int addPicture(InputStream is, int format) throws IOException, InvalidFormatException {
        int imageNumber = getNextPicNameNumber(format);
        XWPFPictureData img = (XWPFPictureData)createRelationship(XWPFPictureData.RELATIONS[format], XWPFFactory.getInstance(), imageNumber, false);
        OutputStream out = img.getPackagePart().getOutputStream();
        IOUtils.copy(is, out);
        out.close();
        pictures.add(img);
        return getAllPictures().size()-1;
    }
    
    /**
     * Adds a picture to the document.
     *
     * @param pictureData       The bytes to read image from
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException 
     */
    public int addPicture(byte[] pictureData, int format) throws InvalidFormatException {
        try {
           return addPicture(new ByteArrayInputStream(pictureData), format);
        } catch (IOException e){
            throw new POIXMLException(e);
        }
    }
    
    /**
     * get the next free ImageNumber
     * @param format
     * @return the next free ImageNumber
     * @throws InvalidFormatException 
     */
    public int getNextPicNameNumber(int format) throws InvalidFormatException{
    	int img = getAllPackagePictures().size()+1;
   		String proposal = XWPFPictureData.RELATIONS[format].getFileName(img);
   		PackagePartName createPartName = PackagingURIHelper.createPartName(proposal);
		while (this.getPackage().getPart(createPartName)!= null){
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
     * @throws Exception 
     */
    public XWPFPictureData getPictureDataByID(String blipID) {
    	for(POIXMLDocumentPart part: getRelations()){
    	  if(part.getPackageRelationship() != null){
    		  if(part.getPackageRelationship().getId() != null){
    			  if(part.getPackageRelationship().getId().equals(blipID)){
    				  return (XWPFPictureData)part;
    			  }
    		  }
    	  	}
    	}
		return null;	    	
    }
    
    /**
     *  getNumbering
     * @return numbering
     */
    public XWPFNumbering getNumbering(){
    	return numbering;
    }

	/**
	 * get Styles 
	 * @return  styles for this document
	 */
	public XWPFStyles getStyles(){
		return styles;
	}
	
	/**
	 *  get the paragraph with the CTP class p
     *
	 * @param p
	 * @return  the paragraph with the CTP class p
	 */
	public XWPFParagraph getParagraph(CTP p){
		for(int i=0; i<getParagraphs().size(); i++){
			if(getParagraphs().get(i).getCTP() == p) return getParagraphs().get(i); 
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
		for(int i=0; i<tables.size(); i++){
			if(getTables().get(i).getCTTbl() == ctTbl) return getTables().get(i); 
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
	public IBody getPart() {
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
}//end class
