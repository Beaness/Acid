package me.beanes.acid.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Check {
    protected final PlayerData data;
    private final String name;
    private final String type;
    private final String description;
    @Getter @Setter
    private boolean enabled = true;
    protected Check(PlayerData data, String name, String type, String description) {
        this.data = data;
        this.name = name;
        this.type = type;
        this.description = description;
    }
    public abstract void onPacketReceive(PacketReceiveEvent event);
    public void alert(String reason) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.broadcastMessage(reason);
        }
    }
}
