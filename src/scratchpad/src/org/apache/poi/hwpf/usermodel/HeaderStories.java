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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.GenericPropertyNode;
import org.apache.poi.hwpf.model.PlexOfCps;

/**
 * A HeaderStory is a Header, a Footer, or footnote/endnote
 *  separator.
 * All the Header Stories get stored in the same Range in the
 *  document, and this handles getting out all the individual
 *  parts.
 *
 * WARNING - you shouldn't change the headers or footers,
 *  as offsets are not yet updated!
 */
public final class HeaderStories {
	private Range headerStories;
	private PlexOfCps plcfHdd;

	private boolean stripFields = false;

	public HeaderStories(HWPFDocument doc) {
		this.headerStories = doc.getHeaderStoryRange();
		FileInformationBlock fib = doc.getFileInformationBlock();

		// If there's no PlcfHdd, nothing to do
		if(fib.getCcpHdd() == 0) {
			return;
		}
		if(fib.getPlcfHddSize() == 0) {
			return;
		}

		// Handle the PlcfHdd
		plcfHdd = new PlexOfCps(
				doc.getTableStream(), fib.getPlcfHddOffset(),
				fib.getPlcfHddSize(), 0
		);
	}

	public String getFootnoteSeparator() {
		return getAt(0);
	}
	public String getFootnoteContSeparator() {
		return getAt(1);
	}
	public String getFootnoteContNote() {
		return getAt(2);
	}
	public String getEndnoteSeparator() {
		return getAt(3);
	}
	public String getEndnoteContSeparator() {
		return getAt(4);
	}
	public String getEndnoteContNote() {
		return getAt(5);
	}


	public String getEvenHeader() {
		return getAt(6+0);
	}
	public String getOddHeader() {
		return getAt(6+1);
	}
	public String getFirstHeader() {
		return getAt(6+4);
	}
	/**
	 * Returns the correct, defined header for the given
	 *  one based page
	 * @param pageNumber The one based page number
	 */
	public String getHeader(int pageNumber) {
		// First page header is optional, only return
		//  if it's set
		if(pageNumber == 1) {
			if(getFirstHeader().length() > 0) {
				return getFirstHeader();
			}
		}
		// Even page header is optional, only return
		//  if it's set
		if(pageNumber % 2 == 0) {
			if(getEvenHeader().length() > 0) {
				return getEvenHeader();
			}
		}
		// Odd is the default
		return getOddHeader();
	}


	public String getEvenFooter() {
		return getAt(6+2);
	}
	public String getOddFooter() {
		return getAt(6+3);
	}
	public String getFirstFooter() {
		return getAt(6+5);
	}
	/**
	 * Returns the correct, defined footer for the given
	 *  one based page
	 * @param pageNumber The one based page number
	 */
	public String getFooter(int pageNumber) {
		// First page footer is optional, only return
		//  if it's set
		if(pageNumber == 1) {
			if(getFirstFooter().length() > 0) {
				return getFirstFooter();
			}
		}
		// Even page footer is optional, only return
		//  if it's set
		if(pageNumber % 2 == 0) {
			if(getEvenFooter().length() > 0) {
				return getEvenFooter();
			}
		}
		// Odd is the default
		return getOddFooter();
	}


	/**
	 * Get the string that's pointed to by the
	 *  given plcfHdd index
	 */
	private String getAt(int plcfHddIndex) {
		if(plcfHdd == null) return null;

		GenericPropertyNode prop = plcfHdd.getProperty(plcfHddIndex);
		if(prop.getStart() == prop.getEnd()) {
			// Empty story
			return "";
		}

		// Grab the contents
		String text =
			headerStories.text().substring(prop.getStart(), prop.getEnd());

		// Strip off fields and macros if requested
		if(stripFields) {
			return Range.stripFields(text);
		}
		// If you create a header/footer, then remove it again, word
		//  will leave \r\r. Turn these back into an empty string,
		//  which is more what you'd expect
		if(text.equals("\r\r")) {
			return "";
		}

		return text;
	}

	public Range getRange() {
		return headerStories;
	}
	protected PlexOfCps getPlcfHdd() {
		return plcfHdd;
	}

	/**
	 * Are fields currently being stripped from
	 *  the text that this {@link HeaderStories} returns?
	 *  Default is false, but can be changed
	 */
	public boolean areFieldsStripped() {
		return stripFields;
	}
	/**
	 * Should fields (eg macros) be stripped from
	 *  the text that this class returns?
	 * Default is not to strip.
	 * @param stripFields
	 */
	public void setAreFieldsStripped(boolean stripFields) {
		this.stripFields = stripFields;
	}
}
