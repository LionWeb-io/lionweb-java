package io.lionweb.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;

public class ClassifierTest {

  @Test
  public void allLinks() {
    assertEquals(
        new HashSet(Collections.singleton(LionCore.getLanguage().getReferenceByName("dependsOn"))),
        new HashSet(LionCore.getLanguage().allReferences()));
    assertEquals(
        new HashSet(Collections.singleton(LionCore.getLanguage().getContainmentByName("entities"))),
        new HashSet(LionCore.getLanguage().allContainments()));
    assertEquals(
        new HashSet(
            Arrays.asList(
                LionCore.getLanguage().getReferenceByName("dependsOn"),
                LionCore.getLanguage().getContainmentByName("entities"))),
        new HashSet(LionCore.getLanguage().allLinks()));
  }

  @Test
  public void getPropertyByID() {
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2023_1).getPropertyByName("name"),
        LionCore.getLanguage(LionWebVersion.v2023_1)
            .getPropertyByID("LionCore-builtins-INamed-name"));
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2024_1).getPropertyByName("name"),
        LionCore.getLanguage(LionWebVersion.v2024_1)
            .getPropertyByID("LionCore-builtins-INamed-name-2024-1"));
  }

  @Test
  public void getContainmentByID() {
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2023_1).getContainmentByName("entities"),
        LionCore.getLanguage(LionWebVersion.v2023_1).getContainmentByID("-id-Language-entities"));
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2024_1).getContainmentByName("entities"),
        LionCore.getLanguage(LionWebVersion.v2024_1)
            .getContainmentByID("-id-Language-entities-2024-1"));
  }

  @Test
  public void getReferenceByID() {
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2023_1).getReferenceByName("dependsOn"),
        LionCore.getLanguage(LionWebVersion.v2023_1).getReferenceByID("-id-Language-dependsOn"));
    assertEquals(
        LionCore.getLanguage(LionWebVersion.v2024_1).getReferenceByName("dependsOn"),
        LionCore.getLanguage(LionWebVersion.v2024_1)
            .getReferenceByID("-id-Language-dependsOn-2024-1"));
  }

  @Test
  public void requireContainmentByNameNegativeCase() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          LionCore.getLanguage().requireContainmentByName("UNEXISTING");
        });
  }

  @Test
  public void requireReferenceByNameNegativeCase() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          LionCore.getLanguage().requireReferenceByName("UNEXISTING");
        });
  }

  @Test
  public void requireReferenceByNamePositiveCase() {
    assertEquals(
        LionCore.getLanguage().getReferenceByName("dependsOn"),
        LionCore.getLanguage().requireReferenceByName("dependsOn"));
  }

  @Test
  public void getLinkByName() {
    assertEquals(null, LionCore.getLanguage().getLinkByName("UNEXISTING"));
    assertEquals(
        LionCore.getLanguage().getReferenceByName("dependsOn"),
        LionCore.getLanguage().getLinkByName("dependsOn"));
    assertEquals(
        LionCore.getLanguage().getContainmentByName("entities"),
        LionCore.getLanguage().getLinkByName("entities"));
  }

  @Test
  public void requirePropertyByNamePositiveCase() {
    // Test with a property that exists directly on the classifier
    Property nameProperty = LionCore.getLanguage().getPropertyByName("name");
    assertEquals(nameProperty, LionCore.getLanguage().requirePropertyByName("name"));
  }

  @Test
  public void requirePropertyByNameInheritedProperty() {
    // Test with a property that is inherited from INamed interface
    // Create a concept that implements INamed to test inheritance
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept testConcept = new Concept(testLanguage, "TestConcept");
    testConcept.addImplementedInterface(LionCoreBuiltins.getINamed());

    // The "name" property should be inherited from INamed
    Property nameProperty = testConcept.requirePropertyByName("name");
    assertEquals("name", nameProperty.getName());
    assertEquals(LionCoreBuiltins.getString(), nameProperty.getType());
  }

  @Test
  public void requirePropertyByNameNegativeCase() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          LionCore.getLanguage().requirePropertyByName("NONEXISTENT_PROPERTY");
        });
  }

  @Test
  public void requirePropertyByNameNegativeCaseWithMessage() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              LionCore.getLanguage().requirePropertyByName("NONEXISTENT_PROPERTY");
            });
    assertEquals(
        "Property NONEXISTENT_PROPERTY not found in Classifier Language", exception.getMessage());
  }

  @Test
  public void requirePropertyByNameNullParameter() {
    assertThrows(
        NullPointerException.class,
        () -> {
          LionCore.getLanguage().requirePropertyByName(null);
        });
  }

  @Test
  public void addContainmentWithoutMultiplicity() {
    // Create a test language and concepts
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");

    // Test the addContainment method without multiplicity parameter
    parentConcept.addContainment("children", childConcept);

    // Verify that the containment was added correctly
    Containment containment = parentConcept.getContainmentByName("children");
    assertEquals("children", containment.getName());
    assertEquals(childConcept, containment.getType());

    // Verify that the default multiplicity is REQUIRED (not optional, not multiple)
    assertEquals(false, containment.isOptional());
    assertEquals(false, containment.isMultiple());
  }

  @Test
  public void addContainmentWithoutMultiplicityVerifyFeaturesList() {
    // Create a test language and concepts
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");

    // Initially the parent concept should have no features
    assertEquals(0, parentConcept.getFeatures().size());

    // Add containment without multiplicity
    parentConcept.addContainment("children", childConcept);

    // Verify that the feature was added to the features list
    assertEquals(1, parentConcept.getFeatures().size());
    assertEquals("children", parentConcept.getFeatures().get(0).getName());
    assertEquals(childConcept, ((Containment) parentConcept.getFeatures().get(0)).getType());
  }

  @Test
  public void addContainmentWithoutMultiplicityMethodChaining() {
    // Create a test language and concepts
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept1 = new Concept(testLanguage, "ChildConcept1");
    Concept childConcept2 = new Concept(testLanguage, "ChildConcept2");

    // Test method chaining - addContainment should return the same concept
    Concept result =
        parentConcept
            .addContainment("children1", childConcept1)
            .addContainment("children2", childConcept2);

    // Verify method chaining works
    assertEquals(parentConcept, result);

    // Verify both containments were added
    assertEquals(2, parentConcept.getFeatures().size());
    assertEquals("children1", parentConcept.getContainmentByName("children1").getName());
    assertEquals("children2", parentConcept.getContainmentByName("children2").getName());
    assertEquals(childConcept1, parentConcept.getContainmentByName("children1").getType());
    assertEquals(childConcept2, parentConcept.getContainmentByName("children2").getType());
  }

  @Test
  public void addPropertyWithInvalidMultiplicityZeroOrMore() {
    // Create a test language and concept
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept testConcept = new Concept(testLanguage, "TestConcept");

    // Test that ZERO_OR_MORE multiplicity is rejected for properties
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              testConcept.addProperty(
                  "testProperty", LionCoreBuiltins.getString(), Multiplicity.ZERO_OR_MORE);
            });

    assertEquals("Multiple values are not supported for properties", exception.getMessage());

    // Verify no property was added
    assertEquals(0, testConcept.getFeatures().size());
    assertEquals(null, testConcept.getPropertyByName("testProperty"));
  }

  @Test
  public void addPropertyWithInvalidMultiplicityOneOrMore() {
    // Create a test language and concept
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept testConcept = new Concept(testLanguage, "TestConcept");

    // Test that ONE_OR_MORE multiplicity is rejected for properties
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              testConcept.addProperty(
                  "testProperty", LionCoreBuiltins.getString(), Multiplicity.ONE_OR_MORE);
            });

    assertEquals("Multiple values are not supported for properties", exception.getMessage());

    // Verify no property was added
    assertEquals(0, testConcept.getFeatures().size());
    assertEquals(null, testConcept.getPropertyByName("testProperty"));
  }

  @Test
  public void addPropertyWithValidMultiplicities() {
    // Create a test language and concept
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept testConcept = new Concept(testLanguage, "TestConcept");

    // Test that REQUIRED multiplicity works
    testConcept.addProperty(
        "requiredProperty", LionCoreBuiltins.getString(), Multiplicity.REQUIRED);
    Property requiredProperty = testConcept.getPropertyByName("requiredProperty");
    assertEquals("requiredProperty", requiredProperty.getName());
    assertEquals(LionCoreBuiltins.getString(), requiredProperty.getType());
    assertEquals(false, requiredProperty.isOptional());

    // Test that OPTIONAL multiplicity works
    testConcept.addProperty(
        "optionalProperty", LionCoreBuiltins.getString(), Multiplicity.OPTIONAL);
    Property optionalProperty = testConcept.getPropertyByName("optionalProperty");
    assertEquals("optionalProperty", optionalProperty.getName());
    assertEquals(LionCoreBuiltins.getString(), optionalProperty.getType());
    assertEquals(true, optionalProperty.isOptional());

    // Verify both properties were added
    assertEquals(2, testConcept.getFeatures().size());
  }

  @Test
  public void addPropertyWithNullMultiplicity() {
    // Create a test language and concept
    Language testLanguage = new Language();
    testLanguage.setName("TestLanguage");

    Concept testConcept = new Concept(testLanguage, "TestConcept");

    // Test that null multiplicity is rejected
    assertThrows(
        NullPointerException.class,
        () -> {
          testConcept.addProperty("testProperty", LionCoreBuiltins.getString(), null);
        });

    // Verify no property was added
    assertEquals(0, testConcept.getFeatures().size());
  }
}
