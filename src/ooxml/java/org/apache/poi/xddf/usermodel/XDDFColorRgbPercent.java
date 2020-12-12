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

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.STPercentage;

@Beta
public class XDDFColorRgbPercent extends XDDFColor {
    private final CTScRgbColor color;

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
        return POIXMLUnits.parsePercent(color.xgetR());
    }

    public void setRed(int red) {
        color.setR(normalize(red));
    }

    public int getGreen() {
        return POIXMLUnits.parsePercent(color.xgetG());
    }

    public void setGreen(int green) {
        color.setG(normalize(green));
    }

    public int getBlue() {
        return POIXMLUnits.parsePercent(color.xgetB());
    }

    public void setBlue(int blue) {
        color.setB(normalize(blue));
    }

    private int normalize(int value) {
        return value < 0 ? 0 : Math.min(100_000, value);
    }

    public String toRGBHex() {
        int c = 0;
        for (STPercentage pct : new STPercentage[] { color.xgetR(), color.xgetG(), color.xgetB() }) {
            c = c << 8 | ((POIXMLUnits.parsePercent(pct) * 255 / 100_000) & 0xFF);
        }
        return String.format(Locale.ROOT, "%06X", c);
    }
}
