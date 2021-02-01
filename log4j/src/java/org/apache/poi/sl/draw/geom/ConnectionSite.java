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

package org.apache.poi.sl.draw.geom;

import java.util.Objects;

/**
 * <p>Java class for CT_ConnectionSite complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_ConnectionSite"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ang" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_ConnectionSite", propOrder = {"pos"})
public final class ConnectionSite {

    // @XmlElement(required = true)
    private final AdjustPoint pos = new AdjustPoint();
    // @XmlAttribute(name = "ang", required = true)
    private String ang;

    /**
     * Gets the value of the pos property.
     *
     * @return
     *     possible object is
     *     {@link AdjustPoint }
     *
     */
    public AdjustPoint getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     *
     * @param pos
     *     allowed object is
     *     {@link AdjustPoint }
     *
     */
    public void setPos(AdjustPoint pos) {
        if (pos != null) {
            this.pos.setX(pos.getX());
            this.pos.setY(pos.getY());
        }
    }

    /**
     * Gets the value of the ang property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAng() {
        return ang;
    }

    /**
     * Sets the value of the ang property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAng(String value) {
        this.ang = value;
    }

    public boolean isSetAng() {
        return (this.ang!= null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionSite)) return false;
        ConnectionSite that = (ConnectionSite) o;
        return Objects.equals(pos, that.pos) &&
                Objects.equals(ang, that.ang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, ang);
    }
}
