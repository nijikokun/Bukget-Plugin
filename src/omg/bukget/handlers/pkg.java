package omg.bukget.handlers;

import java.util.LinkedHashMap;

import omg.bukget.BukGet;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;
import omg.bukget.utils.Calculator;
import omg.bukget.utils.Messaging;
import org.bukkit.command.CommandSender;

public class pkg extends Handler {
    private int width = 325;

    public pkg(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "help"))
            throw new InvalidUsage("You do not have permission to do that.");

        String help_top = Messaging.parse("`S------`s[ `wBukGet Help `s]`S------");
        String dashes = Calculator.dashes(width - Calculator.getStringWidth(help_top)) + "---";

        Messaging.send(sender, help_top + "`S" + dashes);

        for (String action : plugin.Commands.getHelp().keySet()) {
            if(!plugin.hasPermissions(sender, plugin.Commands.getPermission(action))) {
                continue;
            }

            String description = plugin.Commands.getHelp(action)[1];
            String command = "/pkg `w" + action + plugin.Commands.getHelp(action)[0] + "`s";
            command = command
                    .replace("[", "`S[`s")
                    .replace("]", "`S]")
                    .replace("(", "`S(");
            Messaging.send(sender, String.format(" %1$s `S-`s %2$s", command, description));
        }

        return false;
    }
}
