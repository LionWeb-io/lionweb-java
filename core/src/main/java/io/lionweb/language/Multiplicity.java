package io.lionweb.language;

/**
 * Represents the multiplicity of a feature.
 *
 * <p>The {@code Multiplicity} enum includes the following constants: - {@code OPTIONAL}: Indicates
 * an optional relationship or property that is not required and does not allow multiple instances
 * (i.e., values accepted 0..1). - {@code REQUIRED}: Represents a required, non-optional property or
 * relationship that does not allow multiple instances (i.e., values accepted 1..1). - {@code
 * ZERO_OR_MORE}: Allows zero or more instances, making it optional and allowing multiple instances
 * (i.e., values accepted 0..*). This is not a valid value for properties. - {@code ONE_OR_MORE}:
 * Specifies that at least one instance is required, but multiple instances are also allowed (i.e.,
 * values accepted 1..*). This is not a valid value for properties.
 *
 * <p>Each {@code Multiplicity} constant is defined by two attributes: - {@code optional}: Boolean
 * indicating whether the relationship or property is optional. - {@code multiple}: Boolean
 * indicating whether multiple instances are allowed.
 *
 * <p>This enumeration provides methods to access these attributes: - {@link #isOptional()} to check
 * if the multiplicity is optional. - {@link #isMultiple()} to check if the multiplicity allows
 * multiple instances.
 */
public enum Multiplicity {
  OPTIONAL(true, false),
  REQUIRED(false, false),
  ZERO_OR_MORE(true, true),
  ONE_OR_MORE(false, true);

  private final boolean optional;
  private final boolean multiple;

  Multiplicity(boolean optional, boolean multiple) {
    this.optional = optional;
    this.multiple = multiple;
  }

  public boolean isOptional() {
    return optional;
  }

  public boolean isMultiple() {
    return multiple;
  }
}
