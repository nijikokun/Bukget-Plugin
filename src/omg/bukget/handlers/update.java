package omg.bukget.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import omg.bukget.BukGet;
import omg.bukget.Constants;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;
import omg.bukget.exceptions.Incompatibility;
import omg.bukget.exceptions.InvalidExtension;
import omg.bukget.exceptions.MissingPackage;
import omg.bukget.exceptions.MissingVersions;
import org.bukkit.command.CommandSender;
import omg.bukget.Repo.Package;
import omg.bukget.Repo.Version;
import omg.bukget.io.mini.Arguments;
import omg.bukget.io.mini.Mini;
import omg.bukget.utils.Messaging;

public class update extends Handler {

    public update(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "update"))
            throw new InvalidUsage("You do not have permission to do this.");

        if(arguments.isEmpty()) 
            throw new InvalidUsage("pkg update <package>");

        String Package = arguments.get("plugin").getStringValue();
        ArrayList<String> Packages = new ArrayList<String>();

        if(Package.isEmpty() || Package == null || Package.equals("0"))
            throw new InvalidUsage("pkg update <package>");

        Mini Database = new Mini(plugin.getDataFolder().getPath(), "installed.mini");
        LinkedHashMap<String, Arguments> Installed = Database.getIndices();
        boolean upgradeAll = false;
        boolean one = false;
        int count = 0;

        if(Package.contains(",")) {
            if(!hasPermissions(sender, "update.multi"))
                throw new InvalidUsage("You do not have permission to do this.");

            Packages.addAll(Arrays.asList(Package.split(",")));
        }

        if(Package.equals("*")) {
            if(!hasPermissions(sender, "update.all"))
                throw new InvalidUsage("You do not have permission to do this.");

            upgradeAll = true;
        }

        if(Package.isEmpty() && Packages.isEmpty() && !upgradeAll)
            throw new InvalidUsage("pkg upgrade <package>");

        if(Packages.isEmpty() && upgradeAll)
            for(String pack: Installed.keySet())
                Packages.add(pack);

        if(Packages.isEmpty() && !upgradeAll)
            Packages.add(Package);

        for(String pack: Packages)
            if(!plugin.Repo.hasPackage(pack)) {
                count++;
                Messaging.send(sender, "<red>404 <silver>Package <rose>" + pack + "<silver> not found in repository. <gray>Skipping...");

                if(count != Packages.size())
                    Messaging.send(sender, " ");
                continue;
            } else {
                Package Pack = plugin.Repo.getPackage(pack);
                Messaging.send(sender, "<green>Found <silver>Package <green>" + Pack.getName() + "<silver> in repository. <gray>Validating...");
                count++;

                if(!plugin.Repo.isInstalled(pack)) {
                    Messaging.send(sender, "<red>Error <silver>Package <rose>" + pack + "<silver> is not installed. <gray>Skipping...");
                    if(count != Packages.size())
                        Messaging.send(sender, " ");
                    continue;
                } else {
                    Messaging.send(sender, "<green>Validated <gray>Checking version...");
                }

                String version = plugin.Repo.getInstalledVersion(pack);
                String branch = plugin.Repo.getInstalledBranch(pack);
                Version Current = null;
                boolean installed = false;

                if(Constants.FOLLOW_BRANCHES)
                    Current = Pack.getLatestBranchVersion(branch);
                else {
                    Current = Pack.getLatestVersion();
                }

                if(Current == null && Constants.FOLLOW_BRANCHES) {
                    Messaging.send(sender, "<red>Error <silver>Couldn't find/follow branch for <rose>" + Pack.getName() + "<silver>. <gray>Substituting latest...");
                    Current = Pack.getLatestVersion();
                }

                if(Current == null) {
                    Messaging.send(sender, "<red>Error <silver>No versions for <rose>" + Pack.getName() + "<silver>. <gray>Skipping...");

                    if(count != Packages.size())
                        Messaging.send(sender, " ");
                    continue;
                }

                if(Current.getVersion().equals(version)) {
                    Messaging.send(sender, "<green>Latest <gray>Skipping...");

                    if(count != Packages.size())
                        Messaging.send(sender, " ");
                    continue;
                }

                Messaging.send(sender, "<yellow>Updating <green>" + Pack.getName() + "<silver> to <purple>@" + Current.getVersion() + ".");

                try {
                    installed = plugin.Repo.installPackage(Pack.getName(), Current.getVersion());
                } catch (MissingPackage ex) {
                    Messaging.send(sender, ex.getMessage());
                    return false;
                } catch (Incompatibility ex) {
                    Messaging.send(sender, ex.getMessage());
                    return false;
                } catch (MissingVersions ex) {
                    Messaging.send(sender, ex.getMessage());
                    return false;
                } catch (IOException ex) {
                    System.out.println(ex);
                    Messaging.send(sender, ex.getMessage());
                    return false;
                } catch (InvalidExtension ex) {
                    Messaging.send(sender, ex.getMessage());
                    return false;
                }

                if(!installed)
                    Messaging.send(sender, "<red>Error <silver>Could not update <rose>" + Pack.getName() + "<silver> to <purple>@" + Current.getVersion() + ".");
                else {
                    Messaging.send(sender, "<green>Success <silver>Updated " + Pack.getName() + "<silver> to <purple>@" + Current.getVersion() + ".");
                    one = true;
                }

                count++;

                if(count != Packages.size())
                    Messaging.send(sender, " ");
            }

        if(one)
            Messaging.send(sender, "<gray>Restart or Reload the server to complete.");

        return false;
    }
}
