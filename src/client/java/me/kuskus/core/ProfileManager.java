package me.kuskus.core;

import me.kuskus.KusKusKlient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProfileManager {
    private Path dir() {
        return KusKusKlient.CONFIG.root().resolve("profiles");
    }

    public void save(String name) throws IOException {
        KusKusKlient.CONFIG.saveSnapshot(dir().resolve(KusKusKlient.CONFIG.normalizeSnapshotName(name)));
    }

    public void load(String name) throws IOException {
        KusKusKlient.CONFIG.loadSnapshot(dir().resolve(KusKusKlient.CONFIG.normalizeSnapshotName(name)));
    }

    public void delete(String name) throws IOException {
        KusKusKlient.CONFIG.deleteSnapshot(dir().resolve(KusKusKlient.CONFIG.normalizeSnapshotName(name)));
    }

    public List<String> list() throws IOException {
        return KusKusKlient.CONFIG.listSnapshots(dir());
    }
}
