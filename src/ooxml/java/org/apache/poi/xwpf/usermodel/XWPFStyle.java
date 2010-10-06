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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

/**
 * @author Philipp Epp
 *
 */
public class XWPFStyle {
	
	 private CTStyle ctStyle;
	 protected XWPFStyles styles;

	 /**
	  * constructor
	  * @param style
	  */
	 public XWPFStyle(CTStyle style){
		 this(style,null);
	 }
	 /**
	  * constructor
	  * @param style
	  * @param styles
	  */
	 public XWPFStyle(CTStyle style, XWPFStyles styles){
		 this.ctStyle  = style;
		 this.styles = styles;
	 }

	 /**
	  * get StyleID of the style
	  * @return styleID		StyleID of the style
	  */
	 public String getStyleId(){
		 return ctStyle.getStyleId();
	 }
	 
	 /**
	  * get Type of the Style
	  * @return	ctType 
	  */
	 public STStyleType.Enum getType(){
		 return ctStyle.getType();
	 }
	 
	 /**
	  * set style
	  * @param style		
	  */
	 public void setStyle(CTStyle style){
		 this.ctStyle = style;
	 }
	 /**
	  * get ctStyle
	  * @return	ctStyle
	  */
	 public CTStyle getCTStyle(){
		 return this.ctStyle;
	 }
	 /**
	  * set styleID
	  * @param styleId
	  */
	 public void setStyleId(String styleId){
		 ctStyle.setStyleId(styleId);
	 }
	 
	 /**
	  * set styleType
	  * @param type
	  */
	 public void setType(STStyleType.Enum type){
		 ctStyle.setType(type);
	 }
	 /**
	  * get styles
	  * @return styles		the styles to which this style belongs
	  */
	 public XWPFStyles getStyles(){
		 return styles;
	 }
	 
	 public String getBasisStyleID(){
		 if(ctStyle.getBasedOn()!=null)
			 return ctStyle.getBasedOn().getVal();
		 else
			 return null;
	 }
	 
	 
	 /**
	  * get StyleID of the linked Style
	  */
	 public String getLinkStyleID(){
		 if (ctStyle.getLink()!=null)
			 return ctStyle.getLink().getVal();
		 else
			 return null;
	 }
	 
	 /**
	  * get StyleID of the next style
	  */
	 public String getNextStyleID(){
		if(ctStyle.getNext()!=null)
			return ctStyle.getNext().getVal();
		else
			return null;
	 }
	 
	 public String getName() {
	    if(ctStyle.isSetName()) 
	       return ctStyle.getName().getVal();
	    return null;
	 }
	 
	 /**
	  * compares the names of the Styles 
	  * @param compStyle
	  */
	 public boolean hasSameName(XWPFStyle compStyle){
		CTStyle ctCompStyle = compStyle.getCTStyle();
		String name = ctCompStyle.getName().getVal();
		return name.equals(ctStyle.getName().getVal());
	 }
	 
}//end class
