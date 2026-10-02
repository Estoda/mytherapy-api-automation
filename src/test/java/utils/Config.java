package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config 
{
    private static final Properties props = new Properties();

    static 
    {
        try (InputStream in = Config.class.getClassLoader()
                        .getResourceAsStream("config.properties"))
                        {
                            if (in == null)
                            {
                                throw new RuntimeException(
                                    "config.properties not found in src/test/resources"
                                );
                            }
                            props.load(in);
                        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    private static String get(String key)
    {
        String value = props.getProperty(key);
        if (value == null)
        {
            throw new RuntimeException("Missing config key: " + key);
        }
        return value;
    }

    public static String baseUrl()
    {
        return get("baseUrl");
    }

    public static String patientEmail()
    {
        return get("patient.email");
    }

    public static String patientPassword()
    {
        return get("patient.password");
    }
}
