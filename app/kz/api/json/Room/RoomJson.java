package kz.api.json.Room;

import kz.api.json.File.Image;
import kz.api.json.InviteJson;
import kz.api.json.LanguageJson;
import kz.api.json.Location.CityJson;
import kz.api.json.TagJson;
import kz.api.json.User.UserJson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RoomJson {
    private String code;
    private Calendar creationDate;
    private String title;
    private Boolean isPrivate;
    private Image icon;
    private int type;
    private String description;
    private long limit;
    private long size;
    private Image background;
    private List<MemberJson> members;
    private List<MemberJson> blackList;
    CityJson city;
    List<TagJson> tags;
    List<LanguageJson> languages;
    private InviteJson invite;

    public InviteJson getInvite() {
        return invite;
    }

    public RoomJson setInvite(InviteJson invite) {
        this.invite = invite;
        return this;
    }

    public List<MemberJson> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<MemberJson> blackList) {
        this.blackList = blackList;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public CityJson getCity() {
        return city;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCity(CityJson city) {
        this.city = city;
    }

    public List<TagJson> getTags() {
        return tags;
    }

    public void setTags(List<TagJson> tags) {
        this.tags = tags;
    }

    public List<LanguageJson> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageJson> languages) {
        this.languages = languages;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Image getBackground() {
        return background;
    }

    public void setBackground(Image background) {
        this.background = background;
    }

    public List<MemberJson> getMembers() {
        return members;
    }

    public void setMembers(List<MemberJson> members) {
        this.members = members;
    }
}
