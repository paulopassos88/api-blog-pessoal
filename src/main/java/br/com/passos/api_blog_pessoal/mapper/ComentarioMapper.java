package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.model.Comentario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComentarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "post", ignore = true)
    Comentario toEntity(ComentarioRequest request);

    @Mapping(target = "nomeAutor", source = "autor.nome")
    @Mapping(target = "totalCurtidas", expression = "java(comentario.getCurtidas().size())")
    ComentarioResponse toResponse(Comentario comentario);
}
