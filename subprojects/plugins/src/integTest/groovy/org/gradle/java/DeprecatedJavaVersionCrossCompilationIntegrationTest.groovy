/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.java

import org.gradle.api.JavaVersion
import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetVersions
import org.gradle.internal.jvm.JavaInfo
import org.gradle.util.TextUtil
import org.junit.Assume

@TargetVersions(["1.5"])
class DeprecatedJavaVersionCrossCompilationIntegrationTest extends MultiVersionIntegrationSpec {
    JavaVersion getJavaVersion() {
        JavaVersion.toVersion(version)
    }

    JavaInfo getTarget() {
        return AvailableJavaHomes.getJdk(javaVersion)
    }

    def setup() {
        Assume.assumeTrue(target != null)
        def java = TextUtil.escapeString(target.getJavaExecutable())
        def javac = TextUtil.escapeString(target.getExecutable("javac"))
        def javadoc = TextUtil.escapeString(target.getExecutable("javadoc"))

        buildFile << """
apply plugin: 'java'
sourceCompatibility = ${version}
targetCompatibility = ${version}
repositories { mavenCentral() }
tasks.withType(JavaCompile) {
    options.with {
        fork = true
        forkOptions.executable = "$javac"
    }
}
tasks.withType(Javadoc) {
    executable = "$javadoc"
}
tasks.withType(Test) {
    executable = "$java"
}
"""

        file("src/main/java/Thing.java") << """
/** Some thing. */
public class Thing { }
"""
    }

    def "can compile source and run tests using target Java version"() {
        given:
        buildFile << """
dependencies { testCompile 'junit:junit:4.12' }
"""

        file("src/test/java/ThingTest.java") << """
import org.junit.Test;
import static org.junit.Assert.*;

public class ThingTest {
    @Test
    public void verify() {
        assertTrue(System.getProperty("java.version").startsWith("${version}."));
    }
}
"""
        executer.expectDeprecationWarning()

        expect:
        succeeds 'test'
        output.contains("Support for test execution using Java 5 or earlier has been deprecated and is scheduled to be removed in Gradle ")
    }
}
