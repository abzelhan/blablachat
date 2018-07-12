package kz.api.json.Location;

import java.util.List;

public class CityJson {
    Long identifier;
    String title;
    String code;
    String descr;
    CityJson parent;
    List<CityJson> children;
    boolean hasChildren;

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public CityJson getParent() {
        return parent;
    }

    public void setParent(CityJson parent) {
        this.parent = parent;
    }

    public List<CityJson> getChildren() {
        return children;
    }

    public void setChildren(List<CityJson> children) {
        this.children = children;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
}
