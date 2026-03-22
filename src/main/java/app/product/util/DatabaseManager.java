package app.product.util;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
 
public class DatabaseManager {
 
    private static DatabaseManager instance;
    private Connection connection;
 
    private static final String DB_URL = "jdbc:sqlite:noch_products.db";
 
    private DatabaseManager() {
        connect();
        initSchema();
    }
 
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
 
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            Statement st = connection.createStatement();
            st.execute("PRAGMA foreign_keys = ON");
            System.out.println("Connected to SQLite: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
        }
    }
 
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Connection check failed: " + e.getMessage());
        }
        return connection;
    }
 
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DB connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }
 
    // ── Schema init ───────────────────────────────────────────────────────────
 
    private void initSchema() {
        try (Statement st = connection.createStatement()) {
 
            st.execute("""
                CREATE TABLE IF NOT EXISTS login_authentication (
                    user_id       TEXT PRIMARY KEY,
                    email         TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role          TEXT NOT NULL CHECK (role IN ('Customer','Staff')),
                    is_active     INTEGER NOT NULL DEFAULT 1
                )
            """);
 
            st.execute("""
                CREATE TABLE IF NOT EXISTS category (
                    category_id   TEXT PRIMARY KEY,
                    category_name TEXT NOT NULL UNIQUE,
                    category_type TEXT NOT NULL,
                    description   TEXT
                )
            """);
 
            st.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    product_id     TEXT PRIMARY KEY,
                    product_name   TEXT NOT NULL UNIQUE,
                    description    TEXT,
                    price          REAL NOT NULL CHECK (price > 0),
                    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                    gender         TEXT NOT NULL DEFAULT 'Unisex',
                    color          TEXT NOT NULL DEFAULT '',
                    activity       TEXT NOT NULL DEFAULT 'Active'
                                   CHECK (activity IN ('Active','Inactive')),
                    category       TEXT NOT NULL DEFAULT '',
                    image          TEXT DEFAULT ''
                )
            """);
 
            st.execute("""
                CREATE TABLE IF NOT EXISTS product_images (
                    image_id      TEXT PRIMARY KEY,
                    product_id    TEXT NOT NULL,
                    image_url     TEXT NOT NULL,
                    is_primary    INTEGER NOT NULL DEFAULT 0,
                    display_order INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (product_id)
                        REFERENCES products(product_id)
                        ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);
 
            st.execute("""
                CREATE TABLE IF NOT EXISTS product_category (
                    product_id  TEXT NOT NULL,
                    category_id TEXT NOT NULL,
                    PRIMARY KEY (product_id, category_id),
                    FOREIGN KEY (product_id)
                        REFERENCES products(product_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY (category_id)
                        REFERENCES category(category_id)
                        ON DELETE RESTRICT ON UPDATE CASCADE
                )
            """);
 
            // Indexes
            st.execute("CREATE INDEX IF NOT EXISTS idx_products_name     ON products(product_name)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_products_activity ON products(activity)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_products_price    ON products(price)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_img_pid           ON product_images(product_id)");
 
            System.out.println("Schema initialised.");
            seedData(st);
 
        } catch (SQLException e) {
            System.err.println("Schema init failed: " + e.getMessage());
        }
    }
 
    // ── Seed data ─────────────────────────────────────────────────────────────
 
    private void seedData(Statement st) throws SQLException {
 
        st.execute("""
            INSERT OR IGNORE INTO login_authentication VALUES
            ('USR-00001','admin@noch.com',     'staff123',    'Staff',    1),
            ('USR-00002','customer@gmail.com', 'customer123', 'Customer', 1)
        """);
 
        st.execute("""
            INSERT OR IGNORE INTO category VALUES
            ('CAT-00001','Tops',        'Unisex','Tops and shirts'),
            ('CAT-00002','Bottoms',     'Unisex','Trousers, shorts, jeans'),
            ('CAT-00003','Dresses',     'Women', 'Dresses and skirts'),
            ('CAT-00004','Outerwear',   'Unisex','Coats and jackets'),
            ('CAT-00005','Accessories', 'Unisex','Jewellery, belts, scarves')
        """);
 
        // Seed 21 products
        String[] ids = {
            "NOCH-00001","NOCH-00002","NOCH-00003","NOCH-00004","NOCH-00005",
            "NOCH-00006","NOCH-00007","NOCH-00008","NOCH-00009","NOCH-00010",
            "NOCH-00011","NOCH-00012","NOCH-00013","NOCH-00014","NOCH-00015",
            "NOCH-00016","NOCH-00017","NOCH-00018","NOCH-00019","NOCH-00020",
            "NOCH-00021"
        };
        String[] names = {
            "Men's Stainless Steel Bracelet","Women's Satin Evening Dress",
            "Women's Crystal Drop Earrings","Men's Urban Cargo Pants",
            "Women's High-Waist Linen Trousers","Women's Soft Knit Sweater",
            "Men's Casual Button-Down Shirt","Women's Floral Casual Dress",
            "Men's Tailored Formal Trousers","Men's Heritage Leather Belt",
            "Women's Satin Slip Dress","Men's Classic Chinos",
            "Men's Slim-Fit Oxford Shirt","Women's Boho Layered Bracelet",
            "Women's High-Waist Denim Shorts","Women's Minimal Gold Necklace",
            "Men's Rustic Leather Wristband","Men's Summer Linen Shirt",
            "Women's Tailored Wide-Leg Trousers","Men's Sport Performance Shirt",
            "Women's Silk Scarf"
        };
        String[] descs = {
            "A sleek stainless steel bracelet for everyday wear.",
            "Luxurious emerald satin dress for evening occasions.",
            "Delicate crystal drop earrings with sterling silver finish.",
            "Modern olive cargo pants with multiple utility pockets.",
            "Breathable high-waist linen trousers in natural beige.",
            "Cosy cream knit sweater in ultra-soft fabric.",
            "Classic blue button-down shirt for casual occasions.",
            "Light floral print casual dress perfect for summer.",
            "Slim-cut charcoal formal trousers with tailored finish.",
            "Full-grain leather belt with antique brass buckle.",
            "Elegant blush satin slip dress with adjustable straps.",
            "Timeless navy chinos with a slim straight cut.",
            "Crisp white Oxford shirt in a slim fit.",
            "Bohemian layered bracelet set with mixed materials.",
            "Classic light-wash high-waist denim shorts.",
            "Delicate gold chain necklace with minimalist pendant.",
            "Handcrafted rustic leather wristband with snap closure.",
            "Breathable beige linen shirt ideal for warm weather.",
            "Elegant wide-leg trousers in crisp white fabric.",
            "Moisture-wicking performance shirt for active lifestyles.",
            "Luxurious silk scarf in warm mustard yellow."
        };
        double[] prices = {39,142,27,74,68,52,44,116,85,33,128,64,56,22,41,48,25,49,72,37,54};
        int[] stocks    = {50,25,100,40,35,60,75,20,30,80,15,55,90,120,45,200,65,70,28,110,40};
        String[] genders = {
            "Men","Women","Women","Men","Women","Women","Men","Women","Men","Men",
            "Women","Men","Men","Women","Women","Women","Men","Men","Women","Men","Women"
        };
        String[] colors = {
            "Silver","Emerald","Silver","Olive","Beige","Cream","Blue","Floral",
            "Charcoal","Brown","Blush","Navy","White","Multi","Light Wash",
            "Gold","Dark Brown","Beige","White","Black","Mustard"
        };
        String[] cats = {
            "Men, Accessories","Women, Dresses","Women, Accessories","Men, Bottoms",
            "Women, Bottoms","Women, Tops","Men, Tops","Women, Dresses","Men, Bottoms",
            "Men, Accessories","Women, Dresses","Men, Bottoms","Men, Tops",
            "Women, Accessories","Women, Bottoms","Women, Accessories","Men, Accessories",
            "Men, Tops","Women, Bottoms","Men, Tops","Women, Accessories"
        };
        String[] images = {
            "https://images.unsplash.com/photo-1708389828508-51fb95360cef?w=600",
            "https://images.unsplash.com/photo-1667526512546-4710d79a9f4d?w=600",
            "https://images.unsplash.com/photo-1685524820989-5b822999d8db?w=600",
            "https://images.unsplash.com/photo-1754903134409-6a59d908cc75?w=600",
            "https://images.unsplash.com/photo-1638396637969-956ca903df87?w=600",
            "https://images.unsplash.com/photo-1603906650843-b58e94d9df4d?w=600",
            "https://images.unsplash.com/photo-1573662073208-1f58a071c756?w=600",
            "https://images.unsplash.com/photo-1678883643221-08061ee6f1aa?w=600",
            "https://images.unsplash.com/photo-1760616172899-0681b97a2de3?w=600",
            "https://images.unsplash.com/photo-1721483246145-d5b82d10e3e7?w=600",
            "https://images.unsplash.com/photo-1650562816756-0e7f15d14343?w=600",
            "https://images.unsplash.com/photo-1718590812273-4b0e0406a27c?w=600",
            "https://images.unsplash.com/photo-1601460588655-109bd38204db?w=600",
            "https://images.unsplash.com/photo-1644980379438-60fdfafb8e9c?w=600",
            "https://images.unsplash.com/photo-1696889450800-e94ec7a32206?w=600",
            "https://images.unsplash.com/photo-1625792508300-0e1f913a3a50?w=600",
            "https://images.unsplash.com/photo-1759401909241-f554229ed260?w=600",
            "https://images.unsplash.com/photo-1651895884377-5f631be84282?w=600",
            "https://images.unsplash.com/photo-1763558978011-55404124a148?w=600",
            "https://images.unsplash.com/photo-1762331655528-4af4bccf7c62?w=600",
            "https://images.unsplash.com/photo-1594938298603-c8148c4b4dc4?w=600"
        };
 
        String productSql = """
            INSERT OR IGNORE INTO products
                (product_id,product_name,description,price,stock_quantity,
                 gender,color,activity,category,image)
            VALUES (?,?,?,?,?,?,?,?,?,?)
        """;
 
        String imageSql = """
            INSERT OR IGNORE INTO product_images
                (image_id,product_id,image_url,is_primary,display_order)
            VALUES (?,?,?,1,1)
        """;
 
        try (
            java.sql.PreparedStatement pp = connection.prepareStatement(productSql);
            java.sql.PreparedStatement ip = connection.prepareStatement(imageSql)
        ) {
            for (int i = 0; i < ids.length; i++) {
                pp.setString(1, ids[i]);
                pp.setString(2, names[i]);
                pp.setString(3, descs[i]);
                pp.setDouble(4, prices[i]);
                pp.setInt   (5, stocks[i]);
                pp.setString(6, genders[i]);
                pp.setString(7, colors[i]);
                pp.setString(8, "Active");
                pp.setString(9, cats[i]);
                pp.setString(10, images[i]);
                pp.executeUpdate();
 
                ip.setString(1, ids[i] + "-IMG-1");
                ip.setString(2, ids[i]);
                ip.setString(3, images[i]);
                ip.executeUpdate();
            }
        }
        System.out.println("Seed data complete.");
    }
}