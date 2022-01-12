package dev.aseef.console.commands.listeners;

import dev.aseef.console.commands.ConsoleCommandListener;

public class ConsoleStopCommand implements ConsoleCommandListener {

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Gracefully stop the bot";
    }

    @Override
    public void onCommand(String[] args) {
        System.exit(0);
    }

}
