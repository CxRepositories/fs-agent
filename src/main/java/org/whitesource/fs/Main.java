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

import com.beust.jcommander.JCommander;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.agent.ProjectsSender;
import org.whitesource.agent.api.model.AgentProjectInfo;
import org.whitesource.agent.api.model.Coordinates;
import org.whitesource.agent.utils.Pair;
import org.whitesource.fs.configuration.ConfigurationSerializer;
import org.whitesource.web.FsaVerticle;
import java.util.*;

/**
 * Author: Itai Marko
 */
public class Main {

    /* --- Static members --- */

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final long MAX_TIMEOUT = 1000*60*60;

    /* --- Main --- */

    private static Vertx vertx;
    ProjectsCalculator projectsCalculator = new ProjectsCalculator();

    /* --- Main --- */

    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        new JCommander(commandLineArgs, args);

        StatusCode processExitCode;

        // read configuration senderConfig
        FSAConfiguration fsaConfiguration = new FSAConfiguration(args);
        boolean isStandalone = commandLineArgs.web.equals("false");

        if (isStandalone) {
            try {
                if (fsaConfiguration.getErrors() == null || fsaConfiguration.getErrors().size() > 0) {
                    processExitCode = StatusCode.ERROR;
                    fsaConfiguration.getErrors().forEach(error -> logger.error(error));
                    logger.warn("Exiting");
                } else {
                    processExitCode = new Main().scanAndSend(fsaConfiguration, true).getStatusCode();
                }
            } catch (Exception e) {
                // catch any exception that may be thrown, return error code
                logger.warn("Process encountered an error: {}" + e.getMessage(), e);
                processExitCode = StatusCode.ERROR;
            }
            System.exit(processExitCode.getValue());
        } else {
            //this is a work around
            vertx = Vertx.vertx(new VertxOptions()
                    .setBlockedThreadCheckInterval(MAX_TIMEOUT));

            JsonObject config = new JsonObject();
            config.put(FsaVerticle.CONFIGURATION, ConfigurationSerializer.getAsString(fsaConfiguration, false));
            DeploymentOptions options = new DeploymentOptions()
                    .setConfig(config)
                    .setWorker(true);
            vertx.deployVerticle(FsaVerticle.class.getName(), options);
        }
    }

    public ProjectsDetails scanAndSend(FSAConfiguration fsaConfiguration, boolean shouldSend) {
        if (fsaConfiguration.getErrors() != null && fsaConfiguration.getErrors().size() > 0) {
            return new ProjectsDetails(new ArrayList<>(), StatusCode.ERROR, String.join(System.lineSeparator(), fsaConfiguration.getErrors()));
        }

        ProjectsDetails result = projectsCalculator.getAllProjects(fsaConfiguration);
        if (!result.getStatusCode().equals(StatusCode.SUCCESS)) {
            return new ProjectsDetails(result.getProjects(), result.getStatusCode(), "");
        }

        if (shouldSend) {
            ProjectsSender projectsSender = new ProjectsSender(fsaConfiguration.getSender(), fsaConfiguration.getOffline() ,fsaConfiguration.getRequest(), new FileSystemAgentInfo());
            Pair<String, StatusCode> processExitCode = sendProjects(projectsSender, result.getProjects());
            logger.debug("Process finished with exit code {} ({})", processExitCode.getKey(), processExitCode.getValue());
            return new ProjectsDetails(new ArrayList<>(), processExitCode.getValue(), processExitCode.getKey());
        } else {
            return new ProjectsDetails(result.getProjects(), result.getStatusCode(), "");
        }
    }

    private Pair<String, StatusCode> sendProjects(ProjectsSender projectsSender, Collection<AgentProjectInfo> projects) {
        Iterator<AgentProjectInfo> iterator = projects.iterator();
        while (iterator.hasNext()) {
            AgentProjectInfo project = iterator.next();
            if (project.getDependencies().isEmpty()) {
                iterator.remove();

                // if coordinates are null, then use token
                String projectIdentifier;
                Coordinates coordinates = project.getCoordinates();
                if (coordinates == null) {
                    projectIdentifier = project.getProjectToken();
                } else {
                    projectIdentifier = coordinates.getArtifactId();
                }
                logger.info("Removing empty project {} from update (found 0 matching files)", projectIdentifier);
            }
        }

        if (projects.isEmpty()) {
            logger.info("Exiting, nothing to update");
            return new Pair<>("Exiting, nothing to update", StatusCode.SUCCESS);
        } else {
            return projectsSender.sendRequest(projects);
        }
    }
}