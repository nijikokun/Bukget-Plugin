package omg.bukget.io.encryption;

import java.io.FileNotFoundException;
import java.net.URL;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import omg.bukget.BukGet;

public class ZIP {

    private ZIP() { }

    public static void main(String[] args) throws Exception {
       Archive Zip = new Archive("test.zip");
       Zip.add(new File("build/"));
       Zip.create();
    }

    public static void unzip(String zip) throws IOException {
        ZipFile zipFile = null;
        InputStream inputStream = null;
        File inputFile = new File(zip);

        try {
            zipFile = new ZipFile(inputFile);
            Enumeration<? extends ZipEntry> oEnum = zipFile.entries();

            while (oEnum.hasMoreElements()) {
                ZipEntry zipEntry = oEnum.nextElement();
                File file = new File(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    inputStream = zipFile.getInputStream(zipEntry);
                    write(inputStream, file);
                }
            }
        } finally {
            if (zipFile != null) zipFile.close();
            if (inputStream != null) inputStream.close();
        }
    }

    public static void write(InputStream inputStream, File fileToWrite) throws IOException {
        BufferedInputStream buffInputStream = new BufferedInputStream(inputStream);
        FileOutputStream fos = new FileOutputStream(fileToWrite);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int byteData;

        while ((byteData = buffInputStream.read()) != -1) bos.write((byte) byteData);

        bos.close();
        fos.close();
        buffInputStream.close();
    }

    public static void unpack(URL url, File targetDir, boolean force) throws IOException {
        if (!targetDir.exists()) targetDir.mkdirs();
        if (!url.getPath().endsWith(".zip")) throw new IOException("Invalid package");

        InputStream in = new BufferedInputStream(url.openStream(), 1024);

        File tempDir = new File(BukGet.directory.getPath() + File.separator + "-" + File.separator);
             tempDir.mkdirs();

        File zip = File.createTempFile("pkg-", ".zip", new File(tempDir.getPath() + File.separator));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
        copyStream(in, out);
        out.close();
        unpack(zip, targetDir, force);

        for(File file: tempDir.listFiles())
            file.delete();
    }

    public static void unpack(File theFile, File targetDir, boolean force) throws IOException {
        if (!theFile.exists()) 
            throw new IOException(theFile.getAbsolutePath() + " does not exist");

        if (!buildDirectory(targetDir))
            throw new IOException("Could not create directory: " + targetDir);

        ZipFile zipFile = new ZipFile(theFile);

        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            File file = new File(targetDir, File.separator + entry.getName());

            if (!buildDirectory(file.getParentFile())) 
                throw new IOException("Could not create directory: " + file.getParentFile());

            if (!entry.isDirectory()) {
                if(!file.toString().endsWith(".jar") && file.exists() && !force)
                    continue;

                copyStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
            } else {
                if (!buildDirectory(file))
                    throw new IOException("Could not create directory: " + file);
            }
        }

        zipFile.close();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);

        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }

        in.close();
        out.close();
    }

    public static boolean buildDirectory(File file) {
        return file.exists() || file.mkdirs();
    }


    public static class Archive {
        private ZipOutputStream Archive = null;
        private FileOutputStream Writer = null;
        private String name;
        private LinkedList<File> files = new LinkedList<File>();

        public Archive(String destination) {
            this.name = destination;
        }

        public void setFiles(LinkedList<File> files) {
            this.files = files;
        }

        public boolean add(File file) {
            if(file == null)
                return false;

            if(!file.exists())
                return false;

            this.files.add(file);
            return true;
        }

        public File create() throws IOException {
            if(this.files.isEmpty())
                return null;

            Writer = new FileOutputStream(this.name);
            Archive = new ZipOutputStream(Writer);

            for(File f: this.files)
                if(f.isDirectory()) {
                    LinkedList<File> recursed = recurse(f);
                    
                    for(File r: recursed) {
                        if(this.files.contains(r))
                            continue;

                        String filename = r.getName();

                        if(filename.isEmpty())
                            continue;

                        String path = r.getPath().replace(filename, "");
                        System.out.println("Encrypting: " + path + " - " + filename);
                        encrypt(path, filename);
                    }
                } else {
                    String filename = f.getName();

                    if(filename.isEmpty())
                        continue;

                    String path = f.getPath().replace(filename, "");
                    System.out.println("Encrypting: " + path + " - " + filename);
                    encrypt(path, filename);
                }

            Archive.flush();
            Archive.close();

            return new File(name);
        }

        private void encrypt(String path, String filename) throws FileNotFoundException, IOException {
          byte[] buf = new byte[1024];
          int len;
          FileInputStream in = new FileInputStream(path + filename);
          this.Archive.putNextEntry(new ZipEntry(path + filename));

          while ((len = in.read(buf)) > 0)
            this.Archive.write(buf, 0, len);
        }

        private LinkedList<File> recurse(File folder) {
            LinkedList<File> findings = new LinkedList<File>();

            for (File f: folder.listFiles())
                if (f.isDirectory())
                    findings.addAll(recurse(f));
                else {
                    findings.add(f);
                }

            return findings;
	}
    }
    /*
    public void ZIP(String name) {
        
    }

    static public void Zip(String file, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip(file, file, zip);
        zip.flush();
        zip.close();
    }

    static public void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }

    static private void addFolderFileToZip(String path, String srcFile, ZipOutputStream zip)
    throws
    Exception {
        File folder = new File(srcFile);

        if (folder.isDirectory())
          addFolderToZip(path, srcFile, zip);
        else {
          byte[] buf = new byte[1024];
          int len;
          FileInputStream in = new FileInputStream(srcFile);
          zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));

          while ((len = in.read(buf)) > 0)
            zip.write(buf, 0, len);
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
    throws
    Exception {
        File folder = new File(srcFolder);

        for (String fileName : folder.list())
          if (path.equals(""))
            addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
          else {
            addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
          }
    }*/

}