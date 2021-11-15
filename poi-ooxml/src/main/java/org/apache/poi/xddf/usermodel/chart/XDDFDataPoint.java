/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xddf.usermodel.chart;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDPt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;

/**
 * @since 5.1.0
 */
@Beta
public class XDDFDataPoint {
    private final CTDPt point;

    @Internal
    protected XDDFDataPoint(CTDPt point) {
        this.point = point;
    }

    public long getIndex() {
        return point.getIdx().getVal();
    }

    /**
     * @param fill
     *      fill property for the shape representing the point.
     */
    public void setFillProperties(XDDFFillProperties fill) {
        XDDFShapeProperties properties = getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setFillProperties(fill);
        setShapeProperties(properties);
    }

    /**
     * @param line
     *      line property for the shape representing the point.
     */
    public void setLineProperties(XDDFLineProperties line) {
        XDDFShapeProperties properties = getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setLineProperties(line);
        setShapeProperties(properties);
    }

    public XDDFShapeProperties getShapeProperties() {
        if (point.isSetSpPr()) {
            return new XDDFShapeProperties(point.getSpPr());
        } else {
            return null;
        }
    }

    public void setShapeProperties(XDDFShapeProperties properties) {
        if (properties == null) {
            if (point.isSetSpPr()) {
                point.unsetSpPr();
            }
        } else {
            if (point.isSetSpPr()) {
                point.setSpPr(properties.getXmlObject());
            } else {
                point.addNewSpPr().set(properties.getXmlObject());
            }
        }
    }

    public Long getExplosion() {
        if (point.isSetExplosion()) {
            return point.getExplosion().getVal();
        } else {
            return null;
        }
    }

    public void setExplosion(Long explosion) {
        if (explosion == null) {
            if (point.isSetExplosion()) {
                point.unsetExplosion();
            }
        } else {
            if (point.isSetExplosion()) {
                point.getExplosion().setVal(explosion);
            } else {
                point.addNewExplosion().setVal(explosion);
            }
        }
    }

    public boolean getInvertIfNegative() {
        if (point.isSetInvertIfNegative()) {
            return point.getInvertIfNegative().getVal();
        }
        return false;
    }

    public void setInvertIfNegative(boolean invertIfNegative) {
        if (point.isSetInvertIfNegative()) {
            point.getInvertIfNegative().setVal(invertIfNegative);
        } else {
            point.addNewInvertIfNegative().setVal(invertIfNegative);
        }
    }

    /**
     * @param size
     * <dl><dt>Minimum inclusive:</dt><dd>2</dd><dt>Maximum inclusive:</dt><dd>72</dd></dl>
     */
    public void setMarkerSize(short size) {
        if (size < 2 || 72 < size) {
            throw new IllegalArgumentException("Minimum inclusive: 2; Maximum inclusive: 72");
        }
        CTMarker marker = getMarker();
        if (marker.isSetSize()) {
            marker.getSize().setVal(size);
        } else {
            marker.addNewSize().setVal(size);
        }
    }

    public void setMarkerStyle(MarkerStyle style) {
        CTMarker marker = getMarker();
        if (marker.isSetSymbol()) {
            marker.getSymbol().setVal(style.underlying);
        } else {
            marker.addNewSymbol().setVal(style.underlying);
        }
    }

    private CTMarker getMarker() {
        if (point.isSetMarker()) {
            return point.getMarker();
        } else {
            return point.addNewMarker();
        }
    }

}
