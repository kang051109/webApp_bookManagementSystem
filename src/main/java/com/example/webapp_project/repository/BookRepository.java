package com.example.webapp_project.repository;

import com.example.webapp_project.model.Book;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Book data access layer - JDBC Implementation (singleton)
 */
public class BookRepository {

    private static final BookRepository INSTANCE = new BookRepository();
    private BookRepository() {}
    public static BookRepository getInstance() { return INSTANCE; }

    public List<Book> findPage(int page, int size, String keyword, Long categoryId) {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder(
            "SELECT b.*, c.name AS category_name FROM books b LEFT JOIN categories c ON b.category_id=c.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR b.author LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw); params.add(kw);
        }
        if (categoryId != null) { sql.append(" AND b.category_id=?"); params.add(categoryId); }
        sql.append(" ORDER BY b.created_at DESC LIMIT ? OFFSET ?");
        params.add(size); params.add(offset);

        List<Book> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("Failed to list books", e); }
        return list;
    }

    public long count(String keyword, Long categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books b WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR b.author LIKE ?)");
            String kw = "%" + keyword.trim() + "%"; params.add(kw); params.add(kw);
        }
        if (categoryId != null) { sql.append(" AND b.category_id=?"); params.add(categoryId); }
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        } catch (SQLException e) { throw new RuntimeException("Failed to count books", e); }
        return 0;
    }

    public Book findById(Long id) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT b.*, c.name AS category_name FROM books b LEFT JOIN categories c ON b.category_id=c.id WHERE b.id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query book", e); }
        return null;
    }

    public Book findByIsbn(String isbn) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT b.*, c.name AS category_name FROM books b LEFT JOIN categories c ON b.category_id=c.id WHERE b.isbn=?")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query book", e); }
        return null;
    }

    public Book save(Book book) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO books (isbn,title,author,publisher,publish_year,category_id,total_copies,available_copies,description) VALUES (?,?,?,?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn()); ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor()); ps.setString(4, book.getPublisher());
            if (book.getPublishYear() != null) ps.setInt(5, book.getPublishYear()); else ps.setNull(5, Types.INTEGER);
            if (book.getCategoryId() != null) ps.setLong(6, book.getCategoryId()); else ps.setNull(6, Types.BIGINT);
            ps.setInt(7, book.getTotalCopies() != null ? book.getTotalCopies() : 1);
            ps.setInt(8, book.getAvailableCopies() != null ? book.getAvailableCopies() : 1);
            ps.setString(9, book.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return findById(keys.getLong(1)); }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("ISBN already exists");
            throw new RuntimeException("Failed to create book", e);
        }
        return null;
    }

    public Book update(Book book) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE books SET isbn=?,title=?,author=?,publisher=?,publish_year=?,category_id=?,total_copies=?,available_copies=?,description=? WHERE id=?")) {
            ps.setString(1, book.getIsbn()); ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor()); ps.setString(4, book.getPublisher());
            if (book.getPublishYear() != null) ps.setInt(5, book.getPublishYear()); else ps.setNull(5, Types.INTEGER);
            if (book.getCategoryId() != null) ps.setLong(6, book.getCategoryId()); else ps.setNull(6, Types.BIGINT);
            ps.setInt(7, book.getTotalCopies() != null ? book.getTotalCopies() : 1);
            ps.setInt(8, book.getAvailableCopies() != null ? book.getAvailableCopies() : 1);
            ps.setString(9, book.getDescription()); ps.setLong(10, book.getId());
            if (ps.executeUpdate() > 0) return findById(book.getId());
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("ISBN already exists");
            throw new RuntimeException("Failed to update book", e);
        }
        return null;
    }

    public boolean delete(Long id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Check+delete in same connection+Delete, eliminatingTOCTOU
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM borrow_records WHERE book_id=? AND status='borrowed'")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0)
                        throw new IllegalStateException("Cannot delete: book has active borrows");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?")) {
                ps.setLong(1, id); return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    /** Independent connection: updateAvailablequantity (non-tx) */
    public void updateAvailableCopies(Long bookId, int delta) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            updateAvailableCopies(conn, bookId, delta);
        } catch (SQLException e) { throw new RuntimeException("Failed to update book quantity", e); }
    }

    /** Tx connection: atomic decrementAvailablequantity (race-safe)items) */
    public void updateAvailableCopies(Connection conn, Long bookId, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET available_copies = available_copies + ? WHERE id=? AND available_copies + ? >= 0")) {
            ps.setInt(1, delta); ps.setLong(2, bookId); ps.setInt(3, delta);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insufficient stock");
        }
    }

    public long count() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("Failed to count books", e); }
        return 0;
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getLong("id")); b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title")); b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear((Integer) rs.getObject("publish_year"));
        b.setCategoryId((Long) rs.getObject("category_id"));
        try { b.setCategoryName(rs.getString("category_name")); } catch (SQLException ignored) {}
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setDescription(rs.getString("description"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) b.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) b.setUpdatedAt(ut.toLocalDateTime());
        return b;
    }
}
