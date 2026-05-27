package io.lionweb.gradleplugin;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.gradleplugin.generators.LanguageJavaCodeGenerator;
import io.lionweb.gradleplugin.generators.NodeClassesJavaCodeGenerator;
import io.lionweb.language.*;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.TopologicalLanguageSorter;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class NodeClassesJavaCodeGeneratorTest extends AbstractGeneratorTest {

  private static final String PACK = "test.gen";
  private static final LionWebVersion LW_VERSION_USED_IN_TESTS = LionWebVersion.v2023_1;

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private Language buildLanguage(String name, Concept... concepts) {
    Language language = new Language(LW_VERSION_USED_IN_TESTS, name);
    language.setVersion("v1");
    for (Concept c : concepts) {
      language.addElement(c);
    }
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    return language;
  }

  private String readGeneratedClass(File dir, String pkg, String className) throws IOException {
    File file = new File(dir, pkg.replace('.', '/') + "/" + className + ".java");
    assertTrue(file.exists(), "Expected generated file: " + file.getPath());
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
  }

  private void generateBothLanguageAndNodeClasses(File dir, Language language) throws IOException {
    new LanguageJavaCodeGenerator(dir).generate(language, PACK);
    new NodeClassesJavaCodeGenerator(dir).generate(language, PACK);
  }

  @Test
  public void testStarlasuSpecsGeneration() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator languagesGenerator = new LanguageJavaCodeGenerator(destination);
    NodeClassesJavaCodeGenerator nodeClassesGenerator =
        new NodeClassesJavaCodeGenerator(destination);

    Set<String> paths =
        new HashSet<>(
            Arrays.asList(
                "/ast.language.v1.json",
                "/ast.language.v2.json",
                "/codebase.language.v1.json",
                "/codebase.language.v2.json",
                "/comments.language.v1.json",
                "/migration.language.v1.json",
                "/pipeline.language.v1.json"));
    Set<SerializationChunk> chunks =
        paths.stream()
            .map(
                path -> {
                  try {
                    String json = read(this.getClass().getResourceAsStream(path));
                    return new LowLevelJsonSerialization().deserializeSerializationBlock(json);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toSet());
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LW_VERSION_USED_IN_TESTS);
    Set<Language> languages =
        new TopologicalLanguageSorter(LW_VERSION_USED_IN_TESTS)
            .topologicalSort(chunks).stream()
                .map(
                    chunk -> {
                      Language language =
                          (Language)
                              serialization.deserializeSerializationChunk(chunk).stream()
                                  .filter(n -> n.getParent() == null)
                                  .findFirst()
                                  .get();
                      serialization.registerLanguage(language);
                      return language;
                    })
                .collect(Collectors.toSet());
    Map<String, String> primitiveTypes = new HashMap<>();
    primitiveTypes.put(
        "com-strumenta-StarLasu-TokensList-id", "dummy.com.strumenta.starlasu.TokensList");
    primitiveTypes.put(
        "com-strumenta-Starlasu-v2-TokensList-2-id", "dummy.com.strumenta.starlasu.TokensList");
    primitiveTypes.put(
        "com-strumenta-StarLasu-Position-id", "dummy.com.strumenta.starlasu.Position");
    primitiveTypes.put(
        "com-strumenta-Starlasu-v2-Position-2-id", "dummy.com.strumenta.starlasu.Position");
    languagesGenerator.generate(languages, "my.pack");
    nodeClassesGenerator.generate(
        languages, "my.pack", Collections.emptyMap(), primitiveTypes, Collections.emptyMap());
    assertTrue(compileAllJavaFiles(destination));
  }

  // ---------------------------------------------------------------------------
  // Concept inheritance
  // ---------------------------------------------------------------------------

  @Test
  public void testConceptInheritanceExtendsParentClass() throws IOException {
    Concept animal = new Concept(LW_VERSION_USED_IN_TESTS, "Animal");
    animal.addProperty(
        "name", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    Concept dog = new Concept(LW_VERSION_USED_IN_TESTS, "Dog");
    dog.setExtendedConcept(animal);

    File dir = Files.createTempDirectory("gen-inheritance").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Animals", animal, dog));

    String dogSrc = readGeneratedClass(dir, PACK, "Dog");
    assertTrue(dogSrc.contains("extends Animal"), "Dog should extend Animal");
  }

  @Test
  public void testExtendedConceptConstructorCallsSuper() throws IOException {
    Concept base = new Concept(LW_VERSION_USED_IN_TESTS, "Base");
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Child");
    child.setExtendedConcept(base);

    File dir = Files.createTempDirectory("gen-super-ctor").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Hierarchy", base, child));

    String childSrc = readGeneratedClass(dir, PACK, "Child");
    assertTrue(childSrc.contains("super(id)"), "Child constructor should call super(id)");
  }

  @Test
  public void testExtendedConceptDelegatesToSuperForPropertyValue() throws IOException {
    Concept base = new Concept(LW_VERSION_USED_IN_TESTS, "Base");
    base.addProperty(
        "label", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Child");
    child.setExtendedConcept(base);

    File dir = Files.createTempDirectory("gen-super-prop").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Props", base, child));

    String childSrc = readGeneratedClass(dir, PACK, "Child");
    assertTrue(
        childSrc.contains("return super.getPropertyValue(property)"),
        "Child.getPropertyValue should delegate to super");
    assertTrue(
        childSrc.contains("super.setPropertyValue(property, value)"),
        "Child.setPropertyValue should delegate to super");
  }

  @Test
  public void testExtendedConceptDelegatesToSuperForChildren() throws IOException {
    Concept base = new Concept(LW_VERSION_USED_IN_TESTS, "Base");
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Child");
    child.setExtendedConcept(base);

    File dir = Files.createTempDirectory("gen-super-children").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("ContHier", base, child));

    String childSrc = readGeneratedClass(dir, PACK, "Child");
    assertTrue(
        childSrc.contains("return super.getChildren(containment)"),
        "Child.getChildren should delegate to super");
    assertTrue(
        childSrc.contains("super.addChild(containment, child)"),
        "Child.addChild should delegate to super");
  }

  @Test
  public void testExtendedConceptDelegatesToSuperForReferences() throws IOException {
    Concept base = new Concept(LW_VERSION_USED_IN_TESTS, "Base");
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Child");
    child.setExtendedConcept(base);

    File dir = Files.createTempDirectory("gen-super-refs").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("RefHier", base, child));

    String childSrc = readGeneratedClass(dir, PACK, "Child");
    assertTrue(
        childSrc.contains("return super.getReferenceValues(reference)"),
        "Child.getReferenceValues should delegate to super");
    assertTrue(
        childSrc.contains("return super.addReferenceValue(reference, referredNode)"),
        "Child.addReferenceValue(ref, refValue) should delegate to super");
    assertTrue(
        childSrc.contains("return super.addReferenceValue(reference, index, referredNode)"),
        "Child.addReferenceValue(ref, int, refValue) should delegate to super");
  }

  @Test
  public void testBaseConceptThrowsForUnknownProperty() throws IOException {
    Concept standalone = new Concept(LW_VERSION_USED_IN_TESTS, "Standalone");
    standalone.addProperty(
        "title", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    File dir = Files.createTempDirectory("gen-throws").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Throws", standalone));

    String src = readGeneratedClass(dir, PACK, "Standalone");
    assertTrue(
        src.contains("hrow new IllegalStateException(\"Property \" + property + \" not found.\")"),
        "Base concept getPropertyValue must throw IllegalStateException for unknown property");
  }

  @Test
  public void testInheritanceCompilesSuccessfully() throws IOException {
    Concept animal = new Concept(LW_VERSION_USED_IN_TESTS, "Animal");
    animal.addProperty(
        "name", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    Concept dog = new Concept(LW_VERSION_USED_IN_TESTS, "Dog");
    dog.setExtendedConcept(animal);
    dog.addProperty(
        "breed", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-compile-inherit").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Animals", animal, dog));

    assertTrue(compileAllJavaFiles(dir), "Generated inheritance hierarchy must compile");
  }

  // ---------------------------------------------------------------------------
  // Properties
  // ---------------------------------------------------------------------------

  @Test
  public void testPropertySetterNotifiesPartitionObserver() throws IOException {
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "MyNode");
    node.addProperty(
        "value", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    File dir = Files.createTempDirectory("gen-observer").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Observer", node));

    String src = readGeneratedClass(dir, PACK, "MyNode");
    assertTrue(
        src.contains("partitionObserverCache.propertyChanged"),
        "Property setter must notify partitionObserverCache");
  }

  @Test
  public void testPropertyGetterAndSetterGenerated() throws IOException {
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Item");
    node.addProperty(
        "title", LionCoreBuiltins.getString(LW_VERSION_USED_IN_TESTS), Multiplicity.REQUIRED);

    File dir = Files.createTempDirectory("gen-prop-accessors").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Items", node));

    String src = readGeneratedClass(dir, PACK, "Item");
    assertTrue(src.contains("getTitle()"), "Getter for 'title' must be generated");
    assertTrue(src.contains("setTitle("), "Setter for 'title' must be generated");
  }

  // ---------------------------------------------------------------------------
  // References — new methods introduced in this branch
  // ---------------------------------------------------------------------------

  @Test
  public void testSetReferenceValuesMethodGenerated() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Source");
    node.addReference("link", target, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-set-ref-values").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("RefLang", node, target));

    String src = readGeneratedClass(dir, PACK, "Source");
    assertTrue(
        src.contains("void setReferenceValues("), "setReferenceValues method must be generated");
  }

  @Test
  public void testSetReferenceValuesDispatchesByKey() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("target", target, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-set-ref-dispatch").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("Dispatch", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    assertTrue(
        src.contains("reference.getKey()"), "setReferenceValues must dispatch by reference key");
  }

  @Test
  public void testSetReferredMethodGenerated() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("target", target, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-set-referred").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("SetRef", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    assertTrue(src.contains("void setReferred("), "setReferred method must be generated");
    assertTrue(
        src.contains("withReferred(referredNode)"),
        "setReferred must call withReferred on the stored ReferenceValue");
  }

  @Test
  public void testSetResolveInfoMethodGenerated() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("target", target, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-set-resolve-info").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("ResolveInfo", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    assertTrue(src.contains("void setResolveInfo("), "setResolveInfo method must be generated");
    assertTrue(
        src.contains("withResolveInfo(resolveInfo)"),
        "setResolveInfo must call withResolveInfo on the stored ReferenceValue");
  }

  @Test
  public void testAddReferenceValueWithIndexOverloadGenerated() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("targets", target, Multiplicity.ZERO_OR_MORE);

    File dir = Files.createTempDirectory("gen-add-ref-idx").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("AddRefIdx", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    // Both addReferenceValue(Reference, ReferenceValue) and
    // addReferenceValue(Reference, int, ReferenceValue) should be present.
    assertTrue(
        src.contains("int addReferenceValue("), "addReferenceValue override must be generated");
    assertTrue(
        src.contains("int index"), "addReferenceValue with index parameter must be generated");
  }

  @Test
  public void testSingleReferenceGetterReturnReferenceValue() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("myRef", target, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-single-ref").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("SingleRef", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    assertTrue(src.contains("getMyRef()"), "Single-value reference getter must be generated");
    assertTrue(src.contains("setMyRef("), "Single-value reference setter must be generated");
    // Setter must handle observer notification for added/changed/removed values
    assertTrue(
        src.contains("referenceValueAdded") || src.contains("referenceValueChanged"),
        "Single-value reference setter must notify partitionObserverCache");
  }

  @Test
  public void testMultipleReferenceAddRemoveClearGenerated() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("items", target, Multiplicity.ZERO_OR_MORE);

    File dir = Files.createTempDirectory("gen-multi-ref").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("MultiRef", node, target));

    String src = readGeneratedClass(dir, PACK, "Owner");
    assertTrue(src.contains("addToItems("), "addToItems must be generated for multiple reference");
    assertTrue(
        src.contains("removeFromItems("),
        "removeFromItems must be generated for multiple reference");
    assertTrue(src.contains("clearItems()"), "clearItems must be generated for multiple reference");
    assertTrue(src.contains("setItems("), "setItems must be generated for multiple reference");
  }

  @Test
  public void testReferenceMethodsCompile() throws IOException {
    Concept target = new Concept(LW_VERSION_USED_IN_TESTS, "Target");
    Concept node = new Concept(LW_VERSION_USED_IN_TESTS, "Owner");
    node.addReference("single", target, Multiplicity.OPTIONAL);
    node.addReference("multi", target, Multiplicity.ZERO_OR_MORE);

    File dir = Files.createTempDirectory("gen-ref-compile").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("RefCompile", node, target));

    assertTrue(compileAllJavaFiles(dir), "Generated reference methods must compile");
  }

  // ---------------------------------------------------------------------------
  // Containment
  // ---------------------------------------------------------------------------

  @Test
  public void testMultipleContainmentMethodsGenerated() throws IOException {
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Child");
    Concept parent = new Concept(LW_VERSION_USED_IN_TESTS, "Parent");
    parent.addContainment("children", child, Multiplicity.ZERO_OR_MORE);

    File dir = Files.createTempDirectory("gen-multi-cont").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("ContLang", parent, child));

    String src = readGeneratedClass(dir, PACK, "Parent");
    assertTrue(src.contains("addToChildren("), "addToChildren must be generated");
    assertTrue(src.contains("removeFromChildren("), "removeFromChildren must be generated");
    assertTrue(src.contains("clearChildren()"), "clearChildren must be generated");
    assertTrue(src.contains("setChildren("), "setChildren must be generated");
    assertTrue(
        src.contains("partitionObserverCache.childAdded"),
        "addToChildren must notify observer on child added");
  }

  @Test
  public void testSingleContainmentSetterManagesParent() throws IOException {
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Leaf");
    Concept parent = new Concept(LW_VERSION_USED_IN_TESTS, "Branch");
    parent.addContainment("leaf", child, Multiplicity.OPTIONAL);

    File dir = Files.createTempDirectory("gen-single-cont").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("TreeLang", parent, child));

    String src = readGeneratedClass(dir, PACK, "Branch");
    assertTrue(src.contains("setLeaf("), "Setter for single containment must be generated");
    assertTrue(
        src.contains("setParent(this)"), "Single containment setter must set the child's parent");
  }

  @Test
  public void testContainmentCompilesSuccessfully() throws IOException {
    Concept child = new Concept(LW_VERSION_USED_IN_TESTS, "Leaf");
    Concept parent = new Concept(LW_VERSION_USED_IN_TESTS, "Branch");
    parent.addContainment("leaf", child, Multiplicity.OPTIONAL);
    parent.addContainment("leaves", child, Multiplicity.ZERO_OR_MORE);

    File dir = Files.createTempDirectory("gen-cont-compile").toFile();
    generateBothLanguageAndNodeClasses(dir, buildLanguage("ContCompile", parent, child));

    assertTrue(compileAllJavaFiles(dir), "Generated containment methods must compile");
  }
}
