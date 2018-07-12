package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by bakhyt on 10/11/17.
 */
@Entity
@Table(name = "api_params", schema = "blablachat")
public class APIParam extends DomainObject {
    String name;
    String type;
    @Column(columnDefinition = "tinyint default '0'")
    boolean required;
    @ManyToOne
    APIMethod apiMethod;
    @Column(columnDefinition = "longtext")
    String description;

    public APIMethod getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(APIMethod apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
