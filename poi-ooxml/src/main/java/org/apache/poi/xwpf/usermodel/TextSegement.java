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


/**
 * saves the begin and end position  of a text in a Paragraph
*/
public class TextSegement {
	private PositionInParagraph beginPos;
	private PositionInParagraph endPos;

	public TextSegement(){
		this.beginPos = new PositionInParagraph();
		this. endPos = new PositionInParagraph();		
	}
	
	public TextSegement(int beginRun, int endRun, int beginText, int endText, int beginChar, int endChar){
		PositionInParagraph beginPos = new PositionInParagraph(beginRun, beginText, beginChar);
		PositionInParagraph endPos = new PositionInParagraph(endRun, endText, endChar);
		this.beginPos = beginPos;
		this.endPos = endPos;
	}
	
	public TextSegement(PositionInParagraph beginPos, PositionInParagraph endPos){
		this.beginPos = beginPos;
		this.endPos = endPos;
	}

	public PositionInParagraph getBeginPos(){
		return beginPos;
	}
	
	public PositionInParagraph getEndPos(){
		return endPos;
	}
	
	public int getBeginRun(){
		return beginPos.getRun();
	}
	
	public void setBeginRun(int beginRun){
		beginPos.setRun(beginRun);
	}
	
	public int getBeginText(){
		return beginPos.getText();
	}
	
	public void setBeginText(int beginText){
		beginPos.setText(beginText);
	}
	
	public int getBeginChar(){
		return beginPos.getChar();
	}
	
	public void setBeginChar(int beginChar){
		beginPos.setChar(beginChar);
	}
	public int getEndRun(){
		return endPos.getRun();
	}
	
	public void setEndRun(int endRun){
		endPos.setRun(endRun);
	}
	
	public int getEndText(){
		return endPos.getText();
	}
	
	public void setEndText(int endText){
		endPos.setText(endText);
	}
	
	public int getEndChar(){
		return endPos.getChar();
	}
	
	public void setEndChar(int endChar){
		endPos.setChar(endChar);
	}
}
