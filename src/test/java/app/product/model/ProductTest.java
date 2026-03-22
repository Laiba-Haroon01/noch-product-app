package app.product.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

public class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(
            1,
            "NOCH-00001",
            "Test Jacket",
            59.99,
            Arrays.asList("Outerwear", "Women"),
            "Women",
            "Black",
            Arrays.asList("https://image1.com"),
            "A test jacket",
            10,
            true
        );
    }

    // ── id tests ──────────────────────────────────────────────

    @Test
    void testGetId_returnsCorrectId() {
        assertEquals(1, product.getId());
    }

    @Test
    void testSetId_updatesId() {
        product.setId(99);
        assertEquals(99, product.getId());
    }

    // ── productNumber tests ───────────────────────────────────

    @Test
    void testGetProductNumber_returnsCorrectNumber() {
        assertEquals("NOCH-00001", product.getProductNumber());
    }

    @Test
    void testSetProductNumber_updatesNumber() {
        product.setProductNumber("NOCH-00099");
        assertEquals("NOCH-00099", product.getProductNumber());
    }

    // ── name tests ────────────────────────────────────────────

    @Test
    void testGetName_returnsCorrectName() {
        assertEquals("Test Jacket", product.getName());
    }

    @Test
    void testSetName_updatesName() {
        product.setName("Blue Coat");
        assertEquals("Blue Coat", product.getName());
    }

    // ── price tests ───────────────────────────────────────────

    @Test
    void testGetPrice_returnsCorrectPrice() {
        assertEquals(59.99, product.getPrice());
    }

    @Test
    void testSetPrice_updatesPrice() {
        product.setPrice(79.99);
        assertEquals(79.99, product.getPrice());
    }

    @Test
    void testGetPriceDisplay_returnsFormattedPrice() {
        assertEquals("£59.99", product.getPriceDisplay());
    }

    // ── stock tests ───────────────────────────────────────────

    @Test
    void testGetStock_returnsCorrectStock() {
        assertEquals(10, product.getStock());
    }

    @Test
    void testSetStock_updatesStock() {
        product.setStock(0);
        assertEquals(0, product.getStock());
    }

    // ── isActive tests ────────────────────────────────────────

    @Test
    void testIsActive_returnsTrueByDefault() {
        assertTrue(product.isActive());
    }

    @Test
    void testSetActive_false_returnsInactive() {
        product.setActive(false);
        assertFalse(product.isActive());
    }

    @Test
    void testGetStatusDisplay_activeProduct_returnsActive() {
        assertEquals("Active", product.getStatusDisplay());
    }

    @Test
    void testGetStatusDisplay_inactiveProduct_returnsInactive() {
        product.setActive(false);
        assertEquals("Inactive", product.getStatusDisplay());
    }

    // ── categories tests ──────────────────────────────────────

    @Test
    void testGetCategories_returnsCorrectCategories() {
        assertEquals(2, product.getCategories().size());
        assertTrue(product.getCategories().contains("Outerwear"));
    }

    @Test
    void testGetCategoryDisplay_returnsCommaSeparated() {
        assertEquals("Outerwear, Women", product.getCategoryDisplay());
    }

    // ── images tests ──────────────────────────────────────────

    @Test
    void testGetFirstImage_returnsFirstUrl() {
        assertEquals("https://image1.com", product.getFirstImage());
    }

    @Test
    void testGetFirstImage_emptyImages_returnsEmptyString() {
        product.setImages(List.of());
        assertEquals("", product.getFirstImage());
    }

    // ── toString test ─────────────────────────────────────────

    @Test
    void testToString_returnsNameAndNumber() {
        assertEquals("Test Jacket (NOCH-00001)", product.toString());
    }
}