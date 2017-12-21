/**
 * Copyright (C) 2014 WhiteSource Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.whitesource.fs;

import ch.qos.logback.classic.Level;
import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.agent.ConfigPropertyKeys;
import org.whitesource.fs.configuration.ConfigurationValidation;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.whitesource.agent.ConfigPropertyKeys.*;

/**
 * Author: Itai Marko
 */
public class Main {

    /* --- Static members --- */

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String INFO = "info";

    public static final CommandLineArgs commandLineArgs = new CommandLineArgs();
    public static final String FALSE = "false";
    private static ConfigurationValidation configurationValidation = new ConfigurationValidation();

    /* --- Main --- */

    public static void main(String[] args) {
        int processExitCode;
        try {
            processExitCode = execute(args);
        } catch (Exception e) {
            // catch any exception that may be thrown, return error code
            logger.warn("Process encountered an error: {}" + e.getMessage(), e);
            processExitCode = StatusCode.ERROR.getValue();
        }
        System.exit(processExitCode);
    }

    public static int execute(String[] args) {
        new JCommander(commandLineArgs, args);
        // validate args // TODO use jCommander validators
        // TODO add usage command

        // read configuration properties
        String project = commandLineArgs.project;
        Properties configProperties = configurationValidation.readAndValidateConfigFile(commandLineArgs.configFilePath, project);

        List<String> offlineRequestFiles = updateProperties(configProperties, project);

        // read log level from configuration file
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        String logLevel = configProperties.getProperty(LOG_LEVEL_KEY, INFO);
        root.setLevel(Level.toLevel(logLevel, Level.INFO));

        // read directories and files from list-file
        List<String> files = new ArrayList<>();
        String fileListPath = commandLineArgs.fileListPath;
        if (StringUtils.isNotBlank(fileListPath)) {
            try {
                File listFile = new File(fileListPath);
                if (listFile.exists()) {
                    files.addAll(FileUtils.readLines(listFile));
                }
            } catch (IOException e) {
                logger.warn("Error reading list file");
            }
        }

        // read csv directory list
        files.addAll(commandLineArgs.dependencyDirs);

        // run the agent
        FileSystemAgent agent = new FileSystemAgent(configProperties, files, offlineRequestFiles);
        StatusCode processExitCode = agent.sendRequest();
        logger.info("Process finished with exit code {} ({})", processExitCode, processExitCode.getValue());
        return processExitCode.getValue();
    }

    private static List<String> updateProperties(Properties configProps, String project) {
        // Check whether the user inserted api key, project OR/AND product via command line
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.ORG_TOKEN_PROPERTY_KEY, commandLineArgs.apiKey);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.UPDATE_TYPE, commandLineArgs.updateType);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PRODUCT_NAME_PROPERTY_KEY, commandLineArgs.product);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PRODUCT_VERSION_PROPERTY_KEY, commandLineArgs.productVersion);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROJECT_NAME_PROPERTY_KEY, project);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROJECT_VERSION_PROPERTY_KEY, commandLineArgs.projectVersion);
        // request file
        List<String> offlineRequestFiles = new LinkedList<>();
        offlineRequestFiles.addAll(commandLineArgs.requestFiles);
        if (offlineRequestFiles.size() > 0) {
            configProps.put(ConfigPropertyKeys.OFFLINE_PROPERTY_KEY, FALSE);
        }
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.OFFLINE_PROPERTY_KEY, commandLineArgs.offline);

        // proxy
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROXY_HOST_PROPERTY_KEY, commandLineArgs.proxyHost);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROXY_PORT_PROPERTY_KEY, commandLineArgs.proxyPass);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROXY_USER_PROPERTY_KEY, commandLineArgs.proxyPort);
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROXY_PASS_PROPERTY_KEY, commandLineArgs.proxyUser);

        // archiving
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.ARCHIVE_FAST_UNPACK_KEY, commandLineArgs.archiveFastUnpack);

        // project per folder
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.PROJECT_PER_SUBFOLDER, commandLineArgs.projectPerFolder);

        // Check whether the user inserted repositoriesFile via command line
        readPropertyFromCommandLine(configProps, ConfigPropertyKeys.SCM_REPOSITORIES_FILE, commandLineArgs.repositoriesFile);

        return offlineRequestFiles;
    }

    /* --- Private methods --- */

    private static void readPropertyFromCommandLine(Properties configProps, String propertyKey, String propertyValue) {
        if (StringUtils.isNotBlank(propertyValue)) {
            configProps.put(propertyKey, propertyValue);
        }
    }
}
