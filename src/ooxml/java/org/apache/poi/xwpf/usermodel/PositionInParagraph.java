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
 * postion of a character in a paragrapho
* 1st RunPositon
* 2nd TextPosition
* 3rd CharacterPosition 
* 
*
*/
public class PositionInParagraph {
	private int posRun = 0, posText = 0, posChar = 0;

	public PositionInParagraph(){
	}
	
	public PositionInParagraph(int posRun, int posText, int posChar){
		this.posRun=posRun;
		this.posChar=posChar;
		this.posText= posText;
	}
	
	public int getRun() {
		return posRun;
	}

	public void setRun(int beginRun) {
		this.posRun = beginRun;
	}

	public int getText() {
		return posText;
	}

	public void setText(int beginText) {
		this.posText = beginText;
	}

	public int getChar() {
		return posChar;
	}

	public void setChar(int beginChar) {
		this.posChar = beginChar;
	}

}
