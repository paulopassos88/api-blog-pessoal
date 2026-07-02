package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId AND c.pai IS NULL ORDER BY c.dataCriacao DESC")
    List<Comentario> findRootByPostId(Long postId);
}
