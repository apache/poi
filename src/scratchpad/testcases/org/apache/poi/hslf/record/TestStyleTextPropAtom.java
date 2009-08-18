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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.util.HexDump;

/**
 * Tests that StyleTextPropAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestStyleTextPropAtom extends TestCase {
    /** From a real file: a paragraph with 4 different styles */
    private static final byte[] data_a = new byte[] {
      0, 0, 0xA1-256, 0x0F, 0x2A, 0, 0, 0,
      0x36, 00, 00, 00, // paragraph is 54 long
      00, 00,           // (paragraph reserved field)
      00, 00, 00, 00,   // it doesn't have any styles
      0x15, 00, 00, 00, // first char run is 21 long
      00, 00, 00, 00,   // it doesn't have any styles
      0x11, 00, 00, 00, // second char run is 17 long
      00, 00, 0x04, 00, // font.color only
      00, 00, 00, 0x05, // blue
      0x10, 00, 00, 00, // third char run is 16 long
      00, 00, 0x04, 00, // font.color only
      0xFF-256, 0x33, 00, 0xFE-256 // red
    };
    private static final int data_a_text_len = 0x36-1;

    /**
     * From a real file: 4 paragraphs with text in 4 different styles:
     * left aligned+bold (30)
     * centre aligned+italic+blue (28)
     * right aligned+red (25)
     * left aligned+underlined+larger font size (96)
     * left aligned+underlined+larger font size+red (1)
     */
    private static final byte[] data_b = new byte[] {
        0, 0, 0xA1-256, 0x0F, 0x80-256, 0, 0, 0,
        0x1E, 00, 00, 00,     // paragraph is 30 long
        00, 00,               // paragraph reserved field
        00, 0x18, 00, 00,     // mask is 0x1800
        00, 00,               // left aligned
        0x50, 00,             // line spacing 80
        0x1C, 00, 00, 00,     // paragprah is 28 long
        00, 00,               // paragraph reserved field
        00, 0x10, 00, 00,     // mask is 0x1000
        0x50, 00,             // line spacing 80
        0x19, 00, 00, 00,     // paragraph is 25 long
        00, 00,               // paragraph reserved field
        00, 0x18, 00, 00,     // mask is 0x1800
        02, 00,               // right aligned
        0x50, 00,             // line spacing 80
        0x61, 00, 00, 00,     // paragraph is 97 long
        00, 00,               // paragraph reserved field
        00, 0x18, 00, 00,     // mask is 0x1800
        00, 00,               // left aligned
        0x50, 00,             // line spacing 80

        0x1E, 00, 00, 00,     // character run is 30 long
        01, 00, 02, 00,       // mask is 0x020001
        01, 00,               // char flags 0x0001 = bold
        0x14, 00,             // font size 20
        0x1C, 00, 00, 00,     // character run is 28 long
        02, 00, 06, 00,       // mask is 0x060002
        02, 00,               // char flags 0x0002 = italic
        0x14, 00,             // font size 20
        00, 00, 00, 05,       // colour blue
        0x19, 00, 00, 00,     // character run is 25 long
        00, 00, 06, 00,       // char flags 0x060000
        0x14, 00,             // font size 20
        0xFF-256, 0x33, 00, 0xFE-256, // colour red
        0x60, 00, 00, 00,     // character run is 96 long
        04, 00, 03, 00,       // mask is 0x030004
        04, 00,               // char flags 0x0004 = underlined
        01, 00,               // font index is 1
        0x18, 00,             // font size 24

        01, 00, 00, 00,       // character run is 1 long
        04, 00, 07, 00,       // mask is 0x070004
        04, 00,               // char flags 0x0004 = underlined
        01, 00,               // font index is 1
        0x18, 00,             // font size 24
        0xFF-256, 0x33, 00, 0xFE-256 // colour red
    };
    private static final int data_b_text_len = 0xB3;

    /**
     * From a real file. Has a mask with more bits
     *  set than it actually has data for. Shouldn't do,
     *  but some real files do :(
     */
    private static final byte[] data_c = new byte[] {
        0, 0, -95, 15, 62, 0, 0, 0,
        123, 0, 0, 0, 0, 0, 48, 8,
        10, 0, 1, 0, 0, 0, 0, 0,
        1, 0, 2, 0, 1, 0, 0, 0,
        0, 0, 48, 0, 10, 0, 1, 0,
        0, 0, 0, 0, 2, 0, 123, 0,
        0, 0, 0, 0, 3, 0, 1, 0,
        28, 0, 1, 0, 0, 0, 0, 0,
        3, 0, 1, 0, 24, 0
    };
    private int data_c_text_len = 123-1;

    /**
     * From a real file supplied for Bug 40143 by tales@great.ufc.br
     */
    private static final byte[] data_d = {
        0x00, 0x00, 0xA1-256, 0x0F, 0x1E, 0x00, 0x00, 0x00, //header
        (byte)0xA0, 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x08 , 0x00 , 0x00 ,
        0x01 , 0x00, (byte)0xA0 , 0x00 , 0x00 , 0x00 , 0x01 , 0x00 , 0x63 , 0x00 ,
        0x01 , 0x00, 0x01 , 0x00 , 0x00, 0x00 , 0x01 , 0x00 , 0x14 , 0x00
    };
    private static final int data_d_text_len = 0xA0-1;

    public void testRecordType() {
        StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        StyleTextPropAtom stpc = new StyleTextPropAtom(data_c,0,data_c.length);
        assertEquals(4001l, stpa.getRecordType());
        assertEquals(4001l, stpb.getRecordType());
        assertEquals(4001l, stpc.getRecordType());
    }


    public void testCharacterStyleCounts() {
        StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

        // Set for the appropriate text sizes
        stpa.setParentTextSize(data_a_text_len);
        stpb.setParentTextSize(data_b_text_len);

        // In case A, there is a single styling of the characters
        assertEquals(3, stpa.getCharacterStyles().size());
        // In case B, there are 5 different stylings
        assertEquals(5, stpb.getCharacterStyles().size());
    }

    public void testParagraphStyleCounts() {
        StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

        // Set for the appropriate text sizes
        stpa.setParentTextSize(data_a_text_len);
        stpb.setParentTextSize(data_b_text_len);

        // In case A, all has the same spacing and alignment
        assertEquals(1, stpa.getParagraphStyles().size());
        // In case B, all 4 sets have different alignments
        assertEquals(4, stpb.getParagraphStyles().size());
    }


    public void testCharacterStyleLengths() {
        StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

        // Set for the appropriate text sizes
        stpa.setParentTextSize(data_a_text_len);
        stpb.setParentTextSize(data_b_text_len);

        // 54 chars, 21 + 17 + 16
        LinkedList a_ch_l = stpa.getCharacterStyles();
        TextPropCollection a_ch_1 = (TextPropCollection)a_ch_l.get(0);
        TextPropCollection a_ch_2 = (TextPropCollection)a_ch_l.get(1);
        TextPropCollection a_ch_3 = (TextPropCollection)a_ch_l.get(2);
        assertEquals(21, a_ch_1.getCharactersCovered());
        assertEquals(17, a_ch_2.getCharactersCovered());
        assertEquals(16, a_ch_3.getCharactersCovered());

        // 179 chars, 30 + 28 + 25
        LinkedList b_ch_l = stpb.getCharacterStyles();
        TextPropCollection b_ch_1 = (TextPropCollection)b_ch_l.get(0);
        TextPropCollection b_ch_2 = (TextPropCollection)b_ch_l.get(1);
        TextPropCollection b_ch_3 = (TextPropCollection)b_ch_l.get(2);
        TextPropCollection b_ch_4 = (TextPropCollection)b_ch_l.get(3);
        assertEquals(30, b_ch_1.getCharactersCovered());
        assertEquals(28, b_ch_2.getCharactersCovered());
        assertEquals(25, b_ch_3.getCharactersCovered());
        assertEquals(96, b_ch_4.getCharactersCovered());
    }


    public void testCharacterPropOrdering() {
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        stpb.setParentTextSize(data_b_text_len);

        LinkedList b_ch_l = stpb.getCharacterStyles();
        TextPropCollection b_ch_1 = (TextPropCollection)b_ch_l.get(0);
        TextPropCollection b_ch_2 = (TextPropCollection)b_ch_l.get(1);
        TextPropCollection b_ch_3 = (TextPropCollection)b_ch_l.get(2);
        TextPropCollection b_ch_4 = (TextPropCollection)b_ch_l.get(3);

        // In first set, we get a CharFlagsTextProp and a font.size
        assertEquals(2,b_ch_1.getTextPropList().size());
        TextProp tp_1_1 = (TextProp)b_ch_1.getTextPropList().get(0);
        TextProp tp_1_2 = (TextProp)b_ch_1.getTextPropList().get(1);
        assertEquals(true, tp_1_1 instanceof CharFlagsTextProp);
        assertEquals("font.size", tp_1_2.getName());
        assertEquals(20, tp_1_2.getValue());

        // In second set, we get a CharFlagsTextProp and a font.size and a font.color
        assertEquals(3,b_ch_2.getTextPropList().size());
        TextProp tp_2_1 = (TextProp)b_ch_2.getTextPropList().get(0);
        TextProp tp_2_2 = (TextProp)b_ch_2.getTextPropList().get(1);
        TextProp tp_2_3 = (TextProp)b_ch_2.getTextPropList().get(2);
        assertEquals(true, tp_2_1 instanceof CharFlagsTextProp);
        assertEquals("font.size", tp_2_2.getName());
        assertEquals("font.color", tp_2_3.getName());
        assertEquals(20, tp_2_2.getValue());

        // In third set, it's just a font.size and a font.color
        assertEquals(2,b_ch_3.getTextPropList().size());
        TextProp tp_3_1 = (TextProp)b_ch_3.getTextPropList().get(0);
        TextProp tp_3_2 = (TextProp)b_ch_3.getTextPropList().get(1);
        assertEquals("font.size", tp_3_1.getName());
        assertEquals("font.color", tp_3_2.getName());
        assertEquals(20, tp_3_1.getValue());

        // In fourth set, we get a CharFlagsTextProp and a font.index and a font.size
        assertEquals(3,b_ch_4.getTextPropList().size());
        TextProp tp_4_1 = (TextProp)b_ch_4.getTextPropList().get(0);
        TextProp tp_4_2 = (TextProp)b_ch_4.getTextPropList().get(1);
        TextProp tp_4_3 = (TextProp)b_ch_4.getTextPropList().get(2);
        assertEquals(true, tp_4_1 instanceof CharFlagsTextProp);
        assertEquals("font.index", tp_4_2.getName());
        assertEquals("font.size", tp_4_3.getName());
        assertEquals(24, tp_4_3.getValue());
    }

    public void testParagraphProps() {
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        stpb.setParentTextSize(data_b_text_len);

        LinkedList b_p_l = stpb.getParagraphStyles();
        TextPropCollection b_p_1 = (TextPropCollection)b_p_l.get(0);
        TextPropCollection b_p_2 = (TextPropCollection)b_p_l.get(1);
        TextPropCollection b_p_3 = (TextPropCollection)b_p_l.get(2);
        TextPropCollection b_p_4 = (TextPropCollection)b_p_l.get(3);

        // 1st is left aligned + normal line spacing
        assertEquals(2,b_p_1.getTextPropList().size());
        TextProp tp_1_1 = (TextProp)b_p_1.getTextPropList().get(0);
        TextProp tp_1_2 = (TextProp)b_p_1.getTextPropList().get(1);
        assertEquals("alignment", tp_1_1.getName());
        assertEquals("linespacing", tp_1_2.getName());
        assertEquals(0, tp_1_1.getValue());
        assertEquals(80, tp_1_2.getValue());

        // 2nd is centre aligned (default) + normal line spacing
        assertEquals(1,b_p_2.getTextPropList().size());
        TextProp tp_2_1 = (TextProp)b_p_2.getTextPropList().get(0);
        assertEquals("linespacing", tp_2_1.getName());
        assertEquals(80, tp_2_1.getValue());

        // 3rd is right aligned + normal line spacing
        assertEquals(2,b_p_3.getTextPropList().size());
        TextProp tp_3_1 = (TextProp)b_p_3.getTextPropList().get(0);
        TextProp tp_3_2 = (TextProp)b_p_3.getTextPropList().get(1);
        assertEquals("alignment", tp_3_1.getName());
        assertEquals("linespacing", tp_3_2.getName());
        assertEquals(2, tp_3_1.getValue());
        assertEquals(80, tp_3_2.getValue());

        // 4st is left aligned + normal line spacing (despite differing font)
        assertEquals(2,b_p_4.getTextPropList().size());
        TextProp tp_4_1 = (TextProp)b_p_4.getTextPropList().get(0);
        TextProp tp_4_2 = (TextProp)b_p_4.getTextPropList().get(1);
        assertEquals("alignment", tp_4_1.getName());
        assertEquals("linespacing", tp_4_2.getName());
        assertEquals(0, tp_4_1.getValue());
        assertEquals(80, tp_4_2.getValue());
    }

    public void testCharacterProps() {
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        stpb.setParentTextSize(data_b_text_len);

        LinkedList b_ch_l = stpb.getCharacterStyles();
        TextPropCollection b_ch_1 = (TextPropCollection)b_ch_l.get(0);
        TextPropCollection b_ch_2 = (TextPropCollection)b_ch_l.get(1);
        TextPropCollection b_ch_3 = (TextPropCollection)b_ch_l.get(2);
        TextPropCollection b_ch_4 = (TextPropCollection)b_ch_l.get(3);

        // 1st is bold
        CharFlagsTextProp cf_1_1 = (CharFlagsTextProp)b_ch_1.getTextPropList().get(0);
        assertEquals(true,cf_1_1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_1_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_2_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.RELIEF_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.RESET_NUMBERING_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.SHADOW_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.STRIKETHROUGH_IDX));
        assertEquals(false,cf_1_1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        // 2nd is italic
        CharFlagsTextProp cf_2_1 = (CharFlagsTextProp)b_ch_2.getTextPropList().get(0);
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(true,cf_2_1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_1_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_2_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.RELIEF_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.RESET_NUMBERING_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.SHADOW_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.STRIKETHROUGH_IDX));
        assertEquals(false,cf_2_1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        // 3rd is normal, so lacks a CharFlagsTextProp
        assertFalse(b_ch_3.getTextPropList().get(0) instanceof CharFlagsTextProp);

        // 4th is underlined
        CharFlagsTextProp cf_4_1 = (CharFlagsTextProp)b_ch_4.getTextPropList().get(0);
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_1_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_2_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.RELIEF_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.RESET_NUMBERING_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.SHADOW_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.STRIKETHROUGH_IDX));
        assertEquals(true,cf_4_1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        // The value for this should be 4
        assertEquals(0x0004, cf_4_1.getValue());

        // Now make the 4th bold, italic and not underlined
        cf_4_1.setSubValue(true, CharFlagsTextProp.BOLD_IDX);
        cf_4_1.setSubValue(true, CharFlagsTextProp.ITALIC_IDX);
        cf_4_1.setSubValue(false, CharFlagsTextProp.UNDERLINE_IDX);

        assertEquals(true,cf_4_1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(true,cf_4_1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_1_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.ENABLE_NUMBERING_2_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.RELIEF_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.RESET_NUMBERING_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.SHADOW_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.STRIKETHROUGH_IDX));
        assertEquals(false,cf_4_1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        // The value should now be 3
        assertEquals(0x0003, cf_4_1.getValue());
    }

    public void testFindAddTextProp() {
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        stpb.setParentTextSize(data_b_text_len);

        LinkedList b_p_l = stpb.getParagraphStyles();
        TextPropCollection b_p_1 = (TextPropCollection)b_p_l.get(0);
        TextPropCollection b_p_2 = (TextPropCollection)b_p_l.get(1);
        TextPropCollection b_p_3 = (TextPropCollection)b_p_l.get(2);
        TextPropCollection b_p_4 = (TextPropCollection)b_p_l.get(3);

        LinkedList b_ch_l = stpb.getCharacterStyles();
        TextPropCollection b_ch_1 = (TextPropCollection)b_ch_l.get(0);
        TextPropCollection b_ch_2 = (TextPropCollection)b_ch_l.get(1);
        TextPropCollection b_ch_3 = (TextPropCollection)b_ch_l.get(2);
        TextPropCollection b_ch_4 = (TextPropCollection)b_ch_l.get(3);

        // CharFlagsTextProp: 3 doesn't have, 4 does
        assertNull(b_ch_3.findByName("char_flags"));
        assertNotNull(b_ch_4.findByName("char_flags"));

        // Now add in on 3, should go to front
        assertEquals(2, b_ch_3.getTextPropList().size());
        TextProp new_cftp = b_ch_3.addWithName("char_flags");
        assertEquals(3, b_ch_3.getTextPropList().size());
        assertEquals(new_cftp, b_ch_3.getTextPropList().get(0));

        // alignment: 1 does have, 2 doesn't
        assertNotNull(b_p_1.findByName("alignment"));
        assertNull(b_p_2.findByName("alignment"));

        // Now add in on 2, should go to the front
        assertEquals(1, b_p_2.getTextPropList().size());
        TextProp new_al = b_p_2.addWithName("alignment");
        assertEquals(2, b_p_2.getTextPropList().size());
        assertEquals(new_al, b_p_2.getTextPropList().get(0));

        // This should go at the end
        TextProp new_sa = b_p_2.addWithName("spaceafter");
        assertEquals(3, b_p_2.getTextPropList().size());
        assertEquals(new_sa, b_p_2.getTextPropList().get(2));

        // Check we get an error with a made up one
        try {
            b_p_2.addWithName("madeUpOne");
            fail();
        } catch(IllegalArgumentException e) {
            // Good, as expected
        }
    }

    /**
     * Try to recreate an existing StyleTextPropAtom (a) from the empty
     *  constructor, and setting the required properties
     */
    public void testCreateAFromScatch() throws Exception {
        // Start with an empty one
        StyleTextPropAtom stpa = new StyleTextPropAtom(54);

        // Don't need to touch the paragraph styles
        // Add two more character styles
        LinkedList cs = stpa.getCharacterStyles();

        // First char style is boring, and 21 long
        TextPropCollection tpca = (TextPropCollection)cs.get(0);
        tpca.updateTextSize(21);

        // Second char style is coloured, 00 00 00 05, and 17 long
        TextPropCollection tpcb = stpa.addCharacterTextPropCollection(17);
        TextProp tpb = tpcb.addWithName("font.color");
        tpb.setValue(0x05000000);

        // Third char style is coloured, FF 33 00 FE, and 16 long
        TextPropCollection tpcc = stpa.addCharacterTextPropCollection(16);
        TextProp tpc = tpcc.addWithName("font.color");
        tpc.setValue(0xFE0033FF);

        // Should now be the same as data_a
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stpa.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertEquals(data_a.length, b.length);
        for(int i=0; i<data_a.length; i++) {
            assertEquals(data_a[i],b[i]);
        }
    }

    /**
     * Try to recreate an existing StyleTextPropAtom (b) from the empty
     *  constructor, and setting the required properties
     */
    public void testCreateBFromScatch() throws Exception {
        // Start with an empty one
        StyleTextPropAtom stpa = new StyleTextPropAtom(data_b_text_len);


        // Need 4 paragraph styles
        LinkedList ps = stpa.getParagraphStyles();

        // First is 30 long, left aligned, normal spacing
        TextPropCollection tppa = (TextPropCollection)ps.get(0);
        tppa.updateTextSize(30);

        TextProp tp = tppa.addWithName("alignment");
        tp.setValue(0);
        tp = tppa.addWithName("linespacing");
        tp.setValue(80);

        // Second is 28 long, centre aligned and normal spacing
        TextPropCollection tppb = stpa.addParagraphTextPropCollection(28);

        tp = tppb.addWithName("linespacing");
        tp.setValue(80);

        // Third is 25 long, right aligned and normal spacing
        TextPropCollection tppc = stpa.addParagraphTextPropCollection(25);

        tp = tppc.addWithName("alignment");
        tp.setValue(2);
        tp = tppc.addWithName("linespacing");
        tp.setValue(80);

        // Forth is left aligned + normal line spacing (despite differing font)
        TextPropCollection tppd = stpa.addParagraphTextPropCollection(97);

        tp = tppd.addWithName("alignment");
        tp.setValue(0);
        tp = tppd.addWithName("linespacing");
        tp.setValue(80);


        // Now do 4 character styles
        LinkedList cs = stpa.getCharacterStyles();

        // First is 30 long, bold and font size
        TextPropCollection tpca = (TextPropCollection)cs.get(0);
        tpca.updateTextSize(30);

        tp = tpca.addWithName("font.size");
        tp.setValue(20);
        CharFlagsTextProp cftp = (CharFlagsTextProp)
            tpca.addWithName("char_flags");
        assertEquals(0, cftp.getValue());
        cftp.setSubValue(true, CharFlagsTextProp.BOLD_IDX);
        assertEquals(1, cftp.getValue());

        // Second is 28 long, blue and italic
        TextPropCollection tpcb = stpa.addCharacterTextPropCollection(28);

        tp = tpcb.addWithName("font.size");
        tp.setValue(20);
        tp = tpcb.addWithName("font.color");
        tp.setValue(0x05000000);
        cftp = (CharFlagsTextProp)tpcb.addWithName("char_flags");
        cftp.setSubValue(true, CharFlagsTextProp.ITALIC_IDX);
        assertEquals(2, cftp.getValue());

        // Third is 25 long and red
        TextPropCollection tpcc = stpa.addCharacterTextPropCollection(25);

        tp = tpcc.addWithName("font.size");
        tp.setValue(20);
        tp = tpcc.addWithName("font.color");
        tp.setValue(0xfe0033ff);

        // Fourth is 96 long, underlined and different+bigger font
        TextPropCollection tpcd = stpa.addCharacterTextPropCollection(96);

        tp = tpcd.addWithName("font.size");
        tp.setValue(24);
        tp = tpcd.addWithName("font.index");
        tp.setValue(1);
        cftp = (CharFlagsTextProp)tpcd.addWithName("char_flags");
        cftp.setSubValue(true, CharFlagsTextProp.UNDERLINE_IDX);
        assertEquals(4, cftp.getValue());

        // Fifth is 1 long, underlined and different+bigger font + red
        TextPropCollection tpce = stpa.addCharacterTextPropCollection(1);

        tp = tpce.addWithName("font.size");
        tp.setValue(24);
        tp = tpce.addWithName("font.index");
        tp.setValue(1);
        tp = tpce.addWithName("font.color");
        tp.setValue(0xfe0033ff);
        cftp = (CharFlagsTextProp)tpce.addWithName("char_flags");
        cftp.setSubValue(true, CharFlagsTextProp.UNDERLINE_IDX);
        assertEquals(4, cftp.getValue());


        // Check it's as expected
        assertEquals(4, stpa.getParagraphStyles().size());
        assertEquals(5, stpa.getCharacterStyles().size());

        // Compare in detail to b
        StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
        stpb.setParentTextSize(data_b_text_len);
        LinkedList psb = stpb.getParagraphStyles();
        LinkedList csb = stpb.getCharacterStyles();

        assertEquals(psb.size(), ps.size());
        assertEquals(csb.size(), cs.size());

        // Ensure Paragraph Character styles match
        for(int z=0; z<2; z++) {
            LinkedList lla = cs;
            LinkedList llb = csb;
            int upto = 5;
            if(z == 1) {
                lla = ps;
                llb = psb;
                upto = 4;
            }

            for(int i=0; i<upto; i++) {
                TextPropCollection ca = (TextPropCollection)lla.get(i);
                TextPropCollection cb = (TextPropCollection)llb.get(i);

                assertEquals(ca.getCharactersCovered(), cb.getCharactersCovered());
                assertEquals(ca.getTextPropList().size(), cb.getTextPropList().size());

                for(int j=0; j<ca.getTextPropList().size(); j++) {
                    TextProp tpa = (TextProp)ca.getTextPropList().get(j);
                    TextProp tpb = (TextProp)cb.getTextPropList().get(j);
                    //System.out.println("TP " + i + " " + j + " " + tpa.getName() + "\t" + tpa.getValue() );
                    assertEquals(tpa.getName(), tpb.getName());
                    assertEquals(tpa.getMask(), tpb.getMask());
                    assertEquals(tpa.getWriteMask(), tpb.getWriteMask());
                    assertEquals(tpa.getValue(), tpb.getValue());
                }

                ByteArrayOutputStream ba = new ByteArrayOutputStream();
                ByteArrayOutputStream bb = new ByteArrayOutputStream();

                ca.writeOut(ba);
                cb.writeOut(bb);
                byte[] cab = ba.toByteArray();
                byte[] cbb = bb.toByteArray();

                assertEquals(cbb.length, cab.length);
                for(int j=0; j<cab.length; j++) {
                    //System.out.println("On tp " + z + " " + i + " " + j + "\t" + cab[j] + "\t" + cbb[j]);
                    assertEquals(cbb[j], cab[j]);
                }
            }
        }



        // Check byte level with b
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stpa.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertEquals(data_b.length, b.length);
        for(int i=0; i<data_b.length; i++) {
            //System.out.println(i + "\t" + b[i] + "\t" + data_b[i] + "\t" + Integer.toHexString(b[i]) );
            assertEquals(data_b[i],b[i]);
        }
    }

    public void testWriteA() {
        doReadWrite(data_a, -1);
    }

    public void testLoadWriteA() {
        doReadWrite(data_b, data_b_text_len);
    }


    public void testWriteB() {
        doReadWrite(data_b, -1);
    }

    public void testLoadWriteB() {
        doReadWrite(data_b, data_b_text_len);
    }

    public void testLoadWriteC() {
        doReadWrite(data_c, data_c_text_len);
    }

    public void testLoadWriteD() {
        doReadWrite(data_d, data_d_text_len);
    }

    protected void doReadWrite(byte[] data, int textlen) {
        StyleTextPropAtom stpb = new StyleTextPropAtom(data, 0,data.length);
        if(textlen != -1) stpb.setParentTextSize(textlen);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            stpb.writeOut(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = out.toByteArray();

        assertEquals(data.length, bytes.length);
        try {
            assertTrue(Arrays.equals(data, bytes));
        } catch (Throwable e){
            //print hex dump if failed
            assertEquals(HexDump.toHex(data), HexDump.toHex(bytes));
        }
    }

    public void testNotEnoughDataProp() {
        // We don't have enough data in the record to cover
        //  all the properties the mask says we have
        // Make sure we just do the best we can
        StyleTextPropAtom stpc = new StyleTextPropAtom(data_c,0,data_c.length);
        stpc.setParentTextSize(data_c_text_len);

        // If we get here, we didn't break
    }

    /**
     * Check the test data for Bug 40143.
     */
    public void testBug40143() {
        StyleTextPropAtom atom = new StyleTextPropAtom(data_d, 0, data_d.length);
        atom.setParentTextSize(data_d_text_len);

        TextPropCollection prprops = (TextPropCollection)atom.getParagraphStyles().getFirst();
        assertEquals(data_d_text_len+1, prprops.getCharactersCovered());
        assertEquals(1, prprops.getTextPropList().size()); //1 property found
        assertEquals(1, prprops.findByName("alignment").getValue());

        TextPropCollection chprops = (TextPropCollection)atom.getCharacterStyles().getFirst();
        assertEquals(data_d_text_len+1, chprops.getCharactersCovered());
        assertEquals(5, chprops.getTextPropList().size()); //5 properties found
        assertEquals(1, chprops.findByName("char_flags").getValue());
        assertEquals(1, chprops.findByName("font.index").getValue());
        assertEquals(20, chprops.findByName("font.size").getValue());
        assertEquals(0, chprops.findByName("asian.font.index").getValue());
        assertEquals(1, chprops.findByName("ansi.font.index").getValue());
    }

    /**
     * Check the test data for Bug 42677.
     */
     public void test42677() {
        int length = 18;
        byte[] data = {0x00, 0x00, (byte)0xA1, 0x0F, 0x28, 0x00, 0x00, 0x00,
                       0x13, 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , (byte)0xF1 , 0x20 , 0x00, 0x00 , 0x00 , 0x00 ,
                       0x22 , 0x20 , 0x00 , 0x00 , 0x64 , 0x00 , 0x00 , 0x00 , 0x00 , (byte)0xFF ,
                       0x00 , 0x00 , 0x13 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x63 , 0x00 ,
                       0x00 , 0x00 , 0x01 , 0x00 , 0x00 , 0x00 , 0x0F , 0x00
        };
        doReadWrite(data, length);

    }

    /**
     *  Bug 45815: bit mask values are not preserved on read-write
     *
     * From the test file attached to the bug:
     *
     * <StyleTextPropAtom info="0" type="4001" size="94" offset="114782" header="00 00 A1 0F 5E 00 00 00 ">
     *   14 00 00 00 00 00 41 00 0A 00 06 00 50 00 07 00 01 00 00 00 00 00 00 00 02
     *   00 00 00 01 04 00 00 01 04 01 00 00 00 01 08 00 00 01 08 0C 00 00 00 01 0C
     *   00 00 01 0C 01 00 00 00 01 10 00 00 01 10 01 00 00 00 01 14 00 00 01 14 01
     *   00 00 00 01 18 00 00 01 18 01 00 00 00 01 1C 00 00 01 1C
     * </StyleTextPropAtom>
     */
    public void test45815() {
        int length = 19;
        byte[] data = {
                0x00, 0x00, (byte)0xA1, 0x0F, 0x5E, 0x00, 0x00, 0x00, 0x14, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x41, 0x00, 0x0A, 0x00, 0x06, 0x00,
                0x50, 0x00, 0x07, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x00,
                0x01, 0x04, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x00, 0x00,
                0x01, 0x08, 0x0C, 0x00, 0x00, 0x00, 0x01, 0x0C, 0x00, 0x00,
                0x01, 0x0C, 0x01, 0x00, 0x00, 0x00, 0x01, 0x10, 0x00, 0x00,
                0x01, 0x10, 0x01, 0x00, 0x00, 0x00, 0x01, 0x14, 0x00, 0x00,
                0x01, 0x14, 0x01, 0x00, 0x00, 0x00, 0x01, 0x18, 0x00, 0x00,
                0x01, 0x18, 0x01, 0x00, 0x00, 0x00, 0x01, 0x1C, 0x00, 0x00,
                0x01, 0x1C
        };
        doReadWrite(data, length);
    }

}
