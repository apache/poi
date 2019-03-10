// This script is used as input to the Jenkins Job DSL plugin to create all the build-jobs that
// Apache POI uses on the public Jenkins instance at https://builds.apache.org/view/P/view/POI/
//
// See https://github.com/jenkinsci/job-dsl-plugin/wiki for information about the DSL, you can
// use http://job-dsl.herokuapp.com/ to validate the code before checkin
// 

def triggerSundays = '''
# only run this once per week on Sundays
H H * * 0
'''

def xercesUrl = 'http://repo1.maven.org/maven2/xerces/xercesImpl/2.6.1/xercesImpl-2.6.1.jar'
def xercesLib = './xercesImpl-2.6.1.jar'

def poijobs = [
        [ name: 'POI-DSL-1.8', trigger: 'H */12 * * *'
        ],
        [ name: 'POI-DSL-OpenJDK', jdk: 'OpenJDK', trigger: 'H */12 * * *',
          // H13-H20 (Ubuntu 16.04) do not have OpenJDK 6 installed, see https://issues.apache.org/jira/browse/INFRA-12880
          slaveAdd: '&&!beam1&&!beam2&&!beam3&&!beam4&&!beam5&&!beam6&&!beam7&&!beam8' +
                  '&&!H0&&!H1&&!H2&&!H3&&!H4&&!H5&&!H6&&!H7&&!H8&&!H9&&!H10&&!H11' +
                  '&&!qnode3' +
                  '&&!ubuntu-1&&!ubuntu-2&&!ubuntu-4&&!ubuntu-5&&!ubuntu-6&&!ubuntu-eu2&&!ubuntu-us1',
          // the JDK is missing on some slaves so builds are unstable
          skipcigame: true
        ],
        [ name: 'POI-DSL-1.10', jdk: '1.10', trigger: triggerSundays, skipcigame: true
        ],
        [ name: 'POI-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true,
          // Nodes beam* do not yet have JDK 11 installed
          slaveAdd: '&&!beam1&&!beam2&&!beam3&&!beam4&&!beam6&&!beam7&&!beam8&&!beam9&&!beam10&&!beam11&&!beam12&&!beam13&&!beam14&&!beam15&&!beam16'
        ],
        [ name: 'POI-DSL-1.12', jdk: '1.12', trigger: triggerSundays, skipcigame: true,
          // Nodes beam* do not yet have JDK 12 installed
          // H43 has outdated JDK12 installed
          slaveAdd: '&&!beam1&&!beam2&&!beam3&&!beam4&&!beam6&&!beam7&&!beam8&&!beam9&&!beam10&&!beam11&&!beam12&&!beam13&&!beam14&&!beam15&&!beam16&&!H43'
        ],
        [ name: 'POI-DSL-1.13', jdk: '1.13', trigger: triggerSundays, skipcigame: true,
          // Nodes beam* do not yet have JDK 13 installed
          slaveAdd: '&&!beam1&&!beam2&&!beam3&&!beam4&&!beam6&&!beam7&&!beam8&&!beam9&&!beam10&&!beam11&&!beam12&&!beam13&&!beam14&&!beam15&&!beam16',
          properties: [// JaCoCo currently fails with "java.lang.NoSuchFieldException: $jacocoAccess",
                       // need to review/check with newer JDK 13 builds or when at least JaCoCo 0.8.3
                       '-Dcoverage.enabled=false']
        ],
        [ name: 'POI-DSL-IBM-JDK', jdk: 'IBMJDK', trigger: triggerSundays, skipcigame: true
        ],
        [ name: 'POI-DSL-old-Xerces', trigger: triggerSundays,
          shell: "test -f ${xercesLib} || wget -O ${xercesLib} ${xercesUrl}\n",
          // the property triggers using Xerces as XML Parser and previously showed some exception that can occur
          properties: ["-Dadditionaljar=${xercesLib}"]
        ],
        [ name: 'POI-DSL-Maven', trigger: 'H */4 * * *', maven: true
        ],
        [ name: 'POI-DSL-regenerate-javadoc', trigger: triggerSundays, javadoc: true
        ],
        [ name: 'POI-DSL-API-Check', trigger: '@daily', apicheck: true
        ],
        [ name: 'POI-DSL-Gradle', trigger: triggerSundays, email: 'centic@apache.org', gradle: true,
          // Gradle will not run any tests if the code is up-to-date, therefore manually mark the files as updated
          addShell: 'touch --no-create build/*/build/test-results/TEST-*.xml build/*/build/test-results/test/TEST-*.xml'
        ],
        [ name: 'POI-DSL-no-scratchpad', trigger: triggerSundays, noScratchpad: true
        ],
        [ name: 'POI-DSL-SonarQube', trigger: 'H 9 * * *', maven: true, sonar: true, skipcigame: true
        ],
        [ name: 'POI-DSL-SonarQube-Gradle', trigger: 'H 9 * * *', gradle: true, sonar: true, skipcigame: true,
                disabled: true // this one does run, but does not actually send data to Sonarqube for some reason, we need to investigate some more
        ],
        [ name: 'POI-DSL-Windows-1.8', trigger: 'H */12 * * *', windows: true, slaves: 'Windows'
        ],
        [ name: 'POI-DSL-Windows-1.12', jdk: '1.12', trigger: triggerSundays, windows: true, slaves: 'Windows', skipcigame: true
        ],
        [ name: 'POI-DSL-Github-PullRequests', trigger: '', githubpr: true, skipcigame: true,
          // ensure the file which is needed from the separate documentation module does exist
          // as we are checking out from git, we do not have the reference checked out here
          addShell: 'mkdir src/documentation\ntouch src/documentation/RELEASE-NOTES.txt'
        ],
]

def xmlbeansjobs = [
        [ name: 'POI-XMLBeans-DSL-1.6', jdk: '1.6', trigger: 'H */12 * * *', skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.8', jdk: '1.8', trigger: triggerSundays, skipcigame: true,
        ],
        [ name: 'POI-XMLBeans-DSL-1.11', jdk: '1.11', trigger: triggerSundays, skipcigame: true,
                disabled: true // XMLBeans does not yet compile with Java 11
        ],
        [ name: 'POI-XMLBeans-DSL-1.12', jdk: '1.12', trigger: triggerSundays, skipcigame: true,
                disabled: true // XMLBeans does not yet compile with Java 11
        ]
]

def svnBase = 'https://svn.apache.org/repos/asf/poi/trunk'
def xmlbeansSvnBase = 'https://svn.apache.org/repos/asf/xmlbeans/trunk'

def defaultJdk = '1.8'
def defaultTrigger = 'H/15 * * * *'     // check SCM every 60/15 = 4 minutes
def defaultEmail = 'dev@poi.apache.org'
def defaultAnt = 'Ant 1.9.9'
// currently a lot of H?? slaves don't have Ant installed ... H21 seems to have a SVN problem
// H35 fails with ImageIO create cache file errors, although the java.io.tmpdir is writable
def defaultSlaves = '(ubuntu||beam)&&!cloud-slave&&!H15&&!H17&&!H18&&!H24&&!ubuntu-4&&!H21&&!H35'

def jdkMapping = [
        '1.6': 'JDK 1.6 (latest)',
        '1.8': 'JDK 1.8 (latest)',
        '1.10': 'JDK 10 (latest)',
        '1.11': 'JDK 11 (latest)',
        '1.12': 'JDK 12 (latest)',
        '1.13': 'JDK 13 (latest)',
        'OpenJDK': 'OpenJDK 8 (on Ubuntu only) ',   // blank is required here until the name in the Jenkins instance is fixed!
        'IBMJDK': 'IBM 1.8 64-bit (on Ubuntu only)',
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
<b><a href="https://builds.apache.org/analysis/dashboard?id=org.apache.poi%3Apoi-parent&did=1" target="_blank">Sonar reports</a></b> -
<p>
<b><a href="lastSuccessfulBuild/artifact/build/main/build/reports/japi.html">API Check POI</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/ooxml/build/reports/japi.html">API Check POI-OOXML</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/excelant/build/reports/japi.html">API Check POI-Excelant</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/scratchpad/build/reports/japi.html">API Check POI-Scratchpad</a></b>

</p>
'''

def sonarDesc = '''
<p>
<b><a href="lastSuccessfulBuild/findbugsResult/" target="_blank">Findbugs report of latest build</a></b> -
<b><a href="https://builds.apache.org/analysis/dashboard?id=org.apache.poi%3Apoi-parent&did=1" target="_blank">Sonar reports</a></b> -
<b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>
</p>
'''

def shellCmdsUnix =
        '''# show which files are currently modified in the working copy
svn status

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
which ant
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
    def antRT = defaultAnt + (poijob.windows ? ' (Windows)' : '')

    job(poijob.name) {
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
            if(jdkKey == '1.10') {
                // when using JDK 9/10 for running Ant, we need to provide more modules for the forbidden-api-checks task
                // on JDK 11 and newer there is no such module any more, so do not add it here
                env('ANT_OPTS', '--add-modules=java.xml.bind --add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED')
            }
            env('FORREST_HOME', poijob.windows ? 'f:\\jenkins\\tools\\forrest\\latest' : '/home/jenkins/tools/forrest/latest')
        }
        wrappers {
            timeout {
                absolute(180)
                abortBuild()
                writeDescription('Build was aborted due to timeout')
            }
            preBuildCleanup {
                includePattern('**/ooxml-lib/ooxml*.jar')
            }
            if(poijob.sonar) {
                configure { project ->
                    project / buildWrappers << 'hudson.plugins.sonar.SonarBuildWrapper' {}
                }
            }
        }
        jdk(jdkMapping.get(jdkKey))
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
                            url << 'http://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
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
                githubPullRequest {
                    admins(['centic9', 'poi-benchmark', 'tballison', 'gagravarr', 'onealj', 'pjfanning', 'Alain-Bearez'])
                    userWhitelist(['centic9', 'poi-benchmark', 'tballison', 'gagravarr', 'onealj', 'pjfanning', 'Alain-Bearez'])
                    orgWhitelist(['apache'])
                    cron('H/5 * * * *')
                    triggerPhrase('OK to test')
                }
            }
        } else {
            triggers {
                scm(trigger)
            }
        }

        def shellcmds = (poijob.windows ? shellCmdsWin : shellCmdsUnix).replace('POIJOBSHELL', poijob.shell ?: '')

        // Create steps and publishers depending on the type of Job that is selected
        if(poijob.maven) {
            steps {
                shellEx(delegate, shellcmds, poijob)
                maven {
                    goals('clean')
                    rootPOM('sonar/pom.xml')
                    localRepository(LocalRepositoryLocation.LOCAL_TO_WORKSPACE)
                    mavenInstallation('maven-3.2.1')
                }
                /* Currently not done, let's see if it is still necessary:
                    # Maven-Download fails for strange reasons, try to workaround...
                    mkdir -p sonar/ooxml-schema-security/target/schemas && wget -O sonar/ooxml-schema-security/target/schemas/xmldsig-core-schema.xsd http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd
                */
                maven {
                    if(poijob.sonar) {
                        goals('compile $SONAR_MAVEN_GOAL -Dsonar.host.url=$SONAR_HOST_URL')
                    } else {
                        goals('package')
                    }
                    rootPOM('sonar/pom.xml')
                    mavenOpts('-Xmx2g')
                    mavenOpts('-Xms256m')
                    mavenOpts('-XX:-OmitStackTraceInFastThrow')
                    localRepository(LocalRepositoryLocation.LOCAL_TO_WORKSPACE)
                    mavenInstallation('maven-3.2.1')
                }
            }
            publishers {
                if (!poijob.skipcigame) {
                    configure { project ->
                        project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
                    }
                }
                if (!poijob.sonar) {
                    archiveJunit('sonar/*/target/surefire-reports/TEST-*.xml') {
                        testDataPublishers {
                            publishTestStabilityData()
                        }
                    }
                }
                mailer(email, false, false)
            }
        } else if (poijob.javadoc) {
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
        } else if (poijob.apicheck) {
            steps {
                shellEx(delegate, shellcmds, poijob)
                gradle {
                    tasks('japicmp')
                    useWrapper(false)
                }
            }
            publishers {
                archiveArtifacts('build/*/build/reports/japi.html')
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
                    switches('-Dsonar.host.url=$SONAR_HOST_URL')
                    tasks('sonarqube')
                    useWrapper(false)
                }
            }
            publishers {
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
                if(poijob.gradle) {
                    gradle {
                        tasks('check')
                        useWrapper(false)
                    }
                } else if (poijob.noScratchpad) {
                    ant {
                        targets(['clean', 'compile'] + (poijob.properties ?: []))
                        prop('coverage.enabled', true)
                        antInstallation(antRT)
                    }
                    ant {
                        targets(['-Dscratchpad.ignore=true', 'jacocotask', 'test-all', 'testcoveragereport'] + (poijob.properties ?: []))
                        prop('coverage.enabled', true)
                        antInstallation(antRT)
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
                        buildFile('src/integrationtest/build.xml')
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(antRT)
                    }
                }
            }
            publishers {
                findbugs('build/findbugs.xml', false) {
                    healthLimits(3, 20)
                    thresholdLimit('low')
                    defaultEncoding('UTF-8')
                }
                // in archive, junit and jacoco publishers, matches beneath build/*/build/... are for Gradle-build results
                archiveArtifacts('build/dist/*.tar.gz,build/findbugs.html,build/coverage/**,build/integration-test-results/**,ooxml-lib/**,build/*/build/libs/*.jar')
                warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                    resolveRelativePaths()
                }
                archiveJunit('build/ooxml-test-results/*.xml,build/scratchpad-test-results/*.xml,build/test-results/*.xml,build/excelant-test-results/*.xml,build/integration-test-results/*.xml,build/*/build/test-results/test/TEST-*.xml,build/*/build/test-results/TEST-*.xml') {
                    testDataPublishers {
                        publishTestStabilityData()
                    }
                }
                jacocoCodeCoverage {
                    classPattern('build/classes,build/excelant-classes,build/ooxml-classes,build/scratchpad-classes,build/*/build/classes')
                    execPattern('build/*.exec,build/*/build/jacoco/*.exec')
                    sourcePattern('src/java,src/excelant/java,src/ooxml/java,src/scratchpad/src')
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
    }
}

xmlbeansjobs.each { xjob ->
    def jdkKey = xjob.jdk ?: defaultJdk
    def trigger = xjob.trigger ?: defaultTrigger
    def email = xjob.email ?: defaultEmail
    def slaves = xjob.slaves ?: defaultSlaves + (xjob.slaveAdd ?: '')
    def antRT = defaultAnt + (xjob.windows ? ' (Windows)' : '')

    job(xjob.name) {
        if (xjob.disabled) {
            disabled()
        }

        description( defaultDesc + (xjob.apicheck ? apicheckDesc : sonarDesc) )
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
            } else if (jdkKey == '1.11' || jdkKey == '1.12' || jdkKey == '1.13') {
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
        }
        jdk(jdkMapping.get(jdkKey))
        scm {
            svn(xmlbeansSvnBase) { svnNode ->
                svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                        url << 'http://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
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
            ant {
                targets(['clean'])
                antInstallation(antRT)
            }
            ant {
                targets(['jenkins'])
                antInstallation(antRT)
            }
        }
        publishers {
            archiveArtifacts('build/**')

            warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
                resolveRelativePaths()
            }
            archiveJunit('build/test-results/TEST-*.xml') {
                testDataPublishers {
                    publishTestStabilityData()
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
all slaves that we would like to use and test if the java and ant binaries are available
on that machine correctly.
 */
matrixJob('POI-DSL-Test-Environment') {
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
                'JDK 1.8 (latest)',
                'OpenJDK 8 (on Ubuntu only) ',   // blank is required here until the name in the Jenkins instance is fixed!
                'IBM 1.8 64-bit (on Ubuntu only)',

                'JDK 10 (latest)',

                'JDK 11 (latest)',

                'JDK 12 (latest)',

                'JDK 13 (latest)'
        )
        elasticAxis {
            name('Nodes')
            labelString('!cloud-slave&&!H15&&!H17&&!H18&&!H24&&!ubuntu-4&&!H21&&!H35&&!websites1&&!couchdb&&!plc4x&&!ppc64le')
            ignoreOffline(true)
        }
    }
    steps {
        conditionalSteps {
            condition {
                fileExists('/usr', BaseDir.WORKSPACE)
                runner('DontRun')
                steps {
                    shell(
'''which javac
javac -version
echo '<?xml version="1.0"?><project name="POI Build" default="test"><target name="test"><echo>Using Ant: ${ant.version} from ${ant.home}</echo></target></project>' > build.xml
''')
                    ant {
                        antInstallation(defaultAnt)
                    }

                }
            }
        }
        conditionalSteps {
            condition {
                fileExists('c:\\windows', BaseDir.WORKSPACE)
                runner('DontRun')
                steps {
                    batchFile {
                        command(
'''@echo off
echo .
where javac.exe
echo .
javac -version
echo .
echo ^<?xml version=^"1.0^"?^>^<project name=^"POI Build^" default=^"test^"^>^<target name=^"test^"^>^<echo^>Using Ant: ${ant.version} from ${ant.home}, ant detected Java ${ant.java.version} (may be different than actual Java sometimes...), using Java: ${java.version}/${java.runtime.version}/${java.vm.version}/${java.vm.name} from ${java.vm.vendor} on ${os.name}: ${os.version}^</echo^>^</target^>^</project^> > build.xml
''')
                    }
                    ant {
                        antInstallation(defaultAnt + ' (Windows)')
                    }
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
            "      <b><a href=\"https://builds.apache.org/analysis/dashboard?id=org.apache.poi%3Apoi-parent&did=1\" target=\"_blank\">Sonar reports</a></b> -\n" +
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
