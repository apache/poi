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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontHeader;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfoPredefined;
import org.apache.poi.util.IOUtils;

/**
 * {@code FontCollection} ia a container that holds information
 * about all the fonts in the presentation.
 */

@SuppressWarnings("WeakerAccess")
public final class FontCollection extends RecordContainer {
    private final Map<Integer,HSLFFontInfo> fonts = new LinkedHashMap<>();
    private byte[] _header;

    /* package */ FontCollection(byte[] source, int start, int len) {
        _header = Arrays.copyOfRange(source, start, start+8);

        _children = Record.findChildRecords(source,start+8,len-8);

        for (org.apache.poi.hslf.record.Record r : _children){
            if(r instanceof FontEntityAtom) {
                HSLFFontInfo fi = new HSLFFontInfo((FontEntityAtom) r);
                fonts.put(fi.getIndex(), fi);
            } else if (r instanceof FontEmbeddedData) {
                FontEmbeddedData fed = (FontEmbeddedData)r;
                FontHeader fontHeader = fed.getFontHeader();
                HSLFFontInfo fi = addFont(fontHeader);
                fi.addFacet(fed);
            } else {
                LOG.atWarn().log("FontCollection child wasn't a FontEntityAtom, was {}", r.getClass().getSimpleName());
            }
        }
    }

    /**
     * Return the type, which is 2005
     */
    @Override
    public long getRecordType() {
        return RecordTypes.FontCollection.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        writeOut(_header[0],_header[1],getRecordType(),_children,out);
    }

    /**
     * Add font with the given FontInfo configuration to the font collection.
     * The returned FontInfo contains the HSLF specific details and the collection
     * uniquely contains fonts based on their typeface, i.e. calling the method with FontInfo
     * objects having the same name results in the same HSLFFontInfo reference.
     *
     * @param fontInfo the FontInfo configuration, can be a instance of {@link HSLFFontInfo},
     *      {@link HSLFFontInfoPredefined} or a custom implementation
     * @return the register HSLFFontInfo object
     */
    public HSLFFontInfo addFont(FontInfo fontInfo) {
        HSLFFontInfo fi = getFontInfo(fontInfo.getTypeface(), fontInfo.getCharset());
        if (fi != null) {
            return fi;
        }

        fi = new HSLFFontInfo(fontInfo);
        fi.setIndex(fonts.size());
        fonts.put(fi.getIndex(), fi);

        FontEntityAtom fnt = fi.createRecord();

        // Append new child to the end
        appendChildRecord(fnt);

        // the added font is the last in the list
        return fi;
    }

    public HSLFFontInfo addFont(InputStream fontData) throws IOException {
        FontHeader fontHeader = new FontHeader();
        InputStream is = fontHeader.bufferInit(fontData);

        HSLFFontInfo fi = addFont(fontHeader);

        // always overwrite the font info properties when a font data given
        // as the font info properties are assigned generically when only a typeface is given
        FontEntityAtom fea = fi.getFontEntityAtom();
        assert (fea != null);
        fea.setCharSet(fontHeader.getCharsetByte());
        fea.setPitchAndFamily(FontPitch.getNativeId(fontHeader.getPitch(),fontHeader.getFamily()));

        // always activate subsetting
        fea.setFontFlags(1);
        // true type font and no font substitution
        fea.setFontType(12);

        Record after = fea;

        final int insertIdx = getFacetIndex(fontHeader.isItalic(), fontHeader.isBold());

        FontEmbeddedData newChild = null;
        for (FontEmbeddedData fed : fi.getFacets()) {
            FontHeader fh = fed.getFontHeader();
            final int curIdx = getFacetIndex(fh.isItalic(), fh.isBold());

            if (curIdx == insertIdx) {
                newChild = fed;
                break;
            } else if (curIdx > insertIdx) {
                // the new facet needs to be inserted before the current facet
                break;
            }

            after = fed;
        }

        if (newChild == null) {
            newChild = new FontEmbeddedData();
            addChildAfter(newChild, after);
            fi.addFacet(newChild);
        }

        newChild.setFontData(IOUtils.toByteArray(is));
        return fi;
    }

    private static int getFacetIndex(boolean isItalic, boolean isBold) {
        return (isItalic ? 2 : 0) | (isBold ? 1 : 0);
    }


    /**
     * Lookup a FontInfo object by its typeface
     *
     * @param typeface the full font name
     *
     * @return the HSLFFontInfo for the given name or {@code null} if not found
     */
    public HSLFFontInfo getFontInfo(String typeface) {
        return getFontInfo(typeface, null);
    }

    /**
     * Lookup a FontInfo object by its typeface
     *
     * @param typeface the full font name
     * @param charset the charset
     *
     * @return the HSLFFontInfo for the given name or {@code null} if not found
     */
    public HSLFFontInfo getFontInfo(String typeface, FontCharset charset) {
        return fonts.values().stream().filter(findFont(typeface, charset)).findFirst().orElse(null);
    }

    private static Predicate<HSLFFontInfo> findFont(String typeface, FontCharset charset) {
        return (fi) -> typeface.equals(fi.getTypeface()) && (charset == null || charset.equals(fi.getCharset()));
    }

    /**
     * Lookup a FontInfo object by its internal font index
     *
     * @param index the internal font index
     *
     * @return the HSLFFontInfo for the given index or {@code null} if not found
     */
    public HSLFFontInfo getFontInfo(int index) {
        for (HSLFFontInfo fi : fonts.values()) {
            if (fi.getIndex() == index) {
                return fi;
            }
        }
        return null;
    }

    /**
     * @return the number of registered fonts
     */
    public int getNumberOfFonts() {
        return fonts.size();
    }

    public List<HSLFFontInfo> getFonts() {
        return new ArrayList<>(fonts.values());
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
