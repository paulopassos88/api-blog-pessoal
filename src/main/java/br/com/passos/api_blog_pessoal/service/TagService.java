package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.TagRequest;
import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.mapper.TagMapper;
import br.com.passos.api_blog_pessoal.model.Tag;
import br.com.passos.api_blog_pessoal.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repository;
    private final TagMapper mapper;

    @Transactional
    public TagResponse criar(TagRequest request) {
        Tag tag = repository.findByNome(request.nome())
                .orElseGet(() -> repository.save(mapper.toEntity(request)));
        return mapper.toResponse(tag);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public Tag buscarOuCriar(String nome) {
        return repository.findByNome(nome)
                .orElseGet(() -> repository.save(Tag.builder().nome(nome).build()));
    }
}
