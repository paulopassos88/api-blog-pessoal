package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    Post toEntity(PostRequest request);

    @Mapping(target = "nomeAutor", source = "autor.nome")
    PostResponse toResponse(Post post);
}
