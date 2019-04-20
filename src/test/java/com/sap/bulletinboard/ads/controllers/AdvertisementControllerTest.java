package com.sap.bulletinboard.ads.controllers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bulletinboard.ads.config.WebSecurityConfiguration;
import com.sap.bulletinboard.ads.controllers.dto.AdvertisementDto;
import com.sap.bulletinboard.ads.models.AdvertisementRepository;
import com.sap.bulletinboard.ads.services.StatisticsServiceClient;
import com.sap.bulletinboard.ads.services.UserCheckerProxy;
import com.sap.bulletinboard.ads.services.UserServiceClient;
import com.sap.bulletinboard.ads.testutils.JwtGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class AdvertisementControllerTest {

    private static final String LOCATION = "Location";
    private static final String SOME_TITLE = "MyNewAdvertisement";
    private static final String SOME_OTHER_TITLE = "MyOldAdvertisement";
    private static final String SOME_CATEGORY = "MyNewAdvertisementCategory";

    @Autowired
    private AdvertisementRepository repo;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private StatisticsServiceClient statisticsServiceClient;

    @Autowired
    private WebSecurityConfiguration.SystemPropertyHelper securityPropertyHelper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserCheckerProxy userChecker;

    private String jwt;

    @Before
    public void setUp() {
        when(userServiceClient.isPremiumUser(Mockito.anyString())).thenReturn(true);

        // compute valid token with Display and Update scopes
        jwt = new JwtGenerator().getTokenForAuthorizationHeader(securityPropertyHelper.getDisplayScope(),
                securityPropertyHelper.getUpdateScope());
    }

    @After
    public void tearDown() throws Exception {
        // possibly uses the wrong schema. useless?
        repo.deleteAll();
    }

    @Test
    public void create() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, is(not(""))))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void createAndGetByLocation() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andReturn().getResponse();

        mockMvc.perform(get(response.getHeader(LOCATION)).header(AUTHORIZATION, jwt)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void createWithoutUserCheck() throws Exception {
        when(userServiceClient.isPremiumUser(Mockito.any())).thenReturn(false);
        userChecker.disable();

        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andReturn().getResponse();

        mockMvc.perform(get(response.getHeader(LOCATION)).header(AUTHORIZATION, jwt)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
        userChecker.enable(); // reset for next tests
    }

    @Test
    public void createWithUserCheck() throws Exception {
        when(userServiceClient.isPremiumUser(Mockito.any())).thenReturn(false);

        mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isForbidden()).andReturn().getResponse();
    }

    @Test
    public void read_All() throws Exception {
        mockMvc.perform(buildDeleteAllRequest()).andExpect(status().isNoContent());

        mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());

        mockMvc.perform(buildGetRequest("")).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.value", hasSize(3)));
    }

    @Test
    public void read_AllByCategory() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE, SOME_CATEGORY, jwt)).andExpect(status().isCreated());

        mockMvc.perform(buildGetRequest("", SOME_CATEGORY)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value[0].category", is(SOME_CATEGORY)));
    }

    @Test
    public void read_ById_NotFound() throws Exception {
        mockMvc.perform(buildGetRequest("4711")).andExpect(status().isNotFound());
    }

    @Test
    public void read_ById_Negative() throws Exception {
        mockMvc.perform(buildGetRequest("-1")).andExpect(status().isBadRequest());
    }

    @Test
    public void createNullTitle() throws Exception {
        mockMvc.perform(buildPostRequest(null)).andExpect(status().isBadRequest());
    }

    @Test
    public void createBlancTitle() throws Exception {
        mockMvc.perform(buildPostRequest("")).andExpect(status().isBadRequest());
    }

    @Test
    public void createWithNoContent() throws Exception {
        mockMvc.perform(
                post(AdvertisementController.PATH).contentType(APPLICATION_JSON_UTF8).header(AUTHORIZATION, jwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void read_ById() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildGetRequest(id)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void updateNotFound() throws Exception {
        AdvertisementDto advertisement = createAdvertisment(SOME_TITLE);
        advertisement.id = 4711L;
        mockMvc.perform(buildPutRequest("4711", advertisement)).andExpect(status().isNotFound());
    }

    @Test
    public void update_ById() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = SOME_OTHER_TITLE;
        String id = getIdFromLocation(response.getHeader(LOCATION));

        mockMvc.perform(buildPutRequest(id, advertisement)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_OTHER_TITLE)));
    }

    @Test
    public void updateByNotMatchingId() throws Exception {

        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);

        mockMvc.perform(buildPutRequest("1188", advertisement)).andExpect(status().isBadRequest());
    }

    @Test
    public void deleteNotFound() throws Exception {
        mockMvc.perform(buildDeleteRequest("4711")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteById() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id)).andExpect(status().isNoContent());

        mockMvc.perform(buildGetRequest(id)).andExpect(status().isNotFound());

    }

    @Test
    public void deleteAll() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

        mockMvc.perform(buildGetRequest(id)).andExpect(status().isNotFound());

    }

    @Test
    public void doNotReuseIdsOfDeletedItems() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id)).andExpect(status().isNoContent());

        String idNewAd = performPostAndGetId();

        assertThat(idNewAd, is(not(equalTo(id))));
    }

    @Test
    public void deleteById_notAuthenticated() throws Exception {
        String jwtDisplayOnly = new JwtGenerator()
                .getTokenForAuthorizationHeader(securityPropertyHelper.getDisplayScope());
        String id = performPostAndGetId();

        jwt = jwtDisplayOnly;
        mockMvc.perform(buildDeleteRequest(id)).andExpect(status().isForbidden());
    }

    @Test
    public void createForbiddenWithoutUpdateScope() throws Exception {
        String jwtReadOnly = new JwtGenerator()
                .getTokenForAuthorizationHeader(securityPropertyHelper.getDisplayScope());
        mockMvc.perform(buildPostRequest(SOME_TITLE, null, jwtReadOnly)).andExpect(status().isForbidden());
    }

    @Test
    public void readFailsWhenUnauthenticated() throws Exception {
        mockMvc.perform(get(AdvertisementController.PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteAll_notAuthenticated() throws Exception {
        String jwtDisplayOnly = new JwtGenerator()
                .getTokenForAuthorizationHeader(securityPropertyHelper.getDisplayScope());
        performPostAndGetId();

        jwt = jwtDisplayOnly;
        mockMvc.perform(buildDeleteRequest("")).andExpect(status().isForbidden());
    }

    @Test
    public void readAdsFromSeveralPages() throws Exception {
        int adsCount = AdvertisementController.DEFAULT_PAGE_SIZE + 1;

        mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

        for (int i = 0; i < adsCount; i++) {
            mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
        }

        mockMvc.perform(buildGetByPageRequest(0)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(AdvertisementController.DEFAULT_PAGE_SIZE)));

        mockMvc.perform(buildGetByPageRequest(1)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.value.length()", is(1)));
    }

    @Test
    public void navigatePages() throws Exception {
        int adsCount = (AdvertisementController.DEFAULT_PAGE_SIZE * 2) + 1;

        mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

        for (int i = 0; i < adsCount; i++) {
            mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
        }

        // get query
        String linkHeader = performGetRequest(AdvertisementController.PATH).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader, is("</api/v1/ads/pages/1>; rel=\"next\""));

        // navigate to next
        String nextLink = extractLinks(linkHeader).get(0);
        String linkHeader2ndPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader2ndPage,
                is("</api/v1/ads/pages/0>; rel=\"previous\", </api/v1/ads/pages/2>; rel=\"next\""));

        // navigate to next
        nextLink = extractLinks(linkHeader2ndPage).get(1);
        String linkHeader3rdPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader3rdPage, is("</api/v1/ads/pages/1>; rel=\"previous\""));

        // navigate to previous
        String previousLink = extractLinks(linkHeader3rdPage).get(0);
        assertThat(performGetRequest(previousLink).getHeader(HttpHeaders.LINK), is(linkHeader2ndPage));
    }

    @Test
    public void tenantContextSetup() throws Exception {
        String tenantUuid = UUID.randomUUID().toString();
        String jwtTenantB = new JwtGenerator().getTokenForAuthorizationHeader(tenantUuid,
                securityPropertyHelper.getDisplayScope(), securityPropertyHelper.getUpdateScope());
        mockMvc.perform(buildPostRequest("Tenant A data", null, jwt)).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest("Tenant B data", null, jwtTenantB)).andExpect(status().isCreated());
    }

    private MockHttpServletResponse performGetRequest(String path) throws Exception {
        return mockMvc.perform(get(path).header(AUTHORIZATION, jwt)).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
    }

    private MockHttpServletRequestBuilder buildGetByPageRequest(int pageId) {
        return get(AdvertisementController.PATH_PAGES + pageId).header(AUTHORIZATION, jwt);
    }

    private MockHttpServletRequestBuilder buildPostRequest(String adsTitle) throws Exception {
        return buildPostRequest(adsTitle, null, jwt);
    }

    private MockHttpServletRequestBuilder buildPostRequest(String adsTitle, String category, String jwt)
            throws Exception {
        AdvertisementDto advertisement = createAdvertisement(adsTitle, category);

        return post(AdvertisementController.PATH).content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8)
                .header(AUTHORIZATION, jwt);
    }

    private String performPostAndGetId() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
                .andReturn().getResponse();
        return getIdFromLocation(response.getHeader(LOCATION));
    }

    private MockHttpServletRequestBuilder buildGetRequest(String id) throws Exception {
        return buildGetRequest(id, null);
    }

    private MockHttpServletRequestBuilder buildGetRequest(String id, String category) {
        return get(AdvertisementController.PATH + "/" + id + (category == null ? "" : ("?category=" + category)))
                .header(AUTHORIZATION, jwt);
    }

    private MockHttpServletRequestBuilder buildPutRequest(String id, AdvertisementDto advertisement) throws Exception {
        return put(AdvertisementController.PATH + "/" + id).content(toJson(advertisement))
                .contentType(APPLICATION_JSON_UTF8).header(AUTHORIZATION, jwt);
    }

    private MockHttpServletRequestBuilder buildDeleteRequest(String id) {
        return delete(AdvertisementController.PATH + "/" + id).header(AUTHORIZATION, jwt);
    }

    private MockHttpServletRequestBuilder buildDeleteAllRequest() {
        return delete(AdvertisementController.PATH).header(AUTHORIZATION, jwt);
    }

    private static List<String> extractLinks(final String linkHeader) {
        final List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<(?<link>\\S+)>");
        final Matcher matcher = pattern.matcher(linkHeader);
        while (matcher.find()) {
            links.add(matcher.group("link"));
        }
        return links;
    }

    private String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    private String getIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String contentString = response.getContentAsString();
        return objectMapper.readValue(contentString, clazz);
    }

    private AdvertisementDto createAdvertisment(String title) {
        AdvertisementDto dto = new AdvertisementDto();
        dto.title = title;
        dto.contact = "Mister X";
        dto.price = new BigDecimal("42.42");
        dto.currency = "EUR";
        return dto;
    }

    private AdvertisementDto createAdvertisement(String title, String category) {
        AdvertisementDto newAd = createAdvertisment(title);
        newAd.category = category;
        return newAd;
    }

}
