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

import com.beust.jcommander.Parameter;

/**
 * Author: Itai Marko
 */
public class CommandLineArgs {

    private static final String CONFIG_FILE_NAME = "whitesource-fs-agent.config";

    //TODO use a File converter for dependencyDir and configFilePath
    @Parameter(names = "-d", description = "Path to base directory of dependency files to scan")
    String dependencyDir = "."; // TODO this may be a bad default, consider printing usage instead

    @Parameter(names = "-c", description = "Config file path")
    String configFilePath = CONFIG_FILE_NAME;
}
