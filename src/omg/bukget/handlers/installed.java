package omg.bukget.handlers;

import omg.bukget.io.mini.Arguments;
import omg.bukget.io.mini.Mini;
import java.util.LinkedHashMap;

import org.bukkit.command.CommandSender;

import omg.bukget.BukGet;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;

import omg.bukget.Repo.Package;
import omg.bukget.Repo.Version;


import omg.bukget.utils.Messaging;

public class installed extends Handler {

    public installed(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "installed"))
            throw new InvalidUsage("You do not have permission to do that.");

        if(arguments.get("filter").getStringValue().isEmpty() || arguments.get("filter").getStringValue() == null)
            throw new InvalidUsage("Filter argument is empty!");

        String filter = arguments.get("filter").getStringValue();
        Mini Database = new Mini(plugin.getDataFolder().getPath(), "installed.mini");
        LinkedHashMap<String, Arguments> Installed = Database.getIndices();
        int count = 0;

        Messaging.send(sender, "[BUKGET] Current Packages / Plugins Installed:");

        for(String entry: Installed.keySet()) {
            Package pack = null;
            Version version = null;
            String name = entry;
            String latest = "Unknown";
            String latestBranch = "Unknown";
            String verdict = "Missing Package";
            String current = plugin.Repo.getInstalledVersion(entry);
            String currentBranch = "???";
            String color = "<gray>";

            if(plugin.Repo.hasPackage(entry)) {
                pack = plugin.Repo.getPackage(entry);
                name = pack.getName();

                if(!current.equals("???")) {
                    version = pack.getLatestVersion();
                    if(version != null) latest = version.getVersion();

                    currentBranch = plugin.Repo.getInstalledBranch(entry);
                    version = pack.getLatestBranchVersion(currentBranch);
                    if(version != null) latestBranch = version.getVersion();
                }
            }

            if(!latestBranch.equals("Unknown") && latestBranch.equals(current)) {
                if(!filter.equalsIgnoreCase("default"))
                    if(!filter.equalsIgnoreCase("latest")) continue;
                
                verdict = "LATEST";
                color = "<green>";
            } else if (!latestBranch.equals("Unknown") && !latestBranch.equals(current)) {
                if(!filter.equalsIgnoreCase("default"))
                    if(!filter.equalsIgnoreCase("outdated")) continue;

                verdict = "OUTDATED @" + currentBranch + " is on " + latest;
                color = "<rose>";
            } else {
                if(!filter.equalsIgnoreCase("default"))
                    if(!filter.equalsIgnoreCase("missing")) continue;
            }

            if(plugin.Repo.hasPackage(entry) && verdict.endsWith("Missing Package")) {
                verdict = "Missing Version Info";
                color = "<silver>";
            }
            
            Messaging.send(sender, String.format(" %1$-21s @%2$-8s %3$s %4$35s", name, current, color, verdict));
            count++;
        }

        if(count == 0)
            Messaging.send(sender, " <rose>No results found.");

        return false;
    }
}
