package com.l1maor.vehicleworkshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import com.l1maor.vehicleworkshop.security.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestDataGenerator dataGenerator;

    private DieselVehicle dieselVehicle;
    private ElectricVehicle electricVehicle;
    private GasVehicle gasVehicle;

    @BeforeEach
    void setUp() {
        // Clear database
        dataGenerator.clearDatabase();

        // Create test vehicles
        dieselVehicle = dataGenerator.createDieselVehicle();
        electricVehicle = dataGenerator.createElectricVehicle();
        gasVehicle = dataGenerator.createGasVehicle();

        // Save vehicles to db
        dieselVehicle = (DieselVehicle) vehicleRepository.save(dieselVehicle);
        electricVehicle = (ElectricVehicle) vehicleRepository.save(electricVehicle);
        gasVehicle = (GasVehicle) vehicleRepository.save(gasVehicle);

        // Create admin user for testing
        dataGenerator.createUser("admin", "admin123", "ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        dataGenerator.clearDatabase();
    }

    @Test
    @DisplayName("GET /api/vehicles - Get all vehicles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllVehicles() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].vin", containsInAnyOrder(dieselVehicle.getVin(), electricVehicle.getVin(), gasVehicle.getVin())))
                .andExpect(jsonPath("$[*].licensePlate", containsInAnyOrder(dieselVehicle.getLicensePlate(), electricVehicle.getLicensePlate(), gasVehicle.getLicensePlate())));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - Get vehicle by ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehicleById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + dieselVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(dieselVehicle.getId().intValue())))
                .andExpect(jsonPath("$.vin", is(dieselVehicle.getVin())))
                .andExpect(jsonPath("$.licensePlate", is(dieselVehicle.getLicensePlate())))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.injectionPumpType", is(dieselVehicle.getInjectionPumpType().name())));
    }

    @Test
    @DisplayName("POST /api/vehicles/diesel - Create diesel vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateDieselVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("DIESEL_NEW_VIN");
        dto.setLicensePlate("D-NEW");
        dto.setInjectionPumpType("ROTARY");

        // When & Then
        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin", is("DIESEL_NEW_VIN")))
                .andExpect(jsonPath("$.licensePlate", is("D-NEW")))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.injectionPumpType", is("ROTARY")));

        // Verify vehicle was added to database
        assertTrue(vehicleRepository.existsByVin("DIESEL_NEW_VIN"));
    }

    @Test
    @DisplayName("POST /api/vehicles/electric - Create electric vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateElectricVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("ELECTRIC_NEW_VIN");
        dto.setLicensePlate("E-NEW");
        dto.setBatteryType("LITHIUM");
        dto.setBatteryVoltage(400.0);
        dto.setBatteryCurrent(80.0);

        // When & Then
        mockMvc.perform(post("/api/vehicles/electric")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin", is("ELECTRIC_NEW_VIN")))
                .andExpect(jsonPath("$.licensePlate", is("E-NEW")))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.batteryType", is("LITHIUM")))
                .andExpect(jsonPath("$.batteryVoltage", is(400.0)))
                .andExpect(jsonPath("$.batteryCurrent", is(80.0)));

        // Verify vehicle was added to database
        assertTrue(vehicleRepository.existsByVin("ELECTRIC_NEW_VIN"));
    }

    @Test
    @DisplayName("POST /api/vehicles/gas - Create gasoline vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateGasVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("GAS_NEW_VIN");
        dto.setLicensePlate("G-NEW");
        dto.setFuelTypes(new String[]{"B83", "B94", "B100"});

        // When & Then
        mockMvc.perform(post("/api/vehicles/gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin", is("GAS_NEW_VIN")))
                .andExpect(jsonPath("$.licensePlate", is("G-NEW")))
                .andExpect(jsonPath("$.type", is("GASOLINE")))
                .andExpect(jsonPath("$.fuelTypes", hasSize(3)))
                .andExpect(jsonPath("$.fuelTypes", containsInAnyOrder("B83", "B94", "B100")));

        // Verify vehicle was added to database
        assertTrue(vehicleRepository.existsByVin("GAS_NEW_VIN"));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} - Update vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateVehicle() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setId(dieselVehicle.getId());
        dto.setVin(dieselVehicle.getVin());
        dto.setLicensePlate("D-UPDATED");
        dto.setInjectionPumpType("ROTARY");

        // When & Then
        mockMvc.perform(put("/api/vehicles/" + dieselVehicle.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dieselVehicle.getId().intValue())))
                .andExpect(jsonPath("$.vin", is(dieselVehicle.getVin())))
                .andExpect(jsonPath("$.licensePlate", is("D-UPDATED")))
                .andExpect(jsonPath("$.injectionPumpType", is("ROTARY")));

        // Verify vehicle was updated in database
        Vehicle updated = vehicleRepository.findById(dieselVehicle.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("D-UPDATED", updated.getLicensePlate());
        assertEquals(InjectionPumpType.ROTARY, ((DieselVehicle) updated).getInjectionPumpType());
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Delete vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteVehicle() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/vehicles/" + dieselVehicle.getId()))
                .andExpect(status().isNoContent());

        // Verify vehicle was deleted from database
        assertFalse(vehicleRepository.existsById(dieselVehicle.getId()));
    }

    @Test
    @DisplayName("GET /api/vehicles/type/{type} - Get vehicles by type")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehiclesByType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/type/DIESEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dieselVehicle.getId().intValue())))
                .andExpect(jsonPath("$[0].type", is("DIESEL")));

        mockMvc.perform(get("/api/vehicles/type/ELECTRIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(electricVehicle.getId().intValue())))
                .andExpect(jsonPath("$[0].type", is("ELECTRIC")));

        mockMvc.perform(get("/api/vehicles/type/GASOLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(gasVehicle.getId().intValue())))
                .andExpect(jsonPath("$[0].type", is("GASOLINE")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/is-convertible - Check if vehicle is convertible")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testIsVehicleConvertible() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + electricVehicle.getId() + "/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/vehicles/" + dieselVehicle.getId() + "/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/convert-to-gas - Convert electric to gasoline")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConvertElectricToGas() throws Exception {
        // Given
        String[] fuelTypes = {"B83", "B90"};

        // When & Then
        mockMvc.perform(post("/api/vehicles/" + electricVehicle.getId() + "/convert-to-gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuelTypes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(electricVehicle.getId().intValue())))
                .andExpect(jsonPath("$.vin", is(electricVehicle.getVin())))
                .andExpect(jsonPath("$.licensePlate", is(electricVehicle.getLicensePlate())))
                .andExpect(jsonPath("$.type", is("GASOLINE")))
                .andExpect(jsonPath("$.fuelTypes", hasSize(2)))
                .andExpect(jsonPath("$.fuelTypes", containsInAnyOrder("B83", "B90")));

        // Verify vehicle was converted in database
        Vehicle converted = vehicleRepository.findById(electricVehicle.getId()).orElse(null);
        assertNotNull(converted);
        assertTrue(converted instanceof GasVehicle);
        assertEquals(VehicleType.GASOLINE, converted.getType());
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/registration - Get vehicle registration")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehicleRegistration() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + dieselVehicle.getId() + "/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dieselVehicle.getId().intValue())))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.registrationInfo", containsString(dieselVehicle.getLicensePlate())))
                .andExpect(jsonPath("$.convertible", is(false)));

        mockMvc.perform(get("/api/vehicles/" + electricVehicle.getId() + "/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(electricVehicle.getId().intValue())))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.registrationInfo", containsString(electricVehicle.getVin())))
                .andExpect(jsonPath("$.convertible", is(true)))
                .andExpect(jsonPath("$.conversionData", containsString("POTENTIAL FUELS")));
    }

    @Test
    @DisplayName("GET /api/vehicles/registration - Get all vehicle registrations")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllVehicleRegistrations() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        dieselVehicle.getId().intValue(),
                        electricVehicle.getId().intValue(),
                        gasVehicle.getId().intValue()
                )))
                .andExpect(jsonPath("$[*].type", containsInAnyOrder("DIESEL", "ELECTRIC", "GASOLINE")));
    }

    @Test
    @DisplayName("GET /api/vehicles/stream - Stream vehicle events")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testStreamVehicleEvents() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/stream")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @DisplayName("Authorization - Unauthorized access")
    void testUnauthorizedAccess() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization - Access with insufficient privileges")
    @WithMockUser(username = "user", roles = {"USER"})
    void testInsufficientPrivileges() throws Exception {
        // When & Then - User with USER role attempting admin operations
        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Error Handling - Duplicate VIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateVehicleWithDuplicateVin() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin(dieselVehicle.getVin()); // Duplicate VIN
        dto.setLicensePlate("NEW-PLATE");
        dto.setInjectionPumpType("LINEAR");

        // When & Then
        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Error Handling - Duplicate License Plate")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateVehicleWithDuplicateLicensePlate() throws Exception {
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("NEW-VIN");
        dto.setLicensePlate(dieselVehicle.getLicensePlate()); // Duplicate license plate
        dto.setInjectionPumpType("LINEAR");

        // When & Then
        mockMvc.perform(post("/api/vehicles/diesel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Error Handling - Convert Non-Electric Vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConvertNonElectricVehicle() throws Exception {
        // Given
        String[] fuelTypes = {"B83", "B90"};

        // When & Then
        mockMvc.perform(post("/api/vehicles/" + dieselVehicle.getId() + "/convert-to-gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuelTypes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Only electric vehicles can be converted")));
    }
}
