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

package org.apache.poi.xssf.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

public final class TestSheetDataWriter {

    final String unicodeSurrogates = "\uD835\uDF4A\uD835\uDF4B\uD835\uDF4C\uD835\uDF4D\uD835\uDF4E"
            + "\uD835\uDF4F\uD835\uDF50\uD835\uDF51\uD835\uDF52\uD835\uDF53\uD835\uDF54\uD835"
            + "\uDF55\uD835\uDF56\uD835\uDF57\uD835\uDF58\uD835\uDF59\uD835\uDF5A\uD835\uDF5B"
            + "\uD835\uDF5C\uD835\uDF5D\uD835\uDF5E\uD835\uDF5F\uD835\uDF60\uD835\uDF61\uD835"
            + "\uDF62\uD835\uDF63\uD835\uDF64\uD835\uDF65\uD835\uDF66\uD835\uDF67\uD835\uDF68"
            + "\uD835\uDF69\uD835\uDF6A\uD835\uDF6B\uD835\uDF6C\uD835\uDF6D\uD835\uDF6E\uD835"
            + "\uDF6F\uD835\uDF70\uD835\uDF71\uD835\uDF72\uD835\uDF73\uD835\uDF74\uD835\uDF75"
            + "\uD835\uDF76\uD835\uDF77\uD835\uDF78\uD835\uDF79\uD835\uDF7A";

    @Test
	void testReplaceWithQuestionMark() {
        for(int i = 0; i < unicodeSurrogates.length(); i++) {
            assertFalse(SheetDataWriter.replaceWithQuestionMark(unicodeSurrogates.charAt(i)));
        }
        assertTrue(SheetDataWriter.replaceWithQuestionMark('\uFFFE'));
        assertTrue(SheetDataWriter.replaceWithQuestionMark('\uFFFF'));
        assertTrue(SheetDataWriter.replaceWithQuestionMark('\u0000'));
        assertTrue(SheetDataWriter.replaceWithQuestionMark('\u000F'));
        assertTrue(SheetDataWriter.replaceWithQuestionMark('\u001F'));
	}

    @Test
    void testWriteUnicodeSurrogates() throws IOException {
        SheetDataWriter writer = new SheetDataWriter();
        try {
            writer.outputEscapedString(unicodeSurrogates);
            writer.close();
            File file = writer.getTempFile();
            try (FileInputStream is = new FileInputStream(file)) {
                String text = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
                assertEquals(unicodeSurrogates, text);
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
    @Test
    void testWriteNewLines() throws IOException {
        SheetDataWriter writer = new SheetDataWriter();
        try {
            writer.outputEscapedString("\r\n");
            writer.close();
            File file = writer.getTempFile();
            try (FileInputStream is = new FileInputStream(file)) {
                String text = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
                assertEquals("&#xd;&#xa;", text);
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
