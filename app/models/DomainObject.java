/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import kz.smf.utils.BaseX;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.jpa.Model;
import play.i18n.Lang;

import javax.persistence.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Bakhyt
 */
@MappedSuperclass
public abstract class DomainObject extends Model{

    @Transient
    public static int NEED_MODERATE = 0;
    @Transient
    public static int APPROVED = 1;
    @Transient
    protected User caller;
    @ManyToOne
    protected User creator;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    protected Calendar creationDate;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastModificationDate;

    @JsonIgnore
    @Column(columnDefinition = "integer default '0'")
    int deleted;


    String code;

    public String getCode() {
        if (code == null) {
            BaseX base = new BaseX(BaseX.DICTIONARY_32);
            code = base.encode(BigInteger.valueOf(id.longValue()));
            _save();
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDay() {
        try {
            if (this.creationDate != null) {
                return "" + this.creationDate.get(Calendar.DAY_OF_MONTH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCurrentLocale() {
        String locale = Lang.get();
        if (locale != null && !locale.isEmpty()) {
            if (locale.equalsIgnoreCase("kz")) {
                locale = "kz";
            } else if (locale.equalsIgnoreCase("en")) {
                locale = "en";
            } else {
                locale = "ru";
            }
        } else {
            locale = "ru";
        }
        return locale;
    }

    public String getMonth() {
        try {
            if (this.creationDate != null) {
                int month = this.creationDate.get(Calendar.MONTH) + 1;
                String l = getCurrentLocale();

                if (l.equals("kz")) {
                    switch (month) {
                        case 1:
                            return "Қан";
                        case 2:
                            return "Ақп";
                        case 3:
                            return "Нау";
                        case 4:
                            return "Сәу";
                        case 5:
                            return "Мам";
                        case 6:
                            return "Мау";
                        case 7:
                            return "Шіл";
                        case 8:
                            return "Там";
                        case 9:
                            return "Қыр";
                        case 10:
                            return "Қаз";
                        case 11:
                            return "Қар";
                        case 12:
                            return "Жел";
                    }
                } else if (l.equals("en")) {
                    switch (month) {
                        case 1:
                            return "Jan";
                        case 2:
                            return "Feb";
                        case 3:
                            return "Mar";
                        case 4:
                            return "Apr";
                        case 5:
                            return "May";
                        case 6:
                            return "Jun";
                        case 7:
                            return "Jul";
                        case 8:
                            return "Aug";
                        case 9:
                            return "Sep";
                        case 10:
                            return "Oct";
                        case 11:
                            return "Nov";
                        case 12:
                            return "Dec";
                    }
                } else {
                    switch (month) {
                        case 1:
                            return "Янв";
                        case 2:
                            return "Фев";
                        case 3:
                            return "Мар";
                        case 4:
                            return "Апр";
                        case 5:
                            return "Май";
                        case 6:
                            return "Июн";
                        case 7:
                            return "Июл";
                        case 8:
                            return "Авг";
                        case 9:
                            return "Сен";
                        case 10:
                            return "Окт";
                        case 11:
                            return "Ноя";
                        case 12:
                            return "Дек";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCreationDateStr() {
        if (creationDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
            return sdf.format(creationDate.getTime());
        } else {
            return "";
        }
    }

    public Calendar getCreationDate() {
        if (creationDate == null) {
            creationDate = Calendar.getInstance();
            _save();
        }
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Calendar getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Calendar lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    @Override
    public boolean equals(Object object) {
        try {
            DomainObject obj = (DomainObject) object;
            if (this.id.equals(obj.id)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public User getCaller() {
        return caller;
    }

    public void setCaller(User caller) {
        this.caller = caller;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }


}
