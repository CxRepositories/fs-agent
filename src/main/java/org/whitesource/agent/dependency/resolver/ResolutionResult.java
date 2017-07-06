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

import org.whitesource.agent.api.model.DependencyInfo;

import java.util.Collection;

/**
 * Created by eugen on 6/21/2017.
 */
public class ResolutionResult {

    /* --- Members --- */

    private Collection<DependencyInfo> resolvedDependencies;
    private Collection<String> excludes;

    /* --- Constructors --- */

    public ResolutionResult(Collection<DependencyInfo> resolvedDependencies, Collection<String> excludes) {
        this.resolvedDependencies = resolvedDependencies;
        this.excludes = excludes;
    }

    /* --- Getters --- */

    public Collection<String> getExcludes() {
        return excludes;
    }

    public Collection<DependencyInfo> getResolvedDependencies() {
        return resolvedDependencies;
    }
}