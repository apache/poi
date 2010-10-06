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
import java.math.BigInteger;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.NumberingDocument;

/**
 * @author Philipp Epp
 *
 */
public class XWPFNumbering extends POIXMLDocumentPart {
	private CTNumbering ctNumbering;
	protected List<XWPFAbstractNum> abstractNums;
	protected List<XWPFNum> nums;
	protected boolean isNew;
	
	/**
	 *create a new styles object with an existing document 
	 */
	public XWPFNumbering(PackagePart part, PackageRelationship rel) throws IOException, OpenXML4JException{
		super(part, rel);
		isNew = true;
		onDocumentRead();
	}
	
	/**
	 * read numbering form an existing package
	 */
	@Override
	protected void onDocumentRead() throws IOException{
		abstractNums = new ArrayList<XWPFAbstractNum>();
		nums = new ArrayList<XWPFNum>();
		NumberingDocument numberingDoc = null;
		InputStream is;
		is = getPackagePart().getInputStream();
		try {
			numberingDoc = NumberingDocument.Factory.parse(is);
			ctNumbering = numberingDoc.getNumbering();
	        //get any Nums
	        for(CTNum ctNum : ctNumbering.getNumList()) {
	            nums.add(new XWPFNum(ctNum, this));
	        }
	        for(CTAbstractNum ctAbstractNum : ctNumbering.getAbstractNumList()){
	        	abstractNums.add(new XWPFAbstractNum(ctAbstractNum, this));
	        }
	        isNew = false;
		} catch (XmlException e) {
			throw new POIXMLException();
		}
	}
	
	/**
	 * save and commit numbering
	 */
	@Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "numbering"));
        Map map = new HashMap();
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("urn:schemas-microsoft-com:vml", "v");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        xmlOptions.setSaveSuggestedPrefixes(map);
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctNumbering.save(out, xmlOptions);
        out.close();
    }

	
	
	/**
	 * Checks whether number with numID exists
	 * @param numID
	 * @return boolean		true if num exist, false if num not exist
	 */
	public boolean numExist(BigInteger numID){
		for (XWPFNum num : nums) {
			if (num.getCTNum().getNumId().equals(numID))
				return true;
		}
		return false;
	}
	
	/**
	 * add a new number to the numbering document
	 * @param num
	 */
	public BigInteger addNum(XWPFNum num){
		ctNumbering.addNewNum();
		int pos = (ctNumbering.getNumList().size()) - 1;
		ctNumbering.setNumArray(pos, num.getCTNum());
		nums.add(num);
		return num.getCTNum().getNumId();
	}
	
	/**
	 * Add a new num with an abstractNumID
	 * @return return NumId of the added num 
	 */
	public BigInteger addNum(BigInteger abstractNumID){
		CTNum ctNum = this.ctNumbering.addNewNum();
		ctNum.addNewAbstractNumId();
		ctNum.getAbstractNumId().setVal(abstractNumID);
		ctNum.setNumId(BigInteger.valueOf(nums.size()+1));
		XWPFNum num = new XWPFNum(ctNum, this);
		nums.add(num);
		return ctNum.getNumId();
	}
	
	
	/**
	 * get Num by NumID
	 * @param numID
	 * @return abstractNum with NumId if no Num exists with that NumID 
	 * 			null will be returned
	 */
	public XWPFNum getNum(BigInteger numID){
		for(XWPFNum num: nums){
			if(num.getCTNum().getNumId().equals(numID))
				return num;
		}
		return null;
	}
	/**
	 * get AbstractNum by abstractNumID
	 * @param abstractNumID
	 * @return  abstractNum with abstractNumId if no abstractNum exists with that abstractNumID 
	 * 			null will be returned
	 */
	public XWPFAbstractNum getAbstractNum(BigInteger abstractNumID){
		for(XWPFAbstractNum abstractNum: abstractNums){
			if(abstractNum.getAbstractNum().getAbstractNumId().equals(abstractNumID)){
				return abstractNum;
			}
		}
		return null;
	}
	/**
	 * Compare AbstractNum with abstractNums of this numbering document.
	 * If the content of abstractNum equals with an abstractNum of the List in numbering
	 * the BigInteger Value of it will be returned.
	 * If no equal abstractNum is existing null will be returned
	 * 
	 * @param abstractNum
	 * @return 	BigInteger
	 */
	public BigInteger getIdOfAbstractNum(XWPFAbstractNum abstractNum){
		CTAbstractNum copy = (CTAbstractNum) abstractNum.getCTAbstractNum().copy();
		XWPFAbstractNum newAbstractNum = new XWPFAbstractNum(copy, this);
		int i;
		for (i = 0; i < abstractNums.size(); i++) {
			newAbstractNum.getCTAbstractNum().setAbstractNumId(BigInteger.valueOf(i));
			newAbstractNum.setNumbering(this);
			if(newAbstractNum.getCTAbstractNum().valueEquals(abstractNums.get(i).getCTAbstractNum())){
				return newAbstractNum.getCTAbstractNum().getAbstractNumId();
			}
		}
		return null;
	}


	/**
	 * add a new AbstractNum and return its AbstractNumID 
	 * @param abstractNum
	 */
	public BigInteger addAbstractNum(XWPFAbstractNum abstractNum){
		int pos = abstractNums.size();
		ctNumbering.addNewAbstractNum();
		abstractNum.getAbstractNum().setAbstractNumId(BigInteger.valueOf(pos));
		ctNumbering.setAbstractNumArray(pos, abstractNum.getAbstractNum());
		abstractNums.add(abstractNum);
		return abstractNum.getCTAbstractNum().getAbstractNumId();
	}
	
	/**
	 * remove an existing abstractNum 
	 * @param abstractNumID
	 * @return true if abstractNum with abstractNumID exists in NumberingArray,
	 * 		   false if abstractNum with abstractNumID not exists
	 */
	public boolean removeAbstractNum(BigInteger abstractNumID){
		if(abstractNumID.byteValue()<abstractNums.size()){
			ctNumbering.removeAbstractNum(abstractNumID.byteValue());
			abstractNums.remove(abstractNumID.byteValue());
			return true;
		}
		return false;
	}
	/**
	 *return the abstractNumID
	 *If the AbstractNumID not exists
	 *return null
	 * @param 		numID
	 * @return 		abstractNumID
	 */
	public BigInteger getAbstractNumID(BigInteger numID){
		XWPFNum num = getNum(numID);
		if(num == null)
			return null;
		if (num.getCTNum() == null)
			return null;
		if (num.getCTNum().getAbstractNumId() == null)
			return null;
		return num.getCTNum().getAbstractNumId().getVal();
	}
}
	
