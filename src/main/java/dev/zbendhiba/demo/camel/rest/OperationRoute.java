package dev.zbendhiba.demo.camel.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;

import java.util.UUID;

public class OperationRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        //Error Handling
        errorHandler(deadLetterChannel("direct:errorQueue")
                .maximumRedeliveries(3) // Maximum number of redelivery attempts
                .redeliveryDelay(1000) // Delay between redelivery attempts
                .logExhausted(true) // Log if redelivery attempts are exhausted
        );

        // Define the DLC route to handle failed messages
        from("direct:errorQueue")
                .log("ERROR happened Sending notification to Order ${header.orderId}")
                .transform().simple("{'orderId':'${header.orderId}'}")
                .to("kafka:errorTopic")
        ;

        from("direct:add-order")
                .routeId("add-order")
                .bean(OrderBean.class, "generateOrder")
                .log(" New order to persist ${body}")
                .to("jpa:"+CoffeeOrder.class)
                .wireTap("direct:notify")
                .setBody(constant("Thank you for your order"));

        from("direct:orders-api")
                .routeId("orders-api")
                .log("Getting all orders")
                .to("jpa:" + CoffeeOrder.class + "?namedQuery=findAll");

        from("direct:order-api")
                .routeId("order-api")
                .log("Getting order ${header.id}")
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
                .process(exchange ->{
                    CoffeeOrder order = exchange.getMessage().getBody(CoffeeOrder.class);
                    exchange.getMessage().setHeader("orderId", order.getId());
                })
                .bean(OrderBean.class, "generateNotification")
                .log("Sending notification for delivery ${body}")
                .to("telegram:bots?chatId={{telegram.chatId}}");
    }
}
