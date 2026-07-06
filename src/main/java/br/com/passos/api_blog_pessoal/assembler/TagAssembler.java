package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.TagController;
import br.com.passos.api_blog_pessoal.dto.TagResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TagAssembler implements RepresentationModelAssembler<TagResponse, EntityModel<TagResponse>> {
    @Override
    public EntityModel<TagResponse> toModel(TagResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(TagController.class).listar()).withRel("tags")
        );
    }
}
