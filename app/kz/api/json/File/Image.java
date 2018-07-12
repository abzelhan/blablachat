package kz.api.json.File;


import java.util.List;

/**
 * Created by baha on 6/17/15.
 */
public class Image {
    String code;
    String filename;
    String big;
    String small;
    String date;
    String filtered;
    String normal;
    String id;

    //audio

    List<SoundType> soundTypes;

    String original;
    String chipmunk;
    String agent;
    String mosquito;
    String alien;
    String female;
    String male;

    public List<SoundType> getSoundTypes() {
        return soundTypes;
    }

    public void setSoundTypes(List<SoundType> soundTypes) {
        this.soundTypes = soundTypes;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getChipmunk() {
        return chipmunk;
    }

    public void setChipmunk(String chipmunk) {
        this.chipmunk = chipmunk;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getMosquito() {
        return mosquito;
    }

    public void setMosquito(String mosquito) {
        this.mosquito = mosquito;
    }

    public String getAlien() {
        return alien;
    }

    public void setAlien(String alien) {
        this.alien = alien;
    }

    public String getFemale() {
        return female;
    }

    public void setFemale(String female) {
        this.female = female;
    }

    public String getMale() {
        return male;
    }

    public void setMale(String male) {
        this.male = male;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFiltered() {
        return filtered;
    }

    public void setFiltered(String filtered) {
        this.filtered = filtered;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBig() {
        return big;
    }

    public void setBig(String big) {
        this.big = big;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }
}
