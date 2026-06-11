package com.example.webapp_project.service;

import com.example.webapp_project.model.Book;
import com.example.webapp_project.repository.BookRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书Service（single例）
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
        if (book == null) throw new IllegalArgumentException("Book not found");
        return book;
    }

    public Book create(Book book) {
        validate(book);
        if (repo.findByIsbn(book.getIsbn()) != null) throw new IllegalArgumentException("ISBN already exists");
        if (book.getTotalCopies() == null || book.getTotalCopies() < 1) book.setTotalCopies(1);
        if (book.getAvailableCopies() == null) book.setAvailableCopies(book.getTotalCopies());
        if (book.getAvailableCopies() > book.getTotalCopies()) book.setAvailableCopies(book.getTotalCopies());
        return repo.save(book);
    }

    public Book update(Long id, Book book) {
        validate(book);
        Book existing = repo.findById(id);
        if (existing == null) throw new IllegalArgumentException("Book not found");
        Book dup = repo.findByIsbn(book.getIsbn());
        if (dup != null && !dup.getId().equals(id)) throw new IllegalArgumentException("ISBN already used by another book");

        int borrowed = existing.getTotalCopies() - existing.getAvailableCopies();

        // Must checkTotal Copiescannot be less than borrowed
        int newTotal = (book.getTotalCopies() != null && book.getTotalCopies() >= 1)
                ? book.getTotalCopies() : existing.getTotalCopies();
        if (newTotal < borrowed)
            throw new IllegalArgumentException("Total copies cannot be less than borrowed (" + borrowed + ")");
        book.setTotalCopies(newTotal);

        // Available数量 = max(Usergiven value, borrowed), max not exceeding total totalCopies
        if (book.getAvailableCopies() == null) {
            // When unspecified: maintain proportion, minborrowedcopies
            int implied = existing.getAvailableCopies() + (newTotal - existing.getTotalCopies());
            book.setAvailableCopies(Math.max(borrowed, Math.min(newTotal, implied)));
        } else {
            if (book.getAvailableCopies() < borrowed)
                throw new IllegalArgumentException("Available copies cannot be less than borrowed (" + borrowed + ")");
            if (book.getAvailableCopies() > newTotal)
                book.setAvailableCopies(newTotal);
        }

        book.setId(id);
        return repo.update(book);
    }

    public void delete(Long id) {
        if (repo.findById(id) == null) throw new IllegalArgumentException("Book not found");
        repo.delete(id);
    }

    private void validate(Book book) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) throw new IllegalArgumentException("ISBN is required");
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) throw new IllegalArgumentException("Title is required");
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) throw new IllegalArgumentException("Author is required");
        book.setIsbn(book.getIsbn().trim()); book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        if (book.getPublisher() != null) book.setPublisher(book.getPublisher().trim());
    }
}
