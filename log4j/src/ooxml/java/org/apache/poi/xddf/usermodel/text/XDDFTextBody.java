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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextListStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

@Beta
public class XDDFTextBody {
    private CTTextBody _body;
    private TextContainer _parent;

    public XDDFTextBody(TextContainer parent) {
        this(parent, CTTextBody.Factory.newInstance());
    }

    @Internal
    public XDDFTextBody(TextContainer parent, CTTextBody body) {
        this._parent = parent;
        this._body = body;
    }

    @Internal
    public CTTextBody getXmlObject() {
        return _body;
    }

    public TextContainer getParentShape() {
        return _parent;
    }

    public XDDFTextParagraph initialize() {
        _body.addNewLstStyle();
        _body.addNewBodyPr();
        XDDFBodyProperties bp = getBodyProperties();
        bp.setAnchoring(AnchorType.TOP);
        bp.setRightToLeft(false);
        XDDFTextParagraph p = addNewParagraph();
        p.setTextAlignment(TextAlignment.LEFT);
        p.appendRegularRun("");
        XDDFRunProperties end = p.addAfterLastRunProperties();
        end.setLanguage(Locale.US);
        end.setFontSize(11.0);
        return p;
    }

    public void setText(String text) {
        if (_body.sizeOfPArray() > 0) {
            // remove all but first paragraph
            for (int i = _body.sizeOfPArray() - 1; i > 0; i--) {
                _body.removeP(i);
            }
            getParagraph(0).setText(text);
        } else {
            // as there were no paragraphs yet, initialize the text body
            initialize().setText(text);
        }
    }

    public XDDFTextParagraph addNewParagraph() {
        return new XDDFTextParagraph(_body.addNewP(), this);
    }

    public XDDFTextParagraph insertNewParagraph(int index) {
        return new XDDFTextParagraph(_body.insertNewP(index), this);
    }

    public void removeParagraph(int index) {
        _body.removeP(index);
    }

    public XDDFTextParagraph getParagraph(int index) {
        return new XDDFTextParagraph(_body.getPArray(index), this);
    }

    public List<XDDFTextParagraph> getParagraphs() {
        return Collections.unmodifiableList(
            _body.getPList().stream().map(ds -> new XDDFTextParagraph(ds, this)).collect(Collectors.toList()));
    }

    public XDDFBodyProperties getBodyProperties() {
        return new XDDFBodyProperties(_body.getBodyPr());
    }

    public void setBodyProperties(XDDFBodyProperties properties) {
        if (properties == null) {
            _body.addNewBodyPr();
        } else {
            _body.setBodyPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getDefaultProperties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetDefPPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getDefPPr());
        } else {
            return null;
        }
    }

    public void setDefaultProperties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetDefPPr()) {
                    style.unsetDefPPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setDefPPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel1Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl1PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl1PPr());
        } else {
            return null;
        }
    }

    public void setLevel1Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl1PPr()) {
                    style.unsetLvl1PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl1PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel2Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl2PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl2PPr());
        } else {
            return null;
        }
    }

    public void setLevel2Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl2PPr()) {
                    style.unsetLvl2PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl2PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel3Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl3PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl3PPr());
        } else {
            return null;
        }
    }

    public void setLevel3Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl3PPr()) {
                    style.unsetLvl3PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl3PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel4Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl4PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl4PPr());
        } else {
            return null;
        }
    }

    public void setLevel4Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl4PPr()) {
                    style.unsetLvl4PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl4PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel5Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl5PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl5PPr());
        } else {
            return null;
        }
    }

    public void setLevel5Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl5PPr()) {
                    style.unsetLvl5PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl5PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel6Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl6PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl6PPr());
        } else {
            return null;
        }
    }

    public void setLevel6Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl6PPr()) {
                    style.unsetLvl6PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl6PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel7Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl7PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl7PPr());
        } else {
            return null;
        }
    }

    public void setLevel7Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl7PPr()) {
                    style.unsetLvl7PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl7PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel8Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl8PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl8PPr());
        } else {
            return null;
        }
    }

    public void setLevel8Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl8PPr()) {
                    style.unsetLvl8PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl8PPr(properties.getXmlObject());
        }
    }

    public XDDFParagraphProperties getLevel9Properties() {
        if (_body.isSetLstStyle() && _body.getLstStyle().isSetLvl9PPr()) {
            return new XDDFParagraphProperties(_body.getLstStyle().getLvl9PPr());
        } else {
            return null;
        }
    }

    public void setLevel9Properties(XDDFParagraphProperties properties) {
        if (properties == null) {
            if (_body.isSetLstStyle()) {
                CTTextListStyle style = _body.getLstStyle();
                if (style.isSetLvl9PPr()) {
                    style.unsetLvl9PPr();
                }
            }
        } else {
            CTTextListStyle style = _body.isSetLstStyle() ? _body.getLstStyle() : _body.addNewLstStyle();
            style.setLvl9PPr(properties.getXmlObject());
        }
    }

    @Internal
    protected <R> Optional<R> findDefinedParagraphProperty(Predicate<CTTextParagraphProperties> isSet,
        Function<CTTextParagraphProperties, R> getter, int level) {
        if (_body.isSetLstStyle() && level >= 0) {
            CTTextListStyle list = _body.getLstStyle();
            CTTextParagraphProperties props = level == 0 ? list.getDefPPr() : retrieveProperties(list, level);
            if (props != null && isSet.test(props)) {
                return Optional.of(getter.apply(props));
            } else {
                return findDefinedParagraphProperty(isSet, getter, level - 1);
            }
        } else if (_parent != null) {
            return _parent.findDefinedParagraphProperty(isSet, getter);
        } else {
            return Optional.empty();
        }
    }

    @Internal
    protected <R> Optional<R> findDefinedRunProperty(Predicate<CTTextCharacterProperties> isSet,
        Function<CTTextCharacterProperties, R> getter, int level) {
        if (_body.isSetLstStyle() && level >= 0) {
            CTTextListStyle list = _body.getLstStyle();
            CTTextParagraphProperties props = level == 0 ? list.getDefPPr() : retrieveProperties(list, level);
            if (props != null && props.isSetDefRPr() && isSet.test(props.getDefRPr())) {
                return Optional.of(getter.apply(props.getDefRPr()));
            } else {
                return findDefinedRunProperty(isSet, getter, level - 1);
            }
        } else if (_parent != null) {
            return _parent.findDefinedRunProperty(isSet, getter);
        } else {
            return Optional.empty();
        }
    }

    private CTTextParagraphProperties retrieveProperties(CTTextListStyle list, int level) {
        switch (level) {
        case 1:
            if (list.isSetLvl1PPr()) {
                return list.getLvl1PPr();
            } else {
                return null;
            }
        case 2:
            if (list.isSetLvl2PPr()) {
                return list.getLvl2PPr();
            } else {
                return null;
            }
        case 3:
            if (list.isSetLvl3PPr()) {
                return list.getLvl3PPr();
            } else {
                return null;
            }
        case 4:
            if (list.isSetLvl4PPr()) {
                return list.getLvl4PPr();
            } else {
                return null;
            }
        case 5:
            if (list.isSetLvl5PPr()) {
                return list.getLvl5PPr();
            } else {
                return null;
            }
        case 6:
            if (list.isSetLvl6PPr()) {
                return list.getLvl6PPr();
            } else {
                return null;
            }
        case 7:
            if (list.isSetLvl7PPr()) {
                return list.getLvl7PPr();
            } else {
                return null;
            }
        case 8:
            if (list.isSetLvl8PPr()) {
                return list.getLvl8PPr();
            } else {
                return null;
            }
        case 9:
            if (list.isSetLvl9PPr()) {
                return list.getLvl9PPr();
            } else {
                return null;
            }
        default:
            return null;
        }
    }
}
