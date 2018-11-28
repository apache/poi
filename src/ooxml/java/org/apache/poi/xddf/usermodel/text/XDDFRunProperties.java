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

@Beta
public class XDDFRunProperties {
    private CTTextCharacterProperties props;

    public XDDFRunProperties() {
        this(CTTextCharacterProperties.Factory.newInstance());
    }

    @Internal
    protected XDDFRunProperties(CTTextCharacterProperties properties) {
        this.props = properties;
    }

    @Internal
    protected CTTextCharacterProperties getXmlObject() {
        return props;
    }

    public void setBaseline(Integer value) {
        if (value == null) {
            if (props.isSetBaseline()) {
                props.unsetBaseline();
            }
        } else {
            props.setBaseline(value);
        }
    }

    public void setDirty(Boolean dirty) {
        if (dirty == null) {
            if (props.isSetDirty()) {
                props.unsetDirty();
            }
        } else {
            props.setDirty(dirty);
        }
    }

    public void setSpellError(Boolean error) {
        if (error == null) {
            if (props.isSetErr()) {
                props.unsetErr();
            }
        } else {
            props.setErr(error);
        }
    }

    public void setNoProof(Boolean noproof) {
        if (noproof == null) {
            if (props.isSetNoProof()) {
                props.unsetNoProof();
            }
        } else {
            props.setNoProof(noproof);
        }
    }

    public void setNormalizeHeights(Boolean normalize) {
        if (normalize == null) {
            if (props.isSetNormalizeH()) {
                props.unsetNormalizeH();
            }
        } else {
            props.setNormalizeH(normalize);
        }
    }

    public void setKumimoji(Boolean kumimoji) {
        if (kumimoji == null) {
            if (props.isSetKumimoji()) {
                props.unsetKumimoji();
            }
        } else {
            props.setKumimoji(kumimoji);
        }
    }

    public void setBold(Boolean bold) {
        if (bold == null) {
            if (props.isSetB()) {
                props.unsetB();
            }
        } else {
            props.setB(bold);
        }
    }

    public void setItalic(Boolean italic) {
        if (italic == null) {
            if (props.isSetI()) {
                props.unsetI();
            }
        } else {
            props.setI(italic);
        }
    }

    public void setFontSize(Double size) {
        if (size == null) {
            if (props.isSetSz()) {
                props.unsetSz();
            }
        } else if (size < 1 || 400 < size) {
            throw new IllegalArgumentException("Minimum inclusive = 1. Maximum inclusive = 400.");
        } else {
            props.setSz((int) (100 * size));
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

    public void setCharacterKerning(Double kerning) {
        if (kerning == null) {
            if (props.isSetKern()) {
                props.unsetKern();
            }
        } else if (kerning < 0 || 4000 < kerning) {
            throw new IllegalArgumentException("Minimum inclusive = 0. Maximum inclusive = 4000.");
        } else {
            props.setKern((int) (100 * kerning));
        }
    }

    public void setCharacterSpacing(Double spacing) {
        if (spacing == null) {
            if (props.isSetSpc()) {
                props.unsetSpc();
            }
        } else if (spacing < -4000 || 4000 < spacing) {
            throw new IllegalArgumentException("Minimum inclusive = -4000. Maximum inclusive = 4000.");
        } else {
            props.setSpc((int) (100 * spacing));
        }
    }

    public void setFonts(XDDFFont[] fonts) {
        for (XDDFFont font : fonts) {
            CTTextFont xml = font.getXmlObject();
            switch (font.getGroup()) {
            case COMPLEX_SCRIPT:
                if (xml == null) {
                    if (props.isSetCs()) {
                        props.unsetCs();
                    }
                } else {
                    props.setCs(xml);
                }
            case EAST_ASIAN:
                if (xml == null) {
                    if (props.isSetEa()) {
                        props.unsetEa();
                    }
                } else {
                    props.setEa(xml);
                }
            case LATIN:
                if (xml == null) {
                    if (props.isSetLatin()) {
                        props.unsetLatin();
                    }
                } else {
                    props.setLatin(xml);
                }
            case SYMBOL:
                if (xml == null) {
                    if (props.isSetSym()) {
                        props.unsetSym();
                    }
                } else {
                    props.setSym(xml);
                }
            }
        }
    }

    public void setUnderline(UnderlineType underline) {
        if (underline == null) {
            if (props.isSetU()) {
                props.unsetU();
            }
        } else {
            props.setU(underline.underlying);
        }
    }

    public void setStrikeThrough(StrikeType strike) {
        if (strike == null) {
            if (props.isSetStrike()) {
                props.unsetStrike();
            }
        } else {
            props.setStrike(strike.underlying);
        }
    }

    public void setCapitals(CapsType caps) {
        if (caps == null) {
            if (props.isSetCap()) {
                props.unsetCap();
            }
        } else {
            props.setCap(caps.underlying);
        }
    }

    public void setHyperlink(XDDFHyperlink link) {
        if (link == null) {
            if (props.isSetHlinkClick()) {
                props.unsetHlinkClick();
            }
        } else {
            props.setHlinkClick(link.getXmlObject());
        }
    }

    public void setMouseOver(XDDFHyperlink link) {
        if (link == null) {
            if (props.isSetHlinkMouseOver()) {
                props.unsetHlinkMouseOver();
            }
        } else {
            props.setHlinkMouseOver(link.getXmlObject());
        }
    }

    public void setLanguage(Locale lang) {
        if (lang == null) {
            if (props.isSetLang()) {
                props.unsetLang();
            }
        } else {
            props.setLang(lang.toLanguageTag());
        }
    }

    public void setAlternativeLanguage(Locale lang) {
        if (lang == null) {
            if (props.isSetAltLang()) {
                props.unsetAltLang();
            }
        } else {
            props.setAltLang(lang.toLanguageTag());
        }
    }

    public void setHighlight(XDDFColor color) {
        if (color == null) {
            if (props.isSetHighlight()) {
                props.unsetHighlight();
            }
        } else {
            props.setHighlight(color.getColorContainer());
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

    public void setBookmark(String bookmark) {
        if (bookmark == null) {
            if (props.isSetBmk()) {
                props.unsetBmk();
            }
        } else {
            props.setBmk(bookmark);
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
}
