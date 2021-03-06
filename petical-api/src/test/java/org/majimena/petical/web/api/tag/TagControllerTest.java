package org.majimena.petical.web.api.tag;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.majimena.petical.WebApplication;
import org.majimena.petical.TestUtils;
import org.majimena.petical.domain.Tag;
import org.majimena.petical.repository.TagRepository;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by todoken on 2015/07/26.
 */
@RunWith(Enclosed.class)
public class TagControllerTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = WebApplication.class)
    @WebAppConfiguration
    public static class PostTest {

        private MockMvc mockMvc;

        @Inject
        private TagController sut;

        @Inject
        private WebApplicationContext wac;

        @Mocked
        private TagRepository tagRepository;

        @Before
        public void setup() {
            mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
            sut.setTagRepository(tagRepository);
        }

        @Test
        public void サービスが呼び出されて正常終了すること() throws Exception {
            new NonStrictExpectations() {{
                tagRepository.findAll();
                result = Arrays.asList(new Tag("foo"), new Tag("bar"));
            }};

            mockMvc.perform(get("/api/v1/tags")
                .contentType(TestUtils.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]", is("foo")))
                .andExpect(jsonPath("$.[1]", is("bar")));
        }
    }
}
