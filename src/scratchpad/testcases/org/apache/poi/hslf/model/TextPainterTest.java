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
package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.hssf.usermodel.DummyGraphics2d;
import org.junit.Test;


public class TextPainterTest {
    @Test
    public void testTextPainter() {
        HSLFTextShape shape = new Polygon();
        TextPainter painter = new TextPainter(shape);
        painter.getAttributedString(new HSLFTextParagraph(null, new TextCharsAtom(), null));
        painter.paint(new DummyGraphics2d());
        painter.getTextElements((float)1.0, null);
    }

    @Test
    public void testTextPainterWithText() {
        HSLFTextShape shape = new Polygon();
        TextPainter painter = new TextPainter(shape);
        TextCharsAtom tca = new TextCharsAtom();
        tca.setText("some text to read");
        HSLFTextParagraph txrun = new HSLFTextParagraph(new TextHeaderAtom(), tca, new StyleTextPropAtom(10));
        HSLFSlide sheet = new HSLFSlide(1, 1, 1);
        sheet.setSlideShow(new HSLFSlideShow());
        txrun.setSheet(sheet);

        painter.getAttributedString(txrun, new DummyGraphics2d());
        painter.paint(new DummyGraphics2d());
        painter.getTextElements((float)1.0, null);
    }
}
