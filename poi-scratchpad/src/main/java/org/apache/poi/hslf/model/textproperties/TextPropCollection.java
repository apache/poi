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

package org.apache.poi.hslf.model.textproperties;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * For a given run of characters, holds the properties (which could
 *  be paragraph properties or character properties).
 * Used to hold the number of characters affected, the list of active
 *  properties, and the indent level if required.
 */
public class TextPropCollection implements GenericRecord, Duplicatable {
    private static final Logger LOG = LogManager.getLogger(TextPropCollection.class);

    /** All the different kinds of paragraph properties we might handle */
    private static final TextProp[] paragraphTextPropTypes = {
        // TextProp order is according to 2.9.20 TextPFException,
        // bitmask order can be different
        new ParagraphFlagsTextProp(),
        new TextProp(2, 0x80, "bullet.char"),
        new TextProp(2, 0x10, "bullet.font"),
        new TextProp(2, 0x40, "bullet.size"),
        new TextProp(4, 0x20, "bullet.color"),
        new TextAlignmentProp(),
        new TextProp(2, 0x1000, "linespacing"),
        new TextProp(2, 0x2000, "spacebefore"),
        new TextProp(2, 0x4000, "spaceafter"),
        new TextProp(2, 0x100, "text.offset"), // left margin
        // 0x200 - Undefined and MUST be ignored
        new TextProp(2, 0x400, "bullet.offset"), // indent
        new TextProp(2, 0x8000, "defaultTabSize"),
        new HSLFTabStopPropCollection(), // tabstops size is variable!
        new FontAlignmentProp(),
        new WrapFlagsTextProp(),
        new TextProp(2, 0x200000, "textDirection"),
        // 0x400000 MUST be zero and MUST be ignored
        new TextProp(0, 0x800000, "bullet.blip"), // TODO: check size
        new TextProp(0, 0x1000000, "bullet.scheme"), // TODO: check size
        new TextProp(0, 0x2000000, "hasBulletScheme"), // TODO: check size
        // 0xFC000000 MUST be zero and MUST be ignored
    };

    /** All the different kinds of character properties we might handle */
    private static final TextProp[] characterTextPropTypes = new TextProp[] {
        new TextProp(0, 0x100000, "pp10ext"),
        new TextProp(0, 0x1000000, "newAsian.font.index"), // A bit that specifies whether the newEAFontRef field of the TextCFException10 structure that contains this CFMasks exists.
        new TextProp(0, 0x2000000, "cs.font.index"), // A bit that specifies whether the csFontRef field of the TextCFException10 structure that contains this CFMasks exists.
        new TextProp(0, 0x4000000, "pp11ext"), // A bit that specifies whether the pp11ext field of the TextCFException10 structure that contains this CFMasks exists.
        new CharFlagsTextProp(),
        new TextProp(2, 0x10000, "font.index"),
        new TextProp(2, 0x200000, "asian.font.index"),
        new TextProp(2, 0x400000, "ansi.font.index"),
        new TextProp(2, 0x800000, "symbol.font.index"),
        new TextProp(2, 0x20000, "font.size"),
        new TextProp(4, 0x40000, "font.color"),
        new TextProp(2, 0x80000, "superscript")
    };

    public enum TextPropType {
        paragraph, character
    }

    private int charactersCovered;

    // indentLevel is only valid for paragraph collection
    // if it's set to -1, it must be omitted - see 2.9.36 TextMasterStyleLevel
    private short indentLevel;
    private final Map<String,TextProp> textProps = new HashMap<>();
    private int maskSpecial;
    private final TextPropType textPropType;

    /**
     * Create a new collection of text properties (be they paragraph
     *  or character) which will be groked via a subsequent call to
     *  buildTextPropList().
     */
    public TextPropCollection(int charactersCovered, TextPropType textPropType) {
        this.charactersCovered = charactersCovered;
        this.textPropType = textPropType;
    }

    public TextPropCollection(TextPropCollection other) {
        charactersCovered = other.charactersCovered;
        indentLevel = other.indentLevel;
        maskSpecial = other.maskSpecial;
        textPropType = other.textPropType;
        other.textProps.forEach((k,v) -> textProps.put(k, v.copy()));
    }

    public int getSpecialMask() {
        return maskSpecial;
    }

    /** Fetch the number of characters this styling applies to */
    public int getCharactersCovered() {
        return charactersCovered;
    }

    /** Fetch the TextProps that define this styling in the record order */
    public List<TextProp> getTextPropList() {
        List<TextProp> orderedList = new ArrayList<>();
        for (TextProp potProp : getPotentialProperties()) {
            TextProp textProp = textProps.get(potProp.getName());
            if (textProp != null) {
                orderedList.add(textProp);
            }
        }
        return orderedList;
    }

    /** Fetch the TextProp with this name, or null if it isn't present */
    @SuppressWarnings("unchecked")
    public final <T extends TextProp> T findByName(String textPropName) {
        return (T)textProps.get(textPropName);
    }

    @SuppressWarnings("unchecked")
    public final <T extends TextProp> T removeByName(String name) {
        return (T)textProps.remove(name);
    }

    public final TextPropType getTextPropType() {
        return textPropType;
    }

    private TextProp[] getPotentialProperties() {
        return (textPropType == TextPropType.paragraph) ? paragraphTextPropTypes : characterTextPropTypes;
    }

    /**
     * Checks the paragraph or character properties for the given property name.
     * Throws a HSLFException, if the name doesn't belong into this set of properties
     *
     * @param name the property name
     * @return if found, the property template to copy from
     */
    @SuppressWarnings("unchecked")
    private <T extends TextProp> T validatePropName(final String name) {
       for (TextProp tp : getPotentialProperties()) {
            if (tp.getName().equals(name)) {
                return (T)tp;
            }
        }
       String errStr =
           "No TextProp with name " + name + " is defined to add from. " +
           "Character and paragraphs have their own properties/names.";
       throw new HSLFException(errStr);
    }

    /** Add the TextProp with this name to the list */
    @SuppressWarnings("unchecked")
    public final <T extends TextProp> T addWithName(final String name) {
        // Find the base TextProp to base on
        T existing = findByName(name);
        if (existing != null) return existing;

        // Add a copy of this property
        T textProp = (T)validatePropName(name).copy();
        textProps.put(name,textProp);
        return textProp;
    }

    /**
     * Add the property at the correct position. Replaces an existing property with the same name.
     *
     * @param textProp the property to be added
     */
    public final void addProp(TextProp textProp) {
        if (textProp == null) {
            throw new HSLFException("TextProp must not be null");
        }

        String propName = textProp.getName();
        validatePropName(propName);

        textProps.put(propName, textProp);
    }

    /**
     * For an existing set of text properties, build the list of
     *  properties coded for in a given run of properties.
     * @return the number of bytes that were used encoding the properties list
     */
    public int buildTextPropList(int containsField, byte[] data, int dataOffset) {
        int bytesPassed = 0;

        // For each possible entry, see if we match the mask
        // If we do, decode that, save it, and shuffle on
        for(TextProp tp : getPotentialProperties()) {
            // Check there's still data left to read

            // Check if this property is found in the mask
            if((containsField & tp.getMask()) != 0) {
                if(dataOffset+bytesPassed >= data.length) {
                    // Out of data, can't be any more properties to go
                    // remember the mask and return
                    maskSpecial |= tp.getMask();
                    return bytesPassed;
                }

                // Bingo, data contains this property
                TextProp prop = tp.copy();
                int val = 0;
                if (prop instanceof HSLFTabStopPropCollection) {
                    ((HSLFTabStopPropCollection)prop).parseProperty(data, dataOffset+bytesPassed);
                } else if (prop.getSize() == 2) {
                    val = LittleEndian.getShort(data,dataOffset+bytesPassed);
                } else if(prop.getSize() == 4) {
                    val = LittleEndian.getInt(data,dataOffset+bytesPassed);
                } else if (prop.getSize() == 0) {
                    //remember "special" bits.
                    maskSpecial |= tp.getMask();
                    continue;
                }

                if (prop instanceof BitMaskTextProp) {
                    ((BitMaskTextProp)prop).setValueWithMask(val, containsField);
                } else if (!(prop instanceof HSLFTabStopPropCollection)) {
                    prop.setValue(val);
                }
                bytesPassed += prop.getSize();
                addProp(prop);
            }
        }

        // Return how many bytes were used
        return bytesPassed;
    }

    /**
     * Clones the given text properties
     */
    @Override
    public TextPropCollection copy() {
        return new TextPropCollection(this);
    }

    /**
     * Update the size of the text that this set of properties
     *  applies to
     */
    public void updateTextSize(int textSize) {
        charactersCovered = textSize;
    }

       /**
     * Writes out to disk the header, and then all the properties
     */
    public void writeOut(OutputStream o) throws IOException {
        writeOut(o, false);
    }

    /**
     * Writes out to disk the header, and then all the properties
     */
    public void writeOut(OutputStream o, boolean isMasterStyle) throws IOException {
        if (!isMasterStyle) {
            // First goes the number of characters we affect
            // MasterStyles don't have this field
            Record.writeLittleEndian(charactersCovered,o);
        }

        // Then we have the indentLevel field if it's a paragraph collection
        if (textPropType == TextPropType.paragraph && indentLevel > -1) {
            Record.writeLittleEndian(indentLevel, o);
        }

        // Then the mask field
        int mask = maskSpecial;
        for (TextProp textProp : textProps.values()) {
            mask |= textProp.getWriteMask();
        }
        Record.writeLittleEndian(mask,o);

        // Then the contents of all the properties
        for (TextProp textProp : getTextPropList()) {
            int val = textProp.getValue();
            if (textProp instanceof BitMaskTextProp && textProp.getWriteMask() == 0) {
                // don't add empty properties, as they can't be recognized while reading
                continue;
            } else if (textProp.getSize() == 2) {
                Record.writeLittleEndian((short)val,o);
            } else if (textProp.getSize() == 4) {
                Record.writeLittleEndian(val,o);
            } else if (textProp instanceof HSLFTabStopPropCollection) {
                ((HSLFTabStopPropCollection)textProp).writeProperty(o);
            }
        }
    }

    public short getIndentLevel(){
        return indentLevel;
    }

    public void setIndentLevel(short indentLevel) {
        if (textPropType == TextPropType.character) {
            throw new RuntimeException("trying to set an indent on a character collection.");
        }
        this.indentLevel = indentLevel;
    }

    public int hashCode() {
        return Objects.hash(charactersCovered,maskSpecial,indentLevel,textProps);
    }
    /**
     * compares most properties apart of the covered characters length
     */
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;

        TextPropCollection o = (TextPropCollection)other;
        if (o.maskSpecial != this.maskSpecial || o.indentLevel != this.indentLevel) {
            return false;
        }

        return textProps.equals(o.textProps);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("  chars covered: ").append(getCharactersCovered());
        out.append("  special mask flags: 0x").append(HexDump.toHex(getSpecialMask())).append("\n");
        if (textPropType == TextPropType.paragraph) {
            out.append("  indent level: ").append(getIndentLevel()).append("\n");
        }
        for(TextProp p : getTextPropList()) {
            out.append("    ");
            out.append(p.toString());
            out.append("\n");
            if (p instanceof BitMaskTextProp) {
                BitMaskTextProp bm = (BitMaskTextProp)p;
                int i = 0;
                for (String s : bm.getSubPropNames()) {
                    if (bm.getSubPropMatches()[i]) {
                        out.append("          ").append(s).append(" = ").append(bm.getSubValue(i)).append("\n");
                    }
                    i++;
                }
            }
        }

        out.append("  bytes that would be written: \n");

        try {
            UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            writeOut(baos);
            byte[] b = baos.toByteArray();
            out.append(HexDump.dump(b, 0, 0));
        } catch (IOException e ) {
            LOG.atError().withThrowable(e).log("can't dump TextPropCollection");
        }

        return out.toString();
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("charactersCovered", this::getCharactersCovered);
        m.put("indentLevel", this::getIndentLevel);
        textProps.forEach((s,t) -> m.put(s, () -> t));
        m.put("maskSpecial", this::getSpecialMask);
        m.put("textPropType", this::getTextPropType);
        return Collections.unmodifiableMap(m);
    }
}
