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

package org.apache.poi.hwpf.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.HWPFOldDocument.TextAndCHPX;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class to extract the text from old (Word 6 / Word 95) Word Documents.
 *
 * This should only be used on the older files, for most uses you
 *  should call {@link WordExtractor} which deals properly 
 *  with HWPF.
 *
 * @author Nick Burch
 */
public final class Word6Extractor extends POIOLE2TextExtractor {
	private POIFSFileSystem fs;
	private HWPFOldDocument doc;

	/**
	 * Create a new Word Extractor
	 * @param is InputStream containing the word file
	 */
	public Word6Extractor(InputStream is) throws IOException {
		this( new POIFSFileSystem(is) );
	}

	/**
	 * Create a new Word Extractor
	 * @param fs POIFSFileSystem containing the word file
	 */
	public Word6Extractor(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot(), fs);
	}
	public Word6Extractor(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
	    this(new HWPFOldDocument(dir,fs));
	}

	/**
	 * Create a new Word Extractor
	 * @param doc The HWPFOldDocument to extract from
	 */
	public Word6Extractor(HWPFOldDocument doc) {
		super(doc);
		this.doc = doc;
	}

    @Override
    public String getText() {
        StringBuffer text = new StringBuffer();
        for(TextAndCHPX tchpx : doc.getContents()) {
            text.append( Range.stripFields(tchpx.getText()) );
        }
        return text.toString();
    }
}
