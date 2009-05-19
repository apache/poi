
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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a list of all known escher properties including the description and
 * type.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherProperties
{

    // Property constants
    public static final short TRANSFORM__ROTATION = 4;
    public static final short PROTECTION__LOCKROTATION = 119;
    public static final short PROTECTION__LOCKASPECTRATIO = 120;
    public static final short PROTECTION__LOCKPOSITION = 121;
    public static final short PROTECTION__LOCKAGAINSTSELECT = 122;
    public static final short PROTECTION__LOCKCROPPING = 123;
    public static final short PROTECTION__LOCKVERTICES = 124;
    public static final short PROTECTION__LOCKTEXT = 125;
    public static final short PROTECTION__LOCKADJUSTHANDLES = 126;
    public static final short PROTECTION__LOCKAGAINSTGROUPING = 127;
    public static final short TEXT__TEXTID = 128;
    public static final short TEXT__TEXTLEFT = 129;
    public static final short TEXT__TEXTTOP = 130;
    public static final short TEXT__TEXTRIGHT = 131;
    public static final short TEXT__TEXTBOTTOM = 132;
    public static final short TEXT__WRAPTEXT = 133;
    public static final short TEXT__SCALETEXT = 134;
    public static final short TEXT__ANCHORTEXT = 135;
    public static final short TEXT__TEXTFLOW = 136;
    public static final short TEXT__FONTROTATION = 137;
    public static final short TEXT__IDOFNEXTSHAPE = 138;
    public static final short TEXT__BIDIR = 139;
    public static final short TEXT__SINGLECLICKSELECTS = 187;
    public static final short TEXT__USEHOSTMARGINS = 188;
    public static final short TEXT__ROTATETEXTWITHSHAPE = 189;
    public static final short TEXT__SIZESHAPETOFITTEXT = 190;
    public static final short TEXT__SIZE_TEXT_TO_FIT_SHAPE = 191 ;
    public static final short GEOTEXT__UNICODE = 192;
    public static final short GEOTEXT__RTFTEXT = 193;
    public static final short GEOTEXT__ALIGNMENTONCURVE = 194;
    public static final short GEOTEXT__DEFAULTPOINTSIZE = 195;
    public static final short GEOTEXT__TEXTSPACING = 196;
    public static final short GEOTEXT__FONTFAMILYNAME = 197;
    public static final short GEOTEXT__REVERSEROWORDER = 240;
    public static final short GEOTEXT__HASTEXTEFFECT = 241;
    public static final short GEOTEXT__ROTATECHARACTERS = 242;
    public static final short GEOTEXT__KERNCHARACTERS = 243;
    public static final short GEOTEXT__TIGHTORTRACK = 244;
    public static final short GEOTEXT__STRETCHTOFITSHAPE = 245;
    public static final short GEOTEXT__CHARBOUNDINGBOX = 246;
    public static final short GEOTEXT__SCALETEXTONPATH = 247;
    public static final short GEOTEXT__STRETCHCHARHEIGHT = 248;
    public static final short GEOTEXT__NOMEASUREALONGPATH = 249;
    public static final short GEOTEXT__BOLDFONT = 250;
    public static final short GEOTEXT__ITALICFONT = 251;
    public static final short GEOTEXT__UNDERLINEFONT = 252;
    public static final short GEOTEXT__SHADOWFONT = 253;
    public static final short GEOTEXT__SMALLCAPSFONT = 254;
    public static final short GEOTEXT__STRIKETHROUGHFONT = 255;
    public static final short BLIP__CROPFROMTOP = 256;
    public static final short BLIP__CROPFROMBOTTOM = 257;
    public static final short BLIP__CROPFROMLEFT = 258;
    public static final short BLIP__CROPFROMRIGHT = 259;
    public static final short BLIP__BLIPTODISPLAY = 260;
    public static final short BLIP__BLIPFILENAME = 261;
    public static final short BLIP__BLIPFLAGS = 262;
    public static final short BLIP__TRANSPARENTCOLOR = 263;
    public static final short BLIP__CONTRASTSETTING = 264;
    public static final short BLIP__BRIGHTNESSSETTING = 265;
    public static final short BLIP__GAMMA = 266;
    public static final short BLIP__PICTUREID = 267;
    public static final short BLIP__DOUBLEMOD = 268;
    public static final short BLIP__PICTUREFILLMOD = 269;
    public static final short BLIP__PICTURELINE = 270;
    public static final short BLIP__PRINTBLIP = 271;
    public static final short BLIP__PRINTBLIPFILENAME = 272;
    public static final short BLIP__PRINTFLAGS = 273;
    public static final short BLIP__NOHITTESTPICTURE = 316;
    public static final short BLIP__PICTUREGRAY = 317;
    public static final short BLIP__PICTUREBILEVEL = 318;
    public static final short BLIP__PICTUREACTIVE = 319;
    public static final short GEOMETRY__LEFT = 320;
    public static final short GEOMETRY__TOP = 321;
    public static final short GEOMETRY__RIGHT = 322;
    public static final short GEOMETRY__BOTTOM = 323;
    public static final short GEOMETRY__SHAPEPATH = 324;
    public static final short GEOMETRY__VERTICES = 325;
    public static final short GEOMETRY__SEGMENTINFO = 326;
    public static final short GEOMETRY__ADJUSTVALUE = 327;
    public static final short GEOMETRY__ADJUST2VALUE = 328;
    public static final short GEOMETRY__ADJUST3VALUE = 329;
    public static final short GEOMETRY__ADJUST4VALUE = 330;
    public static final short GEOMETRY__ADJUST5VALUE = 331;
    public static final short GEOMETRY__ADJUST6VALUE = 332;
    public static final short GEOMETRY__ADJUST7VALUE = 333;
    public static final short GEOMETRY__ADJUST8VALUE = 334;
    public static final short GEOMETRY__ADJUST9VALUE = 335;
    public static final short GEOMETRY__ADJUST10VALUE = 336;
    public static final short GEOMETRY__SHADOWok = 378;
    public static final short GEOMETRY__3DOK = 379;
    public static final short GEOMETRY__LINEOK = 380;
    public static final short GEOMETRY__GEOTEXTOK = 381;
    public static final short GEOMETRY__FILLSHADESHAPEOK = 382;
    public static final short GEOMETRY__FILLOK = 383;
    public static final short FILL__FILLTYPE = 384;
    public static final short FILL__FILLCOLOR = 385 ;
    public static final short FILL__FILLOPACITY = 386;
    public static final short FILL__FILLBACKCOLOR = 387;
    public static final short FILL__BACKOPACITY = 388;
    public static final short FILL__CRMOD = 389;
    public static final short FILL__PATTERNTEXTURE = 390;
    public static final short FILL__BLIPFILENAME = 391;
    public static final short FILL__BLIPFLAGS = 392;
    public static final short FILL__WIDTH = 393;
    public static final short FILL__HEIGHT = 394;
    public static final short FILL__ANGLE = 395;
    public static final short FILL__FOCUS = 396;
    public static final short FILL__TOLEFT = 397;
    public static final short FILL__TOTOP = 398;
    public static final short FILL__TORIGHT = 399;
    public static final short FILL__TOBOTTOM = 400;
    public static final short FILL__RECTLEFT = 401;
    public static final short FILL__RECTTOP = 402;
    public static final short FILL__RECTRIGHT = 403;
    public static final short FILL__RECTBOTTOM = 404;
    public static final short FILL__DZTYPE = 405;
    public static final short FILL__SHADEPRESET = 406;
    public static final short FILL__SHADECOLORS = 407;
    public static final short FILL__ORIGINX = 408;
    public static final short FILL__ORIGINY = 409;
    public static final short FILL__SHAPEORIGINX = 410;
    public static final short FILL__SHAPEORIGINY = 411;
    public static final short FILL__SHADETYPE = 412;
    public static final short FILL__FILLED = 443;
    public static final short FILL__HITTESTFILL = 444;
    public static final short FILL__SHAPE = 445;
    public static final short FILL__USERECT = 446;
    public static final short FILL__NOFILLHITTEST = 447;
    public static final short LINESTYLE__COLOR = 448 ;
    public static final short LINESTYLE__OPACITY = 449;
    public static final short LINESTYLE__BACKCOLOR = 450;
    public static final short LINESTYLE__CRMOD = 451;
    public static final short LINESTYLE__LINETYPE = 452;
    public static final short LINESTYLE__FILLBLIP = 453;
    public static final short LINESTYLE__FILLBLIPNAME = 454;
    public static final short LINESTYLE__FILLBLIPFLAGS = 455;
    public static final short LINESTYLE__FILLWIDTH = 456;
    public static final short LINESTYLE__FILLHEIGHT = 457;
    public static final short LINESTYLE__FILLDZTYPE = 458;
    public static final short LINESTYLE__LINEWIDTH = 459;
    public static final short LINESTYLE__LINEMITERLIMIT = 460;
    public static final short LINESTYLE__LINESTYLE = 461;
    public static final short LINESTYLE__LINEDASHING = 462;
    public static final short LINESTYLE__LINEDASHSTYLE = 463;
    public static final short LINESTYLE__LINESTARTARROWHEAD = 464;
    public static final short LINESTYLE__LINEENDARROWHEAD = 465;
    public static final short LINESTYLE__LINESTARTARROWWIDTH = 466;
    public static final short LINESTYLE__LINEESTARTARROWLENGTH = 467;
    public static final short LINESTYLE__LINEENDARROWWIDTH = 468;
    public static final short LINESTYLE__LINEENDARROWLENGTH = 469;
    public static final short LINESTYLE__LINEJOINSTYLE = 470;
    public static final short LINESTYLE__LINEENDCAPSTYLE = 471;
    public static final short LINESTYLE__ARROWHEADSOK = 507;
    public static final short LINESTYLE__ANYLINE = 508;
    public static final short LINESTYLE__HITLINETEST = 509;
    public static final short LINESTYLE__LINEFILLSHAPE = 510;
    public static final short LINESTYLE__NOLINEDRAWDASH = 511;
    public static final short SHADOWSTYLE__TYPE = 512;
    public static final short SHADOWSTYLE__COLOR = 513;
    public static final short SHADOWSTYLE__HIGHLIGHT = 514;
    public static final short SHADOWSTYLE__CRMOD = 515;
    public static final short SHADOWSTYLE__OPACITY = 516;
    public static final short SHADOWSTYLE__OFFSETX = 517;
    public static final short SHADOWSTYLE__OFFSETY = 518;
    public static final short SHADOWSTYLE__SECONDOFFSETX = 519;
    public static final short SHADOWSTYLE__SECONDOFFSETY = 520;
    public static final short SHADOWSTYLE__SCALEXTOX = 521;
    public static final short SHADOWSTYLE__SCALEYTOX = 522;
    public static final short SHADOWSTYLE__SCALEXTOY = 523;
    public static final short SHADOWSTYLE__SCALEYTOY = 524;
    public static final short SHADOWSTYLE__PERSPECTIVEX = 525;
    public static final short SHADOWSTYLE__PERSPECTIVEY = 526;
    public static final short SHADOWSTYLE__WEIGHT = 527;
    public static final short SHADOWSTYLE__ORIGINX = 528;
    public static final short SHADOWSTYLE__ORIGINY = 529;
    public static final short SHADOWSTYLE__SHADOW = 574;
    public static final short SHADOWSTYLE__SHADOWOBSURED = 575;
    public static final short PERSPECTIVE__TYPE = 576;
    public static final short PERSPECTIVE__OFFSETX = 577;
    public static final short PERSPECTIVE__OFFSETY = 578;
    public static final short PERSPECTIVE__SCALEXTOX = 579;
    public static final short PERSPECTIVE__SCALEYTOX = 580;
    public static final short PERSPECTIVE__SCALEXTOY = 581;
    public static final short PERSPECTIVE__SCALEYTOY = 582;
    public static final short PERSPECTIVE__PERSPECTIVEX = 583;
    public static final short PERSPECTIVE__PERSPECTIVEY = 584;
    public static final short PERSPECTIVE__WEIGHT = 585;
    public static final short PERSPECTIVE__ORIGINX = 586;
    public static final short PERSPECTIVE__ORIGINY = 587;
    public static final short PERSPECTIVE__PERSPECTIVEON = 639;
    public static final short THREED__SPECULARAMOUNT = 640;
    public static final short THREED__DIFFUSEAMOUNT = 661;
    public static final short THREED__SHININESS = 662;
    public static final short THREED__EDGETHICKNESS = 663;
    public static final short THREED__EXTRUDEFORWARD = 664;
    public static final short THREED__EXTRUDEBACKWARD = 665;
    public static final short THREED__EXTRUDEPLANE = 666;
    public static final short THREED__EXTRUSIONCOLOR = 667;
    public static final short THREED__CRMOD = 648;
    public static final short THREED__3DEFFECT = 700;
    public static final short THREED__METALLIC = 701;
    public static final short THREED__USEEXTRUSIONCOLOR = 702;
    public static final short THREED__LIGHTFACE = 703;
    public static final short THREEDSTYLE__YROTATIONANGLE = 704;
    public static final short THREEDSTYLE__XROTATIONANGLE = 705;
    public static final short THREEDSTYLE__ROTATIONAXISX = 706;
    public static final short THREEDSTYLE__ROTATIONAXISY = 707;
    public static final short THREEDSTYLE__ROTATIONAXISZ = 708;
    public static final short THREEDSTYLE__ROTATIONANGLE = 709;
    public static final short THREEDSTYLE__ROTATIONCENTERX = 710;
    public static final short THREEDSTYLE__ROTATIONCENTERY = 711;
    public static final short THREEDSTYLE__ROTATIONCENTERZ = 712;
    public static final short THREEDSTYLE__RENDERMODE = 713;
    public static final short THREEDSTYLE__TOLERANCE = 714;
    public static final short THREEDSTYLE__XVIEWPOINT = 715;
    public static final short THREEDSTYLE__YVIEWPOINT = 716;
    public static final short THREEDSTYLE__ZVIEWPOINT = 717;
    public static final short THREEDSTYLE__ORIGINX = 718;
    public static final short THREEDSTYLE__ORIGINY = 719;
    public static final short THREEDSTYLE__SKEWANGLE = 720;
    public static final short THREEDSTYLE__SKEWAMOUNT = 721;
    public static final short THREEDSTYLE__AMBIENTINTENSITY = 722;
    public static final short THREEDSTYLE__KEYX = 723;
    public static final short THREEDSTYLE__KEYY = 724;
    public static final short THREEDSTYLE__KEYZ = 725;
    public static final short THREEDSTYLE__KEYINTENSITY = 726;
    public static final short THREEDSTYLE__FILLX = 727;
    public static final short THREEDSTYLE__FILLY = 728;
    public static final short THREEDSTYLE__FILLZ = 729;
    public static final short THREEDSTYLE__FILLINTENSITY = 730;
    public static final short THREEDSTYLE__CONSTRAINROTATION = 763;
    public static final short THREEDSTYLE__ROTATIONCENTERAUTO = 764;
    public static final short THREEDSTYLE__PARALLEL = 765;
    public static final short THREEDSTYLE__KEYHARSH = 766;
    public static final short THREEDSTYLE__FILLHARSH = 767;
    public static final short SHAPE__MASTER = 769;
    public static final short SHAPE__CONNECTORSTYLE = 771;
    public static final short SHAPE__BLACKANDWHITESETTINGS = 772;
    public static final short SHAPE__WMODEPUREBW = 773;
    public static final short SHAPE__WMODEBW = 774;
    public static final short SHAPE__OLEICON = 826;
    public static final short SHAPE__PREFERRELATIVERESIZE = 827;
    public static final short SHAPE__LOCKSHAPETYPE = 828;
    public static final short SHAPE__DELETEATTACHEDOBJECT = 830;
    public static final short SHAPE__BACKGROUNDSHAPE = 831;
    public static final short CALLOUT__CALLOUTTYPE = 832;
    public static final short CALLOUT__XYCALLOUTGAP = 833;
    public static final short CALLOUT__CALLOUTANGLE = 834;
    public static final short CALLOUT__CALLOUTDROPTYPE = 835;
    public static final short CALLOUT__CALLOUTDROPSPECIFIED = 836;
    public static final short CALLOUT__CALLOUTLENGTHSPECIFIED = 837;
    public static final short CALLOUT__ISCALLOUT = 889;
    public static final short CALLOUT__CALLOUTACCENTBAR = 890;
    public static final short CALLOUT__CALLOUTTEXTBORDER = 891;
    public static final short CALLOUT__CALLOUTMINUSX = 892;
    public static final short CALLOUT__CALLOUTMINUSY = 893;
    public static final short CALLOUT__DROPAUTO = 894;
    public static final short CALLOUT__LENGTHSPECIFIED = 895;
    public static final short GROUPSHAPE__SHAPENAME = 896;
    public static final short GROUPSHAPE__DESCRIPTION = 897;
    public static final short GROUPSHAPE__HYPERLINK = 898;
    public static final short GROUPSHAPE__WRAPPOLYGONVERTICES = 899;
    public static final short GROUPSHAPE__WRAPDISTLEFT = 900;
    public static final short GROUPSHAPE__WRAPDISTTOP = 901;
    public static final short GROUPSHAPE__WRAPDISTRIGHT = 902;
    public static final short GROUPSHAPE__WRAPDISTBOTTOM = 903;
    public static final short GROUPSHAPE__REGROUPID = 904;
    public static final short GROUPSHAPE__EDITEDWRAP = 953;
    public static final short GROUPSHAPE__BEHINDDOCUMENT = 954;
    public static final short GROUPSHAPE__ONDBLCLICKNOTIFY = 955;
    public static final short GROUPSHAPE__ISBUTTON = 956;
    public static final short GROUPSHAPE__1DADJUSTMENT = 957;
    public static final short GROUPSHAPE__HIDDEN = 958;
    public static final short GROUPSHAPE__PRINT = 959;


    private static Map properties;

    private static void initProps()
    {
        if ( properties == null )
        {
            properties = new HashMap();
            addProp( TRANSFORM__ROTATION, data( "transform.rotation" ) );
            addProp( PROTECTION__LOCKROTATION , data( "protection.lockrotation" ) );
            addProp( PROTECTION__LOCKASPECTRATIO , data( "protection.lockaspectratio" ) );
            addProp( PROTECTION__LOCKPOSITION , data( "protection.lockposition" ) );
            addProp( PROTECTION__LOCKAGAINSTSELECT , data( "protection.lockagainstselect" ) );
            addProp( PROTECTION__LOCKCROPPING , data( "protection.lockcropping" ) );
            addProp( PROTECTION__LOCKVERTICES , data( "protection.lockvertices" ) );
            addProp( PROTECTION__LOCKTEXT , data( "protection.locktext" ) );
            addProp( PROTECTION__LOCKADJUSTHANDLES , data( "protection.lockadjusthandles" ) );
            addProp( PROTECTION__LOCKAGAINSTGROUPING , data( "protection.lockagainstgrouping", EscherPropertyMetaData.TYPE_BOOLEAN ) );
            addProp( TEXT__TEXTID , data( "text.textid" ) );
            addProp( TEXT__TEXTLEFT , data( "text.textleft" ) );
            addProp( TEXT__TEXTTOP , data( "text.texttop" ) );
            addProp( TEXT__TEXTRIGHT , data( "text.textright" ) );
            addProp( TEXT__TEXTBOTTOM , data( "text.textbottom" ) );
            addProp( TEXT__WRAPTEXT , data( "text.wraptext" ) );
            addProp( TEXT__SCALETEXT , data( "text.scaletext" ) );
            addProp( TEXT__ANCHORTEXT , data( "text.anchortext" ) );
            addProp( TEXT__TEXTFLOW , data( "text.textflow" ) );
            addProp( TEXT__FONTROTATION , data( "text.fontrotation" ) );
            addProp( TEXT__IDOFNEXTSHAPE , data( "text.idofnextshape" ) );
            addProp( TEXT__BIDIR , data( "text.bidir" ) );
            addProp( TEXT__SINGLECLICKSELECTS , data( "text.singleclickselects" ) );
            addProp( TEXT__USEHOSTMARGINS , data( "text.usehostmargins" ) );
            addProp( TEXT__ROTATETEXTWITHSHAPE , data( "text.rotatetextwithshape" ) );
            addProp( TEXT__SIZESHAPETOFITTEXT , data( "text.sizeshapetofittext" ) );
            addProp( TEXT__SIZE_TEXT_TO_FIT_SHAPE, data( "text.sizetexttofitshape", EscherPropertyMetaData.TYPE_BOOLEAN ) );
            addProp( GEOTEXT__UNICODE , data( "geotext.unicode" ) );
            addProp( GEOTEXT__RTFTEXT , data( "geotext.rtftext" ) );
            addProp( GEOTEXT__ALIGNMENTONCURVE , data( "geotext.alignmentoncurve" ) );
            addProp( GEOTEXT__DEFAULTPOINTSIZE , data( "geotext.defaultpointsize" ) );
            addProp( GEOTEXT__TEXTSPACING , data( "geotext.textspacing" ) );
            addProp( GEOTEXT__FONTFAMILYNAME , data( "geotext.fontfamilyname" ) );
            addProp( GEOTEXT__REVERSEROWORDER , data( "geotext.reverseroworder" ) );
            addProp( GEOTEXT__HASTEXTEFFECT , data( "geotext.hastexteffect" ) );
            addProp( GEOTEXT__ROTATECHARACTERS , data( "geotext.rotatecharacters" ) );
            addProp( GEOTEXT__KERNCHARACTERS , data( "geotext.kerncharacters" ) );
            addProp( GEOTEXT__TIGHTORTRACK , data( "geotext.tightortrack" ) );
            addProp( GEOTEXT__STRETCHTOFITSHAPE , data( "geotext.stretchtofitshape" ) );
            addProp( GEOTEXT__CHARBOUNDINGBOX , data( "geotext.charboundingbox" ) );
            addProp( GEOTEXT__SCALETEXTONPATH , data( "geotext.scaletextonpath" ) );
            addProp( GEOTEXT__STRETCHCHARHEIGHT , data( "geotext.stretchcharheight" ) );
            addProp( GEOTEXT__NOMEASUREALONGPATH , data( "geotext.nomeasurealongpath" ) );
            addProp( GEOTEXT__BOLDFONT , data( "geotext.boldfont" ) );
            addProp( GEOTEXT__ITALICFONT , data( "geotext.italicfont" ) );
            addProp( GEOTEXT__UNDERLINEFONT , data( "geotext.underlinefont" ) );
            addProp( GEOTEXT__SHADOWFONT , data( "geotext.shadowfont" ) );
            addProp( GEOTEXT__SMALLCAPSFONT , data( "geotext.smallcapsfont" ) );
            addProp( GEOTEXT__STRIKETHROUGHFONT , data( "geotext.strikethroughfont" ) );
            addProp( BLIP__CROPFROMTOP , data( "blip.cropfromtop" ) );
            addProp( BLIP__CROPFROMBOTTOM , data( "blip.cropfrombottom" ) );
            addProp( BLIP__CROPFROMLEFT , data( "blip.cropfromleft" ) );
            addProp( BLIP__CROPFROMRIGHT , data( "blip.cropfromright" ) );
            addProp( BLIP__BLIPTODISPLAY , data( "blip.bliptodisplay" ) );
            addProp( BLIP__BLIPFILENAME , data( "blip.blipfilename" ) );
            addProp( BLIP__BLIPFLAGS , data( "blip.blipflags" ) );
            addProp( BLIP__TRANSPARENTCOLOR , data( "blip.transparentcolor" ) );
            addProp( BLIP__CONTRASTSETTING , data( "blip.contrastsetting" ) );
            addProp( BLIP__BRIGHTNESSSETTING , data( "blip.brightnesssetting" ) );
            addProp( BLIP__GAMMA , data( "blip.gamma" ) );
            addProp( BLIP__PICTUREID , data( "blip.pictureid" ) );
            addProp( BLIP__DOUBLEMOD , data( "blip.doublemod" ) );
            addProp( BLIP__PICTUREFILLMOD , data( "blip.picturefillmod" ) );
            addProp( BLIP__PICTURELINE , data( "blip.pictureline" ) );
            addProp( BLIP__PRINTBLIP , data( "blip.printblip" ) );
            addProp( BLIP__PRINTBLIPFILENAME , data( "blip.printblipfilename" ) );
            addProp( BLIP__PRINTFLAGS , data( "blip.printflags" ) );
            addProp( BLIP__NOHITTESTPICTURE , data( "blip.nohittestpicture" ) );
            addProp( BLIP__PICTUREGRAY , data( "blip.picturegray" ) );
            addProp( BLIP__PICTUREBILEVEL , data( "blip.picturebilevel" ) );
            addProp( BLIP__PICTUREACTIVE , data( "blip.pictureactive" ) );
            addProp( GEOMETRY__LEFT , data( "geometry.left" ) );
            addProp( GEOMETRY__TOP , data( "geometry.top" ) );
            addProp( GEOMETRY__RIGHT , data( "geometry.right" ) );
            addProp( GEOMETRY__BOTTOM , data( "geometry.bottom" ) );
            addProp( GEOMETRY__SHAPEPATH , data( "geometry.shapepath", EscherPropertyMetaData.TYPE_SHAPEPATH ) );
            addProp( GEOMETRY__VERTICES , data( "geometry.vertices" , EscherPropertyMetaData.TYPE_ARRAY ) );
            addProp( GEOMETRY__SEGMENTINFO , data( "geometry.segmentinfo", EscherPropertyMetaData.TYPE_ARRAY ) );
            addProp( GEOMETRY__ADJUSTVALUE , data( "geometry.adjustvalue" ) );
            addProp( GEOMETRY__ADJUST2VALUE , data( "geometry.adjust2value" ) );
            addProp( GEOMETRY__ADJUST3VALUE , data( "geometry.adjust3value" ) );
            addProp( GEOMETRY__ADJUST4VALUE , data( "geometry.adjust4value" ) );
            addProp( GEOMETRY__ADJUST5VALUE , data( "geometry.adjust5value" ) );
            addProp( GEOMETRY__ADJUST6VALUE , data( "geometry.adjust6value" ) );
            addProp( GEOMETRY__ADJUST7VALUE , data( "geometry.adjust7value" ) );
            addProp( GEOMETRY__ADJUST8VALUE , data( "geometry.adjust8value" ) );
            addProp( GEOMETRY__ADJUST9VALUE , data( "geometry.adjust9value" ) );
            addProp( GEOMETRY__ADJUST10VALUE , data( "geometry.adjust10value" ) );
            addProp( GEOMETRY__SHADOWok , data( "geometry.shadowOK" ) );
            addProp( GEOMETRY__3DOK , data( "geometry.3dok" ) );
            addProp( GEOMETRY__LINEOK , data( "geometry.lineok" ) );
            addProp( GEOMETRY__GEOTEXTOK , data( "geometry.geotextok" ) );
            addProp( GEOMETRY__FILLSHADESHAPEOK , data( "geometry.fillshadeshapeok" ) );
            addProp( GEOMETRY__FILLOK , data( "geometry.fillok", EscherPropertyMetaData.TYPE_BOOLEAN ) );
            addProp( FILL__FILLTYPE , data( "fill.filltype" ) );
            addProp( FILL__FILLCOLOR, data( "fill.fillcolor", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( FILL__FILLOPACITY , data( "fill.fillopacity" ) );
            addProp( FILL__FILLBACKCOLOR , data( "fill.fillbackcolor", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( FILL__BACKOPACITY , data( "fill.backopacity" ) );
            addProp( FILL__CRMOD , data( "fill.crmod" ) );
            addProp( FILL__PATTERNTEXTURE , data( "fill.patterntexture" ) );
            addProp( FILL__BLIPFILENAME , data( "fill.blipfilename" ) );
            addProp( FILL__BLIPFLAGS, data( "fill.blipflags" ) );
            addProp( FILL__WIDTH , data( "fill.width" ) );
            addProp( FILL__HEIGHT , data( "fill.height" ) );
            addProp( FILL__ANGLE , data( "fill.angle" ) );
            addProp( FILL__FOCUS , data( "fill.focus" ) );
            addProp( FILL__TOLEFT , data( "fill.toleft" ) );
            addProp( FILL__TOTOP , data( "fill.totop" ) );
            addProp( FILL__TORIGHT , data( "fill.toright" ) );
            addProp( FILL__TOBOTTOM , data( "fill.tobottom" ) );
            addProp( FILL__RECTLEFT , data( "fill.rectleft" ) );
            addProp( FILL__RECTTOP , data( "fill.recttop" ) );
            addProp( FILL__RECTRIGHT , data( "fill.rectright" ) );
            addProp( FILL__RECTBOTTOM , data( "fill.rectbottom" ) );
            addProp( FILL__DZTYPE , data( "fill.dztype" ) );
            addProp( FILL__SHADEPRESET , data( "fill.shadepreset" ) );
            addProp( FILL__SHADECOLORS , data( "fill.shadecolors", EscherPropertyMetaData.TYPE_ARRAY ) );
            addProp( FILL__ORIGINX , data( "fill.originx" ) );
            addProp( FILL__ORIGINY , data( "fill.originy" ) );
            addProp( FILL__SHAPEORIGINX , data( "fill.shapeoriginx" ) );
            addProp( FILL__SHAPEORIGINY , data( "fill.shapeoriginy" ) );
            addProp( FILL__SHADETYPE , data( "fill.shadetype" ) );
            addProp( FILL__FILLED , data( "fill.filled" ) );
            addProp( FILL__HITTESTFILL , data( "fill.hittestfill" ) );
            addProp( FILL__SHAPE , data( "fill.shape" ) );
            addProp( FILL__USERECT , data( "fill.userect" ) );
            addProp( FILL__NOFILLHITTEST , data( "fill.nofillhittest", EscherPropertyMetaData.TYPE_BOOLEAN ) );
            addProp( LINESTYLE__COLOR, data( "linestyle.color", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( LINESTYLE__OPACITY , data( "linestyle.opacity" ) );
            addProp( LINESTYLE__BACKCOLOR , data( "linestyle.backcolor", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( LINESTYLE__CRMOD , data( "linestyle.crmod" ) );
            addProp( LINESTYLE__LINETYPE , data( "linestyle.linetype" ) );
            addProp( LINESTYLE__FILLBLIP , data( "linestyle.fillblip" ) );
            addProp( LINESTYLE__FILLBLIPNAME , data( "linestyle.fillblipname" ) );
            addProp( LINESTYLE__FILLBLIPFLAGS , data( "linestyle.fillblipflags" ) );
            addProp( LINESTYLE__FILLWIDTH , data( "linestyle.fillwidth" ) );
            addProp( LINESTYLE__FILLHEIGHT , data( "linestyle.fillheight" ) );
            addProp( LINESTYLE__FILLDZTYPE , data( "linestyle.filldztype" ) );
            addProp( LINESTYLE__LINEWIDTH , data( "linestyle.linewidth" ) );
            addProp( LINESTYLE__LINEMITERLIMIT , data( "linestyle.linemiterlimit" ) );
            addProp( LINESTYLE__LINESTYLE , data( "linestyle.linestyle" ) );
            addProp( LINESTYLE__LINEDASHING , data( "linestyle.linedashing" ) );
            addProp( LINESTYLE__LINEDASHSTYLE , data( "linestyle.linedashstyle", EscherPropertyMetaData.TYPE_ARRAY ) );
            addProp( LINESTYLE__LINESTARTARROWHEAD , data( "linestyle.linestartarrowhead" ) );
            addProp( LINESTYLE__LINEENDARROWHEAD , data( "linestyle.lineendarrowhead" ) );
            addProp( LINESTYLE__LINESTARTARROWWIDTH , data( "linestyle.linestartarrowwidth" ) );
            addProp( LINESTYLE__LINEESTARTARROWLENGTH , data( "linestyle.lineestartarrowlength" ) );
            addProp( LINESTYLE__LINEENDARROWWIDTH , data( "linestyle.lineendarrowwidth" ) );
            addProp( LINESTYLE__LINEENDARROWLENGTH , data( "linestyle.lineendarrowlength" ) );
            addProp( LINESTYLE__LINEJOINSTYLE , data( "linestyle.linejoinstyle" ) );
            addProp( LINESTYLE__LINEENDCAPSTYLE , data( "linestyle.lineendcapstyle" ) );
            addProp( LINESTYLE__ARROWHEADSOK , data( "linestyle.arrowheadsok" ) );
            addProp( LINESTYLE__ANYLINE , data( "linestyle.anyline" ) );
            addProp( LINESTYLE__HITLINETEST , data( "linestyle.hitlinetest" ) );
            addProp( LINESTYLE__LINEFILLSHAPE , data( "linestyle.linefillshape" ) );
            addProp( LINESTYLE__NOLINEDRAWDASH , data( "linestyle.nolinedrawdash", EscherPropertyMetaData.TYPE_BOOLEAN ) );
            addProp( SHADOWSTYLE__TYPE , data( "shadowstyle.type" ) );
            addProp( SHADOWSTYLE__COLOR , data( "shadowstyle.color", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( SHADOWSTYLE__HIGHLIGHT , data( "shadowstyle.highlight" ) );
            addProp( SHADOWSTYLE__CRMOD , data( "shadowstyle.crmod" ) );
            addProp( SHADOWSTYLE__OPACITY , data( "shadowstyle.opacity" ) );
            addProp( SHADOWSTYLE__OFFSETX , data( "shadowstyle.offsetx" ) );
            addProp( SHADOWSTYLE__OFFSETY , data( "shadowstyle.offsety" ) );
            addProp( SHADOWSTYLE__SECONDOFFSETX , data( "shadowstyle.secondoffsetx" ) );
            addProp( SHADOWSTYLE__SECONDOFFSETY , data( "shadowstyle.secondoffsety" ) );
            addProp( SHADOWSTYLE__SCALEXTOX , data( "shadowstyle.scalextox" ) );
            addProp( SHADOWSTYLE__SCALEYTOX , data( "shadowstyle.scaleytox" ) );
            addProp( SHADOWSTYLE__SCALEXTOY , data( "shadowstyle.scalextoy" ) );
            addProp( SHADOWSTYLE__SCALEYTOY , data( "shadowstyle.scaleytoy" ) );
            addProp( SHADOWSTYLE__PERSPECTIVEX , data( "shadowstyle.perspectivex" ) );
            addProp( SHADOWSTYLE__PERSPECTIVEY , data( "shadowstyle.perspectivey" ) );
            addProp( SHADOWSTYLE__WEIGHT , data( "shadowstyle.weight" ) );
            addProp( SHADOWSTYLE__ORIGINX , data( "shadowstyle.originx" ) );
            addProp( SHADOWSTYLE__ORIGINY , data( "shadowstyle.originy" ) );
            addProp( SHADOWSTYLE__SHADOW , data( "shadowstyle.shadow" ) );
            addProp( SHADOWSTYLE__SHADOWOBSURED , data( "shadowstyle.shadowobsured" ) );
            addProp( PERSPECTIVE__TYPE , data( "perspective.type" ) );
            addProp( PERSPECTIVE__OFFSETX , data( "perspective.offsetx" ) );
            addProp( PERSPECTIVE__OFFSETY , data( "perspective.offsety" ) );
            addProp( PERSPECTIVE__SCALEXTOX , data( "perspective.scalextox" ) );
            addProp( PERSPECTIVE__SCALEYTOX , data( "perspective.scaleytox" ) );
            addProp( PERSPECTIVE__SCALEXTOY , data( "perspective.scalextoy" ) );
            addProp( PERSPECTIVE__SCALEYTOY , data( "perspective.scaleytoy" ) );
            addProp( PERSPECTIVE__PERSPECTIVEX , data( "perspective.perspectivex" ) );
            addProp( PERSPECTIVE__PERSPECTIVEY , data( "perspective.perspectivey" ) );
            addProp( PERSPECTIVE__WEIGHT , data( "perspective.weight" ) );
            addProp( PERSPECTIVE__ORIGINX , data( "perspective.originx" ) );
            addProp( PERSPECTIVE__ORIGINY , data( "perspective.originy" ) );
            addProp( PERSPECTIVE__PERSPECTIVEON , data( "perspective.perspectiveon" ) );
            addProp( THREED__SPECULARAMOUNT , data( "3d.specularamount" ) );
            addProp( THREED__DIFFUSEAMOUNT , data( "3d.diffuseamount" ) );
            addProp( THREED__SHININESS , data( "3d.shininess" ) );
            addProp( THREED__EDGETHICKNESS , data( "3d.edgethickness" ) );
            addProp( THREED__EXTRUDEFORWARD , data( "3d.extrudeforward" ) );
            addProp( THREED__EXTRUDEBACKWARD , data( "3d.extrudebackward" ) );
            addProp( THREED__EXTRUDEPLANE , data( "3d.extrudeplane" ) );
            addProp( THREED__EXTRUSIONCOLOR , data( "3d.extrusioncolor", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( THREED__CRMOD , data( "3d.crmod" ) );
            addProp( THREED__3DEFFECT , data( "3d.3deffect" ) );
            addProp( THREED__METALLIC , data( "3d.metallic" ) );
            addProp( THREED__USEEXTRUSIONCOLOR , data( "3d.useextrusioncolor", EscherPropertyMetaData.TYPE_RGB ) );
            addProp( THREED__LIGHTFACE , data( "3d.lightface" ) );
            addProp( THREEDSTYLE__YROTATIONANGLE , data( "3dstyle.yrotationangle" ) );
            addProp( THREEDSTYLE__XROTATIONANGLE , data( "3dstyle.xrotationangle" ) );
            addProp( THREEDSTYLE__ROTATIONAXISX , data( "3dstyle.rotationaxisx" ) );
            addProp( THREEDSTYLE__ROTATIONAXISY , data( "3dstyle.rotationaxisy" ) );
            addProp( THREEDSTYLE__ROTATIONAXISZ , data( "3dstyle.rotationaxisz" ) );
            addProp( THREEDSTYLE__ROTATIONANGLE , data( "3dstyle.rotationangle" ) );
            addProp( THREEDSTYLE__ROTATIONCENTERX , data( "3dstyle.rotationcenterx" ) );
            addProp( THREEDSTYLE__ROTATIONCENTERY , data( "3dstyle.rotationcentery" ) );
            addProp( THREEDSTYLE__ROTATIONCENTERZ , data( "3dstyle.rotationcenterz" ) );
            addProp( THREEDSTYLE__RENDERMODE , data( "3dstyle.rendermode" ) );
            addProp( THREEDSTYLE__TOLERANCE , data( "3dstyle.tolerance" ) );
            addProp( THREEDSTYLE__XVIEWPOINT , data( "3dstyle.xviewpoint" ) );
            addProp( THREEDSTYLE__YVIEWPOINT , data( "3dstyle.yviewpoint" ) );
            addProp( THREEDSTYLE__ZVIEWPOINT , data( "3dstyle.zviewpoint" ) );
            addProp( THREEDSTYLE__ORIGINX , data( "3dstyle.originx" ) );
            addProp( THREEDSTYLE__ORIGINY , data( "3dstyle.originy" ) );
            addProp( THREEDSTYLE__SKEWANGLE , data( "3dstyle.skewangle" ) );
            addProp( THREEDSTYLE__SKEWAMOUNT , data( "3dstyle.skewamount" ) );
            addProp( THREEDSTYLE__AMBIENTINTENSITY , data( "3dstyle.ambientintensity" ) );
            addProp( THREEDSTYLE__KEYX , data( "3dstyle.keyx" ) );
            addProp( THREEDSTYLE__KEYY , data( "3dstyle.keyy" ) );
            addProp( THREEDSTYLE__KEYZ , data( "3dstyle.keyz" ) );
            addProp( THREEDSTYLE__KEYINTENSITY , data( "3dstyle.keyintensity" ) );
            addProp( THREEDSTYLE__FILLX , data( "3dstyle.fillx" ) );
            addProp( THREEDSTYLE__FILLY , data( "3dstyle.filly" ) );
            addProp( THREEDSTYLE__FILLZ , data( "3dstyle.fillz" ) );
            addProp( THREEDSTYLE__FILLINTENSITY , data( "3dstyle.fillintensity" ) );
            addProp( THREEDSTYLE__CONSTRAINROTATION , data( "3dstyle.constrainrotation" ) );
            addProp( THREEDSTYLE__ROTATIONCENTERAUTO , data( "3dstyle.rotationcenterauto" ) );
            addProp( THREEDSTYLE__PARALLEL , data( "3dstyle.parallel" ) );
            addProp( THREEDSTYLE__KEYHARSH , data( "3dstyle.keyharsh" ) );
            addProp( THREEDSTYLE__FILLHARSH , data( "3dstyle.fillharsh" ) );
            addProp( SHAPE__MASTER , data( "shape.master" ) );
            addProp( SHAPE__CONNECTORSTYLE , data( "shape.connectorstyle" ) );
            addProp( SHAPE__BLACKANDWHITESETTINGS , data( "shape.blackandwhitesettings" ) );
            addProp( SHAPE__WMODEPUREBW , data( "shape.wmodepurebw" ) );
            addProp( SHAPE__WMODEBW , data( "shape.wmodebw" ) );
            addProp( SHAPE__OLEICON , data( "shape.oleicon" ) );
            addProp( SHAPE__PREFERRELATIVERESIZE , data( "shape.preferrelativeresize" ) );
            addProp( SHAPE__LOCKSHAPETYPE , data( "shape.lockshapetype" ) );
            addProp( SHAPE__DELETEATTACHEDOBJECT , data( "shape.deleteattachedobject" ) );
            addProp( SHAPE__BACKGROUNDSHAPE , data( "shape.backgroundshape" ) );
            addProp( CALLOUT__CALLOUTTYPE , data( "callout.callouttype" ) );
            addProp( CALLOUT__XYCALLOUTGAP , data( "callout.xycalloutgap" ) );
            addProp( CALLOUT__CALLOUTANGLE , data( "callout.calloutangle" ) );
            addProp( CALLOUT__CALLOUTDROPTYPE , data( "callout.calloutdroptype" ) );
            addProp( CALLOUT__CALLOUTDROPSPECIFIED , data( "callout.calloutdropspecified" ) );
            addProp( CALLOUT__CALLOUTLENGTHSPECIFIED , data( "callout.calloutlengthspecified" ) );
            addProp( CALLOUT__ISCALLOUT , data( "callout.iscallout" ) );
            addProp( CALLOUT__CALLOUTACCENTBAR , data( "callout.calloutaccentbar" ) );
            addProp( CALLOUT__CALLOUTTEXTBORDER , data( "callout.callouttextborder" ) );
            addProp( CALLOUT__CALLOUTMINUSX , data( "callout.calloutminusx" ) );
            addProp( CALLOUT__CALLOUTMINUSY , data( "callout.calloutminusy" ) );
            addProp( CALLOUT__DROPAUTO , data( "callout.dropauto" ) );
            addProp( CALLOUT__LENGTHSPECIFIED , data( "callout.lengthspecified" ) );
            addProp( GROUPSHAPE__SHAPENAME , data( "groupshape.shapename" ) );
            addProp( GROUPSHAPE__DESCRIPTION , data( "groupshape.description" ) );
            addProp( GROUPSHAPE__HYPERLINK , data( "groupshape.hyperlink" ) );
            addProp( GROUPSHAPE__WRAPPOLYGONVERTICES , data( "groupshape.wrappolygonvertices", EscherPropertyMetaData.TYPE_ARRAY ) );
            addProp( GROUPSHAPE__WRAPDISTLEFT , data( "groupshape.wrapdistleft" ) );
            addProp( GROUPSHAPE__WRAPDISTTOP , data( "groupshape.wrapdisttop" ) );
            addProp( GROUPSHAPE__WRAPDISTRIGHT , data( "groupshape.wrapdistright" ) );
            addProp( GROUPSHAPE__WRAPDISTBOTTOM , data( "groupshape.wrapdistbottom" ) );
            addProp( GROUPSHAPE__REGROUPID , data( "groupshape.regroupid" ) );
            addProp( GROUPSHAPE__EDITEDWRAP , data( "groupshape.editedwrap" ) );
            addProp( GROUPSHAPE__BEHINDDOCUMENT , data( "groupshape.behinddocument" ) );
            addProp( GROUPSHAPE__ONDBLCLICKNOTIFY , data( "groupshape.ondblclicknotify" ) );
            addProp( GROUPSHAPE__ISBUTTON , data( "groupshape.isbutton" ) );
            addProp( GROUPSHAPE__1DADJUSTMENT , data( "groupshape.1dadjustment" ) );
            addProp( GROUPSHAPE__HIDDEN , data( "groupshape.hidden" ) );
            addProp( GROUPSHAPE__PRINT , data( "groupshape.print", EscherPropertyMetaData.TYPE_BOOLEAN ) );
        }
    }

    private static void addProp( int s, EscherPropertyMetaData data )
    {
        properties.put( new Short( (short) s ), data );
    }

    private static EscherPropertyMetaData data( String propName, byte type )
    {
        return new EscherPropertyMetaData( propName, type );
    }

    private static EscherPropertyMetaData data( String propName )
    {
        return new EscherPropertyMetaData( propName );
    }

    public static String getPropertyName( short propertyId )
    {
        initProps();
        EscherPropertyMetaData o = (EscherPropertyMetaData) properties.get( new Short( propertyId ) );
        return o == null ? "unknown" : o.getDescription();
    }

    public static byte getPropertyType( short propertyId )
    {
        initProps();
        EscherPropertyMetaData escherPropertyMetaData = (EscherPropertyMetaData) properties.get( new Short( propertyId ) );
        return escherPropertyMetaData == null ? 0 : escherPropertyMetaData.getType();
    }
}



