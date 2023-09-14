package dev.zbendhiba.demo.camel.rest;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@RegisterForReflection
@Named("myBean")
public class OrderBean {

    public CoffeeOrder generateOrder(Coffee coffee){
        return new CoffeeOrder(coffee);
    }

    public String generateNotification(CoffeeOrder order) {
        return "New coffee order for delivery : Order number: " + order.getId();
    }
}
