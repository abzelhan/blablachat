package models;

import kz.api.json.Location.CityJson;
import kz.api.json.Location.CitySearchRowJson;

import javax.persistence.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Entity
@Table(name = "cities")
public class City extends DomainObject {
    @ManyToOne
    City parent;

    String title;

    String description;

    public City getParent() {
        return parent;
    }

    public void setParent(City parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public static List<City> byCodeList(List<String> codes) throws NoSuchElementException{
        if (codes != null && !codes.isEmpty()) {
            List<City> list = new ArrayList<>();
            for (String pic : codes) {
                City obj = byCode(pic);
                if (obj != null) {
                    list.add(obj);
                }
            }
            return list;
        } else {
            return null;
        }
    }


    public static City byCode(String code) throws NoSuchElementException{
        if (code != null && !code.isEmpty()) {
            City city = City.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if(city==null){
                throw new NoSuchElementException(code);
                }
                else return city;
        } else {
            throw new NoSuchElementException(code);
        }
    }

    public static City byCodeOptional(String code) throws NoSuchElementException{
        if (code != null && !code.isEmpty()) {
            City city = City.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if(city==null){
                throw new NoSuchElementException(code);
            }
            else return city;
        } else {
            return null;
        }
    }

    public CitySearchRowJson getJsonSearchRow(){
        CitySearchRowJson rowJson = new CitySearchRowJson();
        String city = "";
        String region = "";
        String country = "";

        City temp = this;

        if(getParent()==null){
            country = getTitle();
        }
        if(getParent()!=null && getParent().getParent()==null){
            region = getTitle();
            country = getParent().getTitle();
        }
        if(getParent()!=null && getParent().getParent()!=null){
            city = getTitle();
            region = getParent().getTitle();
            country = getParent().getParent().getTitle();
        }

        rowJson.setCity(city);
        rowJson.setRegion(region);
        rowJson.setCountry(country);
        rowJson.setCode(getCode());
//        rowJson.setCity(getTitle());
        return rowJson;
    }


    public CityJson getJson(){
        CityJson obj = new CityJson();
        obj.setIdentifier(id);
        obj.setTitle(title);
        obj.setCode(code);
        obj.setDescr(description);

        if (parent != null) {
            obj.setParent(parent.getJson());
        }

        obj.setHasChildren(City.find("parent=:parent").setParameter("parent", this).first() != null);

        /*List<City> children = City.find("parent=?",this).fetch();
        if (children!=null && !children.isEmpty()){
            List<kz.api.json.common.City> childrenJson = new ArrayList<>();
            for(City child:children){
                childrenJson.add(child.getJson());
            }
            obj.setChildren(childrenJson);
        }*/
        return obj;
    }

}
