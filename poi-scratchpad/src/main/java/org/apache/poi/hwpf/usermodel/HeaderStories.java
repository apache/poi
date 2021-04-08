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
import org.apache.poi.hwpf.model.SubdocumentType;

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

	private boolean stripFields;

	public HeaderStories(HWPFDocument doc) {
		this.headerStories = doc.getHeaderStoryRange();
		FileInformationBlock fib = doc.getFileInformationBlock();

//        // If there's no PlcfHdd, nothing to do
//        if(fib.getCcpHdd() == 0) {
//            return;
//        }

        if (fib.getSubdocumentTextStreamLength( SubdocumentType.HEADER ) == 0)
		    return;
		
		if(fib.getPlcfHddSize() == 0) {
			return;
		}

        // Handle the PlcfHdd
        /*
         * Page 88:
         * 
         * "The plcfhdd, a table whose location and length within the file is
         * stored in fib.fcPlcfhdd and fib.cbPlcfhdd, describes where the text
         * of each header/footer begins. If there are n headers/footers stored
         * in the Word file, the plcfhdd consists of n+2 CP entries. The
         * beginning CP of the ith header/footer is the ith CP in the plcfhdd.
         * The limit CP (the CP of character 1 position past the end of a
         * header/footer) of the ith header/footer is the i+1st CP in the
         * plcfhdd. Note: at the limit CP - 1, Word always places a chEop as a
         * placeholder which is never displayed as part of the header/footer.
         * This allows Word to change an existing header/footer to be empty.
         * 
         * If there are n header/footers, the n+2nd CP entry value is always 1
         * greater than the n+1st CP entry value. A paragraph end (ASCII 13) is
         * always stored at the file position marked by the n+1st CP value.
         * 
         * The transformation in a full saved file from a header/footer CP to an
         * offset from the beginning of a file (fc) is
         * fc=fib.fcMin+ccpText+ccpFtn+cp."
         */
        plcfHdd = new PlexOfCps( doc.getTableStream(), fib.getPlcfHddOffset(),
                fib.getPlcfHddSize(), 0 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getFootnoteSeparator()
    {
        return getAt( 0 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getFootnoteContSeparator()
    {
        return getAt( 1 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getFootnoteContNote()
    {
        return getAt( 2 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getEndnoteSeparator()
    {
        return getAt( 3 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getEndnoteContSeparator()
    {
        return getAt( 4 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getEndnoteContNote()
    {
        return getAt( 5 );
    }

    public Range getFootnoteSeparatorSubrange()
    {
        return getSubrangeAt( 0 );
    }

    public Range getFootnoteContSeparatorSubrange()
    {
        return getSubrangeAt( 1 );
    }

    public Range getFootnoteContNoteSubrange()
    {
        return getSubrangeAt( 2 );
    }

    public Range getEndnoteSeparatorSubrange()
    {
        return getSubrangeAt( 3 );
    }

    public Range getEndnoteContSeparatorSubrange()
    {
        return getSubrangeAt( 4 );
    }

    public Range getEndnoteContNoteSubrange()
    {
        return getSubrangeAt( 5 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
	@Deprecated
	public String getEvenHeader() {
		return getAt(6+0);
	}
	/**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getOddHeader() {
		return getAt(6+1);
	}
    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getFirstHeader() {
		return getAt(6+4);
	}
	

    public Range getEvenHeaderSubrange() {
        return getSubrangeAt(6+0);
    }
    public Range getOddHeaderSubrange() {
        return getSubrangeAt(6+1);
    }
    public Range getFirstHeaderSubrange() {
        return getSubrangeAt(6+4);
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
            final String fh = getFirstHeader();
			if(fh != null && !fh.isEmpty()) {
				return fh;
			}
		}
		// Even page header is optional, only return
		//  if it's set
		if(pageNumber % 2 == 0) {
            final String eh = getEvenHeader();
			if(eh != null && !eh.isEmpty()) {
				return eh;
			}
		}
		// Odd is the default
		return getOddHeader();
	}

	/**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getEvenFooter()
    {
        return getAt( 6 + 2 );
    }

	/**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getOddFooter()
    {
        return getAt( 6 + 3 );
    }

    /**
     * @deprecated 3.8 beta 4 
     */
    @Deprecated
    public String getFirstFooter()
    {
        return getAt( 6 + 5 );
    }

    public Range getEvenFooterSubrange()
    {
        return getSubrangeAt( 6 + 2 );
    }

    public Range getOddFooterSubrange()
    {
        return getSubrangeAt( 6 + 3 );
    }

    public Range getFirstFooterSubrange()
    {
        return getSubrangeAt( 6 + 5 );
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
		    final String ff = getFirstFooter();
			if(ff != null && !ff.isEmpty()) {
				return ff;
			}
		}
		// Even page footer is optional, only return
		//  if it's set
		if(pageNumber % 2 == 0) {
		    final String ef = getEvenFooter();
			if(ef != null && !ef.isEmpty()) {
				return ef;
			}
		}
		// Odd is the default
		return getOddFooter();
	}


	/**
	 * Get the string that's pointed to by the
	 *  given plcfHdd index
	 * @deprecated 3.8 beta 4
	 */
    @Deprecated
	private String getAt(int plcfHddIndex) {
		if(plcfHdd == null) return null;

		GenericPropertyNode prop = plcfHdd.getProperty(plcfHddIndex);
		if(prop.getStart() == prop.getEnd()) {
			// Empty story
			return "";
		}
		if(prop.getEnd() < prop.getStart()) {
		   // Broken properties?
		   return "";
		}

		// Ensure we're getting a sensible length
		String rawText = headerStories.text();
		int start = Math.min(prop.getStart(), rawText.length());
		int end = Math.min(prop.getEnd(), rawText.length());

		// Grab the contents
		String text = rawText.substring(start, end);

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

    private Range getSubrangeAt( int plcfHddIndex )
    {
        if ( plcfHdd == null )
            return null;

        GenericPropertyNode prop = plcfHdd.getProperty( plcfHddIndex );
        if ( prop.getStart() == prop.getEnd() )
        {
            // Empty story
            return null;
        }
        if ( prop.getEnd() < prop.getStart() )
        {
            // Broken properties?
            return null;
        }

        final int headersLength = headerStories.getEndOffset()
                - headerStories.getStartOffset();
        int start = Math.min( prop.getStart(), headersLength );
        int end = Math.min( prop.getEnd(), headersLength );

        return new Range( headerStories.getStartOffset() + start,
                headerStories.getStartOffset() + end, headerStories );
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
