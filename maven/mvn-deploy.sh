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

VERSION=@VERSION@
DSTAMP=@DSTAMP@

for artifactId in poi poi-scratchpad poi-contrib poi-ooxml poi-examples poi-ooxml-schemas
do
  mvn gpg:sign-and-deploy-file -DrepositoryId=apache-releases -P apache-releases \
    -Durl=$M2_REPOSITORY \
    -Dfile=$artifactId-$VERSION-$DSTAMP.jar -DpomFile=$artifactId-$VERSION.pom
  #The maven sign-and-deploy-file command does NOT sign POM files, so we have to upload the POM's .asc manually
  scp $artifactId-$VERSION.pom.asc $M2_SCP/org/apache/poi/$artifactId/$VERSION/

  if [ -r $artifactId-$VERSION-sources-$DSTAMP.jar ]; then
    mvn deploy:deploy-file -DrepositoryId=apache-releases -P apache-releases \
      -Durl=$M2_REPOSITORY -DgeneratePom=false -Dpackaging=java-source \
      -Dfile=$artifactId-$VERSION-sources-$DSTAMP.jar -DpomFile=$artifactId-$VERSION.pom
    scp $artifactId-$VERSION-sources-$DSTAMP.jar.asc $M2_SCP/org/apache/poi/$artifactId/$VERSION/$artifactId-$VERSION-sources.jar.asc
  fi
done
