package eu.pb4.polymer.other;

import java.util.ArrayList;
import java.util.List;

@Deprecated

public final class DoubleBooleanEvent<T, U> {
    private List<EventHandler<T, U>> handlers = new ArrayList<>();

    public void register(EventHandler<T, U> event) {
        this.handlers.add(event);
    }

    public boolean invoke(T obj, U obj2) {
        for (EventHandler<T, U> consumer : this.handlers) {
            boolean x = consumer.call(obj, obj2);
            if (x) {
                return true;
            }
        }
        return false;
    }


    public interface EventHandler<T, U> {
        boolean call(T obj, U obj2);
    }
}
