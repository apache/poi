<!--
  Copyright 2002-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- ===================================================================

     Apache Common Character Entity Sets (Version 1.0)

PURPOSE:
  Common elements across all DTDs.

TYPICAL INVOCATION:

  <!ENTITY % common-charents PUBLIC
      "-//APACHE//ENTITIES Common Character Entity Sets Vx.y//EN"
      "common-charents-vxy.mod">
  %common-charents;

  where

    x := major version
    y := minor version

FIXME:

CHANGE HISTORY:
[Version 1.0]
  20020613 Initial version. (DC)

==================================================================== -->

<!-- =============================================================== -->
<!-- Common ISO character entity sets -->
<!-- =============================================================== -->

<!ENTITY % ISOlat1 PUBLIC
    "ISO 8879:1986//ENTITIES Added Latin 1//EN//XML"
    "../entity/ISOlat1.pen">
%ISOlat1;

<!ENTITY % ISOpub PUBLIC
    "ISO 8879:1986//ENTITIES Publishing//EN//XML"
    "../entity/ISOpub.pen">
%ISOpub;

<!ENTITY % ISOtech PUBLIC
    "ISO 8879:1986//ENTITIES General Technical//EN//XML"
    "../entity/ISOtech.pen">
%ISOtech;

<!ENTITY % ISOnum PUBLIC
    "ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML"
    "../entity/ISOnum.pen">
%ISOnum;

<!ENTITY % ISOdia PUBLIC
    "ISO 8879:1986//ENTITIES Diacritical Marks//EN//XML"
    "../entity/ISOdia.pen">
%ISOdia;

<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->
