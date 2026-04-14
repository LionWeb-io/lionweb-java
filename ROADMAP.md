# Roadmap

## 1.4.0 (planned)

- **Drop JDK 1.8 support** — the project currently targets Java 8 as the minimum runtime to
  maximise compatibility. In 1.4.0 the minimum will be raised to **Java 11**.

  Migration steps for consumers:
  1. Ensure your build toolchain targets Java 11 or higher.
  2. Remove any `sourceCompatibility = JavaVersion.VERSION_1_8` overrides in your own builds.
  3. Watch for use of APIs that were deprecated in Java 9+ (e.g. `sun.*` internals) — these will
     no longer be available.

- **Complete Delta protocol support** — implement the remaining operations defined in the
  LionWeb Delta protocol specification so that incremental model synchronisation is fully
  supported.

- **Improve documentation** — expand Javadoc coverage, add more worked examples in
  `docs-examples`, and publish tutorials alongside the API reference.

## Beyond 1.4.0

- Investigate Kotlin Multiplatform support to share core model logic with JS/WASM targets.
- Explore a reactive (Flow/coroutine-based) variant of the client API.
