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
package org.apache.poi.ss.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.util.LocaleID;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 *  Convert java DateFormat patterns into Excel custom number formats.
 *  For example, to format a date in excel using the "dd MMMM, yyyy" pattern and Japanese
 *  locale, use the following code:
 *
 *  <pre><code>
 *      // returns "[$-0411]dd MMMM, yyyy;@" where the [$-0411] prefix tells Excel to use the Japanese locale
 *      String excelFormatPattern = DateFormatConverter.convert(Locale.JAPANESE, "dd MMMM, yyyy");
 *
 *      CellStyle cellStyle = workbook.createCellStyle();
 *
 *      DataFormat poiFormat = workbook.createDataFormat();
 *      cellStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
 *      cell.setCellValue(new Date());
 *      cell.setCellStyle(cellStyle);  // formats date as '2012\u5e743\u670817\u65e5'
 *  </code></pre>
 *
 * TODO Generalise this for all Excel format strings
 */
@SuppressWarnings("unused")
public final class DateFormatConverter  {
	private static POILogger logger = POILogFactory.getLogger(DateFormatConverter.class);

	private DateFormatConverter() {
	}

	public static class DateFormatTokenizer {
		String format;
		int pos;

		public DateFormatTokenizer(String format) {
			this.format = format;
		}

		public String getNextToken() {
			if( pos >= format.length() ) {
				return null;
			}
			int subStart = pos;
			final char curChar = format.charAt(pos);
			++pos;
			if( curChar == '\'' ) {
				while( ( pos < format.length() ) && ( format.charAt(pos) != '\'' ) ) {
					++pos;
				}
				if( pos < format.length() ) {
					++pos;
				}
			} else {
				while( ( pos < format.length() ) && ( format.charAt(pos) == curChar ) ) {
					++pos;
				}
			}
			return format.substring(subStart,pos);
		}

		public static String[] tokenize( String format ) {
			List<String> result = new ArrayList<>();

			DateFormatTokenizer tokenizer = new DateFormatTokenizer(format);
			String token;
			while( ( token = tokenizer.getNextToken() ) != null ) {
				result.add(token);
			}

			return result.toArray(new String[0]);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();

			DateFormatTokenizer tokenizer = new DateFormatTokenizer(format);
			String token;
			while( ( token = tokenizer.getNextToken() ) != null ) {
				if( result.length() > 0 ) {
					result.append( ", " );
				}
				result.append("[").append(token).append("]");
			}

			return result.toString();
		}
	}

	private static Map<String,String> tokenConversions = prepareTokenConversions();

	private static Map<String,String> prepareTokenConversions() {
		Map<String,String> result = new HashMap<>();

		result.put( "EEEE", "dddd" );
		result.put( "EEE", "ddd" );
		result.put( "EE", "ddd" );
		result.put( "E", "d" );
		result.put( "Z", "" );
		result.put( "z", "" );
		result.put( "a", "am/pm" );
		result.put( "A", "AM/PM" );
		result.put( "K", "H" );
		result.put( "KK", "HH" );
		result.put( "k", "h" );
		result.put( "kk", "hh" );
		result.put( "S", "0" );
		result.put( "SS", "00" );
		result.put( "SSS", "000" );

		return result;
	}

	public static String getPrefixForLocale( Locale locale ) {
		final String languageTag = locale.toLanguageTag();
		if ("".equals(languageTag)) {
			// JDK 8 adds an empty locale-string, see also https://issues.apache.org/jira/browse/LANG-941
			return "[$-0409]";
		}

		LocaleID loc = LocaleID.lookupByLanguageTag(languageTag);
		if (loc == null) {
			String cmpTag = (languageTag.indexOf('_') > -1) ? languageTag.replace('_','-') : languageTag;
			int idx = languageTag.length();
			while (loc == null && (idx = cmpTag.lastIndexOf('-', idx-1)) > 0) {
				loc = LocaleID.lookupByLanguageTag(languageTag.substring(0, idx));
			}
			if (loc == null) {
				logger.log( POILogger.ERROR, "Unable to find prefix for Locale '", languageTag, "' or its parent locales." );
				return "";
			}
		}

		return String.format(Locale.ROOT, "[$-%04X]", loc.getLcid());
	}

    public static String convert( Locale locale, DateFormat df ) {
        String ptrn = ((SimpleDateFormat)df).toPattern();
        return convert(locale, ptrn);
    }

    public static String convert( Locale locale, String format ) {
		StringBuilder result = new StringBuilder();

		result.append(getPrefixForLocale(locale));
		DateFormatTokenizer tokenizer = new DateFormatTokenizer(format);
		String token;
		while( ( token = tokenizer.getNextToken() ) != null ) {
			if( token.startsWith("'") ) {
				result.append( token.replace('\'', '"') );
			} else if( ! Character.isLetter( token.charAt( 0 ) ) ) {
				result.append( token );
			} else {
				// It's a code, translate it if necessary
				String mappedToken = tokenConversions.get(token);
				result.append( mappedToken == null ? token : mappedToken );
			}
		}
        result.append(";@");
		return result.toString().trim();
	}

	public static String getJavaDatePattern(int style, Locale locale) {
    	DateFormat df = DateFormat.getDateInstance(style, locale);
    	if( df instanceof SimpleDateFormat ) {
    		return ((SimpleDateFormat)df).toPattern();
    	} else {
    		switch( style ) {
    		case DateFormat.SHORT:
    			return "d/MM/yy";
    		case DateFormat.LONG:
    			return "MMMM d, yyyy";
    		case DateFormat.FULL:
    			return "dddd, MMMM d, yyyy";
    		default:
			case DateFormat.MEDIUM:
    			return "MMM d, yyyy";
    		}
    	}
	}

	public static String getJavaTimePattern(int style, Locale locale) {
    	DateFormat df = DateFormat.getTimeInstance(style, locale);
    	if( df instanceof SimpleDateFormat ) {
    		return ((SimpleDateFormat)df).toPattern();
    	} else {
    		switch( style ) {
    		case DateFormat.SHORT:
    			return "h:mm a";
			default:
    		case DateFormat.MEDIUM:
    		case DateFormat.LONG:
    		case DateFormat.FULL:
    			return "h:mm:ss a";
    		}
    	}
	}

	public static String getJavaDateTimePattern(int style, Locale locale) {
    	DateFormat df = DateFormat.getDateTimeInstance(style, style, locale);
    	if( df instanceof SimpleDateFormat ) {
    		return ((SimpleDateFormat)df).toPattern();
    	} else {
    		switch( style ) {
    		case DateFormat.SHORT:
    			return "M/d/yy h:mm a";
    		case DateFormat.LONG:
    			return "MMMM d, yyyy h:mm:ss a";
    		case DateFormat.FULL:
    			return "dddd, MMMM d, yyyy h:mm:ss a";
    		default:
			case DateFormat.MEDIUM:
    			return "MMM d, yyyy h:mm:ss a";
    		}
    	}
	}

}
