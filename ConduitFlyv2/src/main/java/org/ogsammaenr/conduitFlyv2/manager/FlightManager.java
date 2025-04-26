package org.ogsammaenr.conduitFlyv2.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightManager {

    // Oyuncunun uçuşa başlama zamanını (milisaniye cinsinden) tutuyoruz
    private final Map<UUID, Long> flightStartTimes = new HashMap<>();

    // Oyuncu uçmaya başladığında çağrılır
    public void startFlight(Player player) {
        flightStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // Oyuncunun uçuşu durduğunda çağrılır
    public void stopFlight(Player player) {
        flightStartTimes.remove(player.getUniqueId());
    }

    // Oyuncunun ne kadar süredir uçtuğunu döndürür (saniye cinsinden)
    public long getFlightDuration(Player player) {
        Long startTime = flightStartTimes.get(player.getUniqueId());
        if (startTime == null) return 0;
        long now = System.currentTimeMillis();
        return (now - startTime) / 1000;
    }

    // Bu oyuncu şu anda uçuyor mu diye kontrol eder
    public boolean isFlying(Player player) {
        return flightStartTimes.containsKey(player.getUniqueId());
    }

    // Tüm uçan oyuncular
    public Map<UUID, Long> getAllFlyingPlayers() {
        return flightStartTimes;
    }
}
