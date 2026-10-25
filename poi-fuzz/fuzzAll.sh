#!/bin/bash

set -eu

../gradlew shadowJar

for i in src/main/java/org/apache/poi/fuzz/*Fuzzer.java;do
  CLASS=`basename $i | sed -e 's/.java//g'`
  CORPUS=corpus`echo ${CLASS} | sed -e 's/Fuzzer//g' | sed -e 's/POI//g'`

  # no separate corpus for this one
  if [ "${CLASS}" == "XLSX2CSVFuzzer" ]; then
    CORPUS=corpusXSSF
  elif [ "${CLASS}" == "POIFileHandlerFuzzer" ]; then
    CORPUS=corpus
  fi

  echo Running ${CLASS} with corpus ${CORPUS}

  mkdir -p ${CORPUS}

  case "${CLASS}" in
    POIFuzzer)
      cp -a ../test-data corpus
      ;;
    POIHDGFFuzzer)
      cp corpus/diagram/*.vsd corpusHDGF/
      ;;
    POIHMEFFuzzer)
      cp -a corpus/hmef/* corpusHMEF/
      ;;
    POIHPBFFuzzer)
      cp -a corpus/publisher/* corpusHPBF/
      ;;
    POIHPSFFuzzer)
      cp corpus/hpsf/* corpusHPSF/
      ;;
    POIHSLFFuzzer)
      cp corpus/slideshow/*.ppt corpusHSLF/
      ;;
    POIHSMFFuzzer)
      cp -a corpus/hsmf/* corpusHSMF/
      ;;
    POIHSSFFuzzer)
      cp corpus/spreadsheet/*.xls corpusHSSF/
      ;;
    POIHWPFFuzzer)
      cp corpus/document/*.doc corpus/document/*.DOC corpusHWPF/
      ;;
    POIOldExcelFuzzer)
      cp corpus/spreadsheet/*.xls corpus/spreadsheet/*.bin corpusOldExcel/
      ;;
    POIRleFuzzer)
      ;;
    POIVisioFuzzer)
      cp corpus/diagram/* corpusVisio/
      ;;
    POIXSLFFuzzer)
      cp corpus/slideshow/* corpus/integration/*.pptx corpusXSLF/
      ;;
    POIXSSFBFuzzer)
      cp corpus/spreadsheet/*.xlsb corpusXSSFB/
      ;;
    POIXSSFFuzzer)
      cp corpus/spreadsheet/* corpus/integration/*.xlsx corpusXSSF/
      ;;
    POIXWPFFuzzer)
      cp corpus/document/* corpus/integration/*.docx corpusXWPF/
      ;;
    XLSX2CSVFuzzer)
      cp corpus/spreadsheet/* corpusXSSF/
      ;;
  esac

  nice -n 19 ./jazzer \
    --cp=build/libs/poi-fuzz-all.jar \
    "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" \
    --target_class=org.apache.poi.fuzz.${CLASS} \
    -rss_limit_mb=2560 \
    -timeout=600 \
    -max_total_time=600 \
    ${CORPUS}
done