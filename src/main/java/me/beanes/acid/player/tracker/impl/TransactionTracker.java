package me.beanes.acid.player.tracker.impl;

import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.tracker.Tracker;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransactionTracker extends Tracker {
    private int sent = 0;
    private int received = 0;
    private final List<TransactionTask> tasks = new ArrayList<>();
    private final Object cachedTransaction;
    public TransactionTracker(PlayerData data) {
        super(data);

        // Writes the buffer to a netty bytebuf that we can keep reusing to send transactions
        WrapperPlayServerWindowConfirmation transactionWrapper = new WrapperPlayServerWindowConfirmation(0, (short) -1, false);
        transactionWrapper.buffer = UnpooledByteBufAllocationHelper.buffer();
        transactionWrapper.prepareForSend(data.getUser().getChannel(), true, false);
        transactionWrapper.write();

        cachedTransaction = transactionWrapper.buffer;
    }

    public void sendTransaction(boolean requiresEventLoopSchedule) {
        Object transactionPacket = ByteBufHelper.retainedDuplicate(cachedTransaction);

        if (requiresEventLoopSchedule) {
            ChannelHelper.runInEventLoop(data.getUser().getChannel(), () -> {
                data.getUser().sendPacket(transactionPacket);
            });
        } else {
            data.getUser().sendPacket(transactionPacket);
        }
    }

    public void pre(Runnable runnable) {
        // Runs a runnable when the client accepts the last sent transaction
        this.scheduleTrans(0, runnable);
    }

    public void post(Runnable runnable) {
        // Runs a runnable when the client accepts the next sent transaction
        this.scheduleTrans(1, runnable);
    }

    public int getLastTransactionSent() {
        return this.sent;
    }

    public void handleServerTransaction(WrapperPlayServerWindowConfirmation wrapper) {
        if (wrapper.getActionId() != -1) {
            return;
        }

        sent++;
    }

    public void handleClientTransaction(WrapperPlayClientWindowConfirmation wrapper) {
        if (wrapper.getActionId() != -1) {
            return;
        }

        int currentTransaction = ++received;

        Iterator<TransactionTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            TransactionTask scheduledTask = iterator.next();
            if (scheduledTask.getTransaction() == currentTransaction) {
                iterator.remove();
                scheduledTask.getTask().run();
            }
        }
    }

    private void scheduleTrans(int offset, Runnable runnable) {
        int targetTransaction = sent + offset;

        if (received >= targetTransaction) {
            runnable.run();
            return;
        }

        tasks.add(new TransactionTask(targetTransaction, runnable));
    }

    @AllArgsConstructor
    @Getter
    static class TransactionTask {
        private int transaction;
        private Runnable task;
    };

}

