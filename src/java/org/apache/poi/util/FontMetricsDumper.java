
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
        
package org.apache.poi.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

@SuppressWarnings("deprecation")
public class FontMetricsDumper {
    @SuppressForbidden("command line tool")
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();

        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font allFont : allFonts) {
            String fontName = allFont.getFontName();

            Font font = new Font(fontName, Font.BOLD, 10);
            FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            int fontHeight = fontMetrics.getHeight();

            props.setProperty("font." + fontName + ".height", fontHeight + "");
            StringBuilder characters = new StringBuilder();
            for (char c = 'a'; c <= 'z'; c++) {
                characters.append(c).append(", ");
            }
            for (char c = 'A'; c <= 'Z'; c++) {
                characters.append(c).append(", ");
            }
            for (char c = '0'; c <= '9'; c++) {
                characters.append(c).append(", ");
            }
            StringBuilder widths = new StringBuilder();
            for (char c = 'a'; c <= 'z'; c++) {
                widths.append(fontMetrics.getWidths()[c]).append(", ");
            }
            for (char c = 'A'; c <= 'Z'; c++) {
                widths.append(fontMetrics.getWidths()[c]).append(", ");
            }
            for (char c = '0'; c <= '9'; c++) {
                widths.append(fontMetrics.getWidths()[c]).append(", ");
            }
            props.setProperty("font." + fontName + ".characters", characters.toString());
            props.setProperty("font." + fontName + ".widths", widths.toString());
        }

        OutputStream fileOut = new FileOutputStream("font_metrics.properties");
        try {
            props.store(fileOut, "Font Metrics");
        } finally {
            fileOut.close();
        }
    }
}
