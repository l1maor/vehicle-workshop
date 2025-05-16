package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.Vehicle;
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
import java.util.Optional;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class VehicleSearchControllerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private SseService sseService;

    @Mock
    private DtoMapperService dtoMapperService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;
    private TestDataGenerator dataGenerator;

    @BeforeEach
    void setUp() {
        dataGenerator = new TestDataGenerator();
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    @DisplayName("Get all vehicles should return a list of vehicles")
    void testGetAllVehicles() throws Exception {

        DieselVehicle dieselVehicle = dataGenerator.createDieselVehicle(1);
        ElectricVehicle electricVehicle = dataGenerator.createElectricVehicle(2);

        VehicleDto dieselDto = new VehicleDto();
        dieselDto.setId(1L);
        dieselDto.setVin(dieselVehicle.getVin());
        dieselDto.setType(VehicleType.DIESEL);

        VehicleDto electricDto = new VehicleDto();
        electricDto.setId(2L);
        electricDto.setVin(electricVehicle.getVin());
        electricDto.setType(VehicleType.ELECTRIC);

        when(vehicleService.findAllVehicles()).thenReturn(Arrays.asList(dieselVehicle, electricVehicle));
        when(dtoMapperService.mapToDto(dieselVehicle)).thenReturn(dieselDto);
        when(dtoMapperService.mapToDto(electricVehicle)).thenReturn(electricDto);


        mockMvc.perform(get("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].vin", is(dieselVehicle.getVin())))
                .andExpect(jsonPath("$[1].vin", is(electricVehicle.getVin())));
    }

    @Test
    @DisplayName("Get vehicle by ID should return the vehicle")
    void testGetVehicleById() throws Exception {

        Long vehicleId = 1L;
        DieselVehicle vehicle = dataGenerator.createDieselVehicle(1);
        vehicle.setId(vehicleId);

        VehicleDto dto = new VehicleDto();
        dto.setId(vehicleId);
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(VehicleType.DIESEL);
        dto.setInjectionPumpType(vehicle.getInjectionPumpType().name());

        when(vehicleService.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(dtoMapperService.mapToDto(vehicle)).thenReturn(dto);


        mockMvc.perform(get("/api/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(vehicleId.intValue())))
                .andExpect(jsonPath("$.vin", is(vehicle.getVin())))
                .andExpect(jsonPath("$.type", is("DIESEL")));
    }

    @Test
    @DisplayName("Get vehicle by ID that doesn't exist should return 404")
    void testGetVehicleByIdNotFound() throws Exception {

        Long vehicleId = 999L;
        when(vehicleService.findById(vehicleId)).thenReturn(Optional.empty());


        mockMvc.perform(get("/api/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get all vehicles paginated should return paginated results")
    void testGetAllVehiclesPaginated() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);
        DieselVehicle vehicle1 = dataGenerator.createDieselVehicle(1);
        ElectricVehicle vehicle2 = dataGenerator.createElectricVehicle(2);

        Page<Vehicle> vehiclePage = new PageImpl<>(Arrays.asList(vehicle1, vehicle2), pageable, 2);

        VehicleDto dto1 = new VehicleDto();
        dto1.setId(1L);
        dto1.setVin(vehicle1.getVin());
        dto1.setType(VehicleType.DIESEL);

        VehicleDto dto2 = new VehicleDto();
        dto2.setId(2L);
        dto2.setVin(vehicle2.getVin());
        dto2.setType(VehicleType.ELECTRIC);

        when(vehicleService.findAllVehiclesPaginated(any(Pageable.class))).thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(vehicle1)).thenReturn(dto1);
        when(dtoMapperService.mapToDto(vehicle2)).thenReturn(dto2);


        mockMvc.perform(get("/api/vehicles/paginated")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].vin", is(vehicle1.getVin())))
                .andExpect(jsonPath("$.content[1].vin", is(vehicle2.getVin())));
    }

    @Test
    @DisplayName("Search vehicles with no filters should return matching results")
    void testSearchVehicles() throws Exception {

        String searchTerm = "ABC123";
        Pageable pageable = PageRequest.of(0, 10);
        DieselVehicle vehicle = dataGenerator.createDieselVehicle(1);
        vehicle.setId(1L);
        vehicle.setVin("ABC123456");
        vehicle.setLicensePlate("ABC-123");

        Page<Vehicle> vehiclePage = new PageImpl<>(Collections.singletonList(vehicle), pageable, 1);

        VehicleDto dto = new VehicleDto();
        dto.setId(1L);
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(VehicleType.DIESEL);

        when(vehicleService.searchVehicles(eq(searchTerm), any(Pageable.class))).thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(vehicle)).thenReturn(dto);


        mockMvc.perform(get("/api/vehicles/search")
                .param("searchTerm", searchTerm)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].vin", is("ABC123456")))
                .andExpect(jsonPath("$.content[0].licensePlate", is("ABC-123")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Search vehicles with type filter should return only matching vehicles of that type")
    void testSearchVehiclesWithTypeFilter() throws Exception {

        String searchTerm = "ABC123";
        String type = "ELECTRIC";
        Pageable pageable = PageRequest.of(0, 10);

        ElectricVehicle vehicle = dataGenerator.createElectricVehicle(1);
        vehicle.setId(1L);
        vehicle.setVin("ELC123456");
        vehicle.setLicensePlate("E-ABC-123");

        Page<Vehicle> vehiclePage = new PageImpl<>(Collections.singletonList(vehicle), pageable, 1);

        VehicleDto dto = new VehicleDto();
        dto.setId(1L);
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(VehicleType.ELECTRIC);
        dto.setBatteryType(vehicle.getBatteryType().name());

        when(vehicleService.searchVehiclesByType(eq(searchTerm), eq(VehicleType.ELECTRIC), any(Pageable.class)))
            .thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(vehicle)).thenReturn(dto);


        mockMvc.perform(get("/api/vehicles/search")
                .param("searchTerm", searchTerm)
                .param("type", type)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].vin", is("ELC123456")))
                .andExpect(jsonPath("$.content[0].licensePlate", is("E-ABC-123")))
                .andExpect(jsonPath("$.content[0].type", is("ELECTRIC")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Search vehicles with empty search term should return all vehicles")
    void testSearchVehiclesWithEmptySearchTerm() throws Exception {

        String searchTerm = "";
        Pageable pageable = PageRequest.of(0, 10);

        DieselVehicle diesel = dataGenerator.createDieselVehicle(1);
        diesel.setId(1L);
        ElectricVehicle electric = dataGenerator.createElectricVehicle(2);
        electric.setId(2L);

        Page<Vehicle> vehiclePage = new PageImpl<>(Arrays.asList(diesel, electric), pageable, 2);

        VehicleDto dieselDto = new VehicleDto();
        dieselDto.setId(1L);
        dieselDto.setVin(diesel.getVin());
        dieselDto.setType(VehicleType.DIESEL);

        VehicleDto electricDto = new VehicleDto();
        electricDto.setId(2L);
        electricDto.setVin(electric.getVin());
        electricDto.setType(VehicleType.ELECTRIC);

        when(vehicleService.searchVehicles(eq(searchTerm), any(Pageable.class))).thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(diesel)).thenReturn(dieselDto);
        when(dtoMapperService.mapToDto(electric)).thenReturn(electricDto);


        mockMvc.perform(get("/api/vehicles/search")
                .param("searchTerm", searchTerm)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Search vehicles with invalid type should return Bad Request")
    void testSearchVehiclesWithInvalidType() throws Exception {

        mockMvc.perform(get("/api/vehicles/search")
                .param("searchTerm", "ABC")
                .param("type", "INVALID_TYPE")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search vehicles with pagination should return the correct page")
    void testSearchVehiclesWithPagination() throws Exception {

        String searchTerm = "ABC";
        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page

        DieselVehicle vehicle1 = dataGenerator.createDieselVehicle(1);
        vehicle1.setId(1L);
        DieselVehicle vehicle2 = dataGenerator.createDieselVehicle(2);
        vehicle2.setId(2L);

        Page<Vehicle> vehiclePage = new PageImpl<>(
            Arrays.asList(vehicle1, vehicle2),
            pageable,
            12 // Total of 12 elements across all pages
        );

        VehicleDto dto1 = new VehicleDto();
        dto1.setId(1L);
        dto1.setVin(vehicle1.getVin());
        dto1.setType(VehicleType.DIESEL);

        VehicleDto dto2 = new VehicleDto();
        dto2.setId(2L);
        dto2.setVin(vehicle2.getVin());
        dto2.setType(VehicleType.DIESEL);

        when(vehicleService.searchVehicles(eq(searchTerm), any(Pageable.class))).thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(vehicle1)).thenReturn(dto1);
        when(dtoMapperService.mapToDto(vehicle2)).thenReturn(dto2);


        mockMvc.perform(get("/api/vehicles/search")
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

    @Test
    @DisplayName("Get vehicles by type should return vehicles of that type")
    void testGetVehiclesByType() throws Exception {

        String type = "DIESEL";
        DieselVehicle vehicle1 = dataGenerator.createDieselVehicle(1);
        vehicle1.setId(1L);
        DieselVehicle vehicle2 = dataGenerator.createDieselVehicle(2);
        vehicle2.setId(2L);

        VehicleDto dto1 = new VehicleDto();
        dto1.setId(1L);
        dto1.setVin(vehicle1.getVin());
        dto1.setType(VehicleType.DIESEL);

        VehicleDto dto2 = new VehicleDto();
        dto2.setId(2L);
        dto2.setVin(vehicle2.getVin());
        dto2.setType(VehicleType.DIESEL);

        when(vehicleService.findByType(VehicleType.DIESEL)).thenReturn(Arrays.asList(vehicle1, vehicle2));
        when(dtoMapperService.mapToDto(vehicle1)).thenReturn(dto1);
        when(dtoMapperService.mapToDto(vehicle2)).thenReturn(dto2);


        mockMvc.perform(get("/api/vehicles/type/{type}", type)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("DIESEL")))
                .andExpect(jsonPath("$[1].type", is("DIESEL")));
    }

    @Test
    @DisplayName("Get vehicles by type paginated should return paginated vehicles of that type")
    void testGetVehiclesByTypePaginated() throws Exception {

        String type = "GASOLINE";
        Pageable pageable = PageRequest.of(0, 5);

        GasVehicle vehicle1 = dataGenerator.createGasVehicle(1);
        vehicle1.setId(1L);
        GasVehicle vehicle2 = dataGenerator.createGasVehicle(2);
        vehicle2.setId(2L);

        Page<Vehicle> vehiclePage = new PageImpl<>(
            Arrays.asList(vehicle1, vehicle2),
            pageable,
            2
        );

        VehicleDto dto1 = new VehicleDto();
        dto1.setId(1L);
        dto1.setVin(vehicle1.getVin());
        dto1.setType(VehicleType.GASOLINE);

        VehicleDto dto2 = new VehicleDto();
        dto2.setId(2L);
        dto2.setVin(vehicle2.getVin());
        dto2.setType(VehicleType.GASOLINE);

        when(vehicleService.findByTypePaginated(eq(VehicleType.GASOLINE), any(Pageable.class))).thenReturn(vehiclePage);
        when(dtoMapperService.mapToDto(vehicle1)).thenReturn(dto1);
        when(dtoMapperService.mapToDto(vehicle2)).thenReturn(dto2);


        mockMvc.perform(get("/api/vehicles/type/{type}/paginated", type)
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].type", is("GASOLINE")))
                .andExpect(jsonPath("$.content[1].type", is("GASOLINE")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }
}
