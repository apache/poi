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
package org.apache.poi.hpbf.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.hpbf.model.qcbits.QCBit;
import org.apache.poi.hpbf.model.qcbits.QCTextBit;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Extract text from HPBF Publisher files 
 */
public class PublisherTextExtractor extends POIOLE2TextExtractor {
	private HPBFDocument doc;
	
	public PublisherTextExtractor(HPBFDocument doc) {
		super(doc);
		this.doc = doc;
	}
	public PublisherTextExtractor(POIFSFileSystem fs) throws IOException {
		this(new HPBFDocument(fs));
	}
	public PublisherTextExtractor(InputStream is) throws IOException {
		this(new POIFSFileSystem(is));
	}
	
	public String getText() {
		StringBuffer text = new StringBuffer();
		
		// Get the text from the Quill Contents
		QCBit[] bits = doc.getQuillContents().getBits();
		for(int i=0; i<bits.length; i++) {
			if(bits[i] != null && bits[i] instanceof QCTextBit) {
				QCTextBit t = (QCTextBit)bits[i];
				text.append( t.getText().replace('\r', '\n') );
			}
		}
		
		// Get more text
		// TODO
		
		return text.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("  PublisherTextExtractor <file.pub>");
		}
		
		for(int i=0; i<args.length; i++) {
			PublisherTextExtractor te = new PublisherTextExtractor(
					new FileInputStream(args[i])
			);
			System.out.println(te.getText());
		}
	}
}
