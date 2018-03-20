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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_PolarAdjustHandle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PolarAdjustHandle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="gdRefR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" /&gt;
 *       &lt;attribute name="minR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="maxR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="gdRefAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" /&gt;
 *       &lt;attribute name="minAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" /&gt;
 *       &lt;attribute name="maxAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PolarAdjustHandle", propOrder = {
    "pos"
})
public class CTPolarAdjustHandle {

    @XmlElement(required = true)
    protected CTAdjPoint2D pos;
    @XmlAttribute(name = "gdRefR")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefR;
    @XmlAttribute(name = "minR")
    protected String minR;
    @XmlAttribute(name = "maxR")
    protected String maxR;
    @XmlAttribute(name = "gdRefAng")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefAng;
    @XmlAttribute(name = "minAng")
    protected String minAng;
    @XmlAttribute(name = "maxAng")
    protected String maxAng;

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public CTAdjPoint2D getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public void setPos(CTAdjPoint2D value) {
        this.pos = value;
    }

    public boolean isSetPos() {
        return (this.pos!= null);
    }

    /**
     * Gets the value of the gdRefR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefR() {
        return gdRefR;
    }

    /**
     * Sets the value of the gdRefR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefR(String value) {
        this.gdRefR = value;
    }

    public boolean isSetGdRefR() {
        return (this.gdRefR!= null);
    }

    /**
     * Gets the value of the minR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinR() {
        return minR;
    }

    /**
     * Sets the value of the minR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinR(String value) {
        this.minR = value;
    }

    public boolean isSetMinR() {
        return (this.minR!= null);
    }

    /**
     * Gets the value of the maxR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxR() {
        return maxR;
    }

    /**
     * Sets the value of the maxR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxR(String value) {
        this.maxR = value;
    }

    public boolean isSetMaxR() {
        return (this.maxR!= null);
    }

    /**
     * Gets the value of the gdRefAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefAng() {
        return gdRefAng;
    }

    /**
     * Sets the value of the gdRefAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefAng(String value) {
        this.gdRefAng = value;
    }

    public boolean isSetGdRefAng() {
        return (this.gdRefAng!= null);
    }

    /**
     * Gets the value of the minAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinAng() {
        return minAng;
    }

    /**
     * Sets the value of the minAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinAng(String value) {
        this.minAng = value;
    }

    public boolean isSetMinAng() {
        return (this.minAng!= null);
    }

    /**
     * Gets the value of the maxAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxAng() {
        return maxAng;
    }

    /**
     * Sets the value of the maxAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxAng(String value) {
        this.maxAng = value;
    }

    public boolean isSetMaxAng() {
        return (this.maxAng!= null);
    }

}
