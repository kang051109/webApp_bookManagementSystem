package com.example.webapp_project.repository;

import com.example.webapp_project.model.BorrowRecord;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Borrow记录Data access layer - JDBC Implementation (singleton)
 * All writesActionsSupports external Connection for tx control
 */
public class BorrowRecordRepository {

    private static final BorrowRecordRepository INSTANCE = new BorrowRecordRepository();
    private BorrowRecordRepository() {}
    public static BorrowRecordRepository getInstance() { return INSTANCE; }

    /** Save with tx connection */
    public BorrowRecord save(Connection conn, Long userId, Long bookId, LocalDateTime dueDate) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO borrow_records (user_id, book_id, due_date, status) VALUES (?,?,?,'borrowed')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId); ps.setLong(2, bookId);
            ps.setTimestamp(3, Timestamp.valueOf(dueDate));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(conn, keys.getLong(1));
            }
        }
        return null;
    }

    /** Update with tx connectionReturnStatus */
    public void returnBook(Connection conn, Long recordId, LocalDateTime returnDate, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrow_records SET return_date=?, status=? WHERE id=? AND status='borrowed'")) {
            ps.setTimestamp(1, Timestamp.valueOf(returnDate)); ps.setString(2, status); ps.setLong(3, recordId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Return failed: status changed");
        }
    }

    /** Query with tx connection (reads latest in same tx) */
    public BorrowRecord findById(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id WHERE r.id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    /** Independent: find by ID 查询 */
    public BorrowRecord findById(Long id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) { throw new RuntimeException("Failed to query borrow record", e); }
    }

    /** Independent: queryUser的Borrow记录 */
    public List<BorrowRecord> findByUserId(Long userId) {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "WHERE r.user_id=? ORDER BY r.created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query borrow record", e); }
        return list;
    }

    /** Independent: queryAll Records */
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "ORDER BY r.created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("Failed to query borrow record", e); }
        return list;
    }

    /** Tx: queryUseractiveBorrow(re-check in tx) */
    public List<BorrowRecord> findActiveByUserId(Connection conn, Long userId) throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                "WHERE r.user_id=? AND r.status='borrowed' ORDER BY r.created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Independent: queryUseractiveBorrow */
    public List<BorrowRecord> findActiveByUserId(Long userId) {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "WHERE r.user_id=? AND r.status='borrowed' ORDER BY r.created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query borrow record", e); }
        return list;
    }

    /** Independent: countBorrowrecords */
    public long countActive() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='borrowed'")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("Failed to count borrows", e); }
        return 0;
    }

    /** Independent: count逾期数量 */
    public long countOverdue() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='borrowed' AND due_date<NOW()")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("Failed to count overdue records", e); }
        return 0;
    }

    public List<BorrowRecord> findOverdue() {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "WHERE r.status='borrowed' AND r.due_date<NOW() ORDER BY r.due_date ASC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("Failed to query overdue records", e); }
        return list;
    }

    private BorrowRecord map(ResultSet rs) throws SQLException {
        BorrowRecord r = new BorrowRecord();
        r.setId(rs.getLong("id")); r.setUserId(rs.getLong("user_id")); r.setBookId(rs.getLong("book_id"));
        try { r.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        try { r.setUserFullName(rs.getString("user_full_name")); } catch (SQLException ignored) {}
        try { r.setBookTitle(rs.getString("book_title")); } catch (SQLException ignored) {}
        try { r.setBookIsbn(rs.getString("book_isbn")); } catch (SQLException ignored) {}
        r.setStatus(rs.getString("status"));
        Timestamp bd = rs.getTimestamp("borrow_date"); if (bd != null) r.setBorrowDate(bd.toLocalDateTime());
        Timestamp dd = rs.getTimestamp("due_date"); if (dd != null) r.setDueDate(dd.toLocalDateTime());
        Timestamp rd = rs.getTimestamp("return_date"); if (rd != null) r.setReturnDate(rd.toLocalDateTime());
        Timestamp cd = rs.getTimestamp("created_at"); if (cd != null) r.setCreatedAt(cd.toLocalDateTime());
        return r;
    }
}
