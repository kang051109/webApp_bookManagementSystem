-- 在 MySQL Workbench 中打开并执行 (Ctrl+Shift+Enter)
USE book_management;
UPDATE users SET password_hash = '$2b$10$lb4ukdnZYnGyw4cwOXuKVOjUTx8pw4OHGeSiRR9KtPMLVGAxlHlBO' WHERE username = 'admin';
SELECT password_hash FROM users WHERE username = 'admin';
