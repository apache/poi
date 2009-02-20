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
# Shell script to copy digital signatures to the Maven repository.
# Unfortunately "mvn deploy:deploy-file" does not upload *.asc files and we have to upload them after ./mvn-deploy.sh is done.
#
#  Usage:
#   1. Upload release artifacts using ./mvn-deploy.sh 
#   2. login to people.apache.org and cd to the directory with the release files
#   3. ./mvn-sig.sh
# @author Yegor Kozlov

REPO=/www/people.apache.org/repo/m2-ibiblio-rsync-repository/org/apache/poi
VERSION=@VERSION@
DSTAMP=@DSTAMP@

artifactId=poi
cp $artifactId-$VERSION-$DSTAMP.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc
cp $artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar 
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom 

artifactId=poi-contrib
cp $artifactId-$VERSION-$DSTAMP.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc
cp $artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar 
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom 

artifactId=poi-scratchpad
cp $artifactId-$VERSION-$DSTAMP.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc
cp $artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar 
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom 

artifactId=poi-ooxml
cp $artifactId-$VERSION-$DSTAMP.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc
cp $artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.jar 
gpg --verify $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom.asc $REPO/$artifactId/$VERSION/$artifactId-$VERSION.pom 


