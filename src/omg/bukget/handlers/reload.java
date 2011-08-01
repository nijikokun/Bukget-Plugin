package omg.bukget.handlers;

import java.io.File;
import java.util.LinkedHashMap;

import omg.bukget.BukGet;
import omg.bukget.Constants;
import omg.bukget.Repo;
import omg.bukget.Repo.Package;
import omg.bukget.Repo.Version;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser.Argument;
import omg.bukget.command.exceptions.InvalidUsage;
import omg.bukget.io.FileManager;
import omg.bukget.io.Remote;
import omg.bukget.utils.Messaging;
import org.bukkit.command.CommandSender;

public class reload extends Handler {
    private int width = 325;

    public reload(BukGet plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "reload"))
            throw new InvalidUsage("You do not have permission to do that.");

        String pluginFolder = BukGet.directory.getPath();
        FileManager repo = new FileManager(pluginFolder, "repo.json", false);

        if(repo.exists()) {
            String _RepoMD5 = Remote.checksum(Constants.REPO_URL);
            String _MD5 = repo.checksum();

            if(!_RepoMD5.equals(_MD5)) {
                repo.delete();
                Remote.fetch(Constants.REPO_URL, pluginFolder + File.separator + "repo.json");
            }
        } else {
            if(repo.exists()) repo.delete();
            Remote.fetch(Constants.REPO_URL, pluginFolder + File.separator + "repo.json");
        }

        repo.read();

        try { plugin.Repo = new Repo(pluginFolder, repo.getSource()); } catch (Exception ex) {
            System.out.println(ex);
        }

        // Let the user know about packages
        Messaging.send("`S[" + plugin.info.getName() + "] loaded (" + plugin.Repo.packages() + ") packages.");

        // Self updating
        if(Constants.SELF_UPDATE) {
            Package Pack = plugin.Repo.getPackage("bukget");

            if(Pack != null) {
                Version v = Pack.getLatestVersion();

                if(!v.getVersion().equals(plugin.Repo.getInstalledVersion("bukget"))) {
                    Messaging.send("`S[" + plugin.info.getName() + "] updating to version: " + v.getVersion());
                }
            }
        }

        // System Check Installed Plugins, we don't know the version, but that's okay.
        plugin.Repo.checkInstalled();

        // Let the user know about packages
        Messaging.send("`S[" + plugin.info.getName() + "] Finished reloading.");

        return false;
    }
}
