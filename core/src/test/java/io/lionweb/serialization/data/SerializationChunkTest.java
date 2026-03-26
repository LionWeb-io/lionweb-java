package io.lionweb.serialization.data;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SerializationChunkTest {

  @Test
  public void serializationChunkEquality() {
    SerializationChunk c1 = new SerializationChunk();
    SerializationChunk c2 = new SerializationChunk();
    assertEquals(c1, c2);
    c1.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertNotEquals(c1, c2);
    c2.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertEquals(c1, c2);
  }

  // ========== Incremental language registration via addClassifierInstance ==========

  @Test
  public void addClassifierInstanceRegistersLanguageImmediately() {
    SerializationChunk chunk = new SerializationChunk();
    MetaPointer classifier = MetaPointer.get("lang-key", "1.0", "MyClass");
    SerializedClassifierInstance instance = new SerializedClassifierInstance("node-1", classifier);

    chunk.addClassifierInstance(instance);

    List<LanguageVersion> languages = chunk.getLanguages();
    assertEquals(1, languages.size());
    assertEquals("lang-key", languages.get(0).getKey());
    assertEquals("1.0", languages.get(0).getVersion());
  }

  @Test
  public void addClassifierInstanceDeduplicatesLanguages() {
    SerializationChunk chunk = new SerializationChunk();
    MetaPointer mp1 = MetaPointer.get("lang-key", "1.0", "ClassA");
    MetaPointer mp2 = MetaPointer.get("lang-key", "1.0", "ClassB");
    chunk.addClassifierInstance(new SerializedClassifierInstance("n1", mp1));
    chunk.addClassifierInstance(new SerializedClassifierInstance("n2", mp2));

    // Both nodes use the same language version — it should appear only once
    assertEquals(1, chunk.getLanguages().size());
  }

  @Test
  public void addClassifierInstanceRegistersPropertyAndContainmentLanguages() {
    SerializationChunk chunk = new SerializationChunk();
    MetaPointer classifier = MetaPointer.get("lang-A", "1.0", "MyClass");
    SerializedClassifierInstance instance = new SerializedClassifierInstance("n1", classifier);

    MetaPointer propMp = MetaPointer.get("lang-B", "2.0", "name");
    instance.unsafeAppendPropertyValue(SerializedPropertyValue.get(propMp, "hello"));

    chunk.addClassifierInstance(instance);

    List<LanguageVersion> languages = chunk.getLanguages();
    assertEquals(2, languages.size());
    assertTrue(languages.stream().anyMatch(lv -> "lang-A".equals(lv.getKey())));
    assertTrue(languages.stream().anyMatch(lv -> "lang-B".equals(lv.getKey())));
  }

  // ========== populateUsedLanguages idempotency ==========

  @Test
  public void populateUsedLanguagesIsNoOpForIncrementallyBuiltChunk() {
    SerializationChunk chunk = new SerializationChunk();
    MetaPointer mp = MetaPointer.get("lang-key", "1.0", "MyClass");
    chunk.addClassifierInstance(new SerializedClassifierInstance("n1", mp));

    List<LanguageVersion> beforePopulate = List.copyOf(chunk.getLanguages());
    chunk.populateUsedLanguages();
    List<LanguageVersion> afterPopulate = chunk.getLanguages();

    assertEquals(beforePopulate, afterPopulate);
  }

  // ========== Null / incomplete metapointers skipped gracefully ==========

  @Test
  public void addClassifierInstanceWithNullClassifierDoesNotThrow() {
    SerializationChunk chunk = new SerializationChunk();
    SerializedClassifierInstance instance = new SerializedClassifierInstance();
    instance.setID("n1");
    // classifier is null

    assertDoesNotThrow(() -> chunk.addClassifierInstance(instance));
    assertEquals(0, chunk.getLanguages().size());
  }

  @Test
  public void addClassifierInstanceWithIncompleteMetaPointerLanguageSkipped() {
    SerializationChunk chunk = new SerializationChunk();
    // MetaPointer with null language (incomplete)
    MetaPointer incompleteClassifier = MetaPointer.get(null, "1.0", "MyClass");
    SerializedClassifierInstance instance =
        new SerializedClassifierInstance("n1", incompleteClassifier);

    assertDoesNotThrow(() -> chunk.addClassifierInstance(instance));
    assertEquals(0, chunk.getLanguages().size());
  }

  // ========== concat path still requires explicit populateUsedLanguages ==========

  @Test
  public void concatDoesNotAutoRegisterLanguages() {
    SerializationChunk chunk = new SerializationChunk();
    MetaPointer mp = MetaPointer.get("lang-key", "1.0", "MyClass");
    SerializedClassifierInstance instance = new SerializedClassifierInstance("n1", mp);

    // Use concat() to bypass the incremental path
    chunk.concat(java.util.Collections.singletonList(instance));

    // Languages are NOT registered automatically via concat
    assertEquals(0, chunk.getLanguages().size());

    // But populateUsedLanguages fixes it
    chunk.populateUsedLanguages();
    assertEquals(1, chunk.getLanguages().size());
  }

  // ========== Unmodifiable view is live ==========

  @Test
  public void getClassifierInstancesViewReflectsSubsequentAdditions() {
    SerializationChunk chunk = new SerializationChunk();
    List<SerializedClassifierInstance> view = chunk.getClassifierInstances();
    assertEquals(0, view.size());

    MetaPointer mp = MetaPointer.get("lang-key", "1.0", "MyClass");
    chunk.addClassifierInstance(new SerializedClassifierInstance("n1", mp));

    // The view obtained before the add should still reflect the current state
    assertEquals(1, view.size());
  }

  @Test
  public void getLanguagesViewReflectsSubsequentAdditions() {
    SerializationChunk chunk = new SerializationChunk();
    List<LanguageVersion> view = chunk.getLanguages();
    assertEquals(0, view.size());

    chunk.addLanguage(LanguageVersion.of("lang-key", "1.0"));

    assertEquals(1, view.size());
  }

  // ========== addLanguage deduplication ==========

  @Test
  public void addLanguageDeduplicatesCanonicalInstances() {
    SerializationChunk chunk = new SerializationChunk();
    LanguageVersion lv = LanguageVersion.of("k", "1");
    chunk.addLanguage(lv);
    chunk.addLanguage(lv);

    assertEquals(1, chunk.getLanguages().size());
  }
}
