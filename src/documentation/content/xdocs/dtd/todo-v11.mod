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

     Apache Todos module (Version 1.0)

PURPOSE:
  This DTD was developed to create a simple yet powerful document
  type for software development todo lists for use with the Apache projects.
  It is an XML-compliant DTD and it's maintained by the Apache XML
  project.

TYPICAL INVOCATION:

  <!ENTITY % todo PUBLIC
      "-//APACHE//ENTITIES Todo Vxy//EN"
      "todo-vxy.mod">
  %todo;

  where

    x := major version
    y := minor version

NOTES:
  It is important, expecially in open developped software projects, to keep
  track of software changes that need to be done, planned features, development
  assignment, etc. in order to allow better work parallelization and create
  an entry point for people that want to help. This DTD wants to provide
  a solid foundation to provide such information and to allow it to be
  published as well as distributed in a common format.

FIXME:
  - do we need anymore working contexts? (SM)

CHANGE HISTORY:
[Version 1.0]
  19991129 Initial version. (SM)
  19991225 Added actions element for better structure (SM)
[Version 1.1]
  20011212 Used public identifiers for external entities (SM)

==================================================================== -->
<!-- =============================================================== -->
<!-- Common entities -->
<!-- =============================================================== -->
<!ENTITY % priorities "showstopper|high|medium|low|wish|dream">
<!-- =============================================================== -->
<!-- Document Type Definition -->
<!-- =============================================================== -->
<!ELEMENT todo (title?, devs?, actions+)>
<!ATTLIST todo
  %common.att; 
>

<!ELEMENT actions (action+)>
<!ATTLIST actions
  %common.att; 
  priority (%priorities;) #IMPLIED
>
<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->
