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
}

/*
  <publishers>
    <hudson.plugins.findbugs.FindBugsPublisher plugin="findbugs@4.65">
      <healthy></healthy>
      <unHealthy></unHealthy>
      <thresholdLimit>low</thresholdLimit>
      <pluginName>[FINDBUGS] </pluginName>
      <defaultEncoding></defaultEncoding>
      <canRunOnFailed>false</canRunOnFailed>
      <usePreviousBuildAsReference>false</usePreviousBuildAsReference>
      <useStableBuildAsReference>false</useStableBuildAsReference>
      <useDeltaValues>false</useDeltaValues>
      <thresholds plugin="analysis-core@1.79">
        <unstableTotalAll></unstableTotalAll>
        <unstableTotalHigh></unstableTotalHigh>
        <unstableTotalNormal></unstableTotalNormal>
        <unstableTotalLow></unstableTotalLow>
        <unstableNewAll></unstableNewAll>
        <unstableNewHigh></unstableNewHigh>
        <unstableNewNormal></unstableNewNormal>
        <unstableNewLow></unstableNewLow>
        <failedTotalAll></failedTotalAll>
        <failedTotalHigh></failedTotalHigh>
        <failedTotalNormal></failedTotalNormal>
        <failedTotalLow></failedTotalLow>
        <failedNewAll></failedNewAll>
        <failedNewHigh></failedNewHigh>
        <failedNewNormal></failedNewNormal>
        <failedNewLow></failedNewLow>
      </thresholds>
      <shouldDetectModules>false</shouldDetectModules>
      <dontComputeNew>true</dontComputeNew>
      <doNotResolveRelativePaths>false</doNotResolveRelativePaths>
      <pattern>build/findbugs.xml</pattern>
      <isRankActivated>false</isRankActivated>
      <excludePattern></excludePattern>
      <includePattern></includePattern>
    </hudson.plugins.findbugs.FindBugsPublisher>
    <hudson.plugins.warnings.WarningsPublisher plugin="warnings@4.56">
      <healthy></healthy>
      <unHealthy></unHealthy>
      <thresholdLimit>low</thresholdLimit>
      <pluginName>[WARNINGS] </pluginName>
      <defaultEncoding></defaultEncoding>
      <canRunOnFailed>false</canRunOnFailed>
      <usePreviousBuildAsReference>false</usePreviousBuildAsReference>
      <useStableBuildAsReference>false</useStableBuildAsReference>
      <useDeltaValues>false</useDeltaValues>
      <thresholds plugin="analysis-core@1.79">
        <unstableTotalAll></unstableTotalAll>
        <unstableTotalHigh></unstableTotalHigh>
        <unstableTotalNormal></unstableTotalNormal>
        <unstableTotalLow></unstableTotalLow>
        <unstableNewAll></unstableNewAll>
        <unstableNewHigh></unstableNewHigh>
        <unstableNewNormal></unstableNewNormal>
        <unstableNewLow></unstableNewLow>
        <failedTotalAll></failedTotalAll>
        <failedTotalHigh></failedTotalHigh>
        <failedTotalNormal></failedTotalNormal>
        <failedTotalLow></failedTotalLow>
        <failedNewAll></failedNewAll>
        <failedNewHigh></failedNewHigh>
        <failedNewNormal></failedNewNormal>
        <failedNewLow></failedNewLow>
      </thresholds>
      <shouldDetectModules>false</shouldDetectModules>
      <dontComputeNew>true</dontComputeNew>
      <doNotResolveRelativePaths>false</doNotResolveRelativePaths>
      <includePattern></includePattern>
      <excludePattern></excludePattern>
      <parserConfigurations/>
      <consoleParsers>
        <hudson.plugins.warnings.ConsoleParser>
          <parserName>Java Compiler (javac)</parserName>
        </hudson.plugins.warnings.ConsoleParser>
        <hudson.plugins.warnings.ConsoleParser>
          <parserName>JavaDoc Tool</parserName>
        </hudson.plugins.warnings.ConsoleParser>
      </consoleParsers>
    </hudson.plugins.warnings.WarningsPublisher>
    <hudson.tasks.ArtifactArchiver>
      <artifacts>build/dist/*.tar.gz,build/findbugs.html,build/coverage/**,build/integration-test-results/**,ooxml-lib/**</artifacts>
      <allowEmptyArchive>false</allowEmptyArchive>
      <onlyIfSuccessful>false</onlyIfSuccessful>
      <fingerprint>false</fingerprint>
      <defaultExcludes>true</defaultExcludes>
      <caseSensitive>true</caseSensitive>
    </hudson.tasks.ArtifactArchiver>
    <hudson.tasks.junit.JUnitResultArchiver plugin="junit@1.18">
      <testResults>build/ooxml-test-results/*.xml,build/scratchpad-test-results/*.xml,build/test-results/*.xml,build/excelant-test-results/*.xml,build/integration-test-results/*.xml</testResults>
      <keepLongStdio>false</keepLongStdio>
      <testDataPublishers>
        <de.esailors.jenkins.teststability.StabilityTestDataPublisher plugin="test-stability@1.0"/>
      </testDataPublishers>
      <healthScaleFactor>1.0</healthScaleFactor>
      <allowEmptyResults>false</allowEmptyResults>
    </hudson.tasks.junit.JUnitResultArchiver>
    <hudson.plugins.jacoco.JacocoPublisher plugin="jacoco@2.0.1">
      <healthReports>
        <minClass>0</minClass>
        <maxClass>0</maxClass>
        <minMethod>0</minMethod>
        <maxMethod>0</maxMethod>
        <minLine>0</minLine>
        <maxLine>0</maxLine>
        <minBranch>0</minBranch>
        <maxBranch>0</maxBranch>
        <minInstruction>0</minInstruction>
        <maxInstruction>0</maxInstruction>
        <minComplexity>0</minComplexity>
        <maxComplexity>0</maxComplexity>
      </healthReports>
      <execPattern>build/*.exec</execPattern>
      <classPattern>build/classes,build/examples-classes,build/excelant-classes,build/ooxml-classes,build/scratchpad-classes</classPattern>
      <sourcePattern>src/java,src/excelant/java,src/ooxml/java,src/scratchpad/src</sourcePattern>
      <inclusionPattern></inclusionPattern>
      <exclusionPattern>com/microsoft/**,org/openxmlformats/**,org/etsi/**,org/w3/**,schemaorg*/**,schemasMicrosoft*/**,org/apache/poi/hdf/model/hdftypes/definitions/*.class,org/apache/poi/hwpf/model/types/*.class,org/apache/poi/hssf/usermodel/DummyGraphics2d.class,org/apache/poi/sl/draw/binding/*.class</exclusionPattern>
      <minimumInstructionCoverage>0</minimumInstructionCoverage>
      <minimumBranchCoverage>0</minimumBranchCoverage>
      <minimumComplexityCoverage>0</minimumComplexityCoverage>
      <minimumLineCoverage>0</minimumLineCoverage>
      <minimumMethodCoverage>0</minimumMethodCoverage>
      <minimumClassCoverage>0</minimumClassCoverage>
      <maximumInstructionCoverage>0</maximumInstructionCoverage>
      <maximumBranchCoverage>0</maximumBranchCoverage>
      <maximumComplexityCoverage>0</maximumComplexityCoverage>
      <maximumLineCoverage>0</maximumLineCoverage>
      <maximumMethodCoverage>0</maximumMethodCoverage>
      <maximumClassCoverage>0</maximumClassCoverage>
      <changeBuildStatus>false</changeBuildStatus>
    </hudson.plugins.jacoco.JacocoPublisher>
    <hudson.plugins.cigame.GamePublisher plugin="ci-game@1.25"/>
    <hudson.tasks.Mailer plugin="mailer@1.17">
      <recipients>dev@poi.apache.org</recipients>
      <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
      <sendToIndividuals>false</sendToIndividuals>
    </hudson.tasks.Mailer>
  </publishers>
  <buildWrappers>
    <EnvInjectBuildWrapper plugin="envinject@1.92.1">
      <info>
        <propertiesContent>LANG=en_US.UTF-8</propertiesContent>
        <loadFilesFromMaster>false</loadFilesFromMaster>
      </info>
    </EnvInjectBuildWrapper>
    <hudson.plugins.build__timeout.BuildTimeoutWrapper plugin="build-timeout@1.17.1">
      <strategy class="hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy">
        <timeoutMinutes>180</timeoutMinutes>
      </strategy>
      <operationList>
        <hudson.plugins.build__timeout.operations.AbortOperation/>
      </operationList>
    </hudson.plugins.build__timeout.BuildTimeoutWrapper>
  </buildWrappers>
</project>
*/
