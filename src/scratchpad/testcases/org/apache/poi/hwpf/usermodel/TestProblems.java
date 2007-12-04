/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hwpf.usermodel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.util.LittleEndian;

import junit.framework.TestCase;

/**
 * Test various problem documents
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestProblems extends TestCase {
	private String dirname = System.getProperty("HWPF.testdata.path");
	
    protected void setUp() throws Exception {
    }			
    
    /**
     * ListEntry passed no ListTable
     */
    public void testListEntryNoListTable() throws Exception {
    	HWPFDocument doc = new HWPFDocument(new FileInputStream(dirname + "/ListEntryNoListTable.doc"));
    	
    	Range r = doc.getRange();
    	StyleSheet styleSheet = doc.getStyleSheet();
    	for (int x = 0; x < r.numSections(); x++) {
    		Section s = r.getSection(x);
    		for (int y = 0; y < s.numParagraphs(); y++) {
    			Paragraph paragraph = s.getParagraph(y);
    			System.out.println(paragraph.getCharacterRun(0).text());
    		}
    	}
    }
}
