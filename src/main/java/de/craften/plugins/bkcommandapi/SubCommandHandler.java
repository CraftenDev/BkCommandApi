package de.craften.plugins.bkcommandapi;


import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles sub-commands of a command conveniently.
 */
public abstract class SubCommandHandler implements org.bukkit.command.CommandExecutor {
    private String parentCommand;
    private List<CommandHandler> handlers;

    /**
     * Creates a new PluginCommandHandler for the given plugin that uses the specified main command.
     *
     * @param parentCommand Main command, this is the prefix of all commands
     */
    public SubCommandHandler(String parentCommand) {
        this.parentCommand = parentCommand;
        handlers = new ArrayList<>();
    }

    @Override
    public final boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        String subCommand = args.length > 0 ? args[0] : "";

        if (args.length > 1) {
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args = new String[0];
        }

        if (subCommand.equals("help")) {
            if (args.length == 1) {
                onUsageHelpCommand(sender, args[0]);
                return true;
            } else {
                onHelpCommand(sender);
                return true;
            }
        }

        List<Command> possibleCommands = new ArrayList<>();
        for (CommandHandler handler : handlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command methodCommand = method.getAnnotation(Command.class);
                    if (((methodCommand.value().length == 0 && subCommand.isEmpty()) || containsAny(methodCommand.value(), subCommand))
                            && (methodCommand.allowFromConsole() || sender instanceof Player)) {
                        possibleCommands.add(methodCommand);

                        if (args.length >= methodCommand.min() && args.length <= methodCommand.max()) {
                            if (!methodCommand.permission().isEmpty() && !sender.hasPermission(methodCommand.permission())) {
                                onPermissionDenied(sender, command, args);
                                return true;
                            }

                            try {
                                Object result;
                                if (methodCommand.min() > 0 || methodCommand.max() > 0
                                        || method.getParameterTypes().length == 3) { //handler may not have the third argument if there are no arguments
                                    result = method.invoke(handler, sender, args);
                                } else {
                                    result = method.invoke(handler, sender);
                                }

                                if (result instanceof Boolean) {
                                    return (boolean) result;
                                } else if (result instanceof CommandHandler.Result) {
                                    switch ((CommandHandler.Result) result) {
                                        case NoPermission:
                                            onPermissionDenied(sender, command, args);
                                            return true;
                                        case Done:
                                            return true;
                                        case InvalidUsage: // intentionally empty
                                        default:
                                            break;
                                    }
                                } else {
                                    return true;
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                Bukkit.getLogger().severe("Error invoking handler for command: " + command.toString());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        if (possibleCommands.isEmpty()) {
            onInvalidCommand(sender);
        } else {
            for (Command cmd : possibleCommands) {
                sendUsageHelp(sender, cmd);
            }
        }
        return false;
    }

    /**
     * Checks if the given array contains the given string (case insensitive).
     *
     * @param haystack array to search for the needle
     * @param needle   string to search in the array
     * @return true if the string was found in the array, false if not
     */
    private static boolean containsAny(String[] haystack, String needle) {
        for (String s : haystack) {
            if (needle.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers all {@link Command}-annotated methods of the given objects.
     *
     * @param handlers Instances that contains the handlers to register
     */
    public void addHandlers(CommandHandler... handlers) {
        Collections.addAll(this.handlers, handlers);
    }

    /**
     * Gets called if a command is unknown.
     *
     * @param sender Sender of the unknown command
     */
    protected abstract void onInvalidCommand(CommandSender sender);

    /**
     * Gets called if the sender doesn't have permission to use the command.
     *
     * @param sender  The sender that is trying to use the command
     * @param command The command the sender is trying to use
     * @param args    The arguments the sender is trying to invoke the command with
     */
    protected abstract void onPermissionDenied(CommandSender sender, org.bukkit.command.Command command, String[] args);

    /**
     * Sends help to the given target.
     *
     * @param sender Console/player to send the help to
     */
    private void onHelpCommand(CommandSender sender) {
        for (CommandHandler handler : handlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command methodCommand = method.getAnnotation(Command.class);
                    if ((sender instanceof Player || methodCommand.allowFromConsole()) &&
                            (methodCommand.permission().isEmpty() || sender.hasPermission(methodCommand.permission()))) {
                        sendHelpLine(sender, methodCommand);
                    }
                }
            }
        }
    }

    /**
     * Sends help to the given target.
     *
     * @param sender  Console/player to send the help to
     * @param command Subcommand to get the usage for
     */
    private void onUsageHelpCommand(CommandSender sender, String command) {
        for (CommandHandler handler : handlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command methodCommand = method.getAnnotation(Command.class);
                    if ((containsAny(methodCommand.value(), command) && (sender instanceof Player || methodCommand.allowFromConsole())) &&
                            (methodCommand.permission().isEmpty() || sender.hasPermission(methodCommand.permission()))) {
                        sendUsageHelp(sender, methodCommand);
                    }
                }
            }
        }
    }

    /**
     * Sends the help line of the given command to the given command sender.
     *
     * @param sender  command sender to send the help line to
     * @param command command to send the help line for
     */
    protected void sendHelpLine(CommandSender sender, Command command) {
        if (command.value().length > 0) {
            sender.sendMessage("/" + parentCommand + " " + command.value()[0] + " - " + command.description());
        } else {
            sender.sendMessage("/" + parentCommand + " - " + command.description());
        }
    }

    /**
     * Sends usage help for the given command to the given command sender.
     *
     * @param sender  command sender to send the usage help to
     * @param command command to send the usage help for
     */
    protected void sendUsageHelp(CommandSender sender, Command command) {
        sender.sendMessage(command.description());
        if (command.usage().length > 0) {
            for (String usage : command.usage()) {
                sender.sendMessage("/" + parentCommand + " " + usage);
            }
        } else {
            if (command.value().length > 0) {
                sender.sendMessage("/" + parentCommand + " " + command.value()[0] + " - " + command.description());
            } else {
                sender.sendMessage("/" + parentCommand + " - " + command.description());
            }
        }
    }
}