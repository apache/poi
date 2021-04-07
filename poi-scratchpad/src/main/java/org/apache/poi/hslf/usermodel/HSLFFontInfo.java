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

package org.apache.poi.hslf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.hslf.record.FontEmbeddedData;
import org.apache.poi.hslf.record.FontEntityAtom;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.Internal;

/**
 * Represents a Font used in a presentation.<p>
 * 
 * In PowerPoint Font is a shared resource and can be shared among text object in the presentation.
 * 
 * @since POI 3.17-beta2
 */
@SuppressWarnings("WeakerAccess")
public class HSLFFontInfo implements FontInfo {

    public enum FontRenderType {
        raster, device, truetype
    }
    
    /** A bit that specifies whether a subset of this font is embedded. */
    private static final BitField FLAGS_EMBED_SUBSETTED      = BitFieldFactory.getInstance(0x01);
    /** Bits that specifies whether the font is a raster,device or truetype font. */
    private static final BitField FLAGS_RENDER_FONTTYPE      = BitFieldFactory.getInstance(0x07);
    /** A bit that specifies whether font substitution logic is not applied for this font. */
    private static final BitField FLAGS_NO_FONT_SUBSTITUTION = BitFieldFactory.getInstance(0x08);
    
    private int index = -1;
    private String typeface = "undefined";
    private FontCharset charset = FontCharset.ANSI;
    private FontRenderType renderType = FontRenderType.truetype;
    private FontFamily family = FontFamily.FF_SWISS;
    private FontPitch pitch = FontPitch.VARIABLE;
    private boolean isSubsetted;
    private boolean isSubstitutable = true;
    private final List<FontEmbeddedData> facets = new ArrayList<>();
    private FontEntityAtom fontEntityAtom;

    /**
     * Creates a new instance of HSLFFontInfo with more or sensible defaults.<p>
     * 
     * If you don't use default fonts (see {@link HSLFFontInfoPredefined}) then the results
     * of the font substitution will be better, if you also specify the other properties.
     * 
     * @param typeface the font name
     */
    public HSLFFontInfo(String typeface){
        setTypeface(typeface);
    }

    /**
     * Creates a new instance of HSLFFontInfo and initialize it from the supplied font atom
     */
    public HSLFFontInfo(FontEntityAtom fontAtom){
        fontEntityAtom = fontAtom;
        setIndex(fontAtom.getFontIndex());
        setTypeface(fontAtom.getFontName());
        setCharset(FontCharset.valueOf(fontAtom.getCharSet()));
        // assumption: the render type is exclusive
        switch (FLAGS_RENDER_FONTTYPE.getValue(fontAtom.getFontType())) {
        case 1:
            setRenderType(FontRenderType.raster);
            break;
        case 2:
            setRenderType(FontRenderType.device);
            break;
        default:
        case 4:
            setRenderType(FontRenderType.truetype);
            break;
        }
        
        byte pitchAndFamily = (byte)fontAtom.getPitchAndFamily();
        setPitch(FontPitch.valueOfPitchFamily(pitchAndFamily));
        setFamily(FontFamily.valueOfPitchFamily(pitchAndFamily));
        setEmbedSubsetted(FLAGS_EMBED_SUBSETTED.isSet(fontAtom.getFontFlags()));
        setFontSubstitutable(!FLAGS_NO_FONT_SUBSTITUTION.isSet(fontAtom.getFontType()));
    }

    public HSLFFontInfo(FontInfo fontInfo) {
        // don't copy font index on copy constructor - it depends on the FontCollection this record is in
        setTypeface(fontInfo.getTypeface());
        setCharset(fontInfo.getCharset());
        setFamily(fontInfo.getFamily());
        setPitch(fontInfo.getPitch());
        if (fontInfo instanceof HSLFFontInfo) {
            HSLFFontInfo hFontInfo = (HSLFFontInfo)fontInfo;
            setRenderType(hFontInfo.getRenderType());
            setEmbedSubsetted(hFontInfo.isEmbedSubsetted());
            setFontSubstitutable(hFontInfo.isFontSubstitutable());
        }
    }
    
    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getTypeface(){
        return typeface;
    }

    @Override
    public void setTypeface(String typeface){
        if (typeface == null || typeface.isEmpty()) {
            throw new IllegalArgumentException("typeface can't be null nor empty");
        }
        this.typeface = typeface;
    }

    @Override
    public void setCharset(FontCharset charset){
        this.charset = (charset == null) ? FontCharset.ANSI : charset;
    }

    @Override
    public FontCharset getCharset(){
        return charset;
    }
    
    @Override
    public FontFamily getFamily() {
        return family;
    }

    @Override
    public void setFamily(FontFamily family) {
        this.family = (family == null) ? FontFamily.FF_SWISS : family;
    }
    
    @Override
    public FontPitch getPitch() {
        return pitch;
    }

    @Override
    public void setPitch(FontPitch pitch) {
        this.pitch = (pitch == null) ? FontPitch.VARIABLE : pitch;
        
    }

    public FontRenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(FontRenderType renderType) {
        this.renderType = (renderType == null) ? FontRenderType.truetype : renderType;
    }

    public boolean isEmbedSubsetted() {
        return isSubsetted;
    }

    public void setEmbedSubsetted(boolean embedSubset) {
        this.isSubsetted = embedSubset;
    }

    public boolean isFontSubstitutable() {
        return this.isSubstitutable;
    }

    public void setFontSubstitutable(boolean isSubstitutable) {
        this.isSubstitutable = isSubstitutable;
    }
    
    public FontEntityAtom createRecord() {
        assert(fontEntityAtom == null);

        FontEntityAtom fnt = new FontEntityAtom();
        fontEntityAtom = fnt;

        fnt.setFontIndex(getIndex() << 4);
        fnt.setFontName(getTypeface());
        fnt.setCharSet(getCharset().getNativeId());
        fnt.setFontFlags((byte)(isEmbedSubsetted() ? 1 : 0));

        int typeFlag;
        switch (renderType) {
        case device:
            typeFlag = FLAGS_RENDER_FONTTYPE.setValue(0, 1);
            break;
        case raster:
            typeFlag = FLAGS_RENDER_FONTTYPE.setValue(0, 2);
            break;
        default:
        case truetype:
            typeFlag = FLAGS_RENDER_FONTTYPE.setValue(0, 4);
            break;
        }
        typeFlag = FLAGS_NO_FONT_SUBSTITUTION.setBoolean(typeFlag, isFontSubstitutable());
        fnt.setFontType(typeFlag);
        
        fnt.setPitchAndFamily(FontPitch.getNativeId(pitch, family));
        return fnt;
    }

    public void addFacet(FontEmbeddedData facet) {
        facets.add(facet);
    }

    @Override
    public List<FontEmbeddedData> getFacets() {
        return facets;
    }

    @Internal
    public FontEntityAtom getFontEntityAtom() {
        return fontEntityAtom;
    }
}
