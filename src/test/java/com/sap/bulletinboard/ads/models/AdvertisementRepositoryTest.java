package com.sap.bulletinboard.ads.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sap.bulletinboard.ads.testutils.TimeServiceFake;
import com.sap.bulletinboard.ads.util.TimeServiceProvider;

@RunWith(SpringRunner.class)
@DataJpaTest
// Use Replace.NONE to disable in-memory database for this test
// @AutoConfigureTestDatabase(replace = Replace.NONE)
// @DataJpaTest automatically creates a transaction for each test, which is not what we want here
// see https://www.javacodegeeks.com/2011/12/spring-pitfalls-transactional-tests.html
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AdvertisementRepositoryTest {
    @Autowired
    private AdvertisementRepository repo;
    private Advertisement entity;

    @Before
    public void setUp() {
        entity = new Advertisement("SOME title", "contact@email.de", new BigDecimal("77.77"), "EUR");
    }

    @After
    public void tearDown() {
        repo.deleteAllInBatch();
        assertThat(repo.count(), is(0L));
        TimeServiceProvider.setTimeService(Instant::now);
    }

    @Test
    public void shouldSetIdOnFirstSave() {
        entity = repo.save(entity);
        assertThat(entity.getId(), is(notNullValue()));
    }

    @Test
    public void shouldSetCreatedTimestampOnFirstSaveOnly() {
        Instant firstInstant = Instant.now();
        Instant secondInstant = firstInstant.plusSeconds(5);

        fakeTime(firstInstant);
        entity = repo.save(entity);
        Instant timestampAfterCreation = entity.getCreatedAt();
        assertThat(timestampAfterCreation, is(firstInstant));

        fakeTime(secondInstant);
        entity.setTitle("Updated Title");
        entity = repo.save(entity);
        Instant timestampAfterUpdate = entity.getCreatedAt();
        assertThat(timestampAfterUpdate, is(firstInstant));
    }

    @Test
    public void shouldSetUpdatedTimestampOnEveryUpdate() {
        Instant firstInstant = Instant.now();
        Instant secondInstant = firstInstant.plusSeconds(5);

        fakeTime(firstInstant);
        entity = repo.save(entity);

        entity.setTitle("Updated Title");
        entity = repo.save(entity);

        Instant timestampAfterFirstUpdate = entity.getModifiedAt();
        assertThat(timestampAfterFirstUpdate, is(firstInstant));

        fakeTime(secondInstant);
        entity.setTitle("Updated Title 2");
        entity = repo.save(entity);
        Instant timestampAfterSecondUpdate = entity.getModifiedAt();
        assertThat(timestampAfterSecondUpdate, is(secondInstant));
    }

    @Test(expected = ObjectOptimisticLockingFailureException.class)
    public void shouldUseVersionForConflicts() {
        // persists entity and sets initial version
        entity = repo.save(entity);

        entity.setTitle("entity instance 1");
        repo.save(entity); // returns instance with updated version

        repo.save(entity); // tries to persist entity with outdated version
    }

    @Test
    public void shouldFindByTitle() {
        String title = "Find me";

        entity.setTitle(title);
        repo.save(entity);

        Advertisement foundEntity = repo.findByTitle(title).get(0);
        assertThat(foundEntity.getTitle(), is(title));
    }

    @Test
    public void shouldFindByCategory() {
        String category = "Find me";

        entity.setCategory(category);
        repo.save(entity);

        Advertisement foundEntity = repo.findByCategory(category).get(0);
        assertThat(foundEntity.getCategory(), is(category));
    }

    @Test
    public void testPricePrecision() {
        // 12 digit precision
        BigDecimal price = new BigDecimal("123123123.123");
        entity.setPrice(price);
        entity = repo.save(entity);

        Optional<Advertisement> findById = repo.findById(entity.getId());
        assertThat(findById.isPresent(), is(true));
        assertThat(findById.get().getPrice(), is(price));
    }

    private void fakeTime(Instant instant) {
        TimeServiceFake timeService = new TimeServiceFake(instant);
        TimeServiceProvider.setTimeService(timeService);
    }
}