package me.beanes.acid;

import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import me.beanes.acid.check.CheckManager;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.PlayerManager;
import me.beanes.acid.player.listener.PacketEventsListener;
import me.beanes.acid.util.pledge.TickEndUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Acid extends JavaPlugin {
    private static Acid INSTANCE;
    public static boolean DEBUG = true; // Set this to true if you wish to debug legit reach values also

    public Acid() {
        INSTANCE = this;
    }

    public static Acid get() {
        return INSTANCE;
    }

    @Getter
    private CheckManager checkManager;
    @Getter
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());

        this.checkManager = new CheckManager();
        this.playerManager = new PlayerManager();

        // Sends a transaction for every player at the tick start of the server
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerManager.getPlayers()) {
                data.getTransactionTracker().sendTransaction(true);
            }
        }, 0L, 1L);

        // Sends a transaction for every player at the tick end / just before the player's netty channel is flushed
        TickEndUtil.injectRunnable(() -> {
            for (PlayerData data : playerManager.getPlayers()) {
                data.getTransactionTracker().sendTransaction(true);
            }
        });
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {

    }
}
