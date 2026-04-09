# Development Plan

This document tracks planned improvements to the lionweb-java project, organized by priority.
Items are executed roughly in order; check the checkbox when done.

---

## Priority 1 — CI/CD Hardening

- [x] **#1** Upgrade outdated GitHub Actions (`checkout@v5` → `checkout@v4`, `setup-java@v1/v2` → `setup-java@v4`) across all workflows; also update `distribution` from `adopt` to `temurin`
- [x] **#2** Enforce formatting in CI — add `./gradlew spotlessCheck` step to `check.yml`
- [x] **#3** Add `dependabot.yml` for automated Gradle and GitHub Actions dependency updates
- [x] **#4** Archive test results in CI — add `actions/upload-artifact@v4` for JUnit XML reports in all test workflows

## Priority 2 — Documentation

- [x] **#5** Create `CONTRIBUTING.md` with: dev setup, project structure, build/test commands, coding conventions, PR process
- [x] **#6** Create `SECURITY.md` with vulnerability reporting policy and response timeline
- [x] **#7** Add minimal READMEs to modules that were missing them: `emf`, `kotlin-core`, `kotlin-client`
- [x] **#8** Add build-status and coverage badges to root `README.md`
- [ ] **#9** Re-enable Javadoc linting (remove `-Xdoclint:none`) and fix warnings incrementally — requires iterative cleanup across all modules

## Priority 3 — Code Quality

- [ ] **#10** Audit 21+ TODO/FIXME comments — convert to GitHub Issues, remove resolved ones
  - Known locations: `core/` (13), `gradle-plugin/` (6), `emf/` (2)
  - Run `./gradlew grep` or `grep -r "TODO\|FIXME" --include="*.java" .` to list all
- [x] **#11** Add SpotBugs static analysis to the build (`config/spotbugs/exclude.xml` created; `ignoreFailures=true` initially — flip to `false` once known issues are resolved)
- [x] **#12** Add `.editorconfig` for IDE-agnostic formatting consistency
- [ ] **#13** Raise Jacoco coverage floors: measure actual coverage first, then set thresholds just below current level and raise incrementally toward 60% overall / 70% changed files

## Priority 4 — Build & Configuration

- [ ] **#14** Parameterize hardcoded MPS OpenAPI Javadoc URL (`2021.2`) in `core/build.gradle.kts` and `emf/build.gradle.kts` — verify whether a newer URL exists before changing
- [x] **#15** Resolve `.java-version` tracking conflict — removed from `.gitignore` so the committed file is no longer ignored
- [x] **#16** Enable Gradle local build cache (`org.gradle.caching=true` in `gradle.properties`)
- [x] **#17** Document JDK 1.8 drop migration plan and beyond-1.4.0 plans in `ROADMAP.md`

## Priority 5 — Testing

- [ ] **#18** Assess whether `client` and `emf` modules need integration tests
- [ ] **#19** Add PIT (Pitest) mutation testing as an optional/nightly Gradle task
- [ ] **#20** Add `japicmp` plugin to detect breaking API changes between releases (compare against last published version on Maven Central)
- [x] **#21** Add a scheduled GitHub Actions workflow (`performance.yml`) to run performance tests nightly and on-demand via `workflow_dispatch`
