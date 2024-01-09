package me.cooleg.limitedlife.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SQLUtils {

    private Connection connection;
    private final File file;
    private final JavaPlugin plugin;

    public SQLUtils(File file, JavaPlugin plugin) {
        this.file = file;
        this.plugin = plugin;
    }

    public Connection getConnection() {
        if (connection == null) {return newConnection();}

        try {
            if (connection.isClosed()) {return newConnection();}
            else {return connection;}
        } catch (SQLException ex) {
            return newConnection();
        }
    }

    private Connection newConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            return connection;
        } catch (SQLException ex) {
            Bukkit.getLogger().severe("ERROR CONNECTING TO LIMITEDLIFE DATABASE");
            ex.printStackTrace();
            throw new RuntimeException();
        } catch (ClassNotFoundException ex) {
            Bukkit.getLogger().severe("SQLITE DEPENDENCY NOT FOUND!");
            throw new RuntimeException();
        }
    }

    public void createTable() {
        CompletableFuture.runAsync(() -> {
            Connection conn = getConnection();
            try (PreparedStatement statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS LimitedLife (UUID Char(36) PRIMARY KEY, SecondsLeft BIGINT) WITHOUT ROWID")) {
                statement.executeUpdate();
            } catch (SQLException ex) {
                Bukkit.getLogger().severe("FAILED TO CREATE DATABASE TABLE");
            }
        });
    }

    public void setTimeLeft(UUID id, long seconds) {
        CompletableFuture.runAsync(() -> {
            Connection conn = getConnection();
            try (PreparedStatement statement = conn.prepareStatement("INSERT INTO LimitedLife (UUID, SecondsLeft) VALUES (?, ?) ON CONFLICT(UUID) DO UPDATE SET SecondsLeft=?")) {
                statement.setString(1, id.toString());
                statement.setLong(2, seconds);
                statement.setLong(3, seconds);
                statement.executeUpdate();
            }catch (SQLException ex) {
                Bukkit.getLogger().severe("FAILED TO SAVE SECONDS LEFT FOR UUID " + id.toString());
            }
        });
    }

    public void setTimeLeftNow(UUID id, long seconds) {
        Connection conn = getConnection();
        try (PreparedStatement statement = conn.prepareStatement("INSERT INTO LimitedLife (UUID, SecondsLeft) VALUES (?, ?) ON CONFLICT(UUID) DO UPDATE SET SecondsLeft=?")) {
            statement.setString(1, id.toString());
            statement.setLong(2, seconds);
            statement.setLong(3, seconds);
            statement.executeUpdate();
        }catch (SQLException ex) {
            Bukkit.getLogger().severe("FAILED TO SAVE SECONDS LEFT FOR UUID " + id.toString());
        }
    }

    public void getTimeLeft(UUID id, Consumer<Long> callback) {
        CompletableFuture.runAsync(() -> {
            Connection conn = getConnection();
            try (PreparedStatement statement = conn.prepareStatement("SELECT SecondsLeft FROM LimitedLife WHERE UUID = ?")) {
                statement.setString(1, id.toString());
                ResultSet results = statement.executeQuery();

                Long seconds = null;

                if (results.next()) {
                     seconds = results.getLong(1);
                }

                Long finalSeconds = seconds;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(finalSeconds);
                    }
                }.runTask(plugin);

            } catch (SQLException ex) {
                Bukkit.getLogger().severe("FAILED TO GET SECONDS LEFT FOR UUID " + id.toString());
            }
        });
    }

    public Long getTimeLeftNow(UUID id) {
        Connection conn = getConnection();
        try (PreparedStatement statement = conn.prepareStatement("SELECT SecondsLeft FROM LimitedLife WHERE UUID = ?")) {
            statement.setString(1, id.toString());
            ResultSet results = statement.executeQuery();

            Long seconds = null;

            if (results.next()) {
                seconds = results.getLong(1);
            }

            return seconds;
        } catch (SQLException ex) {
            Bukkit.getLogger().severe("FAILED TO GET SECONDS LEFT FOR UUID " + id.toString());
            return null;
        }
    }

}
