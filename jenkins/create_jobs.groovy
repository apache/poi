// This script is used as input to the Jenkins Job DSL plugin to create all the build-jobs that
// Apache POI uses on the public Jenkins instance at https://builds.apache.org/view/POI/
//
// See https://github.com/jenkinsci/job-dsl-plugin/wiki for information about the DSL, you can
// use http://job-dsl.herokuapp.com/ to validate the code before checkin
// 

def triggerSundays = '''
# only run this once per week on Sundays
H H * * 0
'''

def poijobs = [
    [ name: 'POI-DSL-1.6',
            // workaround as Sourceforge does not accept any of the SSL ciphers in JDK 6 any more and thus we cannot download this jar
            // as part of the Ant build
            addShell: 'wget -O lib/findbugs-noUpdateChecks-2.0.3.zip http://downloads.sourceforge.net/project/findbugs/findbugs/2.0.3/findbugs-noUpdateChecks-2.0.3.zip?download='
    ],
    [ name: 'POI-DSL-1.8', jdk: '1.8', trigger: 'H */12 * * *'
    ],
    [ name: 'POI-DSL-OpenJDK', jdk: 'OpenJDK', trigger: 'H */12 * * *',
        // H13-H20 (Ubuntu 16.04) do not have OpenJDK 6 installed, see https://issues.apache.org/jira/browse/INFRA-12880
        slaveAdd: '&&!beam1&&!beam2&&!beam3&&!beam4&&!beam5&&!beam6&&!beam7&&!beam8&&!H12&&!H13&&!H14&&!H15&&!H16&&!H17&&!H18&&!H19&&!H20&&!H21&&!H22&&!H23&&!H24&&!qnode1&&!qnode2&&!qnode3&&!ubuntu-eu2&&!ubuntu-eu3&&!ubuntu-us1',
        // the JDK is missing on some slaves so builds are unstable
        skipcigame: true
    ],
    [ name: 'POI-DSL-1.9', jdk: '1.9', trigger: triggerSundays,
        properties: ['-Dmaxpermsize=-Dthis.is.a.dummy=true',
                     '-Djava9addmods=--add-modules=java.xml.bind',
                     '-Djavadoc9addmods=--add-modules=java.xml.bind',
                     '-Djava9addmodsvalue=-Dsun.reflect.debugModuleAccessChecks=true',
                     '-Djava9addopens1=--add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED',
                     '-Djava9addopens2=--add-opens=java.base/java.io=ALL-UNNAMED',
                     '-Djava9addopens3=--add-opens=java.base/java.nio=ALL-UNNAMED',
                     '-Djava9addopens4=--add-opens=java.base/java.lang=ALL-UNNAMED',
                     '-Djava9addopens5=--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED',
                     '-Djava.locale.providers=JRE,CLDR'],
        skipcigame: true
    ],
    [ name: 'POI-DSL-IBM-JDK', jdk: 'IBMJDK', trigger: triggerSundays,
        // some OOXML tests fail with strange XML parsing errors and missing JCE unlimited strength requirements
        disabled: true, skipcigame: true
    ],
    [ name: 'POI-DSL-old-Xerces', trigger: triggerSundays,
        shell: 'mkdir -p compile-lib && test -f compile-lib/xercesImpl-2.6.1.jar || wget -O compile-lib/xercesImpl-2.6.1.jar http://repo1.maven.org/maven2/xerces/xercesImpl/2.6.1/xercesImpl-2.6.1.jar\n',
        // the property triggers using Xerces as XML Parser and previously showed some exception that can occur
        properties: ['-Dadditionaljar=compile-lib/xercesImpl-2.6.1.jar'],
        // workaround as Sourceforge does not accept any of the SSL ciphers in JDK 6 any more and thus we cannot download this jar
        // as part of the Ant build
        addShell: 'wget -O lib/findbugs-noUpdateChecks-2.0.3.zip http://downloads.sourceforge.net/project/findbugs/findbugs/2.0.3/findbugs-noUpdateChecks-2.0.3.zip?download='
    ],
    [ name: 'POI-DSL-Maven', trigger: 'H */4 * * *', maven: true
    ],
    [ name: 'POI-DSL-regenerate-javadoc', trigger: triggerSundays, javadoc: true
    ],
    [ name: 'POI-DSL-API-Check', jdk: '1.7', trigger: '@daily', apicheck: true
    ],
    [ name: 'POI-DSL-Gradle', jdk: '1.7', trigger: triggerSundays, email: 'centic@apache.org', gradle: true,
        // Gradle will not run any tests if the code is up-to-date, therefore manually mark the files as updated
        addShell: 'touch --no-create build/*/build/test-results/TEST-*.xml build/*/build/test-results/test/TEST-*.xml'
    ],
    [ name: 'POI-DSL-no-scratchpad', trigger: triggerSundays, noScratchpad: true
    ],
    [ name: 'POI-DSL-SonarQube', jdk: '1.8', trigger: 'H 9 * * *', maven: true, sonar: true
    ],
    [ name: 'POI-DSL-SonarQube-Gradle', jdk: '1.8', trigger: 'H 9 * * *', gradle: true, sonar: true, skipcigame: true
    ],
]

def svnBase = 'https://svn.apache.org/repos/asf/poi/trunk'
def defaultJdk = '1.6'
def defaultTrigger = 'H/15 * * * *'     // check SCM every 60/15 = 4 minutes
def defaultEmail = 'dev@poi.apache.org'
def defaultAnt = 'Ant 1.9.9'
// currently a lot of H?? slaves don't have Ant installed ...
def defaultSlaves = 'ubuntu&&!cloud-slave&&!H15&&!H17&&!H18&&!H24&&!ubuntu-4'

def jdkMapping = [
    '1.6': 'JDK 1.6 (latest)',
    '1.7': 'JDK 1.7 (latest)',
    '1.8': 'JDK 1.8 (latest)',
    '1.9': 'JDK 9 b156 (early access build) with project Jigsaw',
    'OpenJDK': 'OpenJDK 6 (on Ubuntu only) ',   // blank is required here until the name in the Jenkins instance is fixed!
    'IBMJDK': 'IBM 1.8 64-bit (on Ubuntu only)',
]

poijobs.each { poijob ->
    def jdkKey = poijob.jdk ?: defaultJdk
    def trigger = poijob.trigger ?: defaultTrigger
    def email = poijob.email ?: defaultEmail
    def slaves = defaultSlaves + (poijob.slaveAdd ?: '')

    job(poijob.name) {
        if (poijob.disabled) {
            disabled()
        }

        def defaultDesc = '''
<img src="https://poi.apache.org/resources/images/project-logo.jpg" />
<p>
Apache POI - the Java API for Microsoft Documents
</p>
<p>
<b>This is an automatically generated Job Config, do not edit it here!
Instead change the Jenkins Job DSL at <a href="https://svn.apache.org/repos/asf/poi/trunk/jenkins">https://svn.apache.org/repos/asf/poi/trunk/jenkins</a>,
see <a href="https://github.com/jenkinsci/job-dsl-plugin/wiki">https://github.com/jenkinsci/job-dsl-plugin/wiki</a>
for more details about the DSL.</b>
</p>'''

        description( defaultDesc +
(poijob.apicheck ? 
'''
<p>
<b><a href="https://builds.apache.org/analysis/dashboard?id=org.apache.poi%3Apoi-parent&did=1" target="_blank">Sonar reports</a></b> -
<p>
<b><a href="lastSuccessfulBuild/artifact/build/main/build/reports/japi.html">API Check POI</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/ooxml/build/reports/japi.html">API Check POI-OOXML</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/excelant/build/reports/japi.html">API Check POI-Excelant</a></b>
<b><a href="lastSuccessfulBuild/artifact/build/scratchpad/build/reports/japi.html">API Check POI-Scratchpad</a></b>

</p>
''' :
'''
<p>
<b><a href="lastSuccessfulBuild/findbugsResult/" target="_blank">Findbugs report of latest build</a></b> -
<b><a href="https://builds.apache.org/analysis/dashboard?id=org.apache.poi%3Apoi-parent&did=1" target="_blank">Sonar reports</a></b> -
<b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>
</p>
'''))
        logRotator {
            numToKeep(5)
            artifactNumToKeep(1)
        }
        label(slaves)
        environmentVariables {
            env('LANG', 'en_US.UTF-8')
            if(jdkKey == '1.9') {
                // when using JDK 9 for running Ant, we need to provide more packages for the forbidden-api-checks task
                env('ANT_OPTS', '--add-modules=java.xml.bind')
            }
        }
        wrappers {
            timeout {
                absolute(180)
                abortBuild()
                writeDescription('Build was aborted due to timeout')
            }
            if(poijob.sonar) {
                configure { project ->
                    project / buildWrappers << 'hudson.plugins.sonar.SonarBuildWrapper' {}
                }
            }
        }
        jdk(jdkMapping.get(jdkKey))
        scm {
            svn(svnBase) { svnNode ->
                        svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                            url << 'http://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
                    }
        }
        triggers {
            scm(trigger)
        }

        def shellcmds = '# show which files are currently modified in the working copy\n' +
            'svn status\n' +
            '\n' +
            'echo Java-Home: $JAVA_HOME\n' +
            'ls -al $JAVA_HOME/\n' +
            '\n' +
            (poijob.shell ?: '') + '\n' +
            '# ignore any error message\n' +
            'exit 0\n'

        // Create steps and publishers depending on the type of Job that is selected
        if(poijob.maven) {
            steps {
                shell(shellcmds)
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
                    mavenOpts('-XX:MaxPermSize=512m')
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
                mailer(email, false, false)
            }
        } else if (poijob.javadoc) {
            steps {
                shell(shellcmds)
                ant {
                    targets(['clean', 'javadocs'] + (poijob.properties ?: []))
                    prop('coverage.enabled', true)
                    // Properties did not work, so I had to use targets instead
                    //properties(poijob.properties ?: '')
                    antInstallation(defaultAnt)
                }
                shell('zip -r build/javadocs.zip build/tmp/site/build/site/apidocs')
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
                shell(shellcmds)
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
                shell(shellcmds)
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
                shell(shellcmds)
                if(poijob.addShell) {
                    shell(poijob.addShell)
                }
                // For Jobs that should still have the default set of publishers we can configure different steps here
                if(poijob.gradle) {
                    gradle {
                        tasks('check')
                        useWrapper(false)
                    }
                } else if (poijob.noScratchpad) {
                    ant {
                        targets(['clean', 'compile-all'] + (poijob.properties ?: []))
                        prop('coverage.enabled', true)
                        antInstallation(defaultAnt)
                    }
                    ant {
                        targets(['-Dscratchpad.ignore=true', 'jacocotask', 'test-all', 'testcoveragereport'] + (poijob.properties ?: []))
                        prop('coverage.enabled', true)
                        antInstallation(defaultAnt)
                    }
                } else {
                    ant {
                        targets(['clean', 'jenkins'] + (poijob.properties ?: []))
                        prop('coverage.enabled', true)
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(defaultAnt)
                    }
                    ant {
                        targets(['run'] + (poijob.properties ?: []))
                        buildFile('src/integrationtest/build.xml')
                        // Properties did not work, so I had to use targets instead
                        //properties(poijob.properties ?: '')
                        antInstallation(defaultAnt)
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
