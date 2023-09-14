package dev.zbendhiba.demo.camel.rest;

import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.model.rest.RestBindingMode;

import java.util.UUID;

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
                .to("direct:order")
                .get("/order").description("The list of all the coffee orders")
                .to("direct:orders-api")
                .get("/order/{id}").description("A Coffee order by id")
                .to("direct:order-api");

        from("direct:order")
                .routeId("orders")
                .bean(MyBean.class, "generateOrder")
                .to("jpa:"+CoffeeOrder.class)
                .wireTap("direct:notify")
                .log(" New order ${body}")
                .setBody(constant("Thank you for your order"))
                ;

        from("direct:orders-api")
                .routeId("orders-api")
                .log("Received a message in route orders-api")
                .to("jpa:" + CoffeeOrder.class + "?namedQuery=findAll")
        ;

        from("direct:order-api")
                .routeId("order-api")
                .log("Received a message in route order-api")
                .toD("jpa://" + CoffeeOrder.class.getName() + "?query=select m  from " + CoffeeOrder.class.getName() + " m  where id =${header.id}")
        ;

        from("direct:notify")
                .to("direct:s3")
                .wireTap("direct:notify-delivery");

        from("direct:s3")
                .log("sending to S3")
                .marshal().json()
                .setHeader(AWS2S3Constants.KEY, () -> UUID.randomUUID())
                .log(String.format("Sending message with header :: ${header.%s}", AWS2S3Constants.KEY))
                .to("aws2-s3:{{aws-s3.bucket-name}}");
        ;

        from("direct:notify-delivery")
                .bean(MyBean.class, "generateNotification")
                .log("Sending notification for delivery ${body}")
                .to("telegram:bots?chatId={{telegram.chatId}}");
    }
}
