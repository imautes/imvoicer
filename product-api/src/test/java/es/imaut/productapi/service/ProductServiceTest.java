package es.imaut.productapi.service;

import es.imaut.productapi.domain.CreateProductRequest;
import es.imaut.productapi.exception.ProductNotFoundException;
import es.imaut.productapi.mapper.ProductDetailsMapper;
import es.imaut.productapi.mapper.ProductDetailsMapperImpl;
import es.imaut.productapi.mapper.ProductMapper;
import es.imaut.productapi.mapper.ProductMapperImpl;
import es.imaut.productapi.model.Product;
import es.imaut.productapi.repository.ProductRepository;
import es.imaut.productapi.service.ProductService;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class ProductServiceTest {
    @Mock
    private ProductRepository repository;
    @Mock
    private ProductMapper productMapper = new ProductMapperImpl();
    @Spy
    private ProductDetailsMapper detailsMapper = new ProductDetailsMapperImpl();
    @InjectMocks
    private ProductService service;

    @Test
    @DisplayName("Find all should call repository")
    void findAllShouldCallRepository() {
        service.findAll();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return list mapped from repository")
    void findAllShouldReturnListMappedFromRepository(@Random(type = Product.class) List<Product> products) {
        when(repository.findAll()).thenReturn(products);
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(products)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(products);
    }

    @Test
    @DisplayName("Find by id should call repository")
    void findByIdShouldCallRepository(@Random Product product) {
        when(repository.findById(product.getId())).thenReturn(of(product));
        service.findById(product.getId());
        verify(repository).findById(product.getId());
    }

    @Test
    @DisplayName("Find by id should throw not found exception")
    void findByIdShouldThrowNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(empty());
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Find by id should return product from repository")
    void findAllShouldReturnProductFromRepository(@Random Product product) {
        when(repository.findById(product.getId())).thenReturn(of(product));
        var result = service.findById(product.getId());
        assertThat(result).usingRecursiveComparison().isEqualTo(product);
    }

    @Test
    @DisplayName("Create should call repository")
    void createShouldCallRepository(@Random CreateProductRequest request, @Random Product product) {
        product.setName(request.getName());
        when(productMapper.from(request))
                .thenReturn(product);
        service.create(request);
        verify(repository).save(argThat(c -> c.getName().equals(request.getName())));
    }

    @Test
    @DisplayName("Create should return created product")
    void createShouldReturnCreatedProduct(@Random CreateProductRequest request, @Random Product product) {
        product.setName(request.getName());
        when(productMapper.from(request))
                .thenReturn(product);
        when(repository.save(argThat(c -> c.getName().equals(request.getName()))))
                .thenReturn(product);
        var result = service.create(request);
        assertThat(result).usingRecursiveComparison().isEqualTo(product);
    }

    @Test
    @DisplayName("Update should call repository to find product by id")
    void updateShouldCallRepositoryToFindProductById(@Random Product product) {
        when(repository.findById(product.getId())).thenReturn(of(product));
        when(repository.save(product)).thenReturn(product);
        when(productMapper.merge(any(JsonMergePatch.class), any(Product.class)))
                .thenReturn(product);
        service.update(product.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).findById(product.getId());
    }

    @Test
    @DisplayName("Update should throw product not found exception")
    void updateShouldThrowProductNotFoundException() {
        when(repository.findById(1L)).thenReturn(empty());
        assertThatThrownBy(() -> service.update(1L, createMergePatch(createObjectBuilder().build())))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Update should call repository to save product")
    void updateShouldCallRepositoryToSaveProduct(@Random Product product) {
        when(repository.findById(product.getId())).thenReturn(of(product));
        when(repository.save(product)).thenReturn(product);
        when(productMapper.merge(any(JsonMergePatch.class), any(Product.class)))
                .thenReturn(product);
        service.update(product.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).save(product);
    }

    @Test
    @DisplayName("Update should return updated product")
    void updateShouldReturnUpdatedProduct(@Random Product product, @Random Product updated) {
        updated.setId(product.getId());
        when(repository.findById(product.getId())).thenReturn(of(product));
        when(repository.save(updated)).thenReturn(updated);
        when(productMapper.merge(any(JsonMergePatch.class), eq(product)))
                .thenReturn(updated);
        var result = service.update(product.getId(), createMergePatch(createObjectBuilder().build()));
        assertThat(result).usingRecursiveComparison().isEqualTo(updated);
    }

    @Test
    @DisplayName("Delete should call repository to check if exists")
    void deleteShouldNotCallRepositoryToCheckIfExists() {
        service.delete(1L);
        verify(repository).existsById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldNotCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(false);
        service.delete(1L);
        verify(repository, times(0)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
