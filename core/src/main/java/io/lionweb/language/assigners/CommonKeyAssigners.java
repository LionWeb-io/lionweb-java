package io.lionweb.language.assigners;

import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.utils.IdUtils;
import javax.annotation.Nonnull;

/**
 * Keys can be assigned following a few common policies. For this reason it is useful to create them
 * and reuse them, so that we can avoid writing a lot of boilerplate to assign keys.
 */
public class CommonKeyAssigners {

  /**
   * This KeyAssigner set the key of a node as the key of the parent followed by the name of this
   * node, separated by dashes.
   */
  public static final KeyAssigner qualifiedKeyAssigner =
      new KeyAssigner() {
        @Override
        public void assignKeys(@Nonnull Language language) {
          assignKeysToIKeyed(language);
        }

        private void assignKeysToIKeyed(@Nonnull IKeyed<?> keyed) {
          if (keyed.getKey() == null) {
            ClassifierInstance<?> parent = ((Node) keyed).getParent();
            String name = keyed.getName();
            if (name == null) {
              throw new IllegalStateException(
                  "Cannot auto assign key to " + keyed + " has it has a null name");
            }
            if (parent == null || parent instanceof Language) {
              // Keys must be unique within a language, so we do not include the language when
              // calculating the key
              keyed.setKey(IdUtils.cleanString(name));
            } else {
              String parentKey = ((IKeyed<?>) parent).getKey();
              if (parentKey == null) {
                throw new IllegalStateException(
                    "Cannot auto assign key to " + keyed + " as it has a parent with a null key");
              }
              keyed.setKey(parentKey + "-" + IdUtils.cleanString(name));
            }
          }
          ClassifierInstanceUtils.getChildren((Node) keyed).stream()
              .filter(n -> n instanceof IKeyed<?>)
              .map(n -> (IKeyed<?>) n)
              .forEach(this::assignKeysToIKeyed);
        }
      };
}
