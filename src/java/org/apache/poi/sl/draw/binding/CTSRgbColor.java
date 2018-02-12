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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_SRgbColor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SRgbColor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorTransform" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="val" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_HexBinary3" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SRgbColor", propOrder = {
    "egColorTransform"
})
public class CTSRgbColor {

    @XmlElementRefs({
        @XmlElementRef(name = "blue", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "gamma", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "blueOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "satOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "green", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "sat", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "greenOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "redOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "hueMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "alphaMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "comp", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "satMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "red", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "hue", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "alphaOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "gray", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "blueMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "alpha", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "lumOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "redMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "hueOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "greenMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "tint", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "invGamma", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "shade", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "lumMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "inv", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "lum", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> egColorTransform;
    @XmlAttribute(name = "val", required = true)
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] val;

    /**
     * Gets the value of the egColorTransform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egColorTransform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGColorTransform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAngle }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getEGColorTransform() {
        if (egColorTransform == null) {
            egColorTransform = new ArrayList<JAXBElement<?>>();
        }
        return this.egColorTransform;
    }

    public boolean isSetEGColorTransform() {
        return ((this.egColorTransform!= null)&&(!this.egColorTransform.isEmpty()));
    }

    public void unsetEGColorTransform() {
        this.egColorTransform = null;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVal(byte[] value) {
        this.val = value;
    }

    public boolean isSetVal() {
        return (this.val!= null);
    }

}
