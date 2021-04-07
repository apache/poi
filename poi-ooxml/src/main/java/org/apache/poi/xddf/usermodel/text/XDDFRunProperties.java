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

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFEffectContainer;
import org.apache.poi.xddf.usermodel.XDDFEffectList;
import org.apache.poi.xddf.usermodel.XDDFExtensionList;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFGradientFillProperties;
import org.apache.poi.xddf.usermodel.XDDFGroupFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFNoFillProperties;
import org.apache.poi.xddf.usermodel.XDDFPatternFillProperties;
import org.apache.poi.xddf.usermodel.XDDFPictureFillProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;

@SuppressWarnings("unused")
@Beta
public class XDDFRunProperties {
    private CTTextCharacterProperties props;

    public XDDFRunProperties() {
        this(CTTextCharacterProperties.Factory.newInstance());
    }

    @Internal
    public XDDFRunProperties(CTTextCharacterProperties properties) {
        this.props = properties;
    }

    @Internal
    protected CTTextCharacterProperties getXmlObject() {
        return props;
    }

    public void setBaseline(Integer value) {
        update(props::isSetBaseline, props::unsetBaseline, props::setBaseline, value);
    }

    public void setDirty(Boolean dirty) {
        update(props::isSetDirty, props::unsetDirty, props::setDirty, dirty);
    }

    public void setSpellError(Boolean error) {
        update(props::isSetErr, props::unsetErr, props::setErr, error);
    }

    public void setNoProof(Boolean noproof) {
        update(props::isSetNoProof, props::unsetNoProof, props::setNoProof, noproof);
    }

    public void setNormalizeHeights(Boolean normalize) {
        update(props::isSetNormalizeH, props::unsetNormalizeH, props::setNormalizeH, normalize);
    }

    public void setKumimoji(Boolean kumimoji) {
        update(props::isSetKumimoji, props::unsetKumimoji, props::setKumimoji, kumimoji);
    }

    public void setBold(Boolean bold) {
        update(props::isSetB, props::unsetB, props::setB, bold);
    }

    public void setItalic(Boolean italic) {
        update(props::isSetI, props::unsetI, props::setI, italic);
    }

    public void setFontSize(Double size) {
        if (size != null && (size < 1 || 400 < size)) {
            throw new IllegalArgumentException("Minimum inclusive = 1. Maximum inclusive = 400.");
        }

        update(props::isSetSz, props::unsetSz, props::setSz, size == null ? null : (int)(100 * size));
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

    public void setCharacterKerning(Double kerning) {
        if (kerning != null && (kerning < 0 || 4000 < kerning)) {
            throw new IllegalArgumentException("Minimum inclusive = 0. Maximum inclusive = 4000.");
        }

        update(props::isSetKern, props::unsetKern, props::setKern, kerning == null ? null : (int)(100 * kerning));
    }

    public void setCharacterSpacing(Double spacing) {
        if (spacing != null && (spacing < -4000 || 4000 < spacing)) {
            throw new IllegalArgumentException("Minimum inclusive = -4000. Maximum inclusive = 4000.");
        }

        update(props::isSetSpc, props::unsetSpc, props::setSpc, spacing == null ? null : (int)(100 * spacing));
    }

    public void setFonts(XDDFFont[] fonts) {
        for (XDDFFont font : fonts) {
            CTTextFont xml = font.getXmlObject();
            switch (font.getGroup()) {
            case COMPLEX_SCRIPT:
                update(props::isSetCs, props::unsetCs, props::setCs, xml);
                break;
            case EAST_ASIAN:
                update(props::isSetEa, props::unsetEa, props::setEa, xml);
                break;
            case LATIN:
                update(props::isSetLatin, props::unsetLatin, props::setLatin, xml);
                break;
            case SYMBOL:
                update(props::isSetSym, props::unsetSym, props::setSym, xml);
                break;
            }
        }
    }

    public void setUnderline(UnderlineType underline) {
        update(props::isSetU, props::unsetU, props::setU, underline == null ? null : underline.underlying);
    }

    public void setStrikeThrough(StrikeType strike) {
        update(props::isSetStrike, props::unsetStrike, props::setStrike, strike == null ? null : strike.underlying);
    }

    public void setCapitals(CapsType caps) {
        update(props::isSetCap, props::unsetCap, props::setCap, caps == null ? null : caps.underlying);
    }

    public void setHyperlink(XDDFHyperlink link) {
        update(props::isSetHlinkClick, props::unsetHlinkClick, props::setHlinkClick, link == null ? null : link.getXmlObject());
    }

    public void setMouseOver(XDDFHyperlink link) {
        update(props::isSetHlinkMouseOver, props::unsetHlinkMouseOver, props::setHlinkMouseOver, link == null ? null : link.getXmlObject());
    }

    public void setLanguage(Locale lang) {
        update(props::isSetLang, props::unsetLang, props::setLang, lang == null ? null : lang.toLanguageTag());
    }

    public void setAlternativeLanguage(Locale lang) {
        update(props::isSetAltLang, props::unsetAltLang, props::setAltLang, lang == null ? null : lang.toLanguageTag());
    }

    public void setHighlight(XDDFColor color) {
        update(props::isSetHighlight, props::unsetHighlight, props::setHighlight, color == null ? null : color.getColorContainer());
    }

    public void setLineProperties(XDDFLineProperties properties) {
        update(props::isSetLn, props::unsetLn, props::setLn, properties == null ? null : properties.getXmlObject());
    }

    public void setBookmark(String bookmark) {
        update(props::isSetBmk, props::unsetBmk, props::setBmk, bookmark);
    }

    public XDDFExtensionList getExtensionList() {
        if (props.isSetExtLst()) {
            return new XDDFExtensionList(props.getExtLst());
        } else {
            return null;
        }
    }

    public void setExtensionList(XDDFExtensionList list) {
        update(props::isSetExtLst, props::unsetExtLst, props::setExtLst, list == null ? null : list.getXmlObject());
    }

    public XDDFEffectContainer getEffectContainer() {
        if (props.isSetEffectDag()) {
            return new XDDFEffectContainer(props.getEffectDag());
        } else {
            return null;
        }
    }

    public void setEffectContainer(XDDFEffectContainer container) {
        update(props::isSetEffectDag, props::unsetEffectDag, props::setEffectDag, container == null ? null : container.getXmlObject());
    }

    public XDDFEffectList getEffectList() {
        if (props.isSetEffectLst()) {
            return new XDDFEffectList(props.getEffectLst());
        } else {
            return null;
        }
    }

    public void setEffectList(XDDFEffectList list) {
        update(props::isSetEffectLst, props::unsetEffectLst, props::setEffectLst, list == null ? null : list.getXmlObject());
    }

    private static <T> void update(Supplier<Boolean> isSet, Runnable unset, Consumer<T> setter, T val) {
        if (val != null) {
            setter.accept(val);
        } else if (isSet.get()) {
            unset.run();
        }
    }
}
