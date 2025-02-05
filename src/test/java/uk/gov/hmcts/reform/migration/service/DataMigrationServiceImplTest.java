package uk.gov.hmcts.reform.migration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

//@ExtendWith(SpringExtension.class)
//@SpringBootTest
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataMigrationServiceImplTest {

    @Autowired
    DataMigrationServiceImpl service;

    @Test
    public void shouldReturnTrueForCaseDetailsPassed() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .build();
        assertTrue(service.accepts().test(caseDetails));
    }

    @Test
    public void shouldReturnFalseForCaseDetailsNull() {
        assertFalse(service.accepts().test(null));
    }

    @Test
    public void shouldReturnPassedDataWhenMigrateCalled() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails details = CaseDetails.builder()
            .lastModified(LocalDateTime.of(2023, 1, 3, 0, 0))
            .data(data)
            .build();
        Map<String, Object> result = service.migrate(details);
        assertNotNull(result);
        assertEquals(data, result);
    }

    @Test
    public void shouldReturnNullWhenDataIsNotPassed() {
        Map<String, Object> result = service.migrate(null);
        assertNull(result);
        assertEquals(null, result);
    }

    @Test
    public void shouldPopulateTimeToLiveForOldDraftCases() {
        Map<String, String> ttlMap = new HashMap<>();
        ttlMap.put("Suspended", "No");
        ttlMap.put("OverrideTTL", null);
        ttlMap.put("SystemTTL", "2023-07-03");
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("TTL", ttlMap);

        CaseDetails inputCaseDetails = CaseDetails.builder()
            .data(new HashMap<>())
            .createdDate(LocalDateTime.of(2022, 6, 1, 0, 0))
            .lastModified(LocalDateTime.of(2023, 1, 3, 0, 0))
            .build();

        Map<String, Object> result = service.migrate(inputCaseDetails);

        assertEquals(result, expectedResult);
    }

    @Test
    public void shouldPopulateTimeToLiveForNewDraftCases() {
        Map<String, String> ttlMap = new HashMap<>();
        ttlMap.put("Suspended", "No");
        ttlMap.put("OverrideTTL", null);
        ttlMap.put("SystemTTL", "2025-08-04");
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("TTL", ttlMap);

        CaseDetails inputCaseDetails = CaseDetails.builder()
            .data(new HashMap<>())
            .createdDate(LocalDateTime.of(2025, 2, 2, 0, 0))
            .lastModified(LocalDateTime.of(2025, 2, 4, 0, 0))
            .build();

        Map<String, Object> result = service.migrate(inputCaseDetails);

        assertEquals(result, expectedResult);
    }
}
