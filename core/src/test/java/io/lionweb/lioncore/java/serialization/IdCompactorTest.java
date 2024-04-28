package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.serialization.protobuf.CompactedId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IdCompactorTest {
    @Test
    public void Dash_caaa() {
        String s = "-caaa";
        CompactedId compacted = IdCompactor.compact(s);
        String actual = IdCompactor.expand(compacted);
        Assert.assertEquals(s, actual);
    }

    @Test
    public void aaa() {
        String s = "aaa";
        CompactedId compacted = IdCompactor.compact(s);
        String actual = IdCompactor.expand(compacted);
        Assert.assertEquals(s, actual);
    }

    @Test
    public void combine() {
        for (int length = 1; length <= 10; length++) {
            char[] chars = new char[length];
            Arrays.fill(chars, 'a');
            for (int pos = 0; pos < length; pos++) {
                for (int i = 0; i < 64; i++) {
                    chars[pos] = IdCompactor.reverseLookup[i];
                    String s = new String(chars);
                    CompactedId compacted = IdCompactor.compact(s);
                    String actual = IdCompactor.expand(compacted);
                    Assert.assertEquals(s, actual);
                    Assert.assertEquals(s.length(), compacted.getLength());
                }
            }
        }
    }
}
