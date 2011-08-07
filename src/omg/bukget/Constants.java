package omg.bukget;

import org.bukkit.util.config.Configuration;

public class Constants {
    public static String REPO_URL = "http://pkg.nexua.org/api/json";
    public static String ALT_REPO_URL = "http://bukget.org/repo.json";
    
    // Booleans
    public static boolean SELF_UPDATE = false;
    public static boolean AUTO_RELOAD = false;
    public static boolean FOLLOW_BRANCHES = true;
    public static boolean REQUIRE_DEPENDENCIES = true;
    public static boolean REQUIRE_OPTIONAL_DEPENDENCIES = false;

    public static void load(Configuration config) {
        config.load();

        SELF_UPDATE = config.getBoolean("auto-self-update", AUTO_RELOAD);
        AUTO_RELOAD = config.getBoolean("auto-reload", AUTO_RELOAD);
        FOLLOW_BRANCHES = config.getBoolean("follow-branches", AUTO_RELOAD);
        REQUIRE_DEPENDENCIES = config.getBoolean("dependencies.required", REQUIRE_DEPENDENCIES);
        REQUIRE_OPTIONAL_DEPENDENCIES = config.getBoolean("dependencies.optional", REQUIRE_OPTIONAL_DEPENDENCIES);
    }
}
