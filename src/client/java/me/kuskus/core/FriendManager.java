package me.kuskus.core;

import me.kuskus.friend.Friend;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendManager {
    private final Map<String, Friend> friends = new ConcurrentHashMap<>();

    public void add(String name, String note) {
        friends.put(name.toLowerCase(), new Friend(name, note == null ? "" : note));
    }

    public void remove(String name) {
        friends.remove(name.toLowerCase());
    }

    public boolean isFriend(String name) {
        return name != null && friends.containsKey(name.toLowerCase());
    }

    public Collection<Friend> all() {
        return friends.values();
    }

    public void clear() {
        friends.clear();
    }
}
