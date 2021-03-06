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
package org.whitesource.fs.configuration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.whitesource.fs.FSAConfiguration;
import java.util.Properties;

import static org.whitesource.agent.ConfigPropertyKeys.*;

public class EndPointConfiguration {

    public static final int DEFAULT_PORT = 443;
    public static final boolean DEFAULT_SSL = false;
    public static final String DEFAULT_CERTIFICATE = "test.jks";
    public static final String DEFAULT_PASS = "123456";
    private static final boolean DEFAULT_ENABLED = false;

    private final int port;
    private final String certificate;
    private final String pass;
    private final boolean enabled;
    private final boolean ssl;

    @JsonProperty(ENDPOINT_PORT)
    public int getPort() {
        return port;
    }

    @JsonProperty(ENDPOINT_CERTIFICATE)
    public String getCertificate() {
        return certificate;
    }

    @JsonProperty(ENDPOINT_PASS)
    public String getPass() {
        return pass;
    }

    @JsonProperty(ENDPOINT_ENABLED)
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty(ENDPOINT_SSL_ENABLED)
    public boolean isSsl() {
        return ssl;
    }

    @JsonCreator
    public EndPointConfiguration(
            @JsonProperty(ENDPOINT_PORT) int port,
            @JsonProperty(ENDPOINT_CERTIFICATE) String certificate,
            @JsonProperty(ENDPOINT_PASS) String pass,
            @JsonProperty(ENDPOINT_ENABLED) boolean enabled,
            @JsonProperty(ENDPOINT_SSL_ENABLED) boolean ssl) {
        this.port = port;
        this.certificate = certificate;
        this.pass = pass;
        this.enabled = enabled;
        this.ssl = ssl;
    }

    public EndPointConfiguration(Properties config) {
        this(FSAConfiguration.getIntProperty(config, ENDPOINT_PORT, DEFAULT_PORT),
                config.getProperty(ENDPOINT_CERTIFICATE) == null ? DEFAULT_CERTIFICATE : config.getProperty(ENDPOINT_CERTIFICATE),
                config.getProperty(ENDPOINT_PASS) == null ? DEFAULT_PASS : config.getProperty(ENDPOINT_PASS),
                FSAConfiguration.getBooleanProperty(config, ENDPOINT_ENABLED, DEFAULT_ENABLED),
                FSAConfiguration.getBooleanProperty(config, ENDPOINT_SSL_ENABLED, DEFAULT_SSL));
    }
}
