package com.example.webapp_project.repository;

import com.example.webapp_project.model.BorrowRecord;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅记录数据访问层 - JDBC 实现（单例）
 * 所有写操作支持传入外部 Connection 实现事务控制
 */
public class BorrowRecordRepository {

    private static final BorrowRecordRepository INSTANCE = new BorrowRecordRepository();
    private BorrowRecordRepository() {}
    public static BorrowRecordRepository getInstance() { return INSTANCE; }

    /** 使用事务连接保存 */
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

    /** 使用事务连接更新归还状态 */
    public void returnBook(Connection conn, Long recordId, LocalDateTime returnDate, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrow_records SET return_date=?, status=? WHERE id=? AND status='borrowed'")) {
            ps.setTimestamp(1, Timestamp.valueOf(returnDate)); ps.setString(2, status); ps.setLong(3, recordId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("归还失败：借阅记录状态已变更");
        }
    }

    /** 使用事务连接查询（用于同一事务中读取最新数据） */
    public BorrowRecord findById(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id WHERE r.id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    /** 独立连接：根据 ID 查询 */
    public BorrowRecord findById(Long id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) { throw new RuntimeException("查询借阅记录失败", e); }
    }

    /** 独立连接：查询用户的借阅记录 */
    public List<BorrowRecord> findByUserId(Long userId) {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "WHERE r.user_id=? ORDER BY r.created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("查询借阅记录失败", e); }
        return list;
    }

    /** 独立连接：查询全部记录 */
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "ORDER BY r.created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("查询借阅记录失败", e); }
        return list;
    }

    /** 事务连接：查询用户活跃借阅（用于事务内二次检查） */
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

    /** 独立连接：查询用户活跃借阅 */
    public List<BorrowRecord> findActiveByUserId(Long userId) {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT r.*, u.username, u.full_name AS user_full_name, b.title AS book_title, b.isbn AS book_isbn " +
                 "FROM borrow_records r LEFT JOIN users u ON r.user_id=u.id LEFT JOIN books b ON r.book_id=b.id " +
                 "WHERE r.user_id=? AND r.status='borrowed' ORDER BY r.created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("查询借阅记录失败", e); }
        return list;
    }

    /** 独立连接：统计借阅中数量 */
    public long countActive() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='borrowed'")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("统计借阅数失败", e); }
        return 0;
    }

    /** 独立连接：统计逾期数量 */
    public long countOverdue() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='borrowed' AND due_date<NOW()")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("统计逾期数失败", e); }
        return 0;
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
