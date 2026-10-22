This is a small sub-project for fuzzing [Apache POI](https://poi.apache.org/) 
with the [jazzer](https://github.com/CodeIntelligenceTesting/jazzer/) fuzzing tool.

See [Fuzzing](https://en.wikipedia.org/wiki/Fuzzing) for a general description of the theory behind fuzzy testing.

Because Java uses a runtime environment which does not crash on invalid actions of an
application (unless native code is invoked), Fuzzing of Java-based applications  
focuses on the following:

* verify if only expected exceptions are thrown
* verify any JNI or native code calls
* find cases of unbounded memory allocations

Apache POI does not use JNI or native code, therefore the fuzzing target mainly
tries to trigger unexpected exceptions and unbounded memory allocations.

# oss-fuzz

These sources are also used to run large-scale fuzzing via [oss-fuzz](https://google.github.io/oss-fuzz/).

# How to fuzz

Build the fuzzing target:

    ../gradlew shadowJar

Copy the corpus of test-files from the sources

    cp -a ../test-data corpus

You can add more documents to the corpus to help Jazzer in producing "nearly"
proper documents which will improve fuzzing a lot. Slightly broken documents
seem to be a good seed for fuzzing as well.

Download Jazzer from the [releases page](https://github.com/CodeIntelligenceTesting/jazzer/releases),
choose the latest version and select the file `jazzer-<os>-<version>.tar.gz`

Unpack the archive:

    tar xzf jazzer-*.tar.gz

Invoke the fuzzing:

    ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIFuzzer -rss_limit_mb=2560 corpus

On Windows/Cygwin use the following:

    ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**;org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIFuzzer -rss_limit_mb=2560 corpus

In this mode Jazzer will stop whenever it detects an unexpected exception
or crashes.

You can use `--keep_going=10` to report a given number of exceptions before stopping.

See `./jazzer` for options which can control details of how Jazzer operates.

# Fuzzing via Jazzer Docker image

You can use a ready-made Docker image for running the fuzzing in a container.

This shields the fuzzing from performing unexpected actions on your local machine
or allows to run it in a container-based infrastructure.

    docker run -v `pwd`:/fuzzing cifuzz/jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIFuzzer -rss_limit_mb=2560 corpus

# Fuzzing single file formats

## HSLF

    mkdir corpusHSLF
    cp corpus/slideshow/*.ppt corpusHSLF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHSLFFuzzer -rss_limit_mb=2560 corpusHSLF

## HSSF

    mkdir corpusHSSF
    cp corpus/spreadsheet/*.xls corpusHSSF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHSSFFuzzer -rss_limit_mb=2560 corpusHSSF

## HWPF

    mkdir corpusHWPF
    cp corpus/document/*.doc corpus/document/*.DOC corpusHWPF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHWPFFuzzer -rss_limit_mb=2560 corpusHWPF

## OldExcel

    mkdir corpusOldExcel
    cp corpus/spreadsheet/*.xls corpus/spreadsheet/*.bin corpusOldExcel/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIOldExcelFuzzer -rss_limit_mb=2560 corpusOldExcel

## XSLF

    mkdir corpusXSLF
    cp corpus/slideshow/* corpus/integration/*.pptx corpusXSLF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIXSLFFuzzer -rss_limit_mb=2560 corpusXSLF

## XSSF

    mkdir corpusXSSF
    cp corpus/spreadsheet/* corpus/integration/*.xlsx corpusXSSF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIXSSFFuzzer -rss_limit_mb=2560 corpusXSSF

## XSSFB

    mkdir corpusXSSFB
    cp corpus/spreadsheet/*.xlsb corpusXSSFB/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIXSSFBFuzzer -rss_limit_mb=2560 corpusXSSFB

## XLSX2CSV

Can re-use XSSF-corpus as the same files are processed here

    mkdir corpusXSSF
    cp corpus/spreadsheet/* corpusXSSF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.XLSX2CSVFuzzer -rss_limit_mb=2560 corpusXSSF

## XWPF

    mkdir corpusXWPF
    cp corpus/document/* corpus/integration/*.docx corpusXWPF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIXWPFFuzzer -rss_limit_mb=2560 corpusXWPF

## Visio

    mkdir corpusVisio
    cp corpus/diagram/* corpusVisio/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIVisioFuzzer -rss_limit_mb=2560 corpusVisio

## HDGF

    mkdir corpusHDGF
    cp corpus/diagram/*.vsd corpusHDGF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHDGFFuzzer -rss_limit_mb=2560 corpusHDGF

## HPSF

    mkdir corpusHPSF
    cp corpus/hpsf/* corpusHPSF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHPSFFuzzer -rss_limit_mb=2560 corpusHPSF

## HMEF

    mkdir corpusHMEF
    cp -a corpus/hmef/* corpusHMEF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHMEFFuzzer -rss_limit_mb=2560 corpusHMEF

## HPBF

    mkdir corpusHPBF
    cp -a corpus/publisher/* corpusHPBF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHPBFFuzzer -rss_limit_mb=2560 corpusHPBF

## HSMF

    mkdir corpusHSMF
    cp -a corpus/hsmf/* corpusHSMF/
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.POIHSMFFuzzer -rss_limit_mb=2048 corpusHSMF

## EncryptDecrypt

    mkdir corpusEncryptDecrypt
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.EncryptDecryptFuzzer -rss_limit_mb=2560 corpusEncryptDecrypt

## FormulaParser

    mkdir corpusFormulaParser
    ../gradlew shadowJar && ./jazzer --cp=build/libs/poi-fuzz-6.0.0-all.jar "--instrumentation_includes=org.apache.poi.**:org.apache.xmlbeans.**" --target_class=org.apache.poi.fuzz.FormulaParserFuzzer -rss_limit_mb=2560 corpusFormulaParser

# Detected issues

* Many of the exceptions are not declared as being thrown in the JavaDoc of Apache POI,
  thus making it hard to write proper defensive try-catches when using Apache POI, for now
  you will need to catch `RuntimeException` and any declared ones.
* The RuntimeExceptions do not have a common base class which would allow to
  only catch this one type of exception to capture all expected ones
* Some places throw an IllegalArgumentException or IllegalStateException where a dedicated class 
  deriving from RuntimeException would be preferable. This currently leads to a very 
  general catching of these exceptions in the fuzz target.
* Some memory allocations are not bounded, i.e. causing out-of-memory on certain
  broken documents. Some more memory guards need to be put in place
