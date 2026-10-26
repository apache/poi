#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
# Small helper script to produce a coverage report when executing the fuzz-model
# against the current corpus.
#

set -eu

if [ $# -gt 0 ]; then
  ONE=$1
  echo
  echo "Fuzzing only ${ONE}"
else
  ONE=
fi


# Build the fuzzer and fetch dependency-jars
echo
echo Rebuilding
../gradlew shadowJar getFuzzDeps


# extract jar-files of Apache POI
mkdir -p build/poifiles
cd build/poifiles

# then unpack the class-files
for i in `find ../runtime -name poi-*.jar`; do
  echo $i
  unzip -o -q $i
done

# Remove some packages that we do not want to include in the Report
rm -r com
rm -r org/openxmlformats
rm -r org/etsi
rm -r org/w3
rm -r org/apache/poi/examples

cd -



# Fetch JaCoCo Agent
test -f jacoco-0.8.15.zip || wget --continue https://repo1.maven.org/maven2/org/jacoco/jacoco/0.8.15/jacoco-0.8.15.zip
unzip -o jacoco-0.8.15.zip lib/jacocoagent.jar lib/jacococli.jar
mv lib/jacocoagent.jar lib/jacococli.jar build/
rmdir lib

mkdir -p build/jacoco

echo
if [[ "$(uname -s)" == CYGWIN* ]]; then
  echo "Running under Cygwin"
  JAZZER_DELIMITER=":"
  PATH_DELIMITER=";"
else
  JAZZER_DELIMITER="\\:"
  PATH_DELIMITER=":"
fi


for i in `cd src/main/java/org/apache/poi/fuzz/ && ls ${ONE}*Fuzzer.java`; do
  CLASS=`echo $i | sed -e 's/.java//g'`
  CORPUS=corpus`echo ${CLASS} | sed -e 's/Fuzzer//g' | sed -e 's/POI//g'`

  # no separate corpus for this one
  if [ "${CLASS}" == "XLSX2CSVFuzzer" ]; then
    CORPUS=corpusXSSF
  elif [ "${CLASS}" == "POIFileHandlerFuzzer" ]; then
    CORPUS=corpus
  fi

  echo
  echo Running jazzer for ${CLASS} and ${CORPUS}

  # Run Jazzer with JaCoCo-Agent to produce coverage information
  ./jazzer \
    "--cp=build/poifiles${PATH_DELIMITER}build/libs/poi-fuzz-all.jar" \
    "--instrumentation_includes=org.apache.poi.**${PATH_DELIMITER}org.apache.xmlbeans.**" \
    --target_class=org.apache.poi.fuzz.${CLASS} \
    --nohooks \
    --jvm_args="-javaagent${JAZZER_DELIMITER}build/jacocoagent.jar=destfile=build/jacoco/${CORPUS}.exec" \
    -rss_limit_mb=8192 \
    -runs=0 \
    ${CORPUS}
done

echo
echo Having coverage-files
ls -al build/jacoco/corpus*.exec

# Finally create the JaCoCo report
echo
echo Creating JaCoCo report
java -jar build/jacococli.jar report build/jacoco/corpus*.exec \
 --classfiles build/poifiles \
 --sourcefiles /opt/apache/poi/git-svn/poi/src/main/java \
 --sourcefiles /opt/apache/poi/git-svn/poi-ooxml/src/main/java \
 --sourcefiles /opt/apache/poi/git-svn/poi-ooxml-lite/src/main/java \
 --sourcefiles /opt/apache/poi/git-svn/poi-scratchpad/src/main/java \
 --sourcefiles /opt/apache/xmlbeans/trunk/src/main/java \
 --sourcefiles src/main/java \
 --html build/reports/jacoco


echo All Done, report is at build/reports/jacoco/index.html
