-- =============================================================
-- Police Quartermaster schema (lookup-driven)
-- Categorization comes from per-domain lookup tables.
-- Script is idempotent: safe to run on a fresh DB or an upgrade.
-- =============================================================

-- ---- Personnel & access -------------------------------------------------

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

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

-- ---- Lookup tables (single-column reference data) -----------------------

CREATE TABLE IF NOT EXISTS equipment_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS weapon_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS calibers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS uniform_sizes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS uniform_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS vehicle_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS storage_locations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ---- Equipment ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS equipment_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    branch ENUM('WEAPON','EQUIPMENT','UNIFORM') NOT NULL DEFAULT 'EQUIPMENT',
    equipment_type_id INT NULL,
    weapon_type_id INT NULL,
    caliber_id INT NULL,
    size_id INT NULL,
    storage_location_id INT NULL,
    serial_number VARCHAR(100) UNIQUE NULL,
    replacement_cost DECIMAL(10,2) NULL,
    `condition` ENUM('NEW','GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD',
    status ENUM('AVAILABLE','ISSUED','MAINTENANCE','RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    is_attachment TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_eq_type    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id) ON DELETE SET NULL,
    CONSTRAINT fk_eq_weapon  FOREIGN KEY (weapon_type_id) REFERENCES weapon_types(id) ON DELETE SET NULL,
    CONSTRAINT fk_eq_caliber FOREIGN KEY (caliber_id) REFERENCES calibers(id) ON DELETE SET NULL,
    CONSTRAINT fk_eq_size    FOREIGN KEY (size_id) REFERENCES uniform_sizes(id) ON DELETE SET NULL,
    CONSTRAINT fk_eq_storage FOREIGN KEY (storage_location_id) REFERENCES storage_locations(id) ON DELETE SET NULL
);

-- Legacy column cleanup: tolerated as no-op on a fresh DB
ALTER TABLE equipment_items DROP COLUMN IF EXISTS category;
ALTER TABLE equipment_items DROP COLUMN IF EXISTS category_id;
DROP TABLE IF EXISTS equipment_categories;

-- Forward-compatible columns when an older equipment_items table exists
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS branch ENUM('WEAPON','EQUIPMENT','UNIFORM') NOT NULL DEFAULT 'EQUIPMENT';
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS equipment_type_id INT NULL;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS weapon_type_id INT NULL;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS caliber_id INT NULL;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS size_id INT NULL;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS storage_location_id INT NULL;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS is_attachment TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS replacement_cost DECIMAL(10,2) NULL;

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
    issued_by_user_id INT NULL,
    issued_date DATE NOT NULL,
    returned_by_user_id INT NULL,
    returned_date DATE NULL,
    CONSTRAINT fk_issuance_officer FOREIGN KEY (officer_id) REFERENCES officers(officer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_issuance_item FOREIGN KEY (item_id) REFERENCES equipment_items(item_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_issuance_issued_by FOREIGN KEY (issued_by_user_id) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_issuance_returned_by FOREIGN KEY (returned_by_user_id) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

ALTER TABLE issuances ADD COLUMN IF NOT EXISTS issued_by_user_id INT NULL;
ALTER TABLE issuances ADD COLUMN IF NOT EXISTS returned_by_user_id INT NULL;

-- ---- Fleet --------------------------------------------------------------

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    unit_number VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    vin VARCHAR(17) UNIQUE NULL,
    plate_number VARCHAR(20) NULL,
    vehicle_type_id INT NULL,
    CONSTRAINT fk_veh_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id) ON DELETE SET NULL
);

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS vehicle_type_id INT NULL;

CREATE TABLE IF NOT EXISTS vehicle_maintenance_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    log_date DATE NOT NULL,
    mileage INT NULL,
    cost DECIMAL(10,2) NULL,
    description TEXT NOT NULL,
    performed_by VARCHAR(100) NULL,
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE vehicle_maintenance_logs ADD COLUMN IF NOT EXISTS cost DECIMAL(10,2) NULL;

-- ---- Seed lookup defaults ----------------------------------------------

INSERT IGNORE INTO equipment_types(name) VALUES
    ('Body Armor'),('Duty Belt'),('Radio'),('Cell Phone'),('Body Camera'),
    ('Flashlight'),('Taser'),('Baton'),('Holster'),('Magazine'),('Optic'),
    ('Light/Laser'),('Sling'),('Suppressor'),('Helmet'),('Medical Kit');

INSERT IGNORE INTO weapon_types(name) VALUES
    ('Handgun'),('Patrol Rifle'),('Shotgun'),('Submachine Gun'),
    ('Less Lethal Launcher'),('Precision Rifle');

INSERT IGNORE INTO calibers(name) VALUES
    ('9mm'),('.40 S&W'),('.45 ACP'),('.380 ACP'),
    ('5.56 NATO'),('.223 Rem'),('7.62 NATO'),('.308 Win'),
    ('12 Gauge'),('.40mm');

INSERT IGNORE INTO uniform_sizes(name) VALUES
    ('XS'),('S'),('M'),('L'),('XL'),('2XL'),('3XL');

INSERT IGNORE INTO uniform_items(name) VALUES
    ('Pants'),('Shirt'),('Boots'),('Shoes'),('Hat/Cap'),('Jacket'),('Rainjacket');

INSERT IGNORE INTO vehicle_types(name) VALUES
    ('Marked Patrol'),('Unmarked'),('K-9 Unit'),('SWAT/Tactical'),
    ('Motorcycle'),('Supervisor'),('Transport Van'),('Detective');

INSERT IGNORE INTO storage_locations(name) VALUES
    ('Main Armory'),('Secondary Armory'),('Quartermaster Cage'),
    ('Locker Room'),('Garage Bay'),('Evidence Room');

INSERT IGNORE INTO app_settings(setting_key, setting_value)
VALUES ('agency_name', 'Police Quartermaster');

-- ---- Seed sample inventory ---------------------------------------------

INSERT IGNORE INTO equipment_items
    (name, branch, equipment_type_id, weapon_type_id, caliber_id, size_id, storage_location_id,
     serial_number, `condition`, status, is_attachment)
VALUES
    (
        'Glock 17 Gen5 - Handgun - 9mm',
        'WEAPON',
        NULL,
        (SELECT id FROM weapon_types WHERE name = 'Handgun' LIMIT 1),
        (SELECT id FROM calibers WHERE name = '9mm' LIMIT 1),
        NULL,
        (SELECT id FROM storage_locations WHERE name = 'Main Armory' LIMIT 1),
        'WPN-G17-0001',
        'GOOD',
        'AVAILABLE',
        0
    ),
    (
        'Glock 17 Gen5 - Handgun - 9mm',
        'WEAPON',
        NULL,
        (SELECT id FROM weapon_types WHERE name = 'Handgun' LIMIT 1),
        (SELECT id FROM calibers WHERE name = '9mm' LIMIT 1),
        NULL,
        (SELECT id FROM storage_locations WHERE name = 'Main Armory' LIMIT 1),
        'WPN-G17-0002',
        'GOOD',
        'AVAILABLE',
        0
    ),
    (
        'Glock 17 Gen5 - Handgun - 9mm',
        'WEAPON',
        NULL,
        (SELECT id FROM weapon_types WHERE name = 'Handgun' LIMIT 1),
        (SELECT id FROM calibers WHERE name = '9mm' LIMIT 1),
        NULL,
        (SELECT id FROM storage_locations WHERE name = 'Main Armory' LIMIT 1),
        'WPN-G17-0003',
        'GOOD',
        'AVAILABLE',
        0
    ),
    (
        'Motorola APX6000 - Radio',
        'EQUIPMENT',
        (SELECT id FROM equipment_types WHERE name = 'Radio' LIMIT 1),
        NULL,
        NULL,
        NULL,
        (SELECT id FROM storage_locations WHERE name = 'Quartermaster Cage' LIMIT 1),
        'EQ-RAD-0101',
        'GOOD',
        'AVAILABLE',
        0
    ),
    (
        'Safariland RDS Optic - Optic',
        'EQUIPMENT',
        (SELECT id FROM equipment_types WHERE name = 'Optic' LIMIT 1),
        NULL,
        NULL,
        NULL,
        (SELECT id FROM storage_locations WHERE name = 'Main Armory' LIMIT 1),
        'ATT-OPT-0001',
        'GOOD',
        'AVAILABLE',
        1
    );

INSERT IGNORE INTO weapon_attachments (weapon_item_id, attachment_item_id)
SELECT w.item_id, a.item_id
FROM equipment_items w
JOIN equipment_items a ON a.serial_number = 'ATT-OPT-0001'
WHERE w.serial_number = 'WPN-G17-0001';
