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

package org.apache.poi.sl.usermodel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Preset colors defined in DrawingML aka known/system colors
 * 
 * @see <a href="https://msdn.microsoft.com/library/system.drawing.knowncolor.aspx">KnownColor Enumeration</a>
 * @see <a href="https://msdn.microsoft.com/library/system.windows.media.colors.aspx">Colors Class</a>
 */
public enum PresetColor {
    // the order of this enum can be found in the definition of .net System.Drawing.KnownColor enumeration
    // or by running the the program in the linked documentation
    
    // default colors for theme-depending colors taken from ... (last post):
    // https://social.technet.microsoft.com/Forums/windows/en-US/ac76cc56-6ff2-4778-b260-8141d7170a3b/windows-7-highlight-text-color-or-selected-text-color-in-aero
    
    // see ST_SystemColorVal for system color names
    
    /** The system-defined color of the active window's border. */
    ActiveBorder            (0xffb4b4b4,   1, "activeBorder"),
    /** The system-defined color of the background of the active window's title bar. */
    ActiveCaption           (0xff99b4d1,   2, "activeCaption"),
    /** The system-defined color of the text in the active window's title bar. */
    ActiveCaptionText       (0xff000000,   3, "captionText"),
    /** The application workspace is the area in a multiple-document view that is not being occupied by documents. */
    AppWorkspace            (0xffababab,   4, "appWorkspace"),
    /** The system-defined face color of a 3-D element. */
    Control                 (0xfff0f0f0,   5, "btnFace"),
    /** The system-defined shadow color of a 3-D element. The shadow color is applied to parts of a 3-D element that face away from the light source. */
    ControlDark             (0xff696969,   6, "btnShadow"),
    /** The system-defined color that is the dark shadow color of a 3-D element. The dark shadow color is applied to the parts of a 3-D element that are the darkest color. */
    ControlDarkDark         (0xff000000,   7, "3dDkShadow"),
    /** The system-defined color that is the light color of a 3-D element. The light color is applied to parts of a 3-D element that face the light source. */
    ControlLight            (0xffe3e3e3,   8, "btnHighlight"),
    /** The system-defined highlight color of a 3-D element. The highlight color is applied to the parts of a 3-D element that are the lightest color. */
    ControlLightLight       (0xffe3e3e3,   9, "3dLight"),
    /** The system-defined color of text in a 3-D element. */
    ControlText             (0xff000000,  10, "btnText"),
    /** The system-defined color of the desktop. */
    Desktop                 (0xff000000,  11, "background"),
    /** The system-defined color of dimmed text. Items in a list that are disabled are displayed in dimmed text. */
    GrayText                (0xff6d6d6d,  12, "grayText"),
    /** The system-defined color of the background of selected items. This includes selected menu items as well as selected text. */
    Highlight               (0xff3399ff,  13, "highlight"),
    /** The system-defined color of the text of selected items. */
    HighlightText           (0xffffffff,  14, "highlightText"),
    /** The system-defined color used to designate a hot-tracked item. Single-clicking a hot-tracked item executes the item. */
    HotTrack                (0xff0066cc,  15, "hotLight"),
    /** The system-defined color of an inactive window's border. */
    InactiveBorder          (0xfff4f7fc,  16, "inactiveBorder"),
    /** The system-defined color of the background of an inactive window's title bar. */
    InactiveCaption         (0xffbfcddb,  17, "inactiveCaption"),
    /** The system-defined color of the text in an inactive window's title bar. */
    InactiveCaptionText     (0xff000000,  18, "inactiveCaptionText"),
    /** The system-defined color of the background of a ToolTip. */
    Info                    (0xffffffe1,  19, "infoBk"),
    /** The system-defined color of the text of a ToolTip. */
    InfoText                (0xff000000,  20, "infoText"),
    /** The system-defined color of a menu's background. */
    Menu                    (0xfff0f0f0,  21, "menu"),
    /** The system-defined color of a menu's text. */
    MenuText                (0xff000000,  22, "menuText"),
    /** The system-defined color of the background of a scroll bar. */
    ScrollBar               (0xffc8c8c8,  23, "scrollBar"),
    /** The system-defined color of the background in the client area of a window. */
    Window                  (0xffffffff,  24, "window"),
    /** The system-defined color of a window frame. */
    WindowFrame             (0xff646464,  25, "windowFrame"),
    /** The system-defined color of the text in the client area of a window. */
    WindowText              (0xff000000,  26, "windowText"),
    Transparent             (0x00ffffff,  27, null),
    AliceBlue               (0xfff0f8ff,  28, "aliceBlue"),
    AntiqueWhite            (0xfffaebd7,  29, "antiqueWhite"),
    Aqua                    (0xff00ffff,  30, "aqua"),
    Aquamarine              (0xff7fffd4,  31, "aquamarine"),
    Azure                   (0xfff0ffff,  32, "azure"),
    Beige                   (0xfff5f5dc,  33, "beige"),
    Bisque                  (0xffffe4c4,  34, "bisque"),
    Black                   (0xff000000,  35, "black"),
    BlanchedAlmond          (0xffffebcd,  36, "blanchedAlmond"),
    Blue                    (0xff0000ff,  37, "blue"),
    BlueViolet              (0xff8a2be2,  38, "blueViolet"),
    Brown                   (0xffa52a2a,  39, "brown"),
    BurlyWood               (0xffdeb887,  40, "burlyWood"),
    CadetBlue               (0xff5f9ea0,  41, "cadetBlue"),
    Chartreuse              (0xff7fff00,  42, "chartreuse"),
    Chocolate               (0xffd2691e,  43, "chocolate"),
    Coral                   (0xffff7f50,  44, "coral"),
    CornflowerBlue          (0xff6495ed,  45, "cornflowerBlue"),
    Cornsilk                (0xfffff8dc,  46, "cornsilk"),
    Crimson                 (0xffdc143c,  47, "crimson"),
    Cyan                    (0xff00ffff,  48, "cyan"),
    DarkBlue                (0xff00008b,  49, "dkBlue"),
    DarkCyan                (0xff008b8b,  50, "dkCyan"),
    DarkGoldenrod           (0xffb8860b,  51, "dkGoldenrod"),
    DarkGray                (0xffa9a9a9,  52, "dkGray"),
    DarkGreen               (0xff006400,  53, "dkGreen"),
    DarkKhaki               (0xffbdb76b,  54, "dkKhaki"),
    DarkMagenta             (0xff8b008b,  55, "dkMagenta"),
    DarkOliveGreen          (0xff556b2f,  56, "dkOliveGreen"),
    DarkOrange              (0xffff8c00,  57, "dkOrange"),
    DarkOrchid              (0xff9932cc,  58, "dkOrchid"),
    DarkRed                 (0xff8b0000,  59, "dkRed"),
    DarkSalmon              (0xffe9967a,  60, "dkSalmon"),
    DarkSeaGreen            (0xff8fbc8b,  61, "dkSeaGreen"),
    DarkSlateBlue           (0xff483d8b,  62, "dkSlateBlue"),
    DarkSlateGray           (0xff2f4f4f,  63, "dkSlateGray"),
    DarkTurquoise           (0xff00ced1,  64, "dkTurquoise"),
    DarkViolet              (0xff9400d3,  65, "dkViolet"),
    DeepPink                (0xffff1493,  66, "deepPink"),
    DeepSkyBlue             (0xff00bfff,  67, "deepSkyBlue"),
    DimGray                 (0xff696969,  68, "dimGray"),
    DodgerBlue              (0xff1e90ff,  69, "dodgerBlue"),
    Firebrick               (0xffb22222,  70, "firebrick"),
    FloralWhite             (0xfffffaf0,  71, "floralWhite"),
    ForestGreen             (0xff228b22,  72, "forestGreen"),
    Fuchsia                 (0xffff00ff,  73, "fuchsia"),
    Gainsboro               (0xffdcdcdc,  74, "gainsboro"),
    GhostWhite              (0xfff8f8ff,  75, "ghostWhite"),
    Gold                    (0xffffd700,  76, "gold"),
    Goldenrod               (0xffdaa520,  77, "goldenrod"),
    Gray                    (0xff808080,  78, "gray"),
    Green                   (0xff008000,  79, "green"),
    GreenYellow             (0xffadff2f,  80, "greenYellow"),
    Honeydew                (0xfff0fff0,  81, "honeydew"),
    HotPink                 (0xffff69b4,  82, "hotPink"),
    IndianRed               (0xffcd5c5c,  83, "indianRed"),
    Indigo                  (0xff4b0082,  84, "indigo"),
    Ivory                   (0xfffffff0,  85, "ivory"),
    Khaki                   (0xfff0e68c,  86, "khaki"),
    Lavender                (0xffe6e6fa,  87, "lavender"),
    LavenderBlush           (0xfffff0f5,  88, "lavenderBlush"),
    LawnGreen               (0xff7cfc00,  89, "lawnGreen"),
    LemonChiffon            (0xfffffacd,  90, "lemonChiffon"),
    LightBlue               (0xffadd8e6,  91, "ltBlue"),
    LightCoral              (0xfff08080,  92, "ltCoral"),
    LightCyan               (0xffe0ffff,  93, "ltCyan"),
    LightGoldenrodYellow    (0xfffafa78,  94, "ltGoldenrodYellow"),
    LightGray               (0xffd3d3d3,  95, "ltGray"),
    LightGreen              (0xff90ee90,  96, "ltGreen"),
    LightPink               (0xffffb6c1,  97, "ltPink"),
    LightSalmon             (0xffffa07a,  98, "ltSalmon"),
    LightSeaGreen           (0xff20b2aa,  99, "ltSeaGreen"),
    LightSkyBlue            (0xff87cefa, 100, "ltSkyBlue"),
    LightSlateGray          (0xff778899, 101, "ltSlateGray"),
    LightSteelBlue          (0xffb0c4de, 102, "ltSteelBlue"),
    LightYellow             (0xffffffe0, 103, "ltYellow"),
    Lime                    (0xff00ff00, 104, "lime"),
    LimeGreen               (0xff32cd32, 105, "limeGreen"),
    Linen                   (0xfffaf0e6, 106, "linen"),
    Magenta                 (0xffff00ff, 107, "magenta"),
    Maroon                  (0xff800000, 108, "maroon"),
    MediumAquamarine        (0xff66cdaa, 109, "medAquamarine"),
    MediumBlue              (0xff0000cd, 110, "medBlue"),
    MediumOrchid            (0xffba55d3, 111, "medOrchid"),
    MediumPurple            (0xff9370db, 112, "medPurple"),
    MediumSeaGreen          (0xff3cb371, 113, "medSeaGreen"),
    MediumSlateBlue         (0xff7b68ee, 114, "medSlateBlue"),
    MediumSpringGreen       (0xff00fa9a, 115, "medSpringGreen"),
    MediumTurquoise         (0xff48d1cc, 116, "medTurquoise"),
    MediumVioletRed         (0xffc71585, 117, "medVioletRed"),
    MidnightBlue            (0xff191970, 118, "midnightBlue"),
    MintCream               (0xfff5fffa, 119, "mintCream"),
    MistyRose               (0xffffe4e1, 120, "mistyRose"),
    Moccasin                (0xffffe4b5, 121, "moccasin"),
    NavajoWhite             (0xffffdead, 122, "navajoWhite"),
    Navy                    (0xff000080, 123, "navy"),
    OldLace                 (0xfffdf5e6, 124, "oldLace"),
    Olive                   (0xff808000, 125, "olive"),
    OliveDrab               (0xff6b8e23, 126, "oliveDrab"),
    Orange                  (0xffffa500, 127, "orange"),
    OrangeRed               (0xffff4500, 128, "orangeRed"),
    Orchid                  (0xffda70d6, 129, "orchid"),
    PaleGoldenrod           (0xffeee8aa, 130, "paleGoldenrod"),
    PaleGreen               (0xff98fb98, 131, "paleGreen"),
    PaleTurquoise           (0xffafeeee, 132, "paleTurquoise"),
    PaleVioletRed           (0xffdb7093, 133, "paleVioletRed"),
    PapayaWhip              (0xffffefd5, 134, "papayaWhip"),
    PeachPuff               (0xffffdab9, 135, "peachPuff"),
    Peru                    (0xffcd853f, 136, "peru"),
    Pink                    (0xffffc0cb, 137, "pink"),
    Plum                    (0xffdda0dd, 138, "plum"),
    PowderBlue              (0xffb0e0e6, 139, "powderBlue"),
    Purple                  (0xff800080, 140, "purple"),
    Red                     (0xffff0000, 141, "red"),
    RosyBrown               (0xffbc8f8f, 142, "rosyBrown"),
    RoyalBlue               (0xff4169e1, 143, "royalBlue"),
    SaddleBrown             (0xff8b4513, 144, "saddleBrown"),
    Salmon                  (0xfffa8072, 145, "salmon"),
    SandyBrown              (0xfff4a460, 146, "sandyBrown"),
    SeaGreen                (0xff2e8b57, 147, "seaGreen"),
    SeaShell                (0xfffff5ee, 148, "seaShell"),
    Sienna                  (0xffa0522d, 149, "sienna"),
    Silver                  (0xffc0c0c0, 150, "silver"),
    SkyBlue                 (0xff87ceeb, 151, "skyBlue"),
    SlateBlue               (0xff6a5acd, 152, "slateBlue"),
    SlateGray               (0xff708090, 153, "slateGray"),
    Snow                    (0xfffffafa, 154, "snow"),
    SpringGreen             (0xff00ff7f, 155, "springGreen"),
    SteelBlue               (0xff4682b4, 156, "steelBlue"),
    Tan                     (0xffd2b48c, 157, "tan"),
    Teal                    (0xff008080, 158, "teal"),
    Thistle                 (0xffd8bfd8, 159, "thistle"),
    Tomato                  (0xffff6347, 160, "tomato"),
    Turquoise               (0xff40e0d0, 161, "turquoise"),
    Violet                  (0xffee82ee, 162, "violet"),
    Wheat                   (0xfff5deb3, 163, "wheat"),
    White                   (0xffffffff, 164, "white"),
    WhiteSmoke              (0xfff5f5f5, 165, "whiteSmoke"),
    Yellow                  (0xffffff00, 166, "yellow"),
    YellowGreen             (0xff9acd32, 167, "yellowGreen"),
    /** The system-defined face color of a 3-D element. */
    ButtonFace              (0xfff0f0f0, 168, null),
    /** The system-defined color that is the highlight color of a 3-D element. This color is applied to parts of a 3-D element that face the light source. */
    ButtonHighlight         (0xffffffff, 169, null),
    /** The system-defined color that is the shadow color of a 3-D element. This color is applied to parts of a 3-D element that face away from the light source. */
    ButtonShadow            (0xffa0a0a0, 170, null),
    /** The system-defined color of the lightest color in the color gradient of an active window's title bar. */
    GradientActiveCaption   (0xffb9d1ea, 171, "gradientActiveCaption"),
    /** The system-defined color of the lightest color in the color gradient of an inactive window's title bar. */
    GradientInactiveCaption (0xffd7e4f2, 172, "gradientInactiveCaption"),
    /** The system-defined color of the background of a menu bar. */
    MenuBar                 (0xfff0f0f0, 173, "menuBar"),
    /** The system-defined color used to highlight menu items when the menu appears as a flat menu. */
    MenuHighlight           (0xff3399ff, 174, "menuHighlight")
    ;

    public final Color color;
    public final int nativeId;
    public final String ooxmlId;

    PresetColor(Integer rgb, int nativeId, String ooxmlId) {
        this.color = (rgb == null) ? null : new Color(rgb, true);
        this.nativeId = nativeId;
        this.ooxmlId = ooxmlId;
    }

    private static final Map<String,PresetColor> lookupOoxmlId;

    static {
        lookupOoxmlId = new HashMap<>();
        for(PresetColor pc : PresetColor.values()) {
            if (pc.ooxmlId != null) {
                lookupOoxmlId.put(pc.ooxmlId, pc);
            }
        }
    }
    
    public static PresetColor valueOfOoxmlId(String ooxmlId) {
        return lookupOoxmlId.get(ooxmlId);
    }
    
    public static PresetColor valueOfNativeId(int nativeId) {
        PresetColor[] vals = values();
        return (0 < nativeId && nativeId <= vals.length) ? vals[nativeId-1] : null;
    }
}
