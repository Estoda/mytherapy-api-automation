package base;

import io.restassured.RestAssured;
import utils.Config;

import org.testng.annotations.BeforeClass;

public class BaseTest 
{
    @BeforeClass
    public void setup()
    {
        RestAssured.baseURI = Config.baseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}