package controllers;

import com.google.gson.Gson;
import kz.api.json.Context;
import kz.api.json.System.ServiceEvent;
import models.User;
import utils.helpers.RedisHelper;

import java.security.AccessControlException;

/**
 * Created by abzalsahitov@gmail.com  on 3/24/18.
 */
public class BaseApi {


    public static String generateToken() {
        String token = "token-" + java.util.UUID.randomUUID().toString();
        int count = 0;
        while (RedisHelper.getInstance().get(token) != null) {
            token = token + "-" + count;
            count++;
        }
        return token;
    }

    public static User getRequiredUser(Context context) throws AccessControlException {
        User user = getOptionalUser(context);

        if (user == null) {
            throw new AccessControlException("You need call adminLogin");
        } else {
            return user;
        }
    }

    public static User getOptionalUser(Context context) {
        return User.find("id=:id and deleted=0").setParameter("id", context.getUser_id()).first();
    }

    public static void causePersonalEvent(String destinationUserCode,
                                          String roomCode,
                                          int serviceCode,
                                          String messageBody) {
        ServiceEvent serviceEvent = ServiceEvent.createRoomEvent(
                serviceCode,
                destinationUserCode,
                messageBody);
        serviceEvent.setRoom(roomCode);
        serviceEvent.setTo(destinationUserCode);
        serviceEvent.setServiceType(ServiceEvent.SERVICE_TYPE_PERSONAL);
        RedisHelper.getInstance().lpush(ServiceEvent.REDIS_KEY,
                new Gson().toJson(serviceEvent)
        );
    }


    public static void causeBroadcastRoomEvent(String destinationRoomCode,
                                               int serviceCode,
                                               String messageBody,
                                               String excludeCode
    ) {
        ServiceEvent serviceEvent = ServiceEvent.createRoomEvent(
                serviceCode,
                destinationRoomCode,
                messageBody
        );
        if(excludeCode!=null){
            serviceEvent.setExcludeCode(excludeCode);
        }
        serviceEvent.setServiceType(ServiceEvent.SERVICE_TYPE_BROADCAST);
        RedisHelper.getInstance().lpush(ServiceEvent.REDIS_KEY,
                new Gson().toJson(serviceEvent)
        );
    }

//    public static void causeDefaultPersonalEventThenBroadcast(
//            String destinationUserCode,
//            String destinationRoomCode,
//            int serviceCode,
//            String messageBody
//    ) {
//        ServiceEvent serviceEvent = ServiceEvent.createRoomEvent(
//                serviceCode,
//                destinationRoomCode,
//                messageBody
//        );
//        serviceEvent.setTo(destinationUserCode);
//
//        RedisHelper.getInstance().lpush(ServiceEvent.REDIS_KEY,
//                new Gson().toJson(serviceEvent)
//        );
//    }


}
