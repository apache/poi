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

package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.HWPFDocument;

/**
 * Helper class for {@link HWPFDocument}, which figures out
 *  where different kinds of text can be found within the
 *  overall CP splurge.
 */
public final class CPSplitCalculator {
	private FileInformationBlock fib;
	public CPSplitCalculator(FileInformationBlock fib) {
		this.fib = fib;
	}

	/**
	 * Where the main document text starts. Always 0.
	 */
	public int getMainDocumentStart() {
		return 0;
	}
	/**
	 * Where the main document text ends.
	 * Given by FibRgLw97.ccpText
	 */
	public int getMainDocumentEnd() {
		return fib.getCcpText();
	}

	/**
	 * Where the Footnotes text starts.
	 * Follows straight on from the main text.
	 */
	public int getFootnoteStart() {
		return getMainDocumentEnd();
	}
	/**
	 * Where the Footnotes text ends.
	 * Length comes from FibRgLw97.ccpFtn
	 */
	public int getFootnoteEnd() {
		return getFootnoteStart() +
			fib.getCcpFtn();
	}

	/**
	 * Where the "Header Story" text starts.
	 * Follows straight on from the footnotes.
	 */
	public int getHeaderStoryStart() {
		return getFootnoteEnd();
	}
	/**
	 * Where the "Header Story" text ends.
	 * Length comes from FibRgLw97.ccpHdd
	 */
	public int getHeaderStoryEnd() {
		return getHeaderStoryStart() +
			fib.getCcpHdd();
	}

	/**
	 * Where the Comment (Atn) text starts.
	 * Follows straight on from the header stories.
	 */
	public int getCommentsStart() {
		return getHeaderStoryEnd();
	}
	/**
	 * Where the Comment (Atn) text ends.
	 * Length comes from FibRgLw97.ccpAtn
	 */
	public int getCommentsEnd() {
		return getCommentsStart() +
			fib.getCcpCommentAtn();
	}

	/**
	 * Where the End Note text starts.
	 * Follows straight on from the comments.
	 */
	public int getEndNoteStart() {
		return getCommentsEnd();
	}
	/**
	 * Where the End Note text ends.
	 * Length comes from FibRgLw97.ccpEdn
	 */
	public int getEndNoteEnd() {
		return getEndNoteStart() +
			fib.getCcpEdn();
	}

	/**
	 * Where the Main Textbox text starts.
	 * Follows straight on from the end note.
	 */
	public int getMainTextboxStart() {
		return getEndNoteEnd();
	}
	/**
	 * Where the Main textbox text ends.
	 * Length comes from FibRgLw97.ccpTxBx
	 */
	public int getMainTextboxEnd() {
		return getMainTextboxStart() +
			fib.getCcpTxtBx();
	}

	/**
	 * Where the Header Textbox text starts.
	 * Follows straight on from the main textbox.
	 */
	public int getHeaderTextboxStart() {
		return getMainTextboxEnd();
	}
	/**
	 * Where the Header textbox text ends.
	 * Length comes from FibRgLw97.ccpHdrTxBx
	 */
	public int getHeaderTextboxEnd() {
		return getHeaderTextboxStart() +
			fib.getCcpHdrTxtBx();
	}
}
