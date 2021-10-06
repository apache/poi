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

package org.apache.poi.xslf.draw;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.parser.DefaultLengthHandler;
import org.apache.batik.parser.LengthHandler;
import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.util.SVGConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.Internal;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Helper class to base image calculation on actual viewbox instead of the base box (1,1)
 */
@Internal
public class SVGUserAgent extends UserAgentAdapter {
    private static final Logger LOG = LogManager.getLogger(SVGUserAgent.class);
    private Rectangle2D viewbox;

    public SVGUserAgent() {
        addStdFeatures();
    }

    @Override
    public Dimension2D getViewportSize() {
        return viewbox != null
            ? new Dimension2DDouble(viewbox.getWidth(), viewbox.getHeight())
            : super.getViewportSize();
    }

    public Rectangle2D getViewbox() {
        return viewbox != null ? viewbox : new Rectangle2D.Double(0, 0, 1, 1);
    }

    public void initViewbox(SVGDocument doc) {
        viewbox = null;
        SVGSVGElement el = doc.getRootElement();
        if (el == null) {
            return;
        }
        String viewBoxStr = el.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
        if (viewBoxStr != null && !viewBoxStr.isEmpty()) {
            float[] rect = ViewBox.parseViewBoxAttribute(el, viewBoxStr, null);
            viewbox = new Rectangle2D.Float(rect[0], rect[1], rect[2], rect[3]);
            return;
        }

        float w = parseLength(el, SVGConstants.SVG_WIDTH_ATTRIBUTE);
        float h = parseLength(el, SVGConstants.SVG_HEIGHT_ATTRIBUTE);
        if (w != 0 && h != 0) {
            viewbox = new Rectangle2D.Double(0, 0, w, h);
        }
    }

    private static float parseLength(SVGSVGElement el, String attr) {
        String a = el.getAttributeNS(null, attr);
        if (a == null || a.isEmpty()) {
            return 0;
        }
        float[] val = { 0 };
        LengthParser lp = new LengthParser();
        LengthHandler lh = new DefaultLengthHandler() {
            @Override
            public void lengthValue(float v) throws ParseException {
                val[0] = v;
            }
        };
        lp.setLengthHandler(lh);
        lp.parse(a);
        return val[0];
    }

    @Override
    public void displayMessage(String message) {
        LOG.atInfo().log(message);
    }

    @Override
    public void displayError(String message) {
        LOG.atError().log(message);
    }

    @Override
    public void displayError(Exception e) {
        LOG.atError().withThrowable(e).log(e.getMessage());
    }

    @Override
    public void showAlert(String message) {
        LOG.atWarn().log(message);
    }
}
