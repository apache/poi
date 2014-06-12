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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A shared style sheet in a .pptx slide show
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFTheme extends POIXMLDocumentPart {
    private CTOfficeStyleSheet _theme;
    private Map<String, CTColor> _schemeColors;
    
    XSLFTheme() {
        super();
        _theme = prototype();
    }

    private static CTOfficeStyleSheet prototype(){
        CTOfficeStyleSheet ctOfficeStyleSheet = CTOfficeStyleSheet.Factory.newInstance();

        ctOfficeStyleSheet.setName("Office Theme");

        CTBaseStyles ctBaseStyles = ctOfficeStyleSheet.addNewThemeElements();

        CTColorScheme ctColorScheme = ctBaseStyles.addNewClrScheme();

        ctColorScheme.setName("Office");
        CTSystemColor ctSystemColor = ctColorScheme.addNewDk1().addNewSysClr();
        ctSystemColor.setLastClr(new byte[]{0, 0, 0});// 000000
        ctSystemColor.setVal(STSystemColorVal.WINDOW_TEXT);

        CTSystemColor ctSystemColor1 = ctColorScheme.addNewLt1().addNewSysClr();

        ctSystemColor1.setLastClr(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF});// FFFFFF
        ctSystemColor1.setVal(STSystemColorVal.WINDOW);

        ctColorScheme.addNewDk2().addNewSrgbClr().setVal(new byte[]{(byte) 0x1F, (byte) 0x49, (byte) 0x7D});// 1F497D
        ctColorScheme.addNewLt2().addNewSrgbClr().setVal(new byte[]{(byte) 0xEE, (byte) 0xEC, (byte) 0xE1});// EEECE1
        ctColorScheme.addNewAccent1().addNewSrgbClr().setVal(new byte[]{(byte) 0x4F, (byte) 0x81, (byte) 0xBD});// 4F81BD
        ctColorScheme.addNewAccent2().addNewSrgbClr().setVal(new byte[]{(byte) 0xC0, (byte) 0x50, (byte) 0x4D});// C0504D
        ctColorScheme.addNewAccent3().addNewSrgbClr().setVal(new byte[]{(byte) 0x9B, (byte) 0xBB, (byte) 0x59});// 9BBB59
        ctColorScheme.addNewAccent4().addNewSrgbClr().setVal(new byte[]{(byte) 0x80, (byte) 0x64, (byte) 0xA2});// 8064A2
        ctColorScheme.addNewAccent5().addNewSrgbClr().setVal(new byte[]{(byte) 0x4B, (byte) 0xAC, (byte) 0xC6});// 4BACC6
        ctColorScheme.addNewAccent6().addNewSrgbClr().setVal(new byte[]{(byte) 0xF7, (byte) 0x96, (byte) 0x46});// F79646

        ctColorScheme.addNewHlink().addNewSrgbClr().setVal(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x46});// 0000FF
        ctColorScheme.addNewFolHlink().addNewSrgbClr().setVal(new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x80});// 800080


        CTFontScheme ctFontScheme = ctBaseStyles.addNewFontScheme();
        ctFontScheme.setName("Office");

        CTFontCollection majorFontCollection = ctFontScheme.addNewMajorFont();
        majorFontCollection.addNewLatin().setTypeface("Calibri");
        majorFontCollection.addNewEa().setTypeface("");
        majorFontCollection.addNewCs().setTypeface("");

        CTFontCollection minorFontCollection = ctFontScheme.addNewMinorFont();
        minorFontCollection.addNewLatin().setTypeface("Calibri");
        minorFontCollection.addNewEa().setTypeface("");
        minorFontCollection.addNewCs().setTypeface("");

        CTStyleMatrix ctStyleMatrix = ctBaseStyles.addNewFmtScheme();
        ctStyleMatrix.setName("Office");


        CTFillStyleList ctFillStyleList = ctStyleMatrix.addNewFillStyleLst();

        ctFillStyleList.addNewSolidFill().addNewSchemeClr().setVal(STSchemeColorVal.PH_CLR);

        {
            CTGradientFillProperties ctGradientFillProperties = ctFillStyleList.addNewGradFill();
            ctGradientFillProperties.setRotWithShape(true);
            CTGradientStopList ctGradientStopList = ctGradientFillProperties.addNewGsLst();

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(0);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(50000);
                ctSchemeColor.addNewSatMod().setVal(300000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(35000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(37000);
                ctSchemeColor.addNewSatMod().setVal(300000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(100000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(15000);
                ctSchemeColor.addNewSatMod().setVal(350000);
            }

            CTLinearShadeProperties ctLinearShadeProperties = ctGradientFillProperties.addNewLin();
            ctLinearShadeProperties.setAng(16200000);
            ctLinearShadeProperties.setScaled(true);
        }

        {
            CTGradientFillProperties ctGradientFillProperties = ctFillStyleList.addNewGradFill();
            ctGradientFillProperties.setRotWithShape(true);
            CTGradientStopList ctGradientStopList = ctGradientFillProperties.addNewGsLst();

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(0);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(100000);
                ctSchemeColor.addNewShade().setVal(100000);
                ctSchemeColor.addNewSatMod().setVal(130000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(100000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(50000);
                ctSchemeColor.addNewShade().setVal(100000);
                ctSchemeColor.addNewSatMod().setVal(350000);
            }

            CTLinearShadeProperties ctLinearShadeProperties = ctGradientFillProperties.addNewLin();
            ctLinearShadeProperties.setAng(16200000);
            ctLinearShadeProperties.setScaled(false);
        }

        CTLineStyleList ctLineStyleList = ctStyleMatrix.addNewLnStyleLst();
        {
            CTLineProperties ctLineProperties = ctLineStyleList.addNewLn();
            ctLineProperties.setAlgn(STPenAlignment.CTR);
            ctLineProperties.setCap(STLineCap.FLAT);
            ctLineProperties.setCmpd(STCompoundLine.SNG);
            ctLineProperties.setW(9525);
            CTSchemeColor ctSchemeColor = ctLineProperties.addNewSolidFill().addNewSchemeClr();
            ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
            ctSchemeColor.addNewShade().setVal(95000);
            ctSchemeColor.addNewSatMod().setVal(105000);
            ctLineProperties.addNewPrstDash().setVal(STPresetLineDashVal.SOLID);
        }
        {
            CTLineProperties ctLineProperties = ctLineStyleList.addNewLn();
            ctLineProperties.setAlgn(STPenAlignment.CTR);
            ctLineProperties.setCap(STLineCap.FLAT);
            ctLineProperties.setCmpd(STCompoundLine.SNG);
            ctLineProperties.setW(25400);
            CTSchemeColor ctSchemeColor = ctLineProperties.addNewSolidFill().addNewSchemeClr();
            ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
            ctLineProperties.addNewPrstDash().setVal(STPresetLineDashVal.SOLID);
        }
        {
            CTLineProperties ctLineProperties = ctLineStyleList.addNewLn();
            ctLineProperties.setAlgn(STPenAlignment.CTR);
            ctLineProperties.setCap(STLineCap.FLAT);
            ctLineProperties.setCmpd(STCompoundLine.SNG);
            ctLineProperties.setW(38100);
            CTSchemeColor ctSchemeColor = ctLineProperties.addNewSolidFill().addNewSchemeClr();
            ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
            ctLineProperties.addNewPrstDash().setVal(STPresetLineDashVal.SOLID);
        }

        CTEffectStyleList ctEffectStyleList = ctStyleMatrix.addNewEffectStyleLst();
        {
            CTEffectList ctEffectList = ctEffectStyleList.addNewEffectStyle().addNewEffectLst();
            CTOuterShadowEffect ctOuterShadowEffect = ctEffectList.addNewOuterShdw();
            ctOuterShadowEffect.setBlurRad(40000);
            ctOuterShadowEffect.setDir(5400000);
            ctOuterShadowEffect.setDist(20000);
            ctOuterShadowEffect.setRotWithShape(false);
            CTSRgbColor ctsRgbColor = ctOuterShadowEffect.addNewSrgbClr();
            ctsRgbColor.setVal(new byte[]{0, 0, 0});// 000000
            ctsRgbColor.addNewAlpha().setVal(38000);
        }

        {
            CTEffectList ctEffectList = ctEffectStyleList.addNewEffectStyle().addNewEffectLst();
            CTOuterShadowEffect ctOuterShadowEffect = ctEffectList.addNewOuterShdw();
            ctOuterShadowEffect.setBlurRad(40000);
            ctOuterShadowEffect.setDir(5400000);
            ctOuterShadowEffect.setDist(23000);
            ctOuterShadowEffect.setRotWithShape(false);
            CTSRgbColor ctsRgbColor = ctOuterShadowEffect.addNewSrgbClr();
            ctsRgbColor.setVal(new byte[]{0, 0, 0});// 000000
            ctsRgbColor.addNewAlpha().setVal(35000);
        }

        {
            CTEffectStyleItem ctEffectStyleItem = ctEffectStyleList.addNewEffectStyle();
            {
                CTEffectList ctEffectList = ctEffectStyleItem.addNewEffectLst();
                CTOuterShadowEffect ctOuterShadowEffect = ctEffectList.addNewOuterShdw();
                ctOuterShadowEffect.setBlurRad(40000);
                ctOuterShadowEffect.setDir(5400000);
                ctOuterShadowEffect.setDist(23000);
                ctOuterShadowEffect.setRotWithShape(false);
                CTSRgbColor ctsRgbColor = ctOuterShadowEffect.addNewSrgbClr();
                ctsRgbColor.setVal(new byte[]{0, 0, 0});// 000000
                ctsRgbColor.addNewAlpha().setVal(35000);
            }
            {
                CTScene3D ctScene3D = ctEffectStyleItem.addNewScene3D();
                CTCamera ctCamera = ctScene3D.addNewCamera();
                ctCamera.setPrst(STPresetCameraType.ORTHOGRAPHIC_FRONT);
                CTSphereCoords ctSphereCoords = ctCamera.addNewRot();
                ctSphereCoords.setLat(0);
                ctSphereCoords.setLon(0);
                ctSphereCoords.setRev(0);

                CTLightRig ctLightRig = ctScene3D.addNewLightRig();
                ctLightRig.setDir(STLightRigDirection.T);
                ctLightRig.setRig(STLightRigType.THREE_PT);
                CTSphereCoords ctSphereCoords1 = ctLightRig.addNewRot();
                ctSphereCoords1.setLat(0);
                ctSphereCoords1.setLon(0);
                ctSphereCoords1.setRev(1200000);
            }
            {
                CTBevel ctBevel = ctEffectStyleItem.addNewSp3D().addNewBevelT();
                ctBevel.setH(25400);
                ctBevel.setW(63500);
            }
        }

        CTBackgroundFillStyleList bgFillStyleLst = ctStyleMatrix.addNewBgFillStyleLst();
        bgFillStyleLst.addNewSolidFill().addNewSchemeClr().setVal(STSchemeColorVal.PH_CLR);

        {
            CTGradientFillProperties ctGradientFillProperties = bgFillStyleLst.addNewGradFill();
            ctGradientFillProperties.setRotWithShape(true);
            CTGradientStopList ctGradientStopList = ctGradientFillProperties.addNewGsLst();

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(0);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(40000);
                ctSchemeColor.addNewSatMod().setVal(350000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(40000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(45000);
                ctSchemeColor.addNewShade().setVal(99000);
                ctSchemeColor.addNewSatMod().setVal(350000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(100000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewShade().setVal(20000);
                ctSchemeColor.addNewSatMod().setVal(255000);
            }
            CTPathShadeProperties ctPathShadeProperties = ctGradientFillProperties.addNewPath();
            ctPathShadeProperties.setPath(STPathShadeType.CIRCLE);
            CTRelativeRect ctRelativeRect = ctPathShadeProperties.addNewFillToRect();
            ctRelativeRect.setB(180000);
            ctRelativeRect.setL(50000);
            ctRelativeRect.setR(50000);
            ctRelativeRect.setT(-80000);
        }

        {
            CTGradientFillProperties ctGradientFillProperties = bgFillStyleLst.addNewGradFill();
            ctGradientFillProperties.setRotWithShape(true);
            CTGradientStopList ctGradientStopList = ctGradientFillProperties.addNewGsLst();

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(0);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewTint().setVal(80000);
                ctSchemeColor.addNewSatMod().setVal(300000);
            }

            {
                CTGradientStop ctGradientStop = ctGradientStopList.addNewGs();
                ctGradientStop.setPos(100000);
                CTSchemeColor ctSchemeColor = ctGradientStop.addNewSchemeClr();
                ctSchemeColor.setVal(STSchemeColorVal.PH_CLR);
                ctSchemeColor.addNewShade().setVal(30000);
                ctSchemeColor.addNewSatMod().setVal(200000);
            }
            CTPathShadeProperties ctPathShadeProperties = ctGradientFillProperties.addNewPath();
            ctPathShadeProperties.setPath(STPathShadeType.CIRCLE);
            CTRelativeRect ctRelativeRect = ctPathShadeProperties.addNewFillToRect();
            ctRelativeRect.setB(50000);
            ctRelativeRect.setL(50000);
            ctRelativeRect.setR(50000);
            ctRelativeRect.setT(50000);
        }

        return ctOfficeStyleSheet;
    }

    public XSLFTheme(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        ThemeDocument doc =
            ThemeDocument.Factory.parse(getPackagePart().getInputStream());
        _theme = doc.getTheme();
        initialize();
    }

    private void initialize(){
    	CTBaseStyles elems = _theme.getThemeElements();
    	CTColorScheme scheme = elems.getClrScheme();
    	// The color scheme is responsible for defining a list of twelve colors. 
    	_schemeColors = new HashMap<String, CTColor>(12);
    	for(XmlObject o : scheme.selectPath("*")){
    		CTColor c = (CTColor)o;
    		String name = c.getDomNode().getLocalName();
    		_schemeColors.put(name, c);
    	}
     }

    /**
     * re-map colors
     *
     * @param cmap color map defined in the master slide referencing this theme
     */
    void initColorMap(CTColorMapping cmap) {
        _schemeColors.put("bg1", _schemeColors.get(cmap.getBg1().toString()));
        _schemeColors.put("bg2", _schemeColors.get(cmap.getBg2().toString()));
        _schemeColors.put("tx1", _schemeColors.get(cmap.getTx1().toString()));
        _schemeColors.put("tx2", _schemeColors.get(cmap.getTx2().toString()));
    }

    /**
     *
     * @return name of this theme, e.g. "Office Theme"
     */
    public String getName(){
        return _theme.getName();
    }

    /**
     * Set name of this theme
     *
     * @param name name of this theme
     */
    public void setName(String name){
        _theme.setName(name);
    }

    /**
     * Get a color from the theme's color scheme by name
     * 
     * @return a theme color or <code>null</code> if not found
     */
    CTColor getCTColor(String name){
    	return _schemeColors.get(name);
    }
    
     /**
     * While developing only!
     */
    @Internal
    public CTOfficeStyleSheet getXmlObject() {
        return _theme;
    }

    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        Map<String, String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        xmlOptions.setSaveSuggestedPrefixes(map);
        xmlOptions.setSaveSyntheticDocumentElement(
                new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "theme"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    /**
     * @return typeface of the major font to use in a document.
     * Typically the major font is used for heading areas of a document.
     *
     */
    public String getMajorFont(){
        return _theme.getThemeElements().getFontScheme().getMajorFont().getLatin().getTypeface();
    }

    /**
     * @return typeface of the minor font to use in a document.
     * Typically the monor font is used for normal text or paragraph areas.
     *
     */
    public String getMinorFont(){
        return _theme.getThemeElements().getFontScheme().getMinorFont().getLatin().getTypeface();
    }


    CTTextParagraphProperties getDefaultParagraphStyle(){
        XmlObject[] o = _theme.selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' " +
                "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' " +
                ".//a:objectDefaults/a:spDef/a:lstStyle/a:defPPr");
        if(o.length == 1){
            return (CTTextParagraphProperties)o[0];
        }
        return null;
    }

}
