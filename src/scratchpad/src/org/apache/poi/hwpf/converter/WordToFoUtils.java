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
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.w3c.dom.Element;

public class WordToFoUtils extends AbstractWordUtils
{
    static void compactInlines( Element blockElement )
    {
        compactChildNodes( blockElement, "fo:inline" );
    }

    public static void setBold( final Element element, final boolean bold )
    {
        element.setAttribute( "font-weight", bold ? "bold" : "normal" );
    }

    public static void setBorder( Element element, BorderCode borderCode,
            String where )
    {
        if ( element == null )
            throw new IllegalArgumentException( "element is null" );

        if ( borderCode == null || borderCode.getBorderType() == 0 )
            return;

        if ( isEmpty( where ) )
        {
            element.setAttribute( "border-style", getBorderType( borderCode ) );
            element.setAttribute( "border-color",
                    getColor( borderCode.getColor() ) );
            element.setAttribute( "border-width", getBorderWidth( borderCode ) );
        }
        else
        {
            element.setAttribute( "border-" + where + "-style",
                    getBorderType( borderCode ) );
            element.setAttribute( "border-" + where + "-color",
                    getColor( borderCode.getColor() ) );
            element.setAttribute( "border-" + where + "-width",
                    getBorderWidth( borderCode ) );
        }
    }

    public static void setCharactersProperties(
            final CharacterRun characterRun, final Element inline )
    {
        StringBuilder textDecorations = new StringBuilder();

        setBorder( inline, characterRun.getBorder(), EMPTY );

        if ( characterRun.isCapitalized() )
        {
            inline.setAttribute( "text-transform", "uppercase" );
        }
        if ( characterRun.isHighlighted() )
        {
            inline.setAttribute( "background-color",
                    getColor( characterRun.getHighlightedColor() ) );
        }
        if ( characterRun.isStrikeThrough() )
        {
            if ( textDecorations.length() > 0 )
                textDecorations.append( " " );
            textDecorations.append( "line-through" );
        }
        if ( characterRun.isShadowed() )
        {
            inline.setAttribute( "text-shadow", characterRun.getFontSize() / 24
                    + "pt" );
        }
        if ( characterRun.isSmallCaps() )
        {
            inline.setAttribute( "font-variant", "small-caps" );
        }
        if ( characterRun.getSubSuperScriptIndex() == 1 )
        {
            inline.setAttribute( "baseline-shift", "super" );
            inline.setAttribute( "font-size", "smaller" );
        }
        if ( characterRun.getSubSuperScriptIndex() == 2 )
        {
            inline.setAttribute( "baseline-shift", "sub" );
            inline.setAttribute( "font-size", "smaller" );
        }
        if ( characterRun.getUnderlineCode() > 0 )
        {
            if ( textDecorations.length() > 0 )
                textDecorations.append( " " );
            textDecorations.append( "underline" );
        }
        if ( characterRun.isVanished() )
        {
            inline.setAttribute( "visibility", "hidden" );
        }
        if ( textDecorations.length() > 0 )
        {
            inline.setAttribute( "text-decoration", textDecorations.toString() );
        }
    }

    public static void setFontFamily( final Element element,
            final String fontFamily )
    {
        if ( isEmpty( fontFamily ) )
            return;

        element.setAttribute( "font-family", fontFamily );
    }

    public static void setFontSize( final Element element, final int fontSize )
    {
        element.setAttribute( "font-size", String.valueOf( fontSize ) );
    }

    public static void setIndent( Paragraph paragraph, Element block )
    {
        if ( paragraph.getFirstLineIndent() != 0 )
        {
            block.setAttribute(
                    "text-indent",
                    String.valueOf( paragraph.getFirstLineIndent()
                            / TWIPS_PER_PT )
                            + "pt" );
        }
        if ( paragraph.getIndentFromLeft() != 0 )
        {
            block.setAttribute(
                    "start-indent",
                    String.valueOf( paragraph.getIndentFromLeft()
                            / TWIPS_PER_PT )
                            + "pt" );
        }
        if ( paragraph.getIndentFromRight() != 0 )
        {
            block.setAttribute(
                    "end-indent",
                    String.valueOf( paragraph.getIndentFromRight()
                            / TWIPS_PER_PT )
                            + "pt" );
        }
        if ( paragraph.getSpacingBefore() != 0 )
        {
            block.setAttribute(
                    "space-before",
                    String.valueOf( paragraph.getSpacingBefore() / TWIPS_PER_PT )
                            + "pt" );
        }
        if ( paragraph.getSpacingAfter() != 0 )
        {
            block.setAttribute( "space-after",
                    String.valueOf( paragraph.getSpacingAfter() / TWIPS_PER_PT )
                            + "pt" );
        }
    }

    public static void setItalic( final Element element, final boolean italic )
    {
        element.setAttribute( "font-style", italic ? "italic" : "normal" );
    }

    public static void setJustification( Paragraph paragraph,
            final Element element )
    {
        String justification = getJustification( paragraph.getJustification() );
        if ( isNotEmpty( justification ) )
            element.setAttribute( "text-align", justification );
    }

    public static void setParagraphProperties( Paragraph paragraph,
            Element block )
    {
        setIndent( paragraph, block );
        setJustification( paragraph, block );

        setBorder( block, paragraph.getBottomBorder(), "bottom" );
        setBorder( block, paragraph.getLeftBorder(), "left" );
        setBorder( block, paragraph.getRightBorder(), "right" );
        setBorder( block, paragraph.getTopBorder(), "top" );

        if ( paragraph.pageBreakBefore() )
        {
            block.setAttribute( "break-before", "page" );
        }

        block.setAttribute( "hyphenate",
                String.valueOf( paragraph.isAutoHyphenated() ) );

        if ( paragraph.keepOnPage() )
        {
            block.setAttribute( "keep-together.within-page", "always" );
        }

        if ( paragraph.keepWithNext() )
        {
            block.setAttribute( "keep-with-next.within-page", "always" );
        }

        block.setAttribute( "linefeed-treatment", "preserve" );
        block.setAttribute( "white-space-collapse", "false" );
    }

    public static void setPictureProperties( Picture picture,
            Element graphicElement )
    {
        final int aspectRatioX = picture.getAspectRatioX();
        final int aspectRatioY = picture.getAspectRatioY();

        if ( aspectRatioX > 0 )
        {
            graphicElement
                    .setAttribute( "content-width", ( ( picture.getDxaGoal()
                            * aspectRatioX / 100 ) / TWIPS_PER_PT )
                            + "pt" );
        }
        else
            graphicElement.setAttribute( "content-width",
                    ( picture.getDxaGoal() / TWIPS_PER_PT ) + "pt" );

        if ( aspectRatioY > 0 )
            graphicElement
                    .setAttribute( "content-height", ( ( picture.getDyaGoal()
                            * aspectRatioY / 100 ) / TWIPS_PER_PT )
                            + "pt" );
        else
            graphicElement.setAttribute( "content-height",
                    ( picture.getDyaGoal() / TWIPS_PER_PT ) + "pt" );

        if ( aspectRatioX <= 0 || aspectRatioY <= 0 )
        {
            graphicElement.setAttribute( "scaling", "uniform" );
        }
        else
        {
            graphicElement.setAttribute( "scaling", "non-uniform" );
        }

        graphicElement.setAttribute( "vertical-align", "text-bottom" );

        if ( picture.getDyaCropTop() != 0 || picture.getDxaCropRight() != 0
                || picture.getDyaCropBottom() != 0
                || picture.getDxaCropLeft() != 0 )
        {
            int rectTop = picture.getDyaCropTop() / TWIPS_PER_PT;
            int rectRight = picture.getDxaCropRight() / TWIPS_PER_PT;
            int rectBottom = picture.getDyaCropBottom() / TWIPS_PER_PT;
            int rectLeft = picture.getDxaCropLeft() / TWIPS_PER_PT;
            graphicElement.setAttribute( "clip", "rect(" + rectTop + "pt, "
                    + rectRight + "pt, " + rectBottom + "pt, " + rectLeft
                    + "pt)" );
            graphicElement.setAttribute( "oveerflow", "hidden" );
        }
    }

    public static void setTableCellProperties( TableRow tableRow,
            TableCell tableCell, Element element, boolean toppest,
            boolean bottomest, boolean leftest, boolean rightest )
    {
        element.setAttribute( "width", ( tableCell.getWidth() / TWIPS_PER_INCH )
                + "in" );
        element.setAttribute( "padding-start",
                ( tableRow.getGapHalf() / TWIPS_PER_INCH ) + "in" );
        element.setAttribute( "padding-end",
                ( tableRow.getGapHalf() / TWIPS_PER_INCH ) + "in" );

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

        setBorder( element, bottom, "bottom" );
        setBorder( element, left, "left" );
        setBorder( element, right, "right" );
        setBorder( element, top, "top" );
    }

    public static void setTableRowProperties( TableRow tableRow,
            Element tableRowElement )
    {
        if ( tableRow.getRowHeight() > 0 )
        {
            tableRowElement.setAttribute( "height",
                    ( tableRow.getRowHeight() / TWIPS_PER_INCH ) + "in" );
        }
        if ( !tableRow.cantSplit() )
        {
            tableRowElement.setAttribute( "keep-together.within-column",
                    "always" );
        }
    }

}
