package org.lionweb.lioncore.java;

/**
 * This is equivalent to MPS' <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F4241665505353447567">Cardinality</a>,
 * which has four equivalent values.
 *
 * In Ecore there is no equivalent as lowerBound and upperBound can be set independently.
 */
public enum Multiplicity {
    Optional,
    Single,
    ZeroOrMore,
    OneOrMore
}

