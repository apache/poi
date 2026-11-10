<!--
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
-->
<!-- ===================================================================
     Apache Changes POI Module
PURPOSE:
  This DTD was developed to create a simple yet powerful document
  type for software development changes for use with the Apache projects.
  It is an XML-compliant DTD and it's maintained by the Apache XML
  project.
TYPICAL INVOCATION:
  <!ENTITY % changes PUBLIC
      "-//APACHE//ENTITIES Changes POI//EN"
      "changes-poi.mod">
  %changes;
NOTES:
  It is important, expecially in open developped software projects, to keep
  track of software changes both to give users indications of bugs that mig
  have been resolved, as well, and not less important, to provide credits
  for the support given to the project. It is considered vital to provide
  adequate payback using recognition and credits to let users and developer
  feel part of the community, thus increasing development power.
==================================================================== -->

<!-- =============================================================== -->
<!-- Document Type Definition -->
<!-- =============================================================== -->
<!ELEMENT changes (contexts, section?, release+)>
<!ATTLIST changes %common.att;>

<!ELEMENT contexts (context+)>
<!ELEMENT context EMPTY>
<!ATTLIST context %common-idreq.att;
        %title.att;>

<!ELEMENT release (summary?,actions)>
<!ATTLIST release %common.att;
        version  CDATA  #REQUIRED
        date     CDATA  #REQUIRED>

<!ELEMENT summary (summary-item+)>
<!ELEMENT summary-item (%content.mix;|bug)*>

<!ELEMENT actions (action+)>
<!ELEMENT action (%content.mix;|bug)*>

<!ENTITY % types "add|remove|update|fix">
<!ENTITY % importances "enhancement|trivial|minor|normal|major|critical|blocker">


<!ATTLIST action
        %common.att;
        type (%types;)  #IMPLIED
        context IDREFS #REQUIRED
        importance (%importances;) "normal"
        due-to CDATA #IMPLIED
        due-to-email CDATA #IMPLIED
        fixes-bug CDATA #IMPLIED
        breaks-compatibility (true|false) #IMPLIED>

<!ELEMENT bug EMPTY>
<!ATTLIST bug %common.att;
        num      CDATA  #REQUIRED>


<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->
