package rest;

import configs.TestMasterConfigurations;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public class testRestAssured extends TestMasterConfigurations {

    Integer newUser;
    Integer postID;

    @BeforeTest
    private void generateUser(){
        var Res = apiTest.generateNewUser();
        newUser = com.jayway.jsonpath.JsonPath.read(Res.asString(),"$.data.id");
        System.out.println(newUser);
        for (int i=0;i<4;i++){
            if (i==2){
                var Post = apiTest.generatePost(
                        newUser,"Catalyst","BUILD TRUST EMPOWER OTHERS"
                );
                postID = com.jayway.jsonpath.JsonPath.read(Post.asString(),"$.id");
            }else{
                var Post = apiTest.generatePost(
                        newUser,"post baru","post baru "+i
                );
            }
        }
    }
    @Test
    public void testAPiNew () {
        var Res = apiTest.testApi();
        Assert.assertEquals(Res.getStatusCode(),200);
    }

    @Test
    public void testGetData() {
        var Res = apiTest.testGetData(newUser);
        String ResString = Res.asString();
        JsonPath jsonPathEvaluator = Res.jsonPath();
        List<String> title = jsonPathEvaluator.get("data.title");
        List<String> body = jsonPathEvaluator.get("data.body");
        Optional<Integer> totalData = Optional.of(title.size());
        int indexTitle = title.indexOf("Catalyst");
        int indexBody = body.indexOf("BUILD TRUST EMPOWER OTHERS");
//        System.out.println("masuk : "+title + indexTitle);
        Assert.assertEquals(Optional.ofNullable(com.jayway.jsonpath.JsonPath.read(ResString, "$.meta.pagination.total")),totalData);
        Assert.assertEquals(com.jayway.jsonpath.JsonPath.read(ResString,"$.data["+indexTitle+"].title"), "Catalyst");
        Assert.assertEquals(com.jayway.jsonpath.JsonPath.read(ResString,"$.data["+indexBody+"].body"), "BUILD TRUST EMPOWER OTHERS");

    }

    @Test
    public void failedGetData() {
        var Res = apiTest.testFailedGetData();
        String ResString = Res.asString();
        JsonPath jsonPathEvaluator = Res.jsonPath();
        List<String> title = jsonPathEvaluator.get("data.title");
        List<String> body = jsonPathEvaluator.get("data.body");
        int indexTitle = title.indexOf("Catalyst");
        int indexBody = body.indexOf("BUILD TRUST EMPOWER OTHERS");
        Assert.assertFalse(ResString.contains("data.title"));
        Assert.assertFalse(ResString.contains("data.title"));

    }
    @Test
    public void insertComment() {
        var Res = apiTest.insertComment(postID,
            "test insert comment 1","test@huy1.com","test insert commment success"
        );
        Assert.assertEquals(Res.getStatusCode(),201);
        String ResString = Res.asString();
        Assert.assertTrue(ResString.contains("post_id"));

    }



}
