package org.ogsammaenr.conduitFly.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.ogsammaenr.conduitFly.ConduitFly;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteConduitStorage implements ConduitDataStorage {
    private final ConduitFly plugin;
    private final Connection connection;

    public SQLiteConduitStorage(ConduitFly plugin) {
        this.plugin = plugin;
        this.connection = connect();

        createTableIfNotExists();
    }

    private Connection connect() {
        try {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/conduits.db";
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database");
            e.printStackTrace();
            return null;
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS conduits (" +
                "island_id TEXT NOT NULL," +
                "world TEXT NOT NULL," +
                "x REAL NOT NULL," +
                "y REAL NOT NULL," +
                "z REAL NOT NULL" +
                ");";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create table");
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Map<String, List<Location>> data) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM conduits");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String sql = "INSERT INTO conduits (island_id, world, x, y, z) VALUES (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, List<Location>> entry : data.entrySet()) {
                String islandId = entry.getKey();
                for (Location location : entry.getValue()) {
                    if (location.getWorld() == null) continue;

                    preparedStatement.setString(1, islandId);
                    preparedStatement.setString(2, location.getWorld().getName());
                    preparedStatement.setDouble(3, location.getX());
                    preparedStatement.setDouble(4, location.getY());
                    preparedStatement.setDouble(5, location.getZ());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save data");
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, List<Location>> loadAll() {
        Map<String, List<Location>> data = new HashMap<>();
        String sql = "SELECT * FROM conduits";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                String islandId = rs.getString("island_id");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");

                if (Bukkit.getWorld(world) == null) continue;

                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                data.computeIfAbsent(islandId, k -> new ArrayList<>()).add(location);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error Loading Conduits From SQLite");
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void removeIslandData(String islandId) {
        String sql = "DELETE FROM conduits WHERE island_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error Removing Conduits From SQLite");
            e.printStackTrace();
        }
    }
}
