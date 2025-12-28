package ci553.happyshop.utility;

import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ProductListFormatterTest {

    private boolean containsMoneyValue(String text, String expectedNumber) {
        String regex = expectedNumber.replace(".", "[\\.,]");
        return Pattern.compile(regex).matcher(text).find();
    }

    @Test
    void buildString_includesProducts_and_totalIsCorrect() {
        ArrayList<Product> list = new ArrayList<>();

        Product p1 = new Product("0001", "Apples", "0001.jpg", 1.50, 10);
        p1.setOrderedQuantity(2); // 3
        list.add(p1);

        Product p2 = new Product("0002", "TV", "0002.jpg", 100.00, 5);
        p2.setOrderedQuantity(1); // 100
        list.add(p2);

        String out = ProductListFormatter.buildString(list);

        assertTrue(out.contains("0001"));
        assertTrue(out.contains("Apples"));
        assertTrue(out.contains("0002"));
        assertTrue(out.contains("TV"));
        assertTrue(out.contains("Total"));

        assertTrue(
                containsMoneyValue(out, "103.00"),
                "Total should be 103.00 (comma or dot separator allowed)"
        );
    }

    @Test
    void buildString_emptyList_totalIsZero() {
        String out = ProductListFormatter.buildString(new ArrayList<>());

        assertTrue(out.contains("Total"));

        assertTrue(
                containsMoneyValue(out, "0.00"),
                "Empty trolley total should be 0.00"
        );
    }
}
