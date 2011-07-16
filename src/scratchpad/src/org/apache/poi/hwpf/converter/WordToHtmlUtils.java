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

import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;

public class WordToHtmlUtils extends AbstractWordUtils
{
    public static void addBold( final boolean bold, StringBuilder style )
    {
        style.append( "font-weight: " + ( bold ? "bold" : "normal" ) + ";" );
    }

    public static void addBorder( BorderCode borderCode, String where,
            StringBuilder style )
    {
        if ( borderCode == null || borderCode.getBorderType() == 0 )
            return;

        if ( isEmpty( where ) )
        {
            style.append( "border-style: " + getBorderType( borderCode ) + "; " );
            style.append( "border-color: " + getColor( borderCode.getColor() )
                    + "; " );
            style.append( "border-width: " + getBorderWidth( borderCode )
                    + "; " );
        }
        else
        {
            style.append( "border-" + where + "-style: "
                    + getBorderType( borderCode ) + "; " );
            style.append( "border-" + where + "-color: "
                    + getColor( borderCode.getColor() ) + "; " );
            style.append( "border-" + where + "-width: "
                    + getBorderWidth( borderCode ) + "; " );
        }
    }

    public static void addCharactersProperties(
            final CharacterRun characterRun, StringBuilder style )
    {
        final CharacterProperties clonedProperties = characterRun
                .cloneProperties();

        addBorder( clonedProperties.getBrc(), EMPTY, style );

        if ( characterRun.isCapitalized() )
        {
            style.append( "text-transform: uppercase; " );
        }
        if ( characterRun.isHighlighted() )
        {
            style.append( "background-color: "
                    + getColor( clonedProperties.getIcoHighlight() ) + "; " );
        }
        if ( characterRun.isStrikeThrough() )
        {
            style.append( "text-decoration: line-through; " );
        }
        if ( characterRun.isShadowed() )
        {
            style.append( "text-shadow: " + characterRun.getFontSize() / 24
                    + "pt; " );
        }
        if ( characterRun.isSmallCaps() )
        {
            style.append( "font-variant: small-caps; " );
        }
        if ( characterRun.getSubSuperScriptIndex() == 1 )
        {
            style.append( "baseline-shift: super; " );
            style.append( "font-size: smaller; " );
        }
        if ( characterRun.getSubSuperScriptIndex() == 2 )
        {
            style.append( "baseline-shift: sub; " );
            style.append( "font-size: smaller; " );
        }
        if ( characterRun.getUnderlineCode() > 0 )
        {
            style.append( "text-decoration: underline; " );
        }
        if ( characterRun.isVanished() )
        {
            style.append( "visibility: hidden; " );
        }
    }

    public static void addFontFamily( final String fontFamily,
            StringBuilder style )
    {
        if ( isEmpty( fontFamily ) )
            return;

        style.append( "font-family: " + fontFamily + "; " );
    }

    public static void addFontSize( final int fontSize, StringBuilder style )
    {
        style.append( "font-size: " + fontSize + "pt; " );
    }

    public static void addIndent( Paragraph paragraph, StringBuilder style )
    {
        addIndent( style, "text-indent", paragraph.getFirstLineIndent() );
        addIndent( style, "start-indent", paragraph.getIndentFromLeft() );
        addIndent( style, "end-indent", paragraph.getIndentFromRight() );
        addIndent( style, "space-before", paragraph.getSpacingBefore() );
        addIndent( style, "space-after", paragraph.getSpacingAfter() );
    }

    private static void addIndent( StringBuilder style, final String cssName,
            final int twipsValue )
    {
        if ( twipsValue == 0 )
            return;

        style.append( cssName + ": " + ( twipsValue / TWIPS_PER_PT ) + "pt; " );
    }

    public static void addJustification( Paragraph paragraph,
            final StringBuilder style )
    {
        String justification = getJustification( paragraph.getJustification() );
        if ( isNotEmpty( justification ) )
            style.append( "text-align: " + justification + "; " );
    }

    public static void addParagraphProperties( Paragraph paragraph,
            StringBuilder style )
    {
        addIndent( paragraph, style );
        addJustification( paragraph, style );

        addBorder( paragraph.getBottomBorder(), "bottom", style );
        addBorder( paragraph.getLeftBorder(), "left", style );
        addBorder( paragraph.getRightBorder(), "right", style );
        addBorder( paragraph.getTopBorder(), "top", style );

        if ( paragraph.pageBreakBefore() )
        {
            style.append( "break-before: page; " );
        }

        style.append( "hyphenate: "
                + ( paragraph.isAutoHyphenated() ? "auto" : "none" ) + "; " );

        if ( paragraph.keepOnPage() )
        {
            style.append( "keep-together.within-page: always; " );
        }

        if ( paragraph.keepWithNext() )
        {
            style.append( "keep-with-next.within-page: always; " );
        }
    }

    public static void addTableCellProperties( TableRow tableRow,
            TableCell tableCell, boolean toppest, boolean bottomest,
            boolean leftest, boolean rightest, StringBuilder style )
    {
        style.append( "width: " + ( tableCell.getWidth() / TWIPS_PER_INCH )
                + "in; " );
        style.append( "padding-start: "
                + ( tableRow.getGapHalf() / TWIPS_PER_INCH ) + "in; " );
        style.append( "padding-end: "
                + ( tableRow.getGapHalf() / TWIPS_PER_INCH ) + "in; " );

        BorderCode top = tableCell.getBrcTop() != null
                && tableCell.getBrcTop().getBorderType() != 0 ? tableCell
                .getBrcTop() : toppest ? tableRow.getTopBorder() : tableRow
                .getHorizontalBorder();
        BorderCode bottom = tableCell.getBrcBottom() != null
                && tableCell.getBrcBottom().getBorderType() != 0 ? tableCell
                .getBrcBottom() : bottomest ? tableRow.getBottomBorder()
                : tableRow.getHorizontalBorder();

        BorderCode left = tableCell.getBrcLeft() != null
                && tableCell.getBrcLeft().getBorderType() != 0 ? tableCell
                .getBrcLeft() : leftest ? tableRow.getLeftBorder() : tableRow
                .getVerticalBorder();
        BorderCode right = tableCell.getBrcRight() != null
                && tableCell.getBrcRight().getBorderType() != 0 ? tableCell
                .getBrcRight() : rightest ? tableRow.getRightBorder()
                : tableRow.getVerticalBorder();

        addBorder( bottom, "bottom", style );
        addBorder( left, "left", style );
        addBorder( right, "right", style );
        addBorder( top, "top", style );
    }

    public static void addTableRowProperties( TableRow tableRow,
            StringBuilder style )
    {
        if ( tableRow.getRowHeight() > 0 )
        {
            style.append( "height: "
                    + ( tableRow.getRowHeight() / TWIPS_PER_INCH ) + "in; " );
        }
        if ( !tableRow.cantSplit() )
        {
            style.append( "keep-together: always; " );
        }
    }

}
