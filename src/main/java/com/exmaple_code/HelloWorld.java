package com.exmaple_code;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by ZloiY on 05.04.17.
 */
@Path("/helloworld")
public class HelloWorld {
    @GET
    @Produces("text/plain")
    public String getHelloWorldMessage(){
        return "Hello World!";
    }
}
