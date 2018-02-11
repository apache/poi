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

package org.apache.poi.sl.draw.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SphereCoords complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SphereCoords"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="lat" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" /&gt;
 *       &lt;attribute name="lon" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" /&gt;
 *       &lt;attribute name="rev" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SphereCoords")
public class CTSphereCoords {

    @XmlAttribute(name = "lat", required = true)
    protected int lat;
    @XmlAttribute(name = "lon", required = true)
    protected int lon;
    @XmlAttribute(name = "rev", required = true)
    protected int rev;

    /**
     * Gets the value of the lat property.
     * 
     */
    public int getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     * 
     */
    public void setLat(int value) {
        this.lat = value;
    }

    public boolean isSetLat() {
        return true;
    }

    /**
     * Gets the value of the lon property.
     * 
     */
    public int getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     * 
     */
    public void setLon(int value) {
        this.lon = value;
    }

    public boolean isSetLon() {
        return true;
    }

    /**
     * Gets the value of the rev property.
     * 
     */
    public int getRev() {
        return rev;
    }

    /**
     * Sets the value of the rev property.
     * 
     */
    public void setRev(int value) {
        this.rev = value;
    }

    public boolean isSetRev() {
        return true;
    }

}
