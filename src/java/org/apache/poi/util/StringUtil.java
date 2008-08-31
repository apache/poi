
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
        
package org.apache.poi.util;

import java.io.UnsupportedEncodingException;
import java.text.FieldPosition;
import java.text.NumberFormat;
/** 
 *  Title: String Utility Description: Collection of string handling utilities 
 *  
 * 
 *@author     Andrew C. Oliver 
 *@author     Sergei Kozello (sergeikozello at mail.ru) 
 *@author     Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp) 
 *@since      May 10, 2002 
 *@version    1.0 
 */
public class StringUtil {
	private final static String ENCODING = "ISO-8859-1";
	/**     
	 *  Constructor for the StringUtil object     
	 */
	private StringUtil() {
	}

	/**     
	 *  Given a byte array of 16-bit unicode characters in Little Endian
	 *  format (most important byte last), return a Java String representation
	 *  of it. 
	 *     
	 * { 0x16, 0x00 } -0x16     
	 *      
	 * @param  string  the byte array to be converted
	 * @param  offset  the initial offset into the
	 *                 byte array. it is assumed that string[ offset ] and string[ offset +
	 *                 1 ] contain the first 16-bit unicode character
     * @param len the length of the final string
	 * @return                                     the converted string
	 * @exception  ArrayIndexOutOfBoundsException  if offset is out of bounds for
	 *      the byte array (i.e., is negative or is greater than or equal to     
	 *      string.length)     
	 * @exception  IllegalArgumentException        if len is too large (i.e.,
	 *      there is not enough data in string to create a String of that     
	 *      length)     
	 */
	public static String getFromUnicodeLE(
		final byte[] string,
		final int offset,
		final int len)
		throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if ((offset < 0) || (offset >= string.length)) {
			throw new ArrayIndexOutOfBoundsException("Illegal offset");
		}
		if ((len < 0) || (((string.length - offset) / 2) < len)) {
			throw new IllegalArgumentException("Illegal length " + len);
		}

		try {
			return new String(string, offset, len * 2, "UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 *  Given a byte array of 16-bit unicode characters in little endian
	 *  format (most important byte last), return a Java String representation
	 *  of it. 
	 *      
	 * { 0x16, 0x00 } -0x16     
	 *     
	 *@param  string  the byte array to be converted     
	 *@return         the converted string    
	 */
	public static String getFromUnicodeLE(final byte[] string) {
		if(string.length == 0) { return ""; }
		return getFromUnicodeLE(string, 0, string.length / 2);
	}

	/**     
	 *  Given a byte array of 16-bit unicode characters in big endian
	 *  format (most important byte first), return a Java String representation
	 *  of it. 
	 *      
	 * { 0x00, 0x16 } -0x16     
	 *     
	 *@param  string                              the byte array to be converted     
	 **@param  offset                              the initial offset into the     
	 *      byte array. it is assumed that string[ offset ] and string[ offset +     
	 *      1 ] contain the first 16-bit unicode character     
         *@param len the length of the final string     
	 *@return                                     the converted string     
	 *@exception  ArrayIndexOutOfBoundsException  if offset is out of bounds for     
	 *      the byte array (i.e., is negative or is greater than or equal to     
	 *      string.length)     
	 *@exception  IllegalArgumentException        if len is too large (i.e.,     
	 *      there is not enough data in string to create a String of that     
	 *      length)     
	 */
	public static String getFromUnicodeBE(
		final byte[] string,
		final int offset,
		final int len)
		throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if ((offset < 0) || (offset >= string.length)) {
			throw new ArrayIndexOutOfBoundsException("Illegal offset");
		}
		if ((len < 0) || (((string.length - offset) / 2) < len)) {
			throw new IllegalArgumentException("Illegal length");
		}
		try {
			return new String(string, offset, len * 2, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 *  Given a byte array of 16-bit unicode characters in big endian
	 *  format (most important byte first), return a Java String representation
	 *  of it.
	 *      
	 * { 0x00, 0x16 } -0x16     
	 *     
	 *@param  string  the byte array to be converted     
	 *@return         the converted string     
	 */
	public static String getFromUnicodeBE(final byte[] string) {
		if(string.length == 0) { return ""; }
		return getFromUnicodeBE(string, 0, string.length / 2);
	}

	/**      
	 * Read 8 bit data (in ISO-8859-1 codepage) into a (unicode) Java
	 * String and return.
	 * (In Excel terms, read compressed 8 bit unicode as a string)
	 *       
	 * @param string byte array to read      
	 * @param offset offset to read byte array      
	 * @param len    length to read byte array      
	 * @return String generated String instance by reading byte array      
	 */
	public static String getFromCompressedUnicode(
		final byte[] string,
		final int offset,
		final int len) {
		try {
			int len_to_use = Math.min(len, string.length - offset);
			return new String(string, offset, len_to_use, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /* unreachable */
		}
	}

	/**      
	 * Takes a unicode (java) string, and returns it as 8 bit data (in ISO-8859-1 
	 * codepage).
	 * (In Excel terms, write compressed 8 bit unicode)
	 *     
	 *@param  input   the String containing the data to be written     
	 *@param  output  the byte array to which the data is to be written     
	 *@param  offset  an offset into the byte arrat at which the data is start     
	 *      when written     
	 */
	public static void putCompressedUnicode(
		final String input,
		final byte[] output,
		final int offset) {
		try {
			byte[] bytes = input.getBytes("ISO-8859-1");
			System.arraycopy(bytes, 0, output, offset, bytes.length);
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 * Takes a unicode string, and returns it as little endian (most 
	 * important byte last) bytes in the supplied byte array.
	 * (In Excel terms, write uncompressed unicode)
	 *     
	 *@param  input   the String containing the unicode data to be written     
	 *@param  output  the byte array to hold the uncompressed unicode, should be twice the length of the String
	 *@param  offset  the offset to start writing into the byte array     
	 */
	public static void putUnicodeLE(
		final String input,
		final byte[] output,
		final int offset) {
		try {
			byte[] bytes = input.getBytes("UTF-16LE");
			System.arraycopy(bytes, 0, output, offset, bytes.length);
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 * Takes a unicode string, and returns it as big endian (most 
	 * important byte first) bytes in the supplied byte array.
	 * (In Excel terms, write uncompressed unicode)
	 *     
	 *@param  input   the String containing the unicode data to be written     
	 *@param  output  the byte array to hold the uncompressed unicode, should be twice the length of the String
	 *@param  offset  the offset to start writing into the byte array     
	 */
	public static void putUnicodeBE(
		final String input,
		final byte[] output,
		final int offset) {
		try {
			byte[] bytes = input.getBytes("UTF-16BE");
			System.arraycopy(bytes, 0, output, offset, bytes.length);
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 *  Apply printf() like formatting to a string.      
	 *  Primarily used for logging.    
	 *@param  message  the string with embedded formatting info 
	 *                 eg. "This is a test %2.2"     
	 *@param  params   array of values to format into the string     
	 *@return          The formatted string     
	 */
	public static String format(String message, Object[] params) {
		int currentParamNumber = 0;
		StringBuffer formattedMessage = new StringBuffer();
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '%') {
				if (currentParamNumber >= params.length) {
					formattedMessage.append("?missing data?");
				} else if (
					(params[currentParamNumber] instanceof Number)
						&& (i + 1 < message.length())) {
					i
						+= matchOptionalFormatting(
							(Number) params[currentParamNumber++],
							message.substring(i + 1),
							formattedMessage);
				} else {
					formattedMessage.append(
						params[currentParamNumber++].toString());
				}
			} else {
				if ((message.charAt(i) == '\\')
					&& (i + 1 < message.length())
					&& (message.charAt(i + 1) == '%')) {
					formattedMessage.append('%');
					i++;
				} else {
					formattedMessage.append(message.charAt(i));
				}
			}
		}
		return formattedMessage.toString();
	}


	private static int matchOptionalFormatting(
		Number number,
		String formatting,
		StringBuffer outputTo) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		if ((0 < formatting.length())
			&& Character.isDigit(formatting.charAt(0))) {
			numberFormat.setMinimumIntegerDigits(
				Integer.parseInt(formatting.charAt(0) + ""));
			if ((2 < formatting.length())
				&& (formatting.charAt(1) == '.')
				&& Character.isDigit(formatting.charAt(2))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(2) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 3;
			}
			numberFormat.format(number, outputTo, new FieldPosition(0));
			return 1;
		} else if (
			(0 < formatting.length()) && (formatting.charAt(0) == '.')) {
			if ((1 < formatting.length())
				&& Character.isDigit(formatting.charAt(1))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(1) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 2;
			}
		}
		numberFormat.format(number, outputTo, new FieldPosition(0));
		return 1;
	}

	/**     
	 * @return the encoding we want to use, currently hardcoded to ISO-8859-1     
	 */
	public static String getPreferredEncoding() {
		return ENCODING;
	}

	/**
	 * check the parameter has multibyte character
	 *
	 * @param value  string to check
	 * @return  boolean result
	 *  true:string has at least one multibyte character
	 */
	public static boolean hasMultibyte(String value){
	    if( value == null )return false;
	    for(int i = 0 ; i < value.length() ; i++ ){
	        char c = value.charAt(i);
	        if(c > 0xFF )return true;
	    }
	    return false;
	}
	
	/**
	 * Checks to see if a given String needs to be represented as Unicode
	 * @param value 
	 * @return true if string needs Unicode to be represented.
	 */
	  public static boolean isUnicodeString(final String value) {
	    try {
	      return !value.equals(new String(value.getBytes("ISO-8859-1"), "ISO-8859-1"));
	    } catch (UnsupportedEncodingException e) {
	      return true;
	    }
	  }
}
