# client-testing

Building functional tests relying on the LionWeb client is difficult, as it requires running a
PostgreSQL database and the [LionWeb Server](https://github.com/LionWeb-io/lionweb-server). To make
it easier we created this module, which is used by the functional tests of the `client`,
`extensions` and `kotlin-client` modules in this project. We expect it will be useful to others
building applications based on the `client` module.

## Base classes

- **`AbstractClientFunctionalTest`** — uses Testcontainers to start a PostgreSQL container and a
  LionWeb Server container, and exposes them to the test. Containers are started once per
  LionWeb version and shared by all test classes in the same JVM. Requires Docker.
- **`AbstractClientInMemoryFunctionalTest`** — runs the same kind of tests against the
  `InMemoryServer` from the `client` module, without Docker.

The server image is built from `src/main/resources/clienttesting-lionweb-server-Dockerfile`. To
use an image built in advance instead, set the `LIONWEB_SERVER_TEST_IMAGE` environment variable to
its name.

## Dependency

```kotlin
dependencies {
    testImplementation("io.lionweb:lionweb-2024.1-client-testing:$lionwebVersion")
}
```

See [TESTING.md](../TESTING.md) for how these classes are used in this project.
