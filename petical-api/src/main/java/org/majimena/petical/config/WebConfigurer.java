package org.majimena.petical.config;

import lombok.extern.slf4j.Slf4j;
import org.majimena.petical.common.factory.JsonFactory;
import org.majimena.petical.web.filter.CrossOriginResourceSharingFilter;
import org.majimena.petical.web.filter.gzip.GZipServletFilter;
import org.majimena.petical.web.servlet.filter.AccessLogFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Slf4j
@Configuration
@AutoConfigureAfter(CacheConfiguration.class)
public class WebConfigurer extends AbstractAnnotationConfigDispatcherServletInitializer implements ServletContextInitializer, EmbeddedServletContainerCustomizer {

    @Inject
    private Environment env;

//    @Autowired(required = false)
//    private MetricRegistry metricRegistry;

    @Autowired(required = false)
    private JsonFactory jsonFactory;

    private RelaxedPropertyResolver propertyResolver;

    private void init() {
        propertyResolver = new RelaxedPropertyResolver(env, "endpoints.cors.");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        log.info("Web application configuration, using profiles: {}", Arrays.toString(env.getActiveProfiles()));
        init();

        // initialize metric filter
//        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
//        initMetrics(servletContext, disps);

        // initialize gzip filter
//        if (env.acceptsProfiles(Constants.SPRING_PROFILE_PRODUCTION)) {
//            initGzipFilter(servletContext, disps);
//        }

        // initialize logging and cors filters
        initCrossOriginResourceSharingFilter(servletContext);
        initAccessLogFilter(servletContext);
//        initOpenEntityManagerInViewFilter(servletContext);

        super.onStartup(servletContext);
        log.info("Web application fully configured");
    }

    @Override
    protected String[] getServletMappings() {
        return new String[0];
    }

    /**
     * Set up Mime types.
     */
    @Override
    @Deprecated
    public void customize(ConfigurableEmbeddedServletContainer container) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
//        mappings.add("html", "text/html;charset=utf-8");
        // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
//        mappings.add("json", "text/html;charset=utf-8");
        container.setMimeMappings(mappings);
    }

    /**
     * Initializes the GZip filter.
     */
    private void initGzipFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Registering GZip Filter");
        FilterRegistration.Dynamic compressingFilter = servletContext.addFilter("gzipFilter", new GZipServletFilter());
        Map<String, String> parameters = new HashMap<>();
        compressingFilter.setInitParameters(parameters);
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.css");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.json");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.html");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.js");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.svg");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "*.ttf");
        compressingFilter.addMappingForUrlPatterns(disps, true, "/api/*");
//        compressingFilter.addMappingForUrlPatterns(disps, true, "/metrics/*");
        compressingFilter.setAsyncSupported(true);
    }

    /**
     * Initializes the cachig HTTP Headers Filter.
     */
//    @Deprecated
//    private void initCachingHttpHeadersFilter(ServletContext servletContext,
//                                              EnumSet<DispatcherType> disps) {
//        log.debug("Registering Caching HTTP Headers Filter");
//        FilterRegistration.Dynamic cachingHttpHeadersFilter =
//                servletContext.addFilter("cachingHttpHeadersFilter",
//                        new CachingHttpHeadersFilter(env));
//
//        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/assets/*");
//        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/scripts/*");
//        cachingHttpHeadersFilter.setAsyncSupported(true);
//    }

    /**
     * Initializes Metrics.
     */
//    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
//        log.debug("Initializing Metrics registries");
//        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
//                metricRegistry);
//        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
//                metricRegistry);
//
//        log.debug("Registering Metrics Filter");
//        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter",
//                new InstrumentedFilter());
//
//        metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
//        metricsFilter.setAsyncSupported(true);
//
//        log.debug("Registering Metrics Servlet");
//        ServletRegistration.Dynamic metricsAdminServlet =
//                servletContext.addServlet("metricsServlet", new MetricsServlet());
//
//        metricsAdminServlet.addMapping("/metrics/metrics/*");
//        metricsAdminServlet.setAsyncSupported(true);
//        metricsAdminServlet.setLoadOnStartup(2);
//    }

    private void initAccessLogFilter(ServletContext context) {
        AccessLogFilter filter = new AccessLogFilter();
        filter.setJsonFactory(jsonFactory);

        FilterRegistration registration = context.addFilter("accessLogFilter", filter);
        registration.addMappingForUrlPatterns(null, false, "/*");
    }

    private void initCrossOriginResourceSharingFilter(ServletContext context) {
        CrossOriginResourceSharingFilter filter = new CrossOriginResourceSharingFilter();
        filter.setAllowCredentials(propertyResolver.getRequiredProperty("allow-credentials", Boolean.class));
        filter.setAllowOrigin(propertyResolver.getRequiredProperty("allowed-origins"));
        filter.setAllowMethods(propertyResolver.getRequiredProperty("allowed-methods"));
        filter.setAllowHeaders(propertyResolver.getRequiredProperty("allowed-headers"));
        filter.setExposeHeaders(propertyResolver.getRequiredProperty("exposed-headers"));
        filter.setMaxAge(propertyResolver.getRequiredProperty("max-age", Integer.class));

        FilterRegistration registration = context.addFilter("crossOriginResourceSharingFilter", filter);
        registration.addMappingForUrlPatterns(null, false, "/*");
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[0];
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic reg) {
        reg.setInitParameter("throwExceptionIfNoHandlerFound", "true");
    }
}
