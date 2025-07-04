package uk.gov.hmcts.reform.migration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


//@ExtendWith(SpringExtension.class)
//@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class DataMigrationServiceImplTest {

    @MockitoBean
    private

        @InjectMocks
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
