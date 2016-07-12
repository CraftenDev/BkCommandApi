package de.craften.plugins.bkcommandapi;

/**
 * A handler for commands. Implementations of this interface can be registered to a {@link SubCommandHandler}. Any
 * public method annotated with {@link Command} will be treated as a command.
 *
 * @see Command
 * @see SubCommandHandler
 */
public interface CommandHandler {
    /**
     * The result of a command. This enum may be used as return type for command methods annotated with {@link Command}.
     */
    enum Result {
        /**
         * The command was executed.
         */
        Done,
        /**
         * The command was not found.
         */
        CommandNotFound,
        /**
         * The sender that invoked the command has no permission to use it.
         */
        NoPermission
    }
}
