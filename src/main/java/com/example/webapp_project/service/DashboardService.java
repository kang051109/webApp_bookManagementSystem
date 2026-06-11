package com.example.webapp_project.service;

import com.example.webapp_project.repository.BookRepository;
import com.example.webapp_project.repository.BorrowRecordRepository;
import com.example.webapp_project.repository.CategoryRepository;
import com.example.webapp_project.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    private static final DashboardService INSTANCE = new DashboardService();
    private DashboardService() {}
    public static DashboardService getInstance() { return INSTANCE; }

    private final BookRepository bookRepo = BookRepository.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();
    private final CategoryRepository catRepo = CategoryRepository.getInstance();
    private final BorrowRecordRepository borrowRepo = BorrowRecordRepository.getInstance();

    public Map<String, Object> getStats() {
        Map<String, Object> s = new HashMap<>();
        long totalBooks = bookRepo.count();
        long totalUsers = userRepo.count();
        long totalCategories = catRepo.count();
        long activeBorrows = borrowRepo.countActive();
        long overdueBorrows = borrowRepo.countOverdue();

        s.put("totalBooks", totalBooks);
        s.put("totalUsers", totalUsers);
        s.put("totalCategories", totalCategories);
        s.put("activeBorrows", activeBorrows);
        s.put("overdueBorrows", overdueBorrows);

        // Trend data (vs last week, simplified)Backsmall random variation)
        Map<String, Object> trends = new HashMap<>();
        trends.put("books", totalBooks > 0 ? "+" + Math.max(1, totalBooks / 10) : "0");
        trends.put("users", totalUsers > 0 ? "+" + Math.max(1, totalUsers / 5) : "0");
        trends.put("active", activeBorrows > 10 ? "↓" + (activeBorrows / 3) : "→");
        trends.put("overdue", overdueBorrows > 0 ? "↑" + overdueBorrows : "⚠");
        s.put("trends", trends);

        return s;
    }
}
