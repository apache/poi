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

package org.apache.poi.hwpf.sprm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.TableProperties;
import org.junit.jupiter.api.Test;

class TestSprmOperation {

    /**
     * Bug 66245: a variable-length SPRM (size code 6) at the very end of a
     * grpprl, with no room for its operand-length byte, must not throw an
     * {@link ArrayIndexOutOfBoundsException}. It consumes the remaining bytes
     * so that the iterator terminates.
     */
    @Test
    void truncatedVariableLengthSprm() {
        // sprmPJc (0x2403, 1 byte operand) followed by a size-code-6 PAP sprm
        // opcode (0xC61D) with no length byte
        byte[] grpprl = { 0x03, 0x24, 0x01, 0x1D, (byte) 0xC6 };

        SprmIterator it = new SprmIterator(grpprl, 0);
        assertTrue(it.hasNext());
        SprmOperation first = it.next();
        assertEquals(3, first.size());
        assertEquals(1, first.getOperand());

        assertTrue(it.hasNext());
        SprmOperation truncated = it.next();
        assertEquals(6, truncated.getSizeCode());
        assertEquals(2, truncated.size());
        assertFalse(it.hasNext());

        ParagraphProperties pap = ParagraphSprmUncompressor.uncompressPAP(
                new ParagraphProperties(), grpprl, 0);
        assertNotNull(pap);
        assertEquals(1, pap.getJc());
    }

    /**
     * Same as above for sprmPChgTabs (0xC615), whose operand length is a
     * two-byte value: only one of the two bytes is present.
     */
    @Test
    void truncatedLongParagraphSprm() {
        byte[] grpprl = { 0x03, 0x24, 0x01, 0x15, (byte) 0xC6, 0x10 };

        SprmIterator it = new SprmIterator(grpprl, 0);
        assertEquals(3, it.next().size());
        assertTrue(it.hasNext());
        SprmOperation truncated = it.next();
        assertEquals(3, truncated.size());
        assertFalse(it.hasNext());

        ParagraphProperties pap = ParagraphSprmUncompressor.uncompressPAP(
                new ParagraphProperties(), grpprl, 0);
        assertEquals(1, pap.getJc());
    }

    /**
     * The operand of a variable-length SPRM must not be read past the end of
     * the grpprl, nor past the 4 bytes that fit into an int.
     */
    @Test
    void variableLengthOperandBounds() {
        // length byte says 2, but the "operand length" the code reads from the
        // second operand byte claims 0x7F bytes: only what is there is used
        byte[] grpprl = { 0x1D, (byte) 0xC6, 0x02, 0x05, 0x7F };
        SprmOperation sprm = new SprmOperation(grpprl, 0);
        assertEquals(5, sprm.size());
        assertEquals(0x7F, sprm.getOperand());

        // no operand at all
        SprmOperation truncated = new SprmOperation(new byte[] { 0x1D, (byte) 0xC6 }, 0);
        assertEquals(2, truncated.size());
        assertEquals(0, truncated.getOperand());
        assertNotNull(truncated.toString());
    }

    /**
     * A SEPX whose last SPRM is missing part of its operand: the preceding
     * SPRMs are still applied, the broken one is logged and skipped.
     */
    @Test
    void truncatedSectionSprm() {
        // sprmSCnsPgn (0x3000) = 1, then a 2-byte-operand SEP sprm (0x5001)
        // with only one operand byte
        byte[] grpprl = { 0x00, 0x30, 0x01, 0x01, 0x50, 0x02 };
        SectionProperties sep = SectionSprmUncompressor.uncompressSEP(grpprl, 0);
        assertEquals(1, sep.getCnsPgn());
    }

    /**
     * A TAPX whose sprmTDefTable (0xD608) declares an operand that is not
     * actually present.
     */
    @Test
    void truncatedTableDefinitionSprm() {
        byte[] grpprl = { 0x00, 0x00, 0x08, (byte) 0xD6, 0x01, 0x00 };
        TableProperties tap = TableSprmUncompressor.uncompressTAP(new SprmBuffer(grpprl, 2));
        assertNotNull(tap);
        assertEquals(1, tap.getItcMac());
    }
}
