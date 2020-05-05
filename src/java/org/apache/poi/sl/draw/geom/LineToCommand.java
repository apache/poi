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

package org.apache.poi.sl.draw.geom;

import java.awt.geom.Path2D;
import java.util.Objects;

/**
 * <p>Java class for CT_Path2DLineTo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_Path2DLineTo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pt" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_Path2DLineTo", propOrder = {"pt"})
public final class LineToCommand implements PathCommand {

    // @XmlElement(required = true)
    private final AdjustPoint pt = new AdjustPoint();

    public AdjustPoint getPt() {
        return pt;
    }

    public void setPt(AdjustPoint pt) {
        if (pt != null) {
            this.pt.setX(pt.getX());
            this.pt.setY(pt.getY());
        }
    }

    @Override
    public void execute(Path2D.Double path, Context ctx){
        double x = ctx.getValue(pt.getX());
        double y = ctx.getValue(pt.getY());
        path.lineTo(x, y);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineToCommand)) return false;
        LineToCommand that = (LineToCommand) o;
        return Objects.equals(pt, that.pt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pt);
    }
}
