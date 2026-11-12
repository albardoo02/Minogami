package world.minogami.thread;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 * A callback registered with {@link WorldTickScheduler#registerTickHook}.
 * <p>
 * It runs once per tick on the thread that is ticking its world, until it is
 * cancelled, its plugin is disabled, or the world is unloaded.
 */
public interface WorldTickHook {

    Plugin getPlugin();

    World getWorld();

    TickPhase getPhase();

    /**
     * Stops the hook. Safe to call from any thread, including from inside the hook itself.
     * If the hook is running when this is called, that run is allowed to finish.
     */
    void cancel();

    boolean isCancelled();
}
