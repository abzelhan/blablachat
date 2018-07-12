/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

/**
 *
 * @author bakhyt.kudaybergenov <bakhyt@intellictech.com>
 */
public class Achtung {

    public static String SUCCESS = "achtungSuccess";
    public static String FAIL = "achtungFail";
    public static String WAIT = "achtungWait";
    String message;
    String style;
    int timeout;

    public Achtung(String message, String style, int timeout) {
        this.message = message;
        this.style = style;
        this.timeout = timeout;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
