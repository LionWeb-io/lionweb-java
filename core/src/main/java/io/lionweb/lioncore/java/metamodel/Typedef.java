package io.lionweb.lioncore.java.metamodel;

import io.lionweb.lioncore.java.Experimental;
import io.lionweb.lioncore.java.model.ReferenceValue;
import javax.annotation.Nullable;

/**
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#constraineddatatypes">MPS
 *     equivalent <i>Constrained Data Type</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1082978499127">MPS
 *     equivalent <i>ConstrainedDataTypeDeclaration</i> in local MPS</a>
 */
@Experimental
public class Typedef extends DataType<Typedef> {
  public Typedef() {
    super();
  }

  public Typedef(@Nullable Metamodel metamodel, @Nullable String name) {
    super(metamodel, name);
  }

  public @Nullable PrimitiveType getPrimitiveType() {
    return getReferenceSingleValue("primitiveType");
  }

  public void setPrimitiveType(@Nullable PrimitiveType primitiveType) {
    if (primitiveType == null) {
      this.setReferenceSingleValue("primitiveType", null);
    } else {
      this.setReferenceSingleValue(
          "primitiveType", new ReferenceValue(primitiveType, primitiveType.getName()));
    }
  }

  @Override
  public Concept getConcept() {
    throw new UnsupportedOperationException(
        "Typedef is currently not yet approved, so there is no concept defined for it");
  }
}
