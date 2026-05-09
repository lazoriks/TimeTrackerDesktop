package com.timetracker.db;

import com.timetracker.model.HoursRecord;
import com.timetracker.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static DatabaseManager instance;
    private final String dbUrl;

    /** Production singleton — uses the real on-disk database. */
    private DatabaseManager() {
        this.dbUrl = "jdbc:sqlite:timetracker.db";
    }

    /** Test-only constructor — accepts any JDBC URL (e.g. a temp-file path). */
    DatabaseManager(String customUrl) {
        this.dbUrl = customUrl;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public void initDatabase() {
        String createUsers =
            "CREATE TABLE IF NOT EXISTS Users (" +
            "  User     VARCHAR(150) PRIMARY KEY," +
            "  Password VARCHAR(50)  NOT NULL," +
            "  SuperUser BOOLEAN     NOT NULL DEFAULT 0," +
            "  Email    VARCHAR(150)," +
            "  Mobile   VARCHAR(15)" +
            ")";

        String createHours =
            "CREATE TABLE IF NOT EXISTS Hours (" +
            "  Id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  User    VARCHAR(150) NOT NULL," +
            "  ComeIn  DATETIME," +
            "  ComeOut DATETIME," +
            "  Hour    DECIMAL(10,2)," +
            "  FOREIGN KEY (User) REFERENCES Users(User) ON UPDATE CASCADE ON DELETE CASCADE" +
            ")";

        String insertAdmin =
            "INSERT OR IGNORE INTO Users (User, Password, SuperUser, Email, Mobile)" +
            " VALUES ('admin', 'admin', 1, '', '')";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createHours);
            stmt.execute(insertAdmin);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database: " + e.getMessage(), e);
        }
    }

    // ── Authentication ─────────────────────────────────────────────────────────

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM Users WHERE User = ? AND Password = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Users CRUD ─────────────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Users ORDER BY User")) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getAllUsernames() {
        List<String> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT User FROM Users ORDER BY User")) {
            while (rs.next()) list.add(rs.getString("User"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (User, Password, SuperUser, Email, Mobile) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUser());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.isSuperUser());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getMobile());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user, String originalUsername) {
        String sql = "UPDATE Users SET User=?, Password=?, SuperUser=?, Email=?, Mobile=? WHERE User=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUser());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.isSuperUser());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getMobile());
            ps.setString(6, originalUsername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(String username) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE User=?")) {
            ps.setString(1, username);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Hours CRUD ─────────────────────────────────────────────────────────────

    public List<HoursRecord> getAllHoursRecordsForUser(String username) {
        String sql = "SELECT * FROM Hours WHERE User=? ORDER BY ComeIn DESC";
        List<HoursRecord> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHoursRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HoursRecord getLastHoursRecord(String username) {
        String sql = "SELECT * FROM Hours WHERE User=? ORDER BY ComeIn DESC LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapHoursRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addHoursRecord(HoursRecord record) {
        String sql = "INSERT INTO Hours (User, ComeIn, ComeOut, Hour) VALUES (?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getUser());
            ps.setString(2, record.getComeIn() != null ? record.getComeIn().format(DT_FMT) : null);
            ps.setString(3, record.getComeOut() != null ? record.getComeOut().format(DT_FMT) : null);
            if (record.getComeOut() != null) {
                ps.setDouble(4, record.getHour());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateHoursRecord(HoursRecord record) {
        String sql = "UPDATE Hours SET ComeOut=?, Hour=? WHERE Id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getComeOut() != null ? record.getComeOut().format(DT_FMT) : null);
            ps.setDouble(2, record.getHour());
            ps.setInt(3, record.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Report query ───────────────────────────────────────────────────────────

    public List<HoursRecord> getHoursReport(LocalDate startDate, LocalDate endDate, String username) {
        boolean filterUser = username != null && !username.isEmpty();
        String sql = "SELECT * FROM Hours WHERE ComeIn >= ? AND ComeIn <= ?" +
                     (filterUser ? " AND User=?" : "") +
                     " ORDER BY User, ComeIn";

        List<HoursRecord> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startDate.atStartOfDay().format(DT_FMT));
            ps.setString(2, endDate.atTime(23, 59, 59).format(DT_FMT));
            if (filterUser) ps.setString(3, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHoursRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("User"),
            rs.getString("Password"),
            rs.getBoolean("SuperUser"),
            rs.getString("Email"),
            rs.getString("Mobile")
        );
    }

    private HoursRecord mapHoursRecord(ResultSet rs) throws SQLException {
        String ci = rs.getString("ComeIn");
        String co = rs.getString("ComeOut");
        return new HoursRecord(
            rs.getInt("Id"),
            rs.getString("User"),
            ci != null ? LocalDateTime.parse(ci, DT_FMT) : null,
            co != null ? LocalDateTime.parse(co, DT_FMT) : null,
            rs.getDouble("Hour")
        );
    }
}
