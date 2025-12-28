package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.*;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class CustomerModelTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException alreadyStarted) {}
    }

    static class TestCustomerView extends CustomerView {
        String lastImage;
        String lastSearch;
        String lastTrolley;
        String lastReceipt;

        TestCustomerView() {
            tfId = new TextField();
            tfName = new TextField();
        }

        @Override
        public void update(String imageName, String searchResult, String trolley, String receipt) {
            lastImage = imageName;
            lastSearch = searchResult;
            lastTrolley = trolley;
            lastReceipt = receipt;
        }
    }

    static class FakeDatabaseRW implements DatabaseRW {
        private final List<Product> catalogue = new ArrayList<>();
        ArrayList<Product> insufficientToReturn = new ArrayList<>();

        void addProduct(Product p) { catalogue.add(p); }

        @Override
        public Product searchByProductId(String id) throws SQLException {
            return catalogue.stream().filter(p -> p.getProductId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public ArrayList<Product> searchProduct(String keyword) throws SQLException {
            ArrayList<Product> out = new ArrayList<>();
            for (Product p : catalogue) {
                if (p.getProductDescription().toLowerCase().contains(keyword.toLowerCase())) out.add(p);
            }
            return out;
        }

        @Override
        public ArrayList<Product> purchaseStocks(ArrayList<Product> groupedTrolley) throws SQLException {
            return insufficientToReturn;
        }

        @Override
        public void updateProduct(String id, String des, double price, String imageName, int stock) throws SQLException {

        }

        @Override
        public void deleteProduct(String id) throws SQLException {

        }

        @Override
        public void insertNewProduct(String id, String des, double price, String image, int stock) throws SQLException {

        }

        @Override
        public boolean isProIdAvailable(String id) throws SQLException {
            return catalogue.stream().noneMatch(p -> p.getProductId().equals(id));
        }

    }

    static class FakeRemoveProductNotifier extends RemoveProductNotifier {
        String lastMessage;

        @Override
        public void closeNotifierWindow() {
        }

        @Override
        public void showRemovalMsg(String message) {
            lastMessage = message;
        }
    }

    private CustomerModel newModelWith(FakeDatabaseRW db, TestCustomerView view, FakeRemoveProductNotifier notifier) {
        CustomerModel model = new CustomerModel();
        model.databaseRW = db;
        model.cusView = view;

        try {
            var f = CustomerModel.class.getDeclaredField("removeProductNotifier");
            f.setAccessible(true);
            f.set(model, notifier);
        } catch (Exception e) {
            fail("Could not inject removeProductNotifier via reflection: " + e.getMessage());
        }

        return model;
    }

    // multiple merges + cap at 50
    @Test
    void addMultiple_capsAt50_andDoesNotExceed() throws SQLException {
        FakeDatabaseRW db = new FakeDatabaseRW();
        Product apples = new Product("0001", "Apples", "0001.jpg", 1.50, 999);
        db.addProduct(apples);

        TestCustomerView view = new TestCustomerView();
        FakeRemoveProductNotifier notifier = new FakeRemoveProductNotifier();

        CustomerModel model = newModelWith(db, view, notifier);

        view.tfId.setText("0001");
        view.tfName.setText("");
        model.search();

        model.addToTrolley(30);
        model.addToTrolley(30);

        ArrayList<Product> trolley = model.getTrolley();
        assertEquals(1, trolley.size(), "Trolley should contain one merged product line");
        assertEquals(50, trolley.get(0).getOrderedQuantity(), "Quantity must be capped at 50");
        assertNotNull(notifier.lastMessage, "User should be informed when max is exceeded");
        assertTrue(notifier.lastMessage.contains("Maximum allowed"), "Message should explain the max limit");
    }

    // checkout under £5 should not clear trolley
    @Test
    void checkOut_underMinimumPayment_keepsTrolley_andShowsMessage() throws Exception {
        FakeDatabaseRW db = new FakeDatabaseRW();
        Product cheap = new Product("0002", "Gum", "0002.jpg", 1.00, 999);
        db.addProduct(cheap);

        TestCustomerView view = new TestCustomerView();
        FakeRemoveProductNotifier notifier = new FakeRemoveProductNotifier();

        CustomerModel model = newModelWith(db, view, notifier);

        view.tfId.setText("0002");
        view.tfName.setText("");
        model.search();

        model.addToTrolley(2);

        model.checkOut();

        assertFalse(model.getTrolley().isEmpty(), "Trolley should remain (payment too low)");
        assertNotNull(notifier.lastMessage, "Should show under-min payment message");
        assertTrue(notifier.lastMessage.toLowerCase().contains("minimum"), "Message should mention minimum checkout");
    }

    // stock shortage removes affected items only
    @Test
    void checkOut_stockShortage_removesOnlyInsufficientItems() throws IOException, SQLException {
        FakeDatabaseRW db = new FakeDatabaseRW();

        Product apples = new Product("0001", "Apples", "0001.jpg", 3.00, 2);
        Product tv = new Product("0002", "TV", "0002.jpg", 10.00, 100);

        db.addProduct(apples);
        db.addProduct(tv);

        Product insufficient = new Product("0001", "Apples", "0001.jpg", 3.00, 2);
        insufficient.setOrderedQuantity(5); // requested > stock
        db.insufficientToReturn = new ArrayList<>();
        db.insufficientToReturn.add(insufficient);

        TestCustomerView view = new TestCustomerView();
        FakeRemoveProductNotifier notifier = new FakeRemoveProductNotifier();
        CustomerModel model = newModelWith(db, view, notifier);

        view.tfId.setText("0001"); view.tfName.setText(""); model.search();
        model.addToTrolley(5);

        view.tfId.setText("0002"); view.tfName.setText(""); model.search();
        model.addToTrolley(1);

        model.checkOut();

        ArrayList<Product> trolley = model.getTrolley();
        assertEquals(1, trolley.size(), "Only sufficient items should remain");
        assertEquals("0002", trolley.get(0).getProductId(), "TV should remain after removing insufficient apples");

        assertNotNull(notifier.lastMessage, "User should be informed about shortage removal");
        assertTrue(notifier.lastMessage.toLowerCase().contains("stock"), "Message should mention stock shortage");
    }
}
