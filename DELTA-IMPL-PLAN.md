# Delta Protocol Implementation Plan

## Context

We are implementing the LionWeb Delta Protocol (v2026.1) for the LionWeb JVM library. The delta protocol
enables real-time synchronisation between multiple clients and an in-memory repository: clients send
commands (model mutations), the server validates and applies them, then broadcasts events to all
subscribed participants.

**What already exists:**
- All 108 message data-classes (commands, events, queries, responses) are fully defined.
- `InMemoryDeltaChannel` routes messages between clients and server in-process.
- `DeltaClient` monitors local node changes and forwards them as commands; handles a small subset of
  incoming events.
- `InMemoryServer` handles: `SignOnRequest`, `ChangeProperty`, `AddChild`, `DeleteChild`, `AddReference`.
- Five integration tests in `DeltaClientAndServerTest` exercise the already-implemented subset.

**What we are building:**
Working scenario-by-scenario (spec source: `DELTA-PROTOCOL-MESSAGE-INVENTORY.md`), we add one test per
scenario, then implement the minimal server + client logic required to make it pass.

---

## Implementation Waves

Each wave corresponds to a group of related spec scenarios. Within each wave, the work order is:

1. Add test(s) to `DeltaClientAndServerTest.java`.
2. Implement server-side handling in `InMemoryServer` (inner `DeltaQueryReceiverImpl` /
   `DeltaCommandReceiverImpl`).
3. Implement client-side handling in `DeltaClient` (`receiveEvent` / `receiveQueryResponse`).
4. Run `./gradlew :client:test` and confirm the new test(s) pass without breaking existing ones.

---

### Wave 1 — Session / Connection Management (Scenarios 1, 5, 6)

**Spec scenarios:** Start participation · Reconnect participation · End participation

| # | Scenario | Test method name | Server work | Client work |
|---|---|---|---|---|
| 1 | Start participation (already works) | `startParticipation` | Verify `SignOnRequest → SignOnResponse` | Already handled |
| 5 | Reconnect participation | `reconnectParticipation` | Handle `ReconnectRequest` → `ReconnectResponse` (store last sequence number, emit `ReconnectResponse` with `lastSentSequenceNumber`) | Handle `ReconnectResponse` in `receiveQueryResponse` |
| 6 | End participation | `endParticipation` | Handle `SignOffRequest` → `SignOffResponse` (remove participation) | Handle `SignOffResponse` in `receiveQueryResponse`; no further events |

**Key files:**
- `client/src/main/java/io/lionweb/client/inmemory/InMemoryServer.java` — add to `DeltaQueryReceiverImpl.receiveQuery`
- `client/src/main/java/io/lionweb/client/delta/DeltaClient.java` — add to `receiveQueryResponse`

---

### Wave 2 — Partition Management (Scenarios 2, 3, and DeletePartition)

**Spec scenarios:** List-and-subscribe partitions · Create partition · Delete partition

| # | Scenario | Test method name | Server work | Client work |
|---|---|---|---|---|
| 2 | List and subscribe all partitions | `listAndSubscribePartitions` | Handle `ListAndSubscribePartitionsRequest` → `ListAndSubscribePartitionsResponse` (serialize all existing partitions; record subscription for each) | Handle `ListAndSubscribePartitionsResponse` |
| 3 | Create partition | `createPartition` | Handle `AddPartition` command → persist node tree → broadcast `PartitionAdded` to subscribed clients | Handle `PartitionAdded` event: deserialize and track new partition nodes |
| — | Delete partition | `deletePartition` | Handle `DeletePartition` command → remove nodes → broadcast `PartitionDeleted` | Handle `PartitionDeleted` event: remove local tracking |

**Supporting changes:**
- `InMemoryServer` needs a per-participation subscription set (`Map<String, Set<String>>
  participationSubscriptions`).
- `ListPartitionsRequest` / `ListPartitionsResponse` is a simpler variant of the above (no subscription);
  add test `listPartitions` as well.
- `SubscribeToPartitionContentsRequest` / `UnsubscribeFromPartitionContentsRequest` round-trips get their
  own small tests.

---

### Wave 3 — Property, Reference, and Classifier Mutations

Extends the already-implemented property-change scenarios to the full property lifecycle and remaining
reference operations.

| Scenario | Test method name | Command | Event |
|---|---|---|---|
| Add property | `addProperty` | `AddProperty` | `PropertyAdded` |
| Delete property | `deleteProperty` | `DeleteProperty` | `PropertyDeleted` |
| Change reference | `changeReference` | `ChangeReference` | `ReferenceChanged` |
| Delete reference | `deleteReference` | `DeleteReference` | `ReferenceDeleted` |
| Change classifier | `changeClassifier` | `ChangeClassifier` | `ClassifierChanged` |

Each test follows the same pattern as the existing `simpleSynchronizationOfNodesInstances` test: two
clients observe the same partition; one makes a change; we assert the other sees it.

---

### Wave 4 — Child Move Operations

| Scenario | Test method name | Command | Event |
|---|---|---|---|
| Move child in same containment | `moveChildInSameContainment` | `MoveChildInSameContainment` | `ChildMovedInSameContainment` |
| Move child from other containment | `moveChildFromOtherContainment` | `MoveChildFromOtherContainment` | `ChildMovedFromOtherContainment` |
| Replace child | `replaceChild` | `ReplaceChild` | `ChildReplaced` |

---

### Wave 5 — Error and Edge-Case Scenarios (Scenarios 7–12)

| # | Scenario | Test method name | Work required |
|---|---|---|---|
| 7 | Invalid participation on query | `queryWithInvalidParticipation` | Server sends `ErrorResponse` with `invalidParticipation` code when `participationId` unknown |
| 8 | Invalid node id format | `commandWithInvalidNodeId` | Server validates node-id syntax; sends `ErrorEvent` with `invalidNodeId` code |
| 9 | Unknown node in command | `changingUnexistingNodeCauseError` | Already implemented; verify existing test is sufficient |
| 10 | Repository internal failure | `internalErrorDuringCommand` | Server catches unexpected exceptions; sends `ErrorEvent` with `internalError` |
| 12 | No-op update | `noOpPropertyChange` | Server detects old==new; broadcasts `NoOp` event instead of `PropertyChanged`; client receives `NoOp` |
| 13 | Command outside subscription scope | `commandOutsideSubscriptionScope` | Sender gets no event back if not subscribed; other subscribed clients still receive the event |

---

### Wave 6 — Annotation Operations

| Scenario | Test method name | Command | Event |
|---|---|---|---|
| Add annotation | `addAnnotation` | `AddAnnotation` | `AnnotationAdded` |
| Delete annotation | `deleteAnnotation` | `DeleteAnnotation` | `AnnotationDeleted` |
| Move annotation (same parent) | `moveAnnotationInSameParent` | `MoveAnnotationInSameParent` | `AnnotationMovedInSameParent` |

---

### Wave 7 — CompositeCommand / CompositeEvent

| Scenario | Test method name | Notes |
|---|---|---|
| Composite command | `compositeCommand` | Client sends `CompositeCommand` wrapping several property + child mutations; server applies atomically; broadcasts `CompositeEvent` |

---

### Wave 8 — Split-Chunk Protocol (Scenario 15, deferred)

Low priority; requires `ContinuedCommand` / `ContinuedQueryResponse` / `ContinuedEvent` handling. Defer
until all other waves are green.

---

## Test File Location

`client/src/test/java/io/lionweb/client/delta/DeltaClientAndServerTest.java`

All new tests are added here with a Javadoc comment citing the scenario number and spec section.

---

## Critical Files to Modify

| File | Changes |
|---|---|
| `client/src/main/java/io/lionweb/client/inmemory/InMemoryServer.java` | Add command handlers and query handlers wave by wave |
| `client/src/main/java/io/lionweb/client/delta/DeltaClient.java` | Add event handlers and query-response handlers wave by wave |
| `client/src/test/java/io/lionweb/client/delta/DeltaClientAndServerTest.java` | Add one test per scenario, with comment |

Avoid modifying message data classes unless a bug is found; they are complete.

---

## Verification

After each wave:
```
./gradlew :client:test --tests "io.lionweb.client.delta.DeltaClientAndServerTest"
```

Full build after each wave:
```
./gradlew build
```

For scenarios that involve querying server state (e.g., verifying a partition was actually stored),
use `server.retrieveAsClassifierInstance(...)` directly (as existing tests do) rather than only
relying on client-side view.

---

## Progress Tracker

- [x] Wave 1 — Session/Connection (scenarios 1, 5, 6)
- [x] Wave 2 — Partition Management (scenarios 2, 3, DeletePartition)
- [x] Wave 3 — Property/Reference/Classifier Mutations
- [x] Wave 4 — Child Move Operations
- [ ] Wave 5 — Error and Edge-Case Scenarios (scenarios 7–12)
- [x] Wave 6 — Annotation Operations
- [ ] Wave 7 — CompositeCommand/CompositeEvent
- [ ] Wave 8 — Split-Chunk Protocol (deferred)

---

## Already Implemented in DeltaClientAndServerTest.java

The following scenarios already have passing tests:

| Test method | Covers | Commands/Events used |
|---|---|---|
| `simpleSynchronizationOfNodesInstances` | Two clients see each other's property mutations | `ChangeProperty` → `PropertyChanged` |
| `changingUnexistingNodeCauseError` | Error scenario 9 (unknown node) | `ChangeProperty` → `ErrorEvent(unknownNode)` |
| `addingChildren` | Child-add synchronisation between two clients | `AddChild` → `ChildAdded` |
| `removingChildren` | Child-delete synchronisation between two clients | `DeleteChild` → `ChildDeleted` |
| `variousOperations` | Large smoke test covering many language-modelling operations (property changes, child add/remove, reference add, interface hierarchy, concept inheritance) | Mix of `ChangeProperty`, `AddChild`, `DeleteChild`, `AddReference` and their corresponding events |

Wave 3 (property mutations) is **partially** covered by `simpleSynchronizationOfNodesInstances`
(`ChangeProperty`) and `variousOperations`. Still needed: `AddProperty`, `DeleteProperty`,
`ChangeReference`, `DeleteReference`, `ChangeClassifier`.
