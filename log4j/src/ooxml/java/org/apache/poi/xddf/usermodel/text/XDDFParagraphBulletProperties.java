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

import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

@Beta
public class XDDFParagraphBulletProperties {
    private CTTextParagraphProperties props;

    @Internal
    protected XDDFParagraphBulletProperties(CTTextParagraphProperties properties) {
        this.props = properties;
    }

    public XDDFBulletStyle getBulletStyle() {
      if (props.isSetBuAutoNum()) {
          return new XDDFBulletStyleAutoNumbered(props.getBuAutoNum());
      } else if (props.isSetBuBlip()) {
          return new XDDFBulletStylePicture(props.getBuBlip());
      } else if (props.isSetBuChar()) {
          return new XDDFBulletStyleCharacter(props.getBuChar());
      } else if (props.isSetBuNone()) {
          return new XDDFBulletStyleNone(props.getBuNone());
      } else {
          return null;
      }
    }

    public void setBulletStyle(XDDFBulletStyle style) {
        if (props.isSetBuAutoNum()) {
            props.unsetBuAutoNum();
        }
        if (props.isSetBuBlip()) {
            props.unsetBuBlip();
        }
        if (props.isSetBuChar()) {
            props.unsetBuChar();
        }
        if (props.isSetBuNone()) {
            props.unsetBuNone();
        }
        if (style != null) {
            if (style instanceof XDDFBulletStyleAutoNumbered) {
                props.setBuAutoNum(((XDDFBulletStyleAutoNumbered) style).getXmlObject());
            } else if (style instanceof XDDFBulletStyleCharacter) {
                props.setBuChar(((XDDFBulletStyleCharacter) style).getXmlObject());
            } else if (style instanceof XDDFBulletStyleNone) {
                props.setBuNone(((XDDFBulletStyleNone) style).getXmlObject());
            } else if (style instanceof XDDFBulletStylePicture) {
                props.setBuBlip(((XDDFBulletStylePicture) style).getXmlObject());
            }
        }
    }

    public XDDFColor getBulletColor() {
        if (props.isSetBuClr()) {
            return XDDFColor.forColorContainer(props.getBuClr());
        } else {
            return null;
        }
    }

    public void setBulletColor(XDDFColor color) {
        if (props.isSetBuClrTx()) {
            props.unsetBuClrTx();
        }
        if (color == null) {
            if (props.isSetBuClr()) {
                props.unsetBuClr();
            }
        } else {
            props.setBuClr(color.getColorContainer());
        }
    }

    public void setBulletColorFollowText() {
        if (props.isSetBuClr()) {
            props.unsetBuClr();
        }
        if (props.isSetBuClrTx()) {
            // nothing to do: already set
        } else {
            props.addNewBuClrTx();
        }
    }

    public XDDFFont getBulletFont() {
        if (props.isSetBuFont()) {
            return new XDDFFont(FontGroup.SYMBOL, props.getBuFont());
        } else {
            return null;
        }
    }

    public void setBulletFont(XDDFFont font) {
        if (props.isSetBuFontTx()) {
            props.unsetBuFontTx();
        }
        if (font == null) {
            if (props.isSetBuFont()) {
                props.unsetBuFont();
            }
        } else {
            props.setBuFont(font.getXmlObject());
        }
    }

    public void setBulletFontFollowText() {
        if (props.isSetBuFont()) {
            props.unsetBuFont();
        }
        if (props.isSetBuFontTx()) {
            // nothing to do: already set
        } else {
            props.addNewBuFontTx();
        }
    }

    public XDDFBulletSize getBulletSize() {
      if (props.isSetBuSzPct()) {
          return new XDDFBulletSizePercent(props.getBuSzPct(), null);
      } else if (props.isSetBuSzPts()) {
          return new XDDFBulletSizePoints(props.getBuSzPts());
      } else if (props.isSetBuSzTx()) {
          return new XDDFBulletSizeFollowText(props.getBuSzTx());
      } else {
          return null;
      }
    }

    public void setBulletSize(XDDFBulletSize size) {
        if (props.isSetBuSzPct()) {
            props.unsetBuSzPct();
        }
        if (props.isSetBuSzPts()) {
            props.unsetBuSzPts();
        }
        if (props.isSetBuSzTx()) {
            props.unsetBuSzTx();
        }
        if (size != null) {
            if (size instanceof XDDFBulletSizeFollowText) {
                props.setBuSzTx(((XDDFBulletSizeFollowText) size).getXmlObject());
            } else if (size instanceof XDDFBulletSizePercent) {
                props.setBuSzPct(((XDDFBulletSizePercent) size).getXmlObject());
            } else if (size instanceof XDDFBulletSizePoints) {
                props.setBuSzPts(((XDDFBulletSizePoints) size).getXmlObject());
            }
        }
    }

    public void clearAll() {
        if (props.isSetBuAutoNum()) {
            props.unsetBuAutoNum();
        }
        if (props.isSetBuBlip()) {
            props.unsetBuBlip();
        }
        if (props.isSetBuChar()) {
            props.unsetBuChar();
        }
        if (props.isSetBuNone()) {
            props.unsetBuNone();
        }
        if (props.isSetBuClr()) {
            props.unsetBuClr();
        }
        if (props.isSetBuClrTx()) {
            props.unsetBuClrTx();
        }
        if (props.isSetBuFont()) {
            props.unsetBuFont();
        }
        if (props.isSetBuFontTx()) {
            props.unsetBuFontTx();
        }
        if (props.isSetBuSzPct()) {
            props.unsetBuSzPct();
        }
        if (props.isSetBuSzPts()) {
            props.unsetBuSzPts();
        }
        if (props.isSetBuSzTx()) {
            props.unsetBuSzTx();
        }
    }

    public CTTextParagraphProperties getXmlObject() {
        return props;
    }
}
