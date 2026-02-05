CREATE DATABASE IF NOT EXISTS ocean_view_resort;
USE ocean_view_resort;

CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guests (
    guest_id       INT AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50)  NOT NULL,
    address        VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20)  NOT NULL,
    email          VARCHAR(100),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
    room_id          INT AUTO_INCREMENT PRIMARY KEY,
    room_number      VARCHAR(10)   NOT NULL UNIQUE,
    room_type        ENUM('Standard', 'Deluxe', 'Suite', 'Family') NOT NULL,
    price_per_night  DECIMAL(10,2) NOT NULL,
    is_available     BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id      INT AUTO_INCREMENT PRIMARY KEY,
    reservation_number  VARCHAR(20)   NOT NULL UNIQUE,
    guest_id            INT           NOT NULL,
    room_type           ENUM('Standard', 'Deluxe', 'Suite', 'Family') NOT NULL,
    check_in_date       DATE          NOT NULL,
    check_out_date      DATE          NOT NULL,
    total_amount        DECIMAL(10,2) DEFAULT 0.00,
    status              ENUM('Confirmed', 'Cancelled', 'Checked Out') DEFAULT 'Confirmed',
    created_by          INT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id)   REFERENCES guests(guest_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

INSERT INTO users (username, password, full_name) VALUES
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin User'),
    ('staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'Front Desk Staff')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO rooms (room_number, room_type, price_per_night) VALUES
    ('101', 'Standard',  80.00),
    ('102', 'Standard',  80.00),
    ('103', 'Standard',  80.00),
    ('201', 'Deluxe',   150.00),
    ('202', 'Deluxe',   150.00),
    ('301', 'Suite',    250.00),
    ('401', 'Family',   180.00),
    ('402', 'Family',   180.00)
ON DUPLICATE KEY UPDATE room_number = room_number;

INSERT INTO guests (first_name, last_name, address, contact_number, email) VALUES
    ('Nimal',  'Perera',   '45 Galle Road, Colombo 03',  '0771234567', 'nimal@email.com'),
    ('Sunil',  'Fernando', '12 Temple Road, Kandy',       '0712345678', 'sunil@email.com'),
    ('Amali',  'Silva',    '78 Sea Street, Galle',        '0754321098', 'amali@email.com')
ON DUPLICATE KEY UPDATE guest_id = guest_id;

DROP FUNCTION IF EXISTS fn_room_rate;
DELIMITER $$
CREATE FUNCTION fn_room_rate(p_room_type VARCHAR(20))
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE v_rate DECIMAL(10,2);

    SELECT price_per_night INTO v_rate
    FROM rooms
    WHERE room_type = p_room_type
    ORDER BY room_id
    LIMIT 1;

    RETURN COALESCE(v_rate, 0.00);
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_reservation_total;
DELIMITER $$
CREATE FUNCTION fn_reservation_total(
    p_room_type VARCHAR(20),
    p_check_in  DATE,
    p_check_out DATE
)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE v_nights INT;
    DECLARE v_rate   DECIMAL(10,2);

    SET v_nights = GREATEST(DATEDIFF(p_check_out, p_check_in), 0);
    SET v_rate   = fn_room_rate(p_room_type);

    RETURN v_nights * v_rate;
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_next_reservation_number;
DELIMITER $$
CREATE FUNCTION fn_next_reservation_number()
RETURNS VARCHAR(20)
NOT DETERMINISTIC
BEGIN
    DECLARE v_next_id INT;
    DECLARE v_year    INT;

    SELECT COALESCE(MAX(reservation_id), 0) + 1 INTO v_next_id
    FROM reservations;

    SET v_year = YEAR(CURDATE());

    RETURN CONCAT('RES-', v_year, LPAD(v_next_id, 4, '0'));
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_reservations_before_insert;
DELIMITER $$
CREATE TRIGGER trg_reservations_before_insert
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    IF NEW.reservation_number IS NULL OR TRIM(NEW.reservation_number) = '' THEN
        SET NEW.reservation_number = fn_next_reservation_number();
    END IF;

    IF NEW.status IS NULL OR TRIM(NEW.status) = '' THEN
        SET NEW.status = 'Confirmed';
    END IF;

    SET NEW.total_amount = fn_reservation_total(
        NEW.room_type,
        NEW.check_in_date,
        NEW.check_out_date
    );
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_reservations_before_update;
DELIMITER $$
CREATE TRIGGER trg_reservations_before_update
BEFORE UPDATE ON reservations
FOR EACH ROW
BEGIN
    SET NEW.total_amount = fn_reservation_total(
        NEW.room_type,
        NEW.check_in_date,
        NEW.check_out_date
    );
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_user_find_by_credentials;
DELIMITER $$
CREATE PROCEDURE sp_user_find_by_credentials(
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255)
)
BEGIN
    SELECT user_id, username, password, full_name
    FROM users
    WHERE username = p_username
      AND password = p_password
    LIMIT 1;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_room_get_all;
DELIMITER $$
CREATE PROCEDURE sp_room_get_all()
BEGIN
    SELECT room_id, room_number, room_type, price_per_night, is_available
    FROM rooms
    ORDER BY room_number;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_create;
DELIMITER $$
CREATE PROCEDURE sp_guest_create(
    IN  p_first_name     VARCHAR(50),
    IN  p_last_name      VARCHAR(50),
    IN  p_address        VARCHAR(255),
    IN  p_contact_number VARCHAR(20),
    IN  p_email          VARCHAR(100),
    OUT p_success        BOOLEAN
)
BEGIN
    INSERT INTO guests (first_name, last_name, address, contact_number, email)
    VALUES (p_first_name, p_last_name, p_address, p_contact_number, p_email);

    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_get_all;
DELIMITER $$
CREATE PROCEDURE sp_guest_get_all()
BEGIN
    SELECT guest_id, first_name, last_name, address, contact_number, email
    FROM guests
    ORDER BY last_name, first_name;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_get_by_id;
DELIMITER $$
CREATE PROCEDURE sp_guest_get_by_id(IN p_guest_id INT)
BEGIN
    SELECT guest_id, first_name, last_name, address, contact_number, email
    FROM guests
    WHERE guest_id = p_guest_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_search;
DELIMITER $$
CREATE PROCEDURE sp_guest_search(IN p_query VARCHAR(255))
BEGIN
    DECLARE v_like VARCHAR(260);
    SET v_like = CONCAT('%', p_query, '%');

    SELECT guest_id, first_name, last_name, address, contact_number, email
    FROM guests
    WHERE first_name LIKE v_like
       OR last_name LIKE v_like
       OR contact_number LIKE v_like
       OR CONCAT(first_name, ' ', last_name) LIKE v_like
    ORDER BY last_name, first_name;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_update;
DELIMITER $$
CREATE PROCEDURE sp_guest_update(
    IN  p_guest_id        INT,
    IN  p_first_name      VARCHAR(50),
    IN  p_last_name       VARCHAR(50),
    IN  p_address         VARCHAR(255),
    IN  p_contact_number  VARCHAR(20),
    IN  p_email           VARCHAR(100),
    OUT p_success         BOOLEAN
)
BEGIN
    UPDATE guests
    SET first_name = p_first_name,
        last_name = p_last_name,
        address = p_address,
        contact_number = p_contact_number,
        email = p_email
    WHERE guest_id = p_guest_id;

    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_guest_delete;
DELIMITER $$
CREATE PROCEDURE sp_guest_delete(
    IN  p_guest_id INT,
    OUT p_success  BOOLEAN
)
BEGIN
    DELETE FROM guests WHERE guest_id = p_guest_id;
    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_create;
DELIMITER $$
CREATE PROCEDURE sp_reservation_create(
    IN  p_reservation_number VARCHAR(20),
    IN  p_guest_id           INT,
    IN  p_room_type          VARCHAR(20),
    IN  p_check_in_date      DATE,
    IN  p_check_out_date     DATE,
    IN  p_total_amount       DECIMAL(10,2),
    IN  p_status             VARCHAR(20),
    IN  p_created_by         INT,
    OUT p_success            BOOLEAN
)
BEGIN
    INSERT INTO reservations (
        reservation_number,
        guest_id,
        room_type,
        check_in_date,
        check_out_date,
        total_amount,
        status,
        created_by
    )
    VALUES (
        p_reservation_number,
        p_guest_id,
        p_room_type,
        p_check_in_date,
        p_check_out_date,
        p_total_amount,
        p_status,
        p_created_by
    );

    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_get_all;
DELIMITER $$
CREATE PROCEDURE sp_reservation_get_all()
BEGIN
    SELECT r.reservation_id, r.reservation_number, r.guest_id, r.room_type,
           r.check_in_date, r.check_out_date, r.total_amount, r.status, r.created_by,
           CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
           g.address, g.contact_number, g.email
    FROM reservations r
    LEFT JOIN guests g ON r.guest_id = g.guest_id
    ORDER BY r.created_at DESC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_get_by_id;
DELIMITER $$
CREATE PROCEDURE sp_reservation_get_by_id(IN p_reservation_id INT)
BEGIN
    SELECT r.reservation_id, r.reservation_number, r.guest_id, r.room_type,
           r.check_in_date, r.check_out_date, r.total_amount, r.status, r.created_by,
           CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
           g.address, g.contact_number, g.email
    FROM reservations r
    LEFT JOIN guests g ON r.guest_id = g.guest_id
    WHERE r.reservation_id = p_reservation_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_search;
DELIMITER $$
CREATE PROCEDURE sp_reservation_search(IN p_query VARCHAR(255))
BEGIN
    DECLARE v_like VARCHAR(260);
    SET v_like = CONCAT('%', p_query, '%');

    SELECT r.reservation_id, r.reservation_number, r.guest_id, r.room_type,
           r.check_in_date, r.check_out_date, r.total_amount, r.status, r.created_by,
           CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
           g.address, g.contact_number, g.email
    FROM reservations r
    LEFT JOIN guests g ON r.guest_id = g.guest_id
    WHERE r.reservation_number LIKE v_like
       OR g.contact_number LIKE v_like
       OR CONCAT(g.first_name, ' ', g.last_name) LIKE v_like
    ORDER BY r.created_at DESC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_update;
DELIMITER $$
CREATE PROCEDURE sp_reservation_update(
    IN  p_reservation_id INT,
    IN  p_room_type      VARCHAR(20),
    IN  p_check_in_date  DATE,
    IN  p_check_out_date DATE,
    IN  p_total_amount   DECIMAL(10,2),
    IN  p_status         VARCHAR(20),
    OUT p_success        BOOLEAN
)
BEGIN
    UPDATE reservations
    SET room_type = p_room_type,
        check_in_date = p_check_in_date,
        check_out_date = p_check_out_date,
        total_amount = p_total_amount,
        status = p_status
    WHERE reservation_id = p_reservation_id;

    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reservation_delete;
DELIMITER $$
CREATE PROCEDURE sp_reservation_delete(
    IN  p_reservation_id INT,
    OUT p_success        BOOLEAN
)
BEGIN
    DELETE FROM reservations WHERE reservation_id = p_reservation_id;
    SET p_success = ROW_COUNT() > 0;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_daily_checkins;
DELIMITER $$
CREATE PROCEDURE sp_report_daily_checkins()
BEGIN
    SELECT r.reservation_number,
           CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
           r.room_type,
           r.check_in_date,
           r.check_out_date,
           r.status
    FROM reservations r
    LEFT JOIN guests g ON r.guest_id = g.guest_id
    WHERE r.check_in_date = CURDATE()
    ORDER BY r.reservation_number;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_daily_checkouts;
DELIMITER $$
CREATE PROCEDURE sp_report_daily_checkouts()
BEGIN
    SELECT r.reservation_number,
           CONCAT(g.first_name, ' ', g.last_name) AS guest_name,
           r.room_type,
           r.check_in_date,
           r.check_out_date,
           r.status
    FROM reservations r
    LEFT JOIN guests g ON r.guest_id = g.guest_id
    WHERE r.check_out_date = CURDATE()
    ORDER BY r.reservation_number;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_revenue_by_room_type;
DELIMITER $$
CREATE PROCEDURE sp_report_revenue_by_room_type()
BEGIN
    SELECT room_type, COUNT(*) AS cnt, SUM(total_amount) AS revenue
    FROM reservations
    WHERE status IN ('Confirmed', 'Checked Out')
    GROUP BY room_type
    ORDER BY revenue DESC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_guest_list;
DELIMITER $$
CREATE PROCEDURE sp_report_guest_list()
BEGIN
    SELECT g.guest_id, g.first_name, g.last_name, g.contact_number, g.email,
           COUNT(r.reservation_id) AS reservation_count,
           COALESCE(SUM(
               CASE
                   WHEN r.status IN ('Confirmed', 'Checked Out') THEN r.total_amount
                   ELSE 0
               END
           ), 0) AS total_spent
    FROM guests g
    LEFT JOIN reservations r ON g.guest_id = r.guest_id
    GROUP BY g.guest_id, g.first_name, g.last_name, g.contact_number, g.email
    ORDER BY reservation_count DESC, g.last_name;
END$$
DELIMITER ;
