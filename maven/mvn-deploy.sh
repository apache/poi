#! /bin/sh
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
# Shell script to deploy POI artifacts in a maven repository.
#
#  Note, You should configure your settings.xml and add a server with id=apache-releases:
#
#    <server>
#      <id>apache-releases</id>
#      <username>apacheId</username>
#      <privateKey>/path/to/private/key</privateKey>
#    </server>
#
#   <profiles>
#      <profile>
#      <id>apache-releases</id>
#      <properties>
#        <gpg.passphrase><!-- Your GPG passphrase --></gpg.passphrase>
#      </properties>
#    </profile>
#  </profiles>
#
#  Usage:
#   1. ant dist
#   2. cd build/dist
#   3. ./mvn-deploy.sh 
# @author Yegor Kozlov

M2_REPOSITORY=scp://people.apache.org/www/people.apache.org/repo/m2-ibiblio-rsync-repository
M2_SCP=people.apache.org:/www/people.apache.org/repo/m2-ibiblio-rsync-repository

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-@VERSION@-@DSTAMP@.jar -DpomFile=poi-@VERSION@.pom
scp poi-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi/@VERSION@/
scp poi-@VERSION@-sources.* $M2_SCP/org/apache/poi/poi/@VERSION@/

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-scratchpad-@VERSION@-@DSTAMP@.jar -DpomFile=poi-scratchpad-@VERSION@.pom
scp poi-scratchpad-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi-scratchpad/@VERSION@/
scp poi-scratchpad-@VERSION@-sources.* $M2_SCP/org/apache/poi/poi-scratchpad/@VERSION@/

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-contrib-@VERSION@-@DSTAMP@.jar -DpomFile=poi-contrib-@VERSION@.pom
scp poi-contrib-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi-contrib/@VERSION@/
scp poi-contrib-@VERSION@-sources.* $M2_SCP/org/apache/poi/poi-contrib/@VERSION@/

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-ooxml-@VERSION@-@DSTAMP@.jar -DpomFile=poi-ooxml-@VERSION@.pom
scp poi-ooxml-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi-ooxml/@VERSION@/
scp poi-ooxml-@VERSION@-sources.* $M2_SCP/org/apache/poi/poi-ooxml/@VERSION@/

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-ooxml-schemas-@VERSION@-@DSTAMP@.jar -DpomFile=poi-ooxml-schemas-@VERSION@.pom
scp poi-ooxml-schemas-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi-ooxml-schemas/@VERSION@/

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-examples-@VERSION@-@DSTAMP@.jar -DpomFile=poi-examples-@VERSION@.pom
scp poi-examples-@VERSION@.pom.asc $M2_SCP/org/apache/poi/poi-examples/@VERSION@/

