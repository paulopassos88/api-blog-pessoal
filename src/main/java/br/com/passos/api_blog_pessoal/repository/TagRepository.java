package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNome(String nome);
}
