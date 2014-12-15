/**
 * Copyright (C) 2014 WhiteSource Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.whitesource.fs;

import org.apache.commons.io.FileUtils;
import org.apache.rat.Defaults;
import org.apache.rat.Report;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.walker.FileReportable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.agent.api.ChecksumUtils;
import org.whitesource.agent.api.model.CopyrightInfo;
import org.whitesource.agent.api.model.DependencyInfo;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Factory class for {@link org.whitesource.agent.api.model.DependencyInfo}.
 * @author tom.shapira
 */
public class DependencyInfoFactory {

    /* --- Static members --- */

    private static final Logger logger = LoggerFactory.getLogger(DependencyInfoFactory.class);

    private static final String COPYRIGHT = "copyright";

    private static final Map<String, String> commentStartEndMap;
    static {
        commentStartEndMap = new HashMap<String, String>();
        commentStartEndMap.put("/*", "*/");
        commentStartEndMap.put("/**", "*/");
        commentStartEndMap.put("<!--", "-->");
        commentStartEndMap.put("\"\"\"", "\"\"\"");
        commentStartEndMap.put("=begin", "=end");
    }

    /* --- Public methods --- */

    public DependencyInfo createDependencyInfo(File basedir, String fileName) {
        DependencyInfo dependencyInfo = null;
        File dependencyFile = new File(basedir, fileName);
        try {
            String sha1 = ChecksumUtils.calculateSHA1(dependencyFile);
            dependencyInfo = new DependencyInfo(sha1);
            dependencyInfo.setArtifactId(dependencyFile.getName());
            dependencyInfo.setLastModified(new Date(dependencyFile.lastModified()));
            try {
                dependencyInfo.setSystemPath(dependencyFile.getCanonicalPath());
            } catch (IOException e) {
                dependencyInfo.setSystemPath(dependencyFile.getAbsolutePath());
            }

            try {
                Set<String> licenses = scanLicenses(dependencyFile);
                dependencyInfo.getLicenses().addAll(licenses);
            } catch (Exception e) {
                logger.debug("Error scanning file for license", e);
            }
            dependencyInfo.getCopyrights().addAll(extractCopyrights(dependencyFile));
        } catch (IOException e) {
            logger.warn("Failed to create dependency " + fileName + " to dependency list: ", e);
        }
        return dependencyInfo;
    }

    /* --- Private methods --- */

    private Set<String> scanLicenses(File file) throws InterruptedException, TransformerConfigurationException, RatException, IOException {
        HeaderMatcherMultiplexer matcherMultiplexer = new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
        ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(matcherMultiplexer);

        ClaimStatistic report = Report.report(new StringWriter(), new FileReportable(file), Defaults.getPlainStyleSheet(), configuration);
        Set<String> licenses = report.getLicenseFileNameMap().keySet();
        if (licenses != null) {
            licenses.remove(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN);
        }
        return licenses;
    }

    private Collection<CopyrightInfo> extractCopyrights(File file) {
        Collection<CopyrightInfo> copyrights = new ArrayList<CopyrightInfo>();
        try {
            boolean commentBlock = false;
            Iterator<String> iterator = FileUtils.readLines(file).iterator();
            int lineIndex = 1;
            while (iterator.hasNext()) {
                // trim (duh..)
                String line = iterator.next().trim();

                // check if comment block
                if (!commentBlock) {
                    for (Map.Entry<String, String> entry : commentStartEndMap.entrySet()) {
                        String commentStart = entry.getKey();
                        String commentEnd = entry.getValue();
                        if (line.startsWith(commentStart)) {
                            if (line.contains(commentEnd)) {
                                commentBlock = false;
                                line = line.substring(commentStart.length(), line.indexOf(commentEnd));
                            } else {
                                commentBlock = true;
                            }
                            break;
                        } else if (line.contains(commentStart)) {
                            int startIndex = line.indexOf(commentStart);
                            if (line.contains(commentEnd)) {
                                int endIndex = line.indexOf(commentEnd);
                                if (startIndex < endIndex) {
                                    commentBlock = false;
                                    line = line.substring(startIndex, endIndex);
                                }
                            } else {
                                commentBlock = true;
                                line = line.substring(startIndex);
                            }
                            break;
                        }
                    }
                }

                // check for one-line comments
                if ((commentBlock || line.startsWith("//") || line.startsWith("#"))
                        && line.toLowerCase().contains(COPYRIGHT)) {
                    line = line.replace("/**", "").replace("/*", "")
                            .replace("*", "").replace("#", "")
                            .replace("/", "").replace("\\t", "")
                            .replace("\\n", "").trim();
                    copyrights.add(new CopyrightInfo(line, lineIndex));
                }

                // check if line has ending of comment block
                for (String commentEnd : commentStartEndMap.values()) {
                    if (line.contains(commentEnd)) {
                        commentBlock = false;
                        break;
                    }
                }
                lineIndex++;
            }
        } catch (FileNotFoundException e) {
            logger.warn("File not found: " + file.getPath());
        } catch (IOException e) {
            logger.warn("Error reading file: " + file.getPath());
        }
        return copyrights;
    }

}
