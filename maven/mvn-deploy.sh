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
#  Usage:
#   1. ant dist
#   2. cd dist
#   3. ./mvn-deploy.sh 
# @author Yegor Kozlov

M2_REPOSITORY=@REPOSITORY@

mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-@VERSION@-@DSTAMP@.jar -DpomFile=poi-@VERSION@.pom
mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-scratchpad-@VERSION@-@DSTAMP@.jar -DpomFile=poi-scratchpad-@VERSION@.pom
mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-contrib-@VERSION@-@DSTAMP@.jar -DpomFile=poi-contrib-@VERSION@.pom
mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases \
  -Durl=$M2_REPOSITORY \
  -Dfile=poi-ooxml-@VERSION@-@DSTAMP@.jar -DpomFile=poi-ooxml-@VERSION@.pom

