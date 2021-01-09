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

module org.apache.poi.scratchpad {
    requires transitive org.apache.poi.poi;
    requires java.desktop;
    requires commons.math3;

    provides org.apache.poi.extractor.ExtractorProvider with org.apache.poi.extractor.ole2.OLE2ScratchpadExtractorFactory;
    provides org.apache.poi.sl.usermodel.SlideShowProvider with org.apache.poi.hslf.usermodel.HSLFSlideShowFactory;
    provides org.apache.poi.sl.draw.ImageRenderer with org.apache.poi.hwmf.draw.HwmfImageRenderer, org.apache.poi.hemf.draw.HemfImageRenderer;

    exports org.apache.poi.hmef;
    exports org.apache.poi.hmef.dev;
    exports org.apache.poi.hmef.extractor;
    exports org.apache.poi.hmef.attribute;
    exports org.apache.poi.hdgf;
    exports org.apache.poi.hdgf.dev;
    exports org.apache.poi.hdgf.streams;
    exports org.apache.poi.hdgf.extractor;
    exports org.apache.poi.hdgf.pointers;
    exports org.apache.poi.hdgf.exceptions;
    exports org.apache.poi.hdgf.chunks;
    exports org.apache.poi.hwpf;
    exports org.apache.poi.hwpf.dev;
    exports org.apache.poi.hwpf.sprm;
    exports org.apache.poi.hwpf.converter;
    exports org.apache.poi.hwpf.extractor;
    exports org.apache.poi.hwpf.usermodel;
    exports org.apache.poi.hwpf.model;
    exports org.apache.poi.hwpf.model.io;
    exports org.apache.poi.hwpf.model.types;
    exports org.apache.poi.hwmf.record;
    exports org.apache.poi.hwmf.draw;
    exports org.apache.poi.hwmf.usermodel;
    exports org.apache.poi.extractor.ole2;
    exports org.apache.poi.hpbf;
    exports org.apache.poi.hpbf.dev;
    exports org.apache.poi.hpbf.extractor;
    exports org.apache.poi.hpbf.model;
    exports org.apache.poi.hpbf.model.qcbits;
    exports org.apache.poi.hslf.dev;
    exports org.apache.poi.hslf.record;
    exports org.apache.poi.hslf.extractor;
    exports org.apache.poi.hslf.exceptions;
    exports org.apache.poi.hslf.usermodel;
    exports org.apache.poi.hslf.blip;
    exports org.apache.poi.hslf.model;
    exports org.apache.poi.hslf.model.textproperties;
    exports org.apache.poi.hslf.util;
    exports org.apache.poi.hssf.converter;
    exports org.apache.poi.hsmf;
    exports org.apache.poi.hsmf.dev;
    exports org.apache.poi.hsmf.datatypes;
    exports org.apache.poi.hsmf.extractor;
    exports org.apache.poi.hsmf.exceptions;
    exports org.apache.poi.hsmf.parsers;
    exports org.apache.poi.hemf.record.emf;
    exports org.apache.poi.hemf.record.emfplus;
    exports org.apache.poi.hemf.draw;
    exports org.apache.poi.hemf.usermodel;


    // test specific exports
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.mockito;

    exports org.apache.poi.hemf.hemfplus.extractor to org.junit.platform.commons;
    exports org.apache.poi.hslf to org.junit.platform.commons;
    exports org.apache.poi.hwmf to org.junit.platform.commons;
    exports org.apache.poi.hwpf.util to org.junit.platform.commons;

    opens org.apache.poi.hwpf.model to org.mockito, org.junit.platform.commons;
    opens org.apache.poi.hwpf.model.types to org.mockito, org.junit.platform.commons;

    opens org.apache.poi.hmef to org.junit.platform.commons;
    opens org.apache.poi.hmef.dev to org.junit.platform.commons;
    opens org.apache.poi.hmef.extractor to org.junit.platform.commons;
    opens org.apache.poi.hmef.attribute to org.junit.platform.commons;
    opens org.apache.poi.hdgf to org.junit.platform.commons;
    opens org.apache.poi.hdgf.dev to org.junit.platform.commons;
    opens org.apache.poi.hdgf.streams to org.junit.platform.commons;
    opens org.apache.poi.hdgf.extractor to org.junit.platform.commons;
    opens org.apache.poi.hdgf.pointers to org.junit.platform.commons;
    opens org.apache.poi.hdgf.exceptions to org.junit.platform.commons;
    opens org.apache.poi.hdgf.chunks to org.junit.platform.commons;
    opens org.apache.poi.hwpf to org.junit.platform.commons;
    opens org.apache.poi.hwpf.dev to org.junit.platform.commons;
    opens org.apache.poi.hwpf.sprm to org.junit.platform.commons;
    opens org.apache.poi.hwpf.converter to org.junit.platform.commons;
    opens org.apache.poi.hwpf.extractor to org.junit.platform.commons;
    opens org.apache.poi.hwpf.usermodel to org.junit.platform.commons;
    opens org.apache.poi.hwpf.model.io to org.junit.platform.commons;
    opens org.apache.poi.hwpf.util to org.junit.platform.commons;
    opens org.apache.poi.hwmf to org.junit.platform.commons;
    opens org.apache.poi.hwmf.record to org.junit.platform.commons;
    opens org.apache.poi.hwmf.draw to org.junit.platform.commons;
    opens org.apache.poi.hwmf.usermodel to org.junit.platform.commons;
    opens org.apache.poi.extractor.ole2 to org.junit.platform.commons;
    opens org.apache.poi.hpbf to org.junit.platform.commons;
    opens org.apache.poi.hpbf.dev to org.junit.platform.commons;
    opens org.apache.poi.hpbf.extractor to org.junit.platform.commons;
    opens org.apache.poi.hpbf.model to org.junit.platform.commons;
    opens org.apache.poi.hpbf.model.qcbits to org.junit.platform.commons;
    opens org.apache.poi.hslf to org.junit.platform.commons;
    opens org.apache.poi.hslf.dev to org.junit.platform.commons;
    opens org.apache.poi.hslf.record to org.junit.platform.commons;
    opens org.apache.poi.hslf.extractor to org.junit.platform.commons;
    opens org.apache.poi.hslf.exceptions to org.junit.platform.commons;
    opens org.apache.poi.hslf.usermodel to org.junit.platform.commons;
    opens org.apache.poi.hslf.blip to org.junit.platform.commons;
    opens org.apache.poi.hslf.model to org.junit.platform.commons;
    opens org.apache.poi.hslf.model.textproperties to org.junit.platform.commons;
    opens org.apache.poi.hslf.util to org.junit.platform.commons;
    opens org.apache.poi.hssf.converter to org.junit.platform.commons;
    opens org.apache.poi.hsmf to org.junit.platform.commons;
    opens org.apache.poi.hsmf.dev to org.junit.platform.commons;
    opens org.apache.poi.hsmf.datatypes to org.junit.platform.commons;
    opens org.apache.poi.hsmf.extractor to org.junit.platform.commons;
    opens org.apache.poi.hsmf.exceptions to org.junit.platform.commons;
    opens org.apache.poi.hsmf.parsers to org.junit.platform.commons;
    opens org.apache.poi.hemf.record.emf to org.junit.platform.commons;
    opens org.apache.poi.hemf.record.emfplus to org.junit.platform.commons;
    opens org.apache.poi.hemf.hemfplus.extractor to org.junit.platform.commons;
    opens org.apache.poi.hemf.draw to org.junit.platform.commons;
    opens org.apache.poi.hemf.usermodel to org.junit.platform.commons;

}