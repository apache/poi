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
 * <p>Java class for CT_Vector3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Vector3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="dx" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" /&gt;
 *       &lt;attribute name="dy" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" /&gt;
 *       &lt;attribute name="dz" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Vector3D")
public class CTVector3D {

    @XmlAttribute(name = "dx", required = true)
    protected long dx;
    @XmlAttribute(name = "dy", required = true)
    protected long dy;
    @XmlAttribute(name = "dz", required = true)
    protected long dz;

    /**
     * Gets the value of the dx property.
     * 
     */
    public long getDx() {
        return dx;
    }

    /**
     * Sets the value of the dx property.
     * 
     */
    public void setDx(long value) {
        this.dx = value;
    }

    public boolean isSetDx() {
        return true;
    }

    /**
     * Gets the value of the dy property.
     * 
     */
    public long getDy() {
        return dy;
    }

    /**
     * Sets the value of the dy property.
     * 
     */
    public void setDy(long value) {
        this.dy = value;
    }

    public boolean isSetDy() {
        return true;
    }

    /**
     * Gets the value of the dz property.
     * 
     */
    public long getDz() {
        return dz;
    }

    /**
     * Sets the value of the dz property.
     * 
     */
    public void setDz(long value) {
        this.dz = value;
    }

    public boolean isSetDz() {
        return true;
    }

}
