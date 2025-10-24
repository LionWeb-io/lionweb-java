package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DeltaInMemoryServer implements DeltaEventReceiver {
    private DeltaChannel channel;
    private HashMap<String, Set<WeakReference<ClassifierInstance<?>>>> nodes = new HashMap<>();

    public DeltaInMemoryServer(DeltaChannel channel) {
        this.channel = channel;
    }

    @Override
    public void receiveEvent(DeltaEvent event) {
        if (event instanceof PropertyChanged) {
            PropertyChanged propertyChanged = (PropertyChanged) event;
            for (WeakReference<ClassifierInstance<?>> classifierInstanceRef :
                    nodes.get(propertyChanged.node)) {
                ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
                if (classifierInstance != null) {
                    ClassifierInstanceUtils.setPropertyValueByMetaPointer(
                            classifierInstance, propertyChanged.property, propertyChanged.newValue);
                }
            }
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported event type: " + event.getClass().getName());
        }
    }

    public void cleanUp() {
        // optional: call periodically to remove dead refs globally
        Iterator<Map.Entry<String, Set<WeakReference<ClassifierInstance<?>>>>> it =
                nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Set<WeakReference<ClassifierInstance<?>>>> e = it.next();
            e.getValue().removeIf(ref -> ref.get() == null);
            if (e.getValue().isEmpty()) it.remove();
        }
    }
}
