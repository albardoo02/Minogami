package world.minogami.thread;

/**
 * When a {@link WorldTickHook} runs within its world's tick.
 */
public enum TickPhase {

    /**
     * Before the world is ticked (entities, block entities, chunks …),
     * after pending handoffs have run.
     */
    PRE,

    /**
     * After the world has been ticked.
     */
    POST
}
