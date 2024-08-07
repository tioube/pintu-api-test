package rest;

import com.jayway.jsonpath.JsonPath;
import commons.Util;
import configs.TestMasterConfigurations;
import io.restassured.module.jsv.JsonSchemaValidator;
import com.github.javafaker.Faker;
import com.jayway.jsonpath.JsonPath.*;
import org.testng.Assert;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class testRestAssured extends TestMasterConfigurations {

    Integer newUser;
    Integer postID;

//    @BeforeTest
//    private void generateUser(){
//        var Res = apiTest.generateNewUser();
//        newUser = com.jayway.jsonpath.JsonPath.read(Res.asString(),"$.data.id");
//        System.out.println(newUser);
//        for (int i=0;i<4;i++){
//            if (i==2){
//                var Post = apiTest.generatePost(
//                        newUser,"Catalyst","BUILD TRUST EMPOWER OTHERS"
//                );
//                postID = com.jayway.jsonpath.JsonPath.read(Post.asString(),"$.id");
//            }else{
//                var Post = apiTest.generatePost(
//                        newUser,"post baru","post baru "+i
//                );
//            }
//        }
//    }
    @Test
    public void getApi () {
        var Res = apiTest.testGetApi();
        Assert.assertEquals(Res.getStatusCode(),200);
        try (InputStream schemaInputStream = testRestAssured.class.getClassLoader().getResourceAsStream("testData/postScheme.json")) {
            if (schemaInputStream == null) {
                throw new FileNotFoundException("Schema file not found");
            }
            Res.then().assertThat()
                    .body(matchesJsonSchema(schemaInputStream));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred while validating JSON schema: " + e.getMessage());
        }
    }

    @Test
    public void postApi () {
        var title = new Faker().name().name().toString();
        var body = new Faker().address().fullAddress().toString();
        var number = Util.generateRandomNumericStringA(String.valueOf(1),4);
        var Res = apiTest.testPostApi(title, body, Integer.valueOf(number));
        Assert.assertEquals(Res.getStatusCode(),201);
        Assert.assertEquals(JsonPath.read(Res.asString(),"$.title"), title);
        Assert.assertEquals(JsonPath.read(Res.asString(),"$.body"), body);
        Assert.assertEquals(JsonPath.read(Res.asString(),"$.userId"), Integer.valueOf(number));
        Assert.assertNotNull(JsonPath.read(Res.asString(),"$.id"));
    }

//    @Test
//    public void testGetData() {
//        var Res = apiTest.testGetData(newUser);
//        String ResString = Res.asString();
//        JsonPath jsonPathEvaluator = Res.jsonPath();
//        List<String> title = jsonPathEvaluator.get("data.title");
//        List<String> body = jsonPathEvaluator.get("data.body");
//        Optional<Integer> totalData = Optional.of(title.size());
//        int indexTitle = title.indexOf("Catalyst");
//        int indexBody = body.indexOf("BUILD TRUST EMPOWER OTHERS");
////        System.out.println("masuk : "+title + indexTitle);
//        Assert.assertEquals(Optional.ofNullable(com.jayway.jsonpath.JsonPath.read(ResString, "$.meta.pagination.total")),totalData);
//        Assert.assertEquals(com.jayway.jsonpath.JsonPath.read(ResString,"$.data["+indexTitle+"].title"), "Catalyst");
//        Assert.assertEquals(com.jayway.jsonpath.JsonPath.read(ResString,"$.data["+indexBody+"].body"), "BUILD TRUST EMPOWER OTHERS");
//
//    }
//
//    @Test
//    public void failedGetData() {
//        var Res = apiTest.testFailedGetData();
//        String ResString = Res.asString();
//        JsonPath jsonPathEvaluator = Res.jsonPath();
//        List<String> title = jsonPathEvaluator.get("data.title");
//        List<String> body = jsonPathEvaluator.get("data.body");
//        int indexTitle = title.indexOf("Catalyst");
//        int indexBody = body.indexOf("BUILD TRUST EMPOWER OTHERS");
//        Assert.assertFalse(ResString.contains("data.title"));
//        Assert.assertFalse(ResString.contains("data.title"));
//
//    }
//    @Test
//    public void insertComment() {
//        var Res = apiTest.insertComment(postID,
//            "test insert comment 1","test@huy1.com","test insert commment success"
//        );
//        Assert.assertEquals(Res.getStatusCode(),201);
//        String ResString = Res.asString();
//        Assert.assertTrue(ResString.contains("post_id"));
//
//    }



}
