package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNome(String nome);
}
