package omg.bukget;

import com.nijikokun.bukkit.Permissions.Permissions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import omg.bukget.Repo.Package;
import omg.bukget.Repo.Version;
import omg.bukget.command.Handler;
import omg.bukget.command.Parser;
import omg.bukget.command.exceptions.InvalidUsage;
import omg.bukget.handlers.*;
import omg.bukget.io.FileManager;
import omg.bukget.io.Remote;
import omg.bukget.utils.Common;
import omg.bukget.utils.Messaging;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.json.simple.parser.ParseException;

public class BukGet extends JavaPlugin {
    public static File directory = null;
    public static String engine = "";
    public static int version = -1;
    public static boolean tSupported = false;

    public PluginDescriptionFile info = null;
    public PluginManager pluginManager = null;
    public Repo Repo = null;

    public Parser Commands = new Parser();
    public Permissions Permissions;
    
    public void onDisable() {

    }

    public void onEnable() {
        pluginManager = getServer().getPluginManager();
        directory = new File(getDataFolder().getPath() + File.separator);
        info = getDescription();

        engine = getServer().getServerName();

        if(getServer().getServerName().equalsIgnoreCase("craftbukkit")) {
            if(!getServer().getVersion().contains("unknown"))
                version = Integer.valueOf(Common.getCraftBukkitVersion(getServer().getVersion()));

            tSupported = ((CraftServer)getServer()).getReader().getTerminal().isANSISupported();
        }

        // Create data folder
        if(!getDataFolder().exists()) (new File(getDataFolder().getPath())).mkdir();

        // Extract Files
        Common.extract("config.yml");

        // Setup Configuration
        Constants.load(new Configuration(new File(directory, "config.yml")));

        // Check the repo for updates
        FileManager repo = new FileManager(getDataFolder().getPath(), "repo.json", false);
        if(repo.exists()) {
            String _RepoMD5 = Remote.checksum(Constants.REPO_URL);
            String _MD5 = repo.checksum();
            
            if(!_RepoMD5.equalsIgnoreCase(_MD5)) {
                repo.delete();
                Remote.fetch(Constants.REPO_URL, getDataFolder().getPath() + File.separator + "repo.json");
            }
        } else {
            if(repo.exists()) repo.delete();
            Remote.fetch(Constants.REPO_URL, getDataFolder().getPath() + File.separator + "repo.json");
        }

        repo.read();

        try {
            Repo = new Repo(getDataFolder().getPath(), repo.getSource());
        } catch (ParseException ex) {
            Logger.getLogger(BukGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(BukGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BukGet.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Let the user know about packages
        System.out.println("[" + info.getName() + "] loaded (" + Repo.packages() + ") packages.");

        // Self updating
        if(Constants.SELF_UPDATE) {
            Package Pack = Repo.getPackage("bukget");

            if(Pack != null) {
                Version v = Pack.getLatestVersion();

                if(!v.getVersion().equals(Repo.getInstalledVersion("bukget"))) {
                    System.out.println("[" + info.getName() + "] updating to version: " + v.getVersion());
                }
            }
        }

        // System Check Installed Plugins, we don't know the version, but that's okay.
        Repo.checkInstalled();

        // Setup Commands
        Commands.add("/pkg", new pkg(this));
        Commands.setPermission("pkg", "bukget.help");

        Commands.add("/pkg -h|help", new pkg(this));
        Commands.setHelp("help", new String[] { "", "Shows this information." });
        Commands.setPermission("help", "bukget.help");

        Commands.add("/pkg -r|reload", new reload(this));
        Commands.setHelp("reload", new String[] { "", "Reload package database, and information." });
        Commands.setPermission("reload", "bukget.reload");

        Commands.add("/pkg -i|install +plugin", new install(this));
        Commands.setHelp("install", new String[]{ " [package]", "Install a Package." });
        Commands.setPermission("install", "bukget.install");

        Commands.add("/pkg -is|installed +filter:default", new installed(this));
        Commands.setHelp("installed", new String[]{ " (filter)", "Show installed packages." });
        Commands.setPermission("installed", "bukget.installed");

        Commands.add("/pkg -u|update +plugin", new update(this));
        Commands.setHelp("update", new String[]{ " [package]", "Update package(s)." });
        Commands.setPermission("update", "bukget.update");
        Commands.setPermission("update.multi", "bukget.update.multi");
        Commands.setPermission("update.all", "bukget.update.all");

        Commands.add("/pkg -l|list +filter:default", new list(this));
        Commands.setHelp("list", new String[]{ "", "List packages in Repo." });
        Commands.setPermission("list", "bukget.list");

        Commands.add("/pkg -nf|info +package +options", new info(this));
        Commands.setHelp("info", new String[]{ " [package] (options)", "View package information." });

        System.out.println("[" + info.getName() + "] initialized commands.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messaging.save(sender);

        Handler handler = Commands.getHandler(command.getName());
        String split = "/" + command.getName().toLowerCase();

        for (int i = 0; i < args.length; i++) {
            split = split + " " + args[i];
        }

        Commands.save(split);
        Commands.parse();

        if(Commands.getHandler() != null)
            handler = Commands.getHandler();

        if(handler == null) return false;
        
        try {
            return handler.perform(sender, Commands.getArguments());
        } catch (InvalidUsage ex) {
            Messaging.send(sender, ex.getMessage());
            return false;
        }
    }

    public boolean hasPermissions(CommandSender sender, String command) {
        if(sender instanceof Player) {
            Player player = (Player)sender;
            if(Commands.hasPermission(command)) {
                String node = Commands.getPermission(command);

                if(this.Permissions != null)
                    return Permissions.Security.permission(player, node);
                else {
                    // Fallback for older versions.
                    try {
                        return player.hasPermission(node);
                    } catch(Exception e) {
                        return player.isOp();
                    }
                }
            }
        }

        return true;
    }
}