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
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.agent.ProjectsSender;
import org.whitesource.agent.api.model.AgentProjectInfo;
import org.whitesource.agent.utils.Pair;
import org.whitesource.web.FsaVerticle;

import java.util.*;

/**
 * Author: Itai Marko
 */
public class Main {

    /* --- Static members --- */

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /* --- Main --- */

    private static Vertx vertx;

    /* --- Main --- */

    public static void main(String[] args) {

        CommandLineArgs commandLineArgs = new CommandLineArgs();
        new JCommander(commandLineArgs, args);

        if(commandLineArgs.web.equals("false")) {
            int processExitCode;
            try {
                processExitCode = execute(args);
            } catch (Exception e) {
                // catch any exception that may be thrown, return error code
                logger.warn("Process encountered an error: {}" + e.getMessage(), e);
                processExitCode = StatusCode.ERROR.getValue();
            }
            System.exit(processExitCode);
        }else {
            vertx = Vertx.vertx();
            vertx.deployVerticle(FsaVerticle.class.getName());
        }
    }

    public static int execute(String[] args) {
        // read configuration config
        FSAConfiguration fsaConfiguration = new FSAConfiguration(args);

        ProjectsCalculator projectsCalculator = new ProjectsCalculator();

        Pair<Collection<AgentProjectInfo>,StatusCode> projects = projectsCalculator.getAllProjects(fsaConfiguration);
        if(!projects.getValue().equals(StatusCode.SUCCESS)){
            return projects.getValue().getValue();
        }

        ProjectsSender projectsSender = new ProjectsSender(fsaConfiguration);
        StatusCode processExitCode = projectsSender.sendProjects(projects.getKey());
        logger.info("Process finished with exit code {} ({})", processExitCode, processExitCode.getValue());
        return processExitCode.getValue();
    }
}
