package br.com.passos.api_blog_pessoal.mapper;

import br.com.passos.api_blog_pessoal.dto.TagRequest;
import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    Tag toEntity(TagRequest request);

    TagResponse toResponse(Tag tag);
}
