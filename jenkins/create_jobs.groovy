// You can use http://job-dsl.herokuapp.com/ to validate the code before checkin
// 
def svnBase = "https://svn.apache.org/repos/asf/poi/trunk"
job('POI-DSL-Test') {
	description('<img src="http://poi.apache.org/resources/images/project-logo.jpg" />\n' +
'<p>\n' +
'Apache POI - the Java API for Microsoft Documents\n' +
'</p>\n' +
'<p>\n' +
'    <b><a href="lastSuccessfulBuild/findbugsResult/" target="_blank">Findbugs report of latest build</a></b> -\n' +
'    <b><a href="https://analysis.apache.org/dashboard/index/221489" target="_blank">Sonar reports</a></b> -\n' +
'    <b><a href="lastSuccessfulBuild/artifact/build/coverage/index.html" target="_blank">Coverage of latest build</a></b>\n' +
'</p>\n')
	logRotator(numToKeep = 5, artifactNumToKeep = 1)
	label('ubuntu&&!cloud-slave')
	jdk('JDK 1.6 (latest')
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
            prop('logging', 'info')
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
        archiveArtifacts('build/test-output/**/*.html')
        archiveJunit('**/target/surefire-reports/*.xml')
        warnings(['Java Compiler (javac)'], ['Java Compiler (javac)': '**/*.log']) {
            excludePattern('**/test**')
            resolveRelativePaths()
        }
    }
}
