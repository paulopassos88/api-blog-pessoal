package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.CategoriaMapper;
import br.com.passos.api_blog_pessoal.model.Categoria;
import br.com.passos.api_blog_pessoal.repository.CategoriaRepository;
import br.com.passos.api_blog_pessoal.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (repository.existsByNome(request.nome())) {
            throw new BusinessException("Categoria já cadastrada");
        }

        Categoria categoria = mapper.toEntity(request);
        categoria.setSlug(SlugUtils.makeSlug(request.nome()));

        return mapper.toResponse(repository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));
    }
}
