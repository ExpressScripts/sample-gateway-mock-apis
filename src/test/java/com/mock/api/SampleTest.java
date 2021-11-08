package com.mock.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
public class SampleTest {

    @Test
    public void testUri() throws URISyntaxException {
        String host = "http://whatever-host-name/";
        final URI uri = URI.create(host);
        URI uri1 = UriComponentsBuilder.fromUri(uri).port(443).build(false).toUri();
        Assertions.assertTrue(uri1.toString().equals("http://whatever-host-name:443/"));
    }

    @Test
    public void param() {
        String expected = "123213";
        String url = "https://whatever:blah/endpoint/v1/part1/" + expected +"/something";
        String pathParamKey = "/part1/{myParamId}/something";
        String regex = pathParamKey.substring(0, pathParamKey.indexOf("{")) + "(.*)" +
                (pathParamKey.indexOf("}") + 1 == pathParamKey.length()
                    ? ""
                    : pathParamKey.substring(pathParamKey.indexOf("}") + 1, pathParamKey.length()));
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        m.find();
        String text = m.group(1);
        System.out.println(text);
        Assertions.assertEquals(expected, text);
    }
}
