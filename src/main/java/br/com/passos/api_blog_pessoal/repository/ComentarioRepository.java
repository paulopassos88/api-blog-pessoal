package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByPostId(Long postId);
}
