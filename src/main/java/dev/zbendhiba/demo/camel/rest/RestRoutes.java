package dev.zbendhiba.demo.camel.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;


public class RestRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
        getContext().getGlobalOptions().put("CamelJacksonTypeConverterToPojo", "true");

        restConfiguration()
                .bindingMode(RestBindingMode.json)
                .componentProperty("lazyStartProducer", "true")
                .dataFormatProperty("autoDiscoverObjectMapper", "true");


        /**
         * REST api to add and fetch Coffee Orders
         */
        rest("order-api").description("Coffee Orders REST service")
                .post("/order").description("Add a new coffee Order")
                .to("direct:add-order")
                .get("/order").description("The list of all the coffee orders")
                .to("direct:orders-api")
                .get("/order/{id}").description("A Coffee order by id")
                .to("direct:order-api");

    }
}
