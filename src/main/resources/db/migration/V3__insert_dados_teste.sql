-- Inserts para a tabela usuarios
INSERT INTO usuarios (nome, email, senha, role, data_criacao, data_atualizacao) VALUES 
('João Silva', 'joao.silva@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Maria Oliveira', 'maria.oliveira@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Carlos Souza', 'carlos.souza@email.com', 'senha123', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ana Costa', 'ana.costa@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pedro Santos', 'pedro.santos@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Julia Rocha', 'julia.rocha@email.com', 'senha123', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Marcos Lima', 'marcos.lima@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Beatriz Gomes', 'beatriz.gomes@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Roberto Dias', 'roberto.dias@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fernanda Alves', 'fernanda.alves@email.com', 'senha123', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserts para a tabela posts (Autores IDs de 1 a 10, mas lembrando que IDs 3 e 6 são ADMIN e validadores barram se tentarem criar via API, mas via SQL é permitido)
INSERT INTO posts (titulo, conteudo, autor_id, data_criacao, data_atualizacao) VALUES 
('Primeiros passos com Spring Boot', 'Conteúdo sobre Spring Boot...', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Entendendo o Hibernate', 'Conteúdo sobre Hibernate...', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dicas de Segurança em APIs', 'Conteúdo sobre Segurança...', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lombok: Produtividade no Java', 'Conteúdo sobre Lombok...', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Testes Unitários com JUnit 5', 'Conteúdo sobre Testes...', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Docker para Desenvolvedores', 'Conteúdo sobre Docker...', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Explorando o ecossistema Spring', 'Conteúdo sobre Spring...', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Java 21: O que há de novo?', 'Conteúdo sobre Java 21...', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Arquitetura de Microserviços', 'Conteúdo sobre Microserviços...', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Clean Code na prática', 'Conteúdo sobre Clean Code...', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserts para a tabela comentarios
INSERT INTO comentarios (texto, autor_id, post_id, data_criacao, data_atualizacao) VALUES 
('Ótimo post!', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Muito esclarecedor.', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tive uma dúvida no passo 2.', 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Excelente explicação.', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Poderia falar mais sobre Mockito?', 7, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ajudou muito no meu projeto.', 8, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('O Java 21 está incrível.', 9, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Microserviços são complexos mas valem a pena.', 10, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dica de ouro!', 3, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Parabéns pelo blog.', 6, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
