/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 *
 * @author apple
 */
public class Utf8Dialect extends MySQL5InnoDBDialect {

    public String getTableTypeString() {
        return " ENGINE=MyISAM DEFAULT CHARSET=utf8";
    }
}
