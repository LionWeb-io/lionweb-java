package org.lionweb.lioncore.java;

/**
 * This is equivalent to MPS' Cardinality, which has four equivalent values.
 *
 * In Ecore there is no equivalent as lowerBound and upperBound can be set independently.
 */
public enum Multiplicity {
    Optional,
    Single,
    ZeroOrMore,
    OneOrMore
}

