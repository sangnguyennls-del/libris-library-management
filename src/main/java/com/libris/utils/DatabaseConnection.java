package com.libris.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.libris.config.Constants;

/**
 * Singleton: chỉ có một instance trong toàn ứng dụng. Instance được tạo lazy ở lần
 * gọi đầu tiên, khi đó database được khởi tạo (chạy database.sql) đúng một lần.
 * Mỗi lần getConnection() trả về một kết nối JDBC mới để DAO đóng bằng try-with-resources.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private DatabaseConnection() throws SQLException {
        try (Connection conn = openConnection()) {
            initializeDatabase(conn);
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Lấy kết nối cơ sở dữ liệu
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().openConnection();
    }

    private Connection openConnection() throws SQLException {
        System.out.println("[DatabaseConnection] Connecting to: " + Constants.URL);
        return DriverManager.getConnection(Constants.URL, Constants.DB_USER, Constants.DB_PASSWORD);
    }

    /**
     * Huỷ instance Singleton; lần gọi getConnection() kế tiếp sẽ khởi tạo lại database
     */
    public static synchronized void closeConnection() {
        instance = null;
        System.out.println("Đóng kết nối cơ sở dữ liệu thành công!");
    }

    /**
     * Kiểm tra trạng thái kết nối cơ sở dữ liệu
     */
    public static boolean isConnectionOpen() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối database: " + e.getMessage());
            return false;
        }
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Kiểm tra bảng Users tồn tại chưa
            try {
                stmt.executeQuery("SELECT 1 FROM Users LIMIT 1");
                // Bảng đã tồn tại
                return;
            } catch (SQLException e) {
                // Bảng chưa tồn tại, tạo mới
                System.out.println("Đang khởi tạo bảng và dữ liệu mẫu MySQL...");
                executeSqlScript(conn);
                System.out.println("Database MySQL đã được khởi tạo thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo database: " + e.getMessage());
        }
    }

    private static void executeSqlScript(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            InputStream inputStream = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.sql");
            
            if (inputStream == null) {
                System.err.println("Lỗi: Không tìm thấy file database.sql trong resources!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Bỏ qua comment và dòng trống
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                sql.append(line).append("\n");
                // Khi gặp dấu ; thì thực thi câu lệnh
                if (line.trim().endsWith(";")) {
                    String statement = sql.toString();
                    // Bỏ qua các câu lệnh SELECT không cần thiết
                    if (!statement.trim().toUpperCase().startsWith("SELECT")) {
                        try {
                            stmt.execute(statement);
                        } catch (SQLException e) {
                            // Bỏ qua lỗi nếu bảng đã tồn tại
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("Lỗi khi thực thi: " + statement.substring(0, Math.min(100, statement.length())) + " -> " + e.getMessage());
                            }
                        }
                    }
                    sql = new StringBuilder();
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Lỗi thực thi script SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
