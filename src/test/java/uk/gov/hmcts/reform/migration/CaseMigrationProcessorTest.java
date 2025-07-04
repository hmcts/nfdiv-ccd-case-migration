package uk.gov.hmcts.reform.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.exception.CaseMigrationException;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;
import uk.gov.hmcts.reform.migration.service.DataMigrationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CaseMigrationProcessorTest {

    private static final String USER_TOKEN = "Bearer eeeejjjttt";
    private static final String EVENT_ID = "system-set-ttl";
    private static final String EVENT_SUMMARY = "Data Migration - Set retain and dispose time to live";
    private static final String EVENT_DESCRIPTION = "Data Migration - Set retain and dispose time to live";
    private static final String CASE_TYPE = "Test_Case_Type";

    @InjectMocks
    private CaseMigrationProcessor caseMigrationProcessor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private DataMigrationService<Map<String, Object>> dataMigrationService;

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private IdamRepository idamRepository;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(caseMigrationProcessor, "caseProcessLimit", 1);
    }

    @Test
    public void shouldMigrateCasesOfACaseType() {
        when(dataMigrationService.accepts()).thenReturn(candidate -> true);
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.add(details);
        when(elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE)).thenReturn(caseDetails);
        List<CaseDetails> listOfCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertNotNull(listOfCaseDetails);
        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY,
                                        EVENT_DESCRIPTION, CASE_TYPE, details.getId(), details.getJurisdiction()))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                    EVENT_ID,
                    EVENT_SUMMARY,
                    EVENT_DESCRIPTION,
                    CASE_TYPE,
                    details.getId(),
                    details.getJurisdiction());
    }

    @Test
    public void shouldMigrateOnlyLimitedNumberOfCases() {
        when(dataMigrationService.accepts()).thenReturn(candidate -> true);
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        CaseDetails details1 = mock(CaseDetails.class);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.add(details);
        caseDetails.add(details1);
        when(elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE)).thenReturn(caseDetails);
        List<CaseDetails> listOfCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertNotNull(listOfCaseDetails);
        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY,
                                        EVENT_DESCRIPTION, CASE_TYPE, details.getId(), details.getJurisdiction()))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                    EVENT_ID,
                    EVENT_SUMMARY,
                    EVENT_DESCRIPTION,
                    CASE_TYPE,
                    details.getId(),
                    details.getJurisdiction());
    }

    @Test
    public void shouldThrowExceptionWhenCaseTypeNull() {
        assertThrows(CaseMigrationException.class, () -> caseMigrationProcessor.migrateCases(null));
    }

    @Test
    public void shouldThrowExceptionWhenMultipleCaseTypesPassed() {
        assertThrows(CaseMigrationException.class, () ->
            caseMigrationProcessor.migrateCases("Cast_Type1,Cast_Type2"));
    }
}
