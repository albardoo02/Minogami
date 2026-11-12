package world.minogami.thread;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Runs plugin code on the thread that ticks a given world.
 * <p>
 * Minogami ticks every world on its own thread, in parallel. Code that only touches one world
 * can run on that world's thread and so in parallel with other worlds, without taking the
 * global event lock and without blocking on {@link ConcurrentTaskHandler}.
 *
 * <h2>Tick order of a world</h2>
 * <ol>
 *     <li>pending {@link WorldTask}s</li>
 *     <li>handoffs queued before this tick started ({@link #handoff})</li>
 *     <li>{@link TickPhase#PRE} hooks</li>
 *     <li>the world tick itself</li>
 *     <li>{@link TickPhase#POST} hooks</li>
 *     <li>pending {@link WorldTask}s</li>
 * </ol>
 *
 * <h2>Rules</h2>
 * <ul>
 *     <li>Inside a hook or handoff, only touch the world it belongs to. To act on another world,
 *     {@link #handoff} to that world instead of waiting for it.</li>
 *     <li>Do not block (I/O, waiting on other threads). Every world waits for all others at the end of each tick,
 *     so a slow world delays the whole server.</li>
 *     <li>Exceptions are caught and logged per hook; they do not stop the world tick or other hooks.</li>
 * </ul>
 *
 * <h2>Availability</h2>
 * This class only exists on Minogami. A plugin that should also run on Paper should check for it first, e.g.
 * {@code Class.forName("world.minogami.thread.WorldTickScheduler")}, and then {@link #isSupported()}.
 */
public abstract class WorldTickScheduler {

    private static final String IMPLEMENTATION = "world.minogami.thread.ImplWorldTickScheduler";

    private static volatile WorldTickScheduler INSTANCE = null;
    private static volatile boolean resolved = false;

    protected WorldTickScheduler() {}

    private static WorldTickScheduler instance() {
        if (!resolved) {
            synchronized (WorldTickScheduler.class) {
                if (!resolved) {
                    try {
                        Class<?> type = Class.forName(IMPLEMENTATION, true, WorldTickScheduler.class.getClassLoader());
                        INSTANCE = (WorldTickScheduler) type.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException | LinkageError ignored) {
                        INSTANCE = null;
                    }
                    resolved = true;
                }
            }
        }
        return INSTANCE;
    }

    private static WorldTickScheduler require() {
        WorldTickScheduler scheduler = instance();
        if (scheduler == null) {
            throw new UnsupportedOperationException("WorldTickScheduler is not available on this server");
        }
        return scheduler;
    }

    /**
     * @return whether the running server implements this API
     */
    public static boolean isSupported() {
        return instance() != null;
    }

    /**
     * Registers a callback that runs every tick on the thread ticking {@code world}.
     * Can be called from any thread; the hook starts at the next {@code phase} of that world.
     *
     * @throws IllegalStateException if the plugin is disabled or the world is not loaded
     */
    public static WorldTickHook registerTickHook(Plugin plugin, World world, TickPhase phase, Runnable task) {
        return require().registerTickHookImpl(plugin, world, phase, task);
    }

    /**
     * Runs {@code task} once on the thread ticking {@code world}, at the start of that world's next tick.
     * Never blocks the caller, and never runs the task inline, even when called from that world's own thread.
     * <p>
     * The future fails if the plugin is disabled, the world is not loaded or the server is stopping.
     * If the world is unloaded after this call, the future may never complete; do not wait on it from a world thread.
     */
    public static <T> CompletableFuture<T> handoff(Plugin plugin, World world, Supplier<T> task) {
        return require().handoffImpl(plugin, world, task);
    }

    /**
     * Same as {@link #handoff(Plugin, World, Supplier)} for a task without a result.
     */
    public static CompletableFuture<Void> handoff(Plugin plugin, World world, Runnable task) {
        if (task == null) throw new NullPointerException("task");
        return require().handoffImpl(plugin, world, () -> {
            task.run();
            return null;
        });
    }

    /**
     * @return whether the current thread is the one ticking {@code world} right now
     */
    public static boolean isWorldThread(World world) {
        return require().isWorldThreadImpl(world);
    }

    /**
     * @return how many times {@code world} has been ticked by its world thread
     */
    public static long getWorldTick(World world) {
        return require().getWorldTickImpl(world);
    }

    protected abstract WorldTickHook registerTickHookImpl(Plugin plugin, World world, TickPhase phase, Runnable task);

    protected abstract <T> CompletableFuture<T> handoffImpl(Plugin plugin, World world, Supplier<T> task);

    protected abstract boolean isWorldThreadImpl(World world);

    protected abstract long getWorldTickImpl(World world);
}
