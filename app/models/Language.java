package models;

import kz.api.json.LanguageJson;
import play.i18n.Lang;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Entity
@Table(name = "languages")
public class Language extends DomainObject{

    private String name;
    private String shortcut;
    @ManyToMany(mappedBy = "languages")
    private List<User> users;
    @ManyToMany(mappedBy = "languages")
    private List<Room> rooms;

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public Language() {
        this.users = new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public static List<Language> byCodeListOrEmpty(List<String> codes){
        if (codes != null ) {
            return initListByCodeList(codes);
        } else {
            return null;
        }
    }


    public static List<Language> byList(List<String> codes){
        if (codes != null && !codes.isEmpty()) {
           return codes.stream()
                   .map(c->Language.find("code=:code and deleted=0")
                           .setParameter("code",c).<Language>first())
                   .filter(l->l!=null)
                   .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public static List<Language> byCodeList(List<String> codes) {
        if (codes != null && !codes.isEmpty()) {
           return initListByCodeList(codes);
        } else {
            return null;
        }
    }

    public static List<Language> initListByCodeList(List<String> codes){
        List<Language> list = new ArrayList<>();
        for (String pic : codes) {
            Language obj = byCode(pic);
            if (obj != null) {
                if(!list.contains(obj)){
                list.add(obj);}
            }
        }
        return list;
    }

    public static Language byCode(String code) {
        if (code != null && !code.isEmpty()) {
            Language lang = Language.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if(lang==null){
                throw new NoSuchElementException(code);
            }
            return lang;
        } else {
            return null;
        }
    }

    public LanguageJson getJson(){
        LanguageJson languageJson = new LanguageJson();
        languageJson.setCode(getCode());
        languageJson.setShortcut(getShortcut());
        languageJson.setName(getName());
        return languageJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Language language = (Language) o;

        return getId().equals(language.getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }
}
