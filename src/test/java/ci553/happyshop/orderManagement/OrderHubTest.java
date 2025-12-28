package ci553.happyshop.orderManagement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderHubTest {

    @Test
    void singletonReturnsSameInstance() {
        OrderHub hub1 = OrderHub.getOrderHub();
        OrderHub hub2 = OrderHub.getOrderHub();

        assertSame(hub1, hub2, "OrderHub should be a singleton");
    }

    @Test
    void newOrderCreatesOrder() throws Exception {
        OrderHub hub = OrderHub.getOrderHub();

        var trolley = new java.util.ArrayList<ci553.happyshop.catalogue.Product>();
        var order = hub.newOrder(trolley);

        assertNotNull(order, "Order should not be null");
        assertEquals(OrderState.Ordered, order.getState(), "New orders must start as -> Ordered");
    }
}
