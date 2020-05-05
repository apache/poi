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


open module org.apache.poi.ooxml.security {
    requires transitive xmlbeans;
    requires java.xml;
    exports com.microsoft.schemas.office.x2006.digsig;
    exports com.microsoft.schemas.office.x2006.encryption;
    exports com.microsoft.schemas.office.x2006.keyEncryptor.certificate;
    exports com.microsoft.schemas.office.x2006.keyEncryptor.password;
    exports org.etsi.uri.x01903.v13;
    exports org.etsi.uri.x01903.v14;
    exports org.openxmlformats.schemas.xpackage.x2006.digitalSignature;
    exports org.openxmlformats.schemas.xpackage.x2006.relationships;
    exports org.w3.x2000.x09.xmldsig;
    // opens schemaorg_apache_xmlbeans.system.OoxmlSecurity to xmlbeans;
}