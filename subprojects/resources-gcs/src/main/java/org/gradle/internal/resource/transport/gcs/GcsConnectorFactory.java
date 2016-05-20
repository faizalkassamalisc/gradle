/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.resource.transport.gcs;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.gradle.authentication.Authentication;
import org.gradle.internal.authentication.AllSchemesAuthentication;
import org.gradle.internal.resource.connector.ResourceConnectorFactory;
import org.gradle.internal.resource.connector.ResourceConnectorSpecification;
import org.gradle.internal.resource.transfer.ExternalResourceConnector;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GcsConnectorFactory implements ResourceConnectorFactory {
    @Override
    public Set<String> getSupportedProtocols() {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        return Collections.singleton("gcs");
=======
        return Collections.singleton("s3");
>>>>>>> a1aedec... Add support for gcs backed artifact repository
=======
        return Collections.singleton("s3");
>>>>>>> a1aedec... Add support for gcs backed artifact repository
=======
        return Collections.singleton("s3");
>>>>>>> a1aedec... Add support for gcs backed artifact repository
    }

    @Override
    public Set<Class<? extends Authentication>> getSupportedAuthentication() {
        Set<Class<? extends Authentication>> supported = new HashSet<Class<? extends Authentication>>();
        supported.add(AllSchemesAuthentication.class);
        return supported;
    }

    @Override
    public ExternalResourceConnector createResourceConnector(ResourceConnectorSpecification connectionDetails) {
        GoogleCredential googleCredential;
        GcsResourceConnector resourceConnector;
        try {
            googleCredential = GoogleCredential.getApplicationDefault();
            resourceConnector = new GcsResourceConnector(new GcsClient(googleCredential));
        } catch (IOException e) {
            throw new IllegalArgumentException("Google Credentials must be set for GCS backed repository.", e);
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Google Credentials must be set for GCS backed repository.", gse);
        }

        return resourceConnector;
    }
}
