package es.imaut.productapi.mapper;

import es.imaut.productapi.domain.ProductResponse;
import es.imaut.productapi.model.Product;
import org.mapstruct.Mapper;

@Mapper
public interface ProductDetailsMapper {
    ProductResponse from(Product product);
}
