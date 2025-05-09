package com.l1maor.vehicleworkshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l1maor.vehicleworkshop.config.TestJpaConfig;
import com.l1maor.vehicleworkshop.config.WebMvcTestConfig;
import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.service.SseService;
import com.l1maor.vehicleworkshop.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@ActiveProfiles("test")
@Import({WebMvcTestConfig.class, TestJpaConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;
    
    @MockBean
    private SseService sseService;

    private DieselVehicle dieselVehicle;
    private ElectricVehicle electricVehicle;
    private GasVehicle gasVehicle;
    private VehicleRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        // Setup test vehicles
        dieselVehicle = new DieselVehicle();
        dieselVehicle.setId(1L);
        dieselVehicle.setVin("DIESEL123");
        dieselVehicle.setLicensePlate("D-123");
        dieselVehicle.setInjectionPumpType(InjectionPumpType.LINEAR);

        electricVehicle = new ElectricVehicle();
        electricVehicle.setId(2L);
        electricVehicle.setVin("ELECTRIC456");
        electricVehicle.setLicensePlate("E-456");
        electricVehicle.setBatteryType(BatteryType.LITHIUM);
        electricVehicle.setBatteryVoltage(240.0);
        electricVehicle.setBatteryCurrent(30.0);

        gasVehicle = new GasVehicle();
        gasVehicle.setId(3L);
        gasVehicle.setVin("GAS789");
        gasVehicle.setLicensePlate("G-789");
        gasVehicle.setFuelTypes(Set.of(FuelType.B83, FuelType.B94));

        registrationDto = new VehicleRegistrationDto(
                1L,
                VehicleType.DIESEL,
                "D-123 + LINEAR",
                false,
                null
        );
    }

    @Test
    @DisplayName("GET /api/vehicles - Get all vehicles")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllVehicles() throws Exception {
        // Given
        List<Vehicle> vehicles = Arrays.asList(dieselVehicle, electricVehicle, gasVehicle);
        when(vehicleService.findAllVehicles()).thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].vin", is("DIESEL123")))
                .andExpect(jsonPath("$[0].licensePlate", is("D-123")))
                .andExpect(jsonPath("$[0].type", is("DIESEL")))
                .andExpect(jsonPath("$[0].injectionPumpType", is("LINEAR")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].batteryType", is("LITHIUM")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].fuelTypes", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - Get vehicle by ID")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetVehicleById() throws Exception {
        // Given
        when(vehicleService.findById(1L)).thenReturn(Optional.of(dieselVehicle));

        // When & Then
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.vin", is("DIESEL123")))
                .andExpect(jsonPath("$.licensePlate", is("D-123")))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.injectionPumpType", is("LINEAR")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - Vehicle not found")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetVehicleById_NotFound() throws Exception {
        // Given
        when(vehicleService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/vehicles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/vehicles/diesel - Create diesel vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateDieselVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("DIESEL123");
        dto.setLicensePlate("D-123");
        dto.setInjectionPumpType("LINEAR");

        when(vehicleService.saveDieselVehicle(any(DieselVehicle.class))).thenReturn(dieselVehicle);

        // When & Then
        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.vin", is("DIESEL123")))
                .andExpect(jsonPath("$.licensePlate", is("D-123")))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.injectionPumpType", is("LINEAR")));
    }

    @Test
    @DisplayName("POST /api/vehicles/electric - Create electric vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateElectricVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("ELECTRIC456");
        dto.setLicensePlate("E-456");
        dto.setBatteryType("LITHIUM");
        dto.setBatteryVoltage(240.0);
        dto.setBatteryCurrent(30.0);

        when(vehicleService.saveElectricVehicle(any(ElectricVehicle.class))).thenReturn(electricVehicle);

        // When & Then
        mockMvc.perform(post("/api/vehicles/electric")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.vin", is("ELECTRIC456")))
                .andExpect(jsonPath("$.licensePlate", is("E-456")))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.batteryType", is("LITHIUM")))
                .andExpect(jsonPath("$.batteryVoltage", is(240.0)))
                .andExpect(jsonPath("$.batteryCurrent", is(30.0)));
    }

    @Test
    @DisplayName("POST /api/vehicles/gas - Create gasoline vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateGasVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("GAS789");
        dto.setLicensePlate("G-789");
        dto.setFuelTypes(new String[]{"B83", "B94"});

        when(vehicleService.saveGasVehicle(any(GasVehicle.class))).thenReturn(gasVehicle);

        // When & Then
        mockMvc.perform(post("/api/vehicles/gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.vin", is("GAS789")))
                .andExpect(jsonPath("$.licensePlate", is("G-789")))
                .andExpect(jsonPath("$.type", is("GASOLINE")))
                .andExpect(jsonPath("$.fuelTypes", hasSize(2)))
                .andExpect(jsonPath("$.fuelTypes", containsInAnyOrder("B83", "B94")));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} - Update vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setId(1L);
        dto.setVin("DIESEL123_UPDATED");
        dto.setLicensePlate("D-123-NEW");
        dto.setInjectionPumpType("ROTARY");

        DieselVehicle updatedVehicle = new DieselVehicle();
        updatedVehicle.setId(1L);
        updatedVehicle.setVin("DIESEL123_UPDATED");
        updatedVehicle.setLicensePlate("D-123-NEW");
        updatedVehicle.setInjectionPumpType(InjectionPumpType.ROTARY);

        when(vehicleService.findById(1L)).thenReturn(Optional.of(dieselVehicle));
        when(vehicleService.saveVehicle(any(Vehicle.class))).thenReturn(updatedVehicle);

        // When & Then
        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.vin", is("DIESEL123_UPDATED")))
                .andExpect(jsonPath("$.licensePlate", is("D-123-NEW")))
                .andExpect(jsonPath("$.injectionPumpType", is("ROTARY")));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Delete vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteVehicle() throws Exception {
        // Given
        when(vehicleService.deleteVehicle(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/vehicles/type/{type} - Get vehicles by type")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetVehiclesByType() throws Exception {
        // Given
        when(vehicleService.findByType(VehicleType.DIESEL)).thenReturn(List.of(dieselVehicle));

        // When & Then
        mockMvc.perform(get("/api/vehicles/type/DIESEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].type", is("DIESEL")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/is-convertible - Check if vehicle is convertible")
    @WithMockUser(username = "user", roles = {"USER"})
    void testIsVehicleConvertible() throws Exception {
        // Given
        when(vehicleService.isVehicleConvertible(2L)).thenReturn(true);
        when(vehicleService.isVehicleConvertible(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/vehicles/2/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/vehicles/1/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/convert-to-gas - Convert electric to gasoline")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConvertElectricToGas() throws Exception {
        // Given
        String[] fuelTypes = {"B83", "B90"};

        Set<FuelType> fuelTypesSet = EnumSet.of(FuelType.B83, FuelType.B90);
        GasVehicle convertedVehicle = new GasVehicle();
        convertedVehicle.setId(2L);
        convertedVehicle.setVin("ELECTRIC456");
        convertedVehicle.setLicensePlate("E-456");
        convertedVehicle.setFuelTypes(fuelTypesSet);

        when(vehicleService.convertElectricToGas(eq(2L), any())).thenReturn(convertedVehicle);

        // When & Then
        mockMvc.perform(post("/api/vehicles/2/convert-to-gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuelTypes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.vin", is("ELECTRIC456")))
                .andExpect(jsonPath("$.licensePlate", is("E-456")))
                .andExpect(jsonPath("$.type", is("GASOLINE")))
                .andExpect(jsonPath("$.fuelTypes", hasSize(2)))
                .andExpect(jsonPath("$.fuelTypes", containsInAnyOrder("B83", "B90")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/registration - Get vehicle registration")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetVehicleRegistration() throws Exception {
        // Given
        when(vehicleService.getRegistrationInfo(1L)).thenReturn(registrationDto);

        // When & Then
        mockMvc.perform(get("/api/vehicles/1/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.registrationInfo", is("D-123 + LINEAR")))
                .andExpect(jsonPath("$.convertible", is(false)))
                .andExpect(jsonPath("$.conversionData").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/vehicles/registration - Get all vehicle registrations")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllVehicleRegistrations() throws Exception {
        // Given
        VehicleRegistrationDto electricRegistrationDto = new VehicleRegistrationDto(
                2L,
                VehicleType.ELECTRIC,
                "ELECTRIC456 + 240.0V + 30.0A + LITHIUM",
                true,
                "E-456 + POTENTIAL FUELS: B83, B90, B94, B100"
        );

        when(vehicleService.getAllRegistrationInfo()).thenReturn(Arrays.asList(registrationDto, electricRegistrationDto));

        // When & Then
        mockMvc.perform(get("/api/vehicles/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].type", is("DIESEL")))
                .andExpect(jsonPath("$[0].registrationInfo", is("D-123 + LINEAR")))
                .andExpect(jsonPath("$[0].convertible", is(false)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].type", is("ELECTRIC")))
                .andExpect(jsonPath("$[1].registrationInfo", is("ELECTRIC456 + 240.0V + 30.0A + LITHIUM")))
                .andExpect(jsonPath("$[1].convertible", is(true)))
                .andExpect(jsonPath("$[1].conversionData", containsString("E-456")));
    }

    @Test
    @DisplayName("GET /api/vehicles/stream - Stream vehicle events")
    @WithMockUser(username = "user", roles = {"USER"})
    void testStreamVehicleEvents() throws Exception {
        // Given
        SseEmitter emitter = new SseEmitter();
        when(sseService.createEmitter()).thenReturn(emitter);

        // When & Then
        mockMvc.perform(get("/api/vehicles/stream")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }
}
