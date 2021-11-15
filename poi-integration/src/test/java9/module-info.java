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

module org.apache.poi.stress {
    requires org.apache.logging.log4j;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires net.bytebuddy;
    requires java.desktop;

    requires org.apache.commons.collections4;
    requires org.apache.poi.examples;
    requires org.apache.poi.scratchpad;

    requires org.apache.santuario.xmlsec;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;
    requires org.codehaus.stax2;



    exports org.apache.poi.stress;

    opens org.apache.poi.stress to org.junit.platform.commons;
}