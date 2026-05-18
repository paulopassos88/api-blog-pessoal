package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoriaMapper.class, TagMapper.class})
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    Post toEntity(PostRequest request);

    @Mapping(target = "nomeAutor", source = "autor.nome")
    @Mapping(target = "totalCurtidas", expression = "java(post.getCurtidas().size())")
    @Mapping(target = "tempoLeitura", expression = "java(calcularTempoLeitura(post.getConteudo()))")
    PostResponse toResponse(Post post);

    default int calcularTempoLeitura(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) return 0;
        int palavrasPorMinuto = 200;
        String[] palavras = conteudo.trim().split("\\s+");
        return (int) Math.ceil((double) palavras.length / palavrasPorMinuto);
    }
}
