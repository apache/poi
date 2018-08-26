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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;

@Beta
public abstract class XDDFColor {
    protected CTColor container;

    @Internal
    protected XDDFColor(CTColor container) {
        this.container = container;
    }

    public static XDDFColor from(byte[] color) {
        return new XDDFColorRgbBinary(color);
    }

    public static XDDFColor from(int red, int green, int blue) {
        return new XDDFColorRgbPercent(red, green, blue);
    }

    public static XDDFColor from(PresetColor color) {
        return new XDDFColorPreset(color);
    }

    public static XDDFColor from(SchemeColor color) {
        return new XDDFColorSchemeBased(color);
    }

    public static XDDFColor from(SystemColor color) {
        return new XDDFColorSystemDefined(color);
    }

    @Internal
    public static XDDFColor forColorContainer(CTColor container) {
        if (container.isSetHslClr()) {
            return new XDDFColorHsl(container.getHslClr(), container);
        } else if (container.isSetPrstClr()) {
            return new XDDFColorPreset(container.getPrstClr(), container);
        } else if (container.isSetSchemeClr()) {
            return new XDDFColorSchemeBased(container.getSchemeClr(), container);
        } else if (container.isSetScrgbClr()) {
            return new XDDFColorRgbPercent(container.getScrgbClr(), container);
        } else if (container.isSetSrgbClr()) {
            return new XDDFColorRgbBinary(container.getSrgbClr(), container);
        } else if (container.isSetSysClr()) {
            return new XDDFColorSystemDefined(container.getSysClr(), container);
        }
        return null;
    }

    @Internal
    public CTColor getColorContainer() {
        return container;
    }

    @Internal
    protected abstract XmlObject getXmlObject();
}
