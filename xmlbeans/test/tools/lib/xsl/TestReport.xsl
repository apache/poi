<?xml version="1.0"?>
<!--
/*   Copyright 2004 The Apache Software Foundation
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*  limitations under the License.
*/
 -->
 <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java" xmlns:weblogic="weblogic">
	<!-- global param -->
	<xsl:param name="instrument">0</xsl:param>
	<xsl:param name="showFeatureInfo">0</xsl:param>
	<xsl:param name="filebasename">myrun</xsl:param>
	<xsl:param name="feature">NONE</xsl:param>
	<xsl:param name="gtlfFileName">myrun.gtlf.xml</xsl:param>
	<xsl:param name="cssFile">junit.css</xsl:param>
	<xsl:param name="showSuccessInErrorSummary">0</xsl:param>
	<xsl:param name="reportSectionOrder">result-focus</xsl:param>
	<xsl:param name="showTheFilters">1</xsl:param>

  <!-- controls the sections that should be part of the diff report
  
  The user can specify a comma separated list of sections they area interested
  in

  for e.g. result-Summary,setup

  The sections are

  report-Summary
  report-summary-error-list
  result-Summary
  result-Detail
  error-Summary
  error-Detail
  setup
  all - all the sections are displayed

  The report summary is always displayed.

  -->
  <xsl:param name="sections">all</xsl:param>


  <!-- controls the display of navigation tools in the diff report
            controls the navigation tools that will be available
             in the report. You can specify a comma separated list 
             of the navigation tools

             The navigation tools are:

             all      - all the tools are displayed
             menu     - menu of the report
             toolbar  - toolbar of the report
             top      - top link displayed in different report
                        sections

  -->
  <xsl:param name="navigationTools">all</xsl:param>
  <xsl:param name="reportSummaryErrorListCount">5</xsl:param>
  <xsl:param name="reportSummaryErrorListHyperlinkPrefix">NA</xsl:param>

  <xsl:variable name="allTool">all</xsl:variable>
  <xsl:variable name="menuNavigationTool">menu</xsl:variable>
  <xsl:variable name="toolbarNavigationTool">toolbar</xsl:variable>
  <xsl:variable name="topNavigationTool">top</xsl:variable>

  <xsl:variable name="showMenuNavigationTool"
                select="contains($navigationTools,$allTool) or
                        contains($navigationTools,$menuNavigationTool)"/>
  <xsl:variable name="showToolbarNavigationTool"
                select="contains($navigationTools,$allTool) or
                        contains($navigationTools,$toolbarNavigationTool)"/>
  <xsl:variable name="showTopNavigationTool"
                select="contains($navigationTools,$allTool) or
                        contains($navigationTools,$topNavigationTool)"/>


  <xsl:variable name="reportSummarySection">report-summary</xsl:variable>
  <xsl:variable 
    name="reportSummaryErrorListSubSection">report-summary-error-list</xsl:variable>
  <xsl:variable name="resultSummarySection">result-summary</xsl:variable>
  <xsl:variable name="resultDetailSection">result-detail</xsl:variable>
  <xsl:variable name="errorSummarySection">error-summary</xsl:variable>
  <xsl:variable name="errorDetailSection">error-detail</xsl:variable>
  <xsl:variable name="setupSection">setup</xsl:variable>
  <xsl:variable name="allSection">all</xsl:variable>

  <xsl:variable name="showReportSummarySection"
                select="contains($sections,$allSection) or
                        contains($sections,$reportSummarySection)"/>
  <xsl:variable name="showReportSummaryErrorListSubSection"
                select="contains($sections,$allSection) or
                        contains($sections,$reportSummaryErrorListSubSection)"/>
  <xsl:variable name="showResultSummarySection"
                select="contains($sections,$allSection) or
                        contains($sections,$resultSummarySection)"/>
  <xsl:variable name="showResultDetailSection"
                select="contains($sections,$allSection) or
                        contains($sections,$resultDetailSection)"/>
  <xsl:variable name="showErrorSummarySection"
                select="contains($sections,$allSection) or
                        contains($sections,$errorSummarySection)"/>
  <xsl:variable name="showErrorDetailSection"
                select="contains($sections,$allSection) or
                        contains($sections,$errorDetailSection)"/>
  <xsl:variable name="showSetupSection"
                select="contains($sections,$allSection) or
                        contains($sections,$setupSection)"/>
  <xsl:output method="html"/>
	<!--

  Structure of the xml
  =====================

    (root)
    test-log (1) 
     |
      environment (1) 
     |    |
     |     env-attribute (1) { name, value }
     |
      header-info (1) { execaccount, execdate, checksum, harnesstype,
     |                  importinfo, testruntype }
     |
      test-result (1.*) { exectime, result, isdone, logicalname, duration}
       |
        test-case (1) {testcsename, testunit, testpath}
       |
        execution-output (1)
       | 
        output-details (1)
       |
        test-replication(0 - 1) 
           |
             info
           |
            command-line
               |
                unix
               |
                win

  -->
	<xsl:template match="/test-log">
		<html>
			<head>
                <!--<link rel="stylesheet" type="text/css" href="junit.css"/>-->
				<xsl:call-template name="javascripts"/>
			</head>
			<body>
				<!-- FILTER SCRIPTS -->
				<xsl:call-template name="filterScript"/>
				<!-- TOOLTIP SCRIPTS -->
				<xsl:call-template name="tooltipScript"/>
				<!-- MENU -->
				<xsl:if test="$showMenuNavigationTool">
					<xsl:call-template name="sidemenu"/>
				</xsl:if>
				<!-- HEADER -->
				<xsl:if test="$showToolbarNavigationTool">
					<xsl:call-template name="header"/>
				</xsl:if>
				<!-- RESULT SUMMARY -->
				<xsl:if test="$showResultSummarySection">
					<xsl:call-template name="summary"/>
				</xsl:if>
				<!-- REPORT SUMMARY -->
				<xsl:if test="$showReportSummarySection">
					<xsl:call-template name="reportSummary"/>
				</xsl:if>
				<!-- display the report sections in the order specified -->
				<xsl:choose>
					<xsl:when test="contains(string($reportSectionOrder),'error-focus')">
						<!-- ERROR DETAIL -->
						<xsl:if test="$showErrorDetailSection">
							<xsl:call-template name="testResultWithErrors"/>
						</xsl:if>
						<!-- ERROR SUMMARY -->
						<xsl:if test="$showErrorSummarySection">
							<xsl:call-template name="testResultErrorSummary"/>
						</xsl:if>
						<!-- RESULT DETAIL -->
						<xsl:if test="$showResultDetailSection">
							<xsl:call-template name="summaryByTestUnit"/>
						</xsl:if>
						<!-- SETUP -->
						<xsl:if test="$showSetupSection">
							<xsl:call-template name="metaInfo"/>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<!-- RESULT DETAIL -->
						<xsl:if test="$showResultDetailSection">
							<xsl:call-template name="summaryByTestUnit"/>
						</xsl:if>
						<!-- ERROR SUMMARY -->
						<xsl:if test="$showErrorSummarySection">
							<xsl:call-template name="testResultErrorSummary"/>
						</xsl:if>
						<!-- SETUP -->
						<xsl:if test="$showSetupSection">
							<xsl:call-template name="metaInfo"/>
						</xsl:if>
						<!-- ERROR DETAIL -->
						<xsl:if test="$showErrorDetailSection">
							<xsl:call-template name="testResultWithErrors"/>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</body>
		</html>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        TEST SETUP                                 -->
	<!-- ================================================================ -->
	<xsl:template name="metaInfo">
		<hr width="100%" size="2"/>
		<a name="metaInfo"/>
		<center>
			<h2> Setup <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>]</xsl:if>
			</h2>
		</center>
		<table id="setupTable" class="details" border="1" cellspacing="0">
			<tr>
				<th class="name"> Name </th>
				<th class="value"> Value </th>
			</tr>
			<tr>
				<td> Test type </td>
				<td>
					<xsl:value-of select="@testtype"/>
				</td>
			</tr>
			<tr>
				<td> Release </td>
				<td>
					<xsl:value-of select="@release"/>
				</td>
			</tr>
			<tr>
				<td> Branch </td>
				<td>
					<xsl:value-of select="@branch"/>
				</td>
			</tr>
			<tr>
				<td> Hostname </td>
				<td>
					<xsl:value-of select="@hostname"/>
				</td>
			</tr>
			<tr>
				<td> Run id </td>
				<td>
					<xsl:value-of select="@runid"/>
				</td>
			</tr>
			<tr>
				<th class="name"> Environment </th>
				<th class="name"> -- </th>
			</tr>
			<tr>
				<td> JVM Name </td>
				<td>
					<xsl:value-of select="environment/env-attribute[@name = 'JVM_NAME']/@value"/>
				</td>
			</tr>
			<tr>
				<td> OS </td>
				<td>
					<xsl:value-of select="environment/env-attribute[@name = 'OS']/@value"/>
				</td>
			</tr>
			<tr>
				<td> JVM version </td>
				<td>
					<xsl:value-of select="environment/env-attribute[@name = 'JVM_VERSION']/@value"/>
				</td>
			</tr>
			<tr>
				<td> Native IO </td>
				<td>
					<xsl:value-of select="environment/env-attribute[@name = 'NativeIO']/@value"/>
				</td>
			</tr>
			<tr>
				<th class="name"> Header Info: </th>
				<th class="name"> -- </th>
			</tr>
			<tr>
				<td> Exec account </td>
				<td>
					<xsl:value-of select="header-info/@execaccount"/>
				</td>
			</tr>
			<tr>
				<td> Exec date </td>
				<td>
					<xsl:value-of select="header-info/@execdate"/>
				</td>
			</tr>
			<tr>
				<td> Checksum </td>
				<td>
					<xsl:value-of select="header-info/@checksum"/>
				</td>
			</tr>
			<tr>
				<td> Result count </td>
				<td>
					<xsl:value-of select="header-info/@resultcount"/>
				</td>
			</tr>
			<tr>
				<td> Harness type </td>
				<td>
					<xsl:value-of select="header-info/@harnesstype"/>
				</td>
			</tr>
			<tr>
				<td> Test run type </td>
				<td>
					<xsl:value-of select="header-info/@testruntype"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                       ERROR SUMMARY                               -->
	<!-- ================================================================ -->
	<xsl:template name="testResultErrorSummary">
		<hr width="100%" size="2"/>
		<a name="testResultErrorSummary"/>
		<center>
			<h2> Error Summary <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>]</xsl:if>
			</h2>
		</center>
		<xsl:variable name="errorTestResults" select="/test-log/test-result[@result = 'FAILURE' or
        @result = 'SKIP' or @result='TIMEOUT' or @result='ABORT' or
        @result = 'ABANDONED' or @result = 'SCRATCH']"/>
		<xsl:if test="$showTheFilters = 1">
			<!-- +++++++++++++++++++++++++++++++++ Filter Table ++++++++++++++++ -->
			<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
     border-top: none; border-right: none; border-bottom: none">
				<!-- filter form elements -->
				<tr>
					<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
            border-top: none; border-right: none; border-bottom: none">
						<form name="errorSummaryFilter" onsubmit="TF_filterTable(document.getElementById
                 ('errorSummaryTable'),document.errorSummaryFilter);
                 return false" onreset="_TF_showAll(document.getElementById
                 ('errorSummaryTable'))">
							<!-- form element table -->
							<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
								<tr>
									<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
										<!-- filter reset -->
										<input type="button" onclick="document.errorSummaryFilter.reset()" value="Show All"/>
										<!-- Result select -->
										<xsl:text>&#160;</xsl:text>
										<b>Result: </b>
										<select TF_colKey="errorSummary_Result" TF_searchType="full" onChange="TF_filterTable(document.getElementById
                      ('errorSummaryTable'), document.errorSummaryFilter)">
											<option TF_not_used="TRUE">-</option>
											<xsl:for-each select="$errorTestResults[ generate-id() = 
                   generate-id(key('errorTestResults',@result)[1])]">
												<xsl:sort select="@result"/>
												<option>
													<xsl:attribute name="value"><xsl:value-of select="@result"/></xsl:attribute>
													<xsl:value-of select="@result"/>
												</option>
											</xsl:for-each>
											<xsl:if test="$showSuccessInErrorSummary = 1">
												<option value="SUCCESS">SUCCESS</option>
											</xsl:if>
										</select>
										<!-- testunit select -->
										<xsl:text>&#160;</xsl:text>
										<b>Testunit: </b>
										<select TF_colKey="errorSummary_Name" TF_searchType="full" onChange="TF_filterTable(document.getElementById
                      ('errorSummaryTable'), document.errorSummaryFilter)">
											<option TF_not_used="TRUE">-</option>
											<xsl:choose>
												<xsl:when test="$showSuccessInErrorSummary = 1">
													<xsl:for-each select="$testResults/test-case[
                        generate-id() = 
                        generate-id(key('testUnits', @testunit)[1])]">
														<xsl:sort select="@testunit"/>
														<option>
															<xsl:attribute name="value"><xsl:value-of select="@testunit"/></xsl:attribute>
															<xsl:value-of select="@testunit"/>
														</option>
													</xsl:for-each>
												</xsl:when>
												<xsl:otherwise>
													<xsl:for-each select="$errorTestResults/test-case[
                        generate-id() = 
                        generate-id(key('errorTestUnits', @testunit)[1])]">
														<xsl:sort select="@testunit"/>
														<option>
															<xsl:attribute name="value"><xsl:value-of select="@testunit"/></xsl:attribute>
															<xsl:value-of select="@testunit"/>
														</option>
													</xsl:for-each>
												</xsl:otherwise>
											</xsl:choose>
										</select>
										<!-- testunit search -->
										<input type="text" TF_colKey="errorSummary_Name" TF_searchType="substring" onkeyup="TF_filterTable(document.getElementById
                       ('errorSummaryTable'), document.errorSummaryFilter)">
              </input>
										<p/>
										<xsl:text>&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:text>
										<xsl:text>&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:text>
										<xsl:text>&#160;&#160;&#160;&#160;</xsl:text>
										<!-- testcase search -->
										<xsl:text>&#160;&#160;</xsl:text>
										<b>Testcase search: </b>
										<input type="text" TF_colKey="errorSummary_LogicalName" TF_searchType="substring" onkeyup="TF_filterTable(document.getElementById
                       ('errorSummaryTable'), document.errorSummaryFilter)">
              </input>
									</td>
									<!-- filter elements -->
								</tr>
								<!-- filter elements row -->
							</table>
							<!-- filter element table -->
						</form>
						<!-- filter form -->
					</td>
				</tr>
			</table>
			<!-- filter table -->
		</xsl:if>
		<!-- iterate all the test units that have an error -->
		<table id="errorSummaryTable" class="details" border="1" cellspacing="0">
			<tr>
				<th class="name"> Time </th>
				<th class="name"> Testunit</th>
				<th class="name"> Result </th>
				<th class="name"> Testcase name</th>
			</tr>
			<xsl:choose>
				<xsl:when test="$showSuccessInErrorSummary = 1">
					<xsl:for-each select="$testResults">
						<tr>
							<td>
								<xsl:value-of select="@exectime"/>
							</td>
							<td TF_colKey="errorSummary_Name">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="test-case/@testunit"/></xsl:attribute>
								<xsl:value-of select="test-case/@testunit"/>
							</td>
							<td TF_colKey="errorSummary_Result">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="@result"/></xsl:attribute>
								<xsl:value-of select="@result"/>
							</td>
							<td TF_colKey="errorSummary_LogicalName">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="@logicalname"/></xsl:attribute>
								<a>
									<xsl:if test="$showErrorDetailSection">
										<xsl:if test="@result != 'SUCCESS'">
											<xsl:attribute name="href">#next_<xsl:value-of select="concat(test-case/@testpath, 
                            @exectime, @logicalname)"/></xsl:attribute>
										</xsl:if>
									</xsl:if>
									<xsl:value-of select="@logicalname"/>
								</a>
							</td>
						</tr>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="$errorTestResults">
						<tr>
							<td>
								<xsl:value-of select="@exectime"/>
							</td>
							<td TF_colKey="errorSummary_Name">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="test-case/@testunit"/></xsl:attribute>
								<xsl:value-of select="test-case/@testunit"/>
							</td>
							<td TF_colKey="errorSummary_Result">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="@result"/></xsl:attribute>
								<xsl:value-of select="@result"/>
							</td>
							<td TF_colKey="errorSummary_LogicalName">
								<xsl:attribute name="TF_colValue"><xsl:value-of select="@logicalname"/></xsl:attribute>
								<a>
									<xsl:if test="$showErrorDetailSection">
										<xsl:attribute name="href">#next_<xsl:value-of select="concat(test-case/@testpath, 
                        @exectime, @logicalname)"/></xsl:attribute>
									</xsl:if>
									<xsl:value-of select="@logicalname"/>
								</a>
							</td>
						</tr>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</table>
	</xsl:template>
	<!-- Test results that were not success -->
	<xsl:key name="errorTestUnits" match="/test-log/test-result[@result = 'FAILURE' or
        @result = 'SKIP' or @result='TIMEOUT' or @result='ABORT' or
        @result = 'ABANDONED' or @result = 'SCRATCH']/test-case" use="@testunit"/>
	<xsl:key name="errorTestResults" match="/test-log/test-result[@result = 'FAILURE' or
        @result = 'SKIP' or @result='TIMEOUT' or @result='ABORT' or
        @result = 'ABANDONED' or @result = 'SCRATCH']" use="@result"/>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        ERROR DETAIL                               -->
	<!-- ================================================================ -->
	<xsl:template name="testResultWithErrors">
		<hr width="100%" size="2"/>
		<a name="testResultWithErrors"/>
		<center>
			<h2> Error Detail <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>] </xsl:if>
			</h2>
		</center>
		<!-- iterate all the test units that have an error -->
		<xsl:variable name="errorTestResults" select="/test-log/test-result[@result = 'FAILURE' or
        @result = 'SKIP' or @result='TIMEOUT' or @result='ABORT' or
        @result = 'ABANDONED' or @result = 'SCRATCH']"/>
		<xsl:for-each select="$errorTestResults/test-case[ 
                    generate-id() = 
                    generate-id(key('errorTestUnits', @testunit)[1])]">
			<xsl:variable name="pos1" select="position()"/>
			<xsl:variable name="errorTestUnit" select="@testunit"/>
			<hr width="100%" size="2"/>
			<a>
				<xsl:attribute name="name">testErrors_<xsl:value-of select="@testunit"/></xsl:attribute>
			</a>
      [<a href="#summaryByTestUnit"> Result Detail</a>]
     <xsl:if test="$showTopNavigationTool">      [<a href="#top"> Top </a>] </xsl:if>
			<xsl:if test="$showTheFilters = 1">
				<!-- +++++++++++++++++++++++++++++++++ Filter Table ++++++++++++++++ -->
				<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
     border-top: none; border-right: none; border-bottom: none">
					<xsl:variable name="tf_enableFilter">TF_enableFilter(document.getElementById('errorDetailTable<xsl:value-of select="$pos1"/>'), document.errorDetailFilter<xsl:value-of select="$pos1"/>, this)</xsl:variable>
					<xsl:variable name="tf_filterTable">TF_filterTable(document.getElementById('errorDetailTable<xsl:value-of select="$pos1"/>'), document.errorDetailFilter<xsl:value-of select="$pos1"/>)</xsl:variable>
					<xsl:variable name="tf_showAll">TF_showAll(document.getElementById('errorDetailTable<xsl:value-of select="$pos1"/>'))</xsl:variable>
					<!-- filter form elements -->
					<tr>
						<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
            border-top: none; border-right: none; border-bottom: none">
							<form>
								<xsl:attribute name="name">errorDetailFilter<xsl:value-of select="$pos1"/></xsl:attribute>
								<xsl:attribute name="onsubmit"><xsl:value-of select="$tf_filterTable"/></xsl:attribute>
								<xsl:attribute name="onreset"><xsl:value-of select="$tf_showAll"/></xsl:attribute>
								<!-- form element table -->
								<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
									<tr>
										<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
											<!-- testunit select -->
											<xsl:text>&#160;&#160;</xsl:text>
											<select TF_colKey="errorDetail_Cell" TF_searchType="substring" multiple="multiple" size="3">
												<xsl:attribute name="onchange"><xsl:value-of select="$tf_filterTable"/></xsl:attribute>
												<option TF_not_used="TRUE">-</option>
												<option value="ExecutionOutput">Execution output</option>
												<option value="LogicalTestCaseName">Logical Testcase name</option>
												<option value="TestParameters">Test parameters</option>
												<option value="Result">Result</option>
												<option value="TestPath">Testpath</option>
												<option value="TestUnit">TestUnit</option>
												<option value="ExecutionTime">Execution time</option>
												<xsl:if test="$showFeatureInfo = 1">
													<option value="BugDetective">BugDetective cmds</option>
												</xsl:if>
												<xsl:if test="boolean(test-replication)">
													<option value="Replication">Replication cmds</option>
												</xsl:if>
												<option value="TestCaseName">Testcase name</option>
											</select>
											<!-- testcase search -->
											<xsl:text>&#160;&#160;</xsl:text>
											<b>Search (outupt, param): </b>
											<input type="text" TF_colKey="errorDetail_Cell" TF_searchType="substring">
												<xsl:attribute name="onkeyup"><xsl:value-of select="$tf_filterTable"/></xsl:attribute>
											</input>
										</td>
									</tr>
								</table>
							</form>
						</td>
					</tr>
				</table>
				<!-- filter table -->
			</xsl:if>
			<!-- error detail table -->
			<table border="1" class="details" cellspacing="0">
				<xsl:attribute name="id">errorDetailTable<xsl:value-of select="$pos1"/></xsl:attribute>
				<tr>
					<th class="title" colspan="2">
						<xsl:value-of select="@testunit"/>
						<xsl:if test="$showTopNavigationTool">            
            [<a href="#top"> Top </a>]</xsl:if>
					</th>
				</tr>
				<xsl:for-each select="$errorTestResults[test-case/@testunit = $errorTestUnit]">
					<xsl:variable name="pos" select="position()"/>
					<tr>
						<th class="name"> Name</th>
						<th class="name" width="90%" style="text-align:left">
							<a>
								<xsl:attribute name="name">next_<xsl:value-of select="concat(test-case/@testpath, 
                  @exectime, @logicalname)"/></xsl:attribute>
							</a>
							<!-- Vishal: disabling next and previous as it's a big perofmance hit. 
                  We can't use RTF as we run into a xalan (DTM) bug
                  in jdk 1.4.1

           <xsl:variable name="nextTestResult" 
                         select="following-sibling::*[@result = 'FAILURE' or
                                 @result = 'SKIP' or @result='TIMEOUT' or 
                                 @result='ABORT' or @result = 'ABANDONED' or 
                                 @result = 'SCRATCH'][1]"/>
           <xsl:variable name="previousTestResult" 
                         select="preceding-sibling::*[@result = 'FAILURE' or
                                 @result = 'SKIP' or @result='TIMEOUT' or 
                                 @result='ABORT' or @result = 'ABANDONED' or 
                                 @result = 'SCRATCH'][1]"/>
              <xsl:value-of select="nextTestResult"/>
          [
          <xsl:if  test="$showTopNavigationTool">              
          <a href="#top">Top</a>]</xsl:if>
          [
              <a>
                <xsl:if test="$showErrorDetailSection">
                <xsl:attribute name="href">#next_<xsl:value-of
              select="concat($nextTestResult/test-case/@testpath, 
              $nextTestResult/@exectime, $nextTestResult/@logicalname)"/>
                </xsl:attribute>
                </xsl:if>
            next 
              </a>]
          [
              <a>
                <xsl:if test="$showErrorDetailSection">
                <xsl:attribute name="href">#next_<xsl:value-of
              select="concat($previousTestResult/test-case/@testpath, 
              $previousTestResult/@exectime, 
              $previousTestResult/@logicalname)"/>
                </xsl:attribute>
                </xsl:if>
            Prev. 
              </a>]
    -->
							<!-- turning off next/previous -->
             Value</th>
					</tr>
					<tr>
						<td width="10%">
            TestUnit
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="TestUnit">
							<xsl:value-of select="test-case/@testunit"/>
						</td>
					</tr>
					<tr>
						<td width="10%">
            Result
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="Result">
							<xsl:value-of select="@result"/>
						</td>
					</tr>
					<tr>
						<td width="10%">
            Execution time
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="ExecutionTime">
							<xsl:value-of select="@exectime"/>
						</td>
					</tr>
					<xsl:if test="$showFeatureInfo = 1">
						<tr>
							<td TF_colKey="errorDetail_Cell" TF_colValue="BugDetective" width="10%"> BugDetective cmd's</td>
							<td>
								<table border="1" class="details" cellspacing="0">
									<tr>
										<th width="10%"> Run cmd </th>
										<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="RunCmd">
											<font color="blue">
												<i>ant -f build-Test.xml run -Dfeature=<xsl:value-of select="$feature"/> -DTEST_ARGS="-Dcoconut.filter.test-level=4 -Dcoconut.string=4  -Dcoconut.test-names=<xsl:value-of select="test-case/@testunit"/>.<xsl:value-of select="@logicalname"/>"</i>
											</font>
										</td>
									</tr>
									<tr>
										<th width="10%"> Coverage cmd </th>
										<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="CoverageCmd">
											<font color="blue">
												<i>ant -f build-Test.xml coverage.all -Dfeature=<xsl:value-of select="$feature"/> -DTEST_ARGS="-Dcoconut.filter.test-level=4 -Dcoconut.string=4  -Dcoconut.test-names=<xsl:value-of select="test-case/@testunit"/>.<xsl:value-of select="@logicalname"/>"</i>
											</font>
										</td>
									</tr>
									<tr>
										<th width="10%"> Inspect cmd </th>
										<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="InspectCmd">
											<font color="blue">
												<i>ant -f build-Test.xml inspect.all -Dfeature=<xsl:value-of select="$feature"/> -DTEST_ARGS="-Dcoconut.filter.test-level=4 -Dcoconut.string=4  -Dcoconut.test-names=<xsl:value-of select="test-case/@testunit"/>.<xsl:value-of select="@logicalname"/>"</i>
											</font>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</xsl:if>
					<tr>
						<td width="10%">
            Logical Testcase name
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%">
							<xsl:attribute name="TF_colValue">LogicalTestCaseName<xsl:value-of select="execution-output/output-details"/><xsl:value-of select="test-parameters"/></xsl:attribute>
							<xsl:value-of select="@logicalname"/>
						</td>
					</tr>
					<tr>
						<td width="10%">
            Testcase name
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="TestCaseName">
							<xsl:value-of select="test-case/@testcasename"/>
						</td>
					</tr>
					<tr>
						<td width="10%">
            Test path
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%" TF_colValue="TestPath">
							<xsl:value-of select="test-case/@testpath"/>
						</td>
					</tr>
					<tr>
						<td width="10%">
            execution output
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%">
							<xsl:attribute name="TF_colValue">ExecutionOutput<xsl:value-of select="execution-output/output-details"/></xsl:attribute>
							<font color="red">
								<pre wrap="true">
									<xsl:value-of select="execution-output/output-details"/>
								</pre>
							</font>
						</td>
					</tr>
					<!-- 
          3/7/2004 Vishal - Test replication is a new feature, and we 
                            should display it in the report on if the 
                            data is there in the xml file.
          -->
					<xsl:if test="boolean(test-replication)">
						<tr>
							<td TF_colKey="errorDetail_Cell" TF_colValue="Replication" width="10%"> Replication </td>
							<td>
								<table border="1" class="details" cellspacing="0">
									<tr>
										<th width="10%"> info</th>
										<td>
											<font color="blue">
												<xsl:value-of select="test-replication/info"/>
											</font>
										</td>
									</tr>
									<tr>
										<th width="10%"> unix cmd.</th>
										<td>
											<font color="blue">
												<xsl:value-of select="test-replication/command-line/unix"/>
											</font>
										</td>
									</tr>
									<tr>
										<th width="10%"> win cmd.</th>
										<td>
											<font color="blue">
												<xsl:value-of select="test-replication/command-line/win"/>
											</font>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</xsl:if>
					<tr>
						<td width="10%">
            test parameters
            </td>
						<td TF_colKey="errorDetail_Cell" width="90%">
							<xsl:attribute name="TF_colValue">TestParameters<xsl:value-of select="test-parameters"/></xsl:attribute>
							<font color="darkgreen">
								<pre wrap="true">
									<xsl:value-of select="test-parameters"/>
								</pre>
							</font>
						</td>
					</tr>
					<xsl:variable name="prevTestResult" select="concat(test-case/@testpath, @exectime, @logicalname)"/>
				</xsl:for-each>
			</table>
		</xsl:for-each>
	</xsl:template>
	<xsl:key name="testUnits" match="/test-log/test-result/test-case" use="@testunit"/>
	<xsl:key name="successTestUnits" match="/test-log/test-result[@result = 'SUCCESS']/test-case" use="@testunit"/>
	<xsl:key name="failTestUnits" match="/test-log/test-result[@result = 'FAILURE']/test-case" use="@testunit"/>
	<xsl:key name="skipTestUnits" match="/test-log/test-result[@result = 'SKIP']/test-case" use="@testunit"/>
	<xsl:key name="timeoutTestUnits" match="/test-log/test-result[@result = 'TIMEOUT']/test-case" use="@testunit"/>
	<xsl:key name="abortTestUnits" match="/test-log/test-result[@result = 'ABORT']/test-case" use="@testunit"/>
	<xsl:key name="abandonedTestUnits" match="/test-log/test-result[@result = 'ABANDONED']/test-case" use="@testunit"/>
	<xsl:key name="scratchTestUnits" match="/test-log/test-result[@result = 'SCRATCH']/test-case" use="@testunit"/>
	<!-- Summary of the test run by test unit -->
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        RESULT DETAIL                              -->
	<!-- ================================================================ -->
	<xsl:template name="summaryByTestUnit">
		<hr width="100%" size="2"/>
		<a name="summaryByTestUnit"/>
		<center>
			<h2> Result Detail <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>]</xsl:if>
			</h2>
		</center>
		<xsl:if test="$showTheFilters = 1">
			<!-- +++++++++++++++++++++++++++++++++ Filter Table ++++++++++++++++ -->
			<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
     border-top: none; border-right: none; border-bottom: none">
				<!-- filter form elements -->
				<tr>
					<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
            border-top: none; border-right: none; border-bottom: none">
						<form name="resultSummaryFilter" onsubmit="TF_filterTable(document.getElementById
                 ('resultSummaryTable'),document.resultSummaryFilter);
                 return false" onreset="_TF_showAll(document.getElementById
                 ('resultSummaryTable'))">
							<!-- form element table -->
							<table cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
								<tr>
									<td cellpadding="0" cellspacing="0" border="0" style="background-color: rgb(255, 255, 255);border-left: none;
                border-top: none; border-right: none; border-bottom: none">
										<!-- filter reset -->
										<input type="button" onclick="document.resultSummaryFilter.reset()" value="Show all"/>
										<xsl:text>&#160;</xsl:text>
										<!-- result select -->
										<xsl:text>&#160;</xsl:text>
										<b>Result: </b>
										<select TF_colKey="resultSummary_Error" TF_searchType="substring" onChange="TF_filterTable(document.getElementById
                      ('resultSummaryTable'), document.resultSummaryFilter)">
											<option TF_not_used="TRUE">-</option>
											<option value="ERRORS">Errors</option>
											<option value="ALL_SUCCESS">No Errors</option>
										</select>
										<!-- testunit select -->
										<xsl:text>&#160;</xsl:text>
										<b>Testunit: </b>
										<select TF_colKey="resultSummary_Name" TF_searchType="full" onChange="TF_filterTable(document.getElementById
                      ('resultSummaryTable'), document.resultSummaryFilter)">
											<option TF_not_used="TRUE">-</option>
											<xsl:for-each select="/test-log/test-result/test-case[ 
                  generate-id() = 
                  generate-id(key('testUnits', @testunit)[1])]">
												<xsl:sort select="@testunit"/>
												<option>
													<xsl:attribute name="value"><xsl:value-of select="@testunit"/></xsl:attribute>
													<xsl:value-of select="@testunit"/>
												</option>
											</xsl:for-each>
										</select>
										<!-- testcase search -->
										<input type="text" TF_colKey="resultSummary_Name" TF_searchType="substring" onkeyup="TF_filterTable(document.getElementById
                       ('resultSummaryTable'), document.resultSummaryFilter)">
              </input>
									</td>
								</tr>
							</table>
						</form>
					</td>
				</tr>
			</table>
			<!-- filter table -->
		</xsl:if>
		<table id="resultSummaryTable" class="details" border="1" cellspacing="0">
			<tr>
				<th class="name">Test Unit name</th>
				<th class="name">Total</th>
				<th class="name">Pass</th>
				<th class="name">Fail</th>
				<th class="name">Skip</th>
				<th class="name">Timeout</th>
				<th class="name">Abort</th>
				<th class="name">Abandoned</th>
				<th class="name">Scratch</th>
				<th class="name">Duration (min.)</th>
			</tr>
			<xsl:for-each select="test-result/test-case[ generate-id() = 
                  generate-id(key('testUnits', @testunit)[1])]">
				<xsl:sort select="@testunit"/>
				<xsl:variable name="currentTestUnit" select="@testunit"/>
				<tr>
					<td TF_colKey="resultSummary_Name">
						<xsl:attribute name="TF_colValue"><xsl:value-of select="@testunit"/></xsl:attribute>
						<xsl:if test="count(key('testUnits',@testunit)) = 
                        count(key('successTestUnits', @testunit))">
							<xsl:value-of select="@testunit"/>
						</xsl:if>
						<xsl:if test="count(key('testUnits',@testunit)) != 
                        count(key('successTestUnits', @testunit))">
							<a>
								<xsl:if test="$showErrorDetailSection">
									<xsl:attribute name="href">#testErrors_<xsl:value-of select="@testunit"/></xsl:attribute>
								</xsl:if>
								<xsl:value-of select="@testunit"/>
							</a>
						</xsl:if>
					</td>
					<xsl:variable name="totalTuTests" select="count(key('testUnits',@testunit))"/>
					<xsl:variable name="successTests" select="count(key('successTestUnits', @testunit))"/>
					<xsl:variable name="failTests" select="count(key('failTestUnits', @testunit))"/>
					<xsl:variable name="skipTests" select="count(key('skipTestUnits', @testunit))"/>
					<xsl:variable name="timeoutTests" select="count(key('timeoutTestUnits', @testunit))"/>
					<xsl:variable name="abortTests" select="count(key('abortTestUnits', @testunit))"/>
					<xsl:variable name="abandonedTests" select="count(key('abandonedTestUnits', @testunit))"/>
					<xsl:variable name="scratchTests" select="count(key('scratchTestUnits', @testunit))"/>
					<td>
						<xsl:value-of select="$totalTuTests"/>
					</td>
					<td TF_colKey="resultSummary_Error">
						<xsl:attribute name="TF_colValue"><xsl:if test="$totalTuTests != $successTests"><xsl:text>ERRORS</xsl:text></xsl:if><xsl:if test="$totalTuTests = $successTests"><xsl:text>ALL_SUCCESS</xsl:text></xsl:if></xsl:attribute>
						<xsl:value-of select="$successTests"/>
					</td>
					<td>
						<xsl:if test="$failTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$failTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:if test="$skipTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$skipTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:if test="$timeoutTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$timeoutTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:if test="$abortTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$abortTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:if test="$abandonedTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$abandonedTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:if test="$scratchTests &gt; 0">
							<font color="red">
								<xsl:value-of select="$scratchTests"/>
							</font>
						</xsl:if>
						<xsl:text>&#160;</xsl:text>
					</td>
					<td>
						<xsl:variable name="tuTime" select="format-number(sum(/test-log/test-result
            [test-case/@testunit= $currentTestUnit]/@duration) div 1000 div 60, 
            '#.##')"/>
						<xsl:value-of select="$tuTime"/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- Creates the test run summary table -->
	<xsl:variable name="totalTime" select="format-number(sum(/test-log/test-result/@duration)
           div 1000 div 60, '#.##')"/>
	<xsl:variable name="totalTests" select="count(/test-log/test-result)"/>
	<xsl:variable name="testResults" select="/test-log/test-result"/>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        REPORT SUMMARY                             -->
	<!-- ================================================================ -->
	<xsl:template name="reportSummary">
		<hr width="100%" size="2"/>
		<a name="reportSummary"/>
		<center>
			<h2> Report Summary <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>]</xsl:if>
			</h2>
		</center>
		<xsl:variable name="successTests" select="count(test-result[@result = 'SUCCESS'])"/>
		<xsl:variable name="failTests" select="count(test-result[@result = 'FAILURE'])"/>
		<xsl:variable name="skipTests" select="count(test-result[@result = 'SKIP'])"/>
		<xsl:variable name="timeoutTests" select="count(test-result[@result = 'TIMEOUT'])"/>
		<xsl:variable name="abortTests" select="count(test-result[@result = 'ABORT'])"/>
		<xsl:variable name="abandonedTests" select="count(test-result[@result = 'ABANDONED'])"/>
		<xsl:variable name="scratchTests" select="count(test-result[@result = 'SCRATCH'])"/>
		<table style="margin-bottom:0em" class="details" border="1" cellspacing="0">
			<!-- result summary description  -->
			<tr>
				<!-- summary description -->
				<tr>
					<!-- start of error statement -->
					<!-- run problem or clean run -->
					<xsl:choose>
						<!-- no result , problem -->
						<xsl:when test="$totalTests = 0">
							<td colspan="2">
								<b>
									<span style="color: rgb(155, 0, 0);font-size:15 pt">
										<xsl:text>&#160;&#160;</xsl:text>
                No test were found, problem running tests.
                </span>
								</b>
							</td>
							<td colspan="7" style="padding:0px;background-color:rgb(255, 0, 0);font-size:10pt">
								<b>
									<center>:-(</center>
								</b>
							</td>
						</xsl:when>
						<!-- clean run -->
						<xsl:when test="$totalTests = $successTests">
							<td colspan="2">
								<b>
									<span style="color: rgb(0, 153, 0);font-size:15 pt">
										<xsl:text>&#160;&#160;</xsl:text>
                Clean run
                </span>
								</b>
							</td>
							<td colspan="7" style="padding:0px;background-color:rgb(0, 255, 0)
                 ;font-size:10pt">
								<b>
									<center>:-)</center>
								</b>
							</td>
						</xsl:when>
					</xsl:choose>
				</tr>
				<!-- end of clean run check -->
				<!-- test has failures -->
				<xsl:if test="not($totalTests = $successTests)">
					<tr>
						<!-- start of failure -->
						<td colspan="9">
							<!-- not  a clean run -->
							<!-- summary description table -->
							<table style="margin-bottom:0em" stype="border=0">
								<tr>
									<td>
										<b>
											<span style="color: rgb(153, 0, 0);font-size:12 pt">
												<xsl:text>&#160;&#160;</xsl:text>Not a clean run :-(.
                </span>
										</b>
										<xsl:text>&#160;</xsl:text>Total Errors: [<font style="padding:0px;color:rgb(255, 0,0);
                      font-size:10pt">
											<xsl:value-of select="$totalTests - $successTests"/>
										</font> / <xsl:value-of select="$totalTests"/>]
                <p/>
										<ul>
											<li>
                    Go to <a href="#testResultErrorSummary"> Error summary </a>
                     section, to view New/Old problem in a table.
                  </li>
										</ul>
										<p/>
										<table style="margin-bottom:0em" cellspacing="0">
											<!-- problem bug table -->
											<tr>
												<td>
													<p/>
													<xsl:for-each select="$testResults">
														<xsl:variable name="testResult" select="."/>
														<xsl:variable name="testUnit" select="string($testResult/test-case/@testunit)"/>
														<xsl:variable name="logicalName" select="string($testResult/@logicalname)"/>
														<xsl:variable name="execTime" select="string($testResult/@exectime)"/>
														<xsl:variable name="testPath" select="string($testResult/test-case/@testpath)"/>
														<xsl:if test="not((position()-1) mod 100)">
                 [<xsl:value-of select="substring($execTime,12,8)"/>]
               </xsl:if>
														<xsl:if test="not((position() - 1) mod 25)">
                 |
               </xsl:if>
														<xsl:variable name="result" select="$testResult/@result"/>
														<!-- success -->
														<xsl:if test="$result = 'SUCCESS'">
															<font style="padding:0px;background-color:Chartreuse;
                 font-size:10pt">
																<xsl:attribute name="onMouseover">ddrivetip('<xsl:value-of select="$testUnit"/>','Chartreuse')</xsl:attribute>
																<xsl:attribute name="onMouseout">hideddrivetip()</xsl:attribute>.</font>
														</xsl:if>
														<!-- failure -->
														<xsl:if test="$result = 'FAILURE'">
															<font style="padding:0px;background-color:hotpink;
                 font-size:10pt">
																<a>
																	<xsl:if test="$showErrorDetailSection">
																		<xsl:attribute name="href">#next_<xsl:value-of select="concat($testPath, $execTime,
                                    $logicalName)"/></xsl:attribute>
																	</xsl:if>
																	<xsl:attribute name="onMouseover">ddrivetip('<xsl:value-of select="concat($testUnit,'.',$logicalName)"/>','hotpink')</xsl:attribute>
																	<xsl:attribute name="onMouseout">hideddrivetip()</xsl:attribute>.</a>
															</font>
														</xsl:if>
														<!-- skip -->
														<xsl:if test="$result = 'SKIP'">
															<font style="padding:0px;background-color:cornflowerblue;
                       font-size:10pt">
																<a>
																	<xsl:if test="$showErrorDetailSection">
																		<xsl:attribute name="href">#next_<xsl:value-of select="concat($testPath, $execTime,
                                    $logicalName)"/></xsl:attribute>
																	</xsl:if>
																	<xsl:attribute name="onMouseover">ddrivetip('<xsl:value-of select="concat($testUnit,'.',$logicalName)"/>','cornflowerblue')</xsl:attribute>
																	<xsl:attribute name="onMouseout">hideddrivetip()</xsl:attribute>.</a>
															</font>
														</xsl:if>
														<!-- timeout -->
														<xsl:if test="$result = 'TIMEOUT'">
															<font style="padding:0px;background-color:yellow;
                      font-size:10pt">
																<a>
																	<xsl:if test="$showErrorDetailSection">
																		<xsl:attribute name="href">#next_<xsl:value-of select="concat($testPath, $execTime,
                                    $logicalName)"/></xsl:attribute>
																	</xsl:if>
																	<xsl:attribute name="onMouseover">ddrivetip('<xsl:value-of select="concat($testUnit,'.',$logicalName)"/>','yellow')</xsl:attribute>
																	<xsl:attribute name="onMouseout">hideddrivetip()</xsl:attribute>.</a>
															</font>
														</xsl:if>
														<!-- Not a Success, failure, skip or timeout -->
														<xsl:if test="not($result = 'TIMEOUT' or 
                                 $result = 'SUCCESS' or
                                 $result = 'FAILURE' or
                                 $result = 'SKIP')">
															<font style="padding:0px;background-color:Darkgray;
                      font-size:10pt">
																<a>
																	<xsl:if test="$showErrorDetailSection">
																		<xsl:attribute name="href">#next_<xsl:value-of select="concat($testPath, $execTime,
                                    $logicalName)"/></xsl:attribute>
																	</xsl:if>
																	<xsl:attribute name="onMouseover">ddrivetip('<xsl:value-of select="concat($testUnit,'.',$logicalName)"/>','Darkgray')</xsl:attribute>
																	<xsl:attribute name="onMouseout">hideddrivetip()</xsl:attribute>.</a>
															</font>
														</xsl:if>
														<!-- wrap the row for every 100 errors -->
														<xsl:if test="not(position() mod 100)">
															<xsl:text>&#160;</xsl:text>
															<xsl:value-of select="position()"/>]<br/>
														</xsl:if>
													</xsl:for-each>] <!-- each error in  -->
												</td>
											</tr>
											<!-- bug row -->
										</table>
										<!--  bug table -->
										<p/>
										<!-- bar label -->
										<table border="1" cellspacing="0" style="margin-bottom:0em">
											<tr>
												<th class="title" colspan="9">
                    ResultBar Information
                   </th>
											</tr>
											<tr>
												<td colspan="9">
													<xsl:text>&#160;&#160;&#160;&#160;&#160;&#160;</xsl:text>
                   Each dot in the above ResultBar represents a
                   test in the  run.</td>
											</tr>
											<tr>
												<th class="name" colspan="1" style="background-color:hotpink">.</th>
												<td colspan="4">FAILURE</td>
												<th class="name" colspan="2">Exec date </th>
												<td colspan="2">
													<xsl:value-of select="/test-log/header-info/@execdate"/>
												</td>
											</tr>
											<tr>
												<th class="name" colspan="1" style="background-color:yellow">.</th>
												<td colspan="4">TIMEOUT</td>
												<th class="name" colspan="2"> File </th>
												<td colspan="2">
													<xsl:value-of select="$gtlfFileName"/>
												</td>
											</tr>
											<tr>
												<th class="name" colspan="1" style="background-color:cornflowerblue">.</th>
												<td colspan="4">SKIP</td>
												<th class="name" colspan="2"> Change # </th>
												<td colspan="2">
													<xsl:value-of select="/test-log/@changenumber"/>
												</td>
											</tr>
											<tr>
												<th class="name" colspan="1" style="background-color:DarkGray;color:white">.</th>
												<td colspan="4">ABORT or ABANDONED or SCRATCH</td>
												<th class="name" colspan="2"> OS</th>
												<td colspan="2">
													<xsl:value-of select="/test-log/environment/env-attribute[@name 
                        = 'OS']/@value"/>
												</td>
											</tr>
											<tr>
												<th class="name" colspan="1" style="background-color:Chartreuse">.</th>
												<td colspan="4">SUCCESS</td>
												<th class="name" colspan="2"> JVM</th>
												<td colspan="2">
													<xsl:value-of select="/test-log/environment/env-attribute[@name 
                        = 'JVM_Name']/@value"/>
													<xsl:text>&#160;</xsl:text>
													<xsl:value-of select="/test-log/environment/env-attribute[@name 
                        = 'JVM_Version']/@value"/>
												</td>
											</tr>
										</table>
										<!-- result bar table -->
									</td>
								</tr>
								<!-- description row row -->
							</table>
							<!-- sumarry desc table -->
						</td>
						<!-- cell with error info -->
					</tr>
				</xsl:if>
				<!-- not a clean run -->
			</tr>
			<!-- summary description section -->
		</table>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        RESULT SUMMARY                             -->
	<!-- ================================================================ -->
	<xsl:template name="summary">
		<hr width="100%" size="2"/>
		<a name="summary"/>
		<center>
			<h2> Result Summary <xsl:if test="$showTopNavigationTool">[<a href="#top"> Top </a>]</xsl:if>
			</h2>
		</center>
		<xsl:variable name="successTests" select="count(test-result[@result = 'SUCCESS'])"/>
		<xsl:variable name="failTests" select="count(test-result[@result = 'FAILURE'])"/>
		<xsl:variable name="skipTests" select="count(test-result[@result = 'SKIP'])"/>
		<xsl:variable name="timeoutTests" select="count(test-result[@result = 'TIMEOUT'])"/>
		<xsl:variable name="abortTests" select="count(test-result[@result = 'ABORT'])"/>
		<xsl:variable name="abandonedTests" select="count(test-result[@result = 'ABANDONED'])"/>
		<xsl:variable name="scratchTests" select="count(test-result[@result = 'SCRATCH'])"/>
		<table border="1" class="details" cellspacing="0">
			<tr>
				<th>Total</th>
				<th>Pass</th>
				<th>Fail</th>
				<th>Skip</th>
				<th>Timeout</th>
				<th>Abort</th>
				<th>Abandoned</th>
				<th>Scratch</th>
				<th>Duration (minutes)</th>
			</tr>
			<tr>
				<td>
					<xsl:value-of select="$totalTests"/>
				</td>
				<td>
					<xsl:value-of select="$successTests"/>
          (<xsl:value-of select="format-number($successTests div $totalTests
          * 100, '#.##')"/>%)
        </td>
				<td>
					<xsl:if test="$failTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$failTests"/>
            (<xsl:value-of select="format-number($failTests div $totalTests
            * 100, '#.##')"/>%)
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:if test="$skipTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$skipTests"/>
            (<xsl:value-of select="format-number($skipTests div $totalTests
            * 100, '#.##')"/>%)    
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:if test="$timeoutTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$timeoutTests"/>
            (<xsl:value-of select="format-number($timeoutTests div $totalTests
            * 100, '#.##')"/>%)    
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:if test="$abortTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$abortTests"/>
            (<xsl:value-of select="format-number($abortTests div $totalTests
            * 100, '#.##')"/>%)    
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:if test="$abandonedTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$abandonedTests"/>
            (<xsl:value-of select="format-number($abandonedTests div $totalTests
            * 100, '#.##')"/>%)    
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:if test="$scratchTests &gt; 0">
						<font color="red">
							<xsl:value-of select="$scratchTests"/>
            (<xsl:value-of select="format-number($scratchTests div $totalTests
            * 100, '#.##')"/>%)    
          </font>
					</xsl:if>
					<xsl:text>&#160;</xsl:text>
				</td>
				<td>
					<xsl:value-of select="$totalTime"/>
				</td>
			</tr>
			<tr>
				<table border="0" cellspacing="0">
					<tr>
						<td width="17%">
							<p/>
							<b>Start time: </b>
						</td>
						<td>
							<p/>
							<xsl:value-of select="test-result[1]/@exectime"/>
						</td>
						<td width="7%">
          </td>
						<td width="17%">
							<p/>
							<b>End time: </b>
						</td>
						<td>
							<p/>
							<xsl:value-of select="test-result[last()]/@exectime"/>
						</td>
					</tr>
				</table>
			</tr>
		</table>
	</xsl:template>
	<xsl:template name="recordTime">
		<xsl:param name="message">Time: </xsl:param>
		<!-- show time only if instrument is turned on (1) -->
		<xsl:if test="$instrument = 1">
          [<xsl:value-of select="string(java:util.Date.new())"/>][<xsl:value-of select="string(java:lang.System.currentTimeMillis())"/>]  <xsl:value-of select="$message"/>
			<br/>
		</xsl:if>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        REPORT HEADER                              -->
	<!-- ================================================================ -->
	<xsl:template name="header">
		<a name="top"/>
		<center>
			<div style="background-color: skyblue;">
				<xsl:if test="$showResultSummarySection">
					<span style="background-color: white;">
               [<a href="#summary">Result Summary</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showResultDetailSection">
					<span style="background-color: white;">
               [<a href="#summaryByTestUnit">Result Detail</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showErrorSummarySection">
					<span style="background-color: white;">
              [<a href="#testResultErrorSummary">Error Summary</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showErrorDetailSection">
					<span style="background-color: white;">
               [<a href="#testResultWithErrors">Error Detail</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showSetupSection">
					<span style="background-color: white;">
               [<a href="#metaInfo">Setup</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
			</div>
		</center>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        MENU                                       -->
	<!-- ================================================================ -->
	<xsl:template name="sidemenu">
		<script language="JavaScript1.2"><![CDATA[
      /********************************************************************************
      Submitted with modifications by Jack Routledge (http://fastway.to/compute) to DynamicDrive.com
      Copyright (C) 1999 Thomas Brattli @ www.bratta.com
      This script is made by and copyrighted to Thomas Brattli 
      This may be used freely as long as this msg is intact!
      This script has been featured on http://www.dynamicdrive.com
      ********************************************************************************
      Browsercheck:*/
      ie=document.all?1:0
      n=document.layers?1:0
      ns6=document.getElementById&&!document.all?1:0
      
      //These are the variables you have to set:
      
      //How much of the layer do you wan't to be visible when it's in the out state?
      lshow=40
      
      //How many pixels should it move every step? 
      var move=10;
      
      //At what speed (in milliseconds, lower value is more speed)
      menuSpeed=0
      
      //Do you want it to move with the page if the user scroll the page?
      var moveOnScroll=true
      
      /********************************************************************************
      You should't have to change anything below this.
      ********************************************************************************/
      //Defining variables
      
      var ltop;
      var tim=0;
      
      //Object constructor
      function makeMenu(obj,nest){
          nest=(!nest) ? '':'document.'+nest+'.'
          if (n) this.css=eval(nest+'document.'+obj)
          else if (ns6) this.css=document.getElementById(obj).style
          else if (ie) this.css=eval(obj+'.style')						
      	this.state=1
      	this.go=0
              if (n) this.width=this.css.document.width
              else if (ns6) this.width=document.getElementById(obj).offsetWidth
              else if (ie) this.width=eval(obj+'.offsetWidth')
      	this.left=b_getleft
          this.obj = obj + "Object"; 	eval(this.obj + "=this")	
      }
      //Get's the top position.
      function b_getleft(){
              if (n||ns6){ gleft=parseInt(this.css.left)}
              else if (ie){ gleft=eval(this.css.pixelLeft)}
      	return gleft;
      }
      /********************************************************************************
      Deciding what way to move the menu (this is called onmouseover, onmouseout or onclick)
      ********************************************************************************/
      function moveMenu(){
      	if(!oMenu.state){
      		clearTimeout(tim)
      		mIn()	
      	}else{
      		clearTimeout(tim)
      		mOut()
      	}
      }
      //Menu in
      function mIn(){
      	if(oMenu.left()>-oMenu.width+lshow){
      		oMenu.go=1
      		oMenu.css.left=oMenu.left()-move
      		tim=setTimeout("mIn()",menuSpeed)
      	}else{
      		oMenu.go=0
      		oMenu.state=1
      	}	
      }
      //Menu out
      function mOut(){
      	if(oMenu.left()<0){
      		oMenu.go=1
      		oMenu.css.left=oMenu.left()+move
      		tim=setTimeout("mOut()",menuSpeed)
      	}else{
      		oMenu.go=0
      		oMenu.state=0
      	}	
      }
      /********************************************************************************
      Checking if the page is scrolled, if it is move the menu after
      ********************************************************************************/
      function checkScrolled(){
      	if(!oMenu.go) oMenu.css.top=eval(scrolled)+parseInt(ltop)
      	if(n||ns6) setTimeout('checkScrolled()',30)
      }
      /********************************************************************************
      Inits the page, makes the menu object, moves it to the right place, 
      show it
      ********************************************************************************/
      function menuInit(){
      	oMenu=new makeMenu('divMenu')
              if (n||ns6) scrolled="window.pageYOffset"
              else if (ie) scrolled="document.body.scrollTop"
      	oMenu.css.left=-oMenu.width+lshow
              if (n||ns6) ltop=oMenu.css.top
              else if (ie) ltop=oMenu.css.pixelTop
      	oMenu.css.visibility='visible'
      	if(moveOnScroll) ie?window.onscroll=checkScrolled:checkScrolled();

      }
      
      
      //Initing menu on pageload
      window.onload=menuInit;
]]></script>
		<div id="divMenu" style="position:absolute; top:0; left:0; visibility:hidden; 
               background-color:lightskyblue">
			<nobr>
				<span style=" font-weight:normal;background-color: rgb(255, 255, 153);">
					<a onclick="moveMenu()" style=" font-weight:normal;background-color:orange;
                 text-decoration:none">[M]</a>
				</span>
				<xsl:text>&#160;</xsl:text>
				<span style=" font-weight:normal;background-color: white;">
             [<a href="#top">Top</a>]
           </span>
				<xsl:text>&#160;</xsl:text>
				<xsl:if test="$showResultSummarySection">
					<span style=" font-weight:normal;background-color: white;">
               [<a href="#summary">Result Summary</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showResultDetailSection">
					<span style=" font-weight:normal;background-color: white;">
               [<a href="#summaryByTestUnit">Result Detail</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showErrorSummarySection">
					<span style=" font-weight:normal;background-color: white;">
               [<a href="#testResultErrorSummary">Error Summary</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showErrorDetailSection">
					<span style=" font-weight:normal;background-color: white;">
                [<a href="#testResultWithErrors">Error Detail</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:if test="$showSetupSection">
					<span style=" font-weight:normal;background-color: white;">
               [<a href="#metaInfo">Setup</a>]
             </span>
					<xsl:text>&#160;</xsl:text>
				</xsl:if>
				<xsl:text>&#160;</xsl:text>
				<a onclick="moveMenu()" style=" font-weight:normal;background-color:orange;
                text-decoration:none">[MENU]</a>
			</nobr>
		</div>
	</xsl:template>

  <xsl:template name="tooltipScript">
		<div id="dhtmltooltip"/>
		<script type="text/javascript"><![CDATA[
    /***********************************************
    * Cool DHTML tooltip script- Dynamic Drive DHTML code library (www.dynamicdrive.com)
    * This notice MUST stay intact for legal use
    * Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
    ***********************************************/
    
    var offsetxpoint=-60 //Customize x offset of tooltip
    var offsetypoint=20 //Customize y offset of tooltip
    var ie=document.all
    var ns6=document.getElementById && !document.all
    var enabletip=false
    if (ie||ns6)
    var tipobj=document.all? document.all["dhtmltooltip"] : document.getElementById? document.getElementById("dhtmltooltip") : ""
    
    function ietruebody(){
    return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
    }
    
    function ddrivetip(thetext, thecolor, thewidth){
    if (ns6||ie){
    if (typeof thewidth!="undefined") tipobj.style.width=thewidth+"px"
    if (typeof thecolor!="undefined" && thecolor!="") tipobj.style.backgroundColor=thecolor
    tipobj.innerHTML=thetext
    enabletip=true
    return false
    }
    }
    
    function positiontip(e){
    if (enabletip){
    var curX=(ns6)?e.pageX : event.x+ietruebody().scrollLeft;
    var curY=(ns6)?e.pageY : event.y+ietruebody().scrollTop;
    //Find out how close the mouse is to the corner of the window
    var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20
    var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20
    
    var leftedge=(offsetxpoint<0)? offsetxpoint*(-1) : -1000
    
    //if the horizontal distance isn't enough to accomodate the width of the context menu
    if (rightedge<tipobj.offsetWidth)
    //move the horizontal position of the menu to the left by it's width
    tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+"px" : window.pageXOffset+e.clientX-tipobj.offsetWidth+"px"
    else if (curX<leftedge)
    tipobj.style.left="5px"
    else
    //position the horizontal position of the menu where the mouse is positioned
    tipobj.style.left=curX+offsetxpoint+"px"
    
    //same concept with the vertical position
    if (bottomedge<tipobj.offsetHeight)
    tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+"px" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+"px"
    else
    tipobj.style.top=curY+offsetypoint+"px"
    tipobj.style.visibility="visible"
    }
    }
    
    function hideddrivetip(){
    if (ns6||ie){
    enabletip=false
    tipobj.style.visibility="hidden"
    tipobj.style.left="-1000px"
    tipobj.style.backgroundColor=''
    tipobj.style.width=''
    }
    }
    
    document.onmousemove=positiontip
    
    ]]></script>
	</xsl:template>
	<xsl:template name="filterScript">
		<script language="JavaScript1.2"><![CDATA[

     function report_init(){
       TF_disableFilter;
     }


     function TF_disableFilter() {
       document.errorSummaryFilter.display = "none";
       document.resultSummaryFilter.display = "none";
     }

     function _TF_trimWhitespace(txt) {
     	var strTmp = txt;
     	//trimming from the front
     	for (counter=0; counter<strTmp.length; counter++)
     		if (strTmp.charAt(counter) != " ")
     			break;
     	//trimming from the back
     	strTmp = strTmp.substring(counter,strTmp.length);
     	counter = strTmp.length - 1;
     	for (counter; counter>=0; counter--)
     		if (strTmp.charAt(counter) != " ")
     			break;
     	return strTmp.substring(0, counter+1);
     }
     
     function _TF_showAll(tb) {
     	for (i=0;i<tb.rows.length;i++)
     	{
     		tb.rows[i].style.display = "";
     	}
     }
     
     function _TF_shouldShow(type, con, val) {
      var sameCaseCon = con.toLowerCase();
      var sameCaseVal = val.toLowerCase();
     	var toshow=true;
     	if (type != null) type = type.toLowerCase();
     	switch (type)
     	{
     		case "item":
     			var strarray = sameCaseVal.split(",");
     			innershow = false;
     			for (ss=0;ss<strarray.length;ss++){
     				if (sameCaseCon==_TF_trimWhitespace(strarray[ss])){
     					innershow=true;
     					break;
     				}
     			}
     			if (innershow == false)
     				toshow=false;
     		break
     		case "full":
     			if (sameCaseVal!=sameCaseCon)
     				toshow = false;
     		break
     		case "substring":
     			if (sameCaseVal.indexOf(sameCaseCon)<0)
     				toshow = false;
     		break
     		default: //is "substring1" search
     			if (sameCaseVal.indexOf(sameCaseCon)!=0) //pattern must start from 1st char
     				toshow = false;
     			if (sameCaseCon.charAt(con.length-1) == " ")
     			{ //last char is a space, so lets do a full search as well
     				if (_TF_trimWhitespace(con) != sameCaseVal)
     					toshow = false;
     				else
     					toshow = true;
     			}
     		break
     	}
     	return toshow;
     }
     
     function _TF_filterTable(tb, conditions) {
     	//given an array of conditions, lets search the table
     	for (i=0;i<tb.rows.length;i++)
     	{
     		var show = true;
     		var rw = tb.rows[i];
     		for (j=0;j<rw.cells.length;j++)
     		{
     			var cl = rw.cells[j];
     			for (k=0;k<conditions.length;k++)
     			{
     				var colKey = cl.getAttribute("TF_colKey");
     				if (colKey == null) //attribute not found
     					continue; //so lets not search on this cell.
     				if (conditions[k].name.toUpperCase() == colKey.toUpperCase())
     				{
     					var tbVal = cl.getAttribute("TF_colValue");
     					var conVals = conditions[k].value;
     					if (conditions[k].single) //single value
     					{ 
     						show = _TF_shouldShow(conditions[k].type, conditions[k].value, cl.getAttribute("TF_colValue"));
     					} else { //multiple values
     						for (l=0;l<conditions[k].value.length;l++)
     						{
     							innershow = _TF_shouldShow(conditions[k].type, conditions[k].value[l], cl.getAttribute("TF_colValue"));
     							if (innershow == true) break;
     						}
     						if (innershow == false)
     							show = false;
     					}
     				}
     			}
     			//if any condition has failed, then we stop the matching (due to AND behaviour)
     			if (show == false)
     				break;
     		}
     		if (show == true)
     			tb.rows[i].style.display = "";
     		else
     			tb.rows[i].style.display = "none";
     	}
     }
     
     /** PUBLIC FUNCTIONS **/
     //main function
     function TF_filterTable(tb, frm) {
     	var conditions = new Array();
     	if (frm.style.display == "none") //filtering is off
     		return _TF_showAll(tb);
     
     	//go thru each type of input elements to figure out the filter conditions
     	var inputs = frm.elements;
     	for (i=0;i<inputs.length;i++)
     	{ //looping thru all INPUT elements
     		if (inputs[i].getAttribute("TF_colKey") == null) //attribute not found
     			continue; //we assume that this input field is not for us
     		switch (inputs[i].type)
     		{
     			case "text":
     			case "hidden":
     				if(inputs[i].value != "")
     				{
     					index = conditions.length;
     					conditions[index] = new Object;
     					conditions[index].name = inputs[i].getAttribute("TF_colKey");
     					conditions[index].type = inputs[i].getAttribute("TF_searchType");
     					conditions[index].value = inputs[i].value;
     					conditions[index].single = true;
     				}
     			break
     		}
     	}
     	var inputs = frm.elements;
     	//able to do multiple selection box
     	for (i=0;i<inputs.length;i++)
     	{ //looping thru all SELECT elements
     		if (inputs[i].getAttribute("TF_colKey") == null) //attribute not found
     			continue; //we assume that this input field is not for us
        if( inputs[i].type != "select-one" && 
            inputs[i].type != "select" && 
            inputs[i].type != "select-multiple") // not of type select
          continue; // only select will be processed
     		var opts = inputs[i].options;
     		var optsSelected = new Array();
     		for (intLoop=0; intLoop<opts.length; intLoop++)
     		{ //looping thru all OPTIONS elements
     			if (opts[intLoop].selected && (opts[intLoop].getAttribute("TF_not_used") == null))
     			{
     				index = optsSelected.length;
     				optsSelected[index] = opts[intLoop].value;
     			}
     		}
     		if (optsSelected.length > 0) //has selected items
     		{
     			index = conditions.length;
     			conditions[index] = new Object;
     			conditions[index].name = inputs[i].getAttribute("TF_colKey");
     			conditions[index].type = inputs[i].getAttribute("TF_searchType");
     			conditions[index].value = optsSelected;
     			conditions[index].single = false;
     		}
     	}
     	//ok, now that we have all the conditions, lets do the filtering proper
     	_TF_filterTable(tb, conditions);
     }
     
     function TF_enableFilter(tb, frm, val) {
     	if (val.checked) //filtering is on
     	{
     		frm.style.display = "";
     	} else { //filtering is off
     		frm.style.display = "none";
     	}
     	//refresh the table
     	TF_filterTable(tb, frm);
     }
     
     function TF_hide(tb){
     	for (i=0;i<tb.rows.length;i++)
     	{
         // We will keep the first row, assuming that it has the title.
         if ( i == 0){
          tb.rows[0].style.display="";
         } else {
     			tb.rows[i].style.display = "none";
         }
      }
     }
     
     function TF_hide_separator(tb, val){
     	if (val.checked) //filtering is on
      {
       	for (i=0;i<tb.rows.length;i++)
       	{
       		var rw = tb.rows[i];
       		for (j=0;j<rw.cells.length;j++)
       		{
       			var cl = rw.cells[j];
            var isSeparator = cl.getAttribute("separator");
  
            if (isSeparator == null) // attribute not found
              continue;
  
            rw.style.display="none";
          } // cells
        } // rows
      } else {
       	for (i=0;i<tb.rows.length;i++)
       	{
       		var rw = tb.rows[i];
       		for (j=0;j<rw.cells.length;j++)
       		{
       			var cl = rw.cells[j];
            var isSeparator = cl.getAttribute("separator");
  
            if (isSeparator == null) // attribute not found
              continue;
  
            rw.style.display="";
          } // cells
        } // rows
      }
      
     } // 

     function TF_show(tb){
     	for (i=0;i<tb.rows.length;i++)
     	{
     			tb.rows[i].style.display = "";
      }
     }

     function _TF_get_value(input) {
     	switch (input.type)
     	{
     		case "text":
     			 return input.value;
     		break
     		case "select-one":
     			if (input.selectedIndex > -1) //has value
     				return input.options(input.selectedIndex).value;
     			else
     				return "";
     		break;
     	}
     }
]]></script>
	</xsl:template>
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!--                        JAVASCIPTS AND CSS                         -->
	<!-- ================================================================ -->
	<xsl:template name="javascripts">
		<meta http-equiv="expires" content="Fri, 24 Jan 1977 10:00:00 GMT"/>
		<xsl:if test="$showFeatureInfo = 1">
			<title>
				<xsl:value-of select="$feature"/> Test Report </title>
		</xsl:if>
		<xsl:if test="not($showFeatureInfo = 1)">
			<title> Test Report </title>
		</xsl:if>
		<xsl:value-of select="$cssFile" disable-output-escaping="yes"/>
		<style type="text/css">

          #dhtmltooltip{
          position: absolute;
          width: 500px;
          border: 2px solid black;
          padding: 2px;
          background-color: lightyellow;
          visibility: hidden;
          z-index: 100;
          text-align: center;
          /*Remove below line to remove shadow. Below line should always appear last within this CSS*/
          filter: progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135);
          }

        </style>
	</xsl:template>
</xsl:stylesheet>
