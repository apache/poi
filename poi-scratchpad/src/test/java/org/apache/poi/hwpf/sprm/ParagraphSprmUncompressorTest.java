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
package org.apache.poi.hwpf.sprm;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.TabDescriptor;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphSprmUncompressorTest {
    @Test
    void testLoadTabStopDefinitions() throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();

        try (HWPFDocument doc = HWPFTestDataSamples.openSampleFile("gh-issue-1126.doc")) {
            Range globalRange = doc.getRange();
            for (int i = 0; i < globalRange.numParagraphs(); i++) {
                paragraphs.add(globalRange.getParagraph(i));
            }
        }

        List<String> descriptions = paragraphs.stream().map(this::extractTabStops).collect(Collectors.toList());

        assertEquals(6, paragraphs.size());
        assertEquals(6, descriptions.size());

        assertEquals("0 (2268, left, none); 1 (5670, left, none)", descriptions.get(0));
        assertEquals("", descriptions.get(1));
        assertEquals("0 (1276, right, hyphen)", descriptions.get(2));
        assertEquals("0 (1276, right, hyphen); 1 (2694, center, dot); 2 (4253, right, underscore)", descriptions.get(3));
        assertEquals("0 (4253, right, underscore); 1 (6237, bar, none)", descriptions.get(4));
        assertEquals("0 (1134, decimal, dot)", descriptions.get(5));
    }

    private String extractTabStops(Paragraph paragraph) {
        TabDescriptor[] tabDescriptors = paragraph.getProps().getRgtbd();
        int[] tabStopsPositions = paragraph.getTabStopsPositions();

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < tabDescriptors.length; i++) {
            if (i > 0) out.append("; ");
            out
                    .append(i)
                    .append(" (")
                    .append(tabStopsPositions[i])
                    .append(", ")
                    .append(describeAlignment(tabDescriptors[i]))
                    .append(", ")
                    .append(describeLeader(tabDescriptors[i]))
                    .append(")");
        }
        return out.toString();
    }

    private String describeAlignment(TabDescriptor tabDescriptor) {
        switch (tabDescriptor.getJc()) {
            case 0:
                return "left";
            case 1:
                return "center";
            case 2:
                return "right";
            case 3:
                return "decimal";
            case 4:
                return "bar";
            case 5:
                return "list";
            default:
                return "unknown";
        }
    }

    private String describeLeader(TabDescriptor tabDescriptor) {
        switch (tabDescriptor.getTlc()) {
            case 0:
                return "none";
            case 1:
                return "dot";
            case 2:
                return "hyphen";
            case 3:
                return "underscore";
            case 4:
                return "heavy";
            case 5:
                return "middleDot";
            case 7:
                return "default";
            default:
                return "unknown";
        }
    }
}

