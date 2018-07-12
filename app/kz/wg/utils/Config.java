/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

import org.hibernate.HibernateException;
import play.Play;

/**
 *
 * @author bakhyt.kudaybergenov <bakhyt@intellictech.com>
 */
public class Config {

    public static String date_format = "dd.MM.yyyy HH:mm:ss";
    //public static String filesLocation = "/jboss/republicpalace.kz/ROOT/WEB-INF/public/images/uploads/";
    public static String filesLocation = Play.applicationPath+"/public/images/uploads/";
    //public static final String[] fileSources = {"/play/projects/republicpalace.kz/public/images/uploads/", "C:/dombira_files/", ""
      //      + "/Users/Bakhyt/NetbeansProjects/AdvancedPlay/republicpalace.kz/public/images/uploads/"};

    static {
        try {
            //Выбираем файловое хранилище
            /*for (String source : fileSources) {
                File f = new File(source);
                if (f.exists()) {
                    System.out.println("Choosed " + source);
                    filesLocation = source;
                    break;
                } else {
                    System.out.println("skipped files source: " + source);
                }
            }*/

            //Настроим ctx

            /*try {
                if (fileSources[0].equals(filesLocation)) {
                    ctx = "";
                    fullCtx = "http://demo.dombira.kz/";
                }
            } catch (Exception e) {
            }*/
        } catch (HibernateException ex) {
            throw new RuntimeException("Configuration problem: " + ex.getMessage(), ex);
        }
    }
    
    
}
