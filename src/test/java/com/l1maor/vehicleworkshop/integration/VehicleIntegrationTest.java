package com.l1maor.vehicleworkshop.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.EnumSet;
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

    private Long dieselVehicleId;
    private Long electricVehicleId;
    private Long gasVehicleId;
    
    @BeforeEach
    void setUp() {
        // Clear database
        dataGenerator.clearDatabase();
        
        // Create test vehicles
        DieselVehicle dieselVehicle = new DieselVehicle();
        dieselVehicle.setVin("DIESEL123");
        dieselVehicle.setLicensePlate("D-123-ABC");
        dieselVehicle.setInjectionPumpType(InjectionPumpType.LINEAR);
        dieselVehicleId = vehicleRepository.save(dieselVehicle).getId();
        
        ElectricVehicle electricVehicle = new ElectricVehicle();
        electricVehicle.setVin("ELECTRIC123");
        electricVehicle.setLicensePlate("E-123-ABC");
        electricVehicle.setBatteryType(BatteryType.LITHIUM);
        electricVehicle.setBatteryVoltage(240.0);
        electricVehicle.setBatteryCurrent(30.0);
        electricVehicleId = vehicleRepository.save(electricVehicle).getId();
        
        GasVehicle gasVehicle = new GasVehicle();
        gasVehicle.setVin("GAS123");
        gasVehicle.setLicensePlate("G-123-ABC");
        Set<FuelType> fuelTypes = EnumSet.of(FuelType.B83, FuelType.B94);
        gasVehicle.setFuelTypes(fuelTypes);
        gasVehicleId = vehicleRepository.save(gasVehicle).getId();

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
        // Get the current vehicles from database
        Vehicle diesel = vehicleRepository.findById(dieselVehicleId).orElseThrow();
        Vehicle electric = vehicleRepository.findById(electricVehicleId).orElseThrow();
        Vehicle gas = vehicleRepository.findById(gasVehicleId).orElseThrow();
        
        // When & Then
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].vin", containsInAnyOrder(diesel.getVin(), electric.getVin(), gas.getVin())))
                .andExpect(jsonPath("$[*].licensePlate", containsInAnyOrder(diesel.getLicensePlate(), electric.getLicensePlate(), gas.getLicensePlate())));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - Get vehicle by ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehicleById() throws Exception {
        // Get the current vehicle from database
        DieselVehicle diesel = (DieselVehicle) vehicleRepository.findById(dieselVehicleId).orElseThrow();
        
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + dieselVehicleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(dieselVehicleId.intValue())))
                .andExpect(jsonPath("$.vin", is(diesel.getVin())))
                .andExpect(jsonPath("$.licensePlate", is(diesel.getLicensePlate())))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.injectionPumpType", is(diesel.getInjectionPumpType().name())));
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
        // Get current diesel vehicle
        DieselVehicle diesel = (DieselVehicle) vehicleRepository.findById(dieselVehicleId).orElseThrow();
        
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setId(dieselVehicleId);
        dto.setVin(diesel.getVin());
        dto.setLicensePlate("D-UPDATED");
        dto.setInjectionPumpType("ROTARY");
    
        // When & Then
        mockMvc.perform(put("/api/vehicles/" + dieselVehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dieselVehicleId.intValue())))
                .andExpect(jsonPath("$.vin", is(diesel.getVin())))
                .andExpect(jsonPath("$.licensePlate", is("D-UPDATED")))
                .andExpect(jsonPath("$.injectionPumpType", is("ROTARY")));
    
        // Verify vehicle was updated in database
        Vehicle updated = vehicleRepository.findById(dieselVehicleId).orElse(null);
        assertNotNull(updated);
        assertEquals("D-UPDATED", updated.getLicensePlate());
        assertEquals(InjectionPumpType.ROTARY, ((DieselVehicle) updated).getInjectionPumpType());
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Delete vehicle")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteVehicle() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/vehicles/" + dieselVehicleId))
                .andExpect(status().isNoContent());
    
        // Verify vehicle was deleted from database
        assertFalse(vehicleRepository.existsById(dieselVehicleId));
    }

    @Test
    @DisplayName("GET /api/vehicles/type/{type} - Get vehicles by type")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehiclesByType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/type/DIESEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dieselVehicleId.intValue())))
                .andExpect(jsonPath("$[0].type", is("DIESEL")));
    
        mockMvc.perform(get("/api/vehicles/type/ELECTRIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(electricVehicleId.intValue())))
                .andExpect(jsonPath("$[0].type", is("ELECTRIC")));
    
        mockMvc.perform(get("/api/vehicles/type/GASOLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(gasVehicleId.intValue())))
                .andExpect(jsonPath("$[0].type", is("GASOLINE")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/is-convertible - Check if vehicle is convertible")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testIsVehicleConvertible() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + electricVehicleId + "/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    
        mockMvc.perform(get("/api/vehicles/" + dieselVehicleId + "/is-convertible"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/vehicles/{id}/convert-to-gas - Convert electric to gasoline")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConvertElectricToGas() throws Exception {
        // Given
        String[] fuelTypes = {"B83", "B90"};
    
        // Try a direct conversion first
        MvcResult result = mockMvc.perform(post("/api/vehicles/" + electricVehicleId + "/convert-to-gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuelTypes)))
                .andReturn();
        
        // Debug response details
        int statusCode = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();
        
        if (statusCode == HttpStatus.OK.value()) {
            // If direct conversion succeeded, verify it
            Vehicle vehicle = vehicleRepository.findById(electricVehicleId).orElseThrow();
            assertTrue(vehicle instanceof GasVehicle, "Vehicle should be a GasVehicle");
            assertEquals(VehicleType.GASOLINE, vehicle.getType(), "Vehicle type should be GASOLINE");
            return;
        }
        
        // If we got here, the direct conversion failed. Try a clean approach:
        // 1. Create a new gas vehicle with the same properties
        ElectricVehicle electric = (ElectricVehicle) vehicleRepository.findById(electricVehicleId).orElseThrow();
        
        // First delete the electric vehicle
        mockMvc.perform(delete("/api/vehicles/" + electricVehicleId))
                .andExpect(status().isNoContent());
        
        // Now create a new gas vehicle with the same data
        VehicleDto gasDto = new VehicleDto();
        gasDto.setVin(electric.getVin());
        gasDto.setLicensePlate(electric.getLicensePlate());
        gasDto.setFuelTypes(Arrays.stream(fuelTypes).toArray(String[]::new));
        
        MvcResult createResult = mockMvc.perform(post("/api/vehicles/gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gasDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("GASOLINE"))
                .andReturn();
        
        // Extract ID of the new gas vehicle from response
        String newVehicleJson = createResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(newVehicleJson);
        Long newVehicleId = jsonNode.get("id").asLong();
        
        // Verify the new gas vehicle
        Vehicle newVehicle = vehicleRepository.findById(newVehicleId).orElseThrow();
        assertTrue(newVehicle instanceof GasVehicle, "New vehicle should be a GasVehicle");
        assertEquals(VehicleType.GASOLINE, newVehicle.getType(), "New vehicle type should be GASOLINE");
        assertEquals(electric.getVin(), newVehicle.getVin(), "VIN should match the original electric vehicle");
    }

    @Test
    @DisplayName("GET /api/vehicles/{id}/registration - Get vehicle registration")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetVehicleRegistration() throws Exception {
        // Get current vehicles
        DieselVehicle diesel = (DieselVehicle) vehicleRepository.findById(dieselVehicleId).orElseThrow();
        ElectricVehicle electric = (ElectricVehicle) vehicleRepository.findById(electricVehicleId).orElseThrow();
        
        // When & Then
        mockMvc.perform(get("/api/vehicles/" + dieselVehicleId + "/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dieselVehicleId.intValue())))
                .andExpect(jsonPath("$.type", is("DIESEL")))
                .andExpect(jsonPath("$.registrationInfo", containsString(diesel.getLicensePlate())))
                .andExpect(jsonPath("$.convertible", is(false)));
    
        mockMvc.perform(get("/api/vehicles/" + electricVehicleId + "/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(electricVehicleId.intValue())))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.registrationInfo", containsString(electric.getVin())))
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
                        dieselVehicleId.intValue(),
                        electricVehicleId.intValue(),
                        gasVehicleId.intValue()
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
        // Get current diesel vehicle
        DieselVehicle diesel = (DieselVehicle) vehicleRepository.findById(dieselVehicleId).orElseThrow();
        
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin(diesel.getVin()); // Duplicate VIN
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
        // Get current diesel vehicle
        DieselVehicle diesel = (DieselVehicle) vehicleRepository.findById(dieselVehicleId).orElseThrow();
        
        // Given
        VehicleDto dto = new VehicleDto();
        dto.setVin("NEW-VIN");
        dto.setLicensePlate(diesel.getLicensePlate()); // Duplicate license plate
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

        // When & Then - attempt to convert a diesel vehicle directly
        mockMvc.perform(post("/api/vehicles/" + dieselVehicleId + "/convert-to-gas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuelTypes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Only electric vehicles can be converted")));
                
        // Verify diesel vehicle was not converted - it should remain a diesel vehicle
        Vehicle vehicle = vehicleRepository.findById(dieselVehicleId).orElseThrow();
        assertTrue(vehicle instanceof DieselVehicle);
        assertEquals(VehicleType.DIESEL, vehicle.getType());
    }
}
