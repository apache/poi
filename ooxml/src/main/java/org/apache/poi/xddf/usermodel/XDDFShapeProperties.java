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
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

@Beta
public class XDDFShapeProperties {
    private CTShapeProperties props;

    public XDDFShapeProperties() {
        this(CTShapeProperties.Factory.newInstance());
    }

    @Internal
    public XDDFShapeProperties(CTShapeProperties properties) {
        this.props = properties;
    }

    @Internal
    public CTShapeProperties getXmlObject() {
        return props;
    }

    public BlackWhiteMode getBlackWhiteMode() {
        if (props.isSetBwMode()) {
            return BlackWhiteMode.valueOf(props.getBwMode());
        } else {
            return null;
        }
    }

    public void setBlackWhiteMode(BlackWhiteMode mode) {
        if (mode == null) {
            if (props.isSetBwMode()) {
                props.unsetBwMode();
            }
        } else {
            props.setBwMode(mode.underlying);
        }
    }

    public XDDFFillProperties getFillProperties() {
        if (props.isSetGradFill()) {
            return new XDDFGradientFillProperties(props.getGradFill());
        } else if (props.isSetGrpFill()) {
            return new XDDFGroupFillProperties(props.getGrpFill());
        } else if (props.isSetNoFill()) {
            return new XDDFNoFillProperties(props.getNoFill());
        } else if (props.isSetPattFill()) {
            return new XDDFPatternFillProperties(props.getPattFill());
        } else if (props.isSetBlipFill()) {
            return new XDDFPictureFillProperties(props.getBlipFill());
        } else if (props.isSetSolidFill()) {
            return new XDDFSolidFillProperties(props.getSolidFill());
        } else {
            return null;
        }
    }

    public void setFillProperties(XDDFFillProperties properties) {
        if (props.isSetBlipFill()) {
            props.unsetBlipFill();
        }
        if (props.isSetGradFill()) {
            props.unsetGradFill();
        }
        if (props.isSetGrpFill()) {
            props.unsetGrpFill();
        }
        if (props.isSetNoFill()) {
            props.unsetNoFill();
        }
        if (props.isSetPattFill()) {
            props.unsetPattFill();
        }
        if (props.isSetSolidFill()) {
            props.unsetSolidFill();
        }
        if (properties == null) {
            return;
        }
        if (properties instanceof XDDFGradientFillProperties) {
            props.setGradFill(((XDDFGradientFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFGroupFillProperties) {
            props.setGrpFill(((XDDFGroupFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFNoFillProperties) {
            props.setNoFill(((XDDFNoFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFPatternFillProperties) {
            props.setPattFill(((XDDFPatternFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFPictureFillProperties) {
            props.setBlipFill(((XDDFPictureFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFSolidFillProperties) {
            props.setSolidFill(((XDDFSolidFillProperties) properties).getXmlObject());
        }
    }

    public XDDFLineProperties getLineProperties() {
        if (props.isSetLn()) {
            return new XDDFLineProperties(props.getLn());
        } else {
            return null;
        }
    }

    public void setLineProperties(XDDFLineProperties properties) {
        if (properties == null) {
            if (props.isSetLn()) {
                props.unsetLn();
            }
        } else {
            props.setLn(properties.getXmlObject());
        }
    }

    public XDDFCustomGeometry2D getCustomGeometry2D() {
        if (props.isSetCustGeom()) {
            return new XDDFCustomGeometry2D(props.getCustGeom());
        } else {
            return null;
        }
    }

    public void setCustomGeometry2D(XDDFCustomGeometry2D geometry) {
        if (geometry == null) {
            if (props.isSetCustGeom()) {
                props.unsetCustGeom();
            }
        } else {
            props.setCustGeom(geometry.getXmlObject());
        }
    }

    public XDDFPresetGeometry2D getPresetGeometry2D() {
        if (props.isSetPrstGeom()) {
            return new XDDFPresetGeometry2D(props.getPrstGeom());
        } else {
            return null;
        }
    }

    public void setPresetGeometry2D(XDDFPresetGeometry2D geometry) {
        if (geometry == null) {
            if (props.isSetPrstGeom()) {
                props.unsetPrstGeom();
            }
        } else {
            props.setPrstGeom(geometry.getXmlObject());
        }
    }

    public XDDFEffectContainer getEffectContainer() {
        if (props.isSetEffectDag()) {
            return new XDDFEffectContainer(props.getEffectDag());
        } else {
            return null;
        }
    }

    public void setEffectContainer(XDDFEffectContainer container) {
        if (container == null) {
            if (props.isSetEffectDag()) {
                props.unsetEffectDag();
            }
        } else {
            props.setEffectDag(container.getXmlObject());
        }
    }

    public XDDFEffectList getEffectList() {
        if (props.isSetEffectLst()) {
            return new XDDFEffectList(props.getEffectLst());
        } else {
            return null;
        }
    }

    public void setEffectList(XDDFEffectList list) {
        if (list == null) {
            if (props.isSetEffectLst()) {
                props.unsetEffectLst();
            }
        } else {
            props.setEffectLst(list.getXmlObject());
        }
    }

    public XDDFExtensionList getExtensionList() {
        if (props.isSetExtLst()) {
            return new XDDFExtensionList(props.getExtLst());
        } else {
            return null;
        }
    }

    public void setExtensionList(XDDFExtensionList list) {
        if (list == null) {
            if (props.isSetExtLst()) {
                props.unsetExtLst();
            }
        } else {
            props.setExtLst(list.getXmlObject());
        }
    }

    public XDDFScene3D getScene3D() {
        if (props.isSetScene3D()) {
            return new XDDFScene3D(props.getScene3D());
        } else {
            return null;
        }
    }

    public void setScene3D(XDDFScene3D scene) {
        if (scene == null) {
            if (props.isSetScene3D()) {
                props.unsetScene3D();
            }
        } else {
            props.setScene3D(scene.getXmlObject());
        }
    }

    public XDDFShape3D getShape3D() {
        if (props.isSetSp3D()) {
            return new XDDFShape3D(props.getSp3D());
        } else {
            return null;
        }
    }

    public void setShape3D(XDDFShape3D shape) {
        if (shape == null) {
            if (props.isSetSp3D()) {
                props.unsetSp3D();
            }
        } else {
            props.setSp3D(shape.getXmlObject());
        }
    }

    public XDDFTransform2D getTransform2D() {
        if (props.isSetXfrm()) {
            return new XDDFTransform2D(props.getXfrm());
        } else {
            return null;
        }
    }

    public void setTransform2D(XDDFTransform2D transform) {
        if (transform == null) {
            if (props.isSetXfrm()) {
                props.unsetXfrm();
            }
        } else {
            props.setXfrm(transform.getXmlObject());
        }
    }
}
