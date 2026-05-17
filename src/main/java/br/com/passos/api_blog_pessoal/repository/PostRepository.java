package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.dto.PostFeedResponse;
import br.com.passos.api_blog_pessoal.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    List<Post> findByAutorId(Long autorId);

    Optional<Post> findBySlug(String slug);

    @Query("""
            SELECT new br.com.passos.api_blog_pessoal.dto.PostFeedResponse(
                p.id, 
                p.titulo, 
                p.slug,
                SUBSTRING(p.conteudo, 1, 200), 
                p.autor.nome, 
                p.dataCriacao,
                (SELECT COUNT(c) FROM Comentario c WHERE c.post.id = p.id)
            )
            FROM Post p
            WHERE (:lastId IS NULL OR p.id < :lastId)
            ORDER BY p.id DESC
            """)
    Slice<PostFeedResponse> findFeed(Long lastId, Pageable pageable);
}
