package utils;

import backup.Backup;
import bot.Backups;
import config.Config;
import console.Logs;
import net.dv8tion.jda.api.entities.Message;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public class Utils {

    public static void runAsync(Runnable target) {
        new Thread(target).start();
    }

    public static long saveAttachment(Message.Attachment attachment) {
        if (Config.get().getBackupSettings().getMaxAttachmentSize() != -1 && attachment.getSize() >= (1024 * 1024 * Config.get().getBackupSettings().getMaxAttachmentSize())) {
            Logs.log("Skipping saving attachment id " + attachment.getIdLong() + " because file exceeds " + Config.get().getBackupSettings().getMaxAttachmentSize() + "M!", Logs.WARNING);
            return -1;
        }
        File dir = new File("db/attachments/" + attachment.getId());
        dir.mkdirs();
        File file = new File(dir, attachment.getFileName());
        attachment.downloadToFile(file)
                .thenAccept( f -> {
                    File zip;
                        if (Config.get().getBackupSettings().isZipFiles()) {
                            zip = Utils.zipFile(file);
                            System.out.println("Created and ziped new attachment directory for attachment id " + attachment.getId() + " [Size: " + getMegabytes(attachment.getSize()) + "M] " + "[Zip Size: " + getMegabytes(zip.length()) + "M]");
                        } else System.out.println("Created new attachment directory for attachment id " + attachment.getId() + " [Size: " + getMegabytes(attachment.getSize()) + "M]");
                })
                .exceptionally(t ->
                { // handle failure
                    t.printStackTrace();
                    return null;
                });
        return attachment.getIdLong();
    }

    public Optional<File> getAttachmentFile (String id) {
        File dir = new File("db/attachments/" + id);
        try {
            return Optional.ofNullable(Objects.requireNonNull(dir.listFiles())[0]);
        } catch (NullPointerException ignored) {
            return Optional.empty();
        }
    }

    public static boolean saveImage(String urlString, @NotNull String pathString, @NotNull String name){
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            InputStream in = conn.getInputStream();
            File pathDir = new File("db/attachments/" + pathString);
            pathDir.mkdirs();
            Path path = Paths.get("db/attachments/" + pathString + "/" + name + ".png");
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            in.close();

            return !Config.get().getBackupSettings().isZipFiles() || zipFile(path.toFile()) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //TODO ADD SUPPORT TO DELETE THE ZIP ONCE ITS DONE BEING USED
    public static Optional<File> getImageFile(@NotNull String pathString, String name) {
        File attachmentDir = new File("db/attachments/");

        try {
            File zip = new File(attachmentDir, pathString + ".zip");
            if (zip.exists()) new ZipFile(zip).extractAll(attachmentDir.getPath() + "/" + pathString);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        File file = new File(attachmentDir.getPath() + "/" + pathString + "/" + name + ".png");
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    /*
    public static Optional<File> getAttachmentFile(@NotNull String name) {
        File attachmentDir = new File("db/attachments/");

        try {
            File zip = new File(attachmentDir, pathString + ".zip");
            new ZipFile(zip).extractAll("db/attachments/");
        } catch (ZipException e) {
            e.printStackTrace();
        }

        File file = new File(attachmentDir, pathString + "/" + name);
        return file.exists() ? Optional.of(file) : Optional.empty();
    }
     */

    //TODO RE-TEST IF THIS STILL WORKS!
    private static File zipFile(File file) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.ULTRA);
            File zip = new File(file.getParent() + ".zip");
            new ZipFile(zip).addFile(file, parameters);

            File parentFile = file.getParentFile();
            boolean success = file.delete();
            File[] parentFiles = parentFile.listFiles();
            if (success && parentFiles != null && parentFiles.length == 0)
                file.getParentFile().delete();

            return zip;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    private static boolean zipFile(File file) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
            new ZipFile(file.getParent() + ".zip").addFile(file, parameters);

            File parentFile = file.getParentFile();
            boolean success = file.delete();
            File[] parentFiles = parentFile.listFiles();
            if (success && parentFiles != null && parentFiles.length == 0)
                file.getParentFile().delete();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
     */

    public static File getConfigFile() {
        File config = new File("config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                InputStream source = Utils.class.getResourceAsStream("/config.yml");
                Files.copy(source, Paths.get(config.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return config;
    }

    public static float getMegabytes(long bytes) {
        return (Math.round((bytes / 1024f / 1024f) * 100f) / 100f);
    }

}
