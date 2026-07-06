package br.com.passos.api_blog_pessoal.assembler;

import br.com.passos.api_blog_pessoal.controller.UsuarioController;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioAssembler implements RepresentationModelAssembler<UsuarioResponse, EntityModel<UsuarioResponse>> {
    @Override
    public EntityModel<UsuarioResponse> toModel(UsuarioResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(UsuarioController.class).buscarPorEmail(entity.email())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).buscarPorNome(entity.nome())).withRel("busca-por-nome")
        );
    }
}
