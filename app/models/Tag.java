package models;

import kz.api.json.TagJson;
import kz.wg.utils.Transliterator;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tags")
public class Tag  extends DomainObject{
    private String value;
    @ManyToMany(mappedBy = "tags")
    private List<User> users;
    @ManyToMany(mappedBy = "tags")
    private List<Room> rooms;

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public Tag() {
        users = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }


    public static List<Tag> getEmptyOrCreateByList(List<String> values){
        if (values != null) {
            return initListByValueList(values);
        } else {
            return null;
        }
    }

    public static List<Tag> getOrCreateByList(List<String> values) {
        if (values != null && !values.isEmpty()) {
           return initListByValueList(values);
        } else {
            return null;
        }
    }

    public static List<Tag> initListByValueList(List<String> values){
        List<Tag> list = new ArrayList<>();
        for (String pic : values) {
            pic = pic.toLowerCase();
            Tag tag = byValue(pic);
            if (tag == null && !pic.isEmpty()) {
                tag = new Tag();
                tag.setCreationDate(Calendar.getInstance());
                tag.setValue(pic);
                tag.save();
            }
            if(!list.contains(tag)) {
                list.add(tag);
            }
        }
        return list;
    }

    public static Tag byValue(String tag) {
        if (tag != null && !tag.isEmpty()) {
            return  Tag.find("value=:tag and deleted=0").setParameter("tag", tag).first();
        } else {
            return null;
        }
    }

    public TagJson getJson(){
        TagJson tagJson = new TagJson();
        tagJson.setCode(getCode());
        tagJson.setValue(getValue());
        return tagJson;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        if (!super.equals(o)) return false;
        Tag tag = (Tag) o;
        return id.equals(tag.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
