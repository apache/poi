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


import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;

/**
 * @author Philipp Epp
 *
 */
public class XWPFNum {
	private CTNum ctNum;
	protected XWPFNumbering numbering;
	
	public XWPFNum(){
		this.ctNum = null;
		this.numbering = null;
	}
	
	public XWPFNum(CTNum ctNum){
		this.ctNum = ctNum;
		this.numbering = null;
	}
	
	public XWPFNum(XWPFNumbering numbering){
		this.ctNum = null;
		this.numbering = numbering;
	}
	
	public XWPFNum(CTNum ctNum, XWPFNumbering numbering){
		this.ctNum = ctNum;
		this.numbering = numbering;
	}
	
	public XWPFNumbering getNumbering(){
		return numbering;
	}
	
	public CTNum getCTNum(){
		return ctNum;
	}
	
	public void setNumbering(XWPFNumbering numbering){
		this.numbering = numbering;
	}
	
	public void setCTNum(CTNum ctNum){
		this.ctNum = ctNum;
	}
}