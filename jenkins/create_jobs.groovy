// You can use http://job-dsl.herokuapp.com/ to validate the code before checkin
// 
def svnBase = "https://svn.apache.org/repos/asf/poi/trunk"
job('POI-DSL-Test') {
	description('<img src="http://poi.apache.org/resources/images/project-logo.jpg" />\n' +
'<p>\n' +
'Apache POI - the Java API for Microsoft Documents\n' +
'</p>\n' +
'<p>\n' +
'  <b>This is an automatically generated Job Config, do not edit it here!\n' +
'    Instead change the Jenkins Job DSL at <a href="http://svn.apache.org/repos/asf/poi/trunk/jenkins">http://svn.apache.org/repos/asf/poi/trunk/jenkins</a>,\n' +
'    see <a href="https://github.com/jenkinsci/job-dsl-plugin/wiki">https://github.com/jenkinsci/job-dsl-plugin/wiki</a>\n' +
'    for more details about the DSL.</b>\n' +
'</p>\n' +
'<p>\n' +
'    <b><a href="lastSuccessfulBuild/findbugsResult/" target="_blank">Findbugs report of latest build</a></b> -\n' +
'    <b><a href="https://analysis.apache.org/dashboard/index/221489" target="_blank">Sonar reports</a></b> -\n' +
'    <b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>\n' +
'</p>\n')
	logRotator {
        numToKeep(5)
        artifactNumToKeep(1)
    }
	label('ubuntu&&!cloud-slave')
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
	jdk('JDK 1.6 (latest)')
    scm {
        svn(svnBase) { svnNode ->
                    svnNode / browser(class: 'hudson.scm.browsers.ViewSVN') /
                        url << 'http://svn.apache.org/viewcvs.cgi/?root=Apache-SVN'
                }
    }
    triggers {
        scm('H/15 * * * *')
    }
    steps {
		shell('# show which files are currently modified in the working copy\n' +
'svn status\n' +
'\n' +
'# ignore any error message\n' +
'exit 0')
        ant {
            targets(['clean', 'jenkins'])
            prop('coverage.enabled', true)
            antInstallation('Ant (latest)')
        }
        ant {
            buildFile('src/integrationtest/build.xml')
            antInstallation('Ant (latest)')
        }
    }
    publishers {
		findbugs('build/findbugs.xml', false) {
            healthLimits(3, 20)
            thresholdLimit('low')
            defaultEncoding('UTF-8')
        }
        archiveArtifacts('build/dist/*.tar.gz,build/findbugs.html,build/coverage/**,build/integration-test-results/**,ooxml-lib/**')
        warnings(['Java Compiler (javac)', 'JavaDoc Tool'], null) {
            resolveRelativePaths()
        }
        archiveJunit('build/ooxml-test-results/*.xml,build/scratchpad-test-results/*.xml,build/test-results/*.xml,build/excelant-test-results/*.xml,build/integration-test-results/*.xml') {
            testDataPublishers {
                publishTestStabilityData()
            }
        }
        jacocoCodeCoverage {
            classPattern('build/classes,build/examples-classes,build/excelant-classes,build/ooxml-classes,build/scratchpad-classes')
            execPattern('build/*.exec')
            sourcePattern('src/java,src/excelant/java,src/ooxml/java,src/scratchpad/src')
            exclusionPattern('com/microsoft/**,org/openxmlformats/**,org/etsi/**,org/w3/**,schemaorg*/**,schemasMicrosoft*/**,org/apache/poi/hdf/model/hdftypes/definitions/*.class,org/apache/poi/hwpf/model/types/*.class,org/apache/poi/hssf/usermodel/DummyGraphics2d.class,org/apache/poi/sl/draw/binding/*.class')
        }
		configure { project ->
			project / publishers << 'hudson.plugins.cigame.GamePublisher' {}
		}
        mailer('dev@poi.apache.org', false, false)
    }
}
