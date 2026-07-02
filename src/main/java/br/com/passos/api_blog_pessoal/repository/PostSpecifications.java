package br.com.passos.api_blog_pessoal.repository;

import br.com.passos.api_blog_pessoal.model.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

    private static final char ESCAPE_CHAR = '\\';

    public static Specification<Post> hasTitulo(String titulo) {
        return (root, query, cb) -> {
            if (titulo == null) return null;
            String pattern = "%" + escapeLike(titulo.toLowerCase()) + "%";
            return cb.like(cb.lower(root.get("titulo")), pattern, ESCAPE_CHAR);
        };
    }

    public static Specification<Post> hasConteudo(String conteudo) {
        return (root, query, cb) -> {
            if (conteudo == null) return null;
            String pattern = "%" + escapeLike(conteudo.toLowerCase()) + "%";
            return cb.like(cb.lower(root.get("conteudo")), pattern, ESCAPE_CHAR);
        };
    }

    public static Specification<Post> hasCategoria(String categoriaSlug) {
        return (root, query, cb) -> categoriaSlug == null ? null : cb.equal(root.get("categoria").get("slug"), categoriaSlug);
    }

    public static Specification<Post> hasTagJoin(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null) return null;
            query.distinct(true);
            return cb.equal(root.join("tags").get("nome"), tagName);
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }
}
