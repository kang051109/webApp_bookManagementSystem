package com.example.webapp_project.service;

import com.example.webapp_project.repository.BookRepository;
import com.example.webapp_project.repository.BorrowRecordRepository;
import com.example.webapp_project.repository.CategoryRepository;
import com.example.webapp_project.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表板统计服务（单例）
 */
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
        s.put("totalBooks", bookRepo.count());
        s.put("totalUsers", userRepo.count());
        s.put("totalCategories", catRepo.count());
        s.put("activeBorrows", borrowRepo.countActive());
        s.put("overdueBorrows", borrowRepo.countOverdue());
        return s;
    }
}
