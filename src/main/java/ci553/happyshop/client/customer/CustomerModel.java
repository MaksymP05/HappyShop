package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import ci553.happyshop.catalogue.exception.UnderMinimumPaymentException;
import ci553.happyshop.catalogue.exception.ExcessiveOrderQuantityException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomerModel with Flexible Search (ID or Name).
 */

public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW;

    private Product theProduct = null;
    private ArrayList<Product> trolley = new ArrayList<>();

    private String imageName = "imageHolder.jpg";
    private String displayLaSearchResult = "No Product was searched yet";
    private String displayTaTrolley = "";
    private String displayTaReceipt = "";

    private RemoveProductNotifier removeProductNotifier;

    // exception rules
    private static final double MIN_PAYMENT = 5.00;
    private static final int MAX_QTY_PER_PRODUCT = 50;

    // helper to remove repeated text
    private String formatProductInfo(Product p) {
        return String.format(
                "Product_Id: %s\n%s,\nPrice: £%.2f\n%d units left.",
                p.getProductId(),
                p.getProductDescription(),
                p.getUnitPrice(),
                p.getStockQuantity()
        );
    }

    // unified search (ID or name)
    void search() throws SQLException {

        String idInput = cusView.tfId.getText().trim();
        String nameInput = cusView.tfName.getText().trim();

        if (!idInput.isEmpty() && !nameInput.isEmpty()) {

            if (!idInput.matches("\\d+")) {
                displayLaSearchResult = "!Invalid ID format. ID must be digits only.";
                theProduct = null;
                updateView();
                return;
            }

            Product idProduct = databaseRW.searchByProductId(idInput);

            if (idProduct == null) {
                displayLaSearchResult = "!No product found with ID: " + idInput;
                theProduct = null;
                updateView();
                return;
            }

            ArrayList<Product> nameProducts = databaseRW.searchProduct(nameInput);

            if (nameProducts.isEmpty()) {
                displayLaSearchResult = "!No product found with name: " + nameInput;
                theProduct = null;
                updateView();
                return;
            }

            Product nameProduct = nameProducts.get(0);

            if (!idProduct.getProductId().equals(nameProduct.getProductId())) {

                displayLaSearchResult =
                        "!ID and Name refer to different products.\n" +
                                "ID → " + idProduct.getProductDescription() + "\n" +
                                "Name → " + nameProduct.getProductDescription();

                theProduct = null;
                updateView();
                return;
            }

            theProduct = idProduct;

            // substituted with helper
            displayLaSearchResult = formatProductInfo(theProduct);

            updateView();
            return;
        }

        if (!idInput.isEmpty()) {

            if (!idInput.matches("\\d+")) {
                displayLaSearchResult = "!Invalid ID format. ID must be digits only.";
                theProduct = null;
                updateView();
                return;
            }

            Product p = databaseRW.searchByProductId(idInput);

            if (p == null) {
                displayLaSearchResult = "!No product found with ID: " + idInput;
                theProduct = null;
                updateView();
                return;
            }

            theProduct = p;

            // substituted with helper
            displayLaSearchResult = formatProductInfo(theProduct);

            updateView();
            return;
        }

        if (!nameInput.isEmpty()) {
            ArrayList<Product> nameMatches = databaseRW.searchProduct(nameInput);

            if (nameMatches.isEmpty()) {
                displayLaSearchResult = "!No products found for: " + nameInput;
                theProduct = null;
                updateView();
                return;
            }

            if (nameMatches.size() == 1) {
                theProduct = nameMatches.get(0);

                // substituted with helper
                displayLaSearchResult = formatProductInfo(theProduct);

                updateView();
                return;
            }

            StringBuilder sb = new StringBuilder("Search Results:\n");
            for (Product p : nameMatches) {
                sb.append(String.format(
                        "%s - %s (£%.2f)\n",
                        p.getProductId(),
                        p.getProductDescription(),
                        p.getUnitPrice()
                ));
            }

            displayLaSearchResult = sb.toString();
            theProduct = null;
            updateView();
            return;
        }

        displayLaSearchResult = "Please type Product ID or Name";
        theProduct = null;
        updateView();
    }

    void addToTrolley() {
        addToTrolley(1);
    }

    public void addToTrolley(int quantity) {

        if (theProduct == null) {
            displayLaSearchResult = "Please select a product before adding it to the trolley";
            displayTaReceipt = "";
            updateView();
            return;
        }

        if (quantity <= 0) {
            displayLaSearchResult = "!Quantity must be at least 1";
            displayTaReceipt = "";
            updateView();
            return;
        }

        final int MAX_QTY_PER_PRODUCT = 50;

        // finding if product already exists in trolley
        for (Product p : trolley) {
            if (p.getProductId().equals(theProduct.getProductId())) {

                int currentQty = p.getOrderedQuantity();
                int requestedTotal = currentQty + quantity;

                // detect exceeding 50
                if (requestedTotal > MAX_QTY_PER_PRODUCT) {

                    int allowedToAdd = Math.max(0, MAX_QTY_PER_PRODUCT - currentQty);
                    p.setOrderedQuantity(MAX_QTY_PER_PRODUCT);

                    mergeDuplicates();
                    sortTrolley();
                    displayTaTrolley = ProductListFormatter.buildString(trolley);
                    displayTaReceipt = "";

                    showMessageToCustomer(
                            "Maximum allowed per item is " + MAX_QTY_PER_PRODUCT + ".\n\n" +
                                    theProduct.getProductId() + ", " + theProduct.getProductDescription() + "\n" +
                                    "You tried to add: " + quantity + "\n" +
                                    "Only added: " + allowedToAdd + "\n" +
                                    "Quantity in trolley is now: " + MAX_QTY_PER_PRODUCT
                    );

                    updateView();
                    return;
                }

                // normal add
                p.setOrderedQuantity(requestedTotal);

                mergeDuplicates();
                sortTrolley();
                displayTaTrolley = ProductListFormatter.buildString(trolley);
                displayTaReceipt = "";
                updateView();
                return;
            }
        }

        // product not in trolley yet
        int qtyToAdd = quantity;

        // if first-time add is > 50, cap and show message
        if (qtyToAdd > MAX_QTY_PER_PRODUCT) {
            qtyToAdd = MAX_QTY_PER_PRODUCT;

            showMessageToCustomer(
                    "Maximum allowed per item is " + MAX_QTY_PER_PRODUCT + ".\n\n" +
                            theProduct.getProductId() + ", " + theProduct.getProductDescription() + "\n" +
                            "You tried to add: " + quantity + "\n" +
                            "The number that has been added: " + qtyToAdd
            );
        }

        Product copy = new Product(
                theProduct.getProductId(),
                theProduct.getProductDescription(),
                theProduct.getProductImageName(),
                theProduct.getUnitPrice(),
                theProduct.getStockQuantity()
        );
        copy.setOrderedQuantity(qtyToAdd);

        trolley.add(copy);

        mergeDuplicates();
        sortTrolley();
        displayTaTrolley = ProductListFormatter.buildString(trolley);
        displayTaReceipt = "";
        updateView();
    }

    // helpers for exceptions + notifier
    private double calculateTotal(ArrayList<Product> list) {
        double total = 0.0;
        for (Product p : list) {
            total += p.getUnitPrice() * p.getOrderedQuantity();
        }
        return total;
    }

    private void validateTrolley(ArrayList<Product> list)
            throws UnderMinimumPaymentException, ExcessiveOrderQuantityException {

        StringBuilder changed = new StringBuilder();
        boolean anyChanged = false;

        for (Product p : list) {
            if (p.getOrderedQuantity() > MAX_QTY_PER_PRODUCT) {
                anyChanged = true;
                changed.append("- ")
                        .append(p.getProductId()).append(", ")
                        .append(p.getProductDescription())
                        .append(" reduced from ")
                        .append(p.getOrderedQuantity())
                        .append(" to ").append(MAX_QTY_PER_PRODUCT)
                        .append("\n");
                p.setOrderedQuantity(MAX_QTY_PER_PRODUCT);
            }
        }

        if (anyChanged) {
            mergeDuplicates();
            sortTrolley();
            displayTaTrolley = ProductListFormatter.buildString(trolley);

            throw new ExcessiveOrderQuantityException(
                    "Some items exceeded the maximum allowed quantity (" + MAX_QTY_PER_PRODUCT + ").\n\n" +
                            "Changes made:\n" + changed +
                            "\nPlease review your trolley and checkout again."
            );
        }

        double total = calculateTotal(list);
        if (total < MIN_PAYMENT) {
            throw new UnderMinimumPaymentException(
                    String.format("Minimum checkout is £%.2f.\nYour total is £%.2f.\n\nAdd more items to proceed.",
                            MIN_PAYMENT, total)
            );
        }
    }

    private void showMessageToCustomer(String message) {
        if (removeProductNotifier == null) {
            removeProductNotifier = new RemoveProductNotifier();
        }
        removeProductNotifier.cusView = cusView;
        removeProductNotifier.closeNotifierWindow();
        removeProductNotifier.showRemovalMsg(message);
    }

    // checkout
    void checkOut() throws IOException, SQLException {

        if (trolley.isEmpty()) {
            displayTaTrolley = "Your trolley is empty";
            updateView();
            return;
        }

        try {
            validateTrolley(trolley);

            ArrayList<Product> groupedTrolley = groupProductsById(trolley);
            ArrayList<Product> insufficientProducts = databaseRW.purchaseStocks(groupedTrolley);

            if (insufficientProducts.isEmpty()) {

                if (removeProductNotifier != null) {
                    removeProductNotifier.closeNotifierWindow();
                }

                OrderHub orderHub = OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley);

                trolley.clear();
                displayTaTrolley = "";

                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );

            } else {

                StringBuilder msg = new StringBuilder("Some items exceed available stock:\n\n");

                for (Product p : insufficientProducts) {
                    msg.append("• ")
                            .append(p.getProductId()).append(", ")
                            .append(p.getProductDescription())
                            .append(" (Only ").append(p.getStockQuantity())
                            .append(" available, ")
                            .append(p.getOrderedQuantity())
                            .append(" requested)\n");
                }

                theProduct = null;

                trolley.removeIf(p ->
                        insufficientProducts.stream()
                                .anyMatch(ins -> ins.getProductId().equals(p.getProductId()))
                );

                mergeDuplicates();
                sortTrolley();
                displayTaTrolley = ProductListFormatter.buildString(trolley);

                showMessageToCustomer(msg + "\nThe affected items were removed. Please checkout again.");
            }

        } catch (UnderMinimumPaymentException e) {
            showMessageToCustomer(e.getMessage());

        } catch (ExcessiveOrderQuantityException e) {
            showMessageToCustomer(e.getMessage());
        }

        updateView();
    }

    // grouping products
    private ArrayList<Product> groupProductsById(ArrayList<Product> list) {

        Map<String, Product> grouped = new HashMap<>();

        for (Product p : list) {
            String id = p.getProductId();

            if (grouped.containsKey(id)) {
                grouped.get(id).setOrderedQuantity(
                        grouped.get(id).getOrderedQuantity() + p.getOrderedQuantity()
                );
            } else {
                Product copy = new Product(
                        p.getProductId(),
                        p.getProductDescription(),
                        p.getProductImageName(),
                        p.getUnitPrice(),
                        p.getStockQuantity()
                );
                copy.setOrderedQuantity(p.getOrderedQuantity());
                grouped.put(id, copy);
            }
        }

        return new ArrayList<>(grouped.values());
    }

    // cancel trolley
    void cancel() {
        trolley.clear();
        displayTaTrolley = "";

        if (removeProductNotifier != null) {
            removeProductNotifier.closeNotifierWindow();
        }

        updateView();
    }

    void closeReceipt() {
        displayTaReceipt = "";
    }

    // UI update
    void updateView() {

        if (theProduct != null) {
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder + imageName;
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString();
        } else {
            imageName = "imageHolder.jpg";
        }

        cusView.update(imageName, displayLaSearchResult, displayTaTrolley, displayTaReceipt);
    }

    public ArrayList<Product> getTrolley() {
        return trolley;
    }

    // merging duplicates
    private void mergeDuplicates() {

        Map<String, Product> merged = new HashMap<>();

        for (Product p : trolley) {
            String id = p.getProductId();

            if (merged.containsKey(id)) {
                Product ex = merged.get(id);
                ex.setOrderedQuantity(ex.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                Product copy = new Product(
                        p.getProductId(),
                        p.getProductDescription(),
                        p.getProductImageName(),
                        p.getUnitPrice(),
                        p.getStockQuantity()
                );
                copy.setOrderedQuantity(p.getOrderedQuantity());
                merged.put(id, copy);
            }
        }

        trolley = new ArrayList<>(merged.values());
    }

    // sort trolley by product id
    private void sortTrolley() {
        trolley.sort((a, b) -> a.getProductId().compareTo(b.getProductId()));
    }
}
