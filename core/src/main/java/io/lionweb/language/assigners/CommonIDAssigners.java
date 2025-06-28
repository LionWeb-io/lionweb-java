package io.lionweb.language.assigners;

import io.lionweb.language.INamed;
import io.lionweb.language.Language;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.HasSettableID;
import io.lionweb.model.Node;
import io.lionweb.utils.IdUtils;
import javax.annotation.Nonnull;

public class CommonIDAssigners {

  public static final IDAssigner qualifiedIDAssigner =
      new IDAssigner() {
        @Override
        public void assignIDs(@Nonnull Language language) {
          assignIDsToNode(language);
        }

        private void assignIDsToNode(@Nonnull Node node) {
          if (!(node instanceof HasSettableID)) {
            throw new IllegalArgumentException(
                "Cannot assign the ID of " + node + " has it is not an instance of HasSettableID");
          }
          if (node.getID() == null) {
            ClassifierInstance<?> parent = node.getParent();
            if (!(node instanceof INamed)) {
              throw new IllegalStateException(
                  "Cannot assign ID to node which is not instance of INamed");
            }
            String name = ((INamed) node).getName();
            if (name == null) {
              throw new IllegalStateException(
                  "Cannot auto assign id to " + node + " has it has a null name");
            }
            if (parent == null) {
              ((HasSettableID) node).setID(IdUtils.cleanString(name));
            } else {
              String parentID = parent.getID();
              if (parentID == null) {
                throw new IllegalStateException(
                    "Cannot auto assign ID to " + node + " as it has a parent with a null ID");
              }
              ((HasSettableID) node).setID(parentID + "-" + IdUtils.cleanString(name));
            }
          }
          ClassifierInstanceUtils.getChildren(node).forEach(this::assignIDsToNode);
        }
      };
}
