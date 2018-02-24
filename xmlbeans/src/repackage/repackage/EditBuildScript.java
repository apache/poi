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

package repackage;

import java.io.*;

public class EditBuildScript
{
    //
    // usgae: edit buildfile token new-value
    //

    public static void main ( String[] args )
        throws Exception
    {
        if (args.length != 3)
            throw new IllegalArgumentException( "Wrong number of arguments" );

        args[ 0 ] = args[ 0 ].replace( '/', File.separatorChar );

        File buildFile = new File( args[ 0 ] );

        StringBuffer sb = readFile( buildFile );

        String tokenStr = "<property name=\"" + args[ 1 ] + "\" value=\"";
                         
        int i = sb.indexOf( tokenStr );

        if (i < 0)
            throw new IllegalArgumentException( "Can't find token: " + tokenStr );

        int j = i + tokenStr.length();

        while ( sb.charAt( j ) != '"' )
            j++;

        sb.replace( i + tokenStr.length(), j, args[ 2 ] );

        writeFile( buildFile, sb );
    }
    
    static StringBuffer readFile ( File f )
        throws IOException
    {
        InputStream in = new FileInputStream( f );
        Reader r = new InputStreamReader( in );
        StringWriter w = new StringWriter();

        copy( r, w );

        w.close();
        r.close();
        in.close();

        return w.getBuffer();
    }
    
    static void writeFile ( File f, StringBuffer chars )
        throws IOException
    {
        OutputStream out = new FileOutputStream( f );
        Writer w = new OutputStreamWriter( out );
        Reader r = new StringReader( chars.toString() );

        copy( r, w );

        r.close();
        w.close();
        out.close();
    }
    
    static void copy ( Reader r, Writer w ) throws IOException
    {
        char[] buffer = new char [ 1024 * 16 ];

        for ( ; ; )
        {
            int n = r.read( buffer, 0, buffer.length );

            if (n < 0)
                break;

            w.write( buffer, 0, n );
        }
    }
}
