package com.sap.bulletinboard.ads.controllers;

import static com.sap.hcp.cf.logging.common.customfields.CustomField.customField;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.sap.bulletinboard.ads.controllers.dto.AdvertisementDto;
import com.sap.bulletinboard.ads.controllers.dto.AdvertisementListDto;
import com.sap.bulletinboard.ads.controllers.dto.PageHeaderBuilder;
import com.sap.bulletinboard.ads.models.Advertisement;
import com.sap.bulletinboard.ads.models.AdvertisementRepository;
import com.sap.bulletinboard.ads.services.StatisticsServiceClient;
import com.sap.bulletinboard.ads.services.UserChecker;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;

@RestController
@Validated
@RequestMapping(AdvertisementController.PATH)
public class AdvertisementController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Marker TECHNICAL = MarkerFactory.getMarker("TECHNICAL");

    static final String PATH = "/api/v1/ads";
    static final String PATH_PAGES = PATH + "/pages/";
    static final int DEFAULT_PAGE_SIZE = 20;
    private static final int FIRST_PAGE_ID = 0;

    private final AdvertisementRepository repository;
    private final StatisticsServiceClient statisticsServiceClient;
    private final UserChecker userChecker;

    public AdvertisementController(AdvertisementRepository repository, StatisticsServiceClient statisticsServiceClient,
            UserChecker userChecker) {
        this.repository = repository;
        this.statisticsServiceClient = statisticsServiceClient;
        this.userChecker = userChecker;
    }

    @GetMapping
    public ResponseEntity<AdvertisementListDto> advertisements(
            @RequestParam(name = "category", required = false) String category) {
        logger.info("retrieving ads for category: {}", category);
        if (category == null || category.trim().isEmpty()) {
            return advertisementsForPage(FIRST_PAGE_ID);
        }
        List<Advertisement> entities = repository.findByCategory(category);
        AdvertisementListDto listDto = toDtoList(entities);
        return new ResponseEntity<>(listDto, HttpStatus.OK);
    }

    @GetMapping("/pages/{pageId}")
    public ResponseEntity<AdvertisementListDto> advertisementsForPage(@PathVariable("pageId") int pageId) {
        Page<Advertisement> page = repository.findAll(PageRequest.of(pageId, DEFAULT_PAGE_SIZE));
        List<Advertisement> entities = page.getContent();
        AdvertisementListDto listDto = toDtoList(entities);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK, PageHeaderBuilder.createLinkHeaderString(page, PATH_PAGES));
        return new ResponseEntity<>(listDto, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public AdvertisementDto advertisementById(@PathVariable("id") @Min(0) Long id) throws UserInfoException {
        MDC.put("endpoint", "GET: " + PATH + "/" + id);
        logger.info("demo of custom fields, not part of message", customField("example-key", "example-value"));
        logger.info("demo of custom fields, part of message: {}", customField("example-key", "example-value"));
        throwIfNonexisting(id);
        statisticsServiceClient.advertisementIsShown(id, SecurityContext.getUserInfo().getIdentityZone());

        AdvertisementDto advertisement = entityToDto(repository.findById(id).get());
        logger.trace("returning advertisementById: {}", advertisement);
        return advertisement;
    }

    @PostMapping
    public ResponseEntity<AdvertisementDto> add(@RequestBody @Valid AdvertisementDto advertisement,
            UriComponentsBuilder uriComponentsBuilder) {
        if (!userChecker.checkUser("42")) {
            NotAuthorizedException exception = new NotAuthorizedException("no premium user");
            logger.warn("no premium user", exception);
            throw exception;
        }

        AdvertisementDto savedAdvertisement = entityToDto(repository.save(dtoToEntity(advertisement)));
        logger.trace(TECHNICAL, "created ad with version {}", savedAdvertisement.metadata.version);
        UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}").buildAndExpand(savedAdvertisement.id);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(savedAdvertisement, headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public AdvertisementDto update(@RequestBody AdvertisementDto updatedAdvertisement, @PathVariable("id") Long id) {
        throwIfInconsistent(id, updatedAdvertisement.id);
        throwIfNonexisting(id);
        AdvertisementDto dto = entityToDto(repository.save(dtoToEntity(updatedAdvertisement)));
        logger.trace(TECHNICAL, "updated ad with version {}", updatedAdvertisement.metadata.version);
        return dto;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@authAccess.hasUpdateScope()")
    public void deleteById(@PathVariable("id") Long id) {
        throwIfNonexisting(id);
        repository.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@authAccess.hasUpdateScope()")
    public void deleteAll() {
        repository.deleteAllInBatch();
    }

    private AdvertisementListDto toDtoList(List<Advertisement> entities) {
        List<AdvertisementDto> dtos = entities.stream().map(this::entityToDto).collect(Collectors.toList());
        AdvertisementListDto listDto = new AdvertisementListDto();
        listDto.advertisements = dtos;
        return listDto;
    }

    private void throwIfNonexisting(@PathVariable("id") Long id) {
        if (!repository.existsById(id)) {
            NotFoundException notFoundException = new NotFoundException("no Advertisement with id " + id);
            logger.warn("request failed", notFoundException);
            throw notFoundException;
        }
    }

    private void throwIfInconsistent(Long expected, Long actual) {
        if (!expected.equals(actual)) {
            String message = "bad request, inconsistent IDs between request and object: request id = " + expected
                    + ", object id = " + actual;
            throw new BadRequestException(message);
        }
    }

    private AdvertisementDto entityToDto(Advertisement ad) {
        AdvertisementDto dto = new AdvertisementDto();

        dto.id = ad.getId();
        dto.title = ad.getTitle();
        dto.category = ad.getCategory();
        dto.contact = ad.getContact();
        dto.price = ad.getPrice();
        dto.currency = ad.getCurrency();
        dto.purchasedOn = ad.getPurchasedOn();

        dto.metadata.createdAt = Objects.toString(ad.getCreatedAt());
        dto.metadata.modifiedAt = Objects.toString(ad.getModifiedAt());
        dto.metadata.version = ad.getVersion();

        return dto;
    }

    private Advertisement dtoToEntity(AdvertisementDto dto) {
        // does not map "read-only" attributes
        Advertisement ad = new Advertisement();
        ad.setId(dto.id);
        ad.setVersion(dto.metadata.version);
        ad.setTitle(dto.title);
        ad.setContact(dto.contact);
        ad.setPrice(dto.price);
        ad.setCurrency(dto.currency);
        ad.setPurchasedOn(dto.purchasedOn);
        ad.setCategory(dto.category);
        return ad;
    }
}
