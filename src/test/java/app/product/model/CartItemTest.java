package app.product.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class CartItemTest {

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product = new Product(
            1,
            "NOCH-00001",
            "Test Jacket",
            59.99,
            Arrays.asList("Outerwear"),
            "Women",
            "Black",
            Arrays.asList("https://image1.com"),
            "A test jacket",
            10,
            true
        );
        cartItem = new CartItem(product);
    }

    // ── quantity tests ────────────────────────────────────────

    @Test
    void testDefaultQuantity_isOne() {
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    void testSetQuantity_updatesCorrectly() {
        cartItem.setQuantity(3);
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void testSetQuantity_negativeValue_setsToZero() {
        cartItem.setQuantity(-5);
        assertEquals(0, cartItem.getQuantity());
    }

    @Test
    void testSetQuantity_zero_allowed() {
        cartItem.setQuantity(0);
        assertEquals(0, cartItem.getQuantity());
    }

    // ── subtotal tests ────────────────────────────────────────

    @Test
    void testDefaultSubtotal_equalsPriceTimesOne() {
        assertEquals(59.99, cartItem.getSubtotal(), 0.001);
    }

    @Test
    void testSubtotal_updatesWhenQuantityChanges() {
        cartItem.setQuantity(3);
        assertEquals(179.97, cartItem.getSubtotal(), 0.001);
    }

    @Test
    void testSubtotal_zeroQuantity_returnsZero() {
        cartItem.setQuantity(0);
        assertEquals(0.0, cartItem.getSubtotal(), 0.001);
    }

    // ── display tests ─────────────────────────────────────────

    @Test
    void testGetPriceDisplay_returnsFormattedPrice() {
        assertEquals("£59.99", cartItem.getPriceDisplay());
    }

    @Test
    void testGetSubtotalDisplay_returnsFormattedSubtotal() {
        cartItem.setQuantity(2);
        assertEquals("£119.98", cartItem.getSubtotalDisplay());
    }

    // ── product reference tests ───────────────────────────────

    @Test
    void testGetName_returnsProductName() {
        assertEquals("Test Jacket", cartItem.getName());
    }

    @Test
    void testGetPrice_returnsProductPrice() {
        assertEquals(59.99, cartItem.getPrice(), 0.001);
    }

    @Test
    void testGetProduct_returnsCorrectProduct() {
        assertEquals(product, cartItem.getProduct());
    }
}