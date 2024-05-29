package me.beanes.acid.check;

import lombok.Getter;
import me.beanes.acid.check.impl.attack.AttackA;
import me.beanes.acid.player.PlayerData;

import java.lang.reflect.Constructor;

@Getter
public class CheckManager {
    private final Constructor<? extends Check>[] constructors;

    public CheckManager() {
        Class<? extends Check>[] checkClasses = new Class[] {
            AttackA.class
        };

        constructors = new Constructor[checkClasses.length];

        for (int i = 0; i < checkClasses.length; i++) {
            try {
                constructors[i] = checkClasses[i].getConstructor(PlayerData.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
