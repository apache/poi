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

package org.apache.poi.sl.draw.geom;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
class PresetParser {
    enum Mode {
        FILE(PresetParser::updateFile),
        SHAPE_LST(PresetParser::updateShapeList),
        SHAPE(PresetParser::updateShape),
        GUIDE_LST(PresetParser::updateGuideList),
        AH_LST(PresetParser::updateAhList),
        CXN_LST(PresetParser::updateCxnList),
        PATH_LST(PresetParser::updatePathLst),
        PATH(PresetParser::updatePath);

        interface Handler {
            void update(PresetParser parser, XMLStreamReader sr) throws XMLStreamException;
        }

        final Handler handler;

        Mode(Handler handler) {
            this.handler = handler;
        }
    }

    private final static POILogger LOG = POILogFactory.getLogger(PresetParser.class);

    private Mode mode;

    private final Map<String, CustomGeometry> geom = new HashMap<>();
    private CustomGeometry customGeometry;
    private boolean useAdjustValue;
    private Path path;


    PresetParser(Mode mode) {
        this.mode = mode;
        if (mode == Mode.SHAPE) {
            customGeometry = new CustomGeometry();
            geom.put("custom", customGeometry);
        }
    }

    void parse(XMLStreamReader sr) throws XMLStreamException {
        while (sr.hasNext()) {
            switch (sr.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    mode.handler.update(this, sr);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    endContext();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                default:
                    break;
            }
        }
    }

    Map<String, CustomGeometry> getGeom() {
        return geom;
    }

    private void updateFile(XMLStreamReader sr) {
        final String name = sr.getLocalName();
        assert("presetShapeDefinitons".equals(name));
        mode = Mode.SHAPE_LST;
    }

    private void updateShapeList(XMLStreamReader sr) {
        final String name = sr.getLocalName();
        customGeometry = new CustomGeometry();
        if (geom.containsKey(name)) {
            LOG.log(POILogger.WARN, "Duplicate definition of " + name);
        }
        geom.put(name, customGeometry);
        mode = Mode.SHAPE;
    }

    private void updateShape(XMLStreamReader sr) throws XMLStreamException {
        final String name = sr.getLocalName();
        switch (name) {
            case "avLst":
                useAdjustValue = true;
                mode = Mode.GUIDE_LST;
                break;
            case "gdLst":
                useAdjustValue = false;
                mode = Mode.GUIDE_LST;
                break;
            case "ahLst":
                mode = Mode.AH_LST;
                break;
            case "cxnLst":
                mode = Mode.CXN_LST;
                break;
            case "rect":
                addRectangle(sr);
                break;
            case "pathLst":
                mode = Mode.PATH_LST;
                break;
        }
    }

    private void updateGuideList(XMLStreamReader sr) throws XMLStreamException {
        final String name = sr.getLocalName();
        assert("gd".equals(name));
        final Guide gd;
        if (useAdjustValue) {
            customGeometry.addAdjustGuide((AdjustValue)(gd = new AdjustValue()));
        } else {
            customGeometry.addGeomGuide(gd = new Guide());
        }

        parseAttributes(sr, (key,val) -> {
            switch (key) {
                case "name":
                    // CollapsedStringAdapter
                    gd.setName(collapseString(val));
                    break;
                case "fmla":
                    gd.setFmla(val);
                    break;
            }
        });

        int tag = nextTag(sr);
        assert(tag == XMLStreamConstants.END_ELEMENT);
    }


    private void updateAhList(XMLStreamReader sr) throws XMLStreamException {
        String name = sr.getLocalName();
        switch (name) {
            case "ahXY":
                addXY(sr);
                break;

            case "ahPolar":
                addPolar(sr);
                break;
        }
    }

    private void addXY(XMLStreamReader sr) throws XMLStreamException {
        XYAdjustHandle ahXY = new XYAdjustHandle();
        customGeometry.addAdjustHandle(ahXY);

        parseAttributes(sr, (key, val) -> {
            switch (key) {
                case "gdRefX":
                    ahXY.setGdRefX(collapseString(val));
                    break;
                case "minX":
                    ahXY.setMinX(val);
                    break;
                case "maxX":
                    ahXY.setMaxX(val);
                    break;
                case "gdRefY":
                    ahXY.setGdRefY(collapseString(val));
                    break;
                case "minY":
                    ahXY.setMinY(val);
                    break;
                case "maxY":
                    ahXY.setMaxY(val);
                    break;
            }
        });

        ahXY.setPos(parsePosPoint(sr));
    }

    private void addPolar(XMLStreamReader sr) throws XMLStreamException {
        PolarAdjustHandle ahPolar = new PolarAdjustHandle();
        customGeometry.addAdjustHandle(ahPolar);

        parseAttributes(sr, (key, val) -> {
            switch (key) {
                case "gdRefR":
                    ahPolar.setGdRefR(collapseString(val));
                    break;
                case "minR":
                    ahPolar.setMinR(val);
                    break;
                case "maxR":
                    ahPolar.setMaxR(val);
                    break;
                case "gdRefAng":
                    ahPolar.setGdRefAng(collapseString(val));
                    break;
                case "minAng":
                    ahPolar.setMinAng(val);
                    break;
                case "maxAng":
                    ahPolar.setMaxAng(val);
                    break;
            }
        });

        ahPolar.setPos(parsePosPoint(sr));
    }

    private void updateCxnList(XMLStreamReader sr) throws XMLStreamException {
        String name = sr.getLocalName();
        assert("cxn".equals(name));

        ConnectionSite cxn = new ConnectionSite();
        customGeometry.addConnectionSite(cxn);

        parseAttributes(sr, (key, val) -> {
            if ("ang".equals(key)) {
                cxn.setAng(val);
            }
        });

        cxn.setPos(parsePosPoint(sr));
    }

    private void updatePathLst(XMLStreamReader sr) {
        String name = sr.getLocalName();
        assert("path".equals(name));

        path = new Path();
        customGeometry.addPath(path);

        parseAttributes(sr, (key, val) -> {
            switch (key) {
                case "w":
                    path.setW(Long.parseLong(val));
                    break;
                case "h":
                    path.setH(Long.parseLong(val));
                    break;
                case "fill":
                    path.setFill(mapFill(val));
                    break;
                case "stroke":
                    path.setStroke(Boolean.parseBoolean(val));
                    break;
                case "extrusionOk":
                    path.setExtrusionOk(Boolean.parseBoolean(val));
                    break;
            }
        });

        mode = Mode.PATH;
    }

    private static PaintModifier mapFill(String fill) {
        switch (fill) {
            default:
            case "none":
                return PaintModifier.NONE;
            case "norm":
                return PaintModifier.NORM;
            case "lighten":
                return PaintModifier.LIGHTEN;
            case "lightenLess":
                return PaintModifier.LIGHTEN_LESS;
            case "darken":
                return PaintModifier.DARKEN;
            case "darkenLess":
                return PaintModifier.DARKEN_LESS;
        }
    }

    private void updatePath(XMLStreamReader sr) throws XMLStreamException {
        String name = sr.getLocalName();
        switch (name) {
            case "close":
                closePath(sr);
                break;
            case "moveTo":
                moveTo(sr);
                break;
            case "lnTo":
                lineTo(sr);
                break;
            case "arcTo":
                arcTo(sr);
                break;
            case "quadBezTo":
                quadBezTo(sr);
                break;
            case "cubicBezTo":
                cubicBezTo(sr);
                break;
        }
    }

    private void closePath(XMLStreamReader sr) throws XMLStreamException {
        path.addCommand(new ClosePathCommand());
        int tag = nextTag(sr);
        assert(tag == XMLStreamConstants.END_ELEMENT);
    }

    private void moveTo(XMLStreamReader sr) throws XMLStreamException {
        MoveToCommand cmd = new MoveToCommand();
        path.addCommand(cmd);

        AdjustPoint pt = parsePtPoint(sr, true);
        assert(pt != null);
        cmd.setPt(pt);
    }

    private void lineTo(XMLStreamReader sr) throws XMLStreamException {
        LineToCommand cmd = new LineToCommand();
        path.addCommand(cmd);

        AdjustPoint pt = parsePtPoint(sr, true);
        assert(pt != null);
        cmd.setPt(pt);
    }

    private void arcTo(XMLStreamReader sr) throws XMLStreamException {
        ArcToCommand cmd = new ArcToCommand();
        path.addCommand(cmd);
        parseAttributes(sr, (key, val) -> {
            switch (key) {
                case "wR":
                    cmd.setWR(val);
                    break;
                case "hR":
                    cmd.setHR(val);
                    break;
                case "stAng":
                    cmd.setStAng(val);
                    break;
                case "swAng":
                    cmd.setSwAng(val);
                    break;
            }
        });
        int tag = nextTag(sr);
        assert (tag == XMLStreamConstants.END_ELEMENT);
    }

    private void quadBezTo(XMLStreamReader sr) throws XMLStreamException {
        QuadToCommand cmd = new QuadToCommand();
        path.addCommand(cmd);
        AdjustPoint pt1 = parsePtPoint(sr, false);
        AdjustPoint pt2 = parsePtPoint(sr, true);
        assert (pt1 != null && pt2 != null);
        cmd.setPt1(pt1);
        cmd.setPt2(pt2);
    }

    private void cubicBezTo(XMLStreamReader sr) throws XMLStreamException {
        CurveToCommand cmd = new CurveToCommand();
        path.addCommand(cmd);
        AdjustPoint pt1 = parsePtPoint(sr, false);
        AdjustPoint pt2 = parsePtPoint(sr, false);
        AdjustPoint pt3 = parsePtPoint(sr, true);
        assert (pt1 != null && pt2 != null && pt3 != null);
        cmd.setPt1(pt1);
        cmd.setPt2(pt2);
        cmd.setPt3(pt3);
    }

    private void addRectangle(XMLStreamReader sr) throws XMLStreamException {
        String[] ltrb = new String[4];
        parseAttributes(sr, (key,val) -> {
            switch (key) {
                case "l":
                    ltrb[0] = val;
                    break;
                case "t":
                    ltrb[1] = val;
                    break;
                case "r":
                    ltrb[2] = val;
                    break;
                case "b":
                    ltrb[3] = val;
                    break;
            }
        });

        customGeometry.setTextBounds(ltrb[0],ltrb[1],ltrb[2],ltrb[3]);

        int tag = nextTag(sr);
        assert(tag == XMLStreamConstants.END_ELEMENT);
    }


    private void endContext() {
        switch (mode) {
            case FILE:
            case SHAPE_LST:
                mode = Mode.FILE;
                break;
            case SHAPE:
                mode = Mode.SHAPE_LST;
                break;
            case CXN_LST:
            case AH_LST:
            case GUIDE_LST:
            case PATH_LST:
                useAdjustValue = false;
                path = null;
                mode = Mode.SHAPE;
                break;
            case PATH:
                path = null;
                mode = Mode.PATH_LST;
                break;
        }
    }

    private AdjustPoint parsePosPoint(XMLStreamReader sr) throws XMLStreamException {
        return parseAdjPoint(sr, true, "pos");
    }

    private AdjustPoint parsePtPoint(XMLStreamReader sr, boolean closeOuter) throws XMLStreamException {
        return parseAdjPoint(sr, closeOuter, "pt");
    }

    private AdjustPoint parseAdjPoint(XMLStreamReader sr, boolean closeOuter, String name) throws XMLStreamException {
        int tag = nextTag(sr);
        if (tag == XMLStreamConstants.END_ELEMENT) {
            return null;
        }

        assert (name.equals(sr.getLocalName()));

        AdjustPoint pos = new AdjustPoint();
        parseAttributes(sr, (key, val) -> {
            switch (key) {
                case "x":
                    pos.setX(val);
                    break;
                case "y":
                    pos.setY(val);
                    break;
            }
        });
        tag = nextTag(sr);
        assert(tag == XMLStreamConstants.END_ELEMENT);
        if (closeOuter) {
            tag = nextTag(sr);
            assert(tag == XMLStreamConstants.END_ELEMENT);
        }
        return pos;
    }

    private void parseAttributes(XMLStreamReader sr, BiConsumer<String,String> c) {
        for (int i=0; i<sr.getAttributeCount(); i++) {
            c.accept(sr.getAttributeLocalName(i), sr.getAttributeValue(i));
        }
    }

    /**
     * Reimplement {@link XMLStreamReader#nextTag()} because of differences of XmlBeans and Xerces XMLStreamReader.
     * XmlBeans doesn't return the END_ELEMENT on nextTag()
     *
     * @param sr the stream reader
     * @return the next tag type (START_ELEMENT, END_ELEMENT, END_DOCUMENT)
     * @throws XMLStreamException if reading the next tag fails
     */
    private static int nextTag(XMLStreamReader sr) throws XMLStreamException {
        int tag;
        do {
            tag = sr.next();
        } while (
            tag != XMLStreamConstants.START_ELEMENT &&
            tag != XMLStreamConstants.END_ELEMENT &&
            tag != XMLStreamConstants.END_DOCUMENT
        );
        return tag;
    }

    /**
     * Removes leading and trailing whitespaces of the string given as the parameter, then truncate any
     * sequence of tab, CR, LF, and SP by a single whitespace character ' '.
     */
    private static String collapseString(String text) {
        if (text==null) {
            return null;
        }

        int len = text.length();

        // most of the texts are already in the collapsed form. so look for the first whitespace in the hope that we
        // will never see it.
        int s;
        for (s=0; s<len; s++) {
            if (isWhiteSpace(text.charAt(s))) {
                break;
            }
        }

        if (s == len) {
            // the input happens to be already collapsed.
            return text;
        }

        // we now know that the input contains spaces. let's sit down and do the collapsing normally.
        // allocate enough size to avoid re-allocation
        StringBuilder result = new StringBuilder(len);

        if (s != 0) {
            for(int i=0; i<s; i++) {
                result.append(text.charAt(i));
            }
            result.append(' ');
        }

        boolean inStripMode = true;
        for (int i = s+1; i < len; i++) {
            char ch = text.charAt(i);
            boolean b = isWhiteSpace(ch);

            if (inStripMode && b) {
                // skip this character
                continue;
            }

            inStripMode = b;
            result.append(inStripMode ? ' ' : ch);
        }

        // remove trailing whitespaces
        len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ') {
            result.setLength(len - 1);
        }

        // whitespaces are already collapsed, so all we have to do is
        // to remove the last one character if it's a whitespace.
        return result.toString();
    }

    /** returns true if the specified char is a white space character. */
    private static boolean isWhiteSpace(char ch) {
        return ch == 0x9 || ch == 0xA || ch == 0xD || ch == 0x20;
    }
}
