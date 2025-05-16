package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import com.l1maor.vehicleworkshop.service.DtoMapperService;
import com.l1maor.vehicleworkshop.service.SseService;
import com.l1maor.vehicleworkshop.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class VehicleRegistrationSearchControllerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private SseService sseService;
    
    @Mock
    private DtoMapperService dtoMapperService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    @DisplayName("Get all vehicle registrations should return a list of registration DTOs")
    void testGetAllVehicleRegistrations() throws Exception {
        
        VehicleRegistrationDto dieselRegistration = createDieselRegistrationDto(1L);
        VehicleRegistrationDto electricRegistration = createElectricRegistrationDto(2L);
        
        List<VehicleRegistrationDto> registrations = Arrays.asList(dieselRegistration, electricRegistration);
        when(vehicleService.getAllRegistrationInfo()).thenReturn(registrations);

        
        mockMvc.perform(get("/api/vehicles/registration")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].type", is("DIESEL")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].type", is("ELECTRIC")));
    }

    @Test
    @DisplayName("Get vehicle registration by ID should return the registration DTO")
    void testGetVehicleRegistrationById() throws Exception {
        
        Long vehicleId = 1L;
        VehicleRegistrationDto registrationDto = createDieselRegistrationDto(vehicleId);
        
        when(vehicleService.getRegistrationInfo(vehicleId)).thenReturn(registrationDto);

        
        mockMvc.perform(get("/api/vehicles/{id}/registration", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(vehicleId.intValue())))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.registrationInfo", is("D-12345 + ROTARY")));
    }

    @Test
    @DisplayName("Get all vehicle registrations paginated should return paginated results")
    void testGetAllVehicleRegistrationsPaginated() throws Exception {
        
        Pageable pageable = PageRequest.of(0, 10);
        VehicleRegistrationDto dieselRegistration = createDieselRegistrationDto(1L);
        VehicleRegistrationDto electricRegistration = createElectricRegistrationDto(2L);
        
        Page<VehicleRegistrationDto> registrationsPage = new PageImpl<>(
            Arrays.asList(dieselRegistration, electricRegistration), 
            pageable, 
            2
        );
        
        when(vehicleService.getAllRegistrationInfoPaginated(any(Pageable.class))).thenReturn(registrationsPage);

        
        mockMvc.perform(get("/api/vehicles/registration/paginated")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("DIESEL")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].type", is("ELECTRIC")));
    }

    @Test
    @DisplayName("Search vehicle registrations with no filters should return matching results")
    void testSearchVehicleRegistrations() throws Exception {
        
        String searchTerm = "D-12345";
        Pageable pageable = PageRequest.of(0, 10);
        VehicleRegistrationDto registrationDto = createDieselRegistrationDto(1L);
        
        Page<VehicleRegistrationDto> registrationsPage = new PageImpl<>(
            Collections.singletonList(registrationDto), 
            pageable, 
            1
        );
        
        when(vehicleService.searchRegistrationInfo(eq(searchTerm), any(Pageable.class))).thenReturn(registrationsPage);

        
        mockMvc.perform(get("/api/vehicles/registration/search")
                .param("searchTerm", searchTerm)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("DIESEL")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Search vehicle registrations with type filter should return only matching vehicles of that type")
    void testSearchVehicleRegistrationsWithTypeFilter() throws Exception {
        
        String searchTerm = "E-";
        String type = "ELECTRIC";
        Pageable pageable = PageRequest.of(0, 10);
        
        VehicleRegistrationDto registrationDto = createElectricRegistrationDto(2L);
        
        Page<VehicleRegistrationDto> registrationsPage = new PageImpl<>(
            Collections.singletonList(registrationDto), 
            pageable, 
            1
        );
        
        when(vehicleService.searchRegistrationInfoByType(eq(searchTerm), eq(VehicleType.ELECTRIC), any(Pageable.class)))
            .thenReturn(registrationsPage);

        
        mockMvc.perform(get("/api/vehicles/registration/search")
                .param("searchTerm", searchTerm)
                .param("type", type)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(2)))
                .andExpect(jsonPath("$.content[0].type", is("ELECTRIC")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }
    
    @Test
    @DisplayName("Search vehicle registrations with empty search term should return all registrations")
    void testSearchVehicleRegistrationsWithEmptySearchTerm() throws Exception {
        
        String searchTerm = "";
        Pageable pageable = PageRequest.of(0, 10);
        
        VehicleRegistrationDto dieselRegistration = createDieselRegistrationDto(1L);
        VehicleRegistrationDto electricRegistration = createElectricRegistrationDto(2L);
        
        Page<VehicleRegistrationDto> registrationsPage = new PageImpl<>(
            Arrays.asList(dieselRegistration, electricRegistration), 
            pageable, 
            2
        );
        
        when(vehicleService.searchRegistrationInfo(eq(searchTerm), any(Pageable.class))).thenReturn(registrationsPage);

        
        mockMvc.perform(get("/api/vehicles/registration/search")
                .param("searchTerm", searchTerm)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }
    
    @Test
    @DisplayName("Search vehicle registrations with invalid type should return Bad Request")
    void testSearchVehicleRegistrationsWithInvalidType() throws Exception {
        
        mockMvc.perform(get("/api/vehicles/registration/search")
                .param("searchTerm", "some-text")
                .param("type", "INVALID_TYPE")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Search vehicle registrations with pagination should return the correct page")
    void testSearchVehicleRegistrationsWithPagination() throws Exception {
        
        String searchTerm = "D-";
        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page
        
        VehicleRegistrationDto dieselRegistration1 = createDieselRegistrationDto(1L);
        VehicleRegistrationDto dieselRegistration2 = createDieselRegistrationDto(2L);
        
        Page<VehicleRegistrationDto> registrationsPage = new PageImpl<>(
            Arrays.asList(dieselRegistration1, dieselRegistration2), 
            pageable, 
            12 // Total of 12 elements across all pages
        );
        
        when(vehicleService.searchRegistrationInfo(eq(searchTerm), any(Pageable.class))).thenReturn(registrationsPage);

        
        mockMvc.perform(get("/api/vehicles/registration/search")
                .param("searchTerm", searchTerm)
                .param("page", "1")  // Second page
                .param("size", "5")  // 5 items per page
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(12)))
                .andExpect(jsonPath("$.totalPages", is(3)))  // 12 elements with 5 per page = 3 pages
                .andExpect(jsonPath("$.number", is(1)));     // Page number is 0-indexed
    }
    
    // Helper methods to create test data
    
    private VehicleRegistrationDto createDieselRegistrationDto(Long id) {
        VehicleRegistrationDto dto = new VehicleRegistrationDto();
        dto.setId(id);
        dto.setType(VehicleType.DIESEL);
        dto.setRegistrationInfo("D-12345 + ROTARY");
        dto.setConvertible(false);
        dto.setConversionData(null);
        dto.setHasConversionHistory(false);
        return dto;
    }
    
    private VehicleRegistrationDto createElectricRegistrationDto(Long id) {
        VehicleRegistrationDto dto = new VehicleRegistrationDto();
        dto.setId(id);
        dto.setType(VehicleType.ELECTRIC);
        dto.setRegistrationInfo("E-67890 + 240V + 80A + LITHIUM");
        dto.setConvertible(true);
        dto.setConversionData("E-67890 + POTENTIAL FUELS: B83, B90");
        dto.setHasConversionHistory(true);
        return dto;
    }
}
