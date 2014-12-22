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
#      <password>mySecurePassw0rd</password>
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

M2_REPOSITORY=https://repository.apache.org/service/local/staging/deploy/maven2

VERSION=@VERSION@
DSTAMP=@DSTAMP@

for artifactId in poi poi-scratchpad poi-ooxml poi-examples poi-ooxml-schemas poi-excelant
do
  SENDS="-Dfile=$artifactId-$VERSION-$DSTAMP.jar"
  SENDS="$SENDS -DpomFile=$artifactId-$VERSION.pom"
  if [ -r $artifactId-$VERSION-sources-$DSTAMP.jar ]; then
     SENDS="$SENDS -Dsources=$artifactId-$VERSION-sources-$DSTAMP.jar"
  fi
  if [ -r $artifactId-$VERSION-javadocs-$DSTAMP.jar ]; then
     SENDS="$SENDS -Djavadoc=$artifactId-$VERSION-javadocs-$DSTAMP.jar"
  fi

  mvn gpg:sign-and-deploy-file \
    -DrepositoryId=apache-releases -P apache-releases \
    -Durl=$M2_REPOSITORY \
    $SENDS
done
