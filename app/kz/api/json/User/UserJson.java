package kz.api.json.User;

import kz.api.json.File.Image;
import kz.api.json.LanguageJson;
import kz.api.json.Location.CityJson;
import kz.api.json.TagJson;

import java.util.List;

public class UserJson {
    String email;
    String username;
    String password;
    Long lastActivity;
    String gender;
    Boolean isInterestedInMale;
    Boolean isInterestedInFemale;
    Boolean searchable;
    Image icon;
    String code;
    CityJson city;
    List<TagJson> tags;
    List<LanguageJson> languages;
    String birthdate;

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public List<LanguageJson> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageJson> languages) {
        this.languages = languages;
    }

    public List<TagJson> getTags() {
        return tags;
    }

    public void setTags(List<TagJson> tags) {
        this.tags = tags;
    }

    public CityJson getCity() {
        return city;
    }

    public void setCity(CityJson city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isInterestedInMale() {
        return isInterestedInMale;
    }

    public void setInterestedInMale(boolean interestedInMale) {
        isInterestedInMale = interestedInMale;
    }

    public boolean isInterestedInFemale() {
        return isInterestedInFemale;
    }

    public void setInterestedInFemale(boolean interestedInFemale) {
        isInterestedInFemale = interestedInFemale;
    }


    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
