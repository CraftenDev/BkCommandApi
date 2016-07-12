package de.craften.plugins.bkcommandapi;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String[] value() default {};

    String permission() default "";

    String[] usage() default {};

    int min() default 0;

    int max() default 0;

    boolean allowFromConsole() default false;

    String description() default "";
}
