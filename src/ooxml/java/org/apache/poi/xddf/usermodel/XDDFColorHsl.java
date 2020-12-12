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

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;

@Beta
public class XDDFColorHsl extends XDDFColor {
    private CTHslColor color;

    public XDDFColorHsl(int hue, int saturation, int luminance) {
        this(CTHslColor.Factory.newInstance(), CTColor.Factory.newInstance());
        setHue(hue);
        setSaturation(saturation);
        setLuminance(luminance);
    }

    @Internal
    protected XDDFColorHsl(CTHslColor color) {
        this(color, null);
    }

    @Internal
    protected XDDFColorHsl(CTHslColor color, CTColor container) {
        super(container);
        this.color = color;
    }

    @Override
    @Internal
    protected XmlObject getXmlObject() {
        return color;
    }

    public int getHue() {
        return color.getHue2();
    }

    public void setHue(int hue) {
        color.setHue2(hue);
    }

    public int getSaturation() {
        return POIXMLUnits.parsePercent(color.xgetSat2()) / 1000;
    }

    public void setSaturation(int saturation) {
        color.setSat2(saturation);
    }

    public int getLuminance() {
        return POIXMLUnits.parsePercent(color.xgetLum2()) / 1000;
    }

    public void setLuminance(int lightness) {
        color.setLum2(lightness);
    }
}
