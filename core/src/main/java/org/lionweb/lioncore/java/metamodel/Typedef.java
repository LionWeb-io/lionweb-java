package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nullable;
import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.ReferenceValue;

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

  public Typedef(@Nullable Metamodel metamodel, @Nullable String simpleName) {
    super(metamodel, simpleName);
  }

  public @Nullable PrimitiveType getPrimitiveType() {
    return getReferenceSingleValue("primitiveType");
  }

  public void setPrimitiveType(@Nullable PrimitiveType primitiveType) {
    if (primitiveType == null) {
      this.setReferenceSingleValue("primitiveType", null);
    } else {
      this.setReferenceSingleValue(
          "primitiveType", new ReferenceValue(primitiveType, primitiveType.getSimpleName()));
    }
  }

  @Override
  public Concept getConcept() {
    throw new UnsupportedOperationException(
        "Typedef is currently not yet approved, so there is no concept defined for it");
  }
}
