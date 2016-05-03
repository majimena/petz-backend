package org.majimena.petical.repository.spec;

import org.majimena.petical.domain.Product;
import org.majimena.petical.domain.product.ProductCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.Optional;

/**
 * プロダクト検索スペック.
 */
public class ProductSpecs {

    public static Specification<Product> of(ProductCriteria criteria) {
        return Specifications
                .where(Optional.ofNullable(criteria.getClinicId()).map(ProductSpecs::equalClinicId).orElse(null))
                .and(Optional.ofNullable(criteria.getName()).map(ProductSpecs::likeAfterName).orElse(null))
                .and(isNotRemoved());
    }

    private static Specification equalClinicId(String clinicId) {
        return (root, query, cb) -> cb.equal(root.get("clinic").get("id"), clinicId);
    }

    private static Specification likeAfterName(String name) {
        return (root, query, cb) -> cb.like(root.get("name"), name + "%");
    }

    private static Specification isNotRemoved() {
        return (root, query, cb) -> cb.equal(root.get("removed"), Boolean.FALSE);
    }
}
