/**
 * Copyright (C) 2017 WhiteSource Ltd.
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
package org.whitesource.agent.dependency.resolver;

import java.util.Map;
import java.util.Set;

/**
 * @author eugen.horovitz
 */
public class ResolvedFolder {

    /* --- Members --- */

    private final String originalScanFolder;
    private final Map<String, Set<String>> topFoldersFound;

    /* --- Constructors --- */

    public ResolvedFolder(String originalScanFolder, Map<String, Set<String>> topFoldersFound) {
        this.originalScanFolder = originalScanFolder;
        this.topFoldersFound = topFoldersFound;
    }

    /* --- Getters --- */

    public String getOriginalScanFolder() {
        return originalScanFolder;
    }

    public Map<String, Set<String>> getTopFoldersFound() {
        return topFoldersFound;
    }
}