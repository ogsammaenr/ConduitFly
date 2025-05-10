package org.ogsammaenr.conduitFly.storage;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public interface ConduitDataStorage {


    void saveAll(Map<String, List<Location>> data);


    Map<String, List<Location>> loadAll();


    void removeIslandData(String islandId);


}
