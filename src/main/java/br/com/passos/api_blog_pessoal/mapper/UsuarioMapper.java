package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateEntityFromDto(UsuarioUpdateRequest dto, @MappingTarget Usuario usuario);
}
