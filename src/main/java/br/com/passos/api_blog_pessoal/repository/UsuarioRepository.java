package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY u.dataCriacao DESC")
    List<Usuario> findByNomeContainingIgnoreCaseOrderByDataCriacaoDesc(String nome);
}
