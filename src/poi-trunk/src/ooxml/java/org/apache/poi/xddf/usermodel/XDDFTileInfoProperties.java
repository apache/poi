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
import org.openxmlformats.schemas.drawingml.x2006.main.CTTileInfoProperties;

@Beta
public class XDDFTileInfoProperties {
    private CTTileInfoProperties props;

    protected XDDFTileInfoProperties(CTTileInfoProperties properties) {
        this.props = properties;
    }

    @Internal
    protected CTTileInfoProperties getXmlObject() {
        return props;
    }

    public void setAlignment(RectangleAlignment alignment) {
        if (alignment == null) {
            if (props.isSetAlgn()) {
                props.unsetAlgn();
            }
        } else {
            props.setAlgn(alignment.underlying);
        }
    }

    public TileFlipMode getFlipMode() {
        if (props.isSetFlip()) {
            return TileFlipMode.valueOf(props.getFlip());
        } else {
            return null;
        }
    }

    public void setFlipMode(TileFlipMode mode) {
        if (mode == null) {
            if (props.isSetFlip()) {
                props.unsetFlip();
            }
        } else {
            props.setFlip(mode.underlying);
        }
    }

    public Integer getSx() {
        if (props.isSetSx()) {
            return props.getSx();
        } else {
            return null;
        }
    }

    public void setSx(Integer value) {
        if (value == null) {
            if (props.isSetSx()) {
                props.unsetSx();
            }
        } else {
            props.setSx(value);
        }
    }

    public Integer getSy() {
        if (props.isSetSy()) {
            return props.getSy();
        } else {
            return null;
        }
    }

    public void setSy(Integer value) {
        if (value == null) {
            if (props.isSetSy()) {
                props.unsetSy();
            }
        } else {
            props.setSy(value);
        }
    }

    public Long getTx() {
        if (props.isSetTx()) {
            return props.getTx();
        } else {
            return null;
        }
    }

    public void setTx(Long value) {
        if (value == null) {
            if (props.isSetTx()) {
                props.unsetTx();
            }
        } else {
            props.setTx(value);
        }
    }

    public Long getTy() {
        if (props.isSetTy()) {
            return props.getTy();
        } else {
            return null;
        }
    }

    public void setTy(Long value) {
        if (value == null) {
            if (props.isSetTy()) {
                props.unsetTy();
            }
        } else {
            props.setTy(value);
        }
    }
}
