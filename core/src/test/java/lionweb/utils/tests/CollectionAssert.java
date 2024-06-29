package lionweb.utils.tests;

import org.junit.Assert;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class CollectionAssert {
    public static void AreEqual(@Nonnull List<?> expected, @Nonnull List<?> actual) {
        Assert.assertEquals("size differs", expected.size(), actual.size());
        Iterator<?> expectedIterator = expected.iterator();
        Iterator<?> actualIterator = actual.iterator();
        int index = 0;
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            Assert.assertEquals("difference at index "+ index, expectedIterator.next(), actualIterator.next());
            index++;
        }
    }
}
