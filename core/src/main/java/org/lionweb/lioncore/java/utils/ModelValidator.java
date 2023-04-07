package org.lionweb.lioncore.java.utils;

import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.model.Model;

public class ModelValidator extends Validator<Model> {
    @Override
    public ValidationResult validate(Model element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Eventually the Metamodel will be also a Model so MetamodelValidator will delegate to ModelValidator.
     * For now, we expose this method instead.
     *
     * Ids can only contain these symbols:
     *
     * - lowercase latin characters: a..z
     * - uppercase latin characters: A..Z
     * - arabic numerals: 0..9
     * - underscore: _
     * - hyphen: -
     */
    public static boolean isValidID(String id) {
        if (id.length() == 0) {
            return false;
        }
        for (char c : id.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c == '_' || c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }
}
