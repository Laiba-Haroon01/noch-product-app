package app.product.controller;

import app.product.model.CartItem;
import app.product.model.Product;
import app.product.service.AuthService;
import app.product.service.ProductService;
import app.product.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class CustomerShopController implements Initializable {

    @FXML private Button      favouritesBtn;
    @FXML private Button      checkoutBtn;
    @FXML private Label       productCountLabel;
    @FXML private FlowPane    productGrid;
    @FXML private Button      filterBtn;
    @FXML private Label       noProductsLabel;
    @FXML private StackPane   heroPane;
    @FXML private Region      heroOverlay;
    @FXML private BorderPane  rootPane;

    private final Set<String>              selectedCategories = new HashSet<>();
    private       String                   selectedGender     = "all";
    private       String                   sortBy             = "default";
    private       String                   availabilityFilter = "all";
    private       double                   filterMinPrice     = 0;
    private       double                   filterMaxPrice     = 999999;

    private final Set<Integer>             favourites  = new HashSet<>();
    private final ObservableList<CartItem> cart        = FXCollections.observableArrayList();
    private final Map<Integer, Button>     heartBtnMap = new HashMap<>();

    private final ProductService productService = ProductService.getInstance();
    private static final double  CARD_WIDTH     = 300;
    private static final double  PANEL_WIDTH    = 420;

    private VBox   sidePanel     = null;
    private String sidePanelMode = "none";

    private final String PRIMARY_BTN =
        "-fx-background-color:#111111;-fx-text-fill:white;" +
        "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
        "-fx-letter-spacing:1px;-fx-cursor:hand;" +
        "-fx-background-radius:4;-fx-pref-height:46px;";
    private final String PRIMARY_BTN_HOV =
        "-fx-background-color:#333333;-fx-text-fill:white;" +
        "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
        "-fx-letter-spacing:1px;-fx-cursor:hand;" +
        "-fx-background-radius:4;-fx-pref-height:46px;";
    private final String SECONDARY_BTN =
        "-fx-background-color:white;-fx-text-fill:#111111;" +
        "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
        "-fx-letter-spacing:1px;-fx-cursor:hand;" +
        "-fx-border-color:#111111;-fx-border-radius:4;-fx-background-radius:4;" +
        "-fx-pref-height:46px;";
    private final String SECONDARY_BTN_HOV =
        "-fx-background-color:#f5f5f5;-fx-text-fill:#111111;" +
        "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
        "-fx-letter-spacing:1px;-fx-cursor:hand;" +
        "-fx-border-color:#111111;-fx-border-radius:4;-fx-background-radius:4;" +
        "-fx-pref-height:46px;";
    private final String DISABLED_BTN =
        "-fx-background-color:#eeeeee;-fx-text-fill:#aaaaaa;" +
        "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
        "-fx-letter-spacing:1px;-fx-cursor:default;" +
        "-fx-border-color:#eeeeee;-fx-border-radius:4;-fx-background-radius:4;" +
        "-fx-pref-height:46px;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadHeroBackground();
        refreshGrid();
        updateHeaderBadges();
        updateFilterBtnText();
    }

    private void loadHeroBackground() {
        String heroUrl = "https://images.unsplash.com/photo-1759229874914-c1ffdb3ebd0c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtaW5pbWFsaXN0JTIwZmFzaGlvbiUyMG5ldXRyYWx8ZW58MXx8fHwxNzYzMDkyNjYwfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral";
        new Thread(() -> {
            try {
                Image img = new Image(heroUrl, 1400, 600, false, true, true);
                Platform.runLater(() -> {
                    BackgroundImage bg = new BackgroundImage(
                        img,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, true, true));
                    heroPane.setBackground(new Background(bg));
                    heroOverlay.setStyle("-fx-background-color:rgba(0,0,0,0.18);");
                });
            } catch (Exception ignored) {}
        }).start();
    }

    @FXML private void onFavourites() { toggleSidePanel("favourites"); }
    @FXML private void onCheckout()   { toggleSidePanel("basket"); }
    @FXML private void onFilter()     { showFilterDialog(); }

    @FXML
    private void onLogout() {
        AuthService.getInstance().logout();
        SceneManager.getInstance().switchTo(SceneManager.LOGIN);
    }

    private void toggleSidePanel(String mode) {
        if (sidePanelMode.equals(mode) && sidePanel != null) {
            rootPane.setRight(null);
            sidePanel = null;
            sidePanelMode = "none";
        } else {
            sidePanelMode = mode;
            buildSidePanel();
        }
    }

    private void buildSidePanel() {
        sidePanel = new VBox(0);
        sidePanel.setPrefWidth(PANEL_WIDTH);
        sidePanel.setMinWidth(PANEL_WIDTH);
        sidePanel.setMaxWidth(PANEL_WIDTH);
        sidePanel.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#eeeeee transparent transparent transparent;" +
            "-fx-border-width:0 0 0 1;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 20, 20, 24));
        header.setStyle(
            "-fx-border-color:transparent transparent #eeeeee transparent;" +
            "-fx-border-width:0 0 1 0;");

        String panelTitle = sidePanelMode.equals("basket") ? "BASKET" : "FAVOURITES";
        Label titleLbl = new Label(panelTitle);
        titleLbl.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;" +
            "-fx-letter-spacing:2px;-fx-text-fill:#111111;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#aaaaaa;" +
            "-fx-font-size:14px;-fx-cursor:hand;-fx-border-color:transparent;");
        closeBtn.setOnAction(e -> {
            rootPane.setRight(null);
            sidePanel = null;
            sidePanelMode = "none";
        });

        header.getChildren().addAll(titleLbl, headerSpacer, closeBtn);
        sidePanel.getChildren().add(header);

        VBox content = new VBox(0);
        content.setPadding(new Insets(0, 20, 20, 24));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:white;-fx-border-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        if (sidePanelMode.equals("basket")) {
            buildBasketContent(content);
        } else {
            buildFavouritesContent(content);
        }

        sidePanel.getChildren().add(scroll);
        rootPane.setRight(sidePanel);
    }

    private void buildBasketContent(VBox content) {
        content.getChildren().clear();

        if (cart.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60, 0, 0, 0));
            Label icon = new Label("🛍");
            icon.setStyle("-fx-font-size:40px;");
            Label msg = new Label("Your basket is empty");
            msg.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#888;");
            empty.getChildren().addAll(icon, msg);
            content.getChildren().add(empty);
            return;
        }

        for (CartItem item : new ArrayList<>(cart)) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(16, 0, 16, 0));
            row.setStyle(
                "-fx-border-color:transparent transparent #f5f5f5 transparent;" +
                "-fx-border-width:0 0 1 0;");

            ImageView iv = new ImageView();
            iv.setFitWidth(72);
            iv.setFitHeight(90);
            iv.setPreserveRatio(false);
            loadImageAsync(iv, item.getProduct().getFirstImage(), 72, 90);

            VBox info = new VBox(4);
            Label n = new Label(item.getName());
            n.setStyle("-fx-font-family:'Georgia';-fx-font-size:13px;-fx-text-fill:#111111;");
            n.setWrapText(true);
            n.setMaxWidth(200);

            Label pr = new Label(item.getPriceDisplay() + " each");
            pr.setStyle("-fx-font-family:'Arial';-fx-font-size:12px;-fx-text-fill:#888;");

            Label sub = new Label("Subtotal: " + item.getSubtotalDisplay());
            sub.setStyle("-fx-font-family:'Arial';-fx-font-size:12px;-fx-text-fill:#555;");

            HBox qtyRow = new HBox(8);
            qtyRow.setAlignment(Pos.CENTER_LEFT);
            qtyRow.setPadding(new Insets(4, 0, 0, 0));

            Button minus = new Button("−");
            minus.setStyle(
                "-fx-background-color:white;-fx-border-color:#dddddd;" +
                "-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-min-width:28px;-fx-min-height:28px;-fx-cursor:hand;-fx-font-size:14px;");

            Label qty = new Label(String.valueOf(item.getQuantity()));
            qty.setStyle(
                "-fx-min-width:20;-fx-alignment:center;" +
                "-fx-font-family:'Arial';-fx-font-size:13px;");

            Button plus = new Button("+");
            plus.setStyle(
                "-fx-background-color:white;-fx-border-color:#dddddd;" +
                "-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-min-width:28px;-fx-min-height:28px;-fx-cursor:hand;-fx-font-size:14px;");

            minus.setOnAction(e -> {
                item.setQuantity(item.getQuantity() - 1);
                if (item.getQuantity() <= 0) cart.remove(item);
                updateHeaderBadges();
                buildSidePanel();
            });
            plus.setOnAction(e -> {
                item.setQuantity(item.getQuantity() + 1);
                updateHeaderBadges();
                buildSidePanel();
            });

            qtyRow.getChildren().addAll(minus, qty, plus);
            info.getChildren().addAll(n, pr, sub, qtyRow);
            HBox.setHgrow(info, Priority.ALWAYS);

            Button remove = new Button("✕");
            remove.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#aaaaaa;" +
                "-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:transparent;");
            remove.setOnAction(e -> {
                cart.remove(item);
                updateHeaderBadges();
                buildSidePanel();
            });

            row.getChildren().addAll(iv, info, remove);
            content.getChildren().add(row);
        }

        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();

        VBox footer = new VBox(12);
        footer.setPadding(new Insets(20, 0, 0, 0));

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLbl = new Label("Total");
        totalLbl.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#888;");
        Region ts = new Region();
        HBox.setHgrow(ts, Priority.ALWAYS);
        Label totalAmt = new Label(String.format("£%.2f", total));
        totalAmt.setStyle(
            "-fx-font-family:'Georgia';-fx-font-size:18px;" +
            "-fx-font-weight:bold;-fx-text-fill:#111111;");
        totalRow.getChildren().addAll(totalLbl, ts, totalAmt);

        Button proceedBtn = new Button("PROCEED TO CHECKOUT");
        proceedBtn.setMaxWidth(Double.MAX_VALUE);
        proceedBtn.setStyle(PRIMARY_BTN);
        proceedBtn.setOnMouseEntered(e -> proceedBtn.setStyle(PRIMARY_BTN_HOV));
        proceedBtn.setOnMouseExited(e  -> proceedBtn.setStyle(PRIMARY_BTN));
        proceedBtn.setOnAction(e -> {
            showAlert("Order Confirmed",
                "Thank you for your order!\nTotal: " + String.format("£%.2f", total));
            cart.clear();
            updateHeaderBadges();
            buildSidePanel();
        });

        footer.getChildren().addAll(totalRow, proceedBtn);
        content.getChildren().add(footer);
    }

    private void buildFavouritesContent(VBox content) {
        content.getChildren().clear();

        List<Product> favList = productService.getAll().stream()
                .filter(p -> favourites.contains(p.getId())).toList();

        if (favList.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60, 0, 0, 0));
            Label icon = new Label("♡");
            icon.setStyle("-fx-font-size:40px;-fx-text-fill:#cccccc;");
            Label msg = new Label("No favourites yet");
            msg.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#888;");
            empty.getChildren().addAll(icon, msg);
            content.getChildren().add(empty);
            return;
        }

        for (Product p : favList) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(16, 0, 16, 0));
            row.setStyle(
                "-fx-border-color:transparent transparent #f5f5f5 transparent;" +
                "-fx-border-width:0 0 1 0;");

            ImageView iv = new ImageView();
            iv.setFitWidth(72);
            iv.setFitHeight(90);
            iv.setPreserveRatio(false);
            loadImageAsync(iv, p.getFirstImage(), 72, 90);

            VBox info = new VBox(4);
            Label n = new Label(p.getName());
            n.setStyle("-fx-font-family:'Georgia';-fx-font-size:13px;-fx-text-fill:#111111;");
            n.setWrapText(true);
            n.setMaxWidth(200);

            Label pr = new Label(p.getPriceDisplay());
            pr.setStyle("-fx-font-family:'Arial';-fx-font-size:12px;-fx-text-fill:#888;");

            Button addBtn = new Button(p.getStock() == 0 ? "Out of Stock" : "Add to Basket");
            String addStyle = p.getStock() == 0
                ? "-fx-background-color:#eeeeee;-fx-text-fill:#aaaaaa;" +
                  "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
                  "-fx-cursor:default;-fx-background-radius:4;" +
                  "-fx-pref-height:30px;-fx-padding:0 10;"
                : "-fx-background-color:#111111;-fx-text-fill:white;" +
                  "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
                  "-fx-cursor:hand;-fx-background-radius:4;" +
                  "-fx-pref-height:30px;-fx-padding:0 10;";
            addBtn.setStyle(addStyle);
            if (p.getStock() > 0) {
                addBtn.setOnAction(e -> {
                    addToCart(p);
                    updateHeaderBadges();
                });
            }

            info.getChildren().addAll(n, pr, addBtn);
            HBox.setHgrow(info, Priority.ALWAYS);

            Button remove = new Button("✕");
            remove.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#aaaaaa;" +
                "-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:transparent;");
            remove.setOnAction(e -> {
                favourites.remove(p.getId());
                updateHeaderBadges();
                updateHeartBtn(p.getId());
                buildSidePanel();
            });

            row.getChildren().addAll(iv, info, remove);
            content.getChildren().add(row);
        }
    }

    private void updateHeartBtn(int productId) {
        Button btn = heartBtnMap.get(productId);
        if (btn == null) return;
        boolean isFav = favourites.contains(productId);
        btn.setText(isFav ? "♥" : "♡");
        btn.setStyle(
            "-fx-background-color:white;" +
            "-fx-text-fill:" + (isFav ? "#111111" : "#bbbbbb") + ";" +
            "-fx-font-size:15px;-fx-background-radius:50%;" +
            "-fx-min-width:34px;-fx-min-height:34px;" +
            "-fx-max-width:34px;-fx-max-height:34px;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),4,0,0,1);");
    }

    private void refreshGrid() {
        heartBtnMap.clear();

        List<Product> active = productService.getAll().stream()
                .filter(Product::isActive)
                .filter(this::matchesFilters)
                .sorted(Comparator.comparingInt(Product::getId))
                .toList();

        List<Product> sorted = applySort(active);

        productCountLabel.setText(
            sorted.size() + (sorted.size() == 1 ? " product" : " products"));
        noProductsLabel.setVisible(sorted.isEmpty());
        noProductsLabel.setManaged(sorted.isEmpty());
        productGrid.getChildren().clear();

        for (Product p : sorted) {
            productGrid.getChildren().add(buildProductCard(p));
        }
    }

    private boolean matchesFilters(Product p) {
        boolean catMatch   = selectedCategories.isEmpty()
                || p.getCategories().stream().anyMatch(selectedCategories::contains);
        boolean genMatch   = "all".equals(selectedGender)
                || p.getGender().equalsIgnoreCase(selectedGender);
        boolean priceMatch = p.getPrice() >= filterMinPrice && p.getPrice() <= filterMaxPrice;
        boolean availMatch = switch (availabilityFilter) {
            case "in-stock"     -> p.getStock() > 0;
            case "out-of-stock" -> p.getStock() == 0;
            default             -> true;
        };
        return catMatch && genMatch && priceMatch && availMatch;
    }

    private List<Product> applySort(List<Product> list) {
        return switch (sortBy) {
            case "price-asc"  -> list.stream()
                    .sorted(Comparator.comparingDouble(Product::getPrice)).toList();
            case "price-desc" -> list.stream()
                    .sorted(Comparator.comparingDouble(Product::getPrice).reversed()).toList();
            default           -> list;
        };
    }

    private VBox buildProductCard(Product product) {
        VBox card = new VBox(0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setStyle("-fx-cursor:hand;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(380);
        imageContainer.setPrefWidth(CARD_WIDTH);
        imageContainer.setMinWidth(CARD_WIDTH);
        imageContainer.setMaxWidth(CARD_WIDTH);
        imageContainer.setStyle("-fx-background-color:#f2f2f2;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(380);
        imageView.setPreserveRatio(false);
        loadImageAsync(imageView, product.getFirstImage(), CARD_WIDTH, 380);

        boolean isFav = favourites.contains(product.getId());
        Button heartBtn = new Button(isFav ? "♥" : "♡");
        heartBtn.setStyle(
            "-fx-background-color:white;" +
            "-fx-text-fill:" + (isFav ? "#111111" : "#bbbbbb") + ";" +
            "-fx-font-size:15px;-fx-background-radius:50%;" +
            "-fx-min-width:34px;-fx-min-height:34px;" +
            "-fx-max-width:34px;-fx-max-height:34px;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),4,0,0,1);");
        StackPane.setAlignment(heartBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(heartBtn, new Insets(10));
        heartBtnMap.put(product.getId(), heartBtn);

        heartBtn.setOnAction(e -> {
            toggleFavourite(product.getId());
            updateHeaderBadges();
            updateHeartBtn(product.getId());
            if (sidePanelMode.equals("favourites")) buildSidePanel();
        });

        if (product.getStock() == 0) {
            Label outBadge = new Label("OUT OF STOCK");
            outBadge.setStyle(
                "-fx-background-color:rgba(0,0,0,0.6);-fx-text-fill:white;" +
                "-fx-font-family:'Arial';-fx-font-size:10px;-fx-font-weight:bold;" +
                "-fx-padding:4 10;-fx-background-radius:4;");
            StackPane.setAlignment(outBadge, Pos.BOTTOM_LEFT);
            StackPane.setMargin(outBadge, new Insets(10));
            imageContainer.getChildren().addAll(imageView, heartBtn, outBadge);
        } else {
            imageContainer.getChildren().addAll(imageView, heartBtn);
        }

        imageContainer.setOnMouseClicked(e -> showProductDetail(product));

        VBox info = new VBox(5);
        info.setPadding(new Insets(12, 2, 8, 2));

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle(
            "-fx-font-family:'Georgia','Times New Roman',serif;" +
            "-fx-font-size:14px;-fx-text-fill:#111111;");
        nameLabel.setWrapText(true);
        nameLabel.setPrefWidth(CARD_WIDTH - 4);
        nameLabel.setOnMouseClicked(e -> showProductDetail(product));

        Label catLabel = new Label(product.getCategoryDisplay());
        catLabel.setStyle(
            "-fx-font-family:'Arial',sans-serif;" +
            "-fx-font-size:11px;-fx-text-fill:#999999;");

        HBox priceRow = new HBox();
        priceRow.setAlignment(Pos.CENTER_LEFT);
        priceRow.setPadding(new Insets(4, 0, 0, 0));

        Label priceLabel = new Label(product.getPriceDisplay());
        priceLabel.setStyle(
            "-fx-font-family:'Georgia','Times New Roman',serif;" +
            "-fx-font-size:14px;-fx-text-fill:#111111;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean oos = product.getStock() == 0;
        String buyDef = oos
            ? "-fx-background-color:#eeeeee;-fx-text-fill:#aaaaaa;" +
              "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
              "-fx-border-color:#eeeeee;-fx-border-radius:4;-fx-background-radius:4;" +
              "-fx-padding:4 12;-fx-cursor:default;"
            : "-fx-background-color:transparent;-fx-text-fill:#111111;" +
              "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
              "-fx-cursor:hand;-fx-border-color:#cccccc;" +
              "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:4 12;";
        String buyHov =
            "-fx-background-color:#111111;-fx-text-fill:white;" +
            "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-cursor:hand;-fx-border-color:#111111;" +
            "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:4 12;";

        Button buyBtn = new Button(oos ? "Sold Out" : "Buy");
        buyBtn.setStyle(buyDef);
        if (!oos) {
            buyBtn.setOnMouseEntered(e -> buyBtn.setStyle(buyHov));
            buyBtn.setOnMouseExited(e  -> buyBtn.setStyle(buyDef));
        }
        buyBtn.setOnAction(e -> {
            if (product.getStock() == 0) {
                showOutOfStockAlert(product.getName());
            } else {
                addToCart(product);
                updateHeaderBadges();
                if (sidePanelMode.equals("basket")) buildSidePanel();
            }
        });

        priceRow.getChildren().addAll(priceLabel, spacer, buyBtn);
        info.getChildren().addAll(nameLabel, catLabel, priceRow);
        card.getChildren().addAll(imageContainer, info);
        return card;
    }

    private void loadImageAsync(ImageView iv, String url, double w, double h) {
        if (url == null || url.isBlank()) return;
        new Thread(() -> {
            try {
                Image img = new Image(url, w, h, false, true, true);
                Platform.runLater(() -> iv.setImage(img));
            } catch (Exception ignored) {}
        }).start();
    }

    private void showOutOfStockAlert(String productName) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Out of Stock");

        VBox root = new VBox(16);
        root.setPadding(new Insets(36));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:white;");
        root.setPrefWidth(360);

        Label icon = new Label("😔");
        icon.setStyle("-fx-font-size:44px;");

        Label head = new Label("Sorry, we're out of stock");
        head.setStyle(
            "-fx-font-family:'Georgia';-fx-font-size:18px;" +
            "-fx-font-weight:bold;-fx-text-fill:#111111;-fx-text-alignment:center;");
        head.setWrapText(true);
        head.setAlignment(Pos.CENTER);

        Label msg = new Label(
            "\"" + productName + "\" is currently unavailable.\n" +
            "Check back soon or explore our other products.");
        msg.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:13px;" +
            "-fx-text-fill:#666666;-fx-text-alignment:center;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        Button closeBtn = new Button("CONTINUE SHOPPING");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle(PRIMARY_BTN);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(PRIMARY_BTN_HOV));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle(PRIMARY_BTN));
        closeBtn.setOnAction(e -> popup.close());

        root.getChildren().addAll(icon, head, msg, closeBtn);
        popup.setScene(new Scene(root, 360, 280));
        popup.getScene().getStylesheets().add(
            getClass().getResource("/styles/main.css").toExternalForm());
        popup.showAndWait();
    }

    private void showProductDetail(Product p) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(p.getName());

        HBox content = new HBox(32);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:white;");
        content.setPrefWidth(780);

        // ── Image carousel (left side) ──────────────────────
        List<String> allImages = new ArrayList<>(p.getImages());
        final int[] currentIndex = {0};

        ImageView iv = new ImageView();
        iv.setFitWidth(340);
        iv.setFitHeight(400);
        iv.setPreserveRatio(false);
        if (!allImages.isEmpty()) {
            loadImageAsync(iv, allImages.get(0), 340, 400);
        }

        Button prevBtn = new Button("‹");
        prevBtn.setStyle(
            "-fx-background-color:white;-fx-text-fill:#111111;" +
            "-fx-font-size:18px;-fx-cursor:hand;-fx-background-radius:50%;" +
            "-fx-min-width:36px;-fx-min-height:36px;" +
            "-fx-max-width:36px;-fx-max-height:36px;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),6,0,0,1);");

        Button nextBtn = new Button("›");
        nextBtn.setStyle(
            "-fx-background-color:white;-fx-text-fill:#111111;" +
            "-fx-font-size:18px;-fx-cursor:hand;-fx-background-radius:50%;" +
            "-fx-min-width:36px;-fx-min-height:36px;" +
            "-fx-max-width:36px;-fx-max-height:36px;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),6,0,0,1);");

        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER);
        dots.setPadding(new Insets(8, 0, 0, 0));

        if (allImages.size() > 1) {
            for (int i = 0; i < allImages.size(); i++) {
                Label dot = new Label("●");
                dot.setStyle(
                    "-fx-font-size:8px;-fx-cursor:hand;" +
                    "-fx-text-fill:" + (i == 0 ? "#111111" : "#cccccc") + ";");
                dots.getChildren().add(dot);
            }

            prevBtn.setOnAction(e -> {
                currentIndex[0] = (currentIndex[0] - 1 + allImages.size()) % allImages.size();
                loadImageAsync(iv, allImages.get(currentIndex[0]), 340, 400);
                updateDots(dots, currentIndex[0]);
            });

            nextBtn.setOnAction(e -> {
                currentIndex[0] = (currentIndex[0] + 1) % allImages.size();
                loadImageAsync(iv, allImages.get(currentIndex[0]), 340, 400);
                updateDots(dots, currentIndex[0]);
            });

            for (int i = 0; i < dots.getChildren().size(); i++) {
                final int idx = i;
                dots.getChildren().get(i).setOnMouseClicked(e -> {
                    currentIndex[0] = idx;
                    loadImageAsync(iv, allImages.get(idx), 340, 400);
                    updateDots(dots, idx);
                });
            }
        }

        StackPane imageStack = new StackPane();
        imageStack.setPrefWidth(340);
        imageStack.setPrefHeight(400);
        imageStack.getChildren().add(iv);

        if (allImages.size() > 1) {
            HBox arrowRow = new HBox();
            arrowRow.setAlignment(Pos.CENTER);
            arrowRow.setPadding(new Insets(0, 8, 0, 8));
            Region arrowSpacer = new Region();
            HBox.setHgrow(arrowSpacer, Priority.ALWAYS);
            arrowRow.getChildren().addAll(prevBtn, arrowSpacer, nextBtn);
            StackPane.setAlignment(arrowRow, Pos.CENTER);
            imageStack.getChildren().add(arrowRow);
        }

        VBox imageSection = new VBox(0);
        imageSection.setPrefWidth(340);
        imageSection.getChildren().add(imageStack);
        if (allImages.size() > 1) {
            imageSection.getChildren().add(dots);
        }

        // ── Details (right side) ────────────────────────────
        VBox details = new VBox(0);
        details.setPrefWidth(370);

        Label name = new Label(p.getName());
        name.setStyle(
            "-fx-font-family:'Georgia','Times New Roman',serif;" +
            "-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#111111;");
        name.setWrapText(true);
        VBox.setMargin(name, new Insets(0, 0, 6, 0));

        Label cat = new Label(p.getCategoryDisplay().toUpperCase());
        cat.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:11px;" +
            "-fx-text-fill:#999999;-fx-letter-spacing:2px;");
        VBox.setMargin(cat, new Insets(0, 0, 16, 0));

        Label price = new Label(p.getPriceDisplay());
        price.setStyle(
            "-fx-font-family:'Georgia','Times New Roman',serif;" +
            "-fx-font-size:20px;-fx-text-fill:#111111;");
        VBox.setMargin(price, new Insets(0, 0, 20, 0));

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(0, 0, 20, 0));

        Label descHead = new Label("Description");
        descHead.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:12px;" +
            "-fx-font-weight:bold;-fx-text-fill:#111111;-fx-letter-spacing:1px;");
        VBox.setMargin(descHead, new Insets(0, 0, 8, 0));

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.setWrapText(true);
        desc.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:14px;-fx-text-fill:#555555;");
        VBox.setMargin(desc, new Insets(0, 0, 18, 0));

        Label colorLabel = new Label("Colour: " + p.getColor());
        colorLabel.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#888888;");
        VBox.setMargin(colorLabel, new Insets(0, 0, 6, 0));

        String stockText = p.getStock() > 0 ? p.getStock() + " in stock" : "Out of stock";
        Label stock = new Label(stockText);
        stock.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:13px;" +
            "-fx-text-fill:" + (p.getStock() > 0 ? "#166534" : "#991b1b") + ";");
        VBox.setMargin(stock, new Insets(0, 0, 24, 0));

        boolean oos = p.getStock() == 0;

        Button addBtn = new Button(oos ? "OUT OF STOCK" : "ADD TO BASKET");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(oos ? DISABLED_BTN : PRIMARY_BTN);
        if (!oos) {
            addBtn.setOnMouseEntered(e -> addBtn.setStyle(PRIMARY_BTN_HOV));
            addBtn.setOnMouseExited(e  -> addBtn.setStyle(PRIMARY_BTN));
            addBtn.setOnAction(e -> {
                addToCart(p);
                updateHeaderBadges();
                if (sidePanelMode.equals("basket")) buildSidePanel();
            });
        } else {
            addBtn.setOnAction(e -> showOutOfStockAlert(p.getName()));
        }
        VBox.setMargin(addBtn, new Insets(0, 0, 10, 0));

        boolean isFav = favourites.contains(p.getId());
        Button favBtn = new Button(isFav ? "♥  REMOVE FROM FAVOURITES" : "♡  ADD TO FAVOURITES");
        favBtn.setMaxWidth(Double.MAX_VALUE);
        favBtn.setStyle(SECONDARY_BTN);
        favBtn.setOnMouseEntered(e -> favBtn.setStyle(SECONDARY_BTN_HOV));
        favBtn.setOnMouseExited(e  -> favBtn.setStyle(SECONDARY_BTN));
        favBtn.setOnAction(e -> {
            toggleFavourite(p.getId());
            updateHeaderBadges();
            updateHeartBtn(p.getId());
            boolean nowFav = favourites.contains(p.getId());
            favBtn.setText(nowFav ? "♥  REMOVE FROM FAVOURITES" : "♡  ADD TO FAVOURITES");
            if (sidePanelMode.equals("favourites")) buildSidePanel();
        });

        details.getChildren().addAll(
            name, cat, price, sep,
            descHead, desc,
            colorLabel, stock,
            addBtn, favBtn);

        content.getChildren().addAll(imageSection, details);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;");
        dialog.setScene(new Scene(scroll, 780, 520));
        dialog.getScene().getStylesheets().add(
            getClass().getResource("/styles/main.css").toExternalForm());
        dialog.showAndWait();
    }

    private void updateDots(HBox dots, int activeIndex) {
        for (int i = 0; i < dots.getChildren().size(); i++) {
            ((Label) dots.getChildren().get(i)).setStyle(
                "-fx-font-size:8px;-fx-cursor:hand;" +
                "-fx-text-fill:" + (i == activeIndex ? "#111111" : "#cccccc") + ";");
        }
    }

    private void showFilterDialog() {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox root = new VBox(0);
        root.setPrefWidth(300);
        root.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e8e8e8;" +
            "-fx-border-radius:12px;" +
            "-fx-background-radius:12px;" +
            "-fx-padding:8;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),16,0,0,6);");

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        scroll.setPrefHeight(480);
        scroll.setPrefWidth(300);

        Label titleLbl = new Label("FILTERS");
        titleLbl.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-text-fill:#aaaaaa;-fx-letter-spacing:2px;-fx-padding:6 8 6 8;");
        root.getChildren().add(titleLbl);
        root.getChildren().add(dropSep());

        Label genHead = dropHead("GENDER");
        ToggleGroup genderGroup = new ToggleGroup();
        VBox genderBox = new VBox(4,
            buildRadio("All",   "all",   genderGroup, selectedGender),
            buildRadio("Men",   "Men",   genderGroup, selectedGender),
            buildRadio("Women", "Women", genderGroup, selectedGender));
        genderBox.setStyle("-fx-padding:6 12 10 12;");
        root.getChildren().addAll(genHead, genderBox, dropSep());

        Label catHead = dropHead("CATEGORIES");
        List<String> cats = List.of("Tops","Bottoms","Dresses","Outerwear","Accessories");
        List<CheckBox> catBoxes = new ArrayList<>();
        VBox catBox = new VBox(4);
        catBox.setStyle("-fx-padding:6 12 10 12;");
        for (String cat : cats) {
            CheckBox cb = new CheckBox(cat);
            cb.setSelected(selectedCategories.contains(cat));
            cb.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#333333;");
            catBoxes.add(cb);
            catBox.getChildren().add(cb);
        }
        root.getChildren().addAll(catHead, catBox, dropSep());

        Label sortHead = dropHead("SORT BY PRICE");
        ToggleGroup sortGroup = new ToggleGroup();
        VBox sortBox = new VBox(4,
            buildRadio("Default",            "default",    sortGroup, sortBy),
            buildRadio("Price: Low to High", "price-asc",  sortGroup, sortBy),
            buildRadio("Price: High to Low", "price-desc", sortGroup, sortBy));
        sortBox.setStyle("-fx-padding:6 12 10 12;");
        root.getChildren().addAll(sortHead, sortBox, dropSep());

        Label availHead = dropHead("AVAILABILITY");
        ToggleGroup availGroup = new ToggleGroup();
        VBox availBox = new VBox(4,
            buildRadio("All Products", "all",          availGroup, availabilityFilter),
            buildRadio("In Stock",     "in-stock",     availGroup, availabilityFilter),
            buildRadio("Out of Stock", "out-of-stock", availGroup, availabilityFilter));
        availBox.setStyle("-fx-padding:6 12 10 12;");
        root.getChildren().addAll(availHead, availBox, dropSep());

        HBox btnRow = new HBox(8);
        btnRow.setStyle("-fx-padding:10 8 8 8;");

        Button applyBtn = new Button("APPLY");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(applyBtn, Priority.ALWAYS);
        applyBtn.setStyle(
            "-fx-background-color:#111111;-fx-text-fill:white;" +
            "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-cursor:hand;-fx-background-radius:6;-fx-pref-height:38px;");

        Button clearBtn = new Button("CLEAR");
        clearBtn.setStyle(
            "-fx-background-color:#f5f5f5;-fx-text-fill:#555555;" +
            "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-cursor:hand;-fx-background-radius:6;" +
            "-fx-pref-height:38px;-fx-padding:0 16;");

        applyBtn.setOnAction(e -> {
            if (genderGroup.getSelectedToggle() != null)
                selectedGender = (String) genderGroup.getSelectedToggle().getUserData();
            selectedCategories.clear();
            for (int i = 0; i < catBoxes.size(); i++)
                if (catBoxes.get(i).isSelected()) selectedCategories.add(cats.get(i));
            if (sortGroup.getSelectedToggle() != null)
                sortBy = (String) sortGroup.getSelectedToggle().getUserData();
            if (availGroup.getSelectedToggle() != null)
                availabilityFilter = (String) availGroup.getSelectedToggle().getUserData();
            refreshGrid();
            popup.hide();
            updateFilterBtnText();
        });

        clearBtn.setOnAction(e -> {
            selectedGender = "all";
            selectedCategories.clear();
            sortBy = "default";
            filterMinPrice = 0;
            filterMaxPrice = 999999;
            availabilityFilter = "all";
            refreshGrid();
            popup.hide();
            updateFilterBtnText();
        });

        btnRow.getChildren().addAll(applyBtn, clearBtn);
        root.getChildren().add(btnRow);
        popup.getContent().add(scroll);

        javafx.geometry.Bounds bounds = filterBtn.localToScreen(filterBtn.getBoundsInLocal());
        popup.show(filterBtn, bounds.getMinX(), bounds.getMaxY() + 6);
    }

    private void updateFilterBtnText() {
        boolean hasFilters = !selectedCategories.isEmpty()
                || !"all".equals(selectedGender)
                || !"default".equals(sortBy)
                || !"all".equals(availabilityFilter);
        filterBtn.setText(hasFilters ? "Filters ●  ▾" : "Filters  ▾");
    }

    private Label dropHead(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:10px;-fx-font-weight:bold;" +
            "-fx-text-fill:#aaaaaa;-fx-letter-spacing:2px;-fx-padding:8 12 4 12;");
        return l;
    }

    private Separator dropSep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:#f0f0f0;");
        return s;
    }

    private RadioButton buildRadio(String label, String value,
                                   ToggleGroup group, String current) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setUserData(value);
        rb.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#333333;");
        if (value.equals(current)) rb.setSelected(true);
        return rb;
    }

    private void toggleFavourite(int id) {
        if (favourites.contains(id)) favourites.remove(id);
        else favourites.add(id);
        updateHeaderBadges();
    }

    private void addToCart(Product product) {
        if (product.getStock() == 0) {
            showOutOfStockAlert(product.getName());
            return;
        }
        cart.stream()
            .filter(ci -> ci.getProduct().getId() == product.getId())
            .findFirst()
            .ifPresentOrElse(
                ci -> ci.setQuantity(ci.getQuantity() + 1),
                () -> cart.add(new CartItem(product)));
        updateHeaderBadges();
    }

    private void updateHeaderBadges() {
        int favCount  = favourites.size();
        int cartCount = cart.stream().mapToInt(CartItem::getQuantity).sum();
        favouritesBtn.setText(
            favCount  > 0 ? "Favourites (" + favCount  + ")" : "Favourites");
        checkoutBtn.setText(
            cartCount > 0 ? "Checkout ("   + cartCount + ")" : "Checkout");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}