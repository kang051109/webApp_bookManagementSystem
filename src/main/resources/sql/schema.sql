CREATE DATABASE IF NOT EXISTS book_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE book_management;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    full_name VARCHAR(100) NOT NULL COMMENT '姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user/admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    isbn VARCHAR(20) NOT NULL UNIQUE COMMENT 'ISBN编号',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(200) NOT NULL COMMENT '作者',
    publisher VARCHAR(200) DEFAULT NULL COMMENT '出版社',
    publish_year INT DEFAULT NULL COMMENT '出版年份',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    total_copies INT NOT NULL DEFAULT 1 COMMENT '总库存量',
    available_copies INT NOT NULL DEFAULT 1 COMMENT '可借数量',
    description TEXT DEFAULT NULL COMMENT '图书描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '借阅日期',
    due_date TIMESTAMP NOT NULL COMMENT '应还日期',
    return_date TIMESTAMP DEFAULT NULL COMMENT '实际归还日期',
    status VARCHAR(20) NOT NULL DEFAULT 'borrowed' COMMENT '状态: borrowed/returned/overdue',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO users (username, password_hash, email, full_name, role) VALUES
('admin', '$2b$10$JVF284mAosvKd40IpwEKqO2LsFp6UQ92.e3A5EpC2nJZrGQfiB8pm', 'admin@book.com', '系统管理员', 'admin')
ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash);

-- 插入示例分类
INSERT INTO categories (name, description) VALUES
('计算机科学', '计算机编程、算法、人工智能等相关书籍'),
('文学', '小说、散文、诗歌等文学作品'),
('历史', '中外历史类书籍'),
('科学', '物理、化学、生物等自然科学类书籍'),
('艺术', '绘画、音乐、设计等艺术类书籍')
ON DUPLICATE KEY UPDATE name=name;
