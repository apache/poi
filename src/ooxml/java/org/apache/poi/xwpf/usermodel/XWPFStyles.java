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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;

/**
 * @author Philipp Epp
 *
 */
public class XWPFStyles extends POIXMLDocumentPart{
	private CTStyles ctStyles;
	protected XWPFLatentStyles latentStyles;
	protected List<XWPFStyle> listStyle;
	
	/**
     * Construct XWPFStyles from a package part
     *
     * @param part the package part holding the data of the styles,
     * @param rel  the package relationship of type "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
     */

	public XWPFStyles(PackagePart part, PackageRelationship rel) throws IOException, OpenXML4JException{
		super(part, rel);
		onDocumentRead();
	}
	/**
	 * Read document
	 */
	 @Override
	protected void onDocumentRead ()throws IOException{
		listStyle = new ArrayList<XWPFStyle>();
		StylesDocument stylesDoc;
		try {
			InputStream is = getPackagePart().getInputStream();
			stylesDoc = StylesDocument.Factory.parse(is);
	        ctStyles = stylesDoc.getStyles();
	        latentStyles = new XWPFLatentStyles(ctStyles.getLatentStyles(), this);
	        
		} catch (XmlException e) {
			throw new POIXMLException();
		}
        //get any Style
        for(CTStyle style : ctStyles.getStyleList()) {
            listStyle.add(new XWPFStyle(style, this));
        }
	}
	
	 @Override
	    protected void commit() throws IOException {
	        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
	        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTStyles.type.getName().getNamespaceURI(), "styles"));
	        Map map = new HashMap();
	        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
	        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
	        xmlOptions.setSaveSuggestedPrefixes(map);
	        PackagePart part = getPackagePart();
	        OutputStream out = part.getOutputStream();
	        ctStyles.save(out, xmlOptions);
	        out.close();
	    }

	

	
	 /**
	  * checks whether style with styleID exist
	  * @param styleID		styleID of the Style in the style-Document
	  * @return				true if style exist, false if style not exist
	  */
	public boolean styleExist(String styleID){
		for (XWPFStyle style : listStyle) {
			if (style.getStyleId().equals(styleID))
				return true;
		}
		return false;
	}
	/**
	 * add a style to the document
	 * @param style				
	 * @throws IOException		 
	 */
	public void addStyle(XWPFStyle style){
		listStyle.add(style);
		ctStyles.addNewStyle();
		int pos = (ctStyles.getStyleList().size()) - 1;
		ctStyles.setStyleArray(pos, style.getCTStyle());
	}
	/**
	 *get style by a styleID 
	 * @param styleID	styleID of the searched style
	 * @return style
	 */
	public XWPFStyle getStyle(String styleID){
		for (XWPFStyle style : listStyle) {
			if(style.getStyleId().equals(styleID))
				return style;		
		}
		return null;
	}

	/**
	 * get the styles which are related to the parameter style and their relatives
	 * this method can be used to copy all styles from one document to another document 
	 * @param style
	 * @return a list of all styles which were used by this method 
	 */
	public List<XWPFStyle> getUsedStyleList(XWPFStyle style){
		List<XWPFStyle> usedStyleList = new ArrayList<XWPFStyle>();
		usedStyleList.add(style);
		return getUsedStyleList(style, usedStyleList);
	}
	
	/** 
	 * get the styles which are related to parameter style
	 * @param style
	 * @return all Styles of the parameterList
	 */
	private List<XWPFStyle> getUsedStyleList(XWPFStyle style, List<XWPFStyle> usedStyleList){
		String basisStyleID  = style.getBasisStyleID();
		XWPFStyle basisStyle = getStyle(basisStyleID);
		if((basisStyle!=null)&&(!usedStyleList.contains(basisStyle))){
			usedStyleList.add(basisStyle);
			getUsedStyleList(basisStyle, usedStyleList);
		}		
		String linkStyleID = style.getLinkStyleID();
		XWPFStyle linkStyle = getStyle(linkStyleID);
		if((linkStyle!=null)&&(!usedStyleList.contains(linkStyle))){
			usedStyleList.add(linkStyle);
			getUsedStyleList(linkStyle, usedStyleList);
		}
		
		String nextStyleID = style.getNextStyleID();
		XWPFStyle nextStyle = getStyle(nextStyleID);
		if((nextStyle!=null)&&(!usedStyleList.contains(nextStyle))){
			usedStyleList.add(linkStyle);
			getUsedStyleList(linkStyle, usedStyleList);
		}		
		return usedStyleList;
	}
	
	
	
	/**
	 * get latentstyles
	 */
	public XWPFLatentStyles getLatentStyles() {
		return latentStyles;
	}
	
	/**
	 * get the style with the same name
	 * if this style is not existing, return null
	 */
	public XWPFStyle getStyleWithSameName(XWPFStyle style){
		for (XWPFStyle ownStyle : listStyle) {
			if(ownStyle.hasSameName(style)){
				return ownStyle;
			}	
		}
		return null;
		
	}
}//end class
