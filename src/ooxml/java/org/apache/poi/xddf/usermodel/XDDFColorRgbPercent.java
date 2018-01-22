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

package org.apache.poi.xddf.usermodel;

import java.util.Locale;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;

@Beta
public class XDDFColorRgbPercent extends XDDFColor {
    private CTScRgbColor color;

    public XDDFColorRgbPercent(int red, int green, int blue) {
        this(CTScRgbColor.Factory.newInstance(), CTColor.Factory.newInstance());
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    @Internal
    protected XDDFColorRgbPercent(CTScRgbColor color) {
        this(color, null);
    }

    @Internal
    protected XDDFColorRgbPercent(CTScRgbColor color, CTColor container) {
        super(container);
        this.color = color;
    }

    @Override
    @Internal
    protected XmlObject getXmlObject() {
        return color;
    }

    public int getRed() {
        return color.getR();
    }

    public void setRed(int red) {
        color.setR(normalize(red));
    }

    public int getGreen() {
        return color.getG();
    }

    public void setGreen(int green) {
        color.setG(normalize(green));
    }

    public int getBlue() {
        return color.getB();
    }

    public void setBlue(int blue) {
        color.setB(normalize(blue));
    }

    private int normalize(int value) {
        if (value < 0) {
            return 0;
        }
        if (100_000 < value) {
            return 100_000;
        }
        return value;
    }

    public String toRGBHex() {
        StringBuilder sb = new StringBuilder(6);
        appendHex(sb, color.getR());
        appendHex(sb, color.getG());
        appendHex(sb, color.getB());
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    private void appendHex(StringBuilder sb, int value) {
        int b = value * 255 / 100_000;
        sb.append(String.format(Locale.ROOT, "%02X", b));
    }
}
