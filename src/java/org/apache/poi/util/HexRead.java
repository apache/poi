package org.apache.poi.util;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class HexRead
{
    public static byte[] readTestData( String filename )
            throws IOException
    {
        File file = new File( filename );
        FileInputStream stream = new FileInputStream( file );
        int characterCount = 0;
        byte b = (byte) 0;
        List bytes = new ArrayList();
        boolean done = false;

        while ( !done )
        {
            int count = stream.read();

            switch ( count )
            {

                case '#':
                    readToEOL(stream);
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    b <<= 4;
                    b += (byte) ( count - '0' );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( new Byte( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;

                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    b <<= 4;
                    b += (byte) ( count + 10 - 'A' );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( new Byte( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;

                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    b <<= 4;
                    b += (byte) ( count + 10 - 'a' );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( new Byte( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;

                case -1:
                    done = true;
                    break;

                default :
                    break;
            }
        }
        stream.close();
        Byte[] polished = (Byte[]) bytes.toArray( new Byte[0] );
        byte[] rval = new byte[polished.length];

        for ( int j = 0; j < polished.length; j++ )
        {
            rval[j] = polished[j].byteValue();
        }
        return rval;
    }

    static private void readToEOL( InputStream stream ) throws IOException
    {
        int c = stream.read();
        while ( c != -1 && c != '\n' && c != '\r')
        {
            c = stream.read();
        }
    }


}
