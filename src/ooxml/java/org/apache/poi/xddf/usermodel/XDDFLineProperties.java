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
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;

@Beta
public class XDDFLineProperties {
    private CTLineProperties props;

    public XDDFLineProperties() {
        this(CTLineProperties.Factory.newInstance());
    }

    @Internal
    public XDDFLineProperties(CTLineProperties properties) {
        this.props = properties;
    }

    @Internal
    public CTLineProperties getXmlObject() {
        return props;
    }

    public PenAlignment getPenAlignment() {
        if (props.isSetAlgn()) {
            return PenAlignment.valueOf(props.getAlgn());
        } else {
            return null;
        }
    }

    public void setPenAlignment(PenAlignment alignment) {
        if (alignment == null) {
            if (props.isSetAlgn()) {
                props.unsetAlgn();
            }
        } else {
            props.setAlgn(alignment.underlying);
        }
    }

    public LineCap getLineCap() {
        if (props.isSetCap()) {
            return LineCap.valueOf(props.getCap());
        } else {
            return null;
        }
    }

    public void setLineCap(LineCap cap) {
        if (cap == null) {
            if (props.isSetCap()) {
                props.unsetCap();
            }
        } else {
            props.setCap(cap.underlying);
        }
    }

    public CompoundLine getCompoundLine() {
        if (props.isSetCmpd()) {
            return CompoundLine.valueOf(props.getCmpd());
        } else {
            return null;
        }
    }

    public void setCompoundLine(CompoundLine compound) {
        if (compound == null) {
            if (props.isSetCmpd()) {
                props.unsetCmpd();
            }
        } else {
            props.setCmpd(compound.underlying);
        }
    }

    public XDDFDashStop addDashStop() {
        if (!props.isSetCustDash()) {
            props.addNewCustDash();
        }
        return new XDDFDashStop(props.getCustDash().addNewDs());
    }

    public XDDFDashStop insertDashStop(int index) {
        if (!props.isSetCustDash()) {
            props.addNewCustDash();
        }
        return new XDDFDashStop(props.getCustDash().insertNewDs(index));
    }

    public void removeDashStop(int index) {
        if (props.isSetCustDash()) {
            props.getCustDash().removeDs(index);
        }
    }

    public XDDFDashStop getDashStop(int index) {
        if (props.isSetCustDash()) {
            return new XDDFDashStop(props.getCustDash().getDsArray(index));
        } else {
            return null;
        }
    }

    public List<XDDFDashStop> getDashStops() {
        if (props.isSetCustDash()) {
            return Collections.unmodifiableList(props
                .getCustDash()
                .getDsList()
                .stream()
                .map(ds -> new XDDFDashStop(ds))
                .collect(Collectors.toList()));
        } else {
            return Collections.emptyList();
        }
    }

    public int countDashStops() {
        if (props.isSetCustDash()) {
            return props.getCustDash().sizeOfDsArray();
        } else {
            return 0;
        }
    }

    public XDDFPresetLineDash getPresetDash() {
        if (props.isSetPrstDash()) {
            return new XDDFPresetLineDash(props.getPrstDash());
        } else {
            return null;
        }
    }

    public void setPresetDash(XDDFPresetLineDash properties) {
        if (properties == null) {
            if (props.isSetPrstDash()) {
                props.unsetPrstDash();
            }
        } else {
            props.setPrstDash(properties.getXmlObject());
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

    public XDDFFillProperties getFillProperties() {
        if (props.isSetGradFill()) {
            return new XDDFGradientFillProperties(props.getGradFill());
        } else if (props.isSetNoFill()) {
            return new XDDFNoFillProperties(props.getNoFill());
        } else if (props.isSetPattFill()) {
            return new XDDFPatternFillProperties(props.getPattFill());
        } else if (props.isSetSolidFill()) {
            return new XDDFSolidFillProperties(props.getSolidFill());
        } else {
            return null;
        }
    }

    public void setFillProperties(XDDFFillProperties properties) {
        if (props.isSetGradFill()) {
            props.unsetGradFill();
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
        } else if (properties instanceof XDDFNoFillProperties) {
            props.setNoFill(((XDDFNoFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFPatternFillProperties) {
            props.setPattFill(((XDDFPatternFillProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFSolidFillProperties) {
            props.setSolidFill(((XDDFSolidFillProperties) properties).getXmlObject());
        }
    }

    public XDDFLineJoinProperties getLineJoinProperties() {
        if (props.isSetBevel()) {
            return new XDDFLineJoinBevelProperties(props.getBevel());
        } else if (props.isSetMiter()) {
            return new XDDFLineJoinMiterProperties(props.getMiter());
        } else if (props.isSetRound()) {
            return new XDDFLineJoinRoundProperties(props.getRound());
        } else {
            return null;
        }
    }

    public void setLineJoinProperties(XDDFLineJoinProperties properties) {
        if (props.isSetBevel()) {
            props.unsetBevel();
        }
        if (props.isSetMiter()) {
            props.unsetMiter();
        }
        if (props.isSetRound()) {
            props.unsetRound();
        }
        if (properties == null) {
            return;
        }
        if (properties instanceof XDDFLineJoinBevelProperties) {
            props.setBevel(((XDDFLineJoinBevelProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFLineJoinMiterProperties) {
            props.setMiter(((XDDFLineJoinMiterProperties) properties).getXmlObject());
        } else if (properties instanceof XDDFLineJoinRoundProperties) {
            props.setRound(((XDDFLineJoinRoundProperties) properties).getXmlObject());
        }
    }

    public XDDFLineEndProperties getHeadEnd() {
        if (props.isSetHeadEnd()) {
            return new XDDFLineEndProperties(props.getHeadEnd());
        } else {
            return null;
        }
    }

    public void setHeadEnd(XDDFLineEndProperties properties) {
        if (properties == null) {
            if (props.isSetHeadEnd()) {
                props.unsetHeadEnd();
            }
        } else {
            props.setHeadEnd(properties.getXmlObject());
        }
    }

    public XDDFLineEndProperties getTailEnd() {
        if (props.isSetTailEnd()) {
            return new XDDFLineEndProperties(props.getTailEnd());
        } else {
            return null;
        }
    }

    public void setTailEnd(XDDFLineEndProperties properties) {
        if (properties == null) {
            if (props.isSetTailEnd()) {
                props.unsetTailEnd();
            }
        } else {
            props.setTailEnd(properties.getXmlObject());
        }
    }

    public Integer getWidth() {
        if (props.isSetW()) {
            return props.getW();
        } else {
            return null;
        }
    }

    public void setWidth(Integer width) {
        if (width == null) {
            if (props.isSetW()) {
                props.unsetW();
            }
        } else {
            props.setW(width);
        }
    }
}
