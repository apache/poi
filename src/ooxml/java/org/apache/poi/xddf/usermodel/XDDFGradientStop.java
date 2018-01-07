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
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;

@Beta
public class XDDFGradientStop {
    private CTGradientStop stop;

    @Internal
    protected XDDFGradientStop(CTGradientStop stop) {
        this.stop = stop;
    }

    @Internal
    protected CTGradientStop getXmlObject() {
        return stop;
    }

    public int getPosition() {
        return stop.getPos();
    }

    public void setPosition(int position) {
        stop.setPos(position);
    }

    public XDDFColor getColor() {
        if (stop.isSetHslClr()) {
            return new XDDFColorHsl(stop.getHslClr());
        } else if (stop.isSetPrstClr()) {
            return new XDDFColorPreset(stop.getPrstClr());
        } else if (stop.isSetSchemeClr()) {
            return new XDDFColorSchemeBased(stop.getSchemeClr());
        } else if (stop.isSetScrgbClr()) {
            return new XDDFColorRgbPercent(stop.getScrgbClr());
        } else if (stop.isSetSrgbClr()) {
            return new XDDFColorRgbBinary(stop.getSrgbClr());
        } else if (stop.isSetSysClr()) {
            return new XDDFColorSystemDefined(stop.getSysClr());
        }
        return null;
    }

    public void setColor(XDDFColor color) {
        if (stop.isSetHslClr()) {
            stop.unsetHslClr();
        }
        if (stop.isSetPrstClr()) {
            stop.unsetPrstClr();
        }
        if (stop.isSetSchemeClr()) {
            stop.unsetSchemeClr();
        }
        if (stop.isSetScrgbClr()) {
            stop.unsetScrgbClr();
        }
        if (stop.isSetSrgbClr()) {
            stop.unsetSrgbClr();
        }
        if (stop.isSetSysClr()) {
            stop.unsetSysClr();
        }
        if (color == null) {
            return;
        }
        if (color instanceof XDDFColorHsl) {
            stop.setHslClr((CTHslColor) color.getXmlObject());
        } else if (color instanceof XDDFColorPreset) {
            stop.setPrstClr((CTPresetColor) color.getXmlObject());
        } else if (color instanceof XDDFColorSchemeBased) {
            stop.setSchemeClr((CTSchemeColor) color.getXmlObject());
        } else if (color instanceof XDDFColorRgbPercent) {
            stop.setScrgbClr((CTScRgbColor) color.getXmlObject());
        } else if (color instanceof XDDFColorRgbBinary) {
            stop.setSrgbClr((CTSRgbColor) color.getXmlObject());
        } else if (color instanceof XDDFColorSystemDefined) {
            stop.setSysClr((CTSystemColor) color.getXmlObject());
        }
    }
}
