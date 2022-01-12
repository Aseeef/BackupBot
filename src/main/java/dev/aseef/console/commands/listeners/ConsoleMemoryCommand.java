package dev.aseef.console.commands.listeners;

import dev.aseef.console.commands.ConsoleCommandListener;

public class ConsoleMemoryCommand implements ConsoleCommandListener {

    @Override
    public void onCommand(String[] args) {
        float absTotal = Math.round((Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float total = Math.round((Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float available = Math.round((Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float using = total - available;
        System.out.println("Current system memory usage: ");
        System.out.println("     Maximum Memory: " + absTotal + " M");
        System.out.println("     Total Memory: " + total + " M");
        System.out.println("     Available Memory: " + available + " M");
        System.out.println("     Using: " + (using) + " M");
    }

    @Override
    public String getCommand() {
        return "memory";
    }

    @Override
    public String getDescription() {
        return "View the memory usage for the bot.";
    }

}
