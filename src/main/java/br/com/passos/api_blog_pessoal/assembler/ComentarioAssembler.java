package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.ComentarioController;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ComentarioAssembler implements RepresentationModelAssembler<ComentarioResponse, EntityModel<ComentarioResponse>> {
    @Override
    public EntityModel<ComentarioResponse> toModel(ComentarioResponse entity) {
        return EntityModel.of(entity,
                linkTo(ComentarioController.class).slash("comentarios").slash(entity.id()).slash("respostas").withRel("responder"),
                linkTo(ComentarioController.class).slash("comentarios").slash(entity.id()).slash("curtir").withRel("curtir")
        );
    }
}
