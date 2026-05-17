package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findBySlug(String slug);
    boolean existsByNome(String nome);
}
