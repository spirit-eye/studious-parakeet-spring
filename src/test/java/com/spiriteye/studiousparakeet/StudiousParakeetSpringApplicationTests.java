package com.spiriteye.studiousparakeet;

import com.spiriteye.studiousparakeet.Repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class StudiousParakeetSpringApplicationTests {

	@MockitoBean
	private ProductRepository productRepository;

	@MockitoBean
	private ElasticsearchOperations elasticsearchOperations;

	@Test
	void contextLoads() {
	}

}
