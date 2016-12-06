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
    [ name: 'POI-DSL-1.6', jdks: ['1.6'] 
    ],
    [ name: 'POI-DSL-1.8', jdks: ['1.8'], trigger: 'H */12 * * *'
    ],
    [ name: 'POI-DSL-OpenJDK', jdks: ["OpenJDK"], trigger: 'H */12 * * *',
        // H16 does not have OpenJDK 6 installed
        slaveAdd: '&&!H16'
    ],
    [ name: 'POI-DSL-1.9', jdks: ['1.9'], trigger: triggerSundays,
        properties: ['-Dmaxpermsize=-Dthis.is.a.dummy=true', '-Djava9addmods=--add-modules=java.xml.bind', '-Djava9addmodsvalue=-Dsun.reflect.debugModuleAccessChecks=true', '-Djava.locale.providers=JRE,CLDR'],
        email: 'centic@apache.org', skipcigame: true
    ],
    // This config was not enabled in Jenkins ever because we did not find the JDK on any of the slaves, we can check this again later
    [ name: 'POI-DSL-IBM-JDK', jdks: ['IBMJDK'], trigger: triggerSundays, noScratchpad: true, disabled: true, skipcigame: true
    ],
    [ name: 'POI-DSL-old-Xerces', jdks: ['1.6'], trigger: triggerSundays,
        shell: 'mkdir -p compile-lib && test -f compile-lib/xercesImpl-2.6.1.jar || wget -O compile-lib/xercesImpl-2.6.1.jar http://repo1.maven.org/maven2/xerces/xercesImpl/2.6.1/xercesImpl-2.6.1.jar\n',
        // the property triggers using Xerces as XML Parser and previously showed some exception that can occur
        properties: ['-Dadditionaljar=compile-lib/xercesImpl-2.6.1.jar']
    ],
    [ name: 'POI-DSL-Maven', trigger: 'H */4 * * *', maven: true
    ],
    [ name: 'POI-DSL-regenerate-javadoc', trigger: triggerSundays, javadoc: true
    ],
    [ name: 'POI-DSL-API-Check', jdks: ['1.7'], trigger: '@daily', apicheck: true
    ],
    [ name: 'POI-DSL-Gradle', jdks: ['1.7'], trigger: triggerSundays, email: 'centic@apache.org', gradle: true
    ],
    [ name: 'POI-DSL-no-scratchpad', trigger: triggerSundays, noScratchpad: true
    ],
]

def svnBase = "https://svn.apache.org/repos/asf/poi/trunk"
def defaultJdks = ['1.6']
def defaultTrigger = 'H/15 * * * *'
def defaultEmail = 'dev@poi.apache.org'
def defaultAnt = 'Ant (latest)'

def jdkMapping = [
    '1.6': "JDK 1.6 (latest)",
    '1.7': "JDK 1.7 (latest)",
    '1.8': "JDK 1.8 (latest)",
    '1.9': "JDK 9 b142 (early access build) with project Jigsaw",
    "OpenJDK": "OpenJDK 6 (on Ubuntu only) ",
    "IBMJDK": "IBM 1.8 64-bit (on Ubuntu only)",
]

poijobs.each { poijob ->
    
    def jdks = poijob.jdks ?: defaultJdks
    def trigger = poijob.trigger ?: defaultTrigger
    def email = poijob.email ?: defaultEmail

    jdks.each { jdkKey ->
        job(poijob.name) {
            if (poijob.disabled) {
                disabled()
            }
            
            def defaultDesc = '''
<img src="http://poi.apache.org/resources/images/project-logo.jpg" />
<p>
Apache POI - the Java API for Microsoft Documents
</p>
<p>
  <b>This is an automatically generated Job Config, do not edit it here!
    Instead change the Jenkins Job DSL at <a href="http://svn.apache.org/repos/asf/poi/trunk/jenkins">http://svn.apache.org/repos/asf/poi/trunk/jenkins</a>,
    see <a href="https://github.com/jenkinsci/job-dsl-plugin/wiki">https://github.com/jenkinsci/job-dsl-plugin/wiki</a>
    for more details about the DSL.</b>
</p>'''

            description( defaultDesc + 
(poijob.apicheck ? 
'''
<p>
    <b><a href="https://analysis.apache.org/dashboard/index/221489" target="_blank">Sonar reports</a></b> -
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
    <b><a href="https://analysis.apache.org/dashboard/index/221489" target="_blank">Sonar reports</a></b> -
    <b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>
</p>
'''))
            logRotator {
                numToKeep(5)
                artifactNumToKeep(1)
            }
            label('ubuntu&&!cloud-slave' + (poijob.slaveAdd ?: ''))
            environmentVariables {
                env('LANG', 'en_US.UTF-8')
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
                        goals('package')
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
            } else {
                steps {
                    shell(shellcmds)
                    // For Jobs that should still have the default set of publishers we can configure different steps here
                    if(poijob.gradle) {
                        // Gradle will not run any tests if the code is up-to-date, therefore manually mark the files as updated
                        shell("touch --no-create build/*/build/test-results/test/TEST-*.xml")
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
                            targets(['-Dscratchpad.ignore=true', 'jacocotask', 'test-main', 'test-ooxml', 'test-excelant', 'test-ooxml-lite', 'testcoveragereport'] + (poijob.properties ?: []))
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
                    archiveJunit('build/ooxml-test-results/*.xml,build/scratchpad-test-results/*.xml,build/test-results/*.xml,build/excelant-test-results/*.xml,build/integration-test-results/*.xml,build/*/build/test-results/test/TEST-*.xml') {
                        testDataPublishers {
                            publishTestStabilityData()
                        }
                    }
                    jacocoCodeCoverage {
                        classPattern('build/classes,build/examples-classes,build/excelant-classes,build/ooxml-classes,build/scratchpad-classes,build/*/build/classes')
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
}
