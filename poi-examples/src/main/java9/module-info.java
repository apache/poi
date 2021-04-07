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

module org.apache.poi.examples {

    requires transitive org.apache.poi.ooxml;
    requires transitive org.apache.poi.scratchpad;
    requires java.xml;

    exports org.apache.poi.examples.crypt;
    exports org.apache.poi.examples.hpsf;
    exports org.apache.poi.examples.hslf;
    exports org.apache.poi.examples.hsmf;
    exports org.apache.poi.examples.hssf.eventusermodel;
    exports org.apache.poi.examples.hssf.usermodel;
    exports org.apache.poi.examples.hwpf;
    exports org.apache.poi.examples.ss;
    exports org.apache.poi.examples.ss.formula;
    exports org.apache.poi.examples.ss.html;
    exports org.apache.poi.examples.util;
    exports org.apache.poi.examples.xslf;
    exports org.apache.poi.examples.xssf.eventusermodel;
    exports org.apache.poi.examples.xssf.streaming;
    exports org.apache.poi.examples.xssf.usermodel;
    exports org.apache.poi.examples.xwpf.usermodel;
}