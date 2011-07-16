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
package org.apache.poi.hwpf.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

public class AbstractWordUtils
{
    static final String EMPTY = "";

    public static final float TWIPS_PER_INCH = 1440.0f;
    public static final int TWIPS_PER_PT = 20;

    static boolean equals( String str1, String str2 )
    {
        return str1 == null ? str2 == null : str1.equals( str2 );
    }

    public static String getBorderType( BorderCode borderCode )
    {
        if ( borderCode == null )
            throw new IllegalArgumentException( "borderCode is null" );

        switch ( borderCode.getBorderType() )
        {
        case 1:
        case 2:
            return "solid";
        case 3:
            return "double";
        case 5:
            return "solid";
        case 6:
            return "dotted";
        case 7:
        case 8:
            return "dashed";
        case 9:
            return "dotted";
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
            return "double";
        case 20:
            return "solid";
        case 21:
            return "double";
        case 22:
            return "dashed";
        case 23:
            return "dashed";
        case 24:
            return "ridge";
        case 25:
            return "grooved";
        default:
            return "solid";
        }
    }
    
    public static String getBorderWidth( BorderCode borderCode )
    {
        int lineWidth = borderCode.getLineWidth();
        int pt = lineWidth / 8;
        int pte = lineWidth - pt * 8;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( pt );
        stringBuilder.append( "." );
        stringBuilder.append( 1000 / 8 * pte );
        stringBuilder.append( "pt" );
        return stringBuilder.toString();
    }

    public static String getBulletText( ListTables listTables,
            Paragraph paragraph, int listId )
    {
        final ListLevel listLevel = listTables.getLevel( listId,
                paragraph.getIlvl() );

        if ( listLevel.getNumberText() == null )
            return EMPTY;

        StringBuffer bulletBuffer = new StringBuffer();
        char[] xst = listLevel.getNumberText().toCharArray();
        for ( char element : xst )
        {
            if ( element < 9 )
            {
                ListLevel numLevel = listTables.getLevel( listId, element );

                int num = numLevel.getStartAt();
                bulletBuffer.append( NumberFormatter.getNumber( num,
                        listLevel.getNumberFormat() ) );

                if ( numLevel == listLevel )
                {
                    numLevel.setStartAt( numLevel.getStartAt() + 1 );
                }

            }
            else
            {
                bulletBuffer.append( element );
            }
        }

        byte follow = listLevel.getTypeOfCharFollowingTheNumber();
        switch ( follow )
        {
        case 0:
            bulletBuffer.append( "\t" );
            break;
        case 1:
            bulletBuffer.append( " " );
            break;
        default:
            break;
        }

        return bulletBuffer.toString();
    }

    public static String getColor( int ico )
    {
        switch ( ico )
        {
        case 1:
            return "black";
        case 2:
            return "blue";
        case 3:
            return "cyan";
        case 4:
            return "green";
        case 5:
            return "magenta";
        case 6:
            return "red";
        case 7:
            return "yellow";
        case 8:
            return "white";
        case 9:
            return "darkblue";
        case 10:
            return "darkcyan";
        case 11:
            return "darkgreen";
        case 12:
            return "darkmagenta";
        case 13:
            return "darkred";
        case 14:
            return "darkyellow";
        case 15:
            return "darkgray";
        case 16:
            return "lightgray";
        default:
            return "black";
        }
    }

    public static String getJustification( int js )
    {
        switch ( js )
        {
        case 0:
            return "start";
        case 1:
            return "center";
        case 2:
            return "end";
        case 3:
        case 4:
            return "justify";
        case 5:
            return "center";
        case 6:
            return "left";
        case 7:
            return "start";
        case 8:
            return "end";
        case 9:
            return "justify";
        }
        return "";
    }

    public static String getListItemNumberLabel( int number, int format )
    {

        if ( format != 0 )
            System.err.println( "NYI: toListItemNumberLabel(): " + format );

        return String.valueOf( number );
    }

    public static SectionProperties getSectionProperties( Section section )
    {
        try
        {
            Field field = Section.class.getDeclaredField( "_props" );
            field.setAccessible( true );
            return (SectionProperties) field.get( section );
        }
        catch ( Exception exc )
        {
            throw new Error( exc );
        }
    }

    static boolean isEmpty( String str )
    {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty( String str )
    {
        return !isEmpty( str );
    }

    public static HWPFDocumentCore loadDoc( File docFile ) throws IOException
    {
        final FileInputStream istream = new FileInputStream( docFile );
        try
        {
            return loadDoc( istream );
        }
        finally
        {
            IOUtils.closeQuietly( istream );
        }
    }

    public static HWPFDocumentCore loadDoc( InputStream inputStream )
            throws IOException
    {
        final POIFSFileSystem poifsFileSystem = HWPFDocumentCore
                .verifyAndBuildPOIFS( inputStream );
        try
        {
            return new HWPFDocument( poifsFileSystem );
        }
        catch ( OldWordFileFormatException exc )
        {
            return new HWPFOldDocument( poifsFileSystem );
        }
    }

    public static TableIterator newTableIterator( Range range, int level )
    {
        try
        {
            Constructor<TableIterator> constructor = TableIterator.class
                    .getDeclaredConstructor( Range.class, int.class );
            constructor.setAccessible( true );
            return constructor.newInstance( range, Integer.valueOf( level ) );
        }
        catch ( Exception exc )
        {
            throw new Error( exc );
        }
    }

    static String substringBeforeLast( String str, String separator )
    {
        if ( isEmpty( str ) || isEmpty( separator ) )
        {
            return str;
        }
        int pos = str.lastIndexOf( separator );
        if ( pos == -1 )
        {
            return str;
        }
        return str.substring( 0, pos );
    }

}
