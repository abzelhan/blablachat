package models;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by bakhyt on 10/11/17.
 */
@Entity
@Table(name = "api_methods", schema = "blablachat")
public class APIMethod extends DomainObject {
    String name;
    String className;
    String methodName;
    String apiName;
    @Column(columnDefinition = "tinyint default '0'")
    boolean needAuth;

    @Column(columnDefinition = "longtext")
    String description;

    @OneToMany(mappedBy = "apiMethod")
    List<APIParam> params;

    public String getExample() throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject params = new JSONObject();
        for (APIParam param : this.params) {
            String name = param.getName();
            switch (param.getType()) {
                case "string":
                    params.put(name, "");
                    break;
                case "integer":
                    params.put(name, 0);
                    break;
                case "long":
                    params.put(name, 0);
                    break;
                case "double":
                    params.put(name, 0.0);
                    break;
                case "code":
                    params.put(name, "");
                    break;
                case "boolean":
                    params.put(name, false);
                    break;
                case "list":
                    JSONArray arr = new JSONArray("[\"\"]");
                    params.set(name, arr);
                    break;
                case "intlist":
                    JSONArray iarr = new JSONArray("[0,0]");
                    params.set(name, iarr);
                    break;
                case "longlist":
                    JSONArray larr = new JSONArray("[0,0]");
                    params.set(name, larr);
                    break;
                case "codelist":
                    JSONArray carr = new JSONArray("[{\"code\":\"\"}]");
                    params.set(name, carr);
                    break;
                case "jsonarray":
                    JSONArray jarr = new JSONArray("[]");
                    params.set(name, jarr);
                    break;
                case "calendar":
                    params.put(name, "");
                    break;
            }

        }
        jsonObject.put("params", params);
        jsonObject.put("command", name);
        //com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        //return mapper.writeValueAsString(jsonObject);
        return jsonObject.toString(2);
    }

    public String getApiName() {
        return apiName;
    }

    public APIMethod setApiName(String apiName) {
        this.apiName = apiName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<APIParam> getParams() {
        if (this.getId() != null && this.getId() > 0) {
            params = APIParam.find("apiMethod=:method and deleted=0 order by required desc").setParameter("method", this).fetch();
        }
        return params;
    }

    public void setParams(List<APIParam> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }
}
