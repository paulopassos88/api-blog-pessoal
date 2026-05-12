CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    conteudo TEXT NOT NULL,
    autor_id BIGINT NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_autor FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);

CREATE TABLE comentarios (
    id BIGSERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    autor_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_comentario_autor FOREIGN KEY (autor_id) REFERENCES usuarios(id),
    CONSTRAINT fk_comentario_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
