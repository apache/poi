/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.util;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.util.Arrays;
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
	 *  given a byte array of 16-bit unicode characters, compress to 8-bit and     
	 *  return a string     
	 *     
	 * { 0x16, 0x00 } -0x16     
	 *      
	 *@param  string                              the byte array to be converted     
	 *@param  offset                              the initial offset into the     
	 *      byte array. it is assumed that string[ offset ] and string[ offset +     
	 *     1 ] contain the first 16-bit unicode character     
	 *@param  len     
	 *@return                                     the converted string     
	 *@exception  ArrayIndexOutOfBoundsException  if offset is out of bounds for     
	 *      the byte array (i.e., is negative or is greater than or equal to     
	 *      string.length)     
	 *@exception  IllegalArgumentException        if len is too large (i.e.,     
	 *      there is not enough data in string to create a String of that     
	 *      length)     
	 *@param len the length of the final string     
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
			throw new IllegalArgumentException("Illegal length");
		}

		try {
			return new String(string, offset, len * 2, "UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /*unreachable*/
		}
	}

	/**     
	 *  given a byte array of 16-bit unicode characters, compress to 8-bit and     
	 *  return a string     
	 *      
	 * { 0x16, 0x00 } -0x16     
	 *     
	 *@param  string  the byte array to be converted     
	 *@return         the converted string    
	 */
	public static String getFromUnicodeLE(final byte[] string) {
		return getFromUnicodeLE(string, 0, string.length / 2);
	}

	/**     
	 *  given a byte array of 16-bit unicode characters, compress to 8-bit and     
	 *  return a string     
	 *      
	 * { 0x00, 0x16 } -0x16     
	 *     
	 *@param  string                              the byte array to be converted     
	 **@param  offset                              the initial offset into the     
	 *      byte array. it is assumed that string[ offset ] and string[ offset +     
	 *      1 ] contain the first 16-bit unicode character     
	 *@param  len     
	 *@return                                     the converted string     
	 *@exception  ArrayIndexOutOfBoundsException  if offset is out of bounds for     
	 *      the byte array (i.e., is negative or is greater than or equal to     
	 *      string.length)     
	 *@exception  IllegalArgumentException        if len is too large (i.e.,     
	 *      there is not enough data in string to create a String of that     
	 *      length)     
	 *@param len the length of the final string     
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
	 *  given a byte array of 16-bit unicode characters, compress to 8-bit and     
	 *  return a string     
	 *      
	 * { 0x00, 0x16 } -0x16     
	 *     
	 *@param  string  the byte array to be converted     
	 *@return         the converted string     
	 */
	public static String getFromUnicodeBE(final byte[] string) {
		return getFromUnicodeBE(string, 0, string.length / 2);
	}

	/**      
	 * read compressed unicode(8bit)      
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
			return new String(string, offset, len, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(); /* unreachable */
		}
	}

	/**     
	 *  write compressed unicode     
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
	 *  Write uncompressed unicode     
	 *     
	 *@param  input   the String containing the unicode data to be written     
	 *@param  output  the byte array to hold the uncompressed unicode     
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
	 *  Write uncompressed unicode     
	 *     
	 *@param  input   the String containing the unicode data to be written     
	 *@param  output  the byte array to hold the uncompressed unicode     
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
	 *  Description of the Method     
	 *     
	 *@param  message  Description of the Parameter     
	 *@param  params   Description of the Parameter     
	 *@return          Description of the Return Value     
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

	/**     
	 *  Description of the Method     
	 *     
	 *@param  number      Description of the Parameter     
	 *@param  formatting  Description of the Parameter     
	 *@param  outputTo    Description of the Parameter     
	 *@return             Description of the Return Value     
	 */
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
	 * @return the encoding we want to use (ISO-8859-1)     
	 */
	public static String getPreferredEncoding() {
		return ENCODING;
	}
}
