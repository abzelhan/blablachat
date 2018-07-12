package controllers;

import models.*;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import play.i18n.Messages;
import utils.helpers.ESClientHelper;
import utils.helpers.ESResponseFuture;
import utils.helpers.RedisHelper;

import java.util.Optional;

public class Endpoint extends Parent {

    public static void log(){
//        play.Logger.info("Hello");
        Logger errorLog = Logger.getLogger("info");
        errorLog.info("Fuck");

        renderText("ok");
    }



    public static void initESIndexAsync(){
        ESClientHelper instance = ESClientHelper.getInstance();
        Optional.ofNullable(User.find("deleted=0 and isSearchable=true").fetch())
                .ifPresent(usersList-> usersList.stream().forEach(u-> {
                    try {
                        ESResponseFuture<IndexResponse,String> responeFuture = new ESResponseFuture<>();
                        instance.addUserAsync((User) u,responeFuture);
                        String res =  responeFuture.getFetched();
                        System.out.println("Add "+((User)u).getCode()+" res "+res);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

    }

public static void initESIndex(){
    ESClientHelper instance = ESClientHelper.getInstance();
    Optional.ofNullable(User.find("deleted=0 and isSearchable=true").fetch())
                .ifPresent(usersList-> usersList.stream().forEach(u-> {
                    try {
                        instance.addUser((User) u);
                        System.out.println("Add "+((User)u).getCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
    renderText("Ok");
}


public static void deleteESIndex(){
    ESClientHelper instance = ESClientHelper.getInstance();
    Optional.ofNullable(User.find("deleted=0 and isSearchable=true").fetch())
            .ifPresent(usersList -> usersList.stream().forEach(
                    u-> {
                        try {
                            String s = instance.deleteUser((User) u);
                            System.out.println("Delete "+((User)u).getCode()+" "+s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


            ));
        renderText("OK");
}

public static void updateESIndex(){
    ESClientHelper instance = ESClientHelper.getInstance();
    Optional.ofNullable(User.find("deleted=0 and isSearchable=true").fetch())
            .ifPresent(userList -> userList.stream().forEach(u->{
                try {
                    ((User)u).setUsername("User"+((User)u).getCode());
                    instance.updateUser((User)u);
                    System.out.println("Update "+((User)u).getCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }));
    renderText("Ok");
}



    public static void deleteRoomsCache() {
        final RedisHelper redisHelper = RedisHelper.getInstance();
        Room.findAll().stream().forEach(r -> redisHelper.remove(((Room) r).getRedisCacheKey()));
    }

    public static void fillRoomsCache() {
        final RedisHelper redisHelper = RedisHelper.getInstance();
        Room.find("deleted=0").fetch().stream().forEach(r -> Member.find("room=:room and deleted=0").setParameter("room", r)
                .fetch().stream().filter(o -> (((Member) o).getDeleted() == 0)).
                        forEach(m -> redisHelper.lpush(((Room) r).getRedisCacheKey(), ((Member) m).getUser().getCode())));

    }


    public static void printMessage() {
        renderText(getMessage("abza", 1, 2));
    }


    public static String getMessage(Object... params) {
        return Messages.getMessage("en", "error.json_parameter.out_of_range", params);
    }


    public static void getCitiesWithoutParent(){

        City.find("parent=null").<City>fetch(0,5).stream().forEach(c-> System.out.println(c.getTitle()));
        renderText("Ok");
    }



    public static void initLang(String name, String shortcut) {
        Language lang = Language.find("name=:name").setParameter("name", name).first();
        if (lang != null) {
            renderText("Exist" + lang.getName() + lang.getShortcut());
        } else {
            lang = new Language();
            lang.setName(name);
            lang.setShortcut(shortcut);
            lang.save();
        }
        renderText("New lang = " + lang.getName() + " " + lang.getShortcut());

    }


}
