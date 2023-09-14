package dev.zbendhiba.demo.camel.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;

import java.util.UUID;

public class OperationRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:add-order")
                .routeId("add-order")
                .bean(OrderBean.class, "generateOrder")
                .to("jpa:"+CoffeeOrder.class)
                .wireTap("direct:notify")
                .log(" New order ${body}")
                .setBody(constant("Thank you for your order"));

        from("direct:orders-api")
                .routeId("orders-api")
                .log("Received a message in route orders-api")
                .to("jpa:" + CoffeeOrder.class + "?namedQuery=findAll");

        from("direct:order-api")
                .routeId("order-api")
                .log("Received a message in route order-api")
                .toD("jpa://" + CoffeeOrder.class.getName() + "?query=select m  from " + CoffeeOrder.class.getName() + " m  where id =${header.id}");

        from("direct:notify")
                .to("direct:s3")
                .wireTap("direct:notify-delivery");

        from("direct:s3")
                .log("sending to S3")
                .marshal().json()
                .setHeader(AWS2S3Constants.KEY, () -> UUID.randomUUID())
                .log(String.format("Sending message with header :: ${header.%s}", AWS2S3Constants.KEY))
                .to("aws2-s3:{{aws-s3.bucket-name}}");


        from("direct:notify-delivery")
                .bean(OrderBean.class, "generateNotification")
                .log("Sending notification for delivery ${body}")
                .to("telegram:bots?chatId={{telegram.chatId}}");
    }
}
