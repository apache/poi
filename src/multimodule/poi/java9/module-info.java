/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

module org.apache.poi.poi {
    requires org.apache.commons.collections4;
    requires org.apache.commons.codec;
    requires commons.math3;
    requires SparseBitSet;
    requires commons.logging;


    exports org.apache.poi.common.usermodel.fonts;
    exports org.apache.poi.ddf;
    exports org.apache.poi.extractor;
    exports org.apache.poi.hpsf;
    exports org.apache.poi.hpsf.extractor;
    exports org.apache.poi.hpsf.wellknown;
    exports org.apache.poi.hssf;
    exports org.apache.poi.hssf.dev;
    exports org.apache.poi.hssf.eventmodel;
    exports org.apache.poi.hssf.eventusermodel;
    exports org.apache.poi.hssf.eventusermodel.dummyrecord;
    exports org.apache.poi.hssf.extractor;
    exports org.apache.poi.hssf.model;
    exports org.apache.poi.hssf.record;
    exports org.apache.poi.hssf.record.aggregates;
    exports org.apache.poi.hssf.record.cf;
    exports org.apache.poi.hssf.record.chart;
    exports org.apache.poi.hssf.record.common;
    exports org.apache.poi.hssf.record.cont;
    exports org.apache.poi.hssf.record.crypto;
    exports org.apache.poi.hssf.record.pivottable;
    exports org.apache.poi.hssf.usermodel;
    exports org.apache.poi.hssf.usermodel.helpers;
    exports org.apache.poi.hssf.util;
    exports org.apache.poi.poifs.common;
    exports org.apache.poi.poifs.crypt;
    exports org.apache.poi.poifs.crypt.binaryrc4;
    exports org.apache.poi.poifs.crypt.cryptoapi;
    exports org.apache.poi.poifs.crypt.standard;
    exports org.apache.poi.poifs.crypt.xor;
    exports org.apache.poi.poifs.dev;
    exports org.apache.poi.poifs.eventfilesystem;
    exports org.apache.poi.poifs.filesystem;
    exports org.apache.poi.poifs.macros;
    exports org.apache.poi.poifs.nio;
    exports org.apache.poi.poifs.property;
    exports org.apache.poi.poifs.storage;
    exports org.apache.poi.sl.draw;
    exports org.apache.poi.sl.draw.geom;
    exports org.apache.poi.sl.extractor;
    exports org.apache.poi.sl.image;
    exports org.apache.poi.sl.usermodel;
    exports org.apache.poi.ss;
    exports org.apache.poi.ss.extractor;
    exports org.apache.poi.ss.format;
    exports org.apache.poi.ss.formula;
    exports org.apache.poi.ss.formula.atp;
    exports org.apache.poi.ss.formula.constant;
    exports org.apache.poi.ss.formula.eval;
    exports org.apache.poi.ss.formula.eval.forked;
    exports org.apache.poi.ss.formula.function;
    exports org.apache.poi.ss.formula.functions;
    exports org.apache.poi.ss.formula.ptg;
    exports org.apache.poi.ss.formula.udf;
    exports org.apache.poi.ss.usermodel;
    exports org.apache.poi.ss.usermodel.charts;
    exports org.apache.poi.ss.usermodel.helpers;
    exports org.apache.poi.ss.util;
    exports org.apache.poi.ss.util.cellwalk;
    exports org.apache.poi.util;
    exports org.apache.poi.wp.usermodel;
}