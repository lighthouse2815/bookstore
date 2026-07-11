package com.bookstore.bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

/** Ensures the dedicated MySQL profile cannot report success after skipping all container tests. */
class DockerAvailabilityMySqlIT {

    @Test
    void testcontainersProfileRequiresAnAvailableDockerDaemon() {
        assertTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "The testcontainers Maven profile requires a reachable Docker daemon. "
                        + "Start Docker Desktop, then rerun .\\mvnw.cmd clean -Ptestcontainers verify. "
                        + "Use .\\mvnw.cmd test when only the non-container test suite is intended."
        );
    }
}
