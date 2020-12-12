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

package org.apache.poi.xddf.usermodel.text;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.XDDFExtensionList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;

@Beta
public class XDDFBodyProperties {
    private CTTextBodyProperties props;

    @Internal
    protected XDDFBodyProperties(CTTextBodyProperties properties) {
        this.props = properties;
    }

    @Internal
    protected CTTextBodyProperties getXmlObject() {
        return props;
    }

    public AnchorType getAnchoring() {
        if (props.isSetAnchor()) {
            return AnchorType.valueOf(props.getAnchor());
        } else {
            return null;
        }
    }

    public void setAnchoring(AnchorType anchor) {
        if (anchor == null) {
            if (props.isSetAnchor()) {
                props.unsetAnchor();
            }
        } else {
            props.setAnchor(anchor.underlying);
        }
    }

    public Boolean isAnchorCentered() {
        if (props.isSetAnchorCtr()) {
            return props.getAnchorCtr();
        } else {
            return null;
        }
    }

    public void setAnchorCentered(Boolean centered) {
        if (centered == null) {
            if (props.isSetAnchorCtr()) {
                props.unsetAnchorCtr();
            }
        } else {
            props.setAnchorCtr(centered);
        }
    }

    public XDDFAutoFit getAutoFit() {
        if (props.isSetNoAutofit()) {
            return new XDDFNoAutoFit(props.getNoAutofit());
        } else if (props.isSetNormAutofit()) {
            return new XDDFNormalAutoFit(props.getNormAutofit());
        } else if (props.isSetSpAutoFit()) {
            return new XDDFShapeAutoFit(props.getSpAutoFit());
        }
        return new XDDFNormalAutoFit();
    }

    public void setAutoFit(XDDFAutoFit autofit) {
        if (props.isSetNoAutofit()) {
            props.unsetNoAutofit();
        }
        if (props.isSetNormAutofit()) {
            props.unsetNormAutofit();
        }
        if (props.isSetSpAutoFit()) {
            props.unsetSpAutoFit();
        }
        if (autofit instanceof XDDFNoAutoFit) {
            props.setNoAutofit(((XDDFNoAutoFit) autofit).getXmlObject());
        } else if (autofit instanceof XDDFNormalAutoFit) {
            props.setNormAutofit(((XDDFNormalAutoFit) autofit).getXmlObject());
        } else if (autofit instanceof XDDFShapeAutoFit) {
            props.setSpAutoFit(((XDDFShapeAutoFit) autofit).getXmlObject());
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

    public Double getBottomInset() {
        if (props.isSetBIns()) {
            return Units.toPoints(POIXMLUnits.parseLength(props.xgetBIns()));
        } else {
            return null;
        }
    }

    public void setBottomInset(Double points) {
        if (points == null || Double.isNaN(points)) {
            if (props.isSetBIns()) {
                props.unsetBIns();
            }
        } else {
            props.setBIns(Units.toEMU(points));
        }
    }

    public Double getLeftInset() {
        if (props.isSetLIns()) {
            return Units.toPoints(POIXMLUnits.parseLength(props.xgetLIns()));
        } else {
            return null;
        }
    }

    public void setLeftInset(Double points) {
        if (points == null || Double.isNaN(points)) {
            if (props.isSetLIns()) {
                props.unsetLIns();
            }
        } else {
            props.setLIns(Units.toEMU(points));
        }
    }

    public Double getRightInset() {
        if (props.isSetRIns()) {
            return Units.toPoints(POIXMLUnits.parseLength(props.xgetRIns()));
        } else {
            return null;
        }
    }

    public void setRightInset(Double points) {
        if (points == null || Double.isNaN(points)) {
            if (props.isSetRIns()) {
                props.unsetRIns();
            }
        } else {
            props.setRIns(Units.toEMU(points));
        }
    }

    public Double getTopInset() {
        if (props.isSetTIns()) {
            return Units.toPoints(POIXMLUnits.parseLength(props.xgetTIns()));
        } else {
            return null;
        }
    }

    public void setTopInset(Double points) {
        if (points == null || Double.isNaN(points)) {
            if (props.isSetTIns()) {
                props.unsetTIns();
            }
        } else {
            props.setTIns(Units.toEMU(points));
        }
    }

    public Boolean hasParagraphSpacing() {
        if (props.isSetSpcFirstLastPara()) {
            return props.getSpcFirstLastPara();
        } else {
            return null;
        }
    }

    public void setParagraphSpacing(Boolean spacing) {
        if (spacing == null) {
            if (props.isSetSpcFirstLastPara()) {
                props.unsetSpcFirstLastPara();
            }
        } else {
            props.setSpcFirstLastPara(spacing);
        }
    }

    public Boolean isRightToLeft() {
        if (props.isSetRtlCol()) {
            return props.getRtlCol();
        } else {
            return null;
        }
    }

    public void setRightToLeft(Boolean rightToLeft) {
        if (rightToLeft == null) {
            if (props.isSetRtlCol()) {
                props.unsetRtlCol();
            }
        } else {
            props.setRtlCol(rightToLeft);
        }
    }
}
