package app.product.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
 
import java.util.ArrayList;
import java.util.List;
 
public class Product {
 
    private final IntegerProperty id             = new SimpleIntegerProperty();
    private final StringProperty  productNumber  = new SimpleStringProperty();
    private final StringProperty  name           = new SimpleStringProperty();
    private final DoubleProperty  price          = new SimpleDoubleProperty();
    private final ObservableList<String> categories = FXCollections.observableArrayList();
    private final StringProperty  gender         = new SimpleStringProperty();
    private final StringProperty  color          = new SimpleStringProperty();
    private final ObservableList<String> images  = FXCollections.observableArrayList();
    private final StringProperty  description    = new SimpleStringProperty();
    private final IntegerProperty stock          = new SimpleIntegerProperty();
    private final BooleanProperty isActive       = new SimpleBooleanProperty(true);
 
    // Derived display property for TableView
    private final StringProperty  categoryDisplay = new SimpleStringProperty();
    private final StringProperty  statusDisplay   = new SimpleStringProperty();
 
    public Product() {}
 
    public Product(int id, String productNumber, String name, double price,
                   List<String> categories, String gender, String color,
                   List<String> images, String description, int stock, boolean isActive) {
        setId(id);
        setProductNumber(productNumber);
        setName(name);
        setPrice(price);
        this.categories.setAll(categories);
        setGender(gender);
        setColor(color);
        this.images.setAll(images);
        setDescription(description);
        setStock(stock);
        setActive(isActive);
        updateDerivedProperties();
    }
 
    private void updateDerivedProperties() {
        categoryDisplay.set(String.join(", ", categories));
        statusDisplay.set(isActive.get() ? "Active" : "Inactive");
    }
 
    // ── id ──────────────────────────────────────────────────
    public int getId()          { return id.get(); }
    public void setId(int v)    { id.set(v); }
    public IntegerProperty idProperty() { return id; }
 
    // ── productNumber ────────────────────────────────────────
    public String getProductNumber()            { return productNumber.get(); }
    public void   setProductNumber(String v)    { productNumber.set(v); }
    public StringProperty productNumberProperty() { return productNumber; }
 
    // ── name ────────────────────────────────────────────────
    public String getName()           { return name.get(); }
    public void   setName(String v)   { name.set(v); }
    public StringProperty nameProperty() { return name; }
 
    // ── price ───────────────────────────────────────────────
    public double getPrice()          { return price.get(); }
    public void   setPrice(double v)  { price.set(v); }
    public DoubleProperty priceProperty() { return price; }
 
    // ── categories ──────────────────────────────────────────
    public ObservableList<String> getCategories() { return categories; }
    public void setCategories(List<String> cats) {
        categories.setAll(cats);
        categoryDisplay.set(String.join(", ", cats));
    }
    public StringProperty categoryDisplayProperty() { return categoryDisplay; }
    public String getCategoryDisplay()              { return categoryDisplay.get(); }
 
    // ── gender ──────────────────────────────────────────────
    public String getGender()          { return gender.get(); }
    public void   setGender(String v)  { gender.set(v); }
    public StringProperty genderProperty() { return gender; }
 
    // ── color ───────────────────────────────────────────────
    public String getColor()          { return color.get(); }
    public void   setColor(String v)  { color.set(v); }
    public StringProperty colorProperty() { return color; }
 
    // ── images ──────────────────────────────────────────────
    public ObservableList<String> getImages() { return images; }
    public void setImages(List<String> imgs)  { images.setAll(imgs); }
    public String getFirstImage() {
        return images.isEmpty() ? "" : images.get(0);
    }
 
    // ── description ─────────────────────────────────────────
    public String getDescription()          { return description.get(); }
    public void   setDescription(String v)  { description.set(v); }
    public StringProperty descriptionProperty() { return description; }
 
    // ── stock ───────────────────────────────────────────────
    public int  getStock()      { return stock.get(); }
    public void setStock(int v) { stock.set(v); }
    public IntegerProperty stockProperty() { return stock; }
 
    // ── isActive ────────────────────────────────────────────
    public boolean isActive()          { return isActive.get(); }
    public void    setActive(boolean v) {
        isActive.set(v);
        statusDisplay.set(v ? "Active" : "Inactive");
    }
    public BooleanProperty isActiveProperty() { return isActive; }
    public StringProperty  statusDisplayProperty() { return statusDisplay; }
    public String          getStatusDisplay()       { return statusDisplay.get(); }
 
    // ── price display (formatted with £) ────────────────────
    public String getPriceDisplay() {
        return String.format("£%.2f", getPrice());
    }
 
    @Override
    public String toString() {
        return getName() + " (" + getProductNumber() + ")";
    }
}
