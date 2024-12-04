package io.lionweb.lioncore.java.versions;

public interface LionWebVersionDependent<V extends LionWebVersionToken> {
    default LionWebVersion getLionWebVersion() {
        return TokenReflector.retrieveLionWebVersion(this)
                .orElseThrow(() -> new IllegalStateException("no LionWeb version found"));
    }
}
