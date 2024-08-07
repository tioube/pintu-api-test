package rest;

import com.jayway.jsonpath.JsonPath;
import commons.Util;
import configs.TestMasterConfigurations;
import com.github.javafaker.Faker;
import org.testng.Assert;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import org.testng.annotations.Test;

import java.io.*;


public class testRestAssured extends TestMasterConfigurations {

    @Test
    public void getApi () {
        var Res = apiTest.testGetApi();
        Assert.assertEquals(Res.getStatusCode(),200);
        try (InputStream schemaInputStream = testRestAssured.class.getClassLoader().getResourceAsStream("testData/postScheme.json")) {
            if (schemaInputStream == null) {
                throw new FileNotFoundException("Schema file not found");
            }
            System.out.print("Checking Schema Should be True");
            Res.then().assertThat()
                    .body(matchesJsonSchema(schemaInputStream));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error when Validating Scheme: " + e.getMessage());
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




}
