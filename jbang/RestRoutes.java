import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;


public class RestRoutes extends RouteBuilder {

    public static final String LIST_ORDERS = "[{\"id\": 1,\"userId\": \"1024\",\"coffeeId\": 469,\"blend_name\": \"Strong Select\"},{ \"id\": 2, \"userId\": \"1025\",  \"coffeeId\": 4351,\"blend_name\": \"Green Pie\"}]";



    public static final String ORDER = "{\"id\": 1,\"userId\": \"1024\",\"coffeeId\": 469,\"blend_name\": \"Strong Select\"}";


    public static final String THANK_YOU = "Thank you for your order";


    @Override
    public void configure() throws Exception {
        /**
         * REST api to add and fetch Coffee Orders
         */
        rest("order-api").description("Coffee Orders REST service")
                .post("/order").description("Add a new coffee Order")
                .to("direct:add-order")
                .get("/order").description("The list of all the coffee orders")
                .to("direct:get-list-orders")
                .get("/order/{id}").description("A Coffee order by id")
                .to("direct:get-order");


        from("direct:add-order")
                .transform().constant(THANK_YOU);

        from("direct:get-list-orders")
                .transform().constant(LIST_ORDERS);

        from("direct:get-order")
                .transform().constant(ORDER);
    }
}