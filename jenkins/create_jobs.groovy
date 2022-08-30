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
        [ name: 'POI-DSL-1.8', trigger: 'H */12 * * *'
        ],
        [ name: 'POI-DSL-OpenJDK', jdk: 'OpenJDK 1.8', trigger: 'H */12 * * *',
          // only a limited set of nodes still have OpenJDK 8 (on Ubuntu) installed
          slaves: 'ubuntu',
          skipcigame: true
        ],
//        [ name: 'POI-DSL-1.10', jdk: '1.10', trigger: triggerSundays, skipcigame: true,
//          // let's save some CPU cycles here, 10 had EOL in September 2018
//          disabled: true
//        ],
        [ name: 'POI-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true
        ],
//        [ name: 'POI-DSL-1.12', jdk: '1.12', trigger: triggerSundays, skipcigame: true,
//          // let's save some CPU cycles here, 12 is not a LTS and JDK 13 is GA as of 17 September 2019
//          disabled: true
//        ],
//        [ name: 'POI-DSL-1.13', jdk: '1.13', trigger: triggerSundays, skipcigame: true,
//          // let's save some CPU cycles here, 13 is not a LTS and JDK 14 is GA as of 17 March 2020
//          disabled: true
//        ],
//        [ name: 'POI-DSL-1.14', jdk: '1.14', trigger: triggerSundays, skipcigame: true,
//          // let's save some CPU cycles here, 14 is not a LTS and JDK 15 is GA as of 15 September 2020
//          disabled: true
//        ],
        [ name: 'POI-DSL-1.15', jdk: '1.15', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 15 is not a LTS and JDK 16 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-1.16', jdk: '1.16', trigger: 'H */12 * * *', skipcigame: true,
          // let's save some CPU cycles here, 16 is not a LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-1.17', jdk: '1.17', trigger: 'H */12 * * *', skipcigame: true
        ],
        [ name: 'POI-DSL-1.18', jdk: '1.18', trigger: triggerSundays, skipcigame: true
        ],
        // Use Ant build for as Gradle 7.5 does not support Java 19 yet (change to gradle: true when we have Gradle support)
        [ name: 'POI-DSL-1.19', jdk: '1.19', trigger: triggerSundays, skipcigame: true, useAnt: true
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
        [ name: 'POI-DSL-Windows-1.8', trigger: 'H */12 * * *', windows: true, slaves: 'Windows'
        ],
//        [ name: 'POI-DSL-Windows-1.12', jdk: '1.12', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
//          // let's save some CPU cycles here, 12 is not a LTS and JDK 13 is GA now
//          disabled: true
//        ],
//        [ name: 'POI-DSL-Windows-1.14', jdk: '1.14', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
//		  // let's only verify the latest two JDKs
//		  disabled: true
//        ],
        [ name: 'POI-DSL-Windows-1.15', jdk: '1.15', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true,
          // let's save some CPU cycles here, 14 is not a LTS and JDK 15 is GA as of 15 September 2020
          disabled: true
        ],
        [ name: 'POI-DSL-Windows-1.16', jdk: '1.16', trigger: 'H */12 * * *', windows: true, slaves: 'Windows', skipcigame: true,
          // let's save some CPU cycles here, 16 is not a LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-DSL-Windows-1.17', jdk: '1.17', trigger: 'H */12 * * *', windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Windows-1.18', jdk: '1.18', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Github-PullRequests', trigger: '', githubpr: true, skipcigame: true,
          // ensure the file which is needed from the separate documentation module does exist
          // as we are checking out from git, we do not have the reference checked out here
          addShell: 'mkdir -p src/documentation\ntouch src/documentation/RELEASE-NOTES.txt'
        ],
]

def xmlbeansjobs = [
        [ name: 'POI-XMLBeans-DSL-1.8', jdk: '1.8', trigger: 'H */12 * * *', skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.16', jdk: '1.16', trigger: triggerSundays, skipcigame: true,
          // let's save some CPU cycles here, 16 is not a LTS and JDK 17 is GA
          disabled: true
        ],
        [ name: 'POI-XMLBeans-DSL-1.17', jdk: '1.17', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.18', jdk: '1.18', trigger: triggerSundays, skipcigame: true,
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
        '1.8': [ jenkinsJdk: 'jdk_1.8_latest', jdkVersion: 8, jdkVendor: 'oracle' ],
        '1.10': [ jenkinsJdk: 'jdk_10_latest', jdkVersion: 10, jdkVendor: 'oracle' ],
        '1.11': [ jenkinsJdk: 'jdk_11_latest', jdkVersion: 11, jdkVendor: 'oracle' ],
        '1.12': [ jenkinsJdk: 'jdk_12_latest', jdkVersion: 12, jdkVendor: '' ],
        '1.13': [ jenkinsJdk: 'jdk_13_latest', jdkVersion: 13, jdkVendor: '' ],
        '1.14': [ jenkinsJdk: 'jdk_14_latest', jdkVersion: 14, jdkVendor: '' ],
        '1.15': [ jenkinsJdk: 'jdk_15_latest', jdkVersion: 15, jdkVendor: '' ],
        '1.16': [ jenkinsJdk: 'jdk_16_latest', jdkVersion: 16, jdkVendor: '' ],
        '1.17': [ jenkinsJdk: 'jdk_17_latest', jdkVersion: 17, jdkVendor: '' ],
        '1.18': [ jenkinsJdk: 'jdk_18_latest', jdkVersion: 18, jdkVendor: '' ],
        '1.19': [ jenkinsJdk: 'jdk_19_latest', jdkVersion: 19, jdkVendor: '' ],
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
<b><a href="lastSuccessfulBuild/findbugsResult/" target="_blank">Findbugs report of latest build</a></b> -
<b><a href="https://sonarcloud.io/dashboard?id=poi-parent" target="_blank">Sonar reports</a></b> -
<b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>
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
            if(jdkKey == '1.10') {
                // when using JDK 9/10 for running Ant, we need to provide more modules for the forbidden-api-checks task
                // on JDK 11 and newer there is no such module any more, so do not add it here
                env('ANT_OPTS', '--add-modules=java.xml.bind --add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED')
            }
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
            if (poijob.githubpr) {
                git {
                    remote {
                        github('apache/poi')
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                    }
                    branch('${sha1}')
                }
            } else {
                svn(svnBase) { svnNode ->
                    svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                            url << 'https://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
                }
            }
        }
        checkoutRetryCount(3)

        if (poijob.githubpr) {
            throttleConcurrentBuilds {
                maxPerNode(1)
                maxTotal(1)
            }
            parameters {
                /* plugin not available:
                gitParam('sha1') {
                    description('Pull request')
                    type('BRANCH')
                }*/
                stringParam('sha1', 'origin/pr/9/head', 'Provide a branch-spec, e.g. origin/pr/9/head')
            }
            triggers {
                pullRequestBuildTrigger()
                /*githubPullRequest {
                    admins(['centic9', 'poi-benchmark', 'tballison', 'gagravarr', 'onealj', 'pjfanning', 'Alain-Bearez'])
                    userWhitelist(['centic9', 'poi-benchmark', 'tballison', 'gagravarr', 'onealj', 'pjfanning', 'Alain-Bearez'])
                    orgWhitelist(['apache'])
                    cron('H/5 * * * *')
                    triggerPhrase('OK to test')
                }*/
            }
        } else {
            triggers {
                scm(trigger)
            }
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
                        tasks('clean jenkins')
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
                        prop('coverage.enabled', true)
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(antRT)
                    }
                    ant {
                        targets(['run'] + (poijob.properties ?: []))
                        buildFile('poi-integration/build.xml')
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(antRT)
                    }
                }
            }
            publishers {
                recordIssues {
                    tools {
                        spotBugs {
                            pattern('*/build/reports/spotbugs/*.xml')
                            reportEncoding('UTF-8')
                        }
                    }
                }
                // in archive, junit and jacoco publishers, matches beneath build/*/build/... are for Gradle-build results
                archiveArtifacts('build/dist/*.zip,build/dist/*.tgz,build/dist/maven/*/*.jar,build/coverage/**,build/hs_err*.log')
                warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                    resolveRelativePaths()
                }
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
        }


        if (poijob.githubpr) {
            configure {
                it / 'properties' << 'com.cloudbees.jenkins.plugins.git.vmerge.JobPropertyImpl'(plugin: 'git-validated-merge') {
                    credentialsId('ASF_Cloudbees_Jenkins_ci-builds')
                    postBuildPushFailureHandler(class: 'com.cloudbees.jenkins.plugins.git.vmerge.pbph.PushFailureIsFailure')
                }
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
            if(jdkKey == '1.10') {
                // when using JDK 9/10 for running Ant, we need to provide more modules for the forbidden-api-checks task
                // on JDK 11 and newer there is no such module any more, so do not add it here
                env('ANT_OPTS', '--add-modules=java.xml.bind --add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED')
            } else if (jdkKey == '1.11' || jdkKey == '1.12' || jdkKey == '1.13' || jdkKey == '1.14' || jdkKey == '1.15' || jdkKey == '1.16' || jdkKey == '1.17' || jdkKey == '1.18' || jdkKey == '1.19') {
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
            archiveArtifacts('build/libs/xmlbeans*.jar,build/distributions/*,build/hs_err*.log')

            warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                resolveRelativePaths()
            }
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
                'jdk_10_latest',
                'jdk_11_latest',
                /* don't look for JDKs that are out of support
                'jdk_12_latest',
                'jdk_13_latest',
                'jdk_14_latest',
                'jdk_15_latest',
                'jdk_16_latest',*/
                'jdk_17_latest',
                'jdk_18_latest',
                'jdk_19_latest',
                'adoptopenjdk_hotspot_8u282',
                'ibmjdk_1.8.0_261'
        )
        // Note H50 is reserved according to it's node-descripion
        label('Nodes','H22','H23','H24','H25','H26','H27','H28','H29','H30','H31','H32','H33','H34','H35','H36','H37','H38','H39','H40','H41','H42','H43','H44','H48','lucene1','lucene2','master')
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
