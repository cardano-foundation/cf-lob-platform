package org.cardanofoundation.lob.app.organisation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationUpsert;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationView;
import org.cardanofoundation.lob.app.organisation.repository.*;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private CostCenterService costCenterService;

    @Mock
    private ProjectMappingRepository projectMappingRepository;

    @Mock
    private OrganisationChartOfAccountTypeRepository organisationChartOfAccountTypeRepository;

    @Mock
    private ChartOfAccountRepository organisationChartOfAccountRepository;

    @Mock
    private OrganisationChartOfAccountSubTypeRepository organisationChartOfAccountSubTypeRepository;

    @Mock
    private AccountEventRepository accountEventRepository;

    @Mock
    private OrganisationCurrencyService organisationCurrencyService;

    @InjectMocks
    private OrganisationService organisationService;

    private Organisation organisation;

    @BeforeEach
    void setUp() {
        organisation = new Organisation();
        organisation.setId("org-123");
    }

    @Test
    void testFindById_WhenOrganisationExists() {
        when(organisationRepository.findById("org-123")).thenReturn(Optional.of(organisation));
        Optional<Organisation> result = organisationService.findById("org-123");
        assertTrue(result.isPresent());
        assertEquals("org-123", result.get().getId());
    }

    @Test
    void testFindById_WhenOrganisationDoesNotExist() {
        when(organisationRepository.findById("org-123")).thenReturn(Optional.empty());
        Optional<Organisation> result = organisationService.findById("org-123");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        List<Organisation> organisations = List.of(organisation);
        when(organisationRepository.findAll()).thenReturn(organisations);
        List<Organisation> result = organisationService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllCostCenter() {
        Set<OrganisationCostCenter> costCenters = new HashSet<>();
        when(costCenterService.getAllCostCenter("org-123")).thenReturn(costCenters);
        Set<OrganisationCostCenter> result = organisationService.getAllCostCenter("org-123");
        assertEquals(costCenters, result);
    }

    @Test
    void testGetAllProjects() {
        Set<OrganisationProject> projects = new HashSet<>();
        when(projectMappingRepository.findAllByOrganisationId("org-123")).thenReturn(projects);
        Set<OrganisationProject> result = organisationService.getAllProjects("org-123");
        assertEquals(projects, result);
    }

    @Test
    void testUpsertOrganisation_NewOrganisation() {
        OrganisationUpsert organisationUpsert = new OrganisationUpsert();
        organisationUpsert.setCountryCode("US");
        organisationUpsert.setTaxIdNumber("12345");
        organisationUpsert.setAddress("Street");
        organisationUpsert.setName("Company name");
        organisationUpsert.setAdminEmail("the@email.com");
        organisationUpsert.setCity("City name");
        organisationUpsert.setCountry("Country name");
        organisationUpsert.setCurrencyId("ISO_4217:CHF");
        organisationUpsert.setPostCode("A127");
        organisationUpsert.setProvince("County co.");
        organisationUpsert.setReportCurrencyId("ISO_4217:CHF");
        organisationUpsert.setPhoneNumber("0101010101");

        when(organisationRepository.findById(any())).thenReturn(Optional.empty());
        when(organisationRepository.saveAndFlush(any())).thenReturn(organisation);

        OrganisationView result = organisationService.upsertOrganisation(organisationUpsert);
        assertNotNull(result);
        assertEquals("6d50ed2208aba5047f54a0b4e603d77463db27f108de9a268bb1670fa9afef11",result.getId());
        assertEquals("Street",result.getAddress());
        assertEquals("City name",result.getCity());
        assertEquals("Country name",result.getCountry());
        assertEquals("County co.",result.getProvince());
    }
}
