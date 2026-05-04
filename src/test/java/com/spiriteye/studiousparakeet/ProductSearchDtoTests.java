package com.spiriteye.studiousparakeet;

import com.spiriteye.studiousparakeet.dtos.ProductSearchFacets;
import com.spiriteye.studiousparakeet.dtos.ProductSearchRequest;
import com.spiriteye.studiousparakeet.dtos.ProductSearchResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSearchDtoTests {

    @Test
    void searchRequestKeepsPaginationInsideSupportedRange() {
        ProductSearchRequest request = new ProductSearchRequest();

        request.setPage(-1);
        request.setSize(200);

        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(100);
    }

    @Test
    void searchRequestUsesDefaultSortWhenSortParamsAreBlank() {
        ProductSearchRequest request = new ProductSearchRequest();

        request.setSortBy(" ");
        request.setSortDirection(null);

        assertThat(request.getSortBy()).isEqualTo("relevance");
        assertThat(request.getSortDirection()).isEqualTo("desc");
    }

    @Test
    void searchResponseExposesPaginationStateAndFacets() {
        ProductSearchFacets facets = new ProductSearchFacets(
                List.of("Apple"),
                List.of("Phone"),
                1000F,
                2000F
        );

        ProductSearchResponse<String> response = new ProductSearchResponse<>(
                List.of("iphone"),
                21,
                1,
                10,
                facets
        );

        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isTrue();
        assertThat(response.getFacets().getBrands()).containsExactly("Apple");
    }
}
