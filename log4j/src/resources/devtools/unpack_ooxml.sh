#!/bin/bash -e
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

# basename it

FILENAME="$1"
BASENAME="${FILENAME##*/}"
OUTDIR="${BASENAME%.*}"

if [ ! -f "$FILENAME" ]; then
    echo "File $FILENAME does not exist"
    exit 1
fi

# if dir exists, fail
if [ -d "$OUTDIR" ]; then
    echo "Directory '$OUTDIR' already exists!"
    exit 1
fi

mkdir "$OUTDIR"
unzip -d "$OUTDIR" "$FILENAME"

pushd "$OUTDIR"

find . -type f \( -iname "*.xml" -or -iname "*.rels" \) -exec python -c "import os, sys, xml.dom.minidom
with open('{}', 'r') as fp:
    s = fp.read()
with open('{}.bak', 'w') as fp:
    fp.write(xml.dom.minidom.parseString(s).toprettyxml().encode('utf-8'))
os.rename('{}.bak', '{}')" \;
