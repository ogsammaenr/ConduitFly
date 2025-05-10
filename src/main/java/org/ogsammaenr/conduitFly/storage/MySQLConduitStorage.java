package org.ogsammaenr.conduitFly.storage;

import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class MySQLConduitStorage implements ConduitDataStorage {
    private final MySQLConnection mySQLConnection;

    public MySQLConduitStorage(MySQLConnection mySQLConnection) {
        this.mySQLConnection = mySQLConnection;

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS conduits (" +
                "island_id VARCHAR(255) NOT NULL," +  // MySQL için VARCHAR kullanıyoruz
                "world VARCHAR(255) NOT NULL," +
                "x DOUBLE NOT NULL," +               // MySQL'de REAL yerine DOUBLE kullanıyoruz
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "PRIMARY KEY (island_id, world, x, y, z)" + // Birincil anahtar (isteğe bağlı) ekleyebilirsiniz
                ");";

        try (Connection connection = mySQLConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql); // Tabloyu oluşturuyoruz.
        } catch (SQLException e) {
            System.out.println("Could not create table in MySQL");
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Map<String, List<Location>> data) {
        String deletesql = "DELETE FROM conduits";
        String sql = "INSERT INTO conduits (island_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)";


        try (Connection connection = mySQLConnection.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deletesql);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            deleteStatement.executeUpdate();  // Cache boşsa, veritabanındaki tüm verileri temizle


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
            e.printStackTrace();
        }
    }


    @Override
    public Map<String, List<Location>> loadAll() {
        Map<String, List<Location>> data = new HashMap<>();
        String sql = "SELECT * FROM conduits";

        try (Connection connection = mySQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String islandId = resultSet.getString("island_id");
                String worldname = resultSet.getString("world");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");

                Location location = new Location(getServer().getWorld(worldname), x, y, z);
                data.computeIfAbsent(islandId, k -> new ArrayList<>()).add(location);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void removeIslandData(String islandId) {
        String sql = "DELETE FROM conduits WHERE island_id = ?";

        try (Connection connection = mySQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, islandId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Island " + islandId + " removed");
            } else {
                System.out.println("Island " + islandId + " not removed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error Removing Conduits From MySQL");
        }
    }
}
