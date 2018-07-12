package models;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by baha on 6/10/15.
 */
@Entity
@Table(name = "logs")
public class Log extends DomainObject {

    String request;
    String response;
    int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
