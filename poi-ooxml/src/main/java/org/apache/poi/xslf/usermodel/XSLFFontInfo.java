/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontFacet;
import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontHeader;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.presentationml.x2006.main.CTEmbeddedFontDataId;
import org.openxmlformats.schemas.presentationml.x2006.main.CTEmbeddedFontList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTEmbeddedFontListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPresentation;

@SuppressWarnings("WeakerAccess")
public class XSLFFontInfo implements FontInfo {
    final XMLSlideShow ppt;
    final String typeface;
    final CTEmbeddedFontListEntry fontListEntry;

    public XSLFFontInfo(XMLSlideShow ppt, String typeface) {
        this.ppt = ppt;
        this.typeface = typeface;

        final CTPresentation pres = ppt.getCTPresentation();
        CTEmbeddedFontList fontList = pres.isSetEmbeddedFontLst()
            ? pres.getEmbeddedFontLst() : pres.addNewEmbeddedFontLst();

        for (CTEmbeddedFontListEntry fe : fontList.getEmbeddedFontArray()) {
            if (typeface.equalsIgnoreCase(fe.getFont().getTypeface())) {
                fontListEntry = fe;
                return;
            }
        }

        fontListEntry = fontList.addNewEmbeddedFont();
        fontListEntry.addNewFont().setTypeface(typeface);
    }

    public XSLFFontInfo(XMLSlideShow ppt, CTEmbeddedFontListEntry fontListEntry) {
        this.ppt = ppt;
        this.typeface = fontListEntry.getFont().getTypeface();
        this.fontListEntry = fontListEntry;
    }

    @Override
    public String getTypeface() {
        return getFont().getTypeface();
    }

    @Override
    public void setTypeface(String typeface) {
        getFont().setTypeface(typeface);
    }

    @Override
    public FontCharset getCharset() {
        return FontCharset.valueOf(getFont().getCharset());
    }

    @Override
    public void setCharset(FontCharset charset) {
        getFont().setCharset((byte)charset.getNativeId());
    }

    @Override
    public FontFamily getFamily() {
        return FontFamily.valueOfPitchFamily(getFont().getPitchFamily());
    }

    @Override
    public void setFamily(FontFamily family) {
        byte pitchAndFamily = getFont().getPitchFamily();
        FontPitch pitch = FontPitch.valueOfPitchFamily(pitchAndFamily);
        getFont().setPitchFamily(FontPitch.getNativeId(pitch, family));
    }

    @Override
    public FontPitch getPitch() {
        return FontPitch.valueOfPitchFamily(getFont().getPitchFamily());
    }

    @Override
    public void setPitch(FontPitch pitch) {
        byte pitchAndFamily = getFont().getPitchFamily();
        FontFamily family = FontFamily.valueOfPitchFamily(pitchAndFamily);
        getFont().setPitchFamily(FontPitch.getNativeId(pitch, family));
    }

    @Override
    public byte[] getPanose() {
        return getFont().getPanose();
    }

    @Override
    public List<FontFacet> getFacets() {
        List<FontFacet> facetList = new ArrayList<>();
        if (fontListEntry.isSetRegular()) {
            facetList.add(new XSLFFontFacet((fontListEntry.getRegular())));
        }
        if (fontListEntry.isSetItalic()) {
            facetList.add(new XSLFFontFacet((fontListEntry.getItalic())));
        }
        if (fontListEntry.isSetBold()) {
            facetList.add(new XSLFFontFacet((fontListEntry.getBold())));
        }
        if (fontListEntry.isSetBoldItalic()) {
            facetList.add(new XSLFFontFacet((fontListEntry.getBoldItalic())));
        }
        return facetList;
    }

    public FontFacet addFacet(InputStream fontData) throws IOException {
        FontHeader header = new FontHeader();
        InputStream is = header.bufferInit(fontData);

        final CTPresentation pres = ppt.getCTPresentation();
        pres.setEmbedTrueTypeFonts(true);
        pres.setSaveSubsetFonts(true);

        final CTEmbeddedFontDataId dataId;
        final int style =
                (header.getWeight() > 400 ? Font.BOLD : Font.PLAIN) |
                        (header.isItalic() ? Font.ITALIC : Font.PLAIN);
        switch (style) {
            case Font.PLAIN:
                dataId = fontListEntry.isSetRegular()
                    ? fontListEntry.getRegular() : fontListEntry.addNewRegular();
                break;
            case Font.BOLD:
                dataId = fontListEntry.isSetBold()
                    ? fontListEntry.getBold() : fontListEntry.addNewBold();
                break;
            case Font.ITALIC:
                dataId = fontListEntry.isSetItalic()
                    ? fontListEntry.getItalic() : fontListEntry.addNewItalic();
                break;
            default:
                dataId = fontListEntry.isSetBoldItalic()
                    ? fontListEntry.getBoldItalic() : fontListEntry.addNewBoldItalic();
                break;
        }

        XSLFFontFacet facet = new XSLFFontFacet(dataId);
        facet.setFontData(is);
        return facet;
    }

    private final class XSLFFontFacet implements FontFacet {
        private final CTEmbeddedFontDataId fontEntry;
        private final FontHeader header = new FontHeader();

        private XSLFFontFacet(CTEmbeddedFontDataId fontEntry) {
            this.fontEntry = fontEntry;
        }

        @Override
        public int getWeight() {
            init();
            return header.getWeight();
        }

        @Override
        public boolean isItalic() {
            init();
            return header.isItalic();
        }

        @Override
        public XSLFFontData getFontData() {
            return ppt.getRelationPartById(fontEntry.getId()).getDocumentPart();
        }

        void setFontData(InputStream is) throws IOException {
            final XSLFRelation fntRel = XSLFRelation.FONT;
            final String relId = fontEntry.getId();
            final XSLFFontData fntData;
            if (relId == null || relId.isEmpty()) {
                final int fntDataIdx;
                try {
                    fntDataIdx = ppt.getPackage().getUnusedPartIndex(fntRel.getDefaultFileName());
                } catch (InvalidFormatException e) {
                    throw new RuntimeException(e);
                }

                POIXMLDocumentPart.RelationPart rp = ppt.createRelationship(fntRel, XSLFFactory.getInstance(), fntDataIdx, false);
                fntData = rp.getDocumentPart();
                fontEntry.setId(rp.getRelationship().getId());
            } else {
                fntData = (XSLFFontData)ppt.getRelationById(relId);
            }

            assert (fntData != null);
            try (OutputStream os = fntData.getOutputStream()) {
                IOUtils.copy(is, os);
            }
        }

        private void init() {
            if (header.getFamilyName() == null) {
                try (InputStream is = getFontData().getInputStream()) {
                    byte[] buf = IOUtils.toByteArray(is, 1000);
                    header.init(buf, 0, buf.length);
                } catch (IOException e) {
                    // TODO: better exception class
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private CTTextFont getFont() {
        return fontListEntry.getFont();
    }



    /**
     * Adds or updates a (MTX-) font
     * @param ppt the slideshow which will contain the font
     * @param fontStream the (MTX) font data as stream
     * @return a font data object
     * @throws IOException if the font data can't be stored
     *
     * @since POI 4.1.0
     */
    public static XSLFFontInfo addFontToSlideShow(XMLSlideShow ppt, InputStream fontStream)
    throws IOException {
        FontHeader header = new FontHeader();
        InputStream is = header.bufferInit(fontStream);

        XSLFFontInfo fontInfo = new XSLFFontInfo(ppt, header.getFamilyName());
        fontInfo.addFacet(is);
        return fontInfo;
    }

    /**
     * Return all registered fonts
     * @param ppt the slideshow containing the fonts
     * @return the list of registered fonts
     */
    public static List<XSLFFontInfo> getFonts(XMLSlideShow ppt) {
        final CTPresentation pres = ppt.getCTPresentation();

        //noinspection deprecation
        return pres.isSetEmbeddedFontLst()
            ? Stream.of(pres.getEmbeddedFontLst().getEmbeddedFontArray())
                .map(fe -> new XSLFFontInfo(ppt, fe)).collect(Collectors.toList())
            : Collections.emptyList();
    }

}
