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

package org.apache.poi.xslf.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2DFontTextDrawer;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class PDFFontMapper extends PdfBoxGraphics2DFontTextDrawer {

    private static final String DEFAULT_TTF_PATTERN = ".*\\.tt[fc]";

    private static final String FONTDIRS_MAC =
        "$HOME/Library/Fonts;" +
        "/Library/Fonts;" +
        "/Network/Library/Fonts;" +
        "/System/Library/Fonts;" +
        "/System Folder/Fonts";

    private static final String FONTDIRS_WIN =
        "C:\\Windows\\Fonts";

    private static final String FONTDIRS_UNX =
        "/usr/share/fonts;" +
        "/usr/local/share/fonts;" +
        "$HOME/.fonts";


    private final Map<String,File> fonts = new HashMap<>();
    private final Set<String> registered = new HashSet<>();

    public PDFFontMapper(String fontDir, String fontTtf) {
        registerFonts(fontDir, fontTtf);
    }


    private void registerFonts(String fontDir, String fontTtf) {
        if (fontDir == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
            if (OS.contains("mac") || OS.contains("darwin")) {
                fontDir = FONTDIRS_MAC;
            } else if (OS.contains("win")) {
                fontDir = FONTDIRS_WIN;
            } else {
                fontDir = FONTDIRS_UNX;
            }
        }

        String fd = fontDir.replace("$HOME", System.getProperty("user.home"));
        final LinkedList<File> dirs = new LinkedList<>();
        Stream.of(fd.split(";")).map(File::new).filter(File::isDirectory).forEach(dirs::add);

        Pattern p = Pattern.compile(fontTtf == null ? DEFAULT_TTF_PATTERN : fontTtf);

        while (!dirs.isEmpty()) {
            File[] ttfs = dirs.removeFirst().listFiles((f, n) -> {
                File f2 = new File(f, n);
                if (f2.isDirectory()) {
                    dirs.add(f2);
                    return false;
                } else {
                    return p.matcher(n).matches();
                }
            });

            if (ttfs == null) {
                continue;
            }

            for (File f : ttfs) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                    fonts.put(font.getFontName(Locale.ROOT), f);
                } catch (IOException|FontFormatException ignored) {
                }

            }
        }
    }

    @Override
    protected PDFont mapFont(Font font, IFontTextDrawerEnv env) throws IOException, FontFormatException {
        String name = font.getFontName(Locale.ROOT);
        if (!registered.contains(name)) {
            registered.add(name);
            File f = fonts.get(name);
            if (f != null) {
                super.registerFont(name, f);
            }
        }
        return super.mapFont(font, env);
    }
}
