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

module org.apache.poi.ooxml {
    requires transitive org.apache.poi.poi;
    requires transitive org.apache.xmlbeans;
    requires transitive org.apache.poi.ooxml.schemas;
    requires org.apache.commons.collections4;
    requires org.apache.commons.codec;
    requires commons.math3;
    requires SparseBitSet;
    requires org.slf4j;
    requires java.logging;
    requires java.desktop;
    requires java.security.jgss;

    provides org.apache.poi.extractor.ExtractorProvider with org.apache.poi.ooxml.extractor.POIXMLExtractorFactory;
    provides org.apache.poi.ss.usermodel.WorkbookProvider with org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
    provides org.apache.poi.sl.usermodel.SlideShowProvider with org.apache.poi.xslf.usermodel.XSLFSlideShowFactory;
    provides org.apache.poi.sl.draw.ImageRenderer with org.apache.poi.xslf.draw.SVGImageRenderer;

    exports org.apache.poi.xwpf.extractor;
    exports org.apache.poi.xwpf.usermodel;
    exports org.apache.poi.xwpf.model;
    exports org.apache.poi.xdgf.extractor;
    exports org.apache.poi.xdgf.exceptions;
    exports org.apache.poi.xdgf.usermodel;
    exports org.apache.poi.xdgf.usermodel.section;
    exports org.apache.poi.xdgf.usermodel.section.geometry;
    exports org.apache.poi.xdgf.usermodel.shape;
    exports org.apache.poi.xdgf.usermodel.shape.exceptions;
    exports org.apache.poi.xdgf.xml;
    exports org.apache.poi.xdgf.util;
    exports org.apache.poi.xdgf.geom;
    exports org.apache.poi.ooxml;
    exports org.apache.poi.ooxml.dev;
    exports org.apache.poi.ooxml.extractor;
    exports org.apache.poi.ooxml.util;
    exports org.apache.poi.xddf.usermodel;
    exports org.apache.poi.xddf.usermodel.text;
    exports org.apache.poi.xddf.usermodel.chart;
    exports org.apache.poi.openxml4j.exceptions;
    exports org.apache.poi.openxml4j.opc;
    exports org.apache.poi.openxml4j.opc.internal;
    exports org.apache.poi.openxml4j.opc.internal.marshallers;
    exports org.apache.poi.openxml4j.opc.internal.unmarshallers;
    exports org.apache.poi.openxml4j.util;
    exports org.apache.poi.xssf;
    exports org.apache.poi.xssf.extractor;
    exports org.apache.poi.xssf.eventusermodel;
    exports org.apache.poi.xssf.usermodel;
    exports org.apache.poi.xssf.usermodel.helpers;
    exports org.apache.poi.xssf.usermodel.extensions;
    exports org.apache.poi.xssf.binary;
    exports org.apache.poi.xssf.model;
    exports org.apache.poi.xssf.streaming;
    exports org.apache.poi.xssf.util;
    exports org.apache.poi.xslf.draw;
    exports org.apache.poi.xslf.usermodel;
    exports org.apache.poi.xslf.model;
    exports org.apache.poi.xslf.util;
    exports org.apache.poi.poifs.crypt.dsig;
    exports org.apache.poi.poifs.crypt.dsig.facets;
    exports org.apache.poi.poifs.crypt.dsig.services;
    exports org.apache.poi.poifs.crypt.temp;

    opens org.apache.poi.openxml4j.opc to org.apache.poi.poi;


    /* optional dependencies for xml signatures - you need to add a require entry to your module-info
     * or add them via the --add-modules JVM argument */
    requires java.xml.crypto;
    requires org.apache.santuario.xmlsec;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;

    /* optional dependencies for slideshow rendering via PPTX2PNG */
    requires batik.all;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires de.rototor.pdfbox.graphics2d;
    requires xmlgraphics.commons;
}