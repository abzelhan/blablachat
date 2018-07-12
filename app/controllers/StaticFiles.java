package controllers;

import models.FileEntity;
import play.libs.Images;
import play.mvc.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.net.URLEncoder;

/**
 * Created by baha on 1/19/15.
 */
public class StaticFiles extends Controller {
    public static void image(String code, int width, int height, String filter) {
        try {
            Long start = System.currentTimeMillis();

            if (width == 0) {
                width = -1;
            }
            if (height == 0) {
                height = -1;
            }


            FileEntity fe = FileEntity.find("code=?", code).first();
            File f_Orig;
            if(width==250 && height==250){
                f_Orig = new File(FileEntity.filesLocation + fe.getSize_250());
                response.setContentTypeIfNotSet(fe.getMime());
                Application.renderBinary(f_Orig);
            }
            else{
                f_Orig = new File(FileEntity.filesLocation + fe.getFilename());
            }

            if (width == -1 && height == -1 && filter == null) {
                //Возможно эта хрень не будет работать на iOS & Android
                //redirect(FileEntity.siteUrl + "/public/uploads/" + URLEncoder.encode(f_Orig.getName(), "UTF-8"));
                response.setContentTypeIfNotSet(fe.getMime());
                Application.renderBinary(f_Orig);
            } else {

                String filenameWihoutExt = fe.getFilename().replace("." + fe.getExtention().replace(".", ""), "");

                //System.out.println("filenameWihoutExt: " + filenameWihoutExt + " ext: " + fe.getExtention());

                StringBuilder sb = new StringBuilder();
                sb.append(FileEntity.filesLocation).append(filenameWihoutExt);

                if (width != -1) {
                    sb.append("-_w_").append(width);
                }

                if (height != -1) {
                    sb.append("-_h_").append(height);
                }

                if (filter != null) {
                    sb.append("-_f_").append(filter);
                }

                sb.append(".").append(fe.getExtention());

                File f = new File(sb.toString());

                //System.out.println("f: "+f.getPath());

                if (filter != null) {
                    try {
                        System.out.println("Adding filter to image...");
                        BufferedImage in = ImageIO.read(f_Orig);
                        System.out.println("Original image: "+in.getWidth()+" and "+in.getHeight());

                        RescaleOp rescaleOp = new RescaleOp(0.3f, 15, null);
                        rescaleOp.filter(in, in);  // Source and destination are the same.
                        ImageIO.write(in, "jpg", f);
                        System.out.println("Added filter to image...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (f.exists()) {
                    //p(f);
                    //ничего не делаем
                } else {
                    //значит такого файла нет, и надо его заколпашить

                    //размеры картинки достаем из самого entity, так как он сам умеет считать свою ширину и высоту.
                    int w = fe.getWidth();
                    int h = fe.getHeight();


                    //Режем картинку только тогда, когда у нас заданы и ширина и высота и нету указанного пользователем обрезанного фото под
                    //эту пропорцию

                    // Если у нас хотя бы одна размерность не задана или есть файл с _ratio_, то просто ресайзим картинку

                    if ((width != -1 && height != -1) && !f_Orig.getName().contains("_ratio_")) {

                        try {
                            double wD = w;
                            double hD = h;
                            double widthD = width;
                            double heightD = height;


                            if (((w < h) && ((wD / widthD) * heightD < h))) {
                                try {
                                    //Если сущ. ширина меньше чем высота и уже увеличенная высота не будет привышать сущ высоту то
                                    //за макс возмем ширину и выровнем по центру вертикали

                                    double d = (double) (wD / widthD);
                                    //p("d: " + d + " w: " + w + " h: " + h + " (w / width) * height=" + (hD / heightD) * heightD);
                                    int newW = w;
                                    int newH = (int) (d * heightD);

                                    int offset = 0;
                                    if (newH < h) {
                                        offset = (h - newH) / 2;
                                    }

                                    int x1 = 0;
                                    int x2 = w;

                                    //int y1 = offset;
                                    //int y2 = h - offset;
                                    int y1 = 0;
                                    int y2 = newH;

                                    //p("1newW: " + newW + "; newH: " + newH + "; offset: " + offset + "; x1,x2,y1,y2:" + x1 + "," + x2 + "," + y1 + "," + y2);
                                    File fCrop = new File(FileEntity.filesLocation + fe.getFilename().replace("." + fe.getExtention(), "") + "_crop" + "_" + newW + "_" + newH + "." + fe.getExtention());
                                    //p("f_orig: " + f_Orig + " fCrop: " + fCrop);
                                    Images.crop(f_Orig, fCrop, x1, y1, x2, y2);
                                    //p("after crop: " + fCrop);
                                    Images.resize(fCrop, f, width, height);
                                    //p("after resize: " + f);
                                } catch (Exception ex) {
                                    //p("==========OOOOPS: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            } else if ((h < w) && ((hD / heightD) * width < w)) {
                                try {
                                    //Если сущ. высота меньше чем ширина и уже увеличенная ширина не будет привышать сущ ширину то
                                    //за макс возмем высоту и выровнем по центру горизонтали
                                    double d = (double) (hD / heightD);

                                    //p("d: " + d);
                                    int newW = (int) (d * widthD);
                                    int newH = h;

                                    int offset = 0;
                                    if (newW < w) {
                                        offset = (w - newW) / 2;
                                    }

                                    int x1 = offset;
                                    int x2 = w - offset;

                                    int y1 = 0;
                                    int y2 = h;

                                    //p("2newW: " + newW + "; newH: " + newH + "; offset: " + offset + "; x1,x2,y1,y2:" + x1 + "," + x2 + "," + y1 + "," + y2);


                                    File fCrop = new File(FileEntity.filesLocation + fe.getFilename().replace("." + fe.getExtention(), "") + "_crop" + "_" + newW + "_" + newH + "." + fe.getExtention());
                                    Images.crop(f_Orig, fCrop, x1, y1, x2, y2);
                                    //p("after crop: " + fCrop);
                                    Images.resize(fCrop, f, width, height);
                                    //p("after resize: " + f);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                //Если они прапорциональны и если они не поддаются нормальному cropу, то маштабируем как есть как есть.
                                System.out.println("Pic is proportional to rezire");
                                Images.resize(f_Orig, f, width, height);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Если идет односительное изменение масшатаба
                        //System.out.println("f_orig: " + f_Orig + " to: " + f);
                        Images.resize(f_Orig, f, width, height);
                    }
                }


                redirect(FileEntity.siteUrl + "/public/uploads/" + URLEncoder.encode(f.getName(), "UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Application.error(404, "Изображение не найдено");
        }
    }
}
