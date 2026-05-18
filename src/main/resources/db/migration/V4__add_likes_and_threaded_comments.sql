-- V5: Curtidas e Respostas a Comentários (Compatível com H2)

-- 1. Respostas a Comentários (Threaded Comments)
ALTER TABLE comentarios ADD COLUMN pai_id BIGINT;
ALTER TABLE comentarios ADD CONSTRAINT fk_comentario_pai FOREIGN KEY (pai_id) REFERENCES comentarios(id) ON DELETE CASCADE;

-- 2. Curtidas em Posts
CREATE TABLE posts_curtidas (
    post_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, usuario_id),
    CONSTRAINT fk_curtida_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_curtida_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 3. Curtidas em Comentários
CREATE TABLE comentarios_curtidas (
    comentario_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (comentario_id, usuario_id),
    CONSTRAINT fk_curtida_comentario FOREIGN KEY (comentario_id) REFERENCES comentarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_curtida_usuario_comentario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
