package omg.bukget.handlers;

import java.util.ArrayList;
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
        ArrayList<String> plugins = new ArrayList<String>();
        String list = "", color = "<silver>", installed = "<green>", outdated = "<rose>";
        int count = 0, i = 0;

        if(filter != null && !filter.equalsIgnoreCase("default") && !filter.equals("0"))
            Messaging.send(sender, "[<blue>BukGet<white>] <yellow>Package list with filter: <white>" + filter);
        else
            Messaging.send(sender, "[<blue>BukGet<white>] Package list");

        for(String entry: Packages.keySet()) {
            Package pack = Packages.get(entry);
            String desc = pack.getDescription();

            if(filter != null && !filter.equalsIgnoreCase("default") && !filter.equals("0"))
                if(!contains(pack.getName(), filter) && !contains(desc, filter) && !hasCategory(filter, pack.getCategories()))
                    continue;

            if(desc.length() > 91)
                desc = desc.substring(0, 91) + "...";

            if(isConsole(sender))
                Messaging.send(sender, String.format("%1s - <silver>%2s", pack.getName(), desc));
            else {
                if(i == 5 || list.length() > 100) {
                    Messaging.send(sender, list.substring(0, list.length()-2));
                    list = "";
                    i = 0;
                }

                if(plugin.Repo.isInstalled(pack.getName()))
                    list += installed + pack.getName() + "<white>, ";
                else
                    list += color + pack.getName() + "<white>, ";

                i++;
            }

            count++;
        }


        if(count == 0)
            Messaging.send(sender, "No results found.");
        else if(!isConsole(sender) && !list.equals(""))
            Messaging.send(sender, list.substring(0, list.length()-2));

        return false;
    }

    private boolean contains(String original, String haystack) {
        if(original == null || haystack == null)
            return false;

        if(original.contains(haystack))
            return true;

        if(original.toLowerCase().contains(haystack.toLowerCase()))
            return true;

        return false;
    }

    private boolean hasCategory(String filter, String[] categories) {
        if((filter == null ? "default" == null : filter.equals("default")))
            return true;

        for (String cat : categories)
            if(contains(cat, filter) || cat.equalsIgnoreCase(filter))
                return true;

        return false;
    }
}
