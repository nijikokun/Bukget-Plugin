package omg.bukget.handlers;

import omg.bukget.io.mini.Arguments;
import omg.bukget.io.mini.Mini;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.bukkit.command.CommandSender;

import omg.bukget.BukGet;
import omg.bukget.Repo.Engine;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.*;

import omg.bukget.Repo.Package;
import omg.bukget.Repo.Version;
import omg.bukget.utils.Common;

import omg.bukget.utils.Messaging;

public class info extends Handler {

    public info(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "info"))
            throw new InvalidUsage("You do not have permission to do that.");

        Messaging.save(sender);
        
        String Package = arguments.get("package").getStringValue();
        String version = null;
        String branch = null;
        Version v = null;
        boolean installed = false;

        if(Package.isEmpty() || Package == null || Package.equals("0"))
            throw new InvalidUsage("pkg info <package> (options)");

        if(Package.contains("@")) {
            version = Package.split("@")[1];
            Package = Package.split("@")[0];
        }

        if(Package.contains("#")) {
            branch = Package.split("#")[1];
            Package = Package.split("#")[0];
        }
        
        if(version != null && version.contains("#")) {
            version = version.split("#")[0];
        }

        if(Package.isEmpty() || Package == null || Package.equals("0"))
            throw new InvalidUsage("pkg info <package> (options)");

        if(!plugin.Repo.hasPackage(Package)) {
            Messaging.send("<red>404<white> Package <rose>" + Package + "<white> not found in repository.");
            return false;
        }

        installed = plugin.Repo.isInstalled(Package);
        Package pack = plugin.Repo.getPackage(Package);
        Version lv = pack.getLatestVersion();

        if(version != null)
            if(pack.hasVersion(version))
                v = pack.getVersion(version);

        if(branch != null)
            if(pack.getLatestBranchVersion(branch) != null)
                v = pack.getLatestBranchVersion(branch);

        if(v == null && (branch != null || version != null)) {
            String method = (version != null) ? "@" + version : "#" + branch;
            Messaging.send(sender, "<red>404<white> Package <rose>" + pack.getName() + method + "<white> does not exist.");
            return false;
        }

        if(v != null) {
            Messaging.send("<green>" + pack.getName() + "@" + v.getVersion() + " <white>(" + v.getBranch().toUpperCase() + ")");
            Messaging.send("<gray>" + v.getChecksum());
                    Messaging.send(" ");
            Messaging.send("Dependencies: <silver>" + ((v.getDependencies().length > 0) ? Arrays.toString(v.getDependencies()) : "None"));
            Messaging.send("Optional: <silver>" + ((v.getOptionalDependencies().length > 0) ? Arrays.toString(v.getOptionalDependencies()) : "None"));
                    Messaging.send(" ");
            Messaging.send("Supported Engines:");

            for(Engine e: v.getEngines())
                    Messaging.send("   <purple>" + e.getName() + " <silver>(b"+ e.getMin() +" - " + e.getMax() + ")");

            return false;
        }

        Messaging.send("<green>" + pack.getName() + " <white>" + ((pack.getWebsite().isEmpty()) ? "" : "(" + pack.getWebsite() + ")"));
        Messaging.send("<gray>by: " + Arrays.toString(pack.getAuthors()));
                Messaging.send(" ");
        Messaging.send("Maintained By: <silver>" + pack.getAuthor());
        Messaging.send("Latest Version: <purple>" + lv.getVersion() + "#" + lv.getBranch());
                Messaging.send(" ");
        Messaging.send("Description: ");

        for(String s: Common.split(pack.getDescription().replace("\n", ""), 120))
                Messaging.send("   <silver>" + s);

        return false;
    }
}
