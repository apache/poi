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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Beta
public class AbstractWordUtils
{
    static final String EMPTY = "";

    private static final POILogger logger = POILogFactory
            .getLogger( AbstractWordUtils.class );

    public static final float TWIPS_PER_INCH = 1440.0f;
    public static final int TWIPS_PER_PT = 20;

    /**
     * Creates array of all possible cell edges. In HTML (and FO) cells from
     * different rows and same column should have same width, otherwise spanning
     * shall be used.
     *
     * @param table
     *            table to build cell edges array from
     * @return array of cell edges (including leftest one) in twips
     */
    static int[] buildTableCellEdgesArray( Table table )
    {
        Set<Integer> edges = new TreeSet<>();

        for ( int r = 0; r < table.numRows(); r++ )
        {
            TableRow tableRow = table.getRow( r );
            for ( int c = 0; c < tableRow.numCells(); c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                edges.add(tableCell.getLeftEdge());
                edges.add(tableCell.getLeftEdge() + tableCell.getWidth());
            }
        }

        Integer[] sorted = edges.toArray(new Integer[0]);
        int[] result = new int[sorted.length];
        for ( int i = 0; i < sorted.length; i++ )
        {
            result[i] = sorted[i];
        }

        return result;
    }

    static boolean canBeMerged( Node node1, Node node2, String requiredTagName )
    {
        if ( node1.getNodeType() != Node.ELEMENT_NODE
                || node2.getNodeType() != Node.ELEMENT_NODE )
            return false;

        Element element1 = (Element) node1;
        Element element2 = (Element) node2;

        if ( !Objects.equals( requiredTagName, element1.getTagName() )
                || !Objects.equals( requiredTagName, element2.getTagName() ) )
            return false;

        NamedNodeMap attributes1 = element1.getAttributes();
        NamedNodeMap attributes2 = element2.getAttributes();

        if ( attributes1.getLength() != attributes2.getLength() )
            return false;

        for ( int i = 0; i < attributes1.getLength(); i++ )
        {
            final Attr attr1 = (Attr) attributes1.item( i );
            final Attr attr2;
            if ( isNotEmpty( attr1.getNamespaceURI() ) )
                attr2 = (Attr) attributes2.getNamedItemNS(
                        attr1.getNamespaceURI(), attr1.getLocalName() );
            else
                attr2 = (Attr) attributes2.getNamedItem( attr1.getName() );

            if ( attr2 == null
                    || !Objects.equals( attr1.getTextContent(), attr2.getTextContent() ) )
                return false;
        }

        return true;
    }

    static void compactChildNodesR( Element parentElement, String childTagName )
    {
        NodeList childNodes = parentElement.getChildNodes();
        for ( int i = 0; i < childNodes.getLength() - 1; i++ )
        {
            Node child1 = childNodes.item( i );
            Node child2 = childNodes.item( i + 1 );
            if ( !AbstractWordUtils.canBeMerged( child1, child2, childTagName ) )
                continue;

            // merge
            while ( child2.getChildNodes().getLength() > 0 )
                child1.appendChild( child2.getFirstChild() );
            child2.getParentNode().removeChild( child2 );
            i--;
        }

        childNodes = parentElement.getChildNodes();
        for ( int i = 0; i < childNodes.getLength() - 1; i++ )
        {
            Node child = childNodes.item( i );
            if ( child instanceof Element )
            {
                compactChildNodesR( (Element) child, childTagName );
            }
        }
    }

    public static String getBorderType( BorderCode borderCode )
    {
        if ( borderCode == null )
            throw new IllegalArgumentException( "borderCode is null" );

        switch ( borderCode.getBorderType() )
        {
            case 3:
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
            case 21:
                return "double";
            case 6:
            case 9:
                return "dotted";
            case 7:
            case 8:
            case 22:
            case 23:
                return "dashed";
            case 24:
                return "ridge";
            case 25:
                return "grooved";
                case 5:
            case 1:
            case 2:
            case 20:
            default:
                return "solid";
        }
    }

    public static String getBorderWidth( BorderCode borderCode )
    {
        int lineWidth = borderCode.getLineWidth();
        int pt = lineWidth / 8;
        int pte = lineWidth - pt * 8;

        return pt + "." + 1000 / 8 * pte + "pt";
    }

    public static class NumberingState
    {

        private final Map<String, Integer> levels = new HashMap<>();

    }

    public static String getBulletText( NumberingState numberingState,
            HWPFList list, char level )
    {
        StringBuilder bulletBuffer = new StringBuilder();
        char[] xst = list.getNumberText( level ).toCharArray();
        for ( char element : xst )
        {
            if ( element < 9 )
            {
                int lsid = list.getLsid();
                final String key = lsid + "#" + ( (int) element );
                int num;

                if ( !list.isStartAtOverriden( element )
                        && numberingState.levels.containsKey( key ) )
                {
                    num = numberingState.levels.get( key );
                    if ( level == element )
                    {
                        num++;
                        numberingState.levels.put( key, num );
                    }
                }
                else
                {
                    num = list.getStartAt( element );
                    numberingState.levels.put( key, num );
                }

                if ( level == element )
                {
                    // cleaning states of nested levels to reset numbering
                    for ( int i = element + 1; i < 9; i++ )
                    {
                        final String childKey = lsid + "#" + i;
                        numberingState.levels.remove( childKey );
                    }
                }

                bulletBuffer.append( NumberFormatter.getNumber( num,
                        list.getNumberFormat( level ) ) );
            }
            else
            {
                bulletBuffer.append( element );
            }
        }

        byte follow = list.getTypeOfCharFollowingTheNumber( level );
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
        switch ( ico ) {
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
            case 1:
            default:
                return "black";
        }
    }

    public static String getOpacity( int argbValue )
    {
        int opacity = (int) ( ( argbValue & 0xFF000000L) >>> 24 );
        if ( opacity == 0 || opacity == 0xFF )
            return ".0";

        return "" + ( opacity / (float) 0xFF );
    }

    public static String getColor24( int argbValue )
    {
        if ( argbValue == -1 )
            throw new IllegalArgumentException( "This colorref is empty" );

        int bgrValue = argbValue & 0x00FFFFFF;
        int rgbValue = ( bgrValue & 0x0000FF ) << 16 | ( bgrValue & 0x00FF00 )
                | ( bgrValue & 0xFF0000 ) >> 16;

        // http://www.w3.org/TR/REC-html40/types.html#h-6.5
        switch ( rgbValue )
        {
        case 0xFFFFFF:
            return "white";
        case 0xC0C0C0:
            return "silver";
        case 0x808080:
            return "gray";
        case 0x000000:
            return "black";
        case 0xFF0000:
            return "red";
        case 0x800000:
            return "maroon";
        case 0xFFFF00:
            return "yellow";
        case 0x808000:
            return "olive";
        case 0x00FF00:
            return "lime";
        case 0x008000:
            return "green";
        case 0x00FFFF:
            return "aqua";
        case 0x008080:
            return "teal";
        case 0x0000FF:
            return "blue";
        case 0x000080:
            return "navy";
        case 0xFF00FF:
            return "fuchsia";
        case 0x800080:
            return "purple";
        }

        StringBuilder result = new StringBuilder( "#" );
        String hex = Integer.toHexString( rgbValue );
        for ( int i = hex.length(); i < 6; i++ )
        {
            result.append( '0' );
        }
        result.append( hex );
        return result.toString();
    }

    public static String getJustification( int js )
    {
        switch ( js )
        {
        case 0:
        case 7:
            return "start";
        case 1:
        case 5:
            return "center";
        case 2:
        case 8:
            return "end";
        case 3:
        case 4:
        case 9:
            return "justify";
        case 6:
            return "left";
        }
        return "";
    }

    public static String getLanguage( int languageCode )
    {
        switch ( languageCode )
        {
        case 1024:
            return EMPTY;
        case 1033:
            return "en-us";
        case 1049:
            return "ru-ru";
        case 2057:
            return "en-uk";
        default:
            logger.log( POILogger.WARN, "Uknown or unmapped language code: ", languageCode);
            return EMPTY;
        }
    }

    public static String getListItemNumberLabel( int number, int format )
    {

        if ( format != 0 )
        	logger.log( POILogger.INFO, "NYI: toListItemNumberLabel(): ", format );

        return String.valueOf( number );
    }

    static boolean isEmpty( String str )
    {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty( String str )
    {
        return !isEmpty( str );
    }

    public static HWPFDocumentCore loadDoc( final DirectoryNode root )
            throws IOException
    {
        try
        {
            return new HWPFDocument( root );
        }
        catch ( OldWordFileFormatException exc )
        {
            return new HWPFOldDocument( root );
        }
    }

    public static HWPFDocumentCore loadDoc( File docFile ) throws IOException
    {
        try (FileInputStream istream = new FileInputStream(docFile)) {
            return loadDoc(istream);
        }
    }

    public static HWPFDocumentCore loadDoc( InputStream inputStream )
            throws IOException
    {
        return loadDoc( HWPFDocumentCore.verifyAndBuildPOIFS( inputStream ) );
    }

    public static HWPFDocumentCore loadDoc(
            final POIFSFileSystem poifsFileSystem ) throws IOException
    {
        return loadDoc( poifsFileSystem.getRoot() );
    }

}
