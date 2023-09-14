package dev.zbendhiba.demo.camel.rest;

import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;

public class RestRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        /**
         * This Camel Route simulates the external app that adds orders
         */
        from("timer:create-random-coffee-orders?period={{timer.period}}")
                .to("https:{{random-coffee-api}}")
                .unmarshal().json(Coffee.class)
                .bean(MyBean.class, "generateOrder")
                .marshal().json()
                .to("kafka:orders");

        // This is the Kafka Consumer Route to implement
        from("kafka:orders")
                .log("received from kafka : ${body}")
                .unmarshal().json(CoffeeOrder.class)
                .to("jpa:"+CoffeeOrder.class)
                .bean(MyBean.class, "generateNotification")
                .log("${body}")
                .to("telegram:bots?chatId={{telegram.chatId}}")
        ;

        /**
         * REST api to fetch Coffee Orders
         */
        rest("order-api").description("Coffee Orders REST service")
                .post().description("Add a new coffee Order")
                .to("direct:order")
                .get("/order").description("The list of all the coffee orders")
                .to("direct:orders-api")
                .get("/order/{id}").description("A Coffee order by id")
                .to("direct:order-api");

        from("direct:order")
                .routeId("orders")
                .log("Add message")
                ;

        from("direct:orders-api")
                .routeId("orders-api")
                .log("Received a message in route orders-api")
                .to("jpa:" + CoffeeOrder.class + "?namedQuery=findAll")
                .marshal().json();

        from("direct:order-api")
                .routeId("order-api")
                .log("Received a message in route order-api")
                /*Complete the route to fetch order by id*/
                .toD("jpa://" + CoffeeOrder.class.getName() + "?query=select m  from " + CoffeeOrder.class.getName() + " m  where id =${header.id}")
                .marshal().json()
        ;
    }
}
