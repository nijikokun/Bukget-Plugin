package omg.bukget.io;

import omg.bukget.io.encryption.MD5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import java.net.URLConnection;
import java.net.URL;

import omg.bukget.utils.Common;
import omg.bukget.utils.Messaging;

public class Remote {

    protected static int count, total, itemCount, itemTotal;
    protected static long lastModified;
    protected static String error;
    protected static boolean cancelled;

    public Remote() { }

    public synchronized void cancel() {
        cancelled = true;
    }

    public static boolean fetch(String location, String filename) {
        try {
            cancelled = false;
            count = total = itemCount = itemTotal = 0;
            System.out.println("[Bukget] Downloading Files");

            if (cancelled) return false;

            System.out.println("   + " + filename + " downloading...");

            download(location, filename);

            System.out.println("   - " + filename + " finished.");
            System.out.println("[Bukget] Downloaded " + filename + "!");

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        final long startTime = System.nanoTime();
        final long endTime;

        try {
          System.out.println(getFileSize("http://mirror.nexua.org/iConomy/Latest%20Build/iConomy.jar"));
        } finally {
          endTime = System.nanoTime();
        }

        final long duration = endTime - startTime;

        System.out.println(Common.readableProfile(duration));
    }

    public static boolean _fetch(String location, String filename, String checksum) {
        try {
            cancelled = false;
            count = total = itemCount = itemTotal = 0;

            if (cancelled) 
                return false;

            String fetched = Remote.checksum(location);

            if(!fetched.equalsIgnoreCase(checksum))
                throw new IOException("Checksum match failed.");

            Messaging.send("<green>fetching <white>" + location + " [" + getFileSize(location) + "]");
            Messaging.send("<purple>checksum <white>" + checksum(location));
            download(location, filename);
        } catch (IOException ex) {
            Messaging.send("<red>Error <rose>Downloading File: " + ex.getMessage());
            return false;
        }

        return true;
    }

    protected static synchronized void download(String location, String filename) throws IOException {
        URLConnection connection = new URL(location).openConnection();
        connection.setUseCaches(false);
        lastModified = connection.getLastModified();
        int filesize = connection.getContentLength();

        String destination = filename;
        File parentDirectory = new File(destination).getParentFile();
        if (parentDirectory != null) parentDirectory.mkdirs();

        InputStream in = connection.getInputStream();
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[65536];
        int currentCount = 0;

        for (;;) {
            if (cancelled) {
                break;
            }

            int count = in.read(buffer);

            if (count < 0) break;

            out.write(buffer, 0, count);
            currentCount += count;
        }

        in.close();
        out.close();
    }

    private static String getFileSize(String location) throws IOException {
        URLConnection connection = new URL(location).openConnection();
        return Common.readableSize(connection.getContentLength());
    }

    public long getLastModified() {
        return lastModified;
    }

    public static String checksum(String location) {
        try {
            URLConnection connection = (getRedirect(new URL(location))).openConnection();
            connection.setUseCaches(false);
            InputStream in = connection.getInputStream();
            return MD5.getHashString(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static URL getRedirect(URL url) throws MalformedURLException, IOException {
        String location = url.openConnection().getHeaderField("Location");

        while(location != null) {
            url = new URL(location);
            location = url.openConnection().getHeaderField("Location");
        }

        return url;
    }
}
