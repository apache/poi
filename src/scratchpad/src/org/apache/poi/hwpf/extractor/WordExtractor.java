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
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.usermodel.HeaderStories;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class to extract the text from a Word Document.
 *
 * You should use either getParagraphText() or getText() unless
 *  you have a strong reason otherwise.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class WordExtractor extends POIOLE2TextExtractor {
	private POIFSFileSystem fs;
	private HWPFDocument doc;

	/**
	 * Create a new Word Extractor
	 * @param is InputStream containing the word file
	 */
	public WordExtractor(InputStream is) throws IOException {
		this( HWPFDocument.verifyAndBuildPOIFS(is) );
	}

	/**
	 * Create a new Word Extractor
	 * @param fs POIFSFileSystem containing the word file
	 */
	public WordExtractor(POIFSFileSystem fs) throws IOException {
		this(new HWPFDocument(fs));
		this.fs = fs;
	}
	public WordExtractor(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
		this(new HWPFDocument(dir, fs));
		this.fs = fs;
	}

	/**
	 * Create a new Word Extractor
	 * @param doc The HWPFDocument to extract from
	 */
	public WordExtractor(HWPFDocument doc) {
		super(doc);
		this.doc = doc;
	}

	/**
	 * Command line extractor, so people will stop moaning that
	 *  they can't just run this.
	 */
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("   java org.apache.poi.hwpf.extractor.WordExtractor <filename>");
			System.exit(1);
		}

		// Process the first argument as a file
		FileInputStream fin = new FileInputStream(args[0]);
		WordExtractor extractor = new WordExtractor(fin);
		System.out.println(extractor.getText());
	}

	/**
	 * Get the text from the word file, as an array with one String
	 *  per paragraph
	 */
        public String[] getParagraphText() {
                String[] ret;

                // Extract using the model code
                try {
                        Range r = doc.getRange();

                        ret = getParagraphText(r);
                } catch (Exception e) {
                        // Something's up with turning the text pieces into paragraphs
                        // Fall back to ripping out the text pieces
                        ret = new String[1];
                        ret[0] = getTextFromPieces();
                }

                return ret;
        }

        public String[] getFootnoteText() {
                Range r = doc.getFootnoteRange();

                return getParagraphText(r);
        }

        public String[] getEndnoteText() {
                Range r = doc.getEndnoteRange();

                return getParagraphText(r);
        }

        public String[] getCommentsText() {
                Range r = doc.getCommentsRange();

                return getParagraphText(r);
        }

        private String[] getParagraphText(Range r) {
                String[] ret;
                ret = new String[r.numParagraphs()];
                for (int i = 0; i < ret.length; i++) {
                        Paragraph p = r.getParagraph(i);
                        ret[i] = p.text();

                        // Fix the line ending
                        if (ret[i].endsWith("\r")) {
                                ret[i] = ret[i] + "\n";
                        }
                }
                return ret;
        }

        /**
	 * Add the header/footer text, if it's not empty
	 */
	private void appendHeaderFooter(String text, StringBuffer out) {
		if(text == null || text.length() == 0)
			return;

		text = text.replace('\r', '\n');
		if(! text.endsWith("\n")) {
			out.append(text);
			out.append('\n');
			return;
		}
		if(text.endsWith("\n\n")) {
			out.append(text.substring(0, text.length()-1));
			return;
		}
		out.append(text);
		return;
	}
	/**
	 * Grab the text from the headers
	 */
	public String getHeaderText() {
		HeaderStories hs = new HeaderStories(doc);

		StringBuffer ret = new StringBuffer();
		if(hs.getFirstHeader() != null) {
			appendHeaderFooter(hs.getFirstHeader(), ret);
		}
		if(hs.getEvenHeader() != null) {
			appendHeaderFooter(hs.getEvenHeader(), ret);
		}
		if(hs.getOddHeader() != null) {
			appendHeaderFooter(hs.getOddHeader(), ret);
		}

		return ret.toString();
	}
	/**
	 * Grab the text from the footers
	 */
	public String getFooterText() {
		HeaderStories hs = new HeaderStories(doc);

		StringBuffer ret = new StringBuffer();
		if(hs.getFirstFooter() != null) {
			appendHeaderFooter(hs.getFirstFooter(), ret);
		}
		if(hs.getEvenFooter() != null) {
			appendHeaderFooter(hs.getEvenFooter(), ret);
		}
		if(hs.getOddFooter() != null) {
			appendHeaderFooter(hs.getOddFooter(), ret);
		}

		return ret.toString();
	}

	/**
	 * Grab the text out of the text pieces. Might also include various
	 *  bits of crud, but will work in cases where the text piece -> paragraph
	 *  mapping is broken. Fast too.
	 */
	public String getTextFromPieces() {
    	StringBuffer textBuf = new StringBuffer();

    	Iterator textPieces = doc.getTextTable().getTextPieces().iterator();
    	while (textPieces.hasNext()) {
    		TextPiece piece = (TextPiece) textPieces.next();

    		String encoding = "Cp1252";
    		if (piece.isUnicode()) {
    			encoding = "UTF-16LE";
    		}
    		try {
    			String text = new String(piece.getRawBytes(), encoding);
    			textBuf.append(text);
    		} catch(UnsupportedEncodingException e) {
    			throw new InternalError("Standard Encoding " + encoding + " not found, JVM broken");
    		}
    	}

    	String text = textBuf.toString();

    	// Fix line endings (Note - won't get all of them
    	text = text.replaceAll("\r\r\r", "\r\n\r\n\r\n");
    	text = text.replaceAll("\r\r", "\r\n\r\n");

    	if(text.endsWith("\r")) {
    		text += "\n";
    	}

    	return text;
	}

	/**
	 * Grab the text, based on the paragraphs. Shouldn't include any crud,
	 *  but slightly slower than getTextFromPieces().
	 */
	public String getText() {
		StringBuffer ret = new StringBuffer();

		ret.append(getHeaderText());

                ArrayList<String> text = new ArrayList<String>();
                text.addAll(Arrays.asList(getParagraphText()));
                text.addAll(Arrays.asList(getFootnoteText()));
                text.addAll(Arrays.asList(getEndnoteText()));

		for(String p : text) {
			ret.append(p);
		}

		ret.append(getFooterText());

		return ret.toString();
	}

	/**
	 * Removes any fields (eg macros, page markers etc)
	 *  from the string.
	 */
	public static String stripFields(String text) {
		return Range.stripFields(text);
	}
}
