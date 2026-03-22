package app.product.service;
 
import app.product.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
 
import java.util.List;
 
public class ProductService {
 
    private static ProductService instance;
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ProductRepository repo = ProductRepository.getInstance();
 
    private ProductService() {
        loadFromDatabase();
    }
 
    public static ProductService getInstance() {
        if (instance == null) instance = new ProductService();
        return instance;
    }
 
    // ── Sync from DB into ObservableList ──────────────────────────────────────
 
    private void loadFromDatabase() {
        products.clear();
        List<Product> dbProducts = repo.findAll();
        // Assign sequential in-memory IDs so controllers can reference them
        for (int i = 0; i < dbProducts.size(); i++) {
            dbProducts.get(i).setId(i + 1);
        }
        products.addAll(dbProducts);
        System.out.println("Loaded " + products.size() + " products from DB.");
    }
 
    public void reload() {
        loadFromDatabase();
    }
 
    // ── CRUD — all persist to DB then reload ──────────────────────────────────
 
    public ObservableList<Product> getAll() {
        return products;
    }
 
    public void add(Product p) {
        boolean ok = repo.insert(p);
        if (ok) {
            loadFromDatabase();
        } else {
            System.err.println("Failed to insert product: " + p.getName());
        }
    }
 
    public void update(Product p) {
        boolean ok = repo.update(p);
        if (ok) {
            loadFromDatabase();
        } else {
            System.err.println("Failed to update product: " + p.getName());
        }
    }
 
    public void delete(int id) {
        findByLocalId(id).ifPresent(p -> {
            repo.delete(p.getProductNumber());
            loadFromDatabase();
        });
    }
 
    public void toggleActive(int id) {
        findByLocalId(id).ifPresent(p -> {
            repo.toggleActive(p.getProductNumber());
            loadFromDatabase();
        });
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private java.util.Optional<Product> findByLocalId(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst();
    }
 
    public String generateProductNumber() {
        return repo.generateProductNumber();
    }
 
    public boolean isProductNumberUnique(String number, int excludeId) {
        String excludeProductId = findByLocalId(excludeId)
                .map(Product::getProductNumber)
                .orElse("");
        return repo.isProductNumberUnique(number, excludeProductId);
    }
 
    public boolean isNameUnique(String name, int excludeId) {
        String excludeProductId = findByLocalId(excludeId)
                .map(Product::getProductNumber)
                .orElse("");
        return repo.isNameUnique(name, excludeProductId);
    }
 
    public int getNextId() {
        return products.size() + 1;
    }
}
 
