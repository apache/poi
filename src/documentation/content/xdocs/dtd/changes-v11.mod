<!--
  Copyright 1999-2004 The Apache Software Foundation

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

     Apache Changes Module (Version 1.1)

PURPOSE:
  This DTD was developed to create a simple yet powerful document
  type for software development changes for use with the Apache projects.
  It is an XML-compliant DTD and it's maintained by the Apache XML
  project.

TYPICAL INVOCATION:

  <!ENTITY % changes PUBLIC
      "-//APACHE//ENTITIES Changes Vxy//EN"
      "changes-vxy.mod">
  %changes;

  where

    x := major version
    y := minor version

NOTES:
  It is important, expecially in open developped software projects, to keep
  track of software changes both to give users indications of bugs that might
  have been resolved, as well, and not less important, to provide credits
  for the support given to the project. It is considered vital to provide
  adequate payback using recognition and credits to let users and developers
  feel part of the community, thus increasing development power.

FIXME:

CHANGE HISTORY:
[Version 1.0]
  19991129 Initial version. (SM)
  20000316 Added bugfixing attribute. (SM)
[Version 1.1]
  20011212 Used public identifiers for external entities (SM)

==================================================================== -->

<!-- =============================================================== -->
<!-- Document Type Definition -->
<!-- =============================================================== -->

<!ELEMENT changes (title?, devs?, release+)>
<!ATTLIST changes %common.att;>

    <!ELEMENT release (action+)>
    <!ATTLIST release %common.att;
                      version  CDATA  #REQUIRED
                      date     CDATA  #REQUIRED>

<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->
