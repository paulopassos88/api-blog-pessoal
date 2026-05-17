package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import br.com.passos.api_blog_pessoal.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    Categoria toEntity(CategoriaRequest request);

    CategoriaResponse toResponse(Categoria categoria);
}
