package omg.bukget;

import omg.bukget.io.mini.Arguments;
import omg.bukget.io.mini.Mini;
import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import omg.bukget.exceptions.Incompatibility;
import omg.bukget.exceptions.InvalidExtension;
import omg.bukget.exceptions.MissingPackage;
import omg.bukget.exceptions.MissingVersions;

import omg.bukget.io.Remote;
import omg.bukget.io.data.JSON;
import omg.bukget.io.encryption.ZIP;
import omg.bukget.io.jar.Resources;
import omg.bukget.utils.Common;

import omg.bukget.utils.Common.vSorter;
import omg.bukget.utils.Messaging;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Repo {
    private String folder;
    private Mini Database;
    private JSONArray repo;
    private JSON JSON;
    private LinkedHashMap<String, Package> Packages = new LinkedHashMap<String, Package>();

    public Repo(String folder, String source) throws ParseException, MalformedURLException, IOException {
        this.folder = folder;
        this.Database = new Mini(folder, "installed.mini");
        this.JSON = new JSON();
        this.repo = JSON.getArray(JSON.setSource(source).parse());

        for(Object data: this.repo) {
            JSONObject info = this.JSON.getObject(data);
            JSONArray versions = this.JSON.getArray(info, "versions");

            String name = JSON.getString(info, "name");
            String author = JSON.getString(info, "maintainer");
            String description = JSON.getString(info, "description");
            String website = JSON.getString(info, "website");
            String[] authors = JSON.getStringArray(info, "authors");
            String[] categories = JSON.getStringArray(info, "categories");

            HashMap<String, Version> Versions = new HashMap<String, Version>();

            for(Object v: versions) {
                JSONObject version = JSON.getObject(v);
                JSONArray engines = JSON.getArray(version, "engines");
                LinkedList<Engine> Engines = new LinkedList<Engine>();

                String branch = JSON.getString(version, "branch");
                String current = JSON.getString(version, "version");
                String checksum = JSON.getString(version, "checksum");
                String location = JSON.getString(version, "location");
                String[] dependencies = JSON.getStringArray(version, "required_dependencies");
                String[] optional = JSON.getStringArray(version, "optional_dependencies");

                for(Object e: engines) {
                    JSONObject engine = this.JSON.getObject(e);
                    String type = JSON.getString(engine, "engine");
                    int max = JSON.getInteger(engine, "build_max");
                    int min = JSON.getInteger(engine, "build_min");
                    Engines.add(new Engine(type, min, max));
                }

                Versions.put(current,new Version(current, branch, checksum, location, Engines, dependencies, optional));
            }

            Packages.put(name.toLowerCase(), new Package(name, author, authors, website, description, categories, Versions));
        }
    }

    public int packages() {
        return Packages.size();
    }

    public LinkedHashMap<String, Package> getPackages() {
        return Packages;
    }
    
    public boolean hasPackage(String name) {
        return Packages.containsKey(name.toLowerCase());
    }

    private String checkGetVersion(String j) {
        String path = new File(".", "plugins").getPath();
        String jarPath = new File(path, j).getPath();

        Resources jar = new Resources(jarPath);
        byte[] buff = jar.getResource("plugin.yml");

        if (buff == null)
            return "???";
        else {
            String str = new String(buff);
            String[] data = str.split("\n");
            String version = "";

            for(String line: data)
                if(line.startsWith("version:"))
                    version = line.replace("version:", "").trim();

            return Common.getCleanVersion(version);
        }
    }

    public void checkInstalled() {
        String files;
        ArrayList<String> found = new ArrayList<String>();
        String path = new File(".", "plugins").getPath();
        File folder = new File(path);
        File[] fileList = folder.listFiles();

        for(File file: fileList) {
            String name = file.getName();

            if(name.endsWith(".jar")) {
                String parsed = name.replace(".jar", "");

                if(!this.Database.hasIndex(parsed)) {
                    Arguments entry = new Arguments(parsed);
                    entry.setValue("version", getClosestVersion(parsed, checkGetVersion(name)));

                    this.Database.addIndex(entry.getKey(), entry);
                    this.Database.update();
                }

                if(this.Database.hasIndex(parsed))
                    found.add(parsed.toLowerCase());
            }
        }

        for(String data: this.Database.getIndices().keySet())
            if(!found.contains(data)) this.Database.removeIndex(data);

        this.Database.update();
    }
    
    public boolean isInstalled(String pack) {
        return this.Database.hasIndex(pack);
    }
    
    public String getInstalledVersion(String pack) {
        return this.Database.getArguments(pack).getValue("version");
    }

    public String getClosestVersion(String pack, String version) {
        Package Pack = this.getPackage(pack);

        if(Pack == null)
            return version;

        for(String vrsn: Pack.getVersions().keySet())
            if(Common.getNumericVersion(version) == Common.getNumericVersion(vrsn))
                return vrsn;

        return version;
    }

    public String getInstalledBranch(String pack) {
        if(!hasPackage(pack))
            return "???";

        String version = this.Database.getArguments(pack).getValue("version");

        if(version.equals("???"))
            return "???";

        Package Pack = this.getPackage(pack);
        Version Version = Pack.getVersion(version);

        if(Version == null) {

            return "???";
        }

        return Version.getBranch();
    }
    
    public boolean installPackage(String name)
    throws
    MissingVersions,
    MissingPackage,
    IOException,
    InvalidExtension,
    Incompatibility {
        if(!hasPackage(name.toLowerCase())) return false;
        
        Package Pack = Packages.get(name.toLowerCase());
        Version version = Pack.getLatestVersion();
        
        if(version == null) return false;
        
        return installPackage(name, version.getVersion(), false);
    }

    public boolean installPackage(String name, String which)
    throws
    MissingVersions,
    MissingPackage,
    IOException,
    InvalidExtension,
    Incompatibility {
        return installPackage(name, which, false);
    }

    public boolean installPackage(String name, String which, boolean force)
    throws
    MissingVersions,
    MissingPackage,
    InvalidExtension,
    Incompatibility,
    IOException {
        if(!hasPackage(name.toLowerCase()))
            throw new MissingPackage("No package found with the name " + name + "!");

        Package Pack = Packages.get(name.toLowerCase());
        Version version = Pack.getVersion(which);

        if(version == null)
            throw new MissingVersions("Package " + name + " is missing version " + which + "!");

        if(version.hasEngine(BukGet.engine))
            if(BukGet.version != -1)
                if(version.getEngine(BukGet.engine).getMin() > BukGet.version)
                    throw new Incompatibility("Package " + name + " is incompatible with " + BukGet.engine + " #" + BukGet.version);

        if(Constants.REQUIRE_DEPENDENCIES)
            for(String dependency: version.getDependencies())
                if(hasPackage(dependency))
                    if(!isInstalled(dependency))
                        throw new Incompatibility("Package " + name + " requires " + dependency + " to be installed.");

        if(Constants.REQUIRE_OPTIONAL_DEPENDENCIES)
            for(String dependency: version.getOptionalDependencies())
                if(hasPackage(dependency))
                    if(!isInstalled(dependency))
                        throw new Incompatibility("Package " + name + " optionally requires " + dependency + " to be installed.");

        String installation = (new File(".")).getPath() + File.separator + "plugins" + File.separator;

        URL location = version.getLocation();
        boolean isJAR = (location.toString().endsWith(".jar")) ? true : false;

        if(isJAR) {
            boolean result = Remote._fetch(location.toString(), installation + Pack.getName() + ".jar", version.getChecksum());

            if(!result)
                return false;

            if(this.Database.hasIndex(name))
                this.Database.setArgument(name, "version", which);
            else {
                Arguments entry = new Arguments(name);
                entry.setValue("version", which);

                this.Database.addIndex(entry.getKey(), entry);
            }

            this.Database.update();
            return true;
        }

        if(!location.toString().endsWith(".zip"))
            throw new InvalidExtension("Package " + name + " contains an invalid extension!");

        String checksum = Remote.checksum(location.toString());
        
        if(!checksum.equalsIgnoreCase(version.getChecksum())) {
            Messaging.send("<red>Error <rose>Downloading File: Checksum match failed.");
            return false;
        }

        Messaging.send("<green>fetching <white>" + location);
        Messaging.send("<purple>checksum <white>" + checksum);

        ZIP.unpack(location, new File("."), force);

        if(this.Database.hasIndex(name))
            this.Database.setArgument(name, "version", which);
        else {
            Arguments entry = new Arguments(name);
            entry.setValue("version", which);

            this.Database.addIndex(entry.getKey(), entry);
        }

        this.Database.update();
        return true;
    }

    public List<Package> searchPackages(String i) {
        List<Package> results = new LinkedList<Package>();
        i = i.toLowerCase();

        if(hasPackage(i)) results.add(Packages.get(i));

        for(String name: Packages.keySet()) {
            if(name.toLowerCase().indexOf(i.toLowerCase()) != -1) results.add(Packages.get(name));
            String[] categories = Packages.get(name).getCategories();

            if(categories.length > 0)
                for(String cat: categories)
                    if(cat.equals(i) || cat.equalsIgnoreCase(i) || cat.toLowerCase().indexOf(i.toLowerCase()) != -1)
                        results.add(Packages.get(name));
        }

        return results;
    }

    public Package getPackage(String name) {
        if(!hasPackage(name.toLowerCase())) return null;
        return Packages.get(name.toLowerCase());
    }

    public class Package {
        private String name;
        private String author;
        private String[] authors;
        private String website;
        private String description;
        private String[] categories;
        private HashMap<String, Version> versions;

        public Package(String name, String author, String[] authors, String website, String description, String[] categories, HashMap<String, Version> versions) {
            this.name = name;
            this.author = author;
            this.authors = authors;
            this.website = website;
            this.description = description;
            this.categories = categories;
            this.versions = versions;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        public String[] getAuthors() {
            return authors;
        }

        public String getWebsite() {
            return website;
        }

        public String getDescription() {
            return description;
        }

        public String[] getCategories() {
            return categories;
        }

        public Version getLatestVersion() {
            if(versions.isEmpty())
                return null;

            List<String> vList = new ArrayList<String>();

            for(String version: versions.keySet())
                vList.add(version);

            if(vList.isEmpty())
                return null;

            Collections.sort(vList, new vSorter());
            return versions.get(vList.get(vList.size()-1));
        }

        public Version getLatestBranchVersion(String branch) {
            if(versions.isEmpty())
                return null;

            List<String> vList = new ArrayList<String>();

            for(String version: versions.keySet()) {
                Version v = this.versions.get(version);

                if(v.getBranch().equalsIgnoreCase(branch))
                    vList.add(version);
            }

            if(vList.isEmpty())
                return null;

            Collections.sort(vList, new vSorter());
            return versions.get(vList.get(vList.size()-1));
        }

        public boolean hasVersion(String version) {
            return versions.containsKey(version);
        }

        public Version getVersion(String version) {
            return versions.get(version);
        }

        public HashMap<String, Version> getVersions() {
            return versions;
        }
    }

    public class Engine {
        private String name;
        private int min, max;

        public Engine(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }

        public String getName() {
            return name;
        }
    }

    public class Version {
        private String version;
        private String branch;
        private String checksum;
        private URL location;
        private LinkedList<Engine> engines;
        private String[] dependencies;
        private String[] optional;

        public Version(String version, String branch, String checksum, String location, LinkedList<Engine> engines, String[] dependencies, String[] optional)
        throws
        MalformedURLException,
        IOException {
            this.version = version;
            this.branch = branch;
            this.checksum = checksum;
            this.location = new URL(location);
            this.engines = engines;
            this.dependencies = dependencies;
            this.optional = optional;
        }

        public String getBranch() {
            return branch;
        }

        public String getChecksum() {
            return checksum;
        }

        public URL getLocation() {
            try {
                return Remote.getRedirect(location);
            } catch (Exception ex) {
                return location;
            }
        }

        public LinkedList<Engine> getEngines() {
            return engines;
        }

        public Engine getEngine(String engine) {
            for(Engine e: engines)
                if(e.getName().equalsIgnoreCase(engine))
                    return e;

            return null;
        }

        public boolean hasEngine(String engine) {
            for(Engine e: engines)
                if(e.getName().equalsIgnoreCase(engine))
                    return true;

            return false;
        }

        public String getVersion() {
            return version;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public void setLocation(URL location) {
            this.location = location;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean hasDependencies() {
            return dependencies.length > 0;
        }

        public boolean needsDependency(String name) {
            for(String dependency: dependencies)
                if(dependency.equalsIgnoreCase(name)) return true;

            return false;
        }

        public String[] getDependencies() {
            return dependencies;
        }

        public String[] getOptionalDependencies() {
            return optional;
        }
    }
}
