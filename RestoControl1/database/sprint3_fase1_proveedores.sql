USE sistema_restaurante2;

CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    ruc CHAR(11) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(30) NOT NULL DEFAULT '',
    correo VARCHAR(150) NOT NULL DEFAULT '',
    estado VARCHAR(20) NOT NULL DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS insumo_proveedor (
    id_insumo INT NOT NULL,
    id_proveedor INT NOT NULL,
    PRIMARY KEY (id_insumo, id_proveedor),
    FOREIGN KEY (id_insumo) REFERENCES insumo(id_insumo),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
