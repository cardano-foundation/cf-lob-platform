package org.cardanofoundation.lob.app.organisation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationCreate;
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationUpdate;
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
    private AccountEventRepository accountEventRepository;

    @Mock
    private OrganisationCurrencyService organisationCurrencyService;

    @InjectMocks
    private OrganisationService organisationService;

    private Organisation organisation;

    @BeforeEach
    void setUp() {
        organisation = new Organisation();
        organisation.setId("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
        organisation.setCountryCode("IE");
        organisation.setTaxIdNumber("1");
    }

    @Test
    void testFindById_WhenOrganisationExists() {
        when(organisationRepository.findById("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0")).thenReturn(Optional.of(organisation));
        Optional<Organisation> result = organisationService.findById("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
        assertTrue(result.isPresent());
        assertEquals("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0", result.get().getId());
    }

    @Test
    void testFindById_WhenOrganisationDoesNotExist() {
        when(organisationRepository.findById("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0")).thenReturn(Optional.empty());
        Optional<Organisation> result = organisationService.findById("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
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
        when(costCenterService.getAllCostCenter("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0")).thenReturn(costCenters);
        Set<OrganisationCostCenter> result = organisationService.getAllCostCenter("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
        assertEquals(costCenters, result);
    }

    @Test
    void testGetAllProjects() {
        Set<OrganisationProject> projects = new HashSet<>();
        when(projectMappingRepository.findAllByOrganisationId("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0")).thenReturn(projects);
        Set<OrganisationProject> result = organisationService.getAllProjects("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
        assertEquals(projects, result);
        verify(projectMappingRepository).findAllByOrganisationId("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0");
        verifyNoMoreInteractions(projectMappingRepository);
    }

    @Test
    void testUpsertOrganisation_NewOrganisation() {

        OrganisationUpdate organisationUpdate = new OrganisationUpdate();
        organisationUpdate.setAddress("Street");
        organisationUpdate.setName("Company name");
        organisationUpdate.setAdminEmail("the@email.com");
        organisationUpdate.setCity("City name");
        organisationUpdate.setCurrencyId("ISO_4217:CHF");
        organisationUpdate.setPostCode("A127");
        organisationUpdate.setProvince("County co.");
        organisationUpdate.setReportCurrencyId("ISO_4217:CHF");
        organisationUpdate.setPhoneNumber("0101010101");

        when(organisationRepository.saveAndFlush(any())).thenReturn(organisation);

        Organisation result = organisationService.upsertOrganisation(organisation, organisationUpdate).get();
        assertNotNull(result);
        assertEquals("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0",result.getId());
        assertEquals("Street",result.getAddress());
        assertEquals("City name",result.getCity());
        assertEquals("County co.",result.getProvince());
        verify(organisationRepository).saveAndFlush(any());
        verifyNoMoreInteractions(organisationRepository);
    }

    @Test
    void testCreateOrganisation_NewOrganisation() {
        OrganisationCreate organisationCreate = new OrganisationCreate();
        organisationCreate.setAddress("Street");
        organisationCreate.setCountryCode("IE");
        organisationCreate.setTaxIdNumber("1");
        organisationCreate.setName("Company name");
        organisationCreate.setAdminEmail("the@email.com");
        organisationCreate.setCity("City name");
        organisationCreate.setCurrencyId("ISO_4217:CHF");
        organisationCreate.setPostCode("A127");
        organisationCreate.setProvince("County co.");
        organisationCreate.setReportCurrencyId("ISO_4217:CHF");
        organisationCreate.setPhoneNumber("0101010101");

        when(organisationRepository.saveAndFlush(any())).thenReturn(organisation);

        Optional<Organisation> optionalOrg = organisationService.createOrganisation(organisationCreate);
        assertTrue(optionalOrg.isPresent());
        Organisation result = optionalOrg.get();
        assertEquals("f3b7485e96cc45b98e825a48a80d856be260b53de5fe45f23287da5b4970b9b0",result.getId());
        assertEquals("Street",result.getAddress());
        assertEquals("City name",result.getCity());
        assertEquals("County co.",result.getProvince());
        verify(organisationRepository).saveAndFlush(any());
        verifyNoMoreInteractions(organisationRepository);
    }

    @Test
    void testGetOrganisationView() {
        Organisation org = new Organisation();
        org.setId("orgId");
        org.setName("orgName");
        org.setTaxIdNumber("taxId");
        org.setCurrencyId("currencyId");
        org.setAdminEmail("adminEmail");
        org.setPhoneNumber("phoneNumber");
        org.setAddress("address");
        org.setCity("city");
        org.setPostCode("postCode");
        org.setProvince("province");
        org.setCountryCode("countryCode");
        org.setWebsiteUrl("webSite");
        org.setLogo("logo");

        when(costCenterService.getAllCostCenter(anyString())).thenReturn(Set.of(new OrganisationCostCenter()));
        when(projectMappingRepository.findAllByOrganisationId(anyString())).thenReturn(Set.of(new OrganisationProject()));
        when(organisationCurrencyService.findAllByOrganisationId(anyString())).thenReturn(Set.of(new OrganisationCurrency()));

        OrganisationView organisationView = organisationService.getOrganisationView(org);

        assertNotNull(organisationView);
        assertEquals(org.getId(), organisationView.getId());
        assertEquals(org.getName(), organisationView.getName());
        assertEquals(org.getTaxIdNumber(), organisationView.getTaxIdNumber());
        assertEquals(org.getCurrencyId(), organisationView.getCurrencyId());
        assertEquals(org.getAdminEmail(), organisationView.getAdminEmail());
        assertEquals(org.getPhoneNumber(), organisationView.getPhoneNumber());
        assertEquals(org.getAddress(), organisationView.getAddress());
        assertEquals(org.getCity(), organisationView.getCity());
        assertEquals(org.getPostCode(), organisationView.getPostCode());
        assertEquals(org.getProvince(), organisationView.getProvince());
        assertEquals(org.getCountryCode(), organisationView.getCountryCode());
        assertEquals(org.getWebsiteUrl(), organisationView.getWebsiteUrl());
        assertEquals(org.getLogo(), organisationView.getLogo());
        assertEquals(1, organisationView.getCostCenters().size());
        assertEquals(1, organisationView.getProjects().size());
        assertEquals(1, organisationView.getOrganisationCurrencies().size());

        verify(costCenterService).getAllCostCenter(anyString());
        verify(projectMappingRepository).findAllByOrganisationId(anyString());
        verify(organisationCurrencyService).findAllByOrganisationId(anyString());
        verifyNoMoreInteractions(costCenterService, projectMappingRepository, organisationCurrencyService);
    }
}
