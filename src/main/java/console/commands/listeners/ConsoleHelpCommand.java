package console.commands.listeners;

import console.commands.ConsoleCommand;
import console.commands.ConsoleCommandListener;

public class ConsoleHelpCommand implements ConsoleCommandListener {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get a list of all commands and their description";
    }

    @Override
    public void onCommand(String[] args) {

        StringBuilder help = new StringBuilder()
                .append("Console Commands Help:\n");

        // List of all commands as string
        for (ConsoleCommandListener command : ConsoleCommand.getInstance().getListeners()) {
            help.append("\n            ")
                    .append(command.getCommand())
                    .append(" - ")
                    .append(command.getDescription());
        }

        System.out.println(help.toString());

    }

}
