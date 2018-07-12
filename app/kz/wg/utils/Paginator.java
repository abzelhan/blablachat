/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

/**
 *
 * @author VAIO
 */
public class Paginator {

    public String getPaginator(int page, Long total, int perPage, int visible, String url) {
        String ret = null;
        int pages = 0;
        if (total.intValue() % perPage == 0) {
            pages = total.intValue() / perPage;
        } else {
            pages = total.intValue() / perPage + 1;
        }
        if (pages > 0) {
            int start = 1;
            int end = visible;
            if ((page-1) * perPage > total.intValue()) {
                page = 1;
            } else if (page < 1) {
                page = 1;
            }
            ret = "<ul class='pager'>";
            if (page > visible) {
                ret += "<li class='pager-first'><a href='" + url + "1' title=''>  << </a></li>";
            }
            if (page != 1) {
                ret += "<li class='pager-previous'><a href='" + url + (page - 1) + "' title=''> < </a></li>";
            }
            if (pages < visible) {
                start = 1;
                end = pages;
           
            } else if (pages - page < visible) {
                start = pages - visible; 
                end = pages-1;
               
            } else if (page  > visible) {
                start = page - (visible - 1) / 2;
                end = page + (visible - 1) / 2;
            }
            for (int i = start; i <= end; i++) {
                if (i != page) {
                    ret += "<li class='pager-item'><a href='" + url + i + "'>" + i + "</a></li>";
                } else {
                    ret += "<li class='pager-current'>" + i  + "</li>";
                }
            }
            if (page != pages) {
                ret += "<li class='pager-next'><a href='" + url + (page + 1) + "' title=''> > </a></li>";
            }
            if (pages - page > visible) {
                ret += "<li class='pager-last'><a href='" + url + pages + "' title=''> >> </a></li>";
            }
            ret += "</ul>";
        }
        return ret;
    }
}
