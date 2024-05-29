package me.beanes.acid.player;

import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import me.beanes.acid.Acid;
import me.beanes.acid.check.Check;
import me.beanes.acid.player.tracker.impl.PositionTracker;
import me.beanes.acid.player.tracker.impl.RotationTracker;
import me.beanes.acid.player.tracker.impl.TransactionTracker;
import me.beanes.acid.player.tracker.impl.entity.EntityTracker;

import java.lang.reflect.Constructor;

public class PlayerData {
    @Getter
    private final User user;
    @Getter
    private final Check[] checks;
    @Getter
    private final TransactionTracker transactionTracker;
    @Getter
    private final PositionTracker positionTracker;
    @Getter
    private final RotationTracker rotationTracker;
    @Getter
    private final EntityTracker entityTracker;

    public PlayerData(User user) {
        this.user = user;

        this.transactionTracker = new TransactionTracker(this);
        this.positionTracker = new PositionTracker(this);
        this.rotationTracker = new RotationTracker(this);
        this.entityTracker = new EntityTracker(this);

        Constructor<? extends Check>[] checkConstructors = Acid.get().getCheckManager().getConstructors();
        checks = new Check[checkConstructors.length];
        for (int i = 0; i < checkConstructors.length; i++) {
            try {
                checks[i] = checkConstructors[i].newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
