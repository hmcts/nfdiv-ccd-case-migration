package uk.gov.hmcts.reform.migration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.model.UserRole;

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

    private final String applicant1OrganisationPolicyKey = "applicant1SolicitorOrganisationPolicy";
    private final String applicant2OrganisationPolicyKey = "applicant2SolicitorOrganisationPolicy";
    private OrganisationPolicy defaultApp1OrgPolicy;
    private OrganisationPolicy defaultApp2OrgPolicy;

    @Autowired
    DataMigrationServiceImpl service;

    @Before
    public void setUp() {
        defaultApp1OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_1_SOLICITOR)
            .build();

        defaultApp2OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .build();
    }

    private Map<String,Object> migrateDataWithOrgPolicies(
        OrganisationPolicy<UserRole> app1OrgPolicy,
        OrganisationPolicy<UserRole> app2OrgPolicy
    ) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(applicant1OrganisationPolicyKey, app1OrgPolicy);
        inputData.put(applicant2OrganisationPolicyKey, app2OrgPolicy);
        return service.migrate(inputData);
    }

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
        Map<String, Object> result = service.migrate(data);
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
    public void shouldPopulateDefaultOrgPoliciesWhenOrgPolicyIsFullyMissing() {
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(applicant1OrganisationPolicyKey, defaultApp1OrgPolicy);
        expectedResult.put(applicant2OrganisationPolicyKey, defaultApp2OrgPolicy);

        assertEquals(expectedResult, migrateDataWithOrgPolicies(null, null));
    }

    @Test
    public void shouldSetDefaultOrganisationWhenOrganisationIsMissing() {
        OrganisationPolicy<UserRole> app1OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_1_SOLICITOR)
            .build();

        OrganisationPolicy<UserRole> app2OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .build();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(applicant1OrganisationPolicyKey, defaultApp1OrgPolicy);
        expectedResult.put(applicant2OrganisationPolicyKey, defaultApp2OrgPolicy);

        assertEquals(expectedResult, migrateDataWithOrgPolicies(app1OrgPolicy, app2OrgPolicy));
    }

    @Test
    public void shouldSetOrgPolicyCaseAssignedRolesWhenTheyAreMissing() {
        OrganisationPolicy<UserRole> app1OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .build();

        OrganisationPolicy<UserRole> app2OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .build();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(applicant1OrganisationPolicyKey, defaultApp1OrgPolicy);
        expectedResult.put(applicant2OrganisationPolicyKey, defaultApp2OrgPolicy);

        assertEquals(expectedResult, migrateDataWithOrgPolicies(app1OrgPolicy, app2OrgPolicy));
    }

    @Test
    public void shouldCorrectOrgPolicyCaseAssignedRoles() {
        OrganisationPolicy<UserRole> invalidApp1OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .build();

        OrganisationPolicy<UserRole> invalidApp2OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId(null).organisationName(null).build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_1_SOLICITOR)
            .build();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(applicant1OrganisationPolicyKey, defaultApp1OrgPolicy);
        expectedResult.put(applicant2OrganisationPolicyKey, defaultApp2OrgPolicy);

        assertEquals(expectedResult, migrateDataWithOrgPolicies(invalidApp1OrgPolicy, invalidApp2OrgPolicy));
    }

    @Test
    public void shouldNotOverwriteExistingOrganisationPolicyFields() {
        OrganisationPolicy<UserRole> app1OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId("Test").organisationName("Test").build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_1_SOLICITOR)
            .orgPolicyReference("TestReference")
            .build();

        OrganisationPolicy<UserRole> app2OrgPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId("Test2").organisationName("Test2").build())
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .orgPolicyReference("TestReference")
            .build();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(applicant1OrganisationPolicyKey, app1OrgPolicy);
        expectedResult.put(applicant2OrganisationPolicyKey, app2OrgPolicy);

        assertEquals(expectedResult, migrateDataWithOrgPolicies(app1OrgPolicy, app2OrgPolicy));
    }
}
