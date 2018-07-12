/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author bakhyt.kudaybergenov <bakhyt@intellictech.com>
 */
public class Translator {

    public static ResourceBundle defaultRb = ResourceBundle.getBundle("StripesResources", new Locale("ru_RU"));

    public static String getVotesStr(int votes){
        String votesStr = votes+" голосов";
        int temp = votes - ((votes%10)*10);
        if ((votes<5&&votes>1)||((votes>21)&&(temp<5))){
            votesStr = votes+" голоса";
        }else if ((votes%10==1)){
            votesStr = votes+" голос";
        }
        return votesStr;
    }
    public static String translate(String text, Locale locale) {
        try {
            String result = defaultRb.getString(text);
            try {
                ResourceBundle rb = ResourceBundle.getBundle("StripesResources", locale);
                result = rb.getString(text);
            } catch (Exception e) {e.printStackTrace();}
            return result;
        } catch (Exception e) {
            return text;
        }
    }
}
