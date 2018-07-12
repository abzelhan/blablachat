package controllers;

import kz.wg.utils.Achtung;
import kz.wg.utils.Config;
import models.FileEntity;
import play.libs.Files;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Calendar;

public class FileManager extends Parent {

    public static void uploadImage(File image) {
        String url = "";
        try {
            if (image != null) {
                if (getCurrentUser() != null) {
                    try {
                        String filename = "blogImage_" + System.currentTimeMillis() + "_" + image.getName().replaceAll(" ","_");
                        String mime = new MimetypesFileTypeMap().getContentType(image);
                        long size = image.length();
                        FileEntity fe = new FileEntity();
                        //fe.setAlbum(fotoAlbum);
                        //fe.setFilename(title);
                        fe.setFilename(filename);
                        fe.setFilesize(size);
                        fe.setMime(mime);
                        fe.setCreationDate(Calendar.getInstance());
                        fe.setCreator(getCurrentUser());
                        File dest = new File(Config.filesLocation + filename);
                        Files.copy(image, dest);
                        Files.delete(image);
                        fe.save();
                        //makeWatermark(fe);
                        url = "https://feedbook.kz/img?code="+fe.getCode();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new Exception();
                }
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            addAchtung("Кешіріңіз, суреттерді тек қана сайтқа кірген кезде еңгізе аласыз", Achtung.FAIL, 10);
            FileManager.renderText("top.window.location='/'");
        }
        url = url.replace("&amp;","&").replace("&ratio=","");
        FileManager.renderText("$('#src').val('" + url + "');ImageDialog.showPreviewImage('" + url + "')");
    }

    public static void d(String c) {
        FileEntity fe = FileEntity.find("from FileEntity where code=?", c).first();
        if (fe == null) {
            notFound();
        }
        fe.setDownloaded(fe.getDownloaded() + 1);
        fe.save();
        String inFile = Config.filesLocation + fe.getFilename();
        File file = new File(inFile);
        FileManager.renderBinary(file, fe.getFilename());
    }
}