package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

    public static Specification<Post> hasTitulo(String titulo) {
        return (root, query, cb) -> titulo == null ? null : cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
    }

    public static Specification<Post> hasConteudo(String conteudo) {
        return (root, query, cb) -> conteudo == null ? null : cb.like(cb.lower(root.get("conteudo")), "%" + conteudo.toLowerCase() + "%");
    }

    public static Specification<Post> hasCategoria(String categoriaSlug) {
        return (root, query, cb) -> categoriaSlug == null ? null : cb.equal(root.get("categoria").get("slug"), categoriaSlug);
    }

    public static Specification<Post> hasTag(String tagName) {
        return (root, query, cb) -> tagName == null ? null : cb.isMember(tagName, root.get("tags").get("nome"));
    }
    
    // Custom join for tags if needed, but isMember might work if we have the names.
    // Actually, root.join("tags").get("nome") is better.
    public static Specification<Post> hasTagJoin(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null) return null;
            return cb.equal(root.join("tags").get("nome"), tagName);
        };
    }
}
