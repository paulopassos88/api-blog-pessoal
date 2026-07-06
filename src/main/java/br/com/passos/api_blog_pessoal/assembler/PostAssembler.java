package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.PostController;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostAssembler implements RepresentationModelAssembler<PostResponse, EntityModel<PostResponse>> {
    @Override
    public EntityModel<PostResponse> toModel(PostResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(PostController.class).buscar(entity.id())).withSelfRel(),
                linkTo(methodOn(PostController.class).listar()).withRel("posts"),
                linkTo(PostController.class).slash("posts").slash(entity.id()).slash("curtir").withRel("curtir")
        );
    }
}
