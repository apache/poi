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
package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;

/**
 * Common class for {@link HSSFHeader} and
 *  {@link HSSFFooter}.
 */
public abstract class HeaderFooter {
	protected String left;
	protected String center;
	protected String right;
	
	private boolean stripFields = false;
	
	protected HeaderFooter(String text) {
		while (text != null && text.length() > 1) {
		    int pos = text.length();
		    switch (text.substring(1, 2).charAt(0)) {
			    case 'L' :
				if (text.indexOf("&C") >= 0) {
				    pos = Math.min(pos, text.indexOf("&C"));
				} 
				if (text.indexOf("&R") >= 0) {
				    pos = Math.min(pos, text.indexOf("&R"));
				} 
				left = text.substring(2, pos);
				text = text.substring(pos);
				break;
		    case 'C' : 
				if (text.indexOf("&L") >= 0) {
				    pos = Math.min(pos, text.indexOf("&L"));
				} 
				if (text.indexOf("&R") >= 0) {
				    pos = Math.min(pos, text.indexOf("&R"));
				} 
				center = text.substring(2, pos);
				text = text.substring(pos);
				break;
		    case 'R' : 
				if (text.indexOf("&C") >= 0) {
				    pos = Math.min(pos, text.indexOf("&C"));
				} 
				if (text.indexOf("&L") >= 0) {
				    pos = Math.min(pos, text.indexOf("&L"));
				} 
				right = text.substring(2, pos);
				text = text.substring(pos);
				break;
		    default: 
		    	text = null;
		    }
		}
	}
	
    /**
     * Get the left side of the header or footer.
     * @return The string representing the left side.
     */
    public String getLeft() {
    	if(stripFields)
    		return stripFields(left);
		return left;
	}
    public abstract void setLeft( String newLeft );

    /**
     * Get the center of the header or footer.
     * @return The string representing the center.
     */
    public String getCenter() {
    	if(stripFields)
    		return stripFields(center);
    	return center;
    }
    public abstract void setCenter( String newCenter );

    /**
     * Get the right side of the header or footer.
     * @return The string representing the right side.
     */
    public String getRight() {
    	if(stripFields)
    		return stripFields(right);
    	return right;
    }
    public abstract void setRight( String newRight );

    
    

    /**
     * Returns the string that represents the change in font size.
     *
     * @param size the new font size
     * @return The special string to represent a new font size
     */
    public static String fontSize( short size )
    {
        return "&" + size;
    }

    /**
     * Returns the string that represents the change in font.
     *
     * @param font  the new font
     * @param style the fonts style, one of regular, italic, bold, italic bold or bold italic
     * @return The special string to represent a new font size
     */
    public static String font( String font, String style )
    {
        return "&\"" + font + "," + style + "\"";
    }

    /**
     * Returns the string representing the current page number
     *
     * @return The special string for page number
     */
    public static String page() {
    	return PAGE_FIELD.sequence;
    }

    /**
     * Returns the string representing the number of pages.
     *
     * @return The special string for the number of pages
     */
    public static String numPages() {
    	return NUM_PAGES_FIELD.sequence;
    }

    /**
     * Returns the string representing the current date
     *
     * @return The special string for the date
     */
    public static String date() {
    	return DATE_FIELD.sequence;
    }

    /**
     * Returns the string representing the current time
     *
     * @return The special string for the time
     */
    public static String time() {
    	return TIME_FIELD.sequence;
    }

    /**
     * Returns the string representing the current file name
     *
     * @return The special string for the file name
     */
    public static String file() {
    	return FILE_FIELD.sequence;
    }

    /**
     * Returns the string representing the current tab (sheet) name
     *
     * @return The special string for tab name
     */
    public static String tab() {
    	return SHEET_NAME_FIELD.sequence;
    }

    /**
     * Returns the string representing the start bold
     *
     * @return The special string for start bold
     */
    public static String startBold() {
    	return BOLD_FIELD.sequence;
    }

    /**
     * Returns the string representing the end bold
     *
     * @return The special string for end bold
     */
    public static String endBold() {
    	return BOLD_FIELD.sequence;
    }

    /**
     * Returns the string representing the start underline
     *
     * @return The special string for start underline
     */
    public static String startUnderline() {
    	return UNDERLINE_FIELD.sequence;
    }

    /**
     * Returns the string representing the end underline
     *
     * @return The special string for end underline
     */
    public static String endUnderline() {
    	return UNDERLINE_FIELD.sequence;
    }

    /**
     * Returns the string representing the start double underline
     *
     * @return The special string for start double underline
     */
    public static String startDoubleUnderline() {
    	return DOUBLE_UNDERLINE_FIELD.sequence;
    }

    /**
     * Returns the string representing the end double underline
     *
     * @return The special string for end double underline
     */
    public static String endDoubleUnderline() {
    	return DOUBLE_UNDERLINE_FIELD.sequence;
    }
    
    
    /**
     * Removes any fields (eg macros, page markers etc)
     *  from the string.
     * Normally used to make some text suitable for showing
     *  to humans, and the resultant text should not normally
     *  be saved back into the document!
     */
    public static String stripFields(String text) {
    	int pos;
    	
    	// Firstly, do the easy ones which are static
    	for(int i=0; i<Field.ALL_FIELDS.size(); i++) {
    		String seq = ((Field)Field.ALL_FIELDS.get(i)).sequence;
    		while((pos = text.indexOf(seq)) > -1) {
    			text = text.substring(0, pos) +
    				text.substring(pos+seq.length());
    		}
    	}
    	
    	// Now do the tricky, dynamic ones
    	text = text.replaceAll("\\&\\d+", "");
    	text = text.replaceAll("\\&\".*?,.*?\"", "");
    	
    	// All done
    	return text;
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

    
    public static final Field SHEET_NAME_FIELD = new Field("&A");
    public static final Field DATE_FIELD = new Field("&D");
    public static final Field FILE_FIELD = new Field("&F");
    public static final Field FULL_FILE_FIELD = new Field("&Z");
    public static final Field PAGE_FIELD = new Field("&P");
    public static final Field TIME_FIELD = new Field("&T");
    public static final Field NUM_PAGES_FIELD = new Field("&N");
    
    public static final Field PICTURE_FIELD = new Field("&P");
    
    public static final PairField BOLD_FIELD = new PairField("&B"); // PAID
    public static final PairField ITALIC_FIELD = new PairField("&I");
    public static final PairField STRIKETHROUGH_FIELD = new PairField("&S");
    public static final PairField SUBSCRIPT_FIELD = new PairField("&Y");
    public static final PairField SUPERSCRIPT_FIELD = new PairField("&X");
    public static final PairField UNDERLINE_FIELD = new PairField("&U");
    public static final PairField DOUBLE_UNDERLINE_FIELD = new PairField("&E");
    
    /**
     * Represents a special field in a header or footer,
     *  eg the page number
     */
    public static class Field {
    	private static ArrayList ALL_FIELDS = new ArrayList();
    	/** The character sequence that marks this field */
    	public final String sequence;
    	private Field(String sequence) {
    		this.sequence = sequence;
    		ALL_FIELDS.add(this);
    	}
    }
    /**
     * A special field that normally comes in a pair, eg
     *  turn on underline / turn off underline
     */
    public static class PairField extends Field {
    	private PairField(String sequence) {
    		super(sequence);
    	}
    }
}
