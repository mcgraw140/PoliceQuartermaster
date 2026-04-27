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

CREATE TABLE IF NOT EXISTS equipment_categories (
        category_id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        parent_category_id INT NULL,
        system_key VARCHAR(50) NULL UNIQUE,
        CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES equipment_categories(category_id)
                ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS equipment_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
        category VARCHAR(50) NULL,
        category_id INT NULL,
    serial_number VARCHAR(100) UNIQUE NULL,
    `condition` ENUM('NEW', 'GOOD', 'FAIR', 'POOR') NOT NULL DEFAULT 'GOOD',
    status ENUM('AVAILABLE', 'ISSUED', 'MAINTENANCE', 'RETIRED') NOT NULL DEFAULT 'AVAILABLE'
);

ALTER TABLE equipment_items ADD COLUMN IF NOT EXISTS category_id INT NULL;

ALTER TABLE equipment_items MODIFY category VARCHAR(50) NULL;

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Weapons', NULL, 'WEAPON'
WHERE NOT EXISTS (SELECT 1 FROM equipment_categories WHERE system_key = 'WEAPON');

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Equipment', NULL, 'EQUIPMENT'
WHERE NOT EXISTS (SELECT 1 FROM equipment_categories WHERE system_key = 'EQUIPMENT');

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Uniforms', NULL, 'UNIFORM'
WHERE NOT EXISTS (SELECT 1 FROM equipment_categories WHERE system_key = 'UNIFORM');

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Weapon Attachments', root.category_id, 'ATTACHMENT'
FROM equipment_categories root
WHERE root.system_key = 'WEAPON'
    AND NOT EXISTS (SELECT 1 FROM equipment_categories WHERE system_key = 'ATTACHMENT');

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Duty Belt', root.category_id, NULL
FROM equipment_categories root
WHERE root.system_key = 'EQUIPMENT'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Duty Belt' AND c.parent_category_id = root.category_id
    );

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Vest', root.category_id, NULL
FROM equipment_categories root
WHERE root.system_key = 'EQUIPMENT'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Vest' AND c.parent_category_id = root.category_id
    );

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Electronics', root.category_id, NULL
FROM equipment_categories root
WHERE root.system_key = 'EQUIPMENT'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Electronics' AND c.parent_category_id = root.category_id
    );

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Radio', electronics.category_id, NULL
FROM equipment_categories electronics
WHERE electronics.name = 'Electronics'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Radio' AND c.parent_category_id = electronics.category_id
    );

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Cell Phone', electronics.category_id, NULL
FROM equipment_categories electronics
WHERE electronics.name = 'Electronics'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Cell Phone' AND c.parent_category_id = electronics.category_id
    );

INSERT INTO equipment_categories (name, parent_category_id, system_key)
SELECT 'Shirts', uniforms.category_id, NULL
FROM equipment_categories uniforms
WHERE uniforms.system_key = 'UNIFORM'
    AND NOT EXISTS (
            SELECT 1 FROM equipment_categories c
            WHERE c.name = 'Shirts' AND c.parent_category_id = uniforms.category_id
    );

UPDATE equipment_items ei
JOIN equipment_categories ec ON ec.system_key = CASE
        WHEN ei.category = 'WEAPON' THEN 'WEAPON'
        WHEN ei.category = 'ATTACHMENT' THEN 'ATTACHMENT'
        WHEN ei.category = 'UNIFORM' THEN 'UNIFORM'
        ELSE 'EQUIPMENT'
END
SET ei.category_id = ec.category_id
WHERE ei.category_id IS NULL;

UPDATE equipment_items ei
JOIN equipment_categories ec ON ec.system_key = 'EQUIPMENT'
SET ei.category_id = ec.category_id
WHERE ei.category_id IS NULL;


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
