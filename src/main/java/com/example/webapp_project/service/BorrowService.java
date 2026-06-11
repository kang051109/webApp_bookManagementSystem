package com.example.webapp_project.service;

import com.example.webapp_project.model.Book;
import com.example.webapp_project.model.BorrowRecord;
import com.example.webapp_project.repository.BookRepository;
import com.example.webapp_project.repository.BorrowRecordRepository;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BorrowService - 含手动事务管理（single例）
 * All writesActionsin same tx connection for consistency
 */
public class BorrowService {

    private static final BorrowService INSTANCE = new BorrowService();
    private BorrowService() {}
    public static BorrowService getInstance() { return INSTANCE; }

    private final BorrowRecordRepository borrowRepo = BorrowRecordRepository.getInstance();
    private final BookRepository bookRepo = BookRepository.getInstance();

    private static final int BORROW_DAYS = 14;

    /**
     * BorrowBook (tx-protected + Atomic SQL race-safe)
     */
    public Map<String, Object> borrowBook(Long userId, Long bookId) {
        Book book = bookRepo.findById(bookId);
        if (book == null) throw new IllegalArgumentException("Book not found");
        if (book.getAvailableCopies() <= 0) throw new IllegalArgumentException("No copies available");

        List<BorrowRecord> active = borrowRepo.findActiveByUserId(userId);
        for (BorrowRecord r : active) {
            if (r.getDueDate().isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("You have overdue books. Return them first");
            if (r.getBookId().equals(bookId))
                throw new IllegalArgumentException("You already borrowed this book");
        }

        LocalDateTime dueDate = LocalDateTime.now().plusDays(BORROW_DAYS);
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // In-tx repeat Borrow + overdue re-check (same conn, race-safe)
            List<BorrowRecord> txActive = borrowRepo.findActiveByUserId(conn, userId);
            for (BorrowRecord r : txActive) {
                if (r.getDueDate().isBefore(LocalDateTime.now()))
                    throw new IllegalArgumentException("You have overdue books. Return them first");
                if (r.getBookId().equals(bookId))
                    throw new IllegalArgumentException("You already borrowed this book");
            }

            // InsertBorrow记录
            BorrowRecord record = borrowRepo.save(conn, userId, bookId, dueDate);

            // Atomic扣减Stock
            bookRepo.updateAvailableCopies(conn, bookId, -1);

            conn.commit();

            BorrowRecord full = borrowRepo.findById(record.getId());
            Map<String, Object> r = new HashMap<>();
            r.put("borrowRecord", full);
            return r;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Borrow failed", e);
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException ignored) {}
        }
    }

    /**
     * ReturnBook (tx-protected）
     */
    public Map<String, Object> returnBook(Long recordId, Long userId, boolean isAdmin) {
        // Pre-check (independent conn, read-only)
        BorrowRecord record = borrowRepo.findById(recordId);
        if (record == null) throw new IllegalArgumentException("Borrow record not found");
        if (!isAdmin && !record.getUserId().equals(userId))
            throw new IllegalArgumentException("Not authorized");
        if (!"borrowed".equals(record.getStatus()))
            throw new IllegalArgumentException("Already returned");

        LocalDateTime now = LocalDateTime.now();
        String status = record.getDueDate().isBefore(now) ? "overdue" : "returned";

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Step1: UpdateBorrow记录Status
            borrowRepo.returnBook(conn, recordId, now, status);

            // Step2: RestoreStock
            bookRepo.updateAvailableCopies(conn, record.getBookId(), 1);

            conn.commit();

            // Read final state with tx connectionStatus
            BorrowRecord updated = borrowRepo.findById(conn, recordId);
            Map<String, Object> r = new HashMap<>();
            r.put("borrowRecord", updated);
            return r;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Return failed", e);
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException ignored) {}
        }
    }

    public List<BorrowRecord> getUserHistory(Long userId) { return borrowRepo.findByUserId(userId); }
    public List<BorrowRecord> getAllRecords() { return borrowRepo.findAll(); }
    public List<BorrowRecord> getOverdueRecords() { return borrowRepo.findOverdue(); }
}
