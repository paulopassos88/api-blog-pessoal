package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.PostController;
import br.com.passos.api_blog_pessoal.dto.PostFeedResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostFeedAssembler implements RepresentationModelAssembler<PostFeedResponse, EntityModel<PostFeedResponse>> {
    @Override
    public EntityModel<PostFeedResponse> toModel(PostFeedResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(PostController.class).buscar(entity.id())).withSelfRel()
        );
    }
}
