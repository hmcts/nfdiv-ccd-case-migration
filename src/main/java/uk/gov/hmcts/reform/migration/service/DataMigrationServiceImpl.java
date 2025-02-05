package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails == null ? false : true;
    }

    @Override
    public Map<String, Object> migrate(CaseDetails details) {
        if (details == null || details.getData() == null) {
            return null;
        }

        Map<String, Object> ttlMap = new HashMap<>();
        LocalDate deleteDate = details.getLastModified().plusMonths(6).toLocalDate();
        ttlMap.put("Suspended", "No");
        ttlMap.put("OverrideTTL", null);
        ttlMap.put("SystemTTL", deleteDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Map<String, Object> caseData = details.getData();
        caseData.put("TTL", ttlMap);

        return caseData;
    }
}
