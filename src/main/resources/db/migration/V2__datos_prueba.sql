INSERT INTO categoria (nombre, slug) VALUES
  ('Remeras', 'remeras'),
  ('Accesorios', 'accesorios');

INSERT INTO producto (nombre, descripcion, precio, activo, categoria_id) VALUES
  ('Remera básica', 'Algodón 100%, varios colores.', 12500.00, TRUE,
     (SELECT id FROM categoria WHERE slug = 'remeras')),
  ('Remera estampada', 'Diseño exclusivo, edición limitada.', 15900.00, TRUE,
     (SELECT id FROM categoria WHERE slug = 'remeras')),
  ('Gorra', 'Ajustable, visera curva.', 8900.00, TRUE,
     (SELECT id FROM categoria WHERE slug = 'accesorios'));