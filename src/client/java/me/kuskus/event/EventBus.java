package me.kuskus.event;

import me.kuskus.KusKusKlient;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType EVENT_SIGNATURE = MethodType.methodType(void.class, Event.class);

    private final Map<Class<? extends Event>, CopyOnWriteArrayList<RegisteredHandler>> listeners = new ConcurrentHashMap<>();
    private final Map<Object, CopyOnWriteArrayList<RegisteredHandler>> owners = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public void subscribe(Object listener) {
        CopyOnWriteArrayList<RegisteredHandler> handlers = new CopyOnWriteArrayList<>();

        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null || method.getParameterCount() != 1 || method.getReturnType() != void.class) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(parameterType)) {
                continue;
            }

            try {
                method.setAccessible(true);
                MethodHandle handle = LOOKUP.unreflect(method)
                    .bindTo(listener)
                    .asType(EVENT_SIGNATURE);
                RegisteredHandler registered = new RegisteredHandler(
                    listener,
                    (Class<? extends Event>) parameterType,
                    handle,
                    annotation.priority()
                );
                handlers.add(registered);
                listeners.computeIfAbsent(registered.type(), ignored -> new CopyOnWriteArrayList<>()).add(registered);
            } catch (IllegalAccessException exception) {
                KusKusKlient.LOGGER.warn("Failed to register listener {}", method, exception);
            }
        }

        if (handlers.isEmpty()) {
            return;
        }

        handlers.sort(Comparator.comparingInt(RegisteredHandler::priority).reversed());
        owners.put(listener, handlers);
        for (RegisteredHandler handler : handlers) {
            listeners.get(handler.type()).sort(Comparator.comparingInt(RegisteredHandler::priority).reversed());
        }
    }

    public void unsubscribe(Object listener) {
        CopyOnWriteArrayList<RegisteredHandler> handlers = owners.remove(listener);
        if (handlers == null) {
            return;
        }

        for (RegisteredHandler handler : handlers) {
            CopyOnWriteArrayList<RegisteredHandler> typedListeners = listeners.get(handler.type());
            if (typedListeners == null) {
                continue;
            }
            typedListeners.remove(handler);
            if (typedListeners.isEmpty()) {
                listeners.remove(handler.type());
            }
        }
    }

    public <T extends Event> T post(T event) {
        CopyOnWriteArrayList<RegisteredHandler> typedListeners = listeners.get(event.getClass());
        if (typedListeners == null) {
            return event;
        }

        for (RegisteredHandler listener : new ArrayList<>(typedListeners)) {
            try {
                listener.handle().invokeExact((Event) event);
            } catch (Throwable exception) {
                KusKusKlient.LOGGER.warn("Event listener failed for {}", event.getClass().getSimpleName(), exception);
            }
        }

        return event;
    }

    private record RegisteredHandler(
        Object owner,
        Class<? extends Event> type,
        MethodHandle handle,
        int priority
    ) {
    }
}
