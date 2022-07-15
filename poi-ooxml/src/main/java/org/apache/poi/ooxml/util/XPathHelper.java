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

package org.apache.poi.ooxml.util;

import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFactory;

import com.microsoft.schemas.compatibility.AlternateContentDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;

public final class XPathHelper {
    private static final Logger LOG = LogManager.getLogger(XPathHelper.class);

    private static final String OSGI_ERROR =
            "Schemas (*.xsb) for <CLASS> can't be loaded - usually this happens when OSGI " +
                    "loading is used and the thread context classloader has no reference to " +
                    "the xmlbeans classes - please either verify if the <XSB>.xsb is on the " +
                    "classpath or alternatively try to use the poi-ooxml-full-x.x.jar";

    private static final String MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";
    private static final String MAC_DML_NS = "http://schemas.microsoft.com/office/mac/drawingml/2008/main";
    private static final QName ALTERNATE_CONTENT_TAG = new QName(MC_NS, "AlternateContent");
    // AlternateContentDocument.AlternateContent.type.getName();

    private XPathHelper() {}

    static final XPathFactory xpathFactory = XPathFactory.newInstance();
    static {
        trySetFeature(xpathFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
    }

    public static XPathFactory getFactory() {
        return xpathFactory;
    }

    private static void trySetFeature(XPathFactory xpf, String feature, boolean enabled) {
        try {
            xpf.setFeature(feature, enabled);
        } catch (Exception e) {
            LOG.atWarn().withThrowable(e).log("XPathFactory Feature ({}) unsupported", feature);
        } catch (AbstractMethodError ame) {
            LOG.atWarn().withThrowable(ame).log("Cannot set XPathFactory feature ({}) because outdated XML parser in classpath", feature);
        }
    }



    /**
     * Internal code - API may change any time!
     * <p>
     * The XSLFShape.selectProperty(Class, String) xquery method has some performance penalties,
     * which can be workaround by using {@link XmlCursor}. This method also takes into account
     * that {@code AlternateContent} tags can occur anywhere on the given path.
     * <p>
     * It returns the first element found - the search order is:
     * <ul>
     *     <li>searching for a direct child</li>
     *     <li>searching for a AlternateContent.Choice child</li>
     *     <li>searching for a AlternateContent.Fallback child</li>
     * </ul>
     * The factory flag is
     * a workaround to process files based on a later edition. But it comes with the drawback:
     * any change on the returned XmlObject aren't saved back to the underlying document -
     * so it's a non updatable clone. If factory is null, a XmlException is
     * thrown if the AlternateContent is not allowed by the surrounding element or if the
     * extracted object is of the generic type XmlAnyTypeImpl.
     *
     * @param resultClass the requested result class
     * @param factory a factory parse method reference to allow reparsing of elements
     *                extracted from AlternateContent elements. Usually the enclosing XmlBeans type needs to be used
     *                to parse the stream
     * @param path the elements path, each array must contain at least 1 QName,
     *             but can contain additional alternative tags
     * @return the xml object at the path location, or null if not found
     *
     * @throws XmlException If factory is null, a XmlException is
     *      thrown if the AlternateContent is not allowed by the surrounding element or if the
     *      extracted object is of the generic type XmlAnyTypeImpl.
     *
     * @since POI 4.1.2
     */
    @SuppressWarnings("unchecked")
    @Internal
    public static <T extends XmlObject> T selectProperty(XmlObject startObject, Class<T> resultClass, XSLFShape.ReparseFactory<T> factory, QName[]... path)
            throws XmlException {
        XmlObject xo = startObject;
        try (
                XmlCursor cur = startObject.newCursor();
                XmlCursor innerCur = selectProperty(cur, path, 0, factory != null, false)
        ) {
            if (innerCur == null) {
                return null;
            }

            // Pesky XmlBeans bug - see Bugzilla #49934
            // it never happens when using poi-ooxml-full jar but may happen with the abridged poi-ooxml-lite jar
            xo = innerCur.getObject();
            if (xo instanceof XmlAnyTypeImpl) {
                String errorTxt = OSGI_ERROR
                        .replace("<CLASS>", resultClass.getSimpleName())
                        .replace("<XSB>", resultClass.getSimpleName().toLowerCase(Locale.ROOT) + "*");
                if (factory == null) {
                    throw new XmlException(errorTxt);
                } else {
                    xo = factory.parse(innerCur.newXMLStreamReader());
                }
            }

            return (T) xo;
        }
    }

    private static XmlCursor selectProperty(final XmlCursor cur, final QName[][] path, final int offset, final boolean reparseAlternate, final boolean isAlternate)
            throws XmlException {
        // first try the direct children
        for (QName qn : path[offset]) {
            for (boolean found = cur.toChild(qn); found; found = cur.toNextSibling(qn)) {
                if (offset == path.length-1) {
                    return cur;
                }
                cur.push();
                XmlCursor innerCur = selectProperty(cur, path, offset+1, reparseAlternate, false);
                if (innerCur != null) {
                    return innerCur;
                }
                cur.pop();
            }
        }
        // if we were called inside an alternate content handling don't look for alternates again
        if (isAlternate || !cur.toChild(ALTERNATE_CONTENT_TAG)) {
            return null;
        }

        // otherwise check first the choice then the fallback content
        XmlObject xo = cur.getObject();
        AlternateContentDocument.AlternateContent alterCont;
        if (xo instanceof AlternateContentDocument.AlternateContent) {
            alterCont = (AlternateContentDocument.AlternateContent)xo;
        } else {
            // Pesky XmlBeans bug - see Bugzilla #49934
            // it never happens when using poi-ooxml-full jar but may happen with the abridged poi-ooxml-lite jar
            if (!reparseAlternate) {
                throw new XmlException(OSGI_ERROR
                        .replace("<CLASS>", "AlternateContent")
                        .replace("<XSB>", "alternatecontentelement")
                );
            }
            try {
                AlternateContentDocument acd = AlternateContentDocument.Factory.parse(cur.newXMLStreamReader());
                alterCont = acd.getAlternateContent();
            } catch (XmlException e) {
                throw new XmlException("unable to parse AlternateContent element", e);
            }
        }

        final int choices = alterCont.sizeOfChoiceArray();
        for (int i=0; i<choices; i++) {
            // TODO: check [Requires] attribute of [Choice] element, if we can handle the content
            AlternateContentDocument.AlternateContent.Choice choice = alterCont.getChoiceArray(i);
            XmlCursor innerCur = null;
            try (XmlCursor cCur = choice.newCursor()) {
                String requiresNS = cCur.namespaceForPrefix(choice.getRequires());
                if (MAC_DML_NS.equalsIgnoreCase(requiresNS)) {
                    // Mac DML usually contains PDFs ...
                    continue;
                }
                innerCur = selectProperty(cCur, path, offset, reparseAlternate, true);
                if (innerCur != null && innerCur != cCur) {
                    return innerCur;
                }
            }
        }

        if (!alterCont.isSetFallback()) {
            return null;
        }

        XmlCursor fCur = alterCont.getFallback().newCursor();
        XmlCursor innerCur = null;
        try {
            innerCur = selectProperty(fCur, path, offset, reparseAlternate, true);
            return innerCur;
        } finally {
            if (innerCur != fCur) {
                fCur.close();
            }
        }
    }

}
