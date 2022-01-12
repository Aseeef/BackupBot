package dev.aseef.console.commands.listeners;

import dev.aseef.console.commands.ConsoleCommand;
import dev.aseef.console.commands.ConsoleCommandListener;

public class ConsoleHelpCommand implements ConsoleCommandListener {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get a list of all dev.aseef.commands and their description";
    }

    @Override
    public void onCommand(String[] args) {

        StringBuilder help = new StringBuilder()
                .append("Console Commands Help:\n");

        // List of all dev.aseef.commands as string
        for (ConsoleCommandListener command : ConsoleCommand.getInstance().getListeners()) {
            help.append("\n            ")
                    .append(command.getCommand())
                    .append(" - ")
                    .append(command.getDescription());
        }

        System.out.println(help.toString());

    }

}
