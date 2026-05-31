# Delta Protocol Integration Test Scenarios

This document defines integration test scenarios for the LionWeb delta protocol. Each scenario
involves one server and two clients (Client1 and Client2) connected via an `InMemoryDeltaChannel`.

The scenarios verify that a command issued from Client1 causes:
1. The server to reach the correct internal state.
2. Client2 to eventually reflect the correct state in its local model object.

## Infrastructure and Conventions

### Setup pattern (shared by all scenarios)

All scenarios start from this base setup unless stated otherwise:

```
server  = new InMemoryServer()
server.createRepository(new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED))
channel = new InMemoryDeltaChannel()
server.monitorDeltaChannel("MyRepo", channel)
client1 = new DeltaClient(channel, "client-1")
client2 = new DeltaClient(channel, "client-2")
client1.sendSignOnRequest()
client2.sendSignOnRequest()
```

### Language / model used

All scenarios use the LionWeb built-in `Language` / `Concept` meta-model as model objects because
it is always available in the classpath, contains properties (`name`), containments
(`elements` → list of `LanguageEntity`), and references (`extendedConcept` on a `Concept`).

Where annotation scenarios need a custom annotation classifier, a minimal `Annotation` named
`"Comment"` is declared in a helper language and pre-registered on both clients via
`client.registerLanguage(...)`.

### Monitoring vs subscribing

`client.monitorPartition(node)` sets up a local observer that automatically fires delta commands
whenever the Java object is mutated (property change, child add/remove, etc.). Both Client1 and
Client2 call `monitorPartition` on their respective copies of the same partition so that incoming
events are applied back to their local object.

### Pre-loading a partition

When a partition must exist before clients connect, create it directly on the server:

```
Language partition = new Language("MyLang", "my-lang-id", "my-lang-key")
server.createPartition("MyRepo", partition, serialization())
```

Then retrieve a second independent copy for Client2:

```
Language partitionForClient2 = (Language) server.retrieveAsClassifierInstance("MyRepo", "my-lang-id", serialization())
```

### Assertions

Every scenario ends with two layers of assertions:

- **Client2 local model** – the Java object held by Client2 has the expected structure/values.
- **Server state** – `server.retrieve("MyRepo", List.of(id), Integer.MAX_VALUE)` or
  `server.listPartitionIDs("MyRepo")` returns the expected data.

---

## Category 1 – Partition Management

### Scenario P-1: Client1 creates a partition; Client2 is notified

**Goal:** Verify that `AddPartition` propagates correctly.

**Setup:**
- Base setup (no pre-existing partitions).
- Client2 calls `sendListAndSubscribePartitionsRequest()` so it subscribes to partition-lifecycle
  events.

**Action (Client1):**
```
Language newLang = new Language("NewLang", "new-lang-id", "new-lang-key")
client1.sendAddPartitionCommand(newLang)
```

**Assertions:**
- Server: `server.listPartitionIDs("MyRepo")` contains `"new-lang-id"`.
- Server: `server.retrieveAsClassifierInstance("MyRepo", "new-lang-id", serialization())` is not null.
- Client2: no `ErrorEventReceivedException` was thrown (the `PartitionAdded` event was handled).

---

### Scenario P-2: Client1 deletes a partition; Client2 is notified

**Goal:** Verify that `DeletePartition` propagates correctly.

**Setup:**
- Base setup plus `Language` partition pre-created directly on the server with id `"my-lang-id"`.
- Client2 calls `sendListAndSubscribePartitionsRequest()`.

**Action (Client1):**
```
client1.sendDeletePartitionCommand("my-lang-id")
```

**Assertions:**
- Server: `server.listPartitionIDs("MyRepo")` does **not** contain `"my-lang-id"`.
- Client2: no `ErrorEventReceivedException` was thrown (the `PartitionDeleted` event was handled).

---

## Category 2 – Node Creation and Deletion

### Scenario N-1: Client1 adds a child node; Client2 sees it

**Goal:** Verify that adding a child node fires `AddChild` and both clients converge.

**Setup:**
- Base setup plus `Language` partition pre-created on the server with id `"my-lang-id"` and **no
  elements**.
- Both clients retrieve their own copy of the partition and call `monitorPartition`.

**Action (Client1):**
```
Concept concept = new Concept(partitionOnClient1, "MyConcept", "my-concept-id", "my-concept-key")
partitionOnClient1.addElement(concept)   // triggers AddChild via observer
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getElements()` has size 1 and the element id is
  `"my-concept-id"`.
- Client2 local model: `partitionOnClient2.getElements()` has size 1 and the element id is
  `"my-concept-id"`.
- Server: `server.retrieve("MyRepo", List.of("my-lang-id"), Integer.MAX_VALUE)` contains a node
  with id `"my-concept-id"`.

---

### Scenario N-2: Client1 deletes a child node; Client2 sees it removed

**Goal:** Verify that removing a child fires `DeleteChild` and both clients converge.

**Setup:**
- Base setup plus `Language` partition pre-created on the server with id `"my-lang-id"` containing
  one `Concept` with id `"my-concept-id"`.
- Both clients retrieve their own copy and call `monitorPartition`.

**Action (Client1):**
```
Concept concept = (Concept) partitionOnClient1.getElements().get(0)
partitionOnClient1.removeElement(concept)   // triggers DeleteChild via observer
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getElements()` is empty.
- Client2 local model: `partitionOnClient2.getElements()` is empty.
- Server: `server.retrieve("MyRepo", List.of("my-lang-id"), Integer.MAX_VALUE)` contains no node
  with id `"my-concept-id"`.

---

## Category 3 – Properties

### Scenario PR-1: Client1 sets a property from null to a value (AddProperty)

**Goal:** Verify `AddProperty` command and `PropertyAdded` event propagation.

**Setup:**
- Base setup plus a `Language` partition pre-created on the server with its `name` set to `null`
  (pass `null` to the `Language` constructor or clear it before `createPartition`).
- Both clients retrieve their copy and call `monitorPartition`.

**Precondition check:** `partitionOnClient1.getName() == null` and
`partitionOnClient2.getName() == null`.

**Action (Client1):**
```
partitionOnClient1.setName("Hello")
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getName()` equals `"Hello"`.
- Client2 local model: `partitionOnClient2.getName()` equals `"Hello"`.
- Server: the retrieved `SerializedClassifierInstance` for `"my-lang-id"` contains a property entry
  for `name` with value `"Hello"`.

---

### Scenario PR-2: Client1 changes an existing property value (ChangeProperty)

**Goal:** Verify `ChangeProperty` command and `PropertyChanged` event propagation.

**Setup:**
- Base setup plus a `Language` partition pre-created with `name = "OldName"`.
- Both clients retrieve their copy and call `monitorPartition`.

**Precondition check:** `partitionOnClient1.getName()` equals `"OldName"`.

**Action (Client1):**
```
partitionOnClient1.setName("NewName")
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getName()` equals `"NewName"`.
- Client2 local model: `partitionOnClient2.getName()` equals `"NewName"`.
- Server: property value for `name` is `"NewName"`.

---

### Scenario PR-3: Client1 clears a property to null (DeleteProperty)

**Goal:** Verify `DeleteProperty` command and `PropertyDeleted` event propagation.

**Setup:**
- Base setup plus a `Language` partition pre-created with `name = "SomeName"`.
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
partitionOnClient1.setName(null)
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getName()` is `null`.
- Client2 local model: `partitionOnClient2.getName()` is `null`.
- Server: the property entry for `name` is absent or null.

---

## Category 4 – Containments

### Scenario C-1: Client1 adds a child at a specific index (AddChild)

**Goal:** Verify that a child added at a given index lands at that index on both clients and server.

**Setup:**
- Base setup plus a `Language` partition pre-created with two elements: `Concept` ids
  `"concept-a"` and `"concept-b"` (in that order).
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
Concept concept = new Concept(partitionOnClient1, "ConceptC", "concept-c", "concept-c-key")
partitionOnClient1.addElement(concept)   // appends at end → index 2
```

**Assertions:**
- Client1 local model: elements list is `["concept-a", "concept-b", "concept-c"]`.
- Client2 local model: elements list is `["concept-a", "concept-b", "concept-c"]`.
- Server: retrieved nodes include `"concept-c"` and the `elements` children list has size 3 with
  `"concept-c"` last.

---

### Scenario C-2: Client1 deletes a child at a given index (DeleteChild)

**Goal:** Verify that removing a specific child propagates the correct index to both clients and
the server no longer stores the node.

**Setup:**
- Base setup plus a `Language` partition with elements `["concept-a", "concept-b", "concept-c"]`.
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
Concept conceptB = (Concept) partitionOnClient1.getElements().get(1)   // "concept-b"
partitionOnClient1.removeElement(conceptB)
```

**Assertions:**
- Client1 local model: elements list is `["concept-a", "concept-c"]`.
- Client2 local model: elements list is `["concept-a", "concept-c"]`.
- Server: `"concept-b"` is no longer present in the retrieved nodes; elements list has size 2.

---

### Scenario C-3: Client1 moves a child within the same containment (MoveChildInSameContainment)

**Goal:** Verify that reordering a child within its containment is reflected on Client2 and the
server.

**Setup:**
- Base setup plus a `Language` partition with elements `["concept-a", "concept-b", "concept-c"]`.
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
// Move "concept-a" from index 0 to index 2
client1.sendMoveChildInSameContainmentCommand(
    "my-lang-id",            // parent id
    <elements MetaPointer>,  // containment MetaPointer for Language.elements
    "concept-a",             // moved child id
    0,                       // old index
    2                        // new index
)
```

**Assertions:**
- Server: elements order is `["concept-b", "concept-c", "concept-a"]`.
- Client2 local model: elements order is `["concept-b", "concept-c", "concept-a"]`.

> **Note on MetaPointer for elements:** use the MetaPointer that identifies the `elements`
> containment on the `Language` concept (language id `io.lionweb.language`, version `1`, key
> `Language-elements`).

---

## Category 5 – References

### Scenario R-1: Client1 sets a reference from null to a target (AddReference)

**Goal:** Verify that adding a reference entry fires `AddReference` / `ReferenceAdded` and
propagates to Client2 and the server.

**Setup:**
- Base setup plus a `Language` partition with two concepts: `"concept-a"` (no extended concept)
  and `"concept-b"`.
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
Concept conceptA = ...  // look up in partitionOnClient1
Concept conceptB = ...  // look up in partitionOnClient1
conceptA.setExtendedConcept(conceptB)
```

**Assertions:**
- Client1 local model: `conceptA.getExtendedConcept().getID()` equals `"concept-b"`.
- Client2 local model: the `extends` reference values on `"concept-a"` in Client2's partition have
  size 1 and `getReferredID()` equals `"concept-b"`.
- Server: the `extends` reference value on the serialized `"concept-a"` node has referred id
  `"concept-b"`.

---

### Scenario R-2: Client1 changes an existing reference target (ChangeReference)

**Goal:** Verify that replacing a reference target fires `ChangeReference` / `ReferenceChanged`.

**Setup:**
- Base setup plus a `Language` partition with three concepts: `"concept-a"` (extends
  `"concept-b"`), `"concept-b"`, and `"concept-c"`.
- Both clients retrieve their copy and call `monitorPartition`.

**Precondition check:** `extends` reference on `"concept-a"` points to `"concept-b"`.

**Action (Client1):**
```
conceptA.setExtendedConcept(conceptC)   // changes existing reference
```

**Assertions:**
- Client1 local model: `conceptA.getExtendedConcept().getID()` equals `"concept-c"`.
- Client2 local model: `extends` reference values on `"concept-a"` have size 1 and
  `getReferredID()` equals `"concept-c"`.
- Server: `extends` reference for `"concept-a"` is `"concept-c"`.

---

### Scenario R-3: Client1 removes a reference (DeleteReference)

**Goal:** Verify that clearing a reference fires `DeleteReference` / `ReferenceDeleted`.

**Setup:**
- Base setup plus a `Language` partition with concepts `"concept-a"` (extends `"concept-b"`) and
  `"concept-b"`.
- Both clients retrieve their copy and call `monitorPartition`.

**Action (Client1):**
```
conceptA.setExtendedConcept(null)
```

**Assertions:**
- Client1 local model: `conceptA.getExtendedConcept()` is `null`.
- Client2 local model: `extends` reference values on `"concept-a"` are empty.
- Server: `extends` reference list for `"concept-a"` is empty.

---

## Category 6 – Annotations

### Shared annotation language

All annotation scenarios declare these helpers (can be static fields on the test class):

```java
Language annLang = new Language("AnnTestLang", "ann-test-lang", "ann-test-lang-key");
Annotation commentAnn = new Annotation(annLang, "Comment", "comment-ann-id", "comment-ann-key");
```

Both clients register `annLang` via `client.registerLanguage(annLang)` before signing on or
immediately after.

---

### Scenario A-1: Client1 adds an annotation; Client2 sees it

**Goal:** Verify that `AddAnnotation` / `AnnotationAdded` propagates.

**Setup:**
- Base setup plus a `Language` partition with id `"my-lang-id"` (no annotations).
- Both clients retrieve their copy, register `annLang`, and call `monitorPartition`.

**Action (Client1):**
```
DynamicAnnotationInstance ann = new DynamicAnnotationInstance("ann-1", commentAnn)
partitionOnClient1.addAnnotation(ann)
```

**Assertions:**
- Client1 local model: `partitionOnClient1.getAnnotations()` has size 1 with id `"ann-1"`.
- Client2 local model: `partitionOnClient2.getAnnotations()` has size 1 with id `"ann-1"`.
- Server: `server.retrieve("MyRepo", List.of("my-lang-id"), Integer.MAX_VALUE)` includes a node
  with id `"ann-1"` and the root node's annotations list contains `"ann-1"`.

---

### Scenario A-2: Client1 removes an annotation; Client2 sees it removed

**Goal:** Verify that `DeleteAnnotation` / `AnnotationDeleted` propagates.

**Setup:**
- Base setup plus a `Language` partition pre-created with annotation `"ann-1"` already attached.

  Since `createPartition` does not take annotation setup, add the annotation **after** creating
  the partition: create the partition, then immediately add the annotation via client1 (which has
  already called `monitorPartition`), let it sync to Client2, and then perform the delete.

  Alternative (simpler): add the annotation in the same test through Client1 first, verify it
  propagated, then delete it.

- Both clients retrieve their copy, register `annLang`, and call `monitorPartition`.

**Action (Client1):**
```
// First add (reuse Scenario A-1 setup / actions to reach steady state)
DynamicAnnotationInstance ann = new DynamicAnnotationInstance("ann-1", commentAnn)
partitionOnClient1.addAnnotation(ann)
// ... assert both sides see it (intermediate check optional) ...

// Then delete
partitionOnClient1.removeAnnotation(ann)
```

**Assertions (after delete):**
- Client1 local model: `partitionOnClient1.getAnnotations()` is empty.
- Client2 local model: `partitionOnClient2.getAnnotations()` is empty.
- Server: no node with id `"ann-1"` in `server.retrieve("MyRepo", List.of("my-lang-id"), Integer.MAX_VALUE)`.

---

### Scenario A-3: Client1 moves an annotation within the same parent (MoveAnnotationInSameParent)

**Goal:** Verify that `MoveAnnotationInSameParent` / `AnnotationMovedInSameParent` reorders the
server-side annotation list.

**Setup:**
- Base setup plus a `Language` partition pre-loaded with annotations
  `["ann-1", "ann-2", "ann-3"]` (add them in sequence via a single monitoring client, let them
  sync, then set up a second client for the verification).

**Action (Client1):**
```
client1.sendMoveAnnotationInSameParentCommand("my-lang-id", "ann-1", 0, 2)
// moves ann-1 from index 0 to index 2 → expected order: [ann-2, ann-3, ann-1]
```

**Assertions:**
- Server: annotations list on `"my-lang-id"` is `["ann-2", "ann-3", "ann-1"]`.
- Client2 local model: `partitionOnClient2.getAnnotations()` has size 3 with the annotation at
  index 0 having id `"ann-2"`, index 1 `"ann-3"`, index 2 `"ann-1"`.

  > **Implementation note:** `ClassifierInstance` may not expose ordered annotation retrieval
  > directly. In that case verify only the server-side ordering (as done in the existing
  > `DeltaAnnotationsTest.moveAnnotationInSameParent`).

---

## Implementation notes for the test author

1. **Test class location:** `client/src/test/java/io/lionweb/client/delta/`; extend
   `AbstractDeltaProtocolTest`.
2. **Synchronous delivery:** `InMemoryDeltaChannel` delivers events synchronously. There is no need
   for `Thread.sleep` or polling; assertions can be placed immediately after the triggering action.
3. **MetaPointer construction:** use
   `MetaPointer.get(languageId, languageVersion, featureKey)` or the static factory methods
   already used in production code. For the built-in `Language.elements` containment use the
   values visible in the LionWeb meta-language definition.
4. **Retrieving nodes from server:** `server.retrieve(repoName, ids, depthLimit)` returns a
   `List<SerializedClassifierInstance>`; use `Integer.MAX_VALUE` as depth to get all descendants.
5. **Multiple children ordering:** after every `addElement` / `removeElement` call, the local list
   on the Client1 object is updated immediately (Java model mutation); Client2's list is updated
   via the incoming event handler.
6. **Reference values on Client2:** because Client2 may hold proxy nodes, access references via
   `node.getReferenceValues(feature)` rather than the typed getter (which casts to a concrete
   class).
