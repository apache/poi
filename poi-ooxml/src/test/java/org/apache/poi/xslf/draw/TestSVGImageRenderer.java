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

package org.apache.poi.xslf.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.batik.bridge.BridgeException;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

class TestSVGImageRenderer {
    private static final String SVG_START =
        "<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' width='10' height='10'>";
    private static final String RED_RECT =
        SVG_START + "<rect id='r' width='10' height='10' fill='red'/></svg>";

    @Test
    void externalFileReferencesAreNotLoaded() throws IOException {
        File local = TempFile.createTempFile("local", ".svg");
        try {
            Files.write(local.toPath(), RED_RECT.getBytes(StandardCharsets.UTF_8));
            String url = local.toURI().toString();

            assertThrows(BridgeException.class, () ->
                render(SVG_START + "<use xlink:href='" + url + "#r'/></svg>"));
            assertThrows(BridgeException.class, () ->
                render(SVG_START + "<image width='10' height='10' xlink:href='" + url + "'/></svg>"));
        } finally {
            assertTrue(local.delete());
        }
    }

    @Test
    void embeddedReferencesAreLoaded() throws IOException {
        assertEquals(0xffff0000, render(SVG_START + "<defs><rect id='r' width='10' height='10' fill='red'/></defs>" +
            "<use xlink:href='#r'/></svg>").getRGB(5, 5));

        String data = "data:image/svg+xml;base64," +
            Base64.getEncoder().encodeToString(RED_RECT.getBytes(StandardCharsets.UTF_8));
        assertEquals(0xffff0000, render(SVG_START + "<image width='10' height='10' xlink:href='" + data + "'/></svg>")
            .getRGB(5, 5));
    }

    private static BufferedImage render(String svg) throws IOException {
        SVGImageRenderer renderer = new SVGImageRenderer();
        renderer.loadImage(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml");
        return renderer.getImage(new Dimension2DDouble(10, 10));
    }
}
