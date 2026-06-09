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
 * 借阅业务服务 - 含手动事务管理（单例）
 * 所有写操作在同一个事务连接中完成，确保数据一致性
 */
public class BorrowService {

    private static final BorrowService INSTANCE = new BorrowService();
    private BorrowService() {}
    public static BorrowService getInstance() { return INSTANCE; }

    private final BorrowRecordRepository borrowRepo = BorrowRecordRepository.getInstance();
    private final BookRepository bookRepo = BookRepository.getInstance();

    private static final int BORROW_DAYS = 14;

    /**
     * 借阅图书（事务保护 + 原子 SQL 防竞态）
     */
    public Map<String, Object> borrowBook(Long userId, Long bookId) {
        Book book = bookRepo.findById(bookId);
        if (book == null) throw new IllegalArgumentException("图书不存在");
        if (book.getAvailableCopies() <= 0) throw new IllegalArgumentException("该图书暂无余量可借");

        List<BorrowRecord> active = borrowRepo.findActiveByUserId(userId);
        for (BorrowRecord r : active) {
            if (r.getDueDate().isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("您有逾期图书未还，请先归还后再借阅");
            if (r.getBookId().equals(bookId))
                throw new IllegalArgumentException("您已借阅过此书且尚未归还");
        }

        LocalDateTime dueDate = LocalDateTime.now().plusDays(BORROW_DAYS);
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // 事务内重复借阅 + 逾期二次检查（同连接，防竞态窗口）
            List<BorrowRecord> txActive = borrowRepo.findActiveByUserId(conn, userId);
            for (BorrowRecord r : txActive) {
                if (r.getDueDate().isBefore(LocalDateTime.now()))
                    throw new IllegalArgumentException("您有逾期图书未还，请先归还后再借阅");
                if (r.getBookId().equals(bookId))
                    throw new IllegalArgumentException("您已借阅过此书且尚未归还");
            }

            // 写入借阅记录
            BorrowRecord record = borrowRepo.save(conn, userId, bookId, dueDate);

            // 原子扣减库存
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
            throw new RuntimeException("借阅操作失败", e);
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException ignored) {}
        }
    }

    /**
     * 归还图书（事务保护）
     */
    public Map<String, Object> returnBook(Long recordId, Long userId, boolean isAdmin) {
        // 预检查（独立连接，只读）
        BorrowRecord record = borrowRepo.findById(recordId);
        if (record == null) throw new IllegalArgumentException("借阅记录不存在");
        if (!isAdmin && !record.getUserId().equals(userId))
            throw new IllegalArgumentException("无权操作此借阅记录");
        if (!"borrowed".equals(record.getStatus()))
            throw new IllegalArgumentException("该记录已归还，无需重复操作");

        LocalDateTime now = LocalDateTime.now();
        String status = record.getDueDate().isBefore(now) ? "overdue" : "returned";

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // 步骤1: 更新借阅记录状态
            borrowRepo.returnBook(conn, recordId, now, status);

            // 步骤2: 恢复库存
            bookRepo.updateAvailableCopies(conn, record.getBookId(), 1);

            conn.commit();

            // 用事务连接读取最终状态
            BorrowRecord updated = borrowRepo.findById(conn, recordId);
            Map<String, Object> r = new HashMap<>();
            r.put("borrowRecord", updated);
            return r;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("归还操作失败", e);
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException ignored) {}
        }
    }

    public List<BorrowRecord> getUserHistory(Long userId) { return borrowRepo.findByUserId(userId); }
    public List<BorrowRecord> getAllRecords() { return borrowRepo.findAll(); }
}
