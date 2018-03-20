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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.poi.sl.draw.binding package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CTScRgbColorInv_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "inv");
    private final static QName _CTScRgbColorLumMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lumMod");
    private final static QName _CTScRgbColorBlue_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blue");
    private final static QName _CTScRgbColorRedMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "redMod");
    private final static QName _CTScRgbColorSatMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "satMod");
    private final static QName _CTScRgbColorHue_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hue");
    private final static QName _CTScRgbColorBlueOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blueOff");
    private final static QName _CTScRgbColorHueMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hueMod");
    private final static QName _CTScRgbColorAlphaMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alphaMod");
    private final static QName _CTScRgbColorGamma_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "gamma");
    private final static QName _CTScRgbColorComp_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "comp");
    private final static QName _CTScRgbColorSatOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "satOff");
    private final static QName _CTScRgbColorGreen_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "green");
    private final static QName _CTScRgbColorLumOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lumOff");
    private final static QName _CTScRgbColorSat_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "sat");
    private final static QName _CTScRgbColorShade_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "shade");
    private final static QName _CTScRgbColorRed_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "red");
    private final static QName _CTScRgbColorGray_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "gray");
    private final static QName _CTScRgbColorAlpha_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alpha");
    private final static QName _CTScRgbColorGreenOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "greenOff");
    private final static QName _CTScRgbColorRedOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "redOff");
    private final static QName _CTScRgbColorHueOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hueOff");
    private final static QName _CTScRgbColorLum_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lum");
    private final static QName _CTScRgbColorBlueMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blueMod");
    private final static QName _CTScRgbColorGreenMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "greenMod");
    private final static QName _CTScRgbColorAlphaOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alphaOff");
    private final static QName _CTScRgbColorTint_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "tint");
    private final static QName _CTScRgbColorInvGamma_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "invGamma");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.poi.sl.draw.binding
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPath2DArcTo }
     * 
     */
    public CTPath2DArcTo createCTPath2DArcTo() {
        return new CTPath2DArcTo();
    }

    /**
     * Create an instance of {@link CTAdjPoint2D }
     * 
     */
    public CTAdjPoint2D createCTAdjPoint2D() {
        return new CTAdjPoint2D();
    }

    /**
     * Create an instance of {@link CTColorMRU }
     * 
     */
    public CTColorMRU createCTColorMRU() {
        return new CTColorMRU();
    }

    /**
     * Create an instance of {@link CTConnection }
     * 
     */
    public CTConnection createCTConnection() {
        return new CTConnection();
    }

    /**
     * Create an instance of {@link CTInverseTransform }
     * 
     */
    public CTInverseTransform createCTInverseTransform() {
        return new CTInverseTransform();
    }

    /**
     * Create an instance of {@link CTPoint3D }
     * 
     */
    public CTPoint3D createCTPoint3D() {
        return new CTPoint3D();
    }

    /**
     * Create an instance of {@link CTPositiveSize2D }
     * 
     */
    public CTPositiveSize2D createCTPositiveSize2D() {
        return new CTPositiveSize2D();
    }

    /**
     * Create an instance of {@link CTColor }
     * 
     */
    public CTColor createCTColor() {
        return new CTColor();
    }

    /**
     * Create an instance of {@link CTComplementTransform }
     * 
     */
    public CTComplementTransform createCTComplementTransform() {
        return new CTComplementTransform();
    }

    /**
     * Create an instance of {@link CTCustomGeometry2D }
     * 
     */
    public CTCustomGeometry2D createCTCustomGeometry2D() {
        return new CTCustomGeometry2D();
    }

    /**
     * Create an instance of {@link CTRelativeRect }
     * 
     */
    public CTRelativeRect createCTRelativeRect() {
        return new CTRelativeRect();
    }

    /**
     * Create an instance of {@link CTHslColor }
     * 
     */
    public CTHslColor createCTHslColor() {
        return new CTHslColor();
    }

    /**
     * Create an instance of {@link CTPercentage }
     * 
     */
    public CTPercentage createCTPercentage() {
        return new CTPercentage();
    }

    /**
     * Create an instance of {@link CTInverseGammaTransform }
     * 
     */
    public CTInverseGammaTransform createCTInverseGammaTransform() {
        return new CTInverseGammaTransform();
    }

    /**
     * Create an instance of {@link CTPath2DMoveTo }
     * 
     */
    public CTPath2DMoveTo createCTPath2DMoveTo() {
        return new CTPath2DMoveTo();
    }

    /**
     * Create an instance of {@link CTPresetColor }
     * 
     */
    public CTPresetColor createCTPresetColor() {
        return new CTPresetColor();
    }

    /**
     * Create an instance of {@link CTGrayscaleTransform }
     * 
     */
    public CTGrayscaleTransform createCTGrayscaleTransform() {
        return new CTGrayscaleTransform();
    }

    /**
     * Create an instance of {@link CTPositiveFixedAngle }
     * 
     */
    public CTPositiveFixedAngle createCTPositiveFixedAngle() {
        return new CTPositiveFixedAngle();
    }

    /**
     * Create an instance of {@link CTPolarAdjustHandle }
     * 
     */
    public CTPolarAdjustHandle createCTPolarAdjustHandle() {
        return new CTPolarAdjustHandle();
    }

    /**
     * Create an instance of {@link CTPath2D }
     * 
     */
    public CTPath2D createCTPath2D() {
        return new CTPath2D();
    }

    /**
     * Create an instance of {@link CTPresetTextShape }
     * 
     */
    public CTPresetTextShape createCTPresetTextShape() {
        return new CTPresetTextShape();
    }

    /**
     * Create an instance of {@link CTAdjustHandleList }
     * 
     */
    public CTAdjustHandleList createCTAdjustHandleList() {
        return new CTAdjustHandleList();
    }

    /**
     * Create an instance of {@link CTScRgbColor }
     * 
     */
    public CTScRgbColor createCTScRgbColor() {
        return new CTScRgbColor();
    }

    /**
     * Create an instance of {@link CTPath2DClose }
     * 
     */
    public CTPath2DClose createCTPath2DClose() {
        return new CTPath2DClose();
    }

    /**
     * Create an instance of {@link CTPath2DLineTo }
     * 
     */
    public CTPath2DLineTo createCTPath2DLineTo() {
        return new CTPath2DLineTo();
    }

    /**
     * Create an instance of {@link CTPath2DList }
     * 
     */
    public CTPath2DList createCTPath2DList() {
        return new CTPath2DList();
    }

    /**
     * Create an instance of {@link CTAngle }
     * 
     */
    public CTAngle createCTAngle() {
        return new CTAngle();
    }

    /**
     * Create an instance of {@link CTSchemeColor }
     * 
     */
    public CTSchemeColor createCTSchemeColor() {
        return new CTSchemeColor();
    }

    /**
     * Create an instance of {@link CTGeomGuide }
     * 
     */
    public CTGeomGuide createCTGeomGuide() {
        return new CTGeomGuide();
    }

    /**
     * Create an instance of {@link CTGroupTransform2D }
     * 
     */
    public CTGroupTransform2D createCTGroupTransform2D() {
        return new CTGroupTransform2D();
    }

    /**
     * Create an instance of {@link CTConnectionSiteList }
     * 
     */
    public CTConnectionSiteList createCTConnectionSiteList() {
        return new CTConnectionSiteList();
    }

    /**
     * Create an instance of {@link CTConnectionSite }
     * 
     */
    public CTConnectionSite createCTConnectionSite() {
        return new CTConnectionSite();
    }

    /**
     * Create an instance of {@link CTXYAdjustHandle }
     * 
     */
    public CTXYAdjustHandle createCTXYAdjustHandle() {
        return new CTXYAdjustHandle();
    }

    /**
     * Create an instance of {@link CTSystemColor }
     * 
     */
    public CTSystemColor createCTSystemColor() {
        return new CTSystemColor();
    }

    /**
     * Create an instance of {@link CTPath2DCubicBezierTo }
     * 
     */
    public CTPath2DCubicBezierTo createCTPath2DCubicBezierTo() {
        return new CTPath2DCubicBezierTo();
    }

    /**
     * Create an instance of {@link CTPath2DQuadBezierTo }
     * 
     */
    public CTPath2DQuadBezierTo createCTPath2DQuadBezierTo() {
        return new CTPath2DQuadBezierTo();
    }

    /**
     * Create an instance of {@link CTRatio }
     * 
     */
    public CTRatio createCTRatio() {
        return new CTRatio();
    }

    /**
     * Create an instance of {@link CTGeomRect }
     * 
     */
    public CTGeomRect createCTGeomRect() {
        return new CTGeomRect();
    }

    /**
     * Create an instance of {@link CTGammaTransform }
     * 
     */
    public CTGammaTransform createCTGammaTransform() {
        return new CTGammaTransform();
    }

    /**
     * Create an instance of {@link CTPoint2D }
     * 
     */
    public CTPoint2D createCTPoint2D() {
        return new CTPoint2D();
    }

    /**
     * Create an instance of {@link CTSRgbColor }
     * 
     */
    public CTSRgbColor createCTSRgbColor() {
        return new CTSRgbColor();
    }

    /**
     * Create an instance of {@link CTTransform2D }
     * 
     */
    public CTTransform2D createCTTransform2D() {
        return new CTTransform2D();
    }

    /**
     * Create an instance of {@link CTPresetGeometry2D }
     * 
     */
    public CTPresetGeometry2D createCTPresetGeometry2D() {
        return new CTPresetGeometry2D();
    }

    /**
     * Create an instance of {@link CTOfficeArtExtension }
     * 
     */
    public CTOfficeArtExtension createCTOfficeArtExtension() {
        return new CTOfficeArtExtension();
    }

    /**
     * Create an instance of {@link CTGeomGuideList }
     * 
     */
    public CTGeomGuideList createCTGeomGuideList() {
        return new CTGeomGuideList();
    }

    /**
     * Create an instance of {@link CTScale2D }
     * 
     */
    public CTScale2D createCTScale2D() {
        return new CTScale2D();
    }

    /**
     * Create an instance of {@link CTPositiveFixedPercentage }
     * 
     */
    public CTPositiveFixedPercentage createCTPositiveFixedPercentage() {
        return new CTPositiveFixedPercentage();
    }

    /**
     * Create an instance of {@link CTSphereCoords }
     * 
     */
    public CTSphereCoords createCTSphereCoords() {
        return new CTSphereCoords();
    }

    /**
     * Create an instance of {@link CTPositivePercentage }
     * 
     */
    public CTPositivePercentage createCTPositivePercentage() {
        return new CTPositivePercentage();
    }

    /**
     * Create an instance of {@link CTVector3D }
     * 
     */
    public CTVector3D createCTVector3D() {
        return new CTVector3D();
    }

    /**
     * Create an instance of {@link CTEmbeddedWAVAudioFile }
     * 
     */
    public CTEmbeddedWAVAudioFile createCTEmbeddedWAVAudioFile() {
        return new CTEmbeddedWAVAudioFile();
    }

    /**
     * Create an instance of {@link CTFixedPercentage }
     * 
     */
    public CTFixedPercentage createCTFixedPercentage() {
        return new CTFixedPercentage();
    }

    /**
     * Create an instance of {@link CTHyperlink }
     * 
     */
    public CTHyperlink createCTHyperlink() {
        return new CTHyperlink();
    }

    /**
     * Create an instance of {@link CTOfficeArtExtensionList }
     * 
     */
    public CTOfficeArtExtensionList createCTOfficeArtExtensionList() {
        return new CTOfficeArtExtensionList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTScRgbColor.class)
    public JAXBElement<CTInverseTransform> createCTScRgbColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTScRgbColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTScRgbColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPositivePercentage> createCTScRgbColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPositivePercentage> createCTScRgbColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTScRgbColor.class)
    public JAXBElement<CTGammaTransform> createCTScRgbColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTScRgbColor.class)
    public JAXBElement<CTComplementTransform> createCTScRgbColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTScRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTScRgbColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTScRgbColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTScRgbColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTScRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTScRgbColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTScRgbColor.class)
    public JAXBElement<CTAngle> createCTScRgbColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTScRgbColor.class)
    public JAXBElement<CTPercentage> createCTScRgbColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTScRgbColor.class)
    public JAXBElement<CTFixedPercentage> createCTScRgbColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTScRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTScRgbColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTScRgbColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTScRgbColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTScRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTPresetColor.class)
    public JAXBElement<CTInverseTransform> createCTPresetColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTPresetColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTPresetColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTPresetColor.class)
    public JAXBElement<CTPositivePercentage> createCTPresetColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTPresetColor.class)
    public JAXBElement<CTPositivePercentage> createCTPresetColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTPresetColor.class)
    public JAXBElement<CTGammaTransform> createCTPresetColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTPresetColor.class)
    public JAXBElement<CTComplementTransform> createCTPresetColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTPresetColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTPresetColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTPresetColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTPresetColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTPresetColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTPresetColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTPresetColor.class)
    public JAXBElement<CTAngle> createCTPresetColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTPresetColor.class)
    public JAXBElement<CTPercentage> createCTPresetColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTPresetColor.class)
    public JAXBElement<CTFixedPercentage> createCTPresetColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTPresetColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTPresetColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTPresetColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTPresetColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTPresetColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTSchemeColor.class)
    public JAXBElement<CTInverseTransform> createCTSchemeColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTSchemeColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPositivePercentage> createCTSchemeColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPositivePercentage> createCTSchemeColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTSchemeColor.class)
    public JAXBElement<CTGammaTransform> createCTSchemeColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTSchemeColor.class)
    public JAXBElement<CTComplementTransform> createCTSchemeColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTSchemeColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTSchemeColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTSchemeColor.class)
    public JAXBElement<CTAngle> createCTSchemeColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTSchemeColor.class)
    public JAXBElement<CTFixedPercentage> createCTSchemeColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTSchemeColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTSchemeColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTSystemColor.class)
    public JAXBElement<CTInverseTransform> createCTSystemColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTSystemColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTSystemColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTSystemColor.class)
    public JAXBElement<CTPositivePercentage> createCTSystemColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTSystemColor.class)
    public JAXBElement<CTPositivePercentage> createCTSystemColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTSystemColor.class)
    public JAXBElement<CTGammaTransform> createCTSystemColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTSystemColor.class)
    public JAXBElement<CTComplementTransform> createCTSystemColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTSystemColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSystemColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTSystemColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTSystemColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTSystemColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSystemColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTSystemColor.class)
    public JAXBElement<CTAngle> createCTSystemColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTSystemColor.class)
    public JAXBElement<CTPercentage> createCTSystemColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTSystemColor.class)
    public JAXBElement<CTFixedPercentage> createCTSystemColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTSystemColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSystemColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTSystemColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTSystemColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTSystemColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTSRgbColor.class)
    public JAXBElement<CTInverseTransform> createCTSRgbColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTSRgbColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPositivePercentage> createCTSRgbColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPositivePercentage> createCTSRgbColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTSRgbColor.class)
    public JAXBElement<CTGammaTransform> createCTSRgbColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTSRgbColor.class)
    public JAXBElement<CTComplementTransform> createCTSRgbColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTSRgbColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTSRgbColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTSRgbColor.class)
    public JAXBElement<CTAngle> createCTSRgbColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTSRgbColor.class)
    public JAXBElement<CTFixedPercentage> createCTSRgbColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTSRgbColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTSRgbColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTHslColor.class)
    public JAXBElement<CTInverseTransform> createCTHslColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTScRgbColorInv_QNAME, CTInverseTransform.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumMod_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlue_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedMod_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatMod_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTHslColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTHslColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTScRgbColorHue_QNAME, CTPositiveFixedAngle.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueOff_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTHslColor.class)
    public JAXBElement<CTPositivePercentage> createCTHslColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorHueMod_QNAME, CTPositivePercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTHslColor.class)
    public JAXBElement<CTPositivePercentage> createCTHslColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTScRgbColorAlphaMod_QNAME, CTPositivePercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTHslColor.class)
    public JAXBElement<CTGammaTransform> createCTHslColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTScRgbColorGamma_QNAME, CTGammaTransform.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTHslColor.class)
    public JAXBElement<CTComplementTransform> createCTHslColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTScRgbColorComp_QNAME, CTComplementTransform.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSatOff_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreen_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLumOff_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorSat_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTHslColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTHslColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorShade_QNAME, CTPositiveFixedPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRed_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTHslColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTHslColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTScRgbColorGray_QNAME, CTGrayscaleTransform.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTHslColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTHslColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenOff_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorRedOff_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTHslColor.class)
    public JAXBElement<CTAngle> createCTHslColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTScRgbColorHueOff_QNAME, CTAngle.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorLum_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorBlueMod_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTHslColor.class)
    public JAXBElement<CTPercentage> createCTHslColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTScRgbColorGreenMod_QNAME, CTPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTHslColor.class)
    public JAXBElement<CTFixedPercentage> createCTHslColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTScRgbColorAlphaOff_QNAME, CTFixedPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTHslColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTHslColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTScRgbColorTint_QNAME, CTPositiveFixedPercentage.class, CTHslColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTHslColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTHslColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTScRgbColorInvGamma_QNAME, CTInverseGammaTransform.class, CTHslColor.class, value);
    }

}
