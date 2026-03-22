package app.product.controller;

import app.product.model.Product;
import app.product.service.AuthService;
import app.product.service.ProductService;
import app.product.util.SceneManager;
import app.product.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class StaffDashboardController implements Initializable {

    @FXML private TextField             searchField;
    @FXML private ComboBox<String>      availabilityCombo;
    @FXML private ComboBox<String>      categoryCombo;
    @FXML private Button                addProductBtn;
    @FXML private TableView<Product>    productTable;
    @FXML private TableColumn<Product, String>  colProductNum;
    @FXML private TableColumn<Product, String>  colName;
    @FXML private TableColumn<Product, String>  colCategory;
    @FXML private TableColumn<Product, String>  colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String>  colStatus;
    @FXML private TableColumn<Product, Void>    colActions;
    @FXML private Label                         statsLabel;

    private final ProductService productService = ProductService.getInstance();
    private FilteredList<Product> filteredList;

    private final Set<String> selectedCategories = new HashSet<>();
    private String selectedAvailability = "ALL";

    // Image cache to prevent flickering on scroll
    private final Map<String, Image> imageCache = new HashMap<>();

    private final List<String> ALL_CATEGORIES =
            List.of("Men","Women","Tops","Bottoms","Dresses","Outerwear","Accessories");

    private final String COMBO_STYLE =
        "-fx-background-color:#111111;" +
        "-fx-text-fill:white;" +
        "-fx-font-family:'Arial';" +
        "-fx-font-size:12px;" +
        "-fx-font-weight:bold;" +
        "-fx-background-radius:22px;" +
        "-fx-border-radius:22px;" +
        "-fx-border-color:transparent;" +
        "-fx-cursor:hand;" +
        "-fx-padding:0 20;";

    private final String COMBO_HOVER =
        "-fx-background-color:#2a2a2a;" +
        "-fx-text-fill:white;" +
        "-fx-font-family:'Arial';" +
        "-fx-font-size:12px;" +
        "-fx-font-weight:bold;" +
        "-fx-background-radius:22px;" +
        "-fx-border-radius:22px;" +
        "-fx-border-color:transparent;" +
        "-fx-cursor:hand;" +
        "-fx-padding:0 20;";

    private final String OPTION_BASE =
        "-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;" +
        "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;" +
        "-fx-padding:10 16;-fx-alignment:CENTER_LEFT;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        updateStats();
    }

    private void setupTable() {
        colProductNum.setCellValueFactory(new PropertyValueFactory<>("productNumber"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryDisplay"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));

        colPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPriceDisplay()));

        colName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getName()));

        colProductNum.setComparator(String.CASE_INSENSITIVE_ORDER);

        colProductNum.setSortable(true);
        colName.setSortable(false);
        colPrice.setSortable(true);
        colStock.setSortable(true);
        colCategory.setSortable(false);
        colStatus.setSortable(false);
        colActions.setSortable(false);

        colPrice.setComparator((a, b) -> {
            try {
                double da = Double.parseDouble(a.replace("£", "").trim());
                double db = Double.parseDouble(b.replace("£", "").trim());
                return Double.compare(da, db);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });

        colStock.setComparator(Comparator.comparingInt(i -> i));

        productTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setStyle("");
                } else if (p.getStock() == 0) {
                    setStyle("-fx-background-color:#fff0f0;");
                } else {
                    setStyle("");
                }
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            private final HBox  box   = new HBox(badge);
            {
                box.setAlignment(Pos.CENTER);
                badge.setAlignment(Pos.CENTER);
                badge.setMinWidth(70);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                if ("Active".equals(item)) {
                    badge.setStyle(
                        "-fx-text-fill:#166534;-fx-font-size:12px;" +
                        "-fx-font-family:'Arial';-fx-font-weight:bold;" +
                        "-fx-background-color:#dcfce7;" +
                        "-fx-background-radius:30;-fx-border-color:#86efac;" +
                        "-fx-border-radius:30;-fx-border-width:1.5;-fx-padding:6 16;");
                } else {
                    badge.setStyle(
                        "-fx-text-fill:#991b1b;-fx-font-size:12px;" +
                        "-fx-font-family:'Arial';-fx-font-weight:bold;" +
                        "-fx-background-color:#fee2e2;" +
                        "-fx-background-radius:30;-fx-border-color:#fca5a5;" +
                        "-fx-border-radius:30;-fx-border-width:1.5;-fx-padding:6 16;");
                }
                setAlignment(Pos.CENTER);
                setGraphic(box);
                setText(null);
            }
        });

        colName.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv         = new ImageView();
            private final Label     nameLabel  = new Label();
            private final Label     colorLabel = new Label();
            private final HBox      box        = new HBox(12);
            {
                iv.setFitWidth(44); iv.setFitHeight(44); iv.setPreserveRatio(false);
                nameLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;" +
                    "-fx-font-weight:bold;-fx-text-fill:#111111;");
                colorLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:11px;" +
                    "-fx-text-fill:#888888;");
                VBox vb = new VBox(3, nameLabel, colorLabel);
                vb.setAlignment(Pos.CENTER_LEFT);
                box.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().addAll(iv, vb);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                Product p = getTableView().getItems().get(getIndex());
                nameLabel.setText(p.getName());
                colorLabel.setText(p.getColor());
                loadImageAsync(iv, p.getFirstImage(), 44, 44);
                setGraphic(box);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(8, editBtn, toggleBtn, deleteBtn);

            private final String editDefault =
                "-fx-background-color:transparent;-fx-font-size:14px;-fx-cursor:hand;" +
                "-fx-padding:4 6;-fx-border-color:transparent;";
            private final String editHover =
                "-fx-background-color:#f0f0f0;-fx-font-size:14px;-fx-cursor:hand;" +
                "-fx-background-radius:6;-fx-padding:4 6;-fx-border-color:transparent;";
            private final String deleteDefault =
                "-fx-background-color:transparent;-fx-font-size:15px;-fx-cursor:hand;" +
                "-fx-text-fill:#e53935;-fx-padding:4 6;-fx-border-color:transparent;";
            private final String deleteHover =
                "-fx-background-color:#fdecea;-fx-font-size:15px;-fx-cursor:hand;" +
                "-fx-text-fill:#c62828;-fx-background-radius:6;-fx-padding:4 6;" +
                "-fx-border-color:transparent;";
            private final String toggleDefault =
                "-fx-background-color:transparent;-fx-text-fill:#111111;" +
                "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:4 6;";
            private final String toggleHover =
                "-fx-background-color:#f0f0f0;-fx-text-fill:#111111;" +
                "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-cursor:hand;-fx-border-color:transparent;" +
                "-fx-background-radius:6;-fx-padding:4 6;";
            {
                editBtn.setStyle(editDefault);
                deleteBtn.setStyle(deleteDefault);
                toggleBtn.setStyle(toggleDefault);
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(editHover));
                editBtn.setOnMouseExited(e  -> editBtn.setStyle(editDefault));
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(deleteHover));
                deleteBtn.setOnMouseExited(e  -> deleteBtn.setStyle(deleteDefault));
                toggleBtn.setOnMouseEntered(e -> toggleBtn.setStyle(toggleHover));
                toggleBtn.setOnMouseExited(e  -> toggleBtn.setStyle(toggleDefault));
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                Product p = getTableView().getItems().get(getIndex());
                toggleBtn.setText(p.isActive() ? "Deactivate" : "Activate");
                toggleBtn.setStyle(toggleDefault);
                editBtn.setOnAction(e -> openProductDialog(p));
                toggleBtn.setOnAction(e -> {
                    productService.toggleActive(p.getId());
                    productTable.refresh();
                    updateStats();
                });
                deleteBtn.setOnAction(e -> confirmDelete(p));
                setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        filteredList = new FilteredList<>(productService.getAll(), p -> true);
        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedList);
    }

    private void setupFilters() {
        categoryCombo.setVisible(false);
        categoryCombo.setManaged(false);
        availabilityCombo.setVisible(false);
        availabilityCombo.setManaged(false);

        HBox filterRow = (HBox) categoryCombo.getParent();

        Button catBtn = new Button("CATEGORIES  ▾");
        catBtn.setStyle(COMBO_STYLE);
        catBtn.setPrefHeight(44);
        HBox.setHgrow(catBtn, Priority.ALWAYS);
        catBtn.setMaxWidth(Double.MAX_VALUE);
        catBtn.setOnMouseEntered(e -> catBtn.setStyle(COMBO_HOVER + "-fx-pref-height:44;"));
        catBtn.setOnMouseExited(e  -> catBtn.setStyle(COMBO_STYLE  + "-fx-pref-height:44;"));
        catBtn.setOnAction(e -> showCategoryDropdown(catBtn));

        Button availBtn = new Button("ALL  ▾");
        availBtn.setStyle(COMBO_STYLE);
        availBtn.setPrefHeight(44);
        availBtn.setPrefWidth(220);
        availBtn.setOnMouseEntered(e -> availBtn.setStyle(COMBO_HOVER + "-fx-pref-height:44;-fx-pref-width:220;"));
        availBtn.setOnMouseExited(e  -> availBtn.setStyle(COMBO_STYLE  + "-fx-pref-height:44;-fx-pref-width:220;"));
        availBtn.setOnAction(e -> showAvailabilityPopup(availBtn));

        filterRow.getChildren().add(0, availBtn);
        filterRow.getChildren().add(0, catBtn);

        searchField.textProperty().addListener((obs, o, text) -> applyFilters());
    }

    private VBox buildDropdownContent(double width) {
        VBox content = new VBox(0);
        content.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e8e8e8;" +
            "-fx-border-radius:12px;" +
            "-fx-background-radius:12px;" +
            "-fx-padding:8;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),16,0,0,6);");
        content.setPrefWidth(width);
        return content;
    }

    private void showCategoryDropdown(Button catBtn) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox content = buildDropdownContent(280);

        Label head = new Label("CATEGORIES");
        head.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-text-fill:#aaaaaa;-fx-padding:6 8 6 8;");
        content.getChildren().add(head);

        Separator topSep = new Separator();
        topSep.setStyle("-fx-background-color:#f0f0f0;-fx-padding:0;");
        content.getChildren().add(topSep);

        List<CheckBox> boxes = new ArrayList<>();
        for (String cat : ALL_CATEGORIES) {
            CheckBox cb = new CheckBox(cat);
            cb.setSelected(selectedCategories.contains(cat));
            cb.setMaxWidth(Double.MAX_VALUE);
            cb.setStyle(
                "-fx-font-family:'Arial';-fx-font-size:13px;" +
                "-fx-text-fill:#111111;-fx-cursor:hand;" +
                "-fx-padding:10 12;-fx-background-radius:8;" +
                "-fx-background-color:transparent;");
            cb.setOnMouseEntered(ev ->
                cb.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;" +
                    "-fx-text-fill:#111111;-fx-cursor:hand;" +
                    "-fx-padding:10 12;-fx-background-radius:8;" +
                    "-fx-background-color:#f5f5f5;"));
            cb.setOnMouseExited(ev ->
                cb.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;" +
                    "-fx-text-fill:#111111;-fx-cursor:hand;" +
                    "-fx-padding:10 12;-fx-background-radius:8;" +
                    "-fx-background-color:transparent;"));
            boxes.add(cb);
            content.getChildren().add(cb);
        }

        Separator bottomSep = new Separator();
        bottomSep.setStyle("-fx-background-color:#f0f0f0;");
        content.getChildren().add(bottomSep);

        Label countLabel = new Label();
        countLabel.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:11px;" +
            "-fx-text-fill:#aaaaaa;-fx-padding:6 8 2 8;");
        updateCountLabel(countLabel, boxes);
        for (CheckBox cb : boxes)
            cb.selectedProperty().addListener((obs, o, n) -> updateCountLabel(countLabel, boxes));
        content.getChildren().add(countLabel);

        HBox btnRow = new HBox(6);
        btnRow.setStyle("-fx-padding:4 8 8 8;");

        Button apply = new Button("Apply");
        apply.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(apply, Priority.ALWAYS);
        apply.setStyle(
            "-fx-background-color:#111111;-fx-text-fill:white;" +
            "-fx-background-radius:8;-fx-border-radius:8;" +
            "-fx-font-family:'Arial';-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-cursor:hand;-fx-padding:8 0;");

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle(
            "-fx-background-color:#f5f5f5;-fx-text-fill:#555555;" +
            "-fx-border-color:transparent;" +
            "-fx-border-radius:8;-fx-background-radius:8;" +
            "-fx-font-family:'Arial';-fx-font-size:12px;" +
            "-fx-cursor:hand;-fx-padding:8 14;");

        apply.setOnAction(e -> {
            selectedCategories.clear();
            for (CheckBox cb : boxes)
                if (cb.isSelected()) selectedCategories.add(cb.getText());
            catBtn.setText(selectedCategories.isEmpty()
                ? "CATEGORIES  ▾"
                : String.join(", ", selectedCategories).toUpperCase() + "  ▾");
            applyFilters();
            popup.hide();
        });

        clearBtn.setOnAction(e -> {
            selectedCategories.clear();
            boxes.forEach(cb -> cb.setSelected(false));
            catBtn.setText("CATEGORIES  ▾");
            applyFilters();
            popup.hide();
        });

        btnRow.getChildren().addAll(apply, clearBtn);
        content.getChildren().add(btnRow);
        popup.getContent().add(content);

        javafx.geometry.Bounds bounds = catBtn.localToScreen(catBtn.getBoundsInLocal());
        popup.show(catBtn, bounds.getMinX(), bounds.getMaxY() + 6);
    }

    private void showAvailabilityPopup(Button availBtn) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox content = buildDropdownContent(220);

        Label head = new Label("AVAILABILITY");
        head.setStyle(
            "-fx-font-family:'Arial';-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-text-fill:#aaaaaa;-fx-padding:6 8 6 8;");
        content.getChildren().add(head);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#f0f0f0;");
        content.getChildren().add(sep);

        for (String opt : List.of("ALL", "ACTIVE", "INACTIVE")) {
            Button b = new Button(opt);
            b.setMaxWidth(Double.MAX_VALUE);
            boolean selected = opt.equals(selectedAvailability);
            b.setStyle(OPTION_BASE +
                "-fx-background-color:" + (selected ? "#111111" : "transparent") + ";" +
                "-fx-text-fill:" + (selected ? "white" : "#111111") + ";" +
                "-fx-border-color:transparent;");
            b.setOnMouseEntered(ev -> {
                if (!opt.equals(selectedAvailability))
                    b.setStyle(OPTION_BASE +
                        "-fx-background-color:#f5f5f5;" +
                        "-fx-text-fill:#111111;" +
                        "-fx-border-color:transparent;");
            });
            b.setOnMouseExited(ev -> {
                b.setStyle(OPTION_BASE +
                    "-fx-background-color:" + (opt.equals(selectedAvailability) ? "#111111" : "transparent") + ";" +
                    "-fx-text-fill:" + (opt.equals(selectedAvailability) ? "white" : "#111111") + ";" +
                    "-fx-border-color:transparent;");
            });
            b.setOnAction(e -> {
                selectedAvailability = opt;
                availBtn.setText(opt + "  ▾");
                applyFilters();
                popup.hide();
            });
            content.getChildren().add(b);
        }

        popup.getContent().add(content);

        javafx.geometry.Bounds bounds = availBtn.localToScreen(availBtn.getBoundsInLocal());
        popup.show(availBtn, bounds.getMinX(), bounds.getMaxY() + 6);
    }

    private void updateCountLabel(Label label, List<CheckBox> boxes) {
        long count = boxes.stream().filter(CheckBox::isSelected).count();
        label.setText(count == 0
            ? "No categories selected — showing all"
            : count + " categor" + (count == 1 ? "y" : "ies") + " selected");
    }

    private void applyFilters() {
        String raw   = searchField.getText();
        String query = normalise(raw == null ? "" : raw);

        filteredList.setPredicate(p -> {
            boolean searchMatch;
            if (query.isBlank()) {
                searchMatch = true;
            } else {
                String searchable = normalise(
                        p.getName() + " " + p.getProductNumber() + " " +
                        p.getCategoryDisplay() + " " + p.getGender() + " " +
                        p.getColor() + " " +
                        (p.getDescription() != null ? p.getDescription() : ""));
                String[] words = query.split("\\s+");
                searchMatch = true;
                for (String word : words) {
                    if (word.equals("men")) {
                        boolean hasMen   = hasGenderToken(searchable, "men");
                        boolean hasWomen = hasGenderToken(searchable, "women");
                        if (!hasMen && hasWomen)  { searchMatch = false; break; }
                        if (!hasMen && !hasWomen) { searchMatch = false; break; }
                    } else if (word.equals("women")) {
                        if (!hasGenderToken(searchable, "women")) { searchMatch = false; break; }
                    } else {
                        if (!searchable.contains(word)) { searchMatch = false; break; }
                    }
                }
            }

            boolean catMatch = selectedCategories.isEmpty()
                    || p.getCategories().stream()
                         .anyMatch(c -> selectedCategories.stream()
                                 .anyMatch(s -> s.equalsIgnoreCase(c)));

            boolean availMatch = switch (selectedAvailability) {
                case "ACTIVE"   -> p.isActive();
                case "INACTIVE" -> !p.isActive();
                default         -> true;
            };

            return searchMatch && catMatch && availMatch;
        });
        updateStats();
    }

    private String normalise(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                   .replace("'", "").replace("\u2019", "")
                   .replace("-", " ").trim();
    }

    private boolean hasGenderToken(String text, String gender) {
        String padded = " " + text + " ";
        return padded.contains(" " + gender + " ")
                || padded.contains(" " + gender + ",")
                || padded.contains(" " + gender + ".")
                || text.startsWith(gender + " ")
                || text.equals(gender);
    }

    @FXML
    private void onAddProduct() { openProductDialog(null); }

    private void openProductDialog(Product editingProduct) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editingProduct == null ? "Add New Product" : "Edit Product");

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setPrefWidth(520);

        Label title = new Label(editingProduct == null ? "Add New Product" : "Edit Product");
        title.setStyle("-fx-font-family:'Georgia';-fx-font-size:20px;-fx-font-weight:bold;");
        Label subtitle = new Label(editingProduct == null
                ? "Add a new product to your catalogue."
                : "Edit the product details below.");
        subtitle.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-text-fill:#888;");

        Label pnLabel = new Label("Product Number *");
        pnLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextField pnField = new TextField(editingProduct != null
                ? editingProduct.getProductNumber() : productService.generateProductNumber());
        pnField.getStyleClass().add("input-field");
        Label pnError = errorLabel();

        Label nameLabel = new Label("Product Name *");
        nameLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextField nameField = new TextField(editingProduct != null ? editingProduct.getName() : "");
        nameField.setPromptText("e.g., Women's Silk Dress");
        nameField.getStyleClass().add("input-field");
        Label nameError = errorLabel();

        HBox priceStockRow = new HBox(12);
        VBox priceBox = new VBox(4);
        Label priceLabel = new Label("Price (£) *");
        priceLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextField priceField = new TextField(editingProduct != null
                ? String.valueOf(editingProduct.getPrice()) : "");
        priceField.setPromptText("0.00");
        priceField.getStyleClass().add("input-field");
        Label priceError = errorLabel();
        priceBox.getChildren().addAll(priceLabel, priceField, priceError);

        VBox stockBox = new VBox(4);
        Label stockLabel = new Label("Stock Quantity *");
        stockLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextField stockField = new TextField(editingProduct != null
                ? String.valueOf(editingProduct.getStock()) : "");
        stockField.setPromptText("0");
        stockField.getStyleClass().add("input-field");
        Label stockError = errorLabel();
        stockBox.getChildren().addAll(stockLabel, stockField, stockError);
        HBox.setHgrow(priceBox, Priority.ALWAYS);
        HBox.setHgrow(stockBox, Priority.ALWAYS);
        priceStockRow.getChildren().addAll(priceBox, stockBox);

        HBox catGenderRow = new HBox(12);
        VBox catBox = new VBox(4);
        Label catLabel = new Label("Categories * (select at least one)");
        catLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        VBox catChecks = new VBox(6);
        catChecks.setStyle("-fx-border-color:#e0e0e0;-fx-border-radius:6;" +
            "-fx-background-color:#fafafa;-fx-background-radius:6;-fx-padding:10;");
        List<CheckBox> catBoxes = new ArrayList<>();
        for (String cat : ALL_CATEGORIES) {
            CheckBox cb = new CheckBox(cat);
            cb.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;");
            if (editingProduct != null) cb.setSelected(editingProduct.getCategories().contains(cat));
            catBoxes.add(cb);
            catChecks.getChildren().add(cb);
        }
        Label catError = errorLabel();
        catBox.getChildren().addAll(catLabel, catChecks, catError);

        VBox genderBox = new VBox(4);
        Label genderLabel = new Label("Gender *");
        genderLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Men", "Women", "Unisex");
        genderCombo.setPromptText("Select gender");
        genderCombo.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;" +
            "-fx-background-color:#f7f7f7;-fx-border-color:#e0e0e0;" +
            "-fx-border-radius:8;-fx-background-radius:8;");
        if (editingProduct != null) genderCombo.setValue(editingProduct.getGender());
        genderBox.getChildren().addAll(genderLabel, genderCombo);
        HBox.setHgrow(catBox, Priority.ALWAYS);
        catGenderRow.getChildren().addAll(catBox, genderBox);

        Label colorLabel = new Label("Color *");
        colorLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextField colorField = new TextField(editingProduct != null ? editingProduct.getColor() : "");
        colorField.setPromptText("e.g., Black, Beige");
        colorField.getStyleClass().add("input-field");
        Label colorError = errorLabel();

        Label imgLabel = new Label("Product Images (Min: 1, Max: 4) *");
        imgLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        List<TextField> imgFields = new ArrayList<>();
        VBox imgBox = new VBox(8);
        String[] imgLabelTexts = {
            "Image 1 (Required)", "Image 2 (Optional)",
            "Image 3 (Optional)", "Image 4 (Optional)"
        };
        for (int i = 0; i < 4; i++) {
            Label lbl = new Label(imgLabelTexts[i]);
            lbl.setStyle("-fx-font-family:'Arial';-fx-font-size:12px;-fx-text-fill:#555;");
            TextField tf = new TextField(
                editingProduct != null && editingProduct.getImages().size() > i
                    ? editingProduct.getImages().get(i) : "");
            tf.setPromptText("https://images.unsplash.com/...");
            tf.getStyleClass().add("input-field");
            imgFields.add(tf);
            imgBox.getChildren().addAll(lbl, tf);
        }
        Label imgError = errorLabel();

        Label descLabel = new Label("Description *");
        descLabel.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;-fx-font-weight:bold;");
        TextArea descArea = new TextArea(editingProduct != null ? editingProduct.getDescription() : "");
        descArea.setPromptText("Product description...");
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;" +
            "-fx-background-color:#f7f7f7;-fx-border-color:#e0e0e0;");
        Label descError = errorLabel();

        CheckBox activeCheck = new CheckBox("Active (visible to customers)");
        activeCheck.setStyle("-fx-font-family:'Arial';-fx-font-size:13px;");
        activeCheck.setSelected(editingProduct == null || editingProduct.isActive());

        HBox btnRow = new HBox(8);
        Button saveBtn = new Button(editingProduct == null ? "Add Product" : "Update Product");
        saveBtn.getStyleClass().add("login-btn");
        saveBtn.setPrefWidth(160);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color:white;-fx-border-color:#dddddd;" +
            "-fx-border-radius:8;-fx-background-radius:8;" +
            "-fx-font-family:'Arial';-fx-font-size:13px;" +
            "-fx-cursor:hand;-fx-padding:8 16;");
        cancelBtn.setOnAction(e -> dialog.close());
        btnRow.getChildren().addAll(saveBtn, cancelBtn);

        saveBtn.setOnAction(e -> {
            for (Label el : List.of(pnError, nameError, priceError,
                    stockError, catError, colorError, imgError, descError))
                el.setText("");

            boolean valid = true;
            int excludeId = editingProduct != null ? editingProduct.getId() : -1;

            String pn = pnField.getText().trim();
            if (ValidationUtil.isBlank(pn)) {
                pnError.setText("Product number is required"); valid = false;
            } else if (!productService.isProductNumberUnique(pn, excludeId)) {
                pnError.setText("Product number already exists"); valid = false;
            }

            String nm = nameField.getText().trim();
            if (ValidationUtil.isBlank(nm)) {
                nameError.setText("Product name is required"); valid = false;
            } else if (!productService.isNameUnique(nm, excludeId)) {
                nameError.setText("Name already exists"); valid = false;
            }

            if (!ValidationUtil.isPositiveDouble(priceField.getText())) {
                priceError.setText("Enter a positive number"); valid = false;
            }
            if (!ValidationUtil.isNonNegativeInt(stockField.getText())) {
                stockError.setText("Enter a non-negative integer"); valid = false;
            }

            List<String> selCats = new ArrayList<>();
            for (int i = 0; i < catBoxes.size(); i++)
                if (catBoxes.get(i).isSelected()) selCats.add(ALL_CATEGORIES.get(i));
            if (selCats.isEmpty()) {
                catError.setText("Select at least one category"); valid = false;
            }

            if (ValidationUtil.isBlank(colorField.getText())) {
                colorError.setText("Color is required"); valid = false;
            }
            if (ValidationUtil.isBlank(descArea.getText())) {
                descError.setText("Description is required"); valid = false;
            }

            List<String> imgs = imgFields.stream()
                    .map(tf -> tf.getText().trim())
                    .filter(s -> !s.isEmpty()).toList();
            if (imgs.isEmpty() || imgs.size() > 4) {
                imgError.setText("Provide between 1 and 4 image URLs"); valid = false;
            }

            if (!valid) return;

            if (editingProduct == null) {
                Product np = new Product(
                        0, pn, nm,
                        Double.parseDouble(priceField.getText()),
                        selCats,
                        genderCombo.getValue() != null ? genderCombo.getValue() : "Unisex",
                        colorField.getText().trim(),
                        imgs,
                        descArea.getText().trim(),
                        Integer.parseInt(stockField.getText()),
                        activeCheck.isSelected());
                productService.add(np);
            } else {
                editingProduct.setProductNumber(pn);
                editingProduct.setName(nm);
                editingProduct.setPrice(Double.parseDouble(priceField.getText()));
                editingProduct.setCategories(selCats);
                editingProduct.setGender(genderCombo.getValue() != null
                        ? genderCombo.getValue() : "Unisex");
                editingProduct.setColor(colorField.getText().trim());
                editingProduct.setImages(imgs);
                editingProduct.setDescription(descArea.getText().trim());
                editingProduct.setStock(Integer.parseInt(stockField.getText()));
                editingProduct.setActive(activeCheck.isSelected());
                productService.update(editingProduct);
            }

            productTable.refresh();
            updateStats();
            dialog.close();
        });

        root.getChildren().addAll(
                title, subtitle, new Separator(),
                pnLabel, pnField, pnError,
                nameLabel, nameField, nameError,
                priceStockRow, catGenderRow,
                colorLabel, colorField, colorError,
                imgLabel, imgBox, imgError,
                descLabel, descArea, descError,
                activeCheck, new Separator(), btnRow);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        dialog.setScene(new Scene(scroll, 560, 700));
        dialog.getScene().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
        dialog.showAndWait();
    }

    private void confirmDelete(Product p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Delete \"" + p.getName() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                productService.delete(p.getId());
                productTable.refresh();
                updateStats();
            }
        });
    }

    @FXML
    private void onLogout() {
        AuthService.getInstance().logout();
        SceneManager.getInstance().switchTo(SceneManager.LOGIN);
    }

    private void updateStats() {
        int total   = productService.getAll().size();
        int showing = filteredList != null ? filteredList.size() : total;
        statsLabel.setText("Showing " + showing + " of " + total + " products");
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill:#e53935;-fx-font-size:12px;-fx-font-family:'Arial';");
        return l;
    }

    private void loadImageAsync(ImageView iv, String url, double w, double h) {
        if (url == null || url.isBlank()) return;
        if (imageCache.containsKey(url)) {
            iv.setImage(imageCache.get(url));
            return;
        }
        iv.setImage(null);
        new Thread(() -> {
            try {
                Image img = new Image(url, w, h, false, true, true);
                Platform.runLater(() -> {
                    imageCache.put(url, img);
                    iv.setImage(img);
                });
            } catch (Exception ignored) {}
        }).start();
    }
}