package controllers;

import play.mvc.Controller;

/**
 * Created by bakhyt on 8/28/17.
 */
public class App extends Controller {

    public static void index(){
        render();
    }

    public static void testwebsocket(){render();}

//    public static void testUpload() {
//        render();
//    }
//
//    public static void clearUser(String username) {
//
//        AutoUser user = AutoUser.find("username=:username").setParameter("username", username).first();
//        user.setEmail("0"+user.getEmail());
//        user.save();
//
//        List<DeviceToken> deviceTokens = DeviceToken.find("user=:user").setParameter("user",user).fetch();
//
//        for (DeviceToken token:deviceTokens){
//            token.setDeleted(1);
//            token.save();
//        }
//
//        renderText("changed phone and deleted all tokens of this user");
//    }
}
