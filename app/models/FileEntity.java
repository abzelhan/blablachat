/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import kz.api.json.File.Image;
import kz.api.json.File.SoundType;
import utils.providers.AvatarGenerator;
import kz.wg.utils.Base64;
import kz.wg.utils.Config;
import kz.wg.utils.Formatter;
import net.coobird.thumbnailator.Thumbnails;
import play.Play;
import play.libs.Files;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * @author Bakhyt
 */
@Entity
@Table(name = "files")
public class FileEntity extends DomainObject {

    public static String filesLocation = Play.applicationPath + "/public/uploads/";

    public static String siteUrl = "http://blablachat.me:9001";
//    public static String siteUrl = "http://localhost:9001";

    String title;
    String filename;
    String mime;

    long filesize;
    @Column(name = "processed", columnDefinition = "tinyint default '0'")
    int processed;
    @Column(name = "downloaded", columnDefinition = "bigint default '0'")
    long downloaded;
    @Column(name = "width", columnDefinition = "integer default '0'")
    int width;
    @Column(name = "height", columnDefinition = "integer default '0'")
    int height;

    String identifier;
    String watermark;
    String size_720;
    String size_250;
    String size_125;

    String uploaderBackend;


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public static String cols() {
        return "[\"ID\",\"Клиент\",\"Токен\",\"Пользователь\",\"Действия\"]";
    }


    //if not exist, then create default random avatar and save
    public static FileEntity createOptionalAvatar(String code) {
        FileEntity avatarFile = null;
        if (code!=null && !code.isEmpty()) {
            avatarFile = byCodeRequired(code);
        }
        if (avatarFile == null) {
            BufferedImage avatar = generateAvatar();
            avatarFile = FileEntity.processFile(avatar, "pic");
            avatarFile.save();
        }

        return avatarFile;
    }

    public static FileEntity byCode(String code) {
        if (code != null && !code.isEmpty()) {
            FileEntity img = FileEntity.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if (img == null) {
                throw new NoSuchElementException(code);
            }
            return img;
        } else {
            return null;
        }
    }

    public static FileEntity byCodeRequired(String code) {
        if (code != null && !code.isEmpty()) {
            FileEntity img = FileEntity.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if (img == null) {
                throw new NoSuchElementException(code);
            }
            return img;
        } else {
            throw new NoSuchElementException(code);
        }
    }


    private static BufferedImage generateAvatar() {
        int imageCount = 1;
        int baseSize = 500;
        double fillFactor = 0.9;
        float alphaFactor = 0.5f;
        int pixelSize = baseSize / 5;
        int borderSize = pixelSize / 2;
        int imageSize = baseSize + (borderSize * 2);
        AvatarGenerator avatarCreator = new AvatarGenerator(imageCount, baseSize,
                fillFactor, alphaFactor, pixelSize, borderSize, imageSize);
        return avatarCreator.createOne();
    }

    public Image getJson() {
        if (mime.startsWith("image")) {
            Image image = new Image();
            image.setCode(code);
            image.setBig(imgUrl(-1, -1));
            image.setSmall(imgUrl(250, 250));
            image.setFilename(null);
//            image.setId(identifier);

            return image;
        } else {
            String link = siteUrl + "/sounds/" + code;
            Image image = new Image();
            image.setCode(code);
            image.setId(identifier);
            image.setOriginal(link);

            List<SoundType> types = new ArrayList<>();

            types.add(new SoundType("chipmunk", "Бурундук", link + "/chipmunk"));
            types.add(new SoundType("agent", "Спец. Агент", link + "/agent"));
            types.add(new SoundType("alien", "Пришелец", link + "/alien"));
            types.add(new SoundType("female", "Женственный", link + "/female"));
            types.add(new SoundType("male", "Мужественный", link + "/male"));
            types.add(new SoundType("mosquito", "Комар", link + "/mosquito"));

            image.setSoundTypes(types);
            return image;
        }
    }

    public String getFileSize() {
        String ret = "";
        try {
            if (filesize > 0) {
                if (filesize > 1024 && filesize < 1024 * 1024) {
                    ret = (filesize / 1024) + " kb";
                } else {
                    ret = (filesize / (1024 * 1024)) + " mb";
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public String getUploadedDate() {
        String ret = "";
        try {
            ret = Formatter.formatForSite(getCreationDate());
        } catch (Exception e) {
        }
        return ret;
    }

    public void getWidthAndHeight() {
        try {
            File f_Orig = new File(Config.filesLocation + this.getFilename());
            ImageInputStream in = ImageIO.createImageInputStream(f_Orig);
            try {
                final Iterator readers = ImageIO.getImageReaders(in);
                if (readers.hasNext()) {
                    ImageReader reader = (ImageReader) readers.next();
                    try {
                        reader.setInput(in);
                        width = reader.getWidth(0);
                        height = reader.getHeight(0);
                    } finally {
                        reader.dispose();
                    }
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getHeight() {
        if (height == 0) {
            getWidthAndHeight();
            _save();
        }
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        if (width == 0) {
            getWidthAndHeight();
            _save();
        }
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getUrl() {
        return "/public/uploads/" + filename;
    }

    public String img(int width, int height) {
        return img(width, height, "");
    }

    public String img(int width, int height, String ratio) {
        String ret = "";
        try {
            if (code == null || code.trim().isEmpty()) {
                code = "" + System.currentTimeMillis();
                this.save();
            }
            ret = "<img title='" + filename + "' src = '/img?code=" + code + "&width=" + width + "&height=" + height + "&ratio=" + ratio + "' />";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String imgUrl(int width, int height) {
        return imgUrl(width, height, "");
    }

    public String imgUrl(int width, int height, String filter) {
        String ret = "";
        try {
            if (code == null || code.trim().isEmpty()) {
                code = "" + System.currentTimeMillis();
                this.save();
            }
            ret = siteUrl + "/img?code=" + code;

            if (width > -1) {
                ret += "&width=" + width;
            }

            if (height > -1) {
                ret += "&height=" + height;
            }

            if (filter != null && !filter.isEmpty()) {
                ret += "&filter=" + filter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getDirectUrl() {
        return "/public/uploads/" + filename;
    }

    public String getExtention() {
        String ext = "";
        try {
            int index = filename.lastIndexOf(".");
            ext = filename.substring(index);
        } catch (Exception e) {
        }
        return ext;
    }

    @Override
    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
        String m = "" + creationDate.getTimeInMillis();
        code = Base64.encodeBytes(m.getBytes()).replaceAll("==", "");
    }

    public String getViewUrl() {
        return imgUrl(-1, -1);
    }

    public String getDownloadUrl() {
        return "/d?c=" + code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public static FileEntity processFile(File fb, String prefix) {
        try {

            String ext = "";
            try {
                int start = fb.getName().lastIndexOf(".");
                if (start > 0 && start <= fb.getName().length() - 2) {
                    ext = fb.getName().substring(start);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int height, width;


            BufferedImage bufferedImage = ImageIO.read(fb);

            height = bufferedImage.getHeight();
            width = bufferedImage.getWidth();

            BufferedImage newBufferedImage = new BufferedImage(width,
                    height, BufferedImage.TYPE_INT_RGB);

            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

            ImageIO.write(newBufferedImage, "jpg", fb);


            String filename = prefix + "_" + System.currentTimeMillis();
            String filenameWithExt = prefix + "_" + System.currentTimeMillis() + ext;
            String mime = new MimetypesFileTypeMap().getContentType(fb);
            long size = fb.length();
            FileEntity fe = new FileEntity();
            fe.setFilename(filenameWithExt);
            fe.setFilesize(size);
            fe.setMime(mime);
            fe.setCreationDate(Calendar.getInstance());
            fe.setTitle(fb.getName());
            File newFile = new File(filesLocation + filenameWithExt);


            Files.copy(fb, newFile);
            File compressedFile = new File(filesLocation + filename + "_250" + ext);
            try {
                Thumbnails.Builder<File> builder = Thumbnails.of(newFile);


                builder = builder.width(250);

                builder = builder.height(250);

                builder.toFile(compressedFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Files.delete(fb);
            fe.setSize_250(filename + "_250" + ext);
            fe.save();
            return fe;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static FileEntity processFile(BufferedImage bi, String prefix) {
        try {
            String ext = ".PNG";
            String filename = prefix + "_" + System.currentTimeMillis() + ext;
            File fb = new File(filesLocation + filename);
            ImageIO.write(bi, "png", fb);
            return processFile(fb, prefix);


//            String mime = new MimetypesFileTypeMap().getContentType(fb);
//            long size = fb.length();
//            FileEntity fe = new FileEntity();
//            fe.setFilename(filename);
//            fe.setFilesize(size);
//            fe.setMime(mime);
//            fe.setCreationDate(Calendar.getInstance());
//            fe.setTitle(fb.getName());
//            fe.save();
//            return fe;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getFile() {
        return new File(filesLocation + filename);
    }

    public static String getFilesLocation() {
        return filesLocation;
    }

    public static void setFilesLocation(String filesLocation) {
        FileEntity.filesLocation = filesLocation;
    }

    public static String getSiteUrl() {
        return siteUrl;
    }

    public static void setSiteUrl(String siteUrl) {
        FileEntity.siteUrl = siteUrl;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

    public String getSize_720() {
        return size_720;
    }

    public void setSize_720(String size_720) {
        this.size_720 = size_720;
    }

    public String getSize_250() {
        return size_250;
    }

    public void setSize_250(String size_250) {
        this.size_250 = size_250;
    }

    public String getSize_125() {
        return size_125;
    }

    public void setSize_125(String size_125) {
        this.size_125 = size_125;
    }

    public String getUploaderBackend() {
        return uploaderBackend;
    }

    public void setUploaderBackend(String uploaderBackend) {
        this.uploaderBackend = uploaderBackend;
    }
}
