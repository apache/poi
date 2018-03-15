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

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Utilities to read hex from files.
 * TODO - move to test packages
 */
public class HexRead {
    /**
     * This method reads hex data from a filename and returns a byte array.
     * The file may contain line comments that are preceded with a # symbol.
     *
     * @param filename  The filename to read
     * @return The bytes read from the file.
     * @throws IOException If there was a problem while reading the file.
     */
    public static byte[] readData( String filename ) throws IOException {
        File file = new File( filename );
        InputStream stream = new FileInputStream( file );
        try {
            return readData( stream, -1 );
        } finally {
            stream.close();
        }
    }

    /**
     * Same as readData(String) except that this method allows you to specify sections within
     * a file.  Sections are referenced using section headers in the form:
     * <pre>
     *  [sectioname]
     * </pre>
     *
     * @see #readData(String)
     */
    public static byte[] readData(InputStream stream, String section ) throws IOException {
        try {
            StringBuffer sectionText = new StringBuffer();
            boolean inSection = false;
            int c = stream.read();
            while ( c != -1 ) {
                switch ( c ) {
                    case '[':
                        inSection = true;
                        break;
                    case '\n':
                    case '\r':
                        inSection = false;
                        sectionText = new StringBuffer();
                        break;
                    case ']':
                        inSection = false;
                        if ( sectionText.toString().equals( section ) ) return readData( stream, '[' );
                        sectionText = new StringBuffer();
                        break;
                    default:
                        if ( inSection ) sectionText.append( (char) c );
                }
                c = stream.read();
            }
        } finally {
            stream.close();
        }

        throw new IOException( "Section '" + section + "' not found" );
    }

    public static byte[] readData( String filename, String section ) throws IOException {
        return readData(new FileInputStream( filename ), section);
    }

    @SuppressWarnings("fallthrough")
    static public byte[] readData( InputStream stream, int eofChar )
            throws IOException
    {
        int characterCount = 0;
        byte b = (byte) 0;
        List<Byte> bytes = new ArrayList<>();
        final char a = 'a' - 10;
        final char A = 'A' - 10;
        while ( true ) {
            int count = stream.read();
            int digitValue = -1;
            if ( '0' <= count && count <= '9' ) {
                digitValue = count - '0';
            } else if ( 'A' <= count && count <= 'F' ) {
                digitValue = count - A;
            } else if ( 'a' <= count && count <= 'f' ) {
                digitValue = count - a;
            } else if ( '#' == count ) {
                readToEOL( stream );
            } else if ( -1 == count || eofChar == count ) {
                break;
            }
            // else: ignore the character

            if (digitValue != -1) {
                b <<= 4;
                b += (byte) digitValue;
                characterCount++;
                if ( characterCount == 2 ) {
                    bytes.add( Byte.valueOf( b ) );
                    characterCount = 0;
                    b = (byte) 0;
                }
            }
        }
        Byte[] polished = bytes.toArray(new Byte[bytes.size()]);
        byte[] rval = new byte[polished.length];
        for ( int j = 0; j < polished.length; j++ ) {
            rval[j] = polished[j].byteValue();
        }
        return rval;
    }

    static public byte[] readFromString(String data) {
        try {
            return readData(new ByteArrayInputStream( data.getBytes(StringUtil.UTF8) ), -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private void readToEOL( InputStream stream ) throws IOException {
        int c = stream.read();
        while ( c != -1 && c != '\n' && c != '\r' ) {
            c = stream.read();
        }
    }
}
