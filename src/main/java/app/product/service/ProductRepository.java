package app.product.service;
 
import app.product.model.Product;
import app.product.util.DatabaseManager;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
public class ProductRepository {
 
    private static ProductRepository instance;
    private final Connection conn;
 
    private ProductRepository() {
        conn = DatabaseManager.getInstance().getConnection();
    }
 
    public static ProductRepository getInstance() {
        if (instance == null) instance = new ProductRepository();
        return instance;
    }
 
    // ── Find all ──────────────────────────────────────────────────────────────
 
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("findAll error: " + e.getMessage());
        }
        return list;
    }
 
    // ── Insert ────────────────────────────────────────────────────────────────
 
    public boolean insert(Product p) {
        String sql = """
            INSERT INTO products
                (product_id, product_name, description, price,
                 stock_quantity, gender, color, activity, category, image)
            VALUES (?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  p.getProductNumber());
            ps.setString(2,  p.getName());
            ps.setString(3,  p.getDescription() != null ? p.getDescription() : "");
            ps.setDouble(4,  p.getPrice());
            ps.setInt   (5,  p.getStock());
            ps.setString(6,  p.getGender() != null ? p.getGender() : "Unisex");
            ps.setString(7,  p.getColor() != null ? p.getColor() : "");
            ps.setString(8,  p.isActive() ? "Active" : "Inactive");
            ps.setString(9,  String.join(", ", p.getCategories()));
            ps.setString(10, p.getImages().isEmpty() ? "" : p.getImages().get(0));
            ps.executeUpdate();
 
            // Delete old images then insert new ones
            deleteImages(p.getProductNumber());
            insertImages(p);
 
            System.out.println("Product inserted to DB: " + p.getProductNumber());
            return true;
 
        } catch (SQLException e) {
            System.err.println("insert error: " + e.getMessage());
            return false;
        }
    }
 
    // ── Update ────────────────────────────────────────────────────────────────
 
    public boolean update(Product p) {
        String sql = """
            UPDATE products SET
                product_name   = ?,
                description    = ?,
                price          = ?,
                stock_quantity = ?,
                gender         = ?,
                color          = ?,
                activity       = ?,
                category       = ?,
                image          = ?
            WHERE product_id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  p.getName());
            ps.setString(2,  p.getDescription() != null ? p.getDescription() : "");
            ps.setDouble(3,  p.getPrice());
            ps.setInt   (4,  p.getStock());
            ps.setString(5,  p.getGender() != null ? p.getGender() : "Unisex");
            ps.setString(6,  p.getColor() != null ? p.getColor() : "");
            ps.setString(7,  p.isActive() ? "Active" : "Inactive");
            ps.setString(8,  String.join(", ", p.getCategories()));
            ps.setString(9,  p.getImages().isEmpty() ? "" : p.getImages().get(0));
            ps.setString(10, p.getProductNumber());
            ps.executeUpdate();
 
            // Refresh images
            deleteImages(p.getProductNumber());
            insertImages(p);
 
            System.out.println("Product updated in DB: " + p.getProductNumber());
            return true;
 
        } catch (SQLException e) {
            System.err.println("update error: " + e.getMessage());
            return false;
        }
    }
 
    // ── Delete ────────────────────────────────────────────────────────────────
 
    public boolean delete(String productId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM products WHERE product_id = ?")) {
            ps.setString(1, productId);
            ps.executeUpdate();
            System.out.println("Product deleted from DB: " + productId);
            return true;
        } catch (SQLException e) {
            System.err.println("delete error: " + e.getMessage());
            return false;
        }
    }
 
    // ── Toggle active ─────────────────────────────────────────────────────────
 
    public boolean toggleActive(String productId) {
        String sql = """
            UPDATE products SET activity =
                CASE WHEN activity = 'Active' THEN 'Inactive' ELSE 'Active' END
            WHERE product_id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.executeUpdate();
            System.out.println("Toggled active: " + productId);
            return true;
        } catch (SQLException e) {
            System.err.println("toggleActive error: " + e.getMessage());
            return false;
        }
    }
 
    // ── Uniqueness checks ─────────────────────────────────────────────────────
 
    public boolean isProductNumberUnique(String number, String excludeId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM products WHERE LOWER(product_id) = LOWER(?) AND product_id != ?")) {
            ps.setString(1, number);
            ps.setString(2, excludeId != null ? excludeId : "");
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            return true;
        }
    }
 
    public boolean isNameUnique(String name, String excludeId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM products WHERE LOWER(product_name) = LOWER(?) AND product_id != ?")) {
            ps.setString(1, name);
            ps.setString(2, excludeId != null ? excludeId : "");
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            return true;
        }
    }
 
    // ── Generate next product number ──────────────────────────────────────────
 
    public String generateProductNumber() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            int count = rs.getInt(1);
            return String.format("NOCH-%05d", count + 1);
        } catch (SQLException e) {
            return "NOCH-00001";
        }
    }
 
    // ── Image helpers ─────────────────────────────────────────────────────────
 
    private void insertImages(Product p) throws SQLException {
        if (p.getImages() == null || p.getImages().isEmpty()) return;
 
        String sql = """
            INSERT OR IGNORE INTO product_images
                (image_id, product_id, image_url, is_primary, display_order)
            VALUES (?,?,?,?,?)
        """;
        for (int i = 0; i < p.getImages().size() && i < 4; i++) {
            String url = p.getImages().get(i);
            if (url == null || url.isBlank()) continue;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getProductNumber() + "-IMG-" + (i + 1));
                ps.setString(2, p.getProductNumber());
                ps.setString(3, url.trim());
                ps.setInt   (4, i == 0 ? 1 : 0);
                ps.setInt   (5, i + 1);
                ps.executeUpdate();
            }
        }
    }
 
    private void deleteImages(String productId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM product_images WHERE product_id = ?")) {
            ps.setString(1, productId);
            ps.executeUpdate();
        }
    }
 
    // ── Load images for a product ─────────────────────────────────────────────
 
    public List<String> loadImages(String productId) {
        List<String> imgs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT image_url FROM product_images WHERE product_id = ? ORDER BY display_order")) {
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) imgs.add(rs.getString("image_url"));
        } catch (SQLException e) {
            System.err.println("loadImages error: " + e.getMessage());
        }
        return imgs;
    }
 
    // ── Map ResultSet row to Product ──────────────────────────────────────────
 
    private Product mapRow(ResultSet rs) throws SQLException {
        String productId = rs.getString("product_id");
        String cats      = rs.getString("category");
 
        List<String> catList = (cats != null && !cats.isBlank())
                ? Arrays.asList(cats.split(",\\s*"))
                : new ArrayList<>();
 
        // Load images from product_images table
        List<String> images = loadImages(productId);
 
        // Fallback to main image column if no images in product_images
        if (images.isEmpty()) {
            String mainImage = rs.getString("image");
            if (mainImage != null && !mainImage.isBlank()) {
                images.add(mainImage);
            }
        }
 
        return new Product(
                0,
                productId,
                rs.getString("product_name"),
                rs.getDouble("price"),
                catList,
                rs.getString("gender"),
                rs.getString("color"),
                images,
                rs.getString("description"),
                rs.getInt("stock_quantity"),
                "Active".equals(rs.getString("activity"))
        );
    }
}