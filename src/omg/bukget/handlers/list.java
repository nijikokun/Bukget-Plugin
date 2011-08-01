package omg.bukget.handlers;

import java.util.LinkedHashMap;

import org.bukkit.command.CommandSender;

import omg.bukget.BukGet;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;
import omg.bukget.Repo.Package;
import omg.bukget.utils.Messaging;

public class list extends Handler {

    public list(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "list"))
            throw new InvalidUsage("You do not have permission to do that.");

        if(arguments.get("filter").getStringValue().isEmpty() || arguments.get("filter").getStringValue() == null)
            throw new InvalidUsage("Filter argument is empty!");

        String filter = arguments.get("filter").getStringValue();
        LinkedHashMap<String, Package> Packages = plugin.Repo.getPackages();
        int count = 0;

        for(String entry: Packages.keySet()) {
            Package pack = Packages.get(entry);
            String desc = pack.getDescription();

            if(desc.length() > 91)
                desc = desc.substring(0, 91) + "...";

            Messaging.send(sender, String.format("%1s - <silver>%2s", pack.getName(), desc));
            count++;
        }

        if(count == 0)
            Messaging.send(sender, "No results found.");

        return false;
    }
}
