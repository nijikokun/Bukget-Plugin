package omg.bukget.handlers;

import java.io.IOException;
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

import omg.bukget.utils.Messaging;

public class install extends Handler {

    public install(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "install"))
            throw new InvalidUsage("You do not have permission to do that.");

        if(arguments.isEmpty()) 
            throw new InvalidUsage("pkg install <package>");

        String Package = arguments.get("plugin").getStringValue();

        if(Package.isEmpty() || Package == null || Package.equals("0"))
            throw new InvalidUsage("pkg install <package>");

        Version v = null;
        String[] data = new String[]{};
        String version = null;
        String branch = null;
        boolean installed = false;

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

        if(Package.isEmpty())
            throw new InvalidUsage("pkg install <package>");

        if(!plugin.Repo.hasPackage(Package)) {
            Messaging.send(sender, "<red>404<white> Package <rose>" + Package + "<white> not found in repository.");
            return false;
        }

        Package pack = plugin.Repo.getPackage(Package);

        if(version != null)
            if(pack.hasVersion(version))
                v = pack.getVersion(version);

        if(branch != null)
            if(pack.getLatestBranchVersion(branch) != null)
                v = pack.getLatestBranchVersion(branch);

        if(version == null && branch == null)
            v = pack.getLatestVersion();

        if(v != null) {
            version = v.getVersion();
        } else {
            String method = (version != null) ? "@" + version : (branch != null) ? "#" + branch : "";
            Messaging.send(sender, "<red>404<white> Package <rose>" + pack.getName() + "<gray>" + method + "<white> does not exist.");
            return false;
        }

        if(plugin.Repo.isInstalled(Package) && plugin.Repo.getInstalledVersion(Package).equals(version)) {
            Messaging.send(sender, "<rose>Package <white>" + pack.getName() + "<gray>@" + version + "<rose> is already installed.");
            return false;
        }

        Messaging.send(sender, "Installing <green>" + pack.getName() + "@" + version + ".");

        try {
            installed = plugin.Repo.installPackage(pack.getName(), version);
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
            Messaging.send(sender, "<red>Error<white> Could not install <rose>" + pack.getName() + "@" + version + ".");
        else {
            Messaging.send(sender, "<green>Success<white> Installed " + pack.getName() + "@" + version + ".");

            if(Constants.AUTO_RELOAD)
                plugin.getServer().reload();
            else {
                Messaging.send(sender, "<gray>Restart or Reload the server to complete.");
            }
        }

        return false;
    }
}
