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
#   create md5 checksums and gpg signatures 

echo "If you use gpg2 you need to set GPG_BIN accordingly"

GPG_BIN=gpg

stty -echo
echo "enter your GPG passphrase"
read passphrase
stty echo

# Do we have md5sum and sha1sum?
# (We can use openssl if not, but the files it produces aren't as nice)
which md5sum > /dev/null
LACKING_MD5SUM=$?
which sha1sum > /dev/null
LACKING_SHA1SUM=$?

for i in *; do
    echo ""
    echo Signing $i
    echo $passphrase | $GPG_BIN --passphrase-fd 0 --output $i.asc --detach-sig --armor $i
    $GPG_BIN --verify $i.asc $i
    echo Hashing $i

    if [ "$LACKING_MD5SUM" = "1" ]; then
       openssl md5 < $i > $i.md5
    else
       md5sum $i > $i.md5
    fi

    if [ "$LACKING_SHA1SUM" = "1" ]; then
       openssl sha1 < $i > $i.sha1
    else
       sha1sum $i > $i.sha1
    fi
done
