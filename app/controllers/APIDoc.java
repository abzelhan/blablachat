package controllers;

import jobs.UpdateAPIDocJob;
import models.APIMethod;
import models.APIParam;
import models.User;
import play.db.jpa.JPA;
import play.libs.F;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bakhyt on 10/16/17.
 */
public class APIDoc extends Parent {
    public static void update() {
        User user = getCurrentUser();
        if (user.isAdmin()) {
            UpdateAPIDocJob job = new UpdateAPIDocJob();
            F.Promise p = job.now();
            await(p);
            renderText("job executed");
        } else {
            error(401, "need to auth");
        }
    }

    private static void injectNav() {
        List<String> apis = (List<String>) JPA.em().createQuery("select distinct (o.apiName) from APIMethod o")
                .getResultList();
//        List<String> apis = Arrays.asList("BlaBlaChatApi");
        renderArgs.put("injectNav", apis);
    }

    public static void api(String className) {
        injectNav();

//        List<APIMethod> methods = APIMethod.find("className=? and deleted=0 order by id desc", className).fetch();
        List<APIMethod> methods = APIMethod.find("apiName=? and deleted=0 order by id desc", className).fetch();
        render(methods,className);
    }

    public static void edit(Long id) {
        injectNav();
        APIMethod method = APIMethod.find("id=:id").setParameter("id", id).first();
        render(method);
    }

    public static void save(APIMethod method, APIParam[] params) {
        APIMethod m = APIMethod.find("id=:id").setParameter("id", method.getId()).first();
        if(m!=null) {
            m.setDescription(method.getDescription());
            m.save();
        }
        if(params!=null){
            for (APIParam param : params) {
                APIParam p = APIParam.find("id=:id").setParameter("id", param.getId()).first();
                p.setDescription(param.getDescription());
                p.save();
            }
        }
        index();
    }

    public static void index(){
        injectNav();
        render();
    }

}
