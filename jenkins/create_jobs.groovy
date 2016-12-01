// You can use http://job-dsl.herokuapp.com/ to validate the code before checkin
// 

// Missing configs:
//
// POI-JDK-IBM: Disabled, did not find the JDK on any of the slaves, need to check this again later

/* Missing configs:
Erfolgreich	20%	Build planen für POI-API-Check
Erfolgreich	100%	Build planen für POI-Gradle
Erfolgreich	100%	Build planen für POI-Maven
Erfolgreich	100%	Build planen für POI-regenerate-javadoc
*/

def poijobs = [
    [
        name: 'POI-DSL-1.6',
        jdks: ["1.6"]
    ],
    [
        name: 'POI-DSL-1.8',
        jdks: ["1.8"],
        trigger: 'H */12 * * *'
    ],
    [
        name: 'POI-DSL-OpenJDK',
        jdks: ["OpenJDK"],
        trigger: 'H */12 * * *'
    ],
    /* Properties do not work?!
    [
        name: 'POI-DSL-1.9',
        jdks: ["1.9"],
        trigger: '# only run this once per week on Sundays\n' +
			'H H * * 0',
		properties: 'maxpermsize=-Dthis.is.a.dummy=true\n' +
			'java9addmods=-addmods\n' +
			'java9addmodsvalue=java.xml.bind\n' +
			'java.locale.providers=JRE,CLDR',
        email: 'centic@apache.org'
    ],*/
    /* Properties do not work?!
    [
        name: 'POI-DSL-old-Xerces',
        jdks: ["1.9"],
        trigger: '# only run this once per week on Sundays\n' +
			'H H * * 0',
		shell: 'mkdir -p compile-lib && test -f compile-lib/xercesImpl-2.6.1.jar || wget -O compile-lib/xercesImpl-2.6.1.jar http://repo1.maven.org/maven2/xerces/xercesImpl/2.6.1/xercesImpl-2.6.1.jar\n',
		properties: '# this triggers using Xerces as XML Parser and previously showed some exception that can occur\n' +
			'additionaljar=compile-lib/xercesImpl-2.6.1.jar'
    ],*/
    /* Not finished yet
    [
		name: 'POI-DSL-no-scratchpad',
        trigger: '# only run this once per week on Sundays\n' +
			'H H * * 0',
	],*/
]

def svnBase = "https://svn.apache.org/repos/asf/poi/trunk"
def defaultJdks = ["1.6"]
def defaultTrigger = 'H/15 * * * *'
def defaultEmail = 'dev@poi.apache.org'

def jdkMapping = [
    "1.6": "JDK 1.6 (latest)",
    "1.7": "JDK 1.7 (latest)",
    "1.8": "JDK 1.8 (latest)",
    "1.9": "JDK 9 b142 (early access build) with project Jigsaw",
    "OpenJDK": "OpenJDK 6 (on Ubuntu only)",
]

poijobs.each { poijob ->
	
	def jdks = poijob.jdks ?: defaultJdks
	def trigger = poijob.trigger ?: defaultTrigger
	def email = poijob.email ?: defaultEmail

	jdks.each { jdkKey ->
		job(poijob.name) {
			// for now we create the jobs in disabled state so they do not run for now
			disabled()
			
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
			steps {
				shell('# show which files are currently modified in the working copy\n' +
		'svn status\n' +
		'\n' +
		'echo $JAVA_HOME\n' +
		'ls -al $JAVA_HOME\n' +
		'\n' +
		(poijob.shell ?: '') + '\n' +
		'# ignore any error message\n' +
		'exit 0\n')
				ant {
					targets(['clean', 'jenkins'])
					prop('coverage.enabled', true)
					//properties(poijob.properties ?: '')
					antInstallation('Ant (latest)')
				}
				ant {
					buildFile('src/integrationtest/build.xml')
					//properties(poijob.properties ?: '')
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
	}
}
