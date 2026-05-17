-- V4: Seed de Dados Fictícios (H2 Friendly)

-- 1. Inserir Usuário (Autor) - Necessário para satisfazer FK em posts e comentarios
INSERT INTO usuarios (nome, email, senha, role, data_criacao, data_atualizacao) VALUES 
('Admin Blog', 'admin@blog.com', '123456', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Categorias
INSERT INTO categorias (nome, slug) VALUES 
('Tecnologia', 'tecnologia'),
('Programação', 'programacao'),
('Carreira', 'carreira'),
('DevOps', 'devops'),
('Design', 'design');

-- 3. Tags
INSERT INTO tags (nome) VALUES 
('Java'), ('Spring Boot'), ('React'), ('Docker'), ('Kubernetes');

-- 4. Posts
-- Usando subquery para pegar o ID do usuário e das categorias para garantir integridade no H2
INSERT INTO posts (titulo, slug, conteudo, autor_id, categoria_id, data_criacao, data_atualizacao)
SELECT 'Post Fictício 1', 'post-ficticio-1', 'Conteúdo do post 1...', u.id, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuarios u, categorias c WHERE u.email = 'admin@blog.com' AND c.slug = 'tecnologia';

INSERT INTO posts (titulo, slug, conteudo, autor_id, categoria_id, data_criacao, data_atualizacao)
SELECT 'Post Fictício 2', 'post-ficticio-2', 'Conteúdo do post 2...', u.id, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuarios u, categorias c WHERE u.email = 'admin@blog.com' AND c.slug = 'programacao';

INSERT INTO posts (titulo, slug, conteudo, autor_id, categoria_id, data_criacao, data_atualizacao)
SELECT 'Post Fictício 3', 'post-ficticio-3', 'Conteúdo do post 3...', u.id, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuarios u, categorias c WHERE u.email = 'admin@blog.com' AND c.slug = 'carreira';

INSERT INTO posts (titulo, slug, conteudo, autor_id, categoria_id, data_criacao, data_atualizacao)
SELECT 'Post Fictício 4', 'post-ficticio-4', 'Conteúdo do post 4...', u.id, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuarios u, categorias c WHERE u.email = 'admin@blog.com' AND c.slug = 'devops';

INSERT INTO posts (titulo, slug, conteudo, autor_id, categoria_id, data_criacao, data_atualizacao)
SELECT 'Post Fictício 5', 'post-ficticio-5', 'Conteúdo do post 5...', u.id, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuarios u, categorias c WHERE u.email = 'admin@blog.com' AND c.slug = 'design';

-- 5. Associações Posts <-> Tags
INSERT INTO posts_tags (post_id, tag_id)
SELECT p.id, t.id FROM posts p, tags t 
WHERE p.slug = 'post-ficticio-1' AND t.nome = 'Java';

INSERT INTO posts_tags (post_id, tag_id)
SELECT p.id, t.id FROM posts p, tags t 
WHERE p.slug = 'post-ficticio-2' AND t.nome = 'Spring Boot';

-- 6. Comentários
INSERT INTO comentarios (texto, autor_id, post_id, data_criacao, data_atualizacao)
SELECT 'Primeiro comentário teste', u.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
FROM usuarios u, posts p WHERE u.email = 'admin@blog.com' AND p.slug = 'post-ficticio-1';
