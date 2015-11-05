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

package org.apache.poi.wp.usermodel;

/**
 * This class represents a run of text that share common properties.
 */
public interface CharacterRun {
    boolean isBold();
    void setBold(boolean bold);

    boolean isItalic();
    void setItalic(boolean italic);

    boolean isSmallCaps();
    void setSmallCaps(boolean smallCaps);

    boolean isCapitalized();
    void setCapitalized(boolean caps);

    boolean isStrikeThrough();
    void setStrikeThrough(boolean strike);
    boolean isDoubleStrikeThrough();
    void setDoubleStrikethrough(boolean dstrike);

    boolean isShadowed();
    void setShadow(boolean shadow);

    boolean isEmbossed();
    void setEmbossed(boolean emboss);

    boolean isImprinted();
    void setImprinted(boolean imprint);

    int getFontSize();
    void setFontSize(int halfPoints);

    int getCharacterSpacing();
    void setCharacterSpacing(int twips);

    int getKerning();
    void setKerning(int kern);
    
    boolean isHighlighted();
    
    // HWPF has colour indexes, XWPF has a highlight enum with the colours in
//    byte getHighlightedColor();
//    void setHighlighted(byte color);
    
    // HWPF has colour indexes, XWPF colour names
//  int getColor();
//  void setColor(int color);


    /**
     * Gets the fonts which shall be used to display the text contents of
     * this run. Specifies a font which shall be used to format all "normal"
     * characters in the run
     *
     * @return a string representing the font
     */
    String getFontName();
    
    /**
     * @return The text of the run, including any tabs/spaces/etc
     */
    String text();

    // HWPF uses indexes, XWPF special
//    int getUnderlineCode();
//    void setUnderlineCode(int kul);

    // HWPF uses indexes, XWPF special vertical alignments
//    short getSubSuperScriptIndex();
//    void setSubSuperScriptIndex(short iss);

    // TODO Review these, and add to XWPFRun if possible
/*
    boolean isFldVanished();
    void setFldVanish(boolean fldVanish);
    
    boolean isOutlined();
    void setOutline(boolean outlined);
    
    boolean isVanished();
    void setVanished(boolean vanish);

    boolean isMarkedDeleted();
    void markDeleted(boolean mark);

    boolean isMarkedInserted();
    void markInserted(boolean mark);
*/
}
