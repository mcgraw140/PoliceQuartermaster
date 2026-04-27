CREATE TABLE IF NOT EXISTS officers (
    officer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rank VARCHAR(50) NOT NULL,
    badge_number VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'OFFICER') NOT NULL,
    officer_id INT NULL,
    CONSTRAINT fk_users_officer FOREIGN KEY (officer_id) REFERENCES officers(officer_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS equipment_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category ENUM('WEAPON', 'ATTACHMENT', 'UNIFORM', 'TACTICAL_GEAR', 'ELECTRONICS', 'AMMUNITION') NOT NULL,
    serial_number VARCHAR(100) UNIQUE NULL,
    `condition` ENUM('NEW', 'GOOD', 'FAIR', 'POOR') NOT NULL DEFAULT 'GOOD',
    status ENUM('AVAILABLE', 'ISSUED', 'MAINTENANCE', 'RETIRED') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS weapon_attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    weapon_item_id INT NOT NULL,
    attachment_item_id INT NOT NULL UNIQUE,
    CONSTRAINT fk_weapon_attachment_weapon FOREIGN KEY (weapon_item_id) REFERENCES equipment_items(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_weapon_attachment_attachment FOREIGN KEY (attachment_item_id) REFERENCES equipment_items(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS issuances (
    issuance_id INT AUTO_INCREMENT PRIMARY KEY,
    officer_id INT NOT NULL,
    item_id INT NOT NULL,
    issued_date DATE NOT NULL,
    returned_date DATE NULL,
    CONSTRAINT fk_issuance_officer FOREIGN KEY (officer_id) REFERENCES officers(officer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_issuance_item FOREIGN KEY (item_id) REFERENCES equipment_items(item_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    unit_number VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    vin VARCHAR(17) UNIQUE NULL,
    plate_number VARCHAR(20) NULL
);

CREATE TABLE IF NOT EXISTS vehicle_maintenance_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    log_date DATE NOT NULL,
    mileage INT NULL,
    description TEXT NOT NULL,
    performed_by VARCHAR(100) NULL,
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
