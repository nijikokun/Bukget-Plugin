package omg.bukget.command;

import java.util.LinkedHashMap;
import java.util.List;
import omg.bukget.BukGet;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class Handler {

    protected static BukGet plugin;

    public Handler(BukGet plugin) {
        this.plugin = plugin;
    }

    public abstract boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage;

    protected static boolean isConsole(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean hasPermissions(CommandSender sender, String command) {
        if(sender instanceof Player) {
            Player player = (Player)sender;

            if(plugin.Commands.hasPermission(command)) {
                String node = plugin.Commands.getPermission(command);

                if(plugin.Permissions != null)
                    return plugin.Permissions.Security.permission(player, node);
                else
                    return player.isOp();
            } else {
                return player.isOp();
            }
        }

        return true;
    }

    protected static Player getPlayer(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            List<Player> players = sender.getServer().matchPlayer(args[index]);

            if (players.isEmpty()) {
                sender.sendMessage("Could not find player with the name: " + args[index]);
                return null;
            } else {
                return players.get(0);
            }
        } else {
            if (isConsole(sender)) {
                return null;
            } else {
                return (Player)sender;
            }
        }
    }
}
