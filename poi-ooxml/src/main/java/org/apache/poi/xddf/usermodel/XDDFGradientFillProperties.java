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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;

@Beta
public class XDDFGradientFillProperties implements XDDFFillProperties {
    private CTGradientFillProperties props;

    public XDDFGradientFillProperties() {
        this(CTGradientFillProperties.Factory.newInstance());
    }

    protected XDDFGradientFillProperties(CTGradientFillProperties properties) {
        this.props = properties;
    }

    @Internal
    public CTGradientFillProperties getXmlObject() {
        return props;
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

    public TileFlipMode getTileFlipMode() {
        if (props.isSetFlip()) {
            return TileFlipMode.valueOf(props.getFlip());
        } else {
            return null;
        }
    }

    public void setTileFlipMode(TileFlipMode mode) {
        if (mode == null) {
            if (props.isSetFlip()) {
                props.unsetFlip();
            }
        } else {
            props.setFlip(mode.underlying);
        }
    }

    public XDDFGradientStop addGradientStop() {
        if (!props.isSetGsLst()) {
            props.addNewGsLst();
        }
        return new XDDFGradientStop(props.getGsLst().addNewGs());
    }

    public XDDFGradientStop insertGradientStop(int index) {
        if (!props.isSetGsLst()) {
            props.addNewGsLst();
        }
        return new XDDFGradientStop(props.getGsLst().insertNewGs(index));
    }

    public void removeGradientStop(int index) {
        if (props.isSetGsLst()) {
            props.getGsLst().removeGs(index);
        }
    }

    public XDDFGradientStop getGradientStop(int index) {
        if (props.isSetGsLst()) {
            return new XDDFGradientStop(props.getGsLst().getGsArray(index));
        } else {
            return null;
        }
    }

    public List<XDDFGradientStop> getGradientStops() {
        if (props.isSetGsLst()) {
            return Collections.unmodifiableList(props
                .getGsLst()
                .getGsList()
                .stream()
                .map(XDDFGradientStop::new)
                .collect(Collectors.toList()));
        } else {
            return Collections.emptyList();
        }
    }

    public int countGradientStops() {
        if (props.isSetGsLst()) {
            return props.getGsLst().sizeOfGsArray();
        } else {
            return 0;
        }
    }

    public XDDFLinearShadeProperties getLinearShadeProperties() {
        if (props.isSetLin()) {
            return new XDDFLinearShadeProperties(props.getLin());
        } else {
            return null;
        }
    }

    public void setLinearShadeProperties(XDDFLinearShadeProperties properties) {
        if (properties == null) {
            if (props.isSetLin()) {
                props.unsetLin();
            }
        } else {
            props.setLin(properties.getXmlObject());
        }
    }

    public XDDFPathShadeProperties getPathShadeProperties() {
        if (props.isSetPath()) {
            return new XDDFPathShadeProperties(props.getPath());
        } else {
            return null;
        }
    }

    public void setPathShadeProperties(XDDFPathShadeProperties properties) {
        if (properties == null) {
            if (props.isSetPath()) {
                props.unsetPath();
            }
        } else {
            props.setPath(properties.getXmlObject());
        }
    }

    public XDDFRelativeRectangle getTileRectangle() {
        if (props.isSetTileRect()) {
            return new XDDFRelativeRectangle(props.getTileRect());
        } else {
            return null;
        }
    }
    public void setTileRectangle(XDDFRelativeRectangle rectangle) {
        if (rectangle == null) {
            if (props.isSetTileRect()) {
                props.unsetTileRect();
            }
        } else {
            props.setTileRect(rectangle.getXmlObject());
        }
    }
}
