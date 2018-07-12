/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Bakhyt <bakhyt@intellictech.com>
 */
public class Formatter {
    
    
    public static Calendar getCalendar(String time) {
        Calendar c = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d = df.parse(time);
            c = Calendar.getInstance();
            c.setTime(d);
        } catch (Exception e) {
        }
        return c;
    }

    public static String formatForSite(Calendar c) {
        String ret = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            ret = df.format(c.getTime());
        } catch (Exception e) {
        }
        return ret;
    }
    public static String formatForBootstrap(Calendar c) {
        String ret = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            ret = df.format(c.getTime());
        } catch (Exception e) {
        }
        return ret;
    }

    public static String getTime(Calendar c) {
        String ret = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            ret = df.format(c.getTime());
        } catch (Exception e) {
        }
        return ret;
    }

    public static String parse(Date d) {
        SimpleDateFormat df = new SimpleDateFormat(Config.date_format);
        return df.format(d);
    }

    public static String encode(String s) {
        String link = null;
        try {
            link = Base64.encodeBytes(s.getBytes());
        } catch (Exception e) {
        }
        return link;
    }

    public static String decode(String s) {
        String link = null;
        try {
            link = new String(Base64.decode(s));
        } catch (Exception e) {
        }
        return link;
    }

    public static String extension(String filename) {
        int dot = filename.lastIndexOf(".");
        return filename.substring(dot + 1);
    }
}
