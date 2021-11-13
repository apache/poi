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
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;

@Beta
public class XDDFPictureFillProperties implements XDDFFillProperties {
    private CTBlipFillProperties props;

    public XDDFPictureFillProperties() {
        this(CTBlipFillProperties.Factory.newInstance());
    }

    protected XDDFPictureFillProperties(CTBlipFillProperties properties) {
        this.props = properties;
    }

    @Internal
    public CTBlipFillProperties getXmlObject() {
        return props;
    }

    public XDDFPicture getPicture() {
        if (props.isSetBlip()) {
            return new XDDFPicture(props.getBlip());
        } else {
            return null;
        }
    }

    public void setPicture(XDDFPicture picture) {
        if (picture == null) {
            props.unsetBlip();
        } else {
            props.setBlip(picture.getXmlObject());
        }
    }

    public Boolean isRotatingWithShape() {
        if (props.isSetRotWithShape()) {
            return props.getRotWithShape();
        } else {
            return false;
        }
    }

    public void setRotatingWithShape(Boolean rotating) {
        if (rotating == null) {
            if (props.isSetRotWithShape()) {
                props.unsetRotWithShape();
            }
        } else {
            props.setRotWithShape(rotating);
        }
    }

    public Long getDpi() {
        if (props.isSetDpi()) {
            return props.getDpi();
        } else {
            return null;
        }
    }

    public void setDpi(Long dpi) {
        if (dpi == null) {
            if (props.isSetDpi()) {
                props.unsetDpi();
            }
        } else {
            props.setDpi(dpi);
        }
    }

    public XDDFRelativeRectangle getSourceRectangle() {
        if (props.isSetSrcRect()) {
            return new XDDFRelativeRectangle(props.getSrcRect());
        } else {
            return null;
        }
    }

    public void setSourceRectangle(XDDFRelativeRectangle rectangle) {
        if (rectangle == null) {
            if (props.isSetSrcRect()) {
                props.unsetSrcRect();
            }
        } else {
            props.setSrcRect(rectangle.getXmlObject());
        }
    }

    public XDDFStretchInfoProperties getStetchInfoProperties() {
        if (props.isSetStretch()) {
            return new XDDFStretchInfoProperties(props.getStretch());
        } else {
            return null;
        }
    }

    public void setStretchInfoProperties(XDDFStretchInfoProperties properties) {
        if (properties == null) {
            if (props.isSetStretch()) {
                props.unsetStretch();
            }
        } else {
            props.setStretch(properties.getXmlObject());
        }
    }

    public XDDFTileInfoProperties getTileInfoProperties() {
        if (props.isSetTile()) {
            return new XDDFTileInfoProperties(props.getTile());
        } else {
            return null;
        }
    }

    public void setTileInfoProperties(XDDFTileInfoProperties properties) {
        if (properties == null) {
            if (props.isSetTile()) {
                props.unsetTile();
            }
        } else {
            props.setTile(properties.getXmlObject());
        }
    }
}
