package app.product.model;

import javafx.beans.property.*;

public class CartItem {

    private final Product product;
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);
    private final DoubleProperty subtotal  = new SimpleDoubleProperty();

    public CartItem(Product product) {
        this.product = product;
        quantity.set(1);
        updateSubtotal();
        quantity.addListener((obs, o, n) -> updateSubtotal());
    }

    private void updateSubtotal() {
        subtotal.set(product.getPrice() * quantity.get());
    }

    public Product getProduct()           { return product; }

    public int  getQuantity()             { return quantity.get(); }
    public void setQuantity(int v)        { quantity.set(Math.max(0, v)); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getSubtotal()           { return subtotal.get(); }
    public DoubleProperty subtotalProperty() { return subtotal; }

    public String getName()               { return product.getName(); }
    public double getPrice()              { return product.getPrice(); }
    public String getPriceDisplay()       { return String.format("£%.2f", product.getPrice()); }
    public String getSubtotalDisplay()    { return String.format("£%.2f", getSubtotal()); }
}
