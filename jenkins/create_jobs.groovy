// This script is used as input to the Jenkins Job DSL plugin to create all the build-jobs that
// Apache POI uses on the public Jenkins instance at https://ci-builds.apache.org/job/POI/
//
// See https://github.com/jenkinsci/job-dsl-plugin/wiki for information about the DSL, you can
// use https://job-dsl.herokuapp.com/ to validate the code before checkin
//

def triggerSundays = '''
# only run this once per week on Sundays
H H * * 0
'''

def xercesUrl = 'https://repo1.maven.org/maven2/xerces/xercesImpl/2.6.1/xercesImpl-2.6.1.jar'
def xercesLib = './xercesImpl-2.6.1.jar'

def poijobs = [
        [ name: 'POI-DSL-1.8', trigger: 'H */12 * * *', jenkinsLite: true
        ],
        [ name: 'POI-DSL-OpenJDK', jdk: 'OpenJDK 1.8', trigger: 'H */12 * * *',
          // only a limited set of nodes still have OpenJDK 8 (on Ubuntu) installed
          slaves: 'ubuntu',
          skipcigame: true,
          jenkinsLite: true
        ],
        [ name: 'POI-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true
        ],
        [ name: 'POI-DSL-1.15', jdk: '1.15', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 15 is not an LTS and JDK 16 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-1.16', jdk: '1.16', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 16 is not an LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-1.17', jdk: '1.17', trigger: 'H */12 * * *', skipcigame: true
        ],
        // Jenkins on ci-builds.apache.org does not support spotbugs with a new enough version of asm for Java18+
        [ name: 'POI-DSL-1.18', jdk: '1.18', trigger: triggerSundays, skipcigame: true, skipSpotbugs: true,
          // let's save some CPU cycles here, 18 is not a LTS and JDK 20 is out
          disabled: true
        ],
        // Jenkins on ci-builds.apache.org does not support spotbugs with a new enough version of asm for Java18+
        [ name: 'POI-DSL-1.19', jdk: '1.19', trigger: triggerSundays, skipcigame: true, skipSpotbugs: true
        ],
        // Jenkins on ci-builds.apache.org does not support spotbugs with a new enough version of asm for Java18+
        [ name: 'POI-DSL-1.20', jdk: '1.20', trigger: triggerSundays, skipcigame: true, skipSpotbugs: true
        ],
        // Jenkins on ci-builds.apache.org does not support spotbugs with a new enough version of asm for Java18+
        [ name: 'POI-DSL-1.21', jdk: '1.21', trigger: 'H */12 * * *', skipcigame: true, skipSpotbugs: true
        ],
        // Jenkins on ci-builds.apache.org does not support spotbugs with a new enough version of asm for Java18+
        [ name: 'POI-DSL-1.22', jdk: '1.22', trigger: triggerSundays, skipcigame: true, skipSpotbugs: true
        ],
        // Use Ant-build for now as selecting IBM JDK via toolchain does not work (yet)
        [ name: 'POI-DSL-IBM-JDK', jdk: 'IBMJDK', trigger: triggerSundays, skipcigame: true, useAnt: true
        ],
        // Use Ant-build for now as passing the "additionaljar" does not work in Gradle build (yet)
        [ name: 'POI-DSL-old-Xerces', trigger: triggerSundays, skipcigame: true, useAnt: true,
          shell: "test -s ${xercesLib} || wget -O ${xercesLib} ${xercesUrl}\n",
          // the property triggers using Xerces as XML Parser and previously showed some exception that can occur
          properties: ["-Dadditionaljar=${xercesLib}"]
        ],
//        [ name: 'POI-DSL-Maven', trigger: 'H */4 * * *', maven: true,
//		  // not needed any more now that we use Gradle for SonarQube
//		  disabled: true
//        ],
        [ name: 'POI-DSL-regenerate-javadoc', trigger: triggerSundays, javadoc: true
        ],
        // it was impossible to make this run stable in Gradle, thus disabling this for now
        [ name: 'POI-DSL-API-Check', trigger: '@daily', apicheck: true, disabled: true, useAnt: true
        ],
//        [ name: 'POI-DSL-Gradle', trigger: triggerSundays, email: 'centic@apache.org'
//        ],
        [ name: 'POI-DSL-no-scratchpad', trigger: triggerSundays, noScratchpad: true
        ],
        [ name: 'POI-DSL-saxon-test', trigger: triggerSundays, saxonTest: true
        ],
//        [ name: 'POI-DSL-SonarQube', jdk: '1.11', trigger: 'H 7 * * *', maven: true, sonar: true, skipcigame: true,
//          email: 'kiwiwings@apache.org',
//		  // replaced by Gradle-based build now
//		  disabled: true
//        ],
        [ name: 'POI-DSL-SonarQube-Gradle', jdk: '1.11', trigger: 'H 7 * * *', sonar: true, skipcigame: true
        ],
        [ name: 'POI-DSL-Windows-1.8', trigger: 'H */12 * * *', windows: true, slaves: 'Windows', jenkinsLite: true
        ],
        [ name: 'POI-DSL-Windows-1.11', jdk: '1.11', trigger: triggerSundays, windows: true, slaves: 'Windows',
          jenkinsLite: true
        ],
        [ name: 'POI-DSL-Windows-1.15', jdk: '1.15', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
          // let's save some CPU cycles here, 14 is not an LTS and JDK 15 is GA as of 15 September 2020
          disabled: true
        ],
        [ name: 'POI-DSL-Windows-1.16', jdk: '1.16', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
          // let's save some CPU cycles here, 16 is not an LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-Windows-1.17', jdk: '1.17', trigger: 'H */12 * * *', windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Windows-1.18', jdk: '1.18', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
          skipSpotbugs: true,
          // let's save some CPU cycles here, 18 is not an LTS and JDK 20 is out
          disabled: true
        ],
        [ name: 'POI-DSL-Windows-1.20', jdk: '1.20', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Windows-1.21', jdk: '1.21', trigger: 'H */12 * * *', windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Windows-1.22', jdk: '1.22', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Github-PullRequests', trigger: '', skipcigame: true, disabled: true
        ],
]

def xmlbeansjobs = [
        [ name: 'POI-XMLBeans-DSL-1.8', jdk: '1.8', trigger: 'H */12 * * *', skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.16', jdk: '1.16', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 16 is not an LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-XMLBeans-DSL-1.17', jdk: '1.17', trigger: 'H */12 * * *', skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.18', jdk: '1.18', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 18 is not an LTS and JDK 20 is out
          disabled: true
        ],
        [ name: 'POI-XMLBeans-DSL-1.19', jdk: '1.19', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 19 is not an LTS
          disabled: true
        ],
        [ name: 'POI-XMLBeans-DSL-1.20', jdk: '1.20', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.21', jdk: '1.21', trigger: 'H */12 * * *', skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.22', jdk: '1.22', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.20', jdk: '1.20', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-Sonar', jdk: '1.11', trigger: triggerSundays, skipcigame: true,
          sonar: true
        ]
]

def svnBase = 'https://svn.apache.org/repos/asf/poi/trunk'
def xmlbeansSvnBase = 'https://svn.apache.org/repos/asf/xmlbeans/trunk'

def defaultJdk = '1.8'
def defaultTrigger = 'H/15 * * * *'     // check SCM every 60/15 = 4 minutes
def defaultEmail = 'dev@poi.apache.org'
def defaultAnt = 'ant_1.10_latest'
def defaultAntWindows = 'ant_1.10_latest_windows'
def defaultMaven = 'maven_3_latest'
// H29 seems to have very little memory
def defaultSlaves = '(ubuntu)&&!beam&&!cloud-slave&&!H29'

def jdkMapping = [
        '1.8': [ jenkinsJdk: 'jdk_1.8_latest', jdkVersion: 8, jdkVendor: '' ],
        '1.11': [ jenkinsJdk: 'jdk_11_latest', jdkVersion: 11, jdkVendor: '' ],
        '1.15': [ jenkinsJdk: 'jdk_15_latest', jdkVersion: 15, jdkVendor: '' ],
        '1.16': [ jenkinsJdk: 'jdk_16_latest', jdkVersion: 16, jdkVendor: '' ],
        '1.17': [ jenkinsJdk: 'jdk_17_latest', jdkVersion: 17, jdkVendor: '' ],
        '1.18': [ jenkinsJdk: 'jdk_18_latest', jdkVersion: 18, jdkVendor: '' ],
        '1.19': [ jenkinsJdk: 'jdk_19_latest', jdkVersion: 19, jdkVendor: '' ],
        '1.20': [ jenkinsJdk: 'jdk_20_latest', jdkVersion: 20, jdkVendor: '' ],
        '1.21': [ jenkinsJdk: 'jdk_21_latest', jdkVersion: 21, jdkVendor: '' ],
        '1.22': [ jenkinsJdk: 'jdk_22_latest', jdkVersion: 22, jdkVendor: '' ],
        'OpenJDK 1.8': [ jenkinsJdk: 'adoptopenjdk_hotspot_8u282', jdkVersion: 8, jdkVendor: 'adoptopenjdk' ],
        'IBMJDK': [ jenkinsJdk: 'ibmjdk_1.8.0_261', jdkVersion: 8, jdkVendor: 'ibm' ]
]

static def shellEx(def context, String cmd, def poijob) {
    if (poijob.windows) {
        context.batchFile(cmd)
    } else {
        context.shell(cmd)
    }
}

def defaultDesc = '''
<img src="https://poi.apache.org/images/project-header.png" />
<p>
Apache POI - the Java API for Microsoft Documents
</p>
<p>
<b>This is an automatically generated Job Config, do not edit it here!
Instead change the Jenkins Job DSL at <a href="https://svn.apache.org/repos/asf/poi/trunk/jenkins">https://svn.apache.org/repos/asf/poi/trunk/jenkins</a>,
see <a href="https://github.com/jenkinsci/job-dsl-plugin/wiki">https://github.com/jenkinsci/job-dsl-plugin/wiki</a>
for more details about the DSL.</b>
</p>'''

def apicheckDesc = '''
<p>
<b><a href="https://sonarcloud.io/dashboard?id=poi-parent" target="_blank">Sonar reports</a></b> -
<p>
<b><a href="lastSuccessfulBuild/artifact/build/poi/build/reports/japi.html">API Check POI</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/poi-ooxml/build/reports/japi.html">API Check POI-OOXML</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/poi-excelant/build/reports/japi.html">API Check POI-Excelant</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/poi-scratchpad/build/reports/japi.html">API Check POI-Scratchpad</a></b>

</p>
'''

def sonarDesc = '''
<p>
<b><a href="lastSuccessfulBuild/spotbugs/" target="_blank">Spotbugs report of latest build</a></b> -
<b><a href="https://sonarcloud.io/dashboard?id=poi-parent" target="_blank">Sonar reports</a></b> -
<b><a href="lastSuccessfulBuild/jacoco/" target="_blank">Coverage of latest build</a></b>
</p>
'''

def shellCmdsUnix =
        '''# remove some outdated directories that should not be there any more
rm -rf examples excelant integrationtest main ooxml ooxml-schema scratchpad build.javacheck.xml

# show which files are currently modified in the working copy
svn status || true
# make sure no changed module-class-files or ooxml-lite-report-files are lingering on
svn revert poi*/src/*/java9/module-info.* || true
svn revert src/resources/ooxml-lite-report.* || true

# print out information about which exact version of java we are using
echo Java-Home: $JAVA_HOME
ls -al $JAVA_HOME/
ls -al $JAVA_HOME/bin
$JAVA_HOME/bin/java -version

echo which java
which java
java -version

echo which javac
which javac
javac -version

echo Ant-Home: $ANT_HOME
ls -al $ANT_HOME
echo which ant
which ant || true
ant -version

echo '<project default="test"><target name="test"><echo>Java ${ant.java.version}/${java.version}</echo><exec executable="javac"><arg value="-version"/></exec></target></project>' > build.javacheck.xml
ant -f build.javacheck.xml -v

POIJOBSHELL

# ignore any error message
exit 0'''

def shellCmdsWin =
        '''@echo off
:: show which files are currently modified in the working copy
svn status
:: make sure no changed module-class-files are lingering on
svn revert poi*\\src\\*\\java9\\module-info.*
:: also revert some files directly as the wildcard-based revert seems to fail sometimes
svn revert poi\\src\\main\\java9\\module-info.class poi\\src\\test\\java9\\module-info.class poi-examples\\src\\main\\java9\\module-info.class poi-excelant\\src\\main\\java9\\module-info.class poi-excelant\\src\\test\\java9\\module-info.class poi-integration\\src\\test\\java9\\module-info.class poi-ooxml\\src\\main\\java9\\module-info.class poi-ooxml\\src\\test\\java9\\module-info.class poi-ooxml-full\\src\\main\\java9\\module-info.class poi-ooxml-lite\\src\\main\\java9\\module-info.class poi-ooxml-lite\\src\\main\\java9\\module-info.java poi-ooxml-lite-agent\\src\\main\\java9\\module-info.class poi-scratchpad\\src\\main\\java9\\module-info.class poi-scratchpad\\src\\test\\java9\\module-info.class src\\resources\\ooxml-lite-report.clazz src\\resources\\ooxml-lite-report.xsb

:: print out information about which exact version of java we are using
echo Java-Home: %JAVA_HOME%
dir "%JAVA_HOME:\\\\=\\%"
"%JAVA_HOME%/bin/java" -version

POIJOBSHELL

:: ignore any error message
exit /b 0'''

poijobs.each { poijob ->
    def jdkKey = poijob.jdk ?: defaultJdk
    def trigger = poijob.trigger ?: defaultTrigger
    def email = poijob.email ?: defaultEmail
    def slaves = poijob.slaves ?: defaultSlaves + (poijob.slaveAdd ?: '')
    def antRT = poijob.windows ? defaultAntWindows : defaultAnt

    job('POI/' + poijob.name) {
        if (poijob.disabled) {
            disabled()
        }

        description( defaultDesc + (poijob.apicheck ? apicheckDesc : sonarDesc) )
        logRotator {
            numToKeep(5)
            artifactNumToKeep(1)
        }
        label(slaves)
        environmentVariables {
            env('LANG', 'en_US.UTF-8')
            env('CI_BUILD', 'TRUE')
            env('FORREST_HOME', poijob.windows ? 'f:\\jenkins\\tools\\forrest\\latest' : '/home/jenkins/tools/forrest/latest')
        }

        wrappers {
            timeout {
                absolute(300)
                abortBuild()
                writeDescription('Build was aborted due to timeout')
            }
            preBuildCleanup {
                /* remove xmlbeans while 4.0 is not stable */
                includePattern('**/lib/ooxml/xmlbeans*.jar')
                includePattern('**/lib/ooxml/ooxml*.jar')
                /* remove ooxml-schemas while the builds migrate to 5th edition */
                includePattern('build/xmlbean-*/**')
                /* remove remaining src debris */
                includePattern('src/*/build/**')
            }
            if(poijob.sonar) {
                credentialsBinding {
                    string('POI_SONAR_TOKEN', 'sonarcloud-poi')
                }
                configure { project ->
                    project / buildWrappers << 'hudson.plugins.sonar.SonarBuildWrapper' {}
                }
            }
        }
        jdk(jdkMapping.get(jdkKey).jenkinsJdk)
        scm {
            svn(svnBase) { svnNode ->
                svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                        url << 'https://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
            }
        }
        checkoutRetryCount(3)

        triggers {
            scm(trigger)
        }

        def shellcmds = (poijob.windows ? shellCmdsWin : shellCmdsUnix).replace('POIJOBSHELL', poijob.shell ?: '')

        // Create steps and publishers depending on the type of Job that is selected
        if (poijob.javadoc) {
            steps {
                shellEx(delegate, shellcmds, poijob)
                ant {
                    targets(['clean', 'javadocs'] + (poijob.properties ?: []))
                    prop('coverage.enabled', true)
                    // Properties did not work, so I had to use targets instead
                    //properties(poijob.properties ?: '')
                    antInstallation(antRT)
                }
                shellEx(delegate, 'zip -r build/javadocs.zip build/site/apidocs', poijob)
            }
            publishers {
                if (!poijob.skipcigame) {
                    configure { project ->
                        project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
                    }
                }
                mailer(email, false, false)
            }
        } else if(poijob.sonar) {
            steps {
                shellEx(delegate, shellcmds, poijob)

                gradle {
                    switches('-PenableSonar')
                    switches('-Dsonar.login=${POI_SONAR_TOKEN}')
                    switches('-Dsonar.organization=apache')
                    switches('-Dsonar.projectKey=poi-parent')
                    switches('-Dsonar.host.url=https://sonarcloud.io')
                    switches("-PjdkVersion=${jdkMapping.get(jdkKey).jdkVersion}")
                    if (jdkMapping.get(jdkKey).jdkVendor != '') {
                        switches("-PjdkVendor=${jdkMapping.get(jdkKey).jdkVendor}")
                    }
                    tasks('clean')
                    tasks('check')
                    tasks('jacocoTestReport')
                    tasks('sonarqube')
                    useWrapper(true)
                }
            }
            publishers {
                // in archive, junit and jacoco publishers, matches beneath build/*/build/... are for Gradle-build results
                archiveArtifacts('build/dist/*.tar.gz,*/build/reports/**,poi-integration/build/test-results/**,*/build/libs/*.jar')
                archiveJunit('*/build/test-results/**/TEST-*.xml') {
                    testDataPublishers {
                        publishTestStabilityData()
                    }
                }
                jacocoCodeCoverage {
                    classPattern('*/build/classes')
                    execPattern('*/build/*.exec,*/build/jacoco/*.exec')
                    sourcePattern('*/src/main/java')
                    exclusionPattern('com/microsoft/**,org/openxmlformats/**,org/etsi/**,org/w3/**,schemaorg*/**,schemasMicrosoft*/**,org/apache/poi/hdf/model/hdftypes/definitions/*.class,org/apache/poi/hwpf/model/types/*.class,org/apache/poi/hssf/usermodel/DummyGraphics2d.class,org/apache/poi/sl/draw/binding/*.class')
                }

                if (!poijob.skipcigame) {
                    configure { project ->
                        project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
                    }
                }
                mailer(email, false, false)
            }
        } else {
            steps {
                shellEx(delegate, shellcmds, poijob)
                if(poijob.addShell) {
                    shellEx(delegate, poijob.addShell, poijob)
                }
                // For Jobs that should still have the default set of publishers we can configure different steps here
                if(!poijob.useAnt) {
                    if (!poijob.windows) {
                        // Gradle will not run any tests if the code is up-to-date, therefore manually mark the files as updated
                        shellEx(delegate, 'touch --no-create build/*/build/test-results/TEST-*.xml build/*/build/test-results/test/TEST-*.xml', poijob)
                    }

                    gradle {
                        if (poijob.jenkinsLite) {
                            tasks('clean jenkinsLite')
                        } else {
                            tasks('clean jenkins')
                        }
                        useWrapper(true)
                        if (poijob.noScratchpad) {
                            switches('-Pscratchpad.ignore=true')
                        }
                        if (poijob.saxonTest) {
                            switches('-Psaxon.test=true')
                        }
                        switches("-PjdkVersion=${jdkMapping.get(jdkKey).jdkVersion}")
                        if (jdkMapping.get(jdkKey).jdkVendor != '') {
                            switches("-PjdkVendor=${jdkMapping.get(jdkKey).jdkVendor}")
                        }
                        switches("--refresh-dependencies")
                    }
                } else {
                    ant {
                        targets(['clean', 'jenkins'] + (poijob.properties ?: []))
                        prop('coverage.enabled', !poijob.skipSpotbugs)
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(antRT)
                    }
                    if(!poijob.skipSourceBuild) {
                        ant {
                            targets(['run'] + (poijob.properties ?: []))
                            buildFile('poi-integration/build.xml')
                            // Properties did not work, so I had to use targets instead
                            //properties(poijob.properties ?: '')
                            antInstallation(antRT)
                        }
                    }
                }
            }
            publishers {
                if (!poijob.skipSpotbugs) {
                    recordIssues {
                        tools {
                            spotBugs {
                                pattern('*/build/reports/spotbugs/*.xml')
                                reportEncoding('UTF-8')
                            }
                        }
                    }
                }
                // in archive, junit and jacoco publishers, matches beneath build/*/build/... are for Gradle-build results
                archiveArtifacts('build/dist/*.zip,build/dist/*.tgz,build/dist/maven/*/*.jar,build/coverage/**,*/build/reports/*.bom.*,build/hs_err*.log')
                /* this plugin is currently missing on the Apache Jenkins instance
                warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                    resolveRelativePaths()
                } */
                archiveJunit('*/build/test-results/**/TEST-*.xml') {
                    testDataPublishers {
                        publishTestStabilityData()
                    }
                }
                if (!poijob.skipSpotbugs) {
                    jacocoCodeCoverage {
                        classPattern('*/build/classes')
                        execPattern('*/build/*.exec,*/build/jacoco/*.exec')
                        sourcePattern('*/src/main/java')
                        exclusionPattern('com/microsoft/**,org/openxmlformats/**,org/etsi/**,org/w3/**,schemaorg*/**,schemasMicrosoft*/**,org/apache/poi/hdf/model/hdftypes/definitions/*.class,org/apache/poi/hwpf/model/types/*.class,org/apache/poi/hssf/usermodel/DummyGraphics2d.class,org/apache/poi/sl/draw/binding/*.class')
                    }
                }

                if (!poijob.skipcigame) {
                    configure { project ->
                        project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
                    }
                }
                mailer(email, false, false)
            }
        }

    }
}

xmlbeansjobs.each { xjob ->
    def jdkKey = xjob.jdk ?: defaultJdk
    def trigger = xjob.trigger ?: defaultTrigger
    def email = xjob.email ?: defaultEmail
    def slaves = xjob.slaves ?: defaultSlaves + (xjob.slaveAdd ?: '')
    def antRT = xjob.windows ? defaultAntWindows : defaultAnt

    job('POI/' + xjob.name) {
        if (xjob.disabled) {
            disabled()
        }

        description( defaultDesc + (xjob.apicheck ? apicheckDesc : sonarDesc.replace('poi-parent','apache_xmlbeans')) )
        logRotator {
            numToKeep(5)
            artifactNumToKeep(1)
        }
        label(slaves)
        environmentVariables {
            env('LANG', 'en_US.UTF-8')
            if (jdkKey == '1.11' || jdkKey == '1.15' || jdkKey == '1.16' || jdkKey == '1.17'
                    || jdkKey == '1.18' || jdkKey == '1.19' || jdkKey == '1.20' || jdkKey == '1.21') {
                env('ANT_OPTS', '--add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED')
            }
            // will be needed for forbidden-apis-check: env('ANT_HOME', xjob.windows ? 'f:\\jenkins\\tools\\ant\\latest' : '/usr/share/ant')
            env('FORREST_HOME', xjob.windows ? 'f:\\jenkins\\tools\\forrest\\latest' : '/home/jenkins/tools/forrest/latest')
        }
        wrappers {
            timeout {
                absolute(180)
                abortBuild()
                writeDescription('Build was aborted due to timeout')
            }
            if(xjob.sonar) {
                credentialsBinding {
                    string('POI_SONAR_TOKEN', 'sonarcloud-poi')
                }
                configure { project ->
                    project / buildWrappers << 'hudson.plugins.sonar.SonarBuildWrapper' {}
                }
            }
        }
        jdk(jdkMapping.get(jdkKey).jenkinsJdk)
        scm {
            svn(xmlbeansSvnBase) { svnNode ->
                svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                        url << 'https://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
            }
        }
        checkoutRetryCount(3)

        triggers {
            scm(trigger)
        }

        def shellcmds = (xjob.windows ? shellCmdsWin : shellCmdsUnix).replace('POIJOBSHELL', xjob.shell ?: '')

        // Create steps and publishers depending on the type of Job that is selected
        steps {
            shellEx(delegate, shellcmds, xjob)
            if(xjob.addShell) {
                shellEx(delegate, xjob.addShell, xjob)
            }

            gradle {
                if (xjob.sonar) {
                    switches('-PenableSonar')
                    switches('-Dsonar.login=${POI_SONAR_TOKEN}')
                    switches('-Dsonar.organization=apache')
                    switches('-Dsonar.projectKey=apache_xmlbeans')
                    switches('-Dsonar.host.url=https://sonarcloud.io')
                    switches("-PjdkVersion=${jdkMapping.get(jdkKey).jdkVersion}")
                    if (jdkMapping.get(jdkKey).jdkVendor != '') {
                        switches("-PjdkVendor=${jdkMapping.get(jdkKey).jdkVendor}")
                    }
                }
                tasks('clean')
                tasks('jenkins')
                tasks('jacocoTestReport')
                if (xjob.sonar) {
                    tasks('sonarqube')
                }
                useWrapper(true)
            }
        }
        publishers {
            archiveArtifacts('build/libs/xmlbeans*.jar,build/distributions/*,build/reports/*.bom.*,build/hs_err*.log')

            /* this plugin is currently missing on the Apache Jenkins instance
            warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                resolveRelativePaths()
            } */
            archiveJunit('build/test-results/test/TEST-*.xml') {
                testDataPublishers {
                    publishTestStabilityData()
                }
            }
            recordIssues {
                tools {
                    spotBugs {
                        pattern('build/reports/spotbugs/*/spotbugs.xml')
                        reportEncoding('UTF-8')
                    }
                }
            }

            if (!xjob.skipcigame) {
                configure { project ->
                    project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
                }
            }
            mailer(email, false, false)
        }
    }
}

/*
Add a special job which spans a two-dimensional matrix of all JDKs that we want to use and
all worker nodes that we would like to use and test if the java and ant binaries are available
on that machine correctly.
 */
matrixJob('POI/POI-DSL-Test-Environment') {
    description(
            '''Check installed version of Java/Ant on all build-nodes

This job is used to verify which machines actually have the required programs installed.

Unfortunately we often see builds break because of changes/new machines...''')

    /*throttleConcurrentBuilds {
        maxPerNode(1)
        maxTotal(1)
    }*/
    logRotator {
        numToKeep(1)
        artifactNumToKeep(1)
    }
    axes {
        jdk(
                'jdk_1.8_latest',
                'jdk_11_latest',
                /* don't look for JDKs that are out of support
                'jdk_10_latest',
                'jdk_12_latest',
                'jdk_13_latest',
                'jdk_14_latest',
                'jdk_15_latest',
                'jdk_16_latest',*/
                'jdk_17_latest',
                'jdk_18_latest',
                'jdk_19_latest',
                'jdk_20_latest',
                'jdk_21_latest',
                'jdk_22_latest',
                'adoptopenjdk_hotspot_8u282',
                'ibmjdk_1.8.0_261'
        )
        // Note H50 is reserved according to its node-description
        label('Nodes','builds22','builds23','builds24','builds25','builds26','builds27','builds28','builds29','builds30','builds31','builds32','builds33','builds34','builds35','builds36','builds37','builds38','builds39','builds40','builds50','builds56','builds57','builds58','builds59','builds60')
    }
    steps {
        conditionalSteps {
            condition {
                fileExists('/usr', BaseDir.WORKSPACE)
            }
            runner('DontRun')
            steps {
                shell(
                        '''which svn || true
which javac
javac -version
echo '<?xml version="1.0"?><project name="POI Build" default="test"><target name="test"><echo>Using Ant: ${ant.version} from ${ant.home}</echo></target></project>' > build.xml
''')
                ant {
                    antInstallation(defaultAnt)
                }

                shell(
                        '''which mvn || true
mvn -version || true
echo '<project><modelVersion>4.0.0</modelVersion><groupId>org.apache.poi</groupId><artifactId>build-tst</artifactId><version>1.0.0</version></project>' > pom.xml
''')
                maven {
                    goals('package')
                    mavenInstallation(defaultMaven)
                }
            }
        }
        conditionalSteps {
            condition {
                fileExists('c:\\windows', BaseDir.WORKSPACE)
            }
            runner('DontRun')
            steps {
                batchFile(
                        '''@echo off
echo .
where javac.exe
echo .
javac -version
echo .
echo ^<?xml version=^"1.0^"?^>^<project name=^"POI Build^" default=^"test^"^>^<target name=^"test^"^>^<echo^>Using Ant: ${ant.version} from ${ant.home}, ant detected Java ${ant.java.version} (may be different than actual Java sometimes...), using Java: ${java.version}/${java.runtime.version}/${java.vm.version}/${java.vm.name} from ${java.vm.vendor} on ${os.name}: ${os.version}^</echo^>^</target^>^</project^> > build.xml
''')
                ant {
                    antInstallation(defaultAntWindows)
                }
            }
        }
    }
}

/* I tried to put the view into a sub-folder/sub-view, but failed, there are multiple related
 plugins so this is all a bit confusing :(, see also https://issues.apache.org/jira/browse/INFRA-14002
dashboardView("P/POI-new") {
    columns {
        status()
        weather()
        configureProject()
        buildButton()
        cronTrigger()
        lastBuildConsole()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        //lastSuccessDescription()
        jacoco()
    }
    description("<table>\n" +
            "  <tr>\n" +
            "    <td><img src=\"https://poi.apache.org/images/project-header.png\" /></td>\n" +
            "    <td>  \n" +
            "      <p>Apache POI - the Java API for Microsoft Documents</p>\n" +
            "      <p><b>Most of the POI Jobs are automatically generated by Jenkins Job DSL\n" +
            "        at <a href=\"https://svn.apache.org/repos/asf/poi/trunk/jenkins\">https://svn.apache.org/repos/asf/poi/trunk/jenkins</a>,<br/>\n" +
            "        see <a href=\"https://github.com/jenkinsci/job-dsl-plugin/wiki\">https://github.com/jenkinsci/job-dsl-plugin/wiki</a>\n" +
            "        for more details about the DSL.</b>\n" +
            "      </p>\n" +
            "      <p>\n" +
            "      <b><a href=\"job/POI-DSL-1.8/lastSuccessfulBuild/findbugsResult/\" target=\"_blank\">Findbugs report of latest build</a></b> -\n" +
            "      <b><a href=\"https://sonarcloud.io/dashboard?id=poi-parent\" target=\"_blank\">Sonar reports</a></b> -\n" +
            "      <b><a href=\"job/POI-DSL-1.8/lastSuccessfulBuild/artifact/build/coverage/index.html\" target=\"_blank\">Coverage of latest build</a></b>\n" +
            "      </p>\n" +
            "    </td>\n" +
            "  </tr>\n" +
            "</table>")
    filterBuildQueue(false)
    filterExecutors(false)

    // Job selection
    jobs {*/
//regex(/.*POI.*/)
/*}

// Layout
topPortlets {
    jenkinsJobsList {
        displayName('POI jobs')
    }
}
leftPortlets {
    testStatisticsChart()
}
rightPortlets {
    testTrendChart()
}
bottomPortlets {
    testStatisticsGrid()
    buildStatistics()
}
}*/
