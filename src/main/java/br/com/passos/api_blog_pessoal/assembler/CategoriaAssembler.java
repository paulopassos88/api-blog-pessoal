package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.CategoriaController;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaAssembler implements RepresentationModelAssembler<CategoriaResponse, EntityModel<CategoriaResponse>> {
    @Override
    public EntityModel<CategoriaResponse> toModel(CategoriaResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(CategoriaController.class).listar()).withRel("categorias")
        );
    }
}
