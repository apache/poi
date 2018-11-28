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
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;

@Beta
public class XDDFSolidFillProperties implements XDDFFillProperties {
    private CTSolidColorFillProperties props;

    public XDDFSolidFillProperties() {
        this(CTSolidColorFillProperties.Factory.newInstance());
    }

    public XDDFSolidFillProperties(XDDFColor color) {
        this(CTSolidColorFillProperties.Factory.newInstance());
        setColor(color);
    }

    @Internal
    public XDDFSolidFillProperties(CTSolidColorFillProperties properties) {
        this.props = properties;
    }

    @Internal
    public CTSolidColorFillProperties getXmlObject() {
        return props;
    }

    public XDDFColor getColor() {
        if (props.isSetHslClr()) {
            return new XDDFColorHsl(props.getHslClr());
        } else if (props.isSetPrstClr()) {
            return new XDDFColorPreset(props.getPrstClr());
        } else if (props.isSetSchemeClr()) {
            return new XDDFColorSchemeBased(props.getSchemeClr());
        } else if (props.isSetScrgbClr()) {
            return new XDDFColorRgbPercent(props.getScrgbClr());
        } else if (props.isSetSrgbClr()) {
            return new XDDFColorRgbBinary(props.getSrgbClr());
        } else if (props.isSetSysClr()) {
            return new XDDFColorSystemDefined(props.getSysClr());
        }
        return null;
    }

    public void setColor(XDDFColor color) {
        if (props.isSetHslClr()) {
            props.unsetHslClr();
        }
        if (props.isSetPrstClr()) {
            props.unsetPrstClr();
        }
        if (props.isSetSchemeClr()) {
            props.unsetSchemeClr();
        }
        if (props.isSetScrgbClr()) {
            props.unsetScrgbClr();
        }
        if (props.isSetSrgbClr()) {
            props.unsetSrgbClr();
        }
        if (props.isSetSysClr()) {
            props.unsetSysClr();
        }
        if (color == null) {
            return;
        }
        if (color instanceof XDDFColorHsl) {
            props.setHslClr((CTHslColor) color.getXmlObject());
        } else if (color instanceof XDDFColorPreset) {
            props.setPrstClr((CTPresetColor) color.getXmlObject());
        } else if (color instanceof XDDFColorSchemeBased) {
            props.setSchemeClr((CTSchemeColor) color.getXmlObject());
        } else if (color instanceof XDDFColorRgbPercent) {
            props.setScrgbClr((CTScRgbColor) color.getXmlObject());
        } else if (color instanceof XDDFColorRgbBinary) {
            props.setSrgbClr((CTSRgbColor) color.getXmlObject());
        } else if (color instanceof XDDFColorSystemDefined) {
            props.setSysClr((CTSystemColor) color.getXmlObject());
        }
    }
}
