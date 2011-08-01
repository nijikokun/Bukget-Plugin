package omg.bukget.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import omg.bukget.BukGet;

public class Common {
    public Common() { }

    private static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        return s1.compareTo(s2);
    }

    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();

        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }

        return sb.toString();
    }

    public static String getCraftBukkitVersion(String version) {
        Pattern v = Pattern.compile("b(\\d{1,4})");
        String vr = "";

        Matcher m = v.matcher(version);
        while (m.find()) {
            vr = m.group();
        }

        if (vr.startsWith("b"))
            vr = vr.substring(1);

        return vr;
    }

    public static String getCleanVersion(String version) {
        String[] dict = new String[]{ "rc", "final" };
        ArrayList<String> Dictionary = new ArrayList();
        Collections.addAll(Dictionary, dict);

        version = version.replace("-SNAPSHOT", "").replace("-snapshot", "").replace("-git", "");

        Pattern v = Pattern.compile("([a-zA-Z]+)-");
        String vr = "";

        Matcher m = v.matcher(version);
        while (m.find()) {
            vr = m.group();

            if(Dictionary.contains(vr.replace("-",""))) {
                version = version.replace(vr, vr.replace("-", "").toLowerCase()); continue;
            } else if(vr.equalsIgnoreCase("ga-")) {
                version = version.replace(vr, "final"); continue;
            } else if(vr.equalsIgnoreCase("cr-") || vr.equalsIgnoreCase("releasecandidate-")) {
                version = version.replace(vr, "rc"); continue;
            }

            version = version.replace(vr, String.valueOf(vr.charAt(0)).toLowerCase());
        }

        version = version.replace("-", ".").replace("_", ".");

        v = Pattern.compile("([a-zA-Z]+)");
        vr = "";

        m = v.matcher(version);
        while (m.find()) {
            vr = m.group();

            if(Dictionary.contains(vr)) {
                version = version.replace(vr, vr.toLowerCase()); continue;
            } else if(vr.equalsIgnoreCase("ga")) {
                version = version.replace(vr, "final"); continue;
            } else if(vr.equalsIgnoreCase("cr") || vr.equalsIgnoreCase("releasecandidate")) {
                version = version.replace(vr, "rc"); continue;
            }

            version = version.replace(vr, String.valueOf(vr.charAt(0)).toLowerCase());
        }

        return unQuote(version).trim();
    }

    public static long getNumericVersion(String version) {
	Pattern vr = Pattern.compile("(\\d{1,3})");
        Pattern vm = Pattern.compile("([a-z])");
        long value = 0L;

	Matcher m = vr.matcher(version);
	while (m.find()) {
            value += Long.valueOf(m.group());
	}

	m = vm.matcher(version);
	while (m.find()) {
            char a = m.group().charAt(0);
            value += (int)a;
	}

        return value;
    }

    public static class vSorter implements Comparator<String> {
        public int compare(String o1, String o2) {
            return Common.compare(o1, o2);
        }
    }

    public static String unQuote(String s) {
        s = (s != null && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) ? s.substring(1, s.length() - 1) : s;
        s = (s != null && ((s.startsWith("\"") || s.startsWith("'")))) ? s.substring(1, s.length()) : s;
        s = (s != null && ((s.endsWith("\"") || s.endsWith("'")))) ? s.substring(0, s.length()-1) : s;
        return s;
    }

    public static String readableSize(long size) {
        String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB" };
        int mod = 1024, i;

        for (i = 0; size > mod; i++) {
            size /= mod;
        }

        return Math.round(size) + " " + units[i];
    }

    public static String readableProfile(long time) {
        int i = 0;
        String[] units = new String[] { "ms", "s", "m", "hr", "day", "week", "mnth", "yr" };
        int[] metric = new int[] { 1000, 60, 60, 24, 7, 30, 12 };
        long current = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);

        for(i = 0; current > metric[i]; i++) {
            current /= metric[i];
        }

        return current + " " + units[i] + ((current > 1 && i > 1) ? "s" : "");
    }

    public static String[] split(String str, int maxLen) {
        int origLen = str.length();
        int splitNum = origLen/maxLen;
        if (origLen % maxLen > 0)
            splitNum += 1;

        String[] splits = new String[splitNum];

        for (int i = 0; i < splitNum; i++) {
            int startPos = i * maxLen;
            int endPos = startPos + maxLen;
            if (endPos > origLen)
                endPos = origLen;

            String substr = str.substring(startPos, endPos);
            splits[i] = substr;
        }
        
       return splits;
    }

    public static void extract(String... names) {
        for(String name: names) {
            File actual = new File(BukGet.directory, name);

            if(actual.exists())
                continue;

            InputStream input = BukGet.class.getResourceAsStream("/resources/" + name);

            if(input == null)
                continue;

            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;

                while ((length = input.read(buf)) > 0)
                    output.write(buf, 0, length);

                System.out.println("[iConomy] Default setup file written: " + name);
            } catch (Exception e) {
            } finally {
                try { if (input != null) input.close();
                } catch (Exception e) { }

                try { if (output != null) output.close();
                } catch (Exception e) { }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Common.normalisedVersion("1.0.rc.1.SNAPSHOT"));
    }
}
