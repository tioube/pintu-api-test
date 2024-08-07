package rest;

import com.github.javafaker.IdNumber;
import commons.Util;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;


public class apiTest {

    @Step
    public static Response testGetApi () {
        String url = "https://jsonplaceholder.typicode.com/posts";
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        return given().headers(headers).when().get(url);
    }

    @Step
    public static Response testPostApi (String title, String body, Integer userId) {
        String url = "https://jsonplaceholder.typicode.com/posts";
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("title",title);
        params.put("body",body);
        params.put("userId",userId);
        params.values().removeAll(Collections.singleton(null));
        String json = Util.mapToJson(params);
        return given().headers(headers).body(json).when().post(url);
    }


//    @Step
//    public static Response generateNewUser() {
//        String url = "https://gorest.co.in/public/v1/users";
//        Map<String, Object> headers = new LinkedHashMap<>();
//        var name = new Faker().funnyName().name();
//        var email = new Faker().internet().emailAddress();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization","Bearer 9f5bef7258bb7aee68e0c4607d1b752fe678833567ac1189aa20d9603e987440");
//        Map<String, Object> params = new LinkedHashMap<>();
//        params.put("name", name);
//        params.put("email",email);
//        params.put("gender","male");
//        params.put("status","active");
//        params.values().removeAll(Collections.singleton(null));
//        String json = Util.mapToJson(params);
//        return given().headers(headers).body(json).when().post(url);
//    }
//
//    public static Response generatePost(Integer user,String title,String body) {
//        String url = "https://gorest.co.in/public/v2/users/"+user+"/posts";
//        Map<String, Object> headers = new LinkedHashMap<>();
//        var name = new Faker().funnyName().name();
//        var email = new Faker().internet().emailAddress();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization","Bearer 9f5bef7258bb7aee68e0c4607d1b752fe678833567ac1189aa20d9603e987440");
//        Map<String, Object> params = new LinkedHashMap<>();
//        params.put("user_id", user);
//        params.put("title",title);
//        params.put("body",body);
//        params.values().removeAll(Collections.singleton(null));
//        String json = Util.mapToJson(params);
//        return given().headers(headers).body(json).when().post(url);
//    }
//
//    @Step
//    public static Response testGetData(Integer user) {
//        String url = "https://gorest.co.in/public/v1/users/"+user+"/posts";
//        Map<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization","Bearer 9f5bef7258bb7aee68e0c4607d1b752fe678833567ac1189aa20d9603e987440");
//        return given().headers(headers).when().get(url);
//    }
//
//    @Step
//    public static Response testFailedGetData() {
//        String url = "https://gorest.co.in/public/v1/users/5168745/posts";
//        Map<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization","Bearer 9f5bef7258bb7aee68e0c4607d1b752fe678833567ac1189aa20d9603e987440");
//        return given().headers(headers).when().get(url);
//    }
//
//    public static Response insertComment(Integer postID,String name,String email,String body) {
//        String url = "https://gorest.co.in/public/v1/posts/"+postID+"/comments";
//        Map<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization","Bearer 9f5bef7258bb7aee68e0c4607d1b752fe678833567ac1189aa20d9603e987440");
//        Map<String, Object> params = new LinkedHashMap<>();
//        params.put("post_id",69975);
//        params.put("name",name);
//        params.put("email",email);
//        params.put("body",body);
//        params.values().removeAll(Collections.singleton(null));
//        String json = Util.mapToJson(params);
//        return given().headers(headers).body(json).when().post(url);
//    }
}
