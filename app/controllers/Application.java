package controllers;

import com.google.gson.JsonArray;

public class Application extends Parent {

    public static void license(){
        render();
    }

    public static void index() {
        render();
    }

    public static void cabinet() {
        render();
    }

    public static void nav() {
        JsonArray arr = new JsonArray();
        renderText("");
    }

    public static void test(){
        render();
    }
}