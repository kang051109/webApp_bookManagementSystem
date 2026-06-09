package com.example.webapp_project.service;

import com.example.webapp_project.model.Book;
import com.example.webapp_project.repository.BookRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书业务服务（单例）
 */
public class BookService {

    private static final BookService INSTANCE = new BookService();
    private BookService() {}
    public static BookService getInstance() { return INSTANCE; }

    private final BookRepository repo = BookRepository.getInstance();

    public Map<String, Object> findPage(int page, int size, String keyword, Long categoryId) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        List<Book> books = repo.findPage(page, size, keyword, categoryId);
        long total = repo.count(keyword, categoryId);
        long totalPages = (total + size - 1) / size;
        Map<String, Object> r = new HashMap<>();
        r.put("books", books); r.put("page", page); r.put("size", size);
        r.put("total", total); r.put("totalPages", totalPages);
        return r;
    }

    public Book findById(Long id) {
        Book book = repo.findById(id);
        if (book == null) throw new IllegalArgumentException("图书不存在");
        return book;
    }

    public Book create(Book book) {
        validate(book);
        if (repo.findByIsbn(book.getIsbn()) != null) throw new IllegalArgumentException("ISBN 已存在");
        if (book.getTotalCopies() == null || book.getTotalCopies() < 1) book.setTotalCopies(1);
        if (book.getAvailableCopies() == null) book.setAvailableCopies(book.getTotalCopies());
        if (book.getAvailableCopies() > book.getTotalCopies()) book.setAvailableCopies(book.getTotalCopies());
        return repo.save(book);
    }

    public Book update(Long id, Book book) {
        validate(book);
        Book existing = repo.findById(id);
        if (existing == null) throw new IllegalArgumentException("图书不存在");
        Book dup = repo.findByIsbn(book.getIsbn());
        if (dup != null && !dup.getId().equals(id)) throw new IllegalArgumentException("ISBN 已被其他图书使用");

        int borrowed = existing.getTotalCopies() - existing.getAvailableCopies();

        // 必须先检查总库存不能小于已借出数量
        int newTotal = (book.getTotalCopies() != null && book.getTotalCopies() >= 1)
                ? book.getTotalCopies() : existing.getTotalCopies();
        if (newTotal < borrowed)
            throw new IllegalArgumentException("总库存不能低于已借出数量(" + borrowed + ")");
        book.setTotalCopies(newTotal);

        // 可借数量 = max(用户指定的值, borrowed)，且不能超过 totalCopies
        if (book.getAvailableCopies() == null) {
            // 未指定时：保持原比例，但至少要有borrowed本
            int implied = existing.getAvailableCopies() + (newTotal - existing.getTotalCopies());
            book.setAvailableCopies(Math.max(borrowed, Math.min(newTotal, implied)));
        } else {
            if (book.getAvailableCopies() < borrowed)
                throw new IllegalArgumentException("可借数量不能低于已借出数量(" + borrowed + ")");
            if (book.getAvailableCopies() > newTotal)
                book.setAvailableCopies(newTotal);
        }

        book.setId(id);
        return repo.update(book);
    }

    public void delete(Long id) {
        if (repo.findById(id) == null) throw new IllegalArgumentException("图书不存在");
        repo.delete(id);
    }

    private void validate(Book book) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) throw new IllegalArgumentException("ISBN 不能为空");
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) throw new IllegalArgumentException("书名不能为空");
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) throw new IllegalArgumentException("作者不能为空");
        book.setIsbn(book.getIsbn().trim()); book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        if (book.getPublisher() != null) book.setPublisher(book.getPublisher().trim());
    }
}
