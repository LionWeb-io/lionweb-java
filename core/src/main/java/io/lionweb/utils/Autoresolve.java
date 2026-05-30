package io.lionweb.utils;

/**
 * The {@code Autoresolve} class provides constant string prefixes that are used for domain-specific
 * purposes related to LionWeb's M3 and built-in namespaces. These prefixes can assist with
 * automatic resolution of identifiers or names within the LionWeb ecosystem.
 */
public class Autoresolve {
  public static final String LIONCORE_AUTORESOLVE_PREFIX = "LionWeb.LionCore_M3.";
  public static final String LIONCOREBUILTINS_AUTORESOLVE_PREFIX = "LionWeb.LionCore_builtins.";

  private Autoresolve() {
    // Prevent instantiation
  }
}
