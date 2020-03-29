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

package org.apache.poi.ddf;

import org.apache.poi.util.Removal;

/**
 * Provides a list of all known escher properties including the description and type.
 *
 * @deprecated use {@link EscherPropertyTypes} enum instead
 */
@SuppressWarnings({"unused","java:S115"})
@Deprecated
@Removal(version = "5.0.0")
public interface EscherProperties {
	short TRANSFORM__ROTATION = EscherPropertyTypes.TRANSFORM__ROTATION.propNumber;
	short PROTECTION__LOCKROTATION = EscherPropertyTypes.PROTECTION__LOCKROTATION.propNumber;
	short PROTECTION__LOCKASPECTRATIO = EscherPropertyTypes.PROTECTION__LOCKASPECTRATIO.propNumber;
	short PROTECTION__LOCKPOSITION = EscherPropertyTypes.PROTECTION__LOCKPOSITION.propNumber;
	short PROTECTION__LOCKAGAINSTSELECT = EscherPropertyTypes.PROTECTION__LOCKAGAINSTSELECT.propNumber;
	short PROTECTION__LOCKCROPPING = EscherPropertyTypes.PROTECTION__LOCKCROPPING.propNumber;
	short PROTECTION__LOCKVERTICES = EscherPropertyTypes.PROTECTION__LOCKVERTICES.propNumber;
	short PROTECTION__LOCKTEXT = EscherPropertyTypes.PROTECTION__LOCKTEXT.propNumber;
	short PROTECTION__LOCKADJUSTHANDLES = EscherPropertyTypes.PROTECTION__LOCKADJUSTHANDLES.propNumber;
	short PROTECTION__LOCKAGAINSTGROUPING = EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING.propNumber;
	short TEXT__TEXTID = EscherPropertyTypes.TEXT__TEXTID.propNumber;
	short TEXT__TEXTLEFT = EscherPropertyTypes.TEXT__TEXTLEFT.propNumber;
	short TEXT__TEXTTOP = EscherPropertyTypes.TEXT__TEXTTOP.propNumber;
	short TEXT__TEXTRIGHT = EscherPropertyTypes.TEXT__TEXTRIGHT.propNumber;
	short TEXT__TEXTBOTTOM = EscherPropertyTypes.TEXT__TEXTBOTTOM.propNumber;
	short TEXT__WRAPTEXT = EscherPropertyTypes.TEXT__WRAPTEXT.propNumber;
	short TEXT__SCALETEXT = EscherPropertyTypes.TEXT__SCALETEXT.propNumber;
	short TEXT__ANCHORTEXT = EscherPropertyTypes.TEXT__ANCHORTEXT.propNumber;
	short TEXT__TEXTFLOW = EscherPropertyTypes.TEXT__TEXTFLOW.propNumber;
	short TEXT__FONTROTATION = EscherPropertyTypes.TEXT__FONTROTATION.propNumber;
	short TEXT__IDOFNEXTSHAPE = EscherPropertyTypes.TEXT__IDOFNEXTSHAPE.propNumber;
	short TEXT__BIDIR = EscherPropertyTypes.TEXT__BIDIR.propNumber;
	short TEXT__SINGLECLICKSELECTS = EscherPropertyTypes.TEXT__SINGLECLICKSELECTS.propNumber;
	short TEXT__USEHOSTMARGINS = EscherPropertyTypes.TEXT__USEHOSTMARGINS.propNumber;
	short TEXT__ROTATETEXTWITHSHAPE = EscherPropertyTypes.TEXT__ROTATETEXTWITHSHAPE.propNumber;
	short TEXT__SIZESHAPETOFITTEXT = EscherPropertyTypes.TEXT__SIZESHAPETOFITTEXT.propNumber;
	short TEXT__SIZE_TEXT_TO_FIT_SHAPE = EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE.propNumber;
	short GEOTEXT__UNICODE = EscherPropertyTypes.GEOTEXT__UNICODE.propNumber;
	short GEOTEXT__RTFTEXT = EscherPropertyTypes.GEOTEXT__RTFTEXT.propNumber;
	short GEOTEXT__ALIGNMENTONCURVE = EscherPropertyTypes.GEOTEXT__ALIGNMENTONCURVE.propNumber;
	short GEOTEXT__DEFAULTPOINTSIZE = EscherPropertyTypes.GEOTEXT__DEFAULTPOINTSIZE.propNumber;
	short GEOTEXT__TEXTSPACING = EscherPropertyTypes.GEOTEXT__TEXTSPACING.propNumber;
	short GEOTEXT__FONTFAMILYNAME = EscherPropertyTypes.GEOTEXT__FONTFAMILYNAME.propNumber;
	short GEOTEXT__REVERSEROWORDER = EscherPropertyTypes.GEOTEXT__REVERSEROWORDER.propNumber;
	short GEOTEXT__HASTEXTEFFECT = EscherPropertyTypes.GEOTEXT__HASTEXTEFFECT.propNumber;
	short GEOTEXT__ROTATECHARACTERS = EscherPropertyTypes.GEOTEXT__ROTATECHARACTERS.propNumber;
	short GEOTEXT__KERNCHARACTERS = EscherPropertyTypes.GEOTEXT__KERNCHARACTERS.propNumber;
	short GEOTEXT__TIGHTORTRACK = EscherPropertyTypes.GEOTEXT__TIGHTORTRACK.propNumber;
	short GEOTEXT__STRETCHTOFITSHAPE = EscherPropertyTypes.GEOTEXT__STRETCHTOFITSHAPE.propNumber;
	short GEOTEXT__CHARBOUNDINGBOX = EscherPropertyTypes.GEOTEXT__CHARBOUNDINGBOX.propNumber;
	short GEOTEXT__SCALETEXTONPATH = EscherPropertyTypes.GEOTEXT__SCALETEXTONPATH.propNumber;
	short GEOTEXT__STRETCHCHARHEIGHT = EscherPropertyTypes.GEOTEXT__STRETCHCHARHEIGHT.propNumber;
	short GEOTEXT__NOMEASUREALONGPATH = EscherPropertyTypes.GEOTEXT__NOMEASUREALONGPATH.propNumber;
	short GEOTEXT__BOLDFONT = EscherPropertyTypes.GEOTEXT__BOLDFONT.propNumber;
	short GEOTEXT__ITALICFONT = EscherPropertyTypes.GEOTEXT__ITALICFONT.propNumber;
	short GEOTEXT__UNDERLINEFONT = EscherPropertyTypes.GEOTEXT__UNDERLINEFONT.propNumber;
	short GEOTEXT__SHADOWFONT = EscherPropertyTypes.GEOTEXT__SHADOWFONT.propNumber;
	short GEOTEXT__SMALLCAPSFONT = EscherPropertyTypes.GEOTEXT__SMALLCAPSFONT.propNumber;
	short GEOTEXT__STRIKETHROUGHFONT = EscherPropertyTypes.GEOTEXT__STRIKETHROUGHFONT.propNumber;
	short BLIP__CROPFROMTOP = EscherPropertyTypes.BLIP__CROPFROMTOP.propNumber;
	short BLIP__CROPFROMBOTTOM = EscherPropertyTypes.BLIP__CROPFROMBOTTOM.propNumber;
	short BLIP__CROPFROMLEFT = EscherPropertyTypes.BLIP__CROPFROMLEFT.propNumber;
	short BLIP__CROPFROMRIGHT = EscherPropertyTypes.BLIP__CROPFROMRIGHT.propNumber;
	short BLIP__BLIPTODISPLAY = EscherPropertyTypes.BLIP__BLIPTODISPLAY.propNumber;
	short BLIP__BLIPFILENAME = EscherPropertyTypes.BLIP__BLIPFILENAME.propNumber;
	short BLIP__BLIPFLAGS = EscherPropertyTypes.BLIP__BLIPFLAGS.propNumber;
	short BLIP__TRANSPARENTCOLOR = EscherPropertyTypes.BLIP__TRANSPARENTCOLOR.propNumber;
	short BLIP__CONTRASTSETTING = EscherPropertyTypes.BLIP__CONTRASTSETTING.propNumber;
	short BLIP__BRIGHTNESSSETTING = EscherPropertyTypes.BLIP__BRIGHTNESSSETTING.propNumber;
	short BLIP__GAMMA = EscherPropertyTypes.BLIP__GAMMA.propNumber;
	short BLIP__PICTUREID = EscherPropertyTypes.BLIP__PICTUREID.propNumber;
	short BLIP__DOUBLEMOD = EscherPropertyTypes.BLIP__DOUBLEMOD.propNumber;
	short BLIP__PICTUREFILLMOD = EscherPropertyTypes.BLIP__PICTUREFILLMOD.propNumber;
	short BLIP__PICTURELINE = EscherPropertyTypes.BLIP__PICTURELINE.propNumber;
	short BLIP__PRINTBLIP = EscherPropertyTypes.BLIP__PRINTBLIP.propNumber;
	short BLIP__PRINTBLIPFILENAME = EscherPropertyTypes.BLIP__PRINTBLIPFILENAME.propNumber;
	short BLIP__PRINTFLAGS = EscherPropertyTypes.BLIP__PRINTFLAGS.propNumber;
	short BLIP__NOHITTESTPICTURE = EscherPropertyTypes.BLIP__NOHITTESTPICTURE.propNumber;
	short BLIP__PICTUREGRAY = EscherPropertyTypes.BLIP__PICTUREGRAY.propNumber;
	short BLIP__PICTUREBILEVEL = EscherPropertyTypes.BLIP__PICTUREBILEVEL.propNumber;
	short BLIP__PICTUREACTIVE = EscherPropertyTypes.BLIP__PICTUREACTIVE.propNumber;
	short GEOMETRY__LEFT = EscherPropertyTypes.GEOMETRY__LEFT.propNumber;
	short GEOMETRY__TOP = EscherPropertyTypes.GEOMETRY__TOP.propNumber;
	short GEOMETRY__RIGHT = EscherPropertyTypes.GEOMETRY__RIGHT.propNumber;
	short GEOMETRY__BOTTOM = EscherPropertyTypes.GEOMETRY__BOTTOM.propNumber;
	short GEOMETRY__SHAPEPATH = EscherPropertyTypes.GEOMETRY__SHAPEPATH.propNumber;
	short GEOMETRY__VERTICES = EscherPropertyTypes.GEOMETRY__VERTICES.propNumber;
	short GEOMETRY__SEGMENTINFO = EscherPropertyTypes.GEOMETRY__SEGMENTINFO.propNumber;
	short GEOMETRY__ADJUSTVALUE = EscherPropertyTypes.GEOMETRY__ADJUSTVALUE.propNumber;
	short GEOMETRY__ADJUST2VALUE = EscherPropertyTypes.GEOMETRY__ADJUST2VALUE.propNumber;
	short GEOMETRY__ADJUST3VALUE = EscherPropertyTypes.GEOMETRY__ADJUST3VALUE.propNumber;
	short GEOMETRY__ADJUST4VALUE = EscherPropertyTypes.GEOMETRY__ADJUST4VALUE.propNumber;
	short GEOMETRY__ADJUST5VALUE = EscherPropertyTypes.GEOMETRY__ADJUST5VALUE.propNumber;
	short GEOMETRY__ADJUST6VALUE = EscherPropertyTypes.GEOMETRY__ADJUST6VALUE.propNumber;
	short GEOMETRY__ADJUST7VALUE = EscherPropertyTypes.GEOMETRY__ADJUST7VALUE.propNumber;
	short GEOMETRY__ADJUST8VALUE = EscherPropertyTypes.GEOMETRY__ADJUST8VALUE.propNumber;
	short GEOMETRY__ADJUST9VALUE = EscherPropertyTypes.GEOMETRY__ADJUST9VALUE.propNumber;
	short GEOMETRY__ADJUST10VALUE = EscherPropertyTypes.GEOMETRY__ADJUST10VALUE.propNumber;
	short GEOMETRY__PCONNECTIONSITES = EscherPropertyTypes.GEOMETRY__PCONNECTIONSITES.propNumber;
	short GEOMETRY__PCONNECTIONSITESDIR = EscherPropertyTypes.GEOMETRY__PCONNECTIONSITESDIR.propNumber;
	short GEOMETRY__XLIMO = EscherPropertyTypes.GEOMETRY__XLIMO.propNumber;
	short GEOMETRY__YLIMO = EscherPropertyTypes.GEOMETRY__YLIMO.propNumber;
	short GEOMETRY__PADJUSTHANDLES = EscherPropertyTypes.GEOMETRY__PADJUSTHANDLES.propNumber;
	short GEOMETRY__PGUIDES = EscherPropertyTypes.GEOMETRY__PGUIDES.propNumber;
	short GEOMETRY__PINSCRIBE = EscherPropertyTypes.GEOMETRY__PINSCRIBE.propNumber;
	short GEOMETRY__CXK = EscherPropertyTypes.GEOMETRY__CXK.propNumber;
	short GEOMETRY__PFRAGMENTS = EscherPropertyTypes.GEOMETRY__PFRAGMENTS.propNumber;
	short GEOMETRY__SHADOWok = EscherPropertyTypes.GEOMETRY__SHADOWOK.propNumber;
	short GEOMETRY__3DOK = EscherPropertyTypes.GEOMETRY__3DOK.propNumber;
	short GEOMETRY__LINEOK = EscherPropertyTypes.GEOMETRY__LINEOK.propNumber;
	short GEOMETRY__GEOTEXTOK = EscherPropertyTypes.GEOMETRY__GEOTEXTOK.propNumber;
	short GEOMETRY__FILLSHADESHAPEOK = EscherPropertyTypes.GEOMETRY__FILLSHADESHAPEOK.propNumber;
	short GEOMETRY__FILLOK = EscherPropertyTypes.GEOMETRY__FILLOK.propNumber;
	short FILL__FILLTYPE = EscherPropertyTypes.FILL__FILLTYPE.propNumber;
	short FILL__FILLCOLOR = EscherPropertyTypes.FILL__FILLCOLOR.propNumber;
	short FILL__FILLOPACITY = EscherPropertyTypes.FILL__FILLOPACITY.propNumber;
	short FILL__FILLBACKCOLOR = EscherPropertyTypes.FILL__FILLBACKCOLOR.propNumber;
	short FILL__BACKOPACITY = EscherPropertyTypes.FILL__BACKOPACITY.propNumber;
	short FILL__CRMOD = EscherPropertyTypes.FILL__CRMOD.propNumber;
	short FILL__PATTERNTEXTURE = EscherPropertyTypes.FILL__PATTERNTEXTURE.propNumber;
	short FILL__BLIPFILENAME = EscherPropertyTypes.FILL__BLIPFILENAME.propNumber;
	short FILL__BLIPFLAGS = EscherPropertyTypes.FILL__BLIPFLAGS.propNumber;
	short FILL__WIDTH = EscherPropertyTypes.FILL__WIDTH.propNumber;
	short FILL__HEIGHT = EscherPropertyTypes.FILL__HEIGHT.propNumber;
	short FILL__ANGLE = EscherPropertyTypes.FILL__ANGLE.propNumber;
	short FILL__FOCUS = EscherPropertyTypes.FILL__FOCUS.propNumber;
	short FILL__TOLEFT = EscherPropertyTypes.FILL__TOLEFT.propNumber;
	short FILL__TOTOP = EscherPropertyTypes.FILL__TOTOP.propNumber;
	short FILL__TORIGHT = EscherPropertyTypes.FILL__TORIGHT.propNumber;
	short FILL__TOBOTTOM = EscherPropertyTypes.FILL__TOBOTTOM.propNumber;
	short FILL__RECTLEFT = EscherPropertyTypes.FILL__RECTLEFT.propNumber;
	short FILL__RECTTOP = EscherPropertyTypes.FILL__RECTTOP.propNumber;
	short FILL__RECTRIGHT = EscherPropertyTypes.FILL__RECTRIGHT.propNumber;
	short FILL__RECTBOTTOM = EscherPropertyTypes.FILL__RECTBOTTOM.propNumber;
	short FILL__DZTYPE = EscherPropertyTypes.FILL__DZTYPE.propNumber;
	short FILL__SHADEPRESET = EscherPropertyTypes.FILL__SHADEPRESET.propNumber;
	short FILL__SHADECOLORS = EscherPropertyTypes.FILL__SHADECOLORS.propNumber;
	short FILL__ORIGINX = EscherPropertyTypes.FILL__ORIGINX.propNumber;
	short FILL__ORIGINY = EscherPropertyTypes.FILL__ORIGINY.propNumber;
	short FILL__SHAPEORIGINX = EscherPropertyTypes.FILL__SHAPEORIGINX.propNumber;
	short FILL__SHAPEORIGINY = EscherPropertyTypes.FILL__SHAPEORIGINY.propNumber;
	short FILL__SHADETYPE = EscherPropertyTypes.FILL__SHADETYPE.propNumber;
	short FILL__FILLED = EscherPropertyTypes.FILL__FILLED.propNumber;
	short FILL__HITTESTFILL = EscherPropertyTypes.FILL__HITTESTFILL.propNumber;
	short FILL__SHAPE = EscherPropertyTypes.FILL__SHAPE.propNumber;
	short FILL__USERECT = EscherPropertyTypes.FILL__USERECT.propNumber;
	short FILL__NOFILLHITTEST = EscherPropertyTypes.FILL__NOFILLHITTEST.propNumber;
	short LINESTYLE__COLOR = EscherPropertyTypes.LINESTYLE__COLOR.propNumber;
	short LINESTYLE__OPACITY = EscherPropertyTypes.LINESTYLE__OPACITY.propNumber;
	short LINESTYLE__BACKCOLOR = EscherPropertyTypes.LINESTYLE__BACKCOLOR.propNumber;
	short LINESTYLE__CRMOD = EscherPropertyTypes.LINESTYLE__CRMOD.propNumber;
	short LINESTYLE__LINETYPE = EscherPropertyTypes.LINESTYLE__LINETYPE.propNumber;
	short LINESTYLE__FILLBLIP = EscherPropertyTypes.LINESTYLE__FILLBLIP.propNumber;
	short LINESTYLE__FILLBLIPNAME = EscherPropertyTypes.LINESTYLE__FILLBLIPNAME.propNumber;
	short LINESTYLE__FILLBLIPFLAGS = EscherPropertyTypes.LINESTYLE__FILLBLIPFLAGS.propNumber;
	short LINESTYLE__FILLWIDTH = EscherPropertyTypes.LINESTYLE__FILLWIDTH.propNumber;
	short LINESTYLE__FILLHEIGHT = EscherPropertyTypes.LINESTYLE__FILLHEIGHT.propNumber;
	short LINESTYLE__FILLDZTYPE = EscherPropertyTypes.LINESTYLE__FILLDZTYPE.propNumber;
	short LINESTYLE__LINEWIDTH = EscherPropertyTypes.LINESTYLE__LINEWIDTH.propNumber;
	short LINESTYLE__LINEMITERLIMIT = EscherPropertyTypes.LINESTYLE__LINEMITERLIMIT.propNumber;
	short LINESTYLE__LINESTYLE = EscherPropertyTypes.LINESTYLE__LINESTYLE.propNumber;
	short LINESTYLE__LINEDASHING = EscherPropertyTypes.LINESTYLE__LINEDASHING.propNumber;
	short LINESTYLE__LINEDASHSTYLE = EscherPropertyTypes.LINESTYLE__LINEDASHSTYLE.propNumber;
	short LINESTYLE__LINESTARTARROWHEAD = EscherPropertyTypes.LINESTYLE__LINESTARTARROWHEAD.propNumber;
	short LINESTYLE__LINEENDARROWHEAD = EscherPropertyTypes.LINESTYLE__LINEENDARROWHEAD.propNumber;
	short LINESTYLE__LINESTARTARROWWIDTH = EscherPropertyTypes.LINESTYLE__LINESTARTARROWWIDTH.propNumber;
	short LINESTYLE__LINESTARTARROWLENGTH = EscherPropertyTypes.LINESTYLE__LINESTARTARROWLENGTH.propNumber;
	short LINESTYLE__LINEENDARROWWIDTH = EscherPropertyTypes.LINESTYLE__LINEENDARROWWIDTH.propNumber;
	short LINESTYLE__LINEENDARROWLENGTH = EscherPropertyTypes.LINESTYLE__LINEENDARROWLENGTH.propNumber;
	short LINESTYLE__LINEJOINSTYLE = EscherPropertyTypes.LINESTYLE__LINEJOINSTYLE.propNumber;
	short LINESTYLE__LINEENDCAPSTYLE = EscherPropertyTypes.LINESTYLE__LINEENDCAPSTYLE.propNumber;
	short LINESTYLE__ARROWHEADSOK = EscherPropertyTypes.LINESTYLE__ARROWHEADSOK.propNumber;
	short LINESTYLE__ANYLINE = EscherPropertyTypes.LINESTYLE__ANYLINE.propNumber;
	short LINESTYLE__HITLINETEST = EscherPropertyTypes.LINESTYLE__HITLINETEST.propNumber;
	short LINESTYLE__LINEFILLSHAPE = EscherPropertyTypes.LINESTYLE__LINEFILLSHAPE.propNumber;
	short LINESTYLE__NOLINEDRAWDASH = EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH.propNumber;
	short LINESTYLE__NOLINEDRAWDASH_LEFT = EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH_LEFT.propNumber;
	short LINESTYLE__NOLINEDRAWDASH_TOP = EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH_TOP.propNumber;
	short LINESTYLE__NOLINEDRAWDASH_BOTTOM = EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH_BOTTOM.propNumber;
	short LINESTYLE__NOLINEDRAWDASH_RIGHT = EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH_RIGHT.propNumber;
	short SHADOWSTYLE__TYPE = EscherPropertyTypes.SHADOWSTYLE__TYPE.propNumber;
	short SHADOWSTYLE__COLOR = EscherPropertyTypes.SHADOWSTYLE__COLOR.propNumber;
	short SHADOWSTYLE__HIGHLIGHT = EscherPropertyTypes.SHADOWSTYLE__HIGHLIGHT.propNumber;
	short SHADOWSTYLE__CRMOD = EscherPropertyTypes.SHADOWSTYLE__CRMOD.propNumber;
	short SHADOWSTYLE__OPACITY = EscherPropertyTypes.SHADOWSTYLE__OPACITY.propNumber;
	short SHADOWSTYLE__OFFSETX = EscherPropertyTypes.SHADOWSTYLE__OFFSETX.propNumber;
	short SHADOWSTYLE__OFFSETY = EscherPropertyTypes.SHADOWSTYLE__OFFSETY.propNumber;
	short SHADOWSTYLE__SECONDOFFSETX = EscherPropertyTypes.SHADOWSTYLE__SECONDOFFSETX.propNumber;
	short SHADOWSTYLE__SECONDOFFSETY = EscherPropertyTypes.SHADOWSTYLE__SECONDOFFSETY.propNumber;
	short SHADOWSTYLE__SCALEXTOX = EscherPropertyTypes.SHADOWSTYLE__SCALEXTOX.propNumber;
	short SHADOWSTYLE__SCALEYTOX = EscherPropertyTypes.SHADOWSTYLE__SCALEYTOX.propNumber;
	short SHADOWSTYLE__SCALEXTOY = EscherPropertyTypes.SHADOWSTYLE__SCALEXTOY.propNumber;
	short SHADOWSTYLE__SCALEYTOY = EscherPropertyTypes.SHADOWSTYLE__SCALEYTOY.propNumber;
	short SHADOWSTYLE__PERSPECTIVEX = EscherPropertyTypes.SHADOWSTYLE__PERSPECTIVEX.propNumber;
	short SHADOWSTYLE__PERSPECTIVEY = EscherPropertyTypes.SHADOWSTYLE__PERSPECTIVEY.propNumber;
	short SHADOWSTYLE__WEIGHT = EscherPropertyTypes.SHADOWSTYLE__WEIGHT.propNumber;
	short SHADOWSTYLE__ORIGINX = EscherPropertyTypes.SHADOWSTYLE__ORIGINX.propNumber;
	short SHADOWSTYLE__ORIGINY = EscherPropertyTypes.SHADOWSTYLE__ORIGINY.propNumber;
	short SHADOWSTYLE__SHADOW = EscherPropertyTypes.SHADOWSTYLE__SHADOW.propNumber;
	short SHADOWSTYLE__SHADOWOBSURED = EscherPropertyTypes.SHADOWSTYLE__SHADOWOBSURED.propNumber;
	short PERSPECTIVE__TYPE = EscherPropertyTypes.PERSPECTIVE__TYPE.propNumber;
	short PERSPECTIVE__OFFSETX = EscherPropertyTypes.PERSPECTIVE__OFFSETX.propNumber;
	short PERSPECTIVE__OFFSETY = EscherPropertyTypes.PERSPECTIVE__OFFSETY.propNumber;
	short PERSPECTIVE__SCALEXTOX = EscherPropertyTypes.PERSPECTIVE__SCALEXTOX.propNumber;
	short PERSPECTIVE__SCALEYTOX = EscherPropertyTypes.PERSPECTIVE__SCALEYTOX.propNumber;
	short PERSPECTIVE__SCALEXTOY = EscherPropertyTypes.PERSPECTIVE__SCALEXTOY.propNumber;
	short PERSPECTIVE__SCALEYTOY = EscherPropertyTypes.PERSPECTIVE__SCALEYTOY.propNumber;
	short PERSPECTIVE__PERSPECTIVEX = EscherPropertyTypes.PERSPECTIVE__PERSPECTIVEX.propNumber;
	short PERSPECTIVE__PERSPECTIVEY = EscherPropertyTypes.PERSPECTIVE__PERSPECTIVEY.propNumber;
	short PERSPECTIVE__WEIGHT = EscherPropertyTypes.PERSPECTIVE__WEIGHT.propNumber;
	short PERSPECTIVE__ORIGINX = EscherPropertyTypes.PERSPECTIVE__ORIGINX.propNumber;
	short PERSPECTIVE__ORIGINY = EscherPropertyTypes.PERSPECTIVE__ORIGINY.propNumber;
	short PERSPECTIVE__PERSPECTIVEON = EscherPropertyTypes.PERSPECTIVE__PERSPECTIVEON.propNumber;
	short THREED__SPECULARAMOUNT = EscherPropertyTypes.THREED__SPECULARAMOUNT.propNumber;
	short THREED__DIFFUSEAMOUNT = EscherPropertyTypes.THREED__DIFFUSEAMOUNT.propNumber;
	short THREED__SHININESS = EscherPropertyTypes.THREED__SHININESS.propNumber;
	short THREED__EDGETHICKNESS = EscherPropertyTypes.THREED__EDGETHICKNESS.propNumber;
	short THREED__EXTRUDEFORWARD = EscherPropertyTypes.THREED__EXTRUDEFORWARD.propNumber;
	short THREED__EXTRUDEBACKWARD = EscherPropertyTypes.THREED__EXTRUDEBACKWARD.propNumber;
	short THREED__EXTRUDEPLANE = EscherPropertyTypes.THREED__EXTRUDEPLANE.propNumber;
	short THREED__EXTRUSIONCOLOR = EscherPropertyTypes.THREED__EXTRUSIONCOLOR.propNumber;
	short THREED__CRMOD = EscherPropertyTypes.THREED__CRMOD.propNumber;
	short THREED__3DEFFECT = EscherPropertyTypes.THREED__3DEFFECT.propNumber;
	short THREED__METALLIC = EscherPropertyTypes.THREED__METALLIC.propNumber;
	short THREED__USEEXTRUSIONCOLOR = EscherPropertyTypes.THREED__USEEXTRUSIONCOLOR.propNumber;
	short THREED__LIGHTFACE = EscherPropertyTypes.THREED__LIGHTFACE.propNumber;
	short THREEDSTYLE__YROTATIONANGLE = EscherPropertyTypes.THREEDSTYLE__YROTATIONANGLE.propNumber;
	short THREEDSTYLE__XROTATIONANGLE = EscherPropertyTypes.THREEDSTYLE__XROTATIONANGLE.propNumber;
	short THREEDSTYLE__ROTATIONAXISX = EscherPropertyTypes.THREEDSTYLE__ROTATIONAXISX.propNumber;
	short THREEDSTYLE__ROTATIONAXISY = EscherPropertyTypes.THREEDSTYLE__ROTATIONAXISY.propNumber;
	short THREEDSTYLE__ROTATIONAXISZ = EscherPropertyTypes.THREEDSTYLE__ROTATIONAXISZ.propNumber;
	short THREEDSTYLE__ROTATIONANGLE = EscherPropertyTypes.THREEDSTYLE__ROTATIONANGLE.propNumber;
	short THREEDSTYLE__ROTATIONCENTERX = EscherPropertyTypes.THREEDSTYLE__ROTATIONCENTERX.propNumber;
	short THREEDSTYLE__ROTATIONCENTERY = EscherPropertyTypes.THREEDSTYLE__ROTATIONCENTERY.propNumber;
	short THREEDSTYLE__ROTATIONCENTERZ = EscherPropertyTypes.THREEDSTYLE__ROTATIONCENTERZ.propNumber;
	short THREEDSTYLE__RENDERMODE = EscherPropertyTypes.THREEDSTYLE__RENDERMODE.propNumber;
	short THREEDSTYLE__TOLERANCE = EscherPropertyTypes.THREEDSTYLE__TOLERANCE.propNumber;
	short THREEDSTYLE__XVIEWPOINT = EscherPropertyTypes.THREEDSTYLE__XVIEWPOINT.propNumber;
	short THREEDSTYLE__YVIEWPOINT = EscherPropertyTypes.THREEDSTYLE__YVIEWPOINT.propNumber;
	short THREEDSTYLE__ZVIEWPOINT = EscherPropertyTypes.THREEDSTYLE__ZVIEWPOINT.propNumber;
	short THREEDSTYLE__ORIGINX = EscherPropertyTypes.THREEDSTYLE__ORIGINX.propNumber;
	short THREEDSTYLE__ORIGINY = EscherPropertyTypes.THREEDSTYLE__ORIGINY.propNumber;
	short THREEDSTYLE__SKEWANGLE = EscherPropertyTypes.THREEDSTYLE__SKEWANGLE.propNumber;
	short THREEDSTYLE__SKEWAMOUNT = EscherPropertyTypes.THREEDSTYLE__SKEWAMOUNT.propNumber;
	short THREEDSTYLE__AMBIENTINTENSITY = EscherPropertyTypes.THREEDSTYLE__AMBIENTINTENSITY.propNumber;
	short THREEDSTYLE__KEYX = EscherPropertyTypes.THREEDSTYLE__KEYX.propNumber;
	short THREEDSTYLE__KEYY = EscherPropertyTypes.THREEDSTYLE__KEYY.propNumber;
	short THREEDSTYLE__KEYZ = EscherPropertyTypes.THREEDSTYLE__KEYZ.propNumber;
	short THREEDSTYLE__KEYINTENSITY = EscherPropertyTypes.THREEDSTYLE__KEYINTENSITY.propNumber;
	short THREEDSTYLE__FILLX = EscherPropertyTypes.THREEDSTYLE__FILLX.propNumber;
	short THREEDSTYLE__FILLY = EscherPropertyTypes.THREEDSTYLE__FILLY.propNumber;
	short THREEDSTYLE__FILLZ = EscherPropertyTypes.THREEDSTYLE__FILLZ.propNumber;
	short THREEDSTYLE__FILLINTENSITY = EscherPropertyTypes.THREEDSTYLE__FILLINTENSITY.propNumber;
	short THREEDSTYLE__CONSTRAINROTATION = EscherPropertyTypes.THREEDSTYLE__CONSTRAINROTATION.propNumber;
	short THREEDSTYLE__ROTATIONCENTERAUTO = EscherPropertyTypes.THREEDSTYLE__ROTATIONCENTERAUTO.propNumber;
	short THREEDSTYLE__PARALLEL = EscherPropertyTypes.THREEDSTYLE__PARALLEL.propNumber;
	short THREEDSTYLE__KEYHARSH = EscherPropertyTypes.THREEDSTYLE__KEYHARSH.propNumber;
	short THREEDSTYLE__FILLHARSH = EscherPropertyTypes.THREEDSTYLE__FILLHARSH.propNumber;
	short SHAPE__MASTER = EscherPropertyTypes.SHAPE__MASTER.propNumber;
	short SHAPE__CONNECTORSTYLE = EscherPropertyTypes.SHAPE__CONNECTORSTYLE.propNumber;
	short SHAPE__BLACKANDWHITESETTINGS = EscherPropertyTypes.SHAPE__BLACKANDWHITESETTINGS.propNumber;
	short SHAPE__WMODEPUREBW = EscherPropertyTypes.SHAPE__WMODEPUREBW.propNumber;
	short SHAPE__WMODEBW = EscherPropertyTypes.SHAPE__WMODEBW.propNumber;
	short SHAPE__OLEICON = EscherPropertyTypes.SHAPE__OLEICON.propNumber;
	short SHAPE__PREFERRELATIVERESIZE = EscherPropertyTypes.SHAPE__PREFERRELATIVERESIZE.propNumber;
	short SHAPE__LOCKSHAPETYPE = EscherPropertyTypes.SHAPE__LOCKSHAPETYPE.propNumber;
	short SHAPE__DELETEATTACHEDOBJECT = EscherPropertyTypes.SHAPE__DELETEATTACHEDOBJECT.propNumber;
	short SHAPE__BACKGROUNDSHAPE = EscherPropertyTypes.SHAPE__BACKGROUNDSHAPE.propNumber;
	short CALLOUT__CALLOUTTYPE = EscherPropertyTypes.CALLOUT__CALLOUTTYPE.propNumber;
	short CALLOUT__XYCALLOUTGAP = EscherPropertyTypes.CALLOUT__XYCALLOUTGAP.propNumber;
	short CALLOUT__CALLOUTANGLE = EscherPropertyTypes.CALLOUT__CALLOUTANGLE.propNumber;
	short CALLOUT__CALLOUTDROPTYPE = EscherPropertyTypes.CALLOUT__CALLOUTDROPTYPE.propNumber;
	short CALLOUT__CALLOUTDROPSPECIFIED = EscherPropertyTypes.CALLOUT__CALLOUTDROPSPECIFIED.propNumber;
	short CALLOUT__CALLOUTLENGTHSPECIFIED = EscherPropertyTypes.CALLOUT__CALLOUTLENGTHSPECIFIED.propNumber;
	short CALLOUT__ISCALLOUT = EscherPropertyTypes.CALLOUT__ISCALLOUT.propNumber;
	short CALLOUT__CALLOUTACCENTBAR = EscherPropertyTypes.CALLOUT__CALLOUTACCENTBAR.propNumber;
	short CALLOUT__CALLOUTTEXTBORDER = EscherPropertyTypes.CALLOUT__CALLOUTTEXTBORDER.propNumber;
	short CALLOUT__CALLOUTMINUSX = EscherPropertyTypes.CALLOUT__CALLOUTMINUSX.propNumber;
	short CALLOUT__CALLOUTMINUSY = EscherPropertyTypes.CALLOUT__CALLOUTMINUSY.propNumber;
	short CALLOUT__DROPAUTO = EscherPropertyTypes.CALLOUT__DROPAUTO.propNumber;
	short CALLOUT__LENGTHSPECIFIED = EscherPropertyTypes.CALLOUT__LENGTHSPECIFIED.propNumber;
	short GROUPSHAPE__SHAPENAME = EscherPropertyTypes.GROUPSHAPE__SHAPENAME.propNumber;
	short GROUPSHAPE__DESCRIPTION = EscherPropertyTypes.GROUPSHAPE__DESCRIPTION.propNumber;
	short GROUPSHAPE__HYPERLINK = EscherPropertyTypes.GROUPSHAPE__HYPERLINK.propNumber;
	short GROUPSHAPE__WRAPPOLYGONVERTICES = EscherPropertyTypes.GROUPSHAPE__WRAPPOLYGONVERTICES.propNumber;
	short GROUPSHAPE__WRAPDISTLEFT = EscherPropertyTypes.GROUPSHAPE__WRAPDISTLEFT.propNumber;
	short GROUPSHAPE__WRAPDISTTOP = EscherPropertyTypes.GROUPSHAPE__WRAPDISTTOP.propNumber;
	short GROUPSHAPE__WRAPDISTRIGHT = EscherPropertyTypes.GROUPSHAPE__WRAPDISTRIGHT.propNumber;
	short GROUPSHAPE__WRAPDISTBOTTOM = EscherPropertyTypes.GROUPSHAPE__WRAPDISTBOTTOM.propNumber;
	short GROUPSHAPE__REGROUPID = EscherPropertyTypes.GROUPSHAPE__REGROUPID.propNumber;
	short GROUPSHAPE__UNUSED906 = EscherPropertyTypes.GROUPSHAPE__UNUSED906.propNumber;
	short GROUPSHAPE__TOOLTIP = EscherPropertyTypes.GROUPSHAPE__TOOLTIP.propNumber;
	short GROUPSHAPE__SCRIPT = EscherPropertyTypes.GROUPSHAPE__SCRIPT.propNumber;
	short GROUPSHAPE__POSH = EscherPropertyTypes.GROUPSHAPE__POSH.propNumber;
	short GROUPSHAPE__POSRELH = EscherPropertyTypes.GROUPSHAPE__POSRELH.propNumber;
	short GROUPSHAPE__POSV = EscherPropertyTypes.GROUPSHAPE__POSV.propNumber;
	short GROUPSHAPE__POSRELV = EscherPropertyTypes.GROUPSHAPE__POSRELV.propNumber;
	short GROUPSHAPE__HR_PCT = EscherPropertyTypes.GROUPSHAPE__HR_PCT.propNumber;
	short GROUPSHAPE__HR_ALIGN = EscherPropertyTypes.GROUPSHAPE__HR_ALIGN.propNumber;
	short GROUPSHAPE__HR_HEIGHT = EscherPropertyTypes.GROUPSHAPE__HR_HEIGHT.propNumber;
	short GROUPSHAPE__HR_WIDTH = EscherPropertyTypes.GROUPSHAPE__HR_WIDTH.propNumber;
	short GROUPSHAPE__SCRIPTEXT = EscherPropertyTypes.GROUPSHAPE__SCRIPTEXT.propNumber;
	short GROUPSHAPE__SCRIPTLANG = EscherPropertyTypes.GROUPSHAPE__SCRIPTLANG.propNumber;
	short GROUPSHAPE__BORDERTOPCOLOR = EscherPropertyTypes.GROUPSHAPE__BORDERTOPCOLOR.propNumber;
	short GROUPSHAPE__BORDERLEFTCOLOR = EscherPropertyTypes.GROUPSHAPE__BORDERLEFTCOLOR.propNumber;
	short GROUPSHAPE__BORDERBOTTOMCOLOR = EscherPropertyTypes.GROUPSHAPE__BORDERBOTTOMCOLOR.propNumber;
	short GROUPSHAPE__BORDERRIGHTCOLOR = EscherPropertyTypes.GROUPSHAPE__BORDERRIGHTCOLOR.propNumber;
	short GROUPSHAPE__TABLEPROPERTIES = EscherPropertyTypes.GROUPSHAPE__TABLEPROPERTIES.propNumber;
	short GROUPSHAPE__TABLEROWPROPERTIES = EscherPropertyTypes.GROUPSHAPE__TABLEROWPROPERTIES.propNumber;
	short GROUPSHAPE__WEBBOT = EscherPropertyTypes.GROUPSHAPE__WEBBOT.propNumber;
	short GROUPSHAPE__METROBLOB = EscherPropertyTypes.GROUPSHAPE__METROBLOB.propNumber;
	short GROUPSHAPE__ZORDER = EscherPropertyTypes.GROUPSHAPE__ZORDER.propNumber;
	short GROUPSHAPE__EDITEDWRAP = EscherPropertyTypes.GROUPSHAPE__EDITEDWRAP.propNumber;
	short GROUPSHAPE__BEHINDDOCUMENT = EscherPropertyTypes.GROUPSHAPE__BEHINDDOCUMENT.propNumber;
	short GROUPSHAPE__ONDBLCLICKNOTIFY = EscherPropertyTypes.GROUPSHAPE__ONDBLCLICKNOTIFY.propNumber;
	short GROUPSHAPE__ISBUTTON = EscherPropertyTypes.GROUPSHAPE__ISBUTTON.propNumber;
	short GROUPSHAPE__1DADJUSTMENT = EscherPropertyTypes.GROUPSHAPE__1DADJUSTMENT.propNumber;
	short GROUPSHAPE__HIDDEN = EscherPropertyTypes.GROUPSHAPE__HIDDEN.propNumber;
	short GROUPSHAPE__FLAGS = EscherPropertyTypes.GROUPSHAPE__FLAGS.propNumber;
	short GROUPSHAPE__PRINT = EscherPropertyTypes.GROUPSHAPE__FLAGS.propNumber;


	static String getPropertyName(short propertyId) {
		return EscherPropertyTypes.forPropertyID(propertyId).propName;
	}

	static byte getPropertyType(short propertyId) {
		return (byte)EscherPropertyTypes.forPropertyID(propertyId).holder.ordinal();
	}
}
