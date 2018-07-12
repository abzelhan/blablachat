package controllers;

import kz.wg.utils.Achtung;
import models.User;

public class Control extends Parent {

    public static void index() {
        render();
    }

    public static void login(String login, String password) {
        User user = User.find("username=:login and password=:password")
                .setParameter("login", login)
                .setParameter("password", password)
                .first();
        if (user != null) {
            System.out.println("Logged in " + user.getUserRole());
            session.put("user", user.getId());
        } else {
            addAchtung("Не удалось войти в систему, попробуйте еще.", Achtung.FAIL, 10);
        }
        redirect("/docs");
    }

    public static void logout() {
        session.clear();
        Control.index();
    }
}
