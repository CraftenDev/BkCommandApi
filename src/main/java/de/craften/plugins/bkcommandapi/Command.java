package de.craften.plugins.bkcommandapi;

import java.lang.annotation.*;

/**
 * Annotation for methods in a {@link CommandHandler} that handle commands.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * The command.
     *
     * @return the command
     */
    String[] value() default {};

    /**
     * The permission that is required to use this command.
     *
     * @return the permission that is required to use this command
     */
    String permission() default "";

    /**
     * A string describing the usage of this command.
     *
     * @return a string describing the usage of this command
     */
    String[] usage() default {};

    /**
     * The minimum number of arguments to use this command with.
     *
     * @return the minimum number of arguments to use this command with
     */
    int min() default 0;

    /**
     * The maximum number of arguments to use this command with.
     *
     * @return the maximum number of arguments to use this command with
     */
    int max() default 0;

    /**
     * Whether this command may be used from the console.
     *
     * @return whether this command may be used from the console
     */
    boolean allowFromConsole() default false;

    /**
     * A description of this command.
     *
     * @return a description of this command
     */
    String description() default "";
}
