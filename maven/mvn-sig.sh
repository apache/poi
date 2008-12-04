#! /bin/sh
# copy signatures

REPO=/www/people.apache.org/repo/m2-ibiblio-rsync-repository/org/apache/poi
version=@VERSION@

artifactId=poi
cp $artifactId-@VERSION@-@DSTAMP@.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar.asc
cp $artifactId-@VERSION@-@DSTAMP@.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom.asc
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar 
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom 

artifactId=poi-contrib
cp $artifactId-@VERSION@-@DSTAMP@.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar.asc
cp $artifactId-@VERSION@-@DSTAMP@.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom.asc
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar 
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom 

artifactId=poi-scratchpad
cp $artifactId-@VERSION@-@DSTAMP@.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar.asc
cp $artifactId-@VERSION@-@DSTAMP@.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom.asc
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar 
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom 

artifactId=poi-ooxml
cp $artifactId-@VERSION@-@DSTAMP@.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar.asc
cp $artifactId-@VERSION@-@DSTAMP@.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom.asc
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.jar.asc $REPO/$artifactId/$version/$artifactId-$version.jar 
gpg --verify $REPO/$artifactId/$version/$artifactId-$version.pom.asc $REPO/$artifactId/$version/$artifactId-$version.pom 

