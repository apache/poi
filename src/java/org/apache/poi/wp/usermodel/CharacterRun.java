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
    public boolean isBold();
    public void setBold(boolean bold);

    public boolean isItalic();
    public void setItalic(boolean italic);

    public boolean isSmallCaps();
    public void setSmallCaps(boolean smallCaps);

    public boolean isCapitalized();
    public void setCapitalized(boolean caps);

    public boolean isStrikeThrough();
    public void setStrikeThrough(boolean strike);
    public boolean isDoubleStrikeThrough();
    public void setDoubleStrikethrough(boolean dstrike);

    public boolean isShadowed();
    public void setShadow(boolean shadow);

    public boolean isEmbossed();
    public void setEmbossed(boolean emboss);

    public boolean isImprinted();
    public void setImprinted(boolean imprint);

    public int getFontSize();
    public void setFontSize(int halfPoints);

    public int getCharacterSpacing();
    public void setCharacterSpacing(int twips);

    public int getKerning();
    public void setKerning(int kern);

    public String getFontName();
    
    /**
     * @return The text of the run, including any tabs/spaces/etc
     */
    public String text();

    // HWPF uses indexes, XWPF special
//    public int getUnderlineCode();
//    public void setUnderlineCode(int kul);

    // HWPF uses indexes, XWPF special vertical alignments
//    public short getSubSuperScriptIndex();
//    public void setSubSuperScriptIndex(short iss);

    // HWPF uses indexes, XWPF special vertical alignments
//    public int getVerticalOffset();
//    public void setVerticalOffset(int hpsPos);

    // HWPF has colour indexes, XWPF colour names
//    public int getColor();
//    public void setColor(int color);

    // TODO Review these, and add to XWPFRun if possible
/*
    public boolean isFldVanished();
    public void setFldVanish(boolean fldVanish);
    
    public boolean isOutlined();
    public void setOutline(boolean outlined);
    
    public boolean isVanished();
    public void setVanished(boolean vanish);

    public boolean isMarkedDeleted();
    public void markDeleted(boolean mark);

    public boolean isMarkedInserted();
    public void markInserted(boolean mark);
*/
}
