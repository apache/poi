/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.util;

import java.io.UnsupportedEncodingException;
/**
 * This class provides encode/decode for RFC 2045 Base64 as
 * defined by RFC 2045, N. Freed and N. Borenstein.
 * RFC 2045: Multipurpose Internet Mail Extensions (MIME)
 * Part One: Format of Internet Message Bodies. Reference
 * 1996 Available at: http://www.ietf.org/rfc/rfc2045.txt
 * This class is used by XML Schema binary format validation
 *
 * This implementation does not encode/decode streaming
 * data. You need the data that you will encode/decode
 * already on a byte arrray.
 *
 * @author Jeffrey Rodriguez
 * @author Sandy Gao
 * @version $Id$
 */
public final class  Base64 {

    static private final int  BASELENGTH         = 255;
    static private final int  LOOKUPLENGTH       = 64;
    static private final int  TWENTYFOURBITGROUP = 24;
    static private final int  EIGHTBIT           = 8;
    static private final int  SIXTEENBIT         = 16;
    //static private final int  SIXBIT             = 6;
    static private final int  FOURBYTE           = 4;
    static private final int  SIGN               = -128;
    static private final byte PAD                = ( byte ) '=';
    static private final boolean fDebug          = false;
    static private byte [] base64Alphabet        = new byte[BASELENGTH];
    static private byte [] lookUpBase64Alphabet  = new byte[LOOKUPLENGTH];

    static {

        for (int i = 0; i<BASELENGTH; i++) {
            base64Alphabet[i] = -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            base64Alphabet[i] = (byte) (i-'A');
        }
        for (int i = 'z'; i>= 'a'; i--) {
            base64Alphabet[i] = (byte) ( i-'a' + 26);
        }

        for (int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i-'0' + 52);
        }

        base64Alphabet['+']  = 62;
        base64Alphabet['/']  = 63;

        for (int i = 0; i<=25; i++)
            lookUpBase64Alphabet[i] = (byte) ('A'+i );

        for (int i = 26,  j = 0; i<=51; i++, j++)
            lookUpBase64Alphabet[i] = (byte) ('a'+ j );

        for (int i = 52,  j = 0; i<=61; i++, j++)
            lookUpBase64Alphabet[i] = (byte) ('0' + j );
        lookUpBase64Alphabet[62] = (byte) '+';
        lookUpBase64Alphabet[63] = (byte) '/';

    }

    protected static boolean isWhiteSpace(byte octect) {
        return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
    }

    protected static boolean isPad(byte octect) {
        return (octect == PAD);
    }

    protected static boolean isData(byte octect) {
        return (base64Alphabet[octect] != -1);
    }

    protected static boolean isBase64(byte octect) {
        return (isWhiteSpace(octect) || isPad(octect) || isData(octect));
    }

    /**
     * Encodes hex octects into Base64
     *
     * @param binaryData Array containing binaryData
     * @return Encoded Base64 array
     */
    public static byte[] encode(byte[] binaryData) {

        if (binaryData == null)
            return null;

        int      lengthDataBits    = binaryData.length*EIGHTBIT;
        int      fewerThan24bits   = lengthDataBits%TWENTYFOURBITGROUP;
        int      numberTriplets    = lengthDataBits/TWENTYFOURBITGROUP;
        byte     encodedData[]     = null;

        if (fewerThan24bits != 0) //data not divisible by 24 bit
            encodedData = new byte[ (numberTriplets + 1 )*4  ];
        else // 16 or 8 bit
            encodedData = new byte[ numberTriplets*4 ];

        byte k=0, l=0, b1=0,b2=0,b3=0;

        int encodedIndex = 0;
        int dataIndex   = 0;
        int i           = 0;
        if (fDebug) {
            System.out.println("number of triplets = " + numberTriplets );
        }
        for (i = 0; i<numberTriplets; i++) {

            dataIndex = i*3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            if (fDebug) {
                System.out.println( "b1= " + b1 +", b2= " + b2 + ", b3= " + b3 );
            }

            l  = (byte)(b2 & 0x0f);
            k  = (byte)(b1 & 0x03);

            encodedIndex = i*4;
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);

            byte val2 = ((b2 & SIGN)==0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);
            byte val3 = ((b3 & SIGN)==0)?(byte)(b3>>6):(byte)((b3)>>6^0xfc);

            encodedData[encodedIndex]   = lookUpBase64Alphabet[ val1 ];
            if (fDebug) {
                System.out.println( "val2 = " + val2 );
                System.out.println( "k4   = " + (k<<4));
                System.out.println( "vak  = " + (val2 | (k<<4)));
            }

            encodedData[encodedIndex+1] = lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex+2] = lookUpBase64Alphabet[ (l <<2 ) | val3 ];
            encodedData[encodedIndex+3] = lookUpBase64Alphabet[ b3 & 0x3f ];
        }

        // form integral number of 6-bit groups
        dataIndex    = i*3;
        encodedIndex = i*4;
        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) ( b1 &0x03 );
            if (fDebug) {
                System.out.println("b1=" + b1);
                System.out.println("b1<<2 = " + (b1>>2) );
            }
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ k<<4 ];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == SIXTEENBIT) {

            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex +1 ];
            l = ( byte ) ( b2 &0x0f );
            k = ( byte ) ( b1 &0x03 );

            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            byte val2 = ((b2 & SIGN)==0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);

            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[ l<<2 ];
            encodedData[encodedIndex + 3] = PAD;
        }

        return encodedData;
    }

    /**
     * Decodes Base64 data into octects
     *
     * @param base64Data Byte array containing Base64 data
     * @return Array containind decoded data.
     */
    public static byte[] decode(byte[] base64Data) {

        if (base64Data == null)
            return null;

        // remove white spaces
        base64Data = removeWhiteSpace(base64Data);

        if (base64Data.length%FOURBYTE != 0) {
            return null;//should be divisible by four
        }

        int      numberQuadruple    = (base64Data.length/FOURBYTE );

        if (numberQuadruple == 0)
            return new byte[0];

        byte     decodedData[]      = null;
        byte     b1=0,b2=0,b3=0, b4=0;//, marker0=0, marker1=0;
        byte     d1=0,d2=0,d3=0,d4=0;

        // Throw away anything not in normalizedBase64Data
        // Adjust size
        int i = 0;
        int encodedIndex = 0;
        int dataIndex    = 0;
        decodedData      = new byte[ (numberQuadruple)*3];

        for (; i<numberQuadruple-1; i++) {

            if (!isData( (d1 = base64Data[dataIndex++]) )||
                !isData( (d2 = base64Data[dataIndex++]) )||
                !isData( (d3 = base64Data[dataIndex++]) )||
                !isData( (d4 = base64Data[dataIndex++]) ))
                return null;//if found "no data" just return null

            b1 = base64Alphabet[d1];
            b2 = base64Alphabet[d2];
            b3 = base64Alphabet[d3];
            b4 = base64Alphabet[d4];

            decodedData[encodedIndex++] = (byte)(  b1 <<2 | b2>>4 ) ;
            decodedData[encodedIndex++] = (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
            decodedData[encodedIndex++] = (byte)( b3<<6 | b4 );
        }

        if (!isData( (d1 = base64Data[dataIndex++]) ) ||
            !isData( (d2 = base64Data[dataIndex++]) )) {
            return null;//if found "no data" just return null
        }

        b1 = base64Alphabet[d1];
        b2 = base64Alphabet[d2];

        d3 = base64Data[dataIndex++];
        d4 = base64Data[dataIndex++];
        if (!isData( (d3 ) ) ||
            !isData( (d4 ) )) {//Check if they are PAD characters
            if (isPad( d3 ) && isPad( d4)) {               //Two PAD e.g. 3c[Pad][Pad]
                if ((b2 & 0xf) != 0)//last 4 bits should be zero
                    return null;
                byte[] tmp = new byte[ i*3 + 1 ];
                System.arraycopy( decodedData, 0, tmp, 0, i*3 );
                tmp[encodedIndex]   = (byte)(  b1 <<2 | b2>>4 ) ;
                return tmp;
            } else if (!isPad( d3) && isPad(d4)) {               //One PAD  e.g. 3cQ[Pad]
                b3 = base64Alphabet[ d3 ];
                if ((b3 & 0x3 ) != 0)//last 2 bits should be zero
                    return null;
                byte[] tmp = new byte[ i*3 + 2 ];
                System.arraycopy( decodedData, 0, tmp, 0, i*3 );
                tmp[encodedIndex++] = (byte)(  b1 <<2 | b2>>4 );
                tmp[encodedIndex]   = (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
                return tmp;
            } else {
                return null;//an error  like "3c[Pad]r", "3cdX", "3cXd", "3cXX" where X is non data
            }
        } else { //No PAD e.g 3cQl
            b3 = base64Alphabet[ d3 ];
            b4 = base64Alphabet[ d4 ];
            decodedData[encodedIndex++] = (byte)(  b1 <<2 | b2>>4 ) ;
            decodedData[encodedIndex++] = (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
            decodedData[encodedIndex++] = (byte)( b3<<6 | b4 );

        }

        return decodedData;
    }

//    /**
//     * Decodes Base64 data into octects
//     *
//     * @param base64Data String containing Base64 data
//     * @return string containing decoded data.
//     */
//    public static String decode(String base64Data) {
//        if (base64Data == null)
//            return null;
//
//        byte[] decoded = null;
//        try {
//            decoded = decode(base64Data.getBytes("utf-8"));
//        }
//        catch(UnsupportedEncodingException e) {
//        }
//        finally {
//            return decoded == null ? null : new String(decoded);
//        }
//    }
//
//    /**
//     * Encodes octects (using utf-8) into Base64 data
//     *
//     * @param binaryData String containing Hex data
//     * @return string containing decoded data.
//     */
//    public static String encode(String binaryData) {
//        if (binaryData == null)
//            return null;
//
//        byte[] encoded = null;
//         try {
//          encoded = encode(binaryData.getBytes("utf-8"));
//        }
//        catch(UnsupportedEncodingException e) {}
//        finally {
//            return encoded == null ? null : new String(encoded);
//        }
//    }

    /**
     * remove WhiteSpace from MIME containing encoded Base64 data.
     *
     * @param data  the byte array of base64 data (with WS)
     * @return      the byte array of base64 data (without WS)
     */
    protected static byte[] removeWhiteSpace(byte[] data) {
        if (data == null)
            return null;

        // count characters that's not whitespace
        int newSize = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            if (!isWhiteSpace(data[i]))
                newSize++;
        }

        // if no whitespace, just return the input array
        if (newSize == len)
            return data;

        // create the array to return
        byte[] newArray = new byte[newSize];

        int j = 0;
        for (int i = 0; i < len; i++) {
            if (!isWhiteSpace(data[i]))
                newArray[j++] = data[i];
        }
        return newArray;
    }
}
