package dev.zbendhiba.demo.camel.rest;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

@ApplicationScoped
public class OperationRoute extends EndpointRouteBuilder {

    private static final String QUERY = "select m  from " + CoffeeOrder.class.getName() + " m  where id =${header.id}";

    @ConfigProperty(name = "aws-s3.bucket-name")
    String bucketName;

    @ConfigProperty(name = "aws2-s3.access-key")
    String accessKey;

    @ConfigProperty(name = "aws2-s3.secret-key")
    String secretKey;

    @ConfigProperty(name = "aws2-s3.region")
    String region;

    @ConfigProperty(name = "telegram.chatId")
    String chatId;

    @ConfigProperty(name = "telegram.authorization-token")
    String authorizationToken;


    @Override
    public void configure() throws Exception {
        //Error Handling
        errorHandler(deadLetterChannel("direct:errorQueue")
                .maximumRedeliveries(3) // Maximum number of redelivery attempts
                .redeliveryDelay(1000) // Delay between redelivery attempts
                .logExhausted(true) // Log if redelivery attempts are exhausted
        );

        // Define the DLC route to handle failed messages
        from(direct("errorQueue"))
                .log("ERROR happened Sending notification to Order ${header.orderId}")
                .transform().simple("{'orderId':'${header.orderId}'}")
                .to(kafka("errorTopic"));

        // Adding one order
        from(direct("add-order"))
                .bean(OrderBean.class, "generateOrder")
                .log(" New order to persist ${body}")
                .to(jpa(CoffeeOrder.class.getName()))
                .wireTap(direct("notify"))
                .setBody(constant("Thank you for your order"));


        // Finding all orders
        from(direct("orders-api"))
                .log("Getting all orders")
                .to(jpa(CoffeeOrder.class.getName())
                        .namedQuery("findAll"));

        // Finding one order
        from(direct("order-api"))
                .log("Getting order ${header.id}")
                .toD(jpa(CoffeeOrder.class.getName())
                        .query(QUERY));

        // Sending file and notifications
        from(direct("notify"))
                .to(direct("s3"))
                .wireTap(direct("notify-delivery"));


        // Create and store a file
        from(direct("s3"))
                .log("sending to S3")
                .marshal().json()
                .setHeader(AWS2S3Constants.KEY, () -> UUID.randomUUID())
                .log(String.format("Sending message with header :: ${header.%s}", AWS2S3Constants.KEY))
                .to(aws2S3(bucketName)
                        .accessKey(accessKey)
                        .secretKey(secretKey)
                        .region(region));


        // Notify for delivery
        from(direct("notify-delivery"))
                .process(exchange ->{
                    CoffeeOrder order = exchange.getMessage().getBody(CoffeeOrder.class);
                    exchange.getMessage().setHeader("orderId", order.getId());
                })
                .bean(OrderBean.class, "generateNotification")
                .log("Sending notification for delivery ${body}")
                .to(telegram("bots")
                        .chatId(chatId)
                        .authorizationToken(authorizationToken));

    }
}