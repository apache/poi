/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

import java.io.UnsupportedEncodingException;

import java.text.NumberFormat;
import java.text.FieldPosition;

/**
 *  Title: String Utility Description: Collection of string handling utilities
 * 
 * Now it is quite confusing: the method pairs, in which
 * one of them write data and other read written data are:
 * putUncompressedUnicodeHigh and getFromUnicode
 * putUncompressedUnicode     and getFromUnicodeHigh
 *
 *@author     Andrew C. Oliver
 *@author     Sergei Kozello (sergeikozello at mail.ru)
 *@created    May 10, 2002
 *@version    1.0
 */

public class StringUtil {
    
    private final static String ENCODING="ISO-8859-1";
    /**
     *  Constructor for the StringUtil object
     */
    private StringUtil() { }

    
    /**
     *  given a byte array of 16-bit unicode characters, compress to 8-bit and
     *  return a string
     *
     * { 0x16, 0x00 } -> 0x16
     * 
     *@param  string                              the byte array to be converted
     *@param  offset                              the initial offset into the
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
     *@len                                        the length of the final string
     */

    public static String getFromUnicodeHigh(final byte[] string,
            final int offset, final int len)
             throws ArrayIndexOutOfBoundsException, IllegalArgumentException {

        if ((offset < 0) || (offset >= string.length)) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset");
        }
        if ((len < 0) || (((string.length - offset) / 2) < len)) {
            throw new IllegalArgumentException("Illegal length");
        }
        
        char[] chars = new char[ len ];
        for ( int i = 0; i < chars.length; i++ ) {
            chars[i] = (char)( string[ offset + ( 2*i ) ] & 0xFF | 
                             ( string[ offset + ( 2*i+1 ) ] << 8 ) );
        }

        return new String( chars );
    }
    
    
    /**
     *  given a byte array of 16-bit unicode characters, compress to 8-bit and
     *  return a string
     * 
     * { 0x16, 0x00 } -> 0x16
     *
     *@param  string  the byte array to be converted
     *@return         the converted string
     */

    public static String getFromUnicodeHigh( final byte[] string ) {
        return getFromUnicodeHigh( string, 0, string.length / 2 );
    }


    /**
     *  given a byte array of 16-bit unicode characters, compress to 8-bit and
     *  return a string
     * 
     * { 0x00, 0x16 } -> 0x16
     *
     *@param  string                              the byte array to be converted
     *@param  offset                              the initial offset into the
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
     *@len                                        the length of the final string
     */

    public static String getFromUnicode(final byte[] string,
            final int offset, final int len)
             throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if ((offset < 0) || (offset >= string.length)) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset");
        }
        if ((len < 0) || (((string.length - offset) / 2) < len)) {
            throw new IllegalArgumentException("Illegal length");
        }

        
        char[] chars = new char[ len ];
        for ( int i = 0; i < chars.length; i++ ) {
            chars[i] = (char)( ( string[ offset + ( 2*i ) ] << 8 ) +
                              string[ offset + ( 2*i+1 ) ] );
        }
        
        return new String( chars );
    }


    /**
     *  given a byte array of 16-bit unicode characters, compress to 8-bit and
     *  return a string
     * 
     * { 0x00, 0x16 } -> 0x16
     *
     *@param  string  the byte array to be converted
     *@return         the converted string
     */

    public static String getFromUnicode(final byte[] string) {
        return getFromUnicode(string, 0, string.length / 2);
    }


      /**
      * read compressed unicode(8bit)
      * 
      * @author Toshiaki Kamoshida(kamoshida.toshiaki at future dot co dot jp)
      * 
      * @param string byte array to read
      * @param offset offset to read byte array
      * @param len    length to read byte array
      * @return String generated String instance by reading byte array
      */
     public static String getFromCompressedUnicode(final byte[] string,
            final int offset, final int len){
         try{
             return new String(string,offset,len,"ISO-8859-1");
         }
         catch(UnsupportedEncodingException e){
             throw new InternalError();/* unreachable */
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

    public static void putCompressedUnicode(final String input,
            final byte[] output,
            final int offset) {
        int strlen = input.length();

        for (int k = 0; k < strlen; k++) {
            output[offset + k] = (byte) input.charAt(k);
        }
    }


    /**
     *  Write uncompressed unicode
     *
     *@param  input   the String containing the unicode data to be written
     *@param  output  the byte array to hold the uncompressed unicode
     *@param  offset  the offset to start writing into the byte array
     */

    public static void putUncompressedUnicode(final String input,
            final byte[] output,
            final int offset) {
        int strlen = input.length();

        for (int k = 0; k < strlen; k++) {
            char c = input.charAt(k);

            output[offset + (2 * k)] = (byte) c;
            output[offset + (2 * k) + 1] = (byte) (c >> 8);
        }
    }

    /**
     *  Write uncompressed unicode
     *
     *@param  input   the String containing the unicode data to be written
     *@param  output  the byte array to hold the uncompressed unicode
     *@param  offset  the offset to start writing into the byte array
     */

    public static void putUncompressedUnicodeHigh(final String input,
            final byte[] output,
            final int offset) {
        int strlen = input.length();

        for (int k = 0; k < strlen; k++) {
            char c = input.charAt(k);

            output[offset + (2 * k)] = (byte) (c >> 8);
            output[offset + (2 * k)] = (byte) c;
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
                } else if ((params[currentParamNumber] instanceof Number)
                        && (i + 1 < message.length())) {
                    i += matchOptionalFormatting(
                            (Number) params[currentParamNumber++],
                            message.substring(i + 1), formattedMessage);
                } else {
                    formattedMessage.append(params[currentParamNumber++].toString());
                }
            } else {
                if ((message.charAt(i) == '\\') && (i + 1 < message.length())
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
    private static int matchOptionalFormatting(Number number,
            String formatting,
            StringBuffer outputTo) {
        NumberFormat numberFormat = NumberFormat.getInstance();

        if ((0 < formatting.length())
                && Character.isDigit(formatting.charAt(0))) {
            numberFormat.setMinimumIntegerDigits(Integer.parseInt(formatting.charAt(0) + ""));
            if ((2 < formatting.length()) && (formatting.charAt(1) == '.')
                    && Character.isDigit(formatting.charAt(2))) {
                numberFormat.setMaximumFractionDigits(Integer.parseInt(formatting.charAt(2) + ""));
                numberFormat.format(number, outputTo, new FieldPosition(0));
                return 3;
            }
            numberFormat.format(number, outputTo, new FieldPosition(0));
            return 1;
        } else if ((0 < formatting.length()) && (formatting.charAt(0) == '.')) {
            if ((1 < formatting.length())
                    && Character.isDigit(formatting.charAt(1))) {
                numberFormat.setMaximumFractionDigits(Integer.parseInt(formatting.charAt(1) + ""));
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