package es.imaut.productapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.productapi.domain.CreateProductRequest;
import es.imaut.productapi.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.JsonMergePatch;
import javax.json.JsonValue;

@Mapper
public abstract class ProductMapper {
    @Autowired
    private ObjectMapper objectMapper;

    public abstract Product from(CreateProductRequest request);

    public Product merge(JsonMergePatch request, @MappingTarget Product product) {
        var productJson = objectMapper.convertValue(product, JsonValue.class);
        var patchedJson = request.apply(productJson);
        return objectMapper.convertValue(patchedJson, Product.class);
    }
}
