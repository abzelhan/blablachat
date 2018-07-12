package models;

import com.google.gson.Gson;
import kz.api.json.LanguageJson;
import kz.api.json.TagJson;
import kz.api.json.User.UserJson;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User extends DomainObject {

    @Transient
    public static final String ES_INDEX_STR = "users";

    @Transient
    public static final String ES_TYPE_STR = "user";

    @Transient
    public int USER_ROLE = 0;
    @Transient
    public int ADMIN_ROLE = 1;

    String email;
    String username;
    String password;

    @Transient
    public static final String GENDER_MALE = "male";
    @Transient
    public static final String GENDER_FEMALE = "female";

    @Transient
    public static final int PASSWORD_MIN_LENGTH = 4;
    @Transient
    public static final int PASSWORD_MAX_LENGTH = 255;
    @Transient
    public static final int USERNAME_MIN_LENGTH = 4;
    @Transient
    public static final int USERNAME_MAX_LENGTH = 255;
    @Transient
    public static final int EMAIL_MIN_LENGTH = 10;
    @Transient
    public static final int EMAIL_MAX_LENGTH = 255;


    @Column(columnDefinition = "integer default '0'")
    long lastActivity;

    String gender;

    @Column(columnDefinition = "integer default '1'")
    boolean isInterestedInMale;

    @Column(columnDefinition = "integer default '1'")
    boolean isInterestedInFemale;

    @ManyToOne
    FileEntity image;


    @ManyToMany
    List<Tag> tags;
    @ManyToMany
    List<Language> languages;

    @ManyToOne
    City city;
    @Column(columnDefinition = "tinyint default '1'")
    boolean isSearchable;
    @Temporal(TemporalType.TIMESTAMP)
    Calendar birthdate;

    @Column(columnDefinition = "integer(11) default '0'")
    int userRole;

    @Transient
    SimpleDateFormat sdf = new SimpleDateFormat(User.BIRTH_DATE_FORMAT);

    @Transient
    public static final String BIRTH_DATE_FORMAT = "dd.MM.yyyy";

    public User() {
        tags = new ArrayList<>();
        languages = new ArrayList<>();
    }

    public static boolean isValidGender(String value) {
        return (!value.isEmpty() && (value.equals(GENDER_MALE) || value.equals(GENDER_FEMALE)));
    }


    public boolean isAdmin() {
        return userRole == ADMIN_ROLE;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }

    public boolean isSearchable() {
        return isSearchable;
    }

    public void setSearchable(boolean searchable) {
        isSearchable = searchable;
    }

    public Calendar getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Calendar birthdate) {
        this.birthdate = birthdate;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public User(String username) {
        this.username = username;
    }

    public static User byUsername(String username) {
        if (username != null && !username.isEmpty()) {
            return User.find("username=:username").setParameter("username", username).first();
        } else {
            return null;
        }
    }

    public static User byCodeRequired(String code) {
        if (code != null && !code.isEmpty()) {
            User user = User.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if (user == null) {
                throw new NoSuchElementException(code);
            }
            return user;
        } else {
            throw new NoSuchElementException(code);
        }
    }

    public static User byUsername(String username, int deleted) {
        if (username != null && !username.isEmpty()) {
            return User.find("username=:username and deleted=:deleted").
                    setParameter("username", username).
                    setParameter("deleted", deleted).
                    first();
        } else {
            return null;
        }
    }


    public Map<String, Object> getESOBject() {
        Gson gson = new Gson();
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("code", getCode());//code *
        jsonMap.put("username", getUsername());//username *
        jsonMap.put("password", getPassword());//password *
        jsonMap.put("deleted", getDeleted());//deleted *
        Optional.ofNullable(getEmail()).ifPresent(e -> jsonMap.put("email", e));//email ~
        Optional.ofNullable(getImage()).ifPresent(image -> jsonMap.put("icon", gson.toJson(image.getJson()))); //image ~
        jsonMap.put("searchable", isSearchable());//isSearchable * if true below
        if (isSearchable()) {
            jsonMap.put("gender", Optional.ofNullable(getGender()).orElse("man"));//yes, am a sexist

            jsonMap.put("isInterestedInFemale", isInterestedInFemale());
            jsonMap.put("isInterestedInMale", isInterestedInMale());
            jsonMap.put("city", Optional.ofNullable(getCity()).map(innerCity -> innerCity.getCode()).orElse("all"));
            jsonMap.put("birthdate",Optional.ofNullable(getBirthdate()).orElse(Calendar.getInstance()));//change this line
            jsonMap.put("languages", Optional.ofNullable(getLanguages()).map(innerList -> innerList.stream().
                    map(o -> o.getCode()).
                    collect(Collectors.toList()))
                    .orElse(Arrays.asList("all"))
            );
            jsonMap.put("tags",Optional.ofNullable(getTags()).map(tagList -> tagList.stream().
                    map(o -> o.getValue()).
                    collect(Collectors.toList()))
                    .orElse(Arrays.asList("all")));

//            Optional.ofNullable(getLanguages())
//                    .ifPresent(languages1 -> jsonMap.put("languages",
//                            languages1.stream().
//                                    map(o -> o.getCode()).
//                                    collect(Collectors.toList())));//list of codes
//            Optional.ofNullable(getTags())
//                    .ifPresent(tags1 -> jsonMap.put("tags",
//                            tags1.stream().
//                                    map(o -> o.getValue()).
//                                    collect(Collectors.toList())));//list of values
        }
        return jsonMap;
    }

    public UserJson getSimpleJson() {
        UserJson userJson = new UserJson();
        userJson.setUsername(getUsername());
        userJson.setCode(getCode());
        userJson.setGender(getGender());
        Optional.ofNullable(getImage()).
                ifPresent(image -> userJson.setIcon(image.getJson()));
        return userJson;
    }

    public UserJson getOptionalJson(boolean includePassword) {
        UserJson userJson = new UserJson();
        userJson.setCode(getCode());
        userJson.setUsername(username);
        if (includePassword) {
            userJson.setPassword(password);
        }
        userJson.setEmail(email);
        userJson.setSearchable(isSearchable);
        Optional.ofNullable(image).ifPresent(i -> userJson.setIcon(i.getJson()));
        if (isSearchable) {

            Optional.ofNullable(city).ifPresent(c -> userJson.setCity(getCity().getJson()));
            Optional.ofNullable(languages).ifPresent(
                    languages -> userJson.
                            setLanguages(languages.stream().map(l -> l.getJson()).collect(Collectors.toList())));
            Optional.ofNullable(tags).ifPresent(t -> userJson.setTags(t.stream().map(Tag::getJson).collect(Collectors.toList())));
            Optional.ofNullable(birthdate).ifPresent(b -> userJson.setBirthdate(sdf.format(getBirthdate().getTime())));
            userJson.setGender(gender);
            userJson.setInterestedInFemale(isInterestedInFemale);
            userJson.setInterestedInMale(isInterestedInMale);
            userJson.setLastActivity(lastActivity);
        }
        return userJson;
    }


    public UserJson getJson() {
        UserJson userJson = new UserJson();
        userJson.setUsername(username);
        userJson.setGender(gender);
        userJson.setInterestedInFemale(isInterestedInFemale);
        userJson.setInterestedInMale(isInterestedInMale);
        List<LanguageJson> languageJsons = new ArrayList<>();
        for (Language lang :
                languages) {
            languageJsons.add(lang.getJson());
        }
        userJson.setLanguages(languageJsons);
        userJson.setLastActivity(lastActivity);
        userJson.setPassword(password);
        userJson.setEmail(email);
        userJson.setCode(getCode());
        userJson.setCity(getCity().getJson());

        userJson.setBirthdate(sdf.format(getBirthdate().getTime()));

        //tags
        List<TagJson> tagJsons = new ArrayList<>();
        for (Tag tag :
                getTags()) {
            tagJsons.add(tag.getJson());
        }
        userJson.setTags(tagJsons);
        userJson.setSearchable(isSearchable);

        if (getImage() != null) {
            userJson.setIcon(getImage().getJson());
        }
        return userJson;
    }


    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public FileEntity getImage() {
        return image;
    }

    public void setImage(FileEntity image) {
        this.image = image;
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


    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
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


    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return (user.getCode().equals(getCode()));
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getEmail());
    }
}
