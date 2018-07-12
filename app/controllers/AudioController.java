package controllers;

import com.google.gson.JsonObject;
import kz.api.json.Command;
import kz.api.json.File.Image;
import kz.api.json.Result;
import models.FileEntity;
import models.User;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;

/**
 * Created by baha on 6/10/15.
 */
public class AudioController extends Parent {

    public static void sound(String code, String f) {
        FileEntity fe = FileEntity.find("code=?", code).first();
        if (fe == null) {
            error(404, "not found");
        } else {
            if (fe.getMime().startsWith("audio")) {

                try {
                    if (f == null || f.isEmpty()) {
                        redirect(FileEntity.siteUrl + "/public/uploads/" + URLEncoder.encode(fe.getFilename(), "UTF-8"));
                    } else {

                        StringBuilder command = new StringBuilder();
                        command.append("sox ").append(FileEntity.filesLocation).append(fe.getFilename());


                        String newFilename = "sounds/filtered/" + fe.getCode() + "_" + f+fe.getExtention();

                        String physName = FileEntity.filesLocation + newFilename;

                        if (new File(newFilename).exists()) {
                            redirect(FileEntity.siteUrl + "/public/uploads/" + newFilename);
                        } else {
                            String ff = "";

                            if (f.equalsIgnoreCase("chipmunk")) {
                                ff = "pitch 1000";
                            } else if (f.equalsIgnoreCase("agent")) {
                                ff = "pitch -800";
                            } else if (f.equalsIgnoreCase("male")) {
                                ff = "pitch -500";
                            } else if (f.equalsIgnoreCase("female")) {
                                ff = "pitch 500";
                            } else if (f.equalsIgnoreCase("mosquito")) {
                                ff = "pitch 1100";
                            } else if (f.equalsIgnoreCase("alien")) {
                                ff = "stretch 1.2 133.33 lin 0.2 0.4 overdrive 20 10 echo 0.4 0.8 15 0.8 synth sine fmod 30 echo 0.8 0.8 29 0.8";
                            }

                            command.append(" ").append(physName).append(" ").append(ff);

                            try {
                                String line;
                                System.out.println("command: " + command.toString());
                                Process p = Runtime.getRuntime().exec
                                        (command.toString());
                                BufferedReader input =
                                        new BufferedReader
                                                (new InputStreamReader(p.getInputStream()));
                                while ((line = input.readLine()) != null) {
                                    System.out.println(line);
                                }
                                input.close();

                                int r = p.waitFor();

                                System.out.println("command result: " + r);
                                if (r != 1) {
                                    redirect(FileEntity.siteUrl + "/public/uploads/" + newFilename);
                                }
                            } catch (Exception err) {
                                err.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error(500, "Произошла ошибка");
                }
            } else {
                error(400, "incorrect audio format");
            }
        }
    }

    public static void removeImage(String code) {
        JsonObject jo = new JsonObject();
        try {
            FileEntity fe = FileEntity.find("code=?", code).first();
            User user = getCurrentUser();

            if (fe != null) {
                if (fe.getCreator() == null || fe.getCreator().equals(user)) {
                    fe.setDeleted(1);
                    fe.save();

                    jo.addProperty("status", 200);
                    jo.addProperty("message", "Фотография успешно удалена");
                } else {
                    //TODO:check auth data
                    jo.addProperty("status", 403);
                    jo.addProperty("message", "У вас нет прав на удаление данной фотографии");
                }
            } else {
                jo.addProperty("status", 404);
                jo.addProperty("message", "Фотография не найдена");
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            jo.addProperty("status", 500);
            jo.addProperty("message", "Произошла ошибка");
        }

        renderJSON(jo);
    }

    public static void plainUploadBase64(String file, String filename, Long user_id, String id) {
        try {
            System.out.println("filename: " + filename);
            filename = filename.replaceAll(" ", "_");
            System.out.println("filename: " + filename);

            byte[] data = DatatypeConverter.parseBase64Binary(file
                    .replaceFirst("data:image/jpg;base64,", "")
                    .replaceFirst("data:image/jpeg;base64,", "")
                    .replaceFirst("data:image/png;base64,", "")
                    .replaceFirst("data:audio/wav;base64,", "")
                    .replaceFirst("data:audio/mp3;base64,", "")
                    .replaceFirst("data:audio/aac;base64,", "")
                    .replaceFirst("data:audio/m4a;base64,", "")
                    .replaceFirst("data:audio/3gp;base64,", ""));

            System.out.println("got byte data: " + data.length);
            File tempFile = File.createTempFile("base64", filename);
            FileUtils.writeByteArrayToFile(tempFile, data);

            System.out.println("tempFile: " + tempFile.getPath());

            //plainUpload(tempFile,user_id);
            User user = null;
            if (user_id != null) {
                user = User.find("id=?", user_id).first();
            }

            FileEntity fe = FileEntity.processFile(tempFile, "audio");
            fe.setCreator(user);
            fe.setIdentifier(id);
            fe.save();

            if (fe.getExtention().equalsIgnoreCase(".m4a") ||
                    fe.getExtention().equalsIgnoreCase(".3gp")){
                try {
                    String command = "ffmpeg -i "+
                            FileEntity.filesLocation+fe.getFilename()+" "+
                            FileEntity.filesLocation+fe.getFilename().replace(fe.getExtention(),".wav");
                    String line;
                    System.out.println("command: " + command.toString());
                    Process p = Runtime.getRuntime().exec
                            (command.toString());
                    BufferedReader input =
                            new BufferedReader
                                    (new InputStreamReader(p.getInputStream()));
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                    }
                    input.close();

                    int r = p.waitFor();

                    System.out.println("command result: " + r);
                    if (r != 1) {
                        fe.setFilename(fe.getFilename().replace(fe.getExtention(),".wav"));
                        fe.setMime("audio/wav");
                        fe.save();
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            renderJSON(fe.getJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void plainUpload(File file, Long user_id) {
        Command c = new Command();
        c.setCommand("plainUpload");
        Result result = getOkResult(c);
        System.out.println("got image upload request " + file.getPath());
        try {
            if (file != null) {
                User user = null;
                if (user_id != null) {
                    user = User.find("id=?", user_id).first();
                }
                try {
                    FileEntity fe = FileEntity.processFile(file, "pic" );
                    fe.setCreator(user);
                    fe.save();

                    if (fe.getExtention().equalsIgnoreCase(".m4a") ||
                            fe.getExtention().equalsIgnoreCase(".3gp")){
                        try {
                            String command = "ffmpeg -i "+
                                    FileEntity.filesLocation+fe.getFilename()+" "+
                                    FileEntity.filesLocation+fe.getFilename().replace(fe.getExtention(),".wav");
                            String line;
                            System.out.println("command: " + command.toString());
                            Process p = Runtime.getRuntime().exec
                                    (command.toString());
                            BufferedReader input =
                                    new BufferedReader
                                            (new InputStreamReader(p.getInputStream()));
                            while ((line = input.readLine()) != null) {
                                System.out.println(line);
                            }
                            input.close();

                            int r = p.waitFor();

                            System.out.println("command result: " + r);
                            if (r != 1) {
                                fe.setFilename(fe.getFilename().replace(fe.getExtention(),".wav"));
                                fe.setMime("audio/wav");
                                fe.save();
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }

                    Image image = fe.getJson();
                    //image.setFilename(fe.getTitle());

                    result.addParam("image", image);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.setStatus(SERVER_ERROR);
                    result.clearAllParams();
                }
            } else {
                result.setStatus(NOT_ENOGHT_PARAMS);
                result.clearAllParams();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(NOT_ENOGHT_PARAMS);
            result.clearAllParams();
        }
        System.out.println("result " + result);
        renderJSON(result);
    }

//    public static void getPoster(String code) {
//        Post post = Post.find("code=?", code).first();
//
//        if (post.getType() == Post.TYPE_BUY) {
//            renderBinary(generateFile(post));
//        } else {
//            error(418, "Nice try teapod");
//        }
//    }
//
//    public static File generateFile(Post post) {
//        File zzOut = new File(Play.applicationPath + "/public/uploads/buy_gen/" + post.getCode() + ".png");
//
//        /*if (zzOut.existxs()){
//            return zzOut;
//        }else {*/
//        List<File> files = new ArrayList<>();
//        List<String> titles = new ArrayList<>();
//
//        for (Car car : post.getCars()) {
//            if (car.getModel() != null) {
//                files.add(car.getModel().getBrand().getIcon().getFile());
//                titles.add(car.getModel().getTitle());
//            } else if (car.getBrand() != null) {
//                files.add(car.getBrand().getIcon().getFile());
//                titles.add("Все модели");
//            } else if (car.getCarBody() != null) {
//                files.add(car.getCarBody().getIcon().getFile());
//                titles.add(car.getCarBody().getTitle());
//            }
//        }
//
//
//        try {
//            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(Play.applicationPath + "" +
//                    "/public/uploads/buy_gen/helvetica-light-normal.ttf"));
//            GraphicsEnvironment ge =
//                    GraphicsEnvironment.getLocalGraphicsEnvironment();
//            ge.registerFont(font);
//
//            if (files.size() == 1) {
//                try {
//                    File zz = new File(Play.applicationPath + "/public/uploads/buy_gen/empty_1000.png");
//                    BufferedImage source = ImageIO.read(zz);
//                    BufferedImage logo = ImageIO.read(files.get(0));
//
//                    Graphics g = source.getGraphics();
//
//                    draw(g, logo, titles.get(0), 250, 250);
//
//                    ImageIO.write(source, "png", zzOut);
//                } catch (Exception e) {
//                    e.printStackTrace(System.out);
//                }
//            } else if (files.size() == 2) {
//                try {
//                    File zz = new File(Play.applicationPath + "/public/uploads/buy_gen/2_gen.png");
//                    BufferedImage source = ImageIO.read(zz);
//                    BufferedImage logo1 = ImageIO.read(files.get(0));
//                    BufferedImage logo2 = ImageIO.read(files.get(1));
//
//                    Graphics g = source.getGraphics();
//                    draw(g, logo1, titles.get(0), 0, 250);
//                    draw(g, logo2, titles.get(1), 500, 250);
//                    ImageIO.write(source, "png", zzOut);
//                } catch (Exception e) {
//                    e.printStackTrace(System.out);
//                }
//            } else if (files.size() == 3) {
//                try {
//                    File zz = new File(Play.applicationPath + "/public/uploads/buy_gen/3_gen.png");
//                    BufferedImage source = ImageIO.read(zz);
//                    BufferedImage logo1 = ImageIO.read(files.get(0));
//                    BufferedImage logo2 = ImageIO.read(files.get(1));
//                    BufferedImage logo3 = ImageIO.read(files.get(2));
//
//                    Graphics g = source.getGraphics();
//                    draw(g, logo1, titles.get(0), 0, 0);
//                    draw(g, logo2, titles.get(1), 500, 0);
//                    draw(g, logo3, titles.get(2), 250, 500);
//
//                    ImageIO.write(source, "png", zzOut);
//                } catch (Exception e) {
//                    e.printStackTrace(System.out);
//                }
//            } else if (files.size() == 4) {
//                try {
//                    File zz = new File(Play.applicationPath + "/public/uploads/buy_gen/4_gen.png");
//                    BufferedImage source = ImageIO.read(zz);
//                    BufferedImage logo1 = ImageIO.read(files.get(0));
//                    BufferedImage logo2 = ImageIO.read(files.get(1));
//                    BufferedImage logo3 = ImageIO.read(files.get(2));
//                    BufferedImage logo4 = ImageIO.read(files.get(3));
//
//                    Graphics g = source.getGraphics();
//                    draw(g, logo1, titles.get(0), 0, 0);
//                    draw(g, logo2, titles.get(1), 500, 0);
//                    draw(g, logo3, titles.get(2), 0, 500);
//                    draw(g, logo4, titles.get(3), 500, 500);
//
//                    ImageIO.write(source, "png", zzOut);
//                } catch (Exception e) {
//                    e.printStackTrace(System.out);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return zzOut;
//        //}
//
//    }
//
//    private static void draw(Graphics g, BufferedImage file, String title, int x, int y) throws Exception {
//        x += 75;
//        y += 25;
//        java.awt.Image fileImage = file.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
//        g.drawImage(fileImage, x, y, null);
//        g.setColor(Color.DARK_GRAY);
//        g.setFont(new Font("Helvetica_light-Normal", Font.TRUETYPE_FONT, 80));
//
//        int width = title.length() * 35;
//        int offset = (x + 175) - (width / 2);
//        g.drawString(title, offset, y + 420);
//
//    }
//
//    public static void generateCarQR(String code) {
//        String ex = "http://avtovse.kz/car/qr/" + code;
//
//        File qr = QRCode.from(ex).withColor(0xFF4D4183, 0xFFFFFFFF).withSize(550, 550).file();
//
//        renderBinary(qr);
//    }

    /*public static Result upload(Command command, Long user_id) {
        Result result = getOkResult(command);
        try {
            List<String> images = command.getAllParams("image");
            List<File> files = new ArrayList<File>();

            for (String image : images) {
                try {
                    File file = File.createTempFile("img_", "tmp_");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(DatatypeConverter.parseBase64Binary(image));
                    fos.close();
                    files.add(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            User user = User.find("id=?", user_id).first();
            try {
                for (File file : files) {
                    FileEntity fe = FileEntity.processFile(file, "pic");
                    fe.setCreator(user);
                    fe.save();
                    result.addParam("image", fe.getJson());
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.setStatus(SERVER_ERROR);
                result.clearAllParams();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(NOT_ENOGHT_PARAMS);
            result.clearAllParams();
        }
        return result;
    } */
}
