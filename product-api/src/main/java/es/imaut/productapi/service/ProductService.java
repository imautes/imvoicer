package es.imaut.productapi.service;

import es.imaut.productapi.domain.ProductResponse;
import es.imaut.productapi.domain.CreateProductRequest;
import es.imaut.productapi.exception.ProductNotFoundException;
import es.imaut.productapi.mapper.ProductDetailsMapper;
import es.imaut.productapi.mapper.ProductMapper;
import es.imaut.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.json.JsonMergePatch;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper productMapper;
    private final ProductDetailsMapper detailsMapper;

    public List<ProductResponse> findAll() {
        return repository.findAll().stream().map(detailsMapper::from).toList();
    }

    public ProductResponse findById(Long id) {
        return repository.findById(id)
                .map(detailsMapper::from)
                .orElseThrow(ProductNotFoundException::new);
    }

    public ProductResponse create(CreateProductRequest request) {
        return detailsMapper.from(repository.save(productMapper.from(request)));
    }

    public ProductResponse update(Long id, JsonMergePatch patch) {
        return repository.findById(id)
                .map(c -> productMapper.merge(patch, c))
                .map(repository::save)
                .map(detailsMapper::from)
                .orElseThrow(ProductNotFoundException::new);
    }

    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }
}
