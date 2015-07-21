package org.majimena.petz.service.impl;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.majimena.petz.Application;
import org.majimena.petz.domain.Project;
import org.majimena.petz.domain.User;
import org.majimena.petz.repository.ProjectRepository;
import org.majimena.petz.repository.UserRepository;
import org.majimena.petz.security.SecurityUtils;
import org.majimena.petz.service.ProjectService;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ProjectService
 */
@RunWith(Enclosed.class)
public class ProjectServiceImplTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @WebAppConfiguration
    @SpringApplicationConfiguration(classes = Application.class)
    public static class SaveProjectTest {

        private ProjectServiceImpl sut = new ProjectServiceImpl();

        @Mocked
        private ProjectRepository projectRepository;

        @Mocked
        private UserRepository userRepository;

        @Mocked
        private SecurityUtils securityUtils;

        @PostConstruct
        public void postConstruct() {
        }

        @Before
        public void before() {
            sut.projectRepository = projectRepository;
            sut.userRepository = userRepository;
        }

        @Test
        public void testNormalEnd() throws Exception {
            new NonStrictExpectations() {{
                securityUtils.getCurrentLogin();
                result = "test";
                userRepository.findOneByLogin("test");
                result = Optional.of(createTestUser());
                Project testData = Project.builder().name("test project").description("desc").owner(createTestUser()).build();
                projectRepository.save(testData);
                result = testData;
            }};

            Project project = Project.builder().name("test project").description("desc").build();
            Optional<Project> result = sut.saveProject(project);

            assertThat(result.isPresent()).isTrue();
            assertThat(result.get().getName()).isEqualTo("test project");
            assertThat(result.get().getDescription()).isEqualTo("desc");
            assertThat(result.get().getOwner().getId()).isEqualTo("1");
            assertThat(result.get().getOwner().getLogin()).isEqualTo("test");
        }

        private User createTestUser() {
            User user = new User();
            user.setId("1");
            user.setLogin("test");
            return user;
        }
    }

}