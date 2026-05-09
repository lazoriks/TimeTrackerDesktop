package com.timetracker.db;

import com.timetracker.model.HoursRecord;
import com.timetracker.model.User;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseManager")
class DatabaseManagerTest {

    private DatabaseManager db;
    private Path tempDb;

    // ── Fixtures ───────────────────────────────────────────────────────────────

    private static final LocalDateTime CHECK_IN_1  = LocalDateTime.of(2026, 5, 1,  9,  0, 0);
    private static final LocalDateTime CHECK_OUT_1 = LocalDateTime.of(2026, 5, 1,  17, 30, 0);
    private static final LocalDateTime CHECK_IN_2  = LocalDateTime.of(2026, 5, 2,  8,  0, 0);
    private static final LocalDateTime CHECK_OUT_2 = LocalDateTime.of(2026, 5, 2,  16, 0, 0);

    @BeforeEach
    void setUp() throws Exception {
        tempDb = Files.createTempFile("tt_test_", ".db");
        db = new DatabaseManager("jdbc:sqlite:" + tempDb.toAbsolutePath());
        db.initDatabase();
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(tempDb);
    }

    // ── Init ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("initDatabase creates the default admin user")
    void init_adminUserExists() {
        User admin = db.authenticate("admin", "admin");
        assertNotNull(admin, "admin user should exist after init");
    }

    @Test
    @DisplayName("default admin is a super user")
    void init_adminIsSuperUser() {
        User admin = db.authenticate("admin", "admin");
        assertNotNull(admin);
        assertTrue(admin.isSuperUser());
    }

    @Test
    @DisplayName("calling initDatabase twice is idempotent")
    void init_calledTwice_doesNotThrow() {
        assertDoesNotThrow(() -> db.initDatabase());
        // Admin still there (INSERT OR IGNORE)
        assertNotNull(db.authenticate("admin", "admin"));
    }

    // ── Authentication ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("authenticate returns User for valid credentials")
    void authenticate_validCredentials_returnsUser() {
        db.addUser(new User("alice", "pw123", false, "a@b.com", ""));
        User u = db.authenticate("alice", "pw123");
        assertNotNull(u);
        assertEquals("alice", u.getUser());
    }

    @Test
    @DisplayName("authenticate returns null for wrong password")
    void authenticate_wrongPassword_returnsNull() {
        db.addUser(new User("alice", "pw123", false, "", ""));
        assertNull(db.authenticate("alice", "wrong"));
    }

    @Test
    @DisplayName("authenticate returns null for unknown user")
    void authenticate_unknownUser_returnsNull() {
        assertNull(db.authenticate("nobody", "any"));
    }

    // ── Users CRUD ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addUser then getAllUsers returns the added user")
    void addUser_appearsInGetAll() {
        db.addUser(new User("bob", "pass", false, "bob@x.com", "555"));
        List<User> users = db.getAllUsers();
        assertTrue(users.stream().anyMatch(u -> u.getUser().equals("bob")));
    }

    @Test
    @DisplayName("getAllUsers returns users in alphabetical order")
    void getAllUsers_alphabeticalOrder() {
        db.addUser(new User("zara", "pw", false, "", ""));
        db.addUser(new User("anna", "pw", false, "", ""));
        db.addUser(new User("mike", "pw", false, "", ""));

        List<User> users = db.getAllUsers();
        // admin always first alphabetically; check the three new users are ordered
        List<String> names = users.stream().map(User::getUser).toList();
        assertTrue(names.indexOf("admin") < names.indexOf("anna"));
        assertTrue(names.indexOf("anna")  < names.indexOf("mike"));
        assertTrue(names.indexOf("mike")  < names.indexOf("zara"));
    }

    @Test
    @DisplayName("addUser with duplicate username returns false")
    void addUser_duplicateUsername_returnsFalse() {
        db.addUser(new User("carol", "pw", false, "", ""));
        boolean second = db.addUser(new User("carol", "other", false, "", ""));
        assertFalse(second);
    }

    @Test
    @DisplayName("getAllUsernames returns only the User column values")
    void getAllUsernames_returnsCorrectNames() {
        db.addUser(new User("dan", "pw", false, "", ""));
        List<String> names = db.getAllUsernames();
        assertTrue(names.contains("dan"));
        assertTrue(names.contains("admin"));
    }

    @Test
    @DisplayName("updateUser changes all editable fields")
    void updateUser_changesFields() {
        db.addUser(new User("eve", "old", false, "old@e.com", "111"));
        db.updateUser(new User("eve", "new", true, "new@e.com", "999"), "eve");

        User u = db.authenticate("eve", "new");
        assertNotNull(u);
        assertTrue(u.isSuperUser());
        assertEquals("new@e.com", u.getEmail());
        assertEquals("999",       u.getMobile());
    }

    @Test
    @DisplayName("updateUser can rename the username")
    void updateUser_renamesUsername() {
        db.addUser(new User("frank", "pw", false, "", ""));
        db.updateUser(new User("franklin", "pw", false, "", ""), "frank");

        assertNull(db.authenticate("frank",    "pw"), "old name should not exist");
        assertNotNull(db.authenticate("franklin", "pw"), "new name should exist");
    }

    @Test
    @DisplayName("deleteUser removes the user from Users")
    void deleteUser_removesUser() {
        db.addUser(new User("grace", "pw", false, "", ""));
        db.deleteUser("grace");
        assertNull(db.authenticate("grace", "pw"));
    }

    @Test
    @DisplayName("deleteUser cascades to Hours records")
    void deleteUser_cascadesToHours() {
        db.addUser(new User("henry", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "henry", CHECK_IN_1, null, 0));

        db.deleteUser("henry");

        // getAllHoursRecordsForUser should return empty (user gone, cascade deleted)
        List<HoursRecord> records = db.getAllHoursRecordsForUser("henry");
        assertTrue(records.isEmpty());
    }

    // ── Hours CRUD ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addHoursRecord saves a check-in (no check-out)")
    void addHoursRecord_checkIn_saved() {
        db.addUser(new User("ivan", "pw", false, "", ""));
        boolean ok = db.addHoursRecord(new HoursRecord(0, "ivan", CHECK_IN_1, null, 0));
        assertTrue(ok);

        HoursRecord last = db.getLastHoursRecord("ivan");
        assertNotNull(last);
        assertEquals(CHECK_IN_1, last.getComeIn());
        assertNull(last.getComeOut());
    }

    @Test
    @DisplayName("updateHoursRecord sets ComeOut and Hour")
    void updateHoursRecord_setsCheckOutAndHour() {
        db.addUser(new User("julia", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "julia", CHECK_IN_1, null, 0));

        HoursRecord rec = db.getLastHoursRecord("julia");
        rec.setComeOut(CHECK_OUT_1);
        rec.setHour(8.50);
        db.updateHoursRecord(rec);

        HoursRecord updated = db.getLastHoursRecord("julia");
        assertEquals(CHECK_OUT_1, updated.getComeOut());
        assertEquals(8.50, updated.getHour(), 0.001);
    }

    @Test
    @DisplayName("getLastHoursRecord returns null when no records exist")
    void getLastHoursRecord_noRecords_returnsNull() {
        db.addUser(new User("kevin", "pw", false, "", ""));
        assertNull(db.getLastHoursRecord("kevin"));
    }

    @Test
    @DisplayName("getLastHoursRecord returns the most recent record")
    void getLastHoursRecord_returnsMostRecent() {
        db.addUser(new User("laura", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "laura", CHECK_IN_1,  CHECK_OUT_1, 8.50));
        db.addHoursRecord(new HoursRecord(0, "laura", CHECK_IN_2,  null,         0));

        HoursRecord last = db.getLastHoursRecord("laura");
        assertEquals(CHECK_IN_2, last.getComeIn());
        assertNull(last.getComeOut());
    }

    @Test
    @DisplayName("getAllHoursRecordsForUser returns records in reverse chronological order")
    void getAllHoursRecordsForUser_reverseOrder() {
        db.addUser(new User("mark", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "mark", CHECK_IN_1, CHECK_OUT_1, 8.50));
        db.addHoursRecord(new HoursRecord(0, "mark", CHECK_IN_2, null,        0));

        List<HoursRecord> list = db.getAllHoursRecordsForUser("mark");
        assertEquals(2, list.size());
        // Most recent first
        assertEquals(CHECK_IN_2, list.get(0).getComeIn());
        assertEquals(CHECK_IN_1, list.get(1).getComeIn());
    }

    @Test
    @DisplayName("getAllHoursRecordsForUser does not return records of other users")
    void getAllHoursRecordsForUser_isolatedByUser() {
        db.addUser(new User("nina",  "pw", false, "", ""));
        db.addUser(new User("oscar", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "nina",  CHECK_IN_1, null, 0));
        db.addHoursRecord(new HoursRecord(0, "oscar", CHECK_IN_2, null, 0));

        List<HoursRecord> ninaRecords = db.getAllHoursRecordsForUser("nina");
        assertTrue(ninaRecords.stream().allMatch(r -> r.getUser().equals("nina")));
    }

    // ── Report ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getHoursReport filters by date range (ComeIn)")
    void getHoursReport_filtersByDateRange() {
        db.addUser(new User("peter", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "peter", CHECK_IN_1, CHECK_OUT_1, 8.50)); // May 1
        db.addHoursRecord(new HoursRecord(0, "peter", CHECK_IN_2, CHECK_OUT_2, 8.00)); // May 2

        List<HoursRecord> may1Only = db.getHoursReport(
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1), "peter");
        assertEquals(1, may1Only.size());
        assertEquals(CHECK_IN_1, may1Only.get(0).getComeIn());
    }

    @Test
    @DisplayName("getHoursReport with empty username returns all users")
    void getHoursReport_emptyUsername_allUsers() {
        db.addUser(new User("quinn", "pw", false, "", ""));
        db.addUser(new User("rose",  "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "quinn", CHECK_IN_1, CHECK_OUT_1, 8.50));
        db.addHoursRecord(new HoursRecord(0, "rose",  CHECK_IN_2, CHECK_OUT_2, 8.00));

        List<HoursRecord> all = db.getHoursReport(
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2), "");
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("getHoursReport filters by specific user")
    void getHoursReport_filtersByUser() {
        db.addUser(new User("sam",  "pw", false, "", ""));
        db.addUser(new User("tina", "pw", false, "", ""));
        db.addHoursRecord(new HoursRecord(0, "sam",  CHECK_IN_1, CHECK_OUT_1, 8.50));
        db.addHoursRecord(new HoursRecord(0, "tina", CHECK_IN_2, CHECK_OUT_2, 8.00));

        List<HoursRecord> samOnly = db.getHoursReport(
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2), "sam");
        assertEquals(1, samOnly.size());
        assertEquals("sam", samOnly.get(0).getUser());
    }

    @Test
    @DisplayName("getHoursReport returns empty list when no records match")
    void getHoursReport_noMatch_returnsEmpty() {
        List<HoursRecord> result = db.getHoursReport(
            LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 31), "");
        assertTrue(result.isEmpty());
    }
}
