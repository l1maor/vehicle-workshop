package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ConversionHistoryRepository conversionHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SseService sseService;

    private VehicleService vehicleService;
    private TestDataGenerator dataGenerator;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(
                vehicleRepository, 
                conversionHistoryRepository,
                eventPublisher,
                sseService
        );
        
        dataGenerator = new TestDataGenerator();
    }

    @Test
    void testSaveVehicle_DuplicateVin() {
        // Given
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin("VIN123");
        vehicle.setLicensePlate("LP123");
        
        when(vehicleRepository.existsByVin("VIN123")).thenReturn(true);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.saveVehicle(vehicle);
        });
        
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testSaveVehicle_DuplicateLicensePlate() {
        // Given
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin("VIN123");
        vehicle.setLicensePlate("LP123");
        
        when(vehicleRepository.existsByVin("VIN123")).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate("LP123")).thenReturn(true);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.saveVehicle(vehicle);
        });
        
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testSaveVehicle_Success() {
        // Given
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin("VIN123");
        vehicle.setLicensePlate("LP123");
        vehicle.setInjectionPumpType(InjectionPumpType.LINEAR);
        
        when(vehicleRepository.existsByVin("VIN123")).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate("LP123")).thenReturn(false);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);
        
        // When
        Vehicle saved = vehicleService.saveVehicle(vehicle);
        
        // Then
        assertNotNull(saved);
        assertEquals("VIN123", saved.getVin());
        assertEquals("LP123", saved.getLicensePlate());
        assertEquals(InjectionPumpType.LINEAR, ((DieselVehicle)saved).getInjectionPumpType());
        
        verify(vehicleRepository).save(vehicle);
        verify(sseService).broadcastVehicleUpdate(vehicle);
    }

    @Test
    void testFindByType() {
        // Given
        DieselVehicle diesel = new DieselVehicle();
        diesel.setVin("D123");
        diesel.setLicensePlate("DLP123");
        diesel.setInjectionPumpType(InjectionPumpType.LINEAR);
        
        List<Vehicle> dieselVehicles = Collections.singletonList(diesel);
        when(vehicleRepository.findByType(VehicleType.DIESEL)).thenReturn(dieselVehicles);
        
        // When
        List<Vehicle> found = vehicleService.findByType(VehicleType.DIESEL);
        
        // Then
        assertEquals(1, found.size());
        assertTrue(found.get(0) instanceof DieselVehicle);
        assertEquals("D123", found.get(0).getVin());
    }

    @Test
    void testDeleteVehicle_NotFound() {
        // Given
        Long id = 1L;
        when(vehicleRepository.existsById(id)).thenReturn(false);
        
        // When
        boolean result = vehicleService.deleteVehicle(id);
        
        // Then
        assertFalse(result);
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
        verify(sseService, never()).broadcastVehicleDelete(any(Long.class));
    }

    @Test
    void testDeleteVehicle_Success() {
        // Given
        Long id = 1L;
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setId(id);
        
        when(vehicleRepository.existsById(id)).thenReturn(true);
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));
        
        // When
        boolean result = vehicleService.deleteVehicle(id);
        
        // Then
        assertTrue(result);
        verify(vehicleRepository).delete(vehicle);
        verify(sseService).broadcastVehicleDelete(id);
    }

    @Test
    void testConvertElectricToGas_NotElectric() {
        // Given
        Long id = 1L;
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setId(id);
        
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            vehicleService.convertElectricToGas(id, EnumSet.of(FuelType.B83));
        });
        
        verify(conversionHistoryRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any(GasVehicle.class));
    }

    @Test
    void testConvertElectricToGas_Success() {
        // Given
        Long id = 1L;
        ElectricVehicle electric = new ElectricVehicle();
        electric.setId(id);
        electric.setVin("E123");
        electric.setLicensePlate("ELP123");
        electric.setBatteryType(BatteryType.LITHIUM);
        electric.setBatteryVoltage(240.0);
        electric.setBatteryCurrent(30.0);
        
        Set<FuelType> newFuelTypes = EnumSet.of(FuelType.B83, FuelType.B90);
        
        GasVehicle gasVehicle = new GasVehicle();
        gasVehicle.setId(id);
        gasVehicle.setVin("E123");
        gasVehicle.setLicensePlate("ELP123");
        gasVehicle.setFuelTypes(newFuelTypes);
        
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(electric));
        when(vehicleRepository.save(any(GasVehicle.class))).thenReturn(gasVehicle);
        
        // When
        GasVehicle converted = vehicleService.convertElectricToGas(id, newFuelTypes);
        
        // Then
        assertNotNull(converted);
        assertEquals(id, converted.getId());
        assertEquals("E123", converted.getVin());
        assertEquals("ELP123", converted.getLicensePlate());
        assertEquals(2, converted.getFuelTypes().size());
        assertTrue(converted.getFuelTypes().contains(FuelType.B83));
        assertTrue(converted.getFuelTypes().contains(FuelType.B90));
        
        verify(conversionHistoryRepository).save(any());
        verify(vehicleRepository).save(any(GasVehicle.class));
        verify(sseService).broadcastVehicleUpdate(converted);
    }

    @Test
    void testIsVehicleConvertible() {
        // Given
        ElectricVehicle electric = new ElectricVehicle();
        DieselVehicle diesel = new DieselVehicle();
        GasVehicle gas = new GasVehicle();
        
        // When & Then
        assertTrue(vehicleService.isVehicleConvertible(electric));
        assertFalse(vehicleService.isVehicleConvertible(diesel));
        assertFalse(vehicleService.isVehicleConvertible(gas));
    }

    @Test
    void testGetRegistrationInfo_Diesel() {
        // Given
        Long id = 1L;
        DieselVehicle diesel = new DieselVehicle();
        diesel.setId(id);
        diesel.setVin("D123");
        diesel.setLicensePlate("DLP123");
        diesel.setInjectionPumpType(InjectionPumpType.LINEAR);
        
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(diesel));
        
        // When
        VehicleRegistrationDto dto = vehicleService.getRegistrationInfo(id);
        
        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(VehicleType.DIESEL, dto.getType());
        assertEquals("DLP123 + LINEAR", dto.getRegistrationInfo());
        assertFalse(dto.isConvertible());
        assertNull(dto.getConversionData());
    }

    @Test
    void testGetRegistrationInfo_Electric() {
        // Given
        Long id = 1L;
        ElectricVehicle electric = new ElectricVehicle();
        electric.setId(id);
        electric.setVin("E123");
        electric.setLicensePlate("ELP123");
        electric.setBatteryType(BatteryType.LITHIUM);
        electric.setBatteryVoltage(240.0);
        electric.setBatteryCurrent(30.0);
        
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(electric));
        
        // When
        VehicleRegistrationDto dto = vehicleService.getRegistrationInfo(id);
        
        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(VehicleType.ELECTRIC, dto.getType());
        assertEquals("E123 + 240.0V + 30.0A + LITHIUM", dto.getRegistrationInfo());
        assertTrue(dto.isConvertible());
        assertTrue(dto.getConversionData().contains("ELP123"));
        assertTrue(dto.getConversionData().contains("POTENTIAL FUELS"));
    }

    @Test
    void testGetRegistrationInfo_Gas() {
        // Given
        Long id = 1L;
        GasVehicle gas = new GasVehicle();
        gas.setId(id);
        gas.setVin("G123");
        gas.setLicensePlate("GLP123");
        gas.setFuelTypes(EnumSet.of(FuelType.B83, FuelType.B94));
        
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(gas));
        
        // When
        VehicleRegistrationDto dto = vehicleService.getRegistrationInfo(id);
        
        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(VehicleType.GASOLINE, dto.getType());
        assertTrue(dto.getRegistrationInfo().startsWith("GLP123 + FUELS:"));
        assertTrue(dto.getRegistrationInfo().contains("B83"));
        assertTrue(dto.getRegistrationInfo().contains("B94"));
        assertFalse(dto.isConvertible());
        assertNull(dto.getConversionData());
    }

    @Test
    void testGetRegistrationInfo_NotFound() {
        // Given
        Long id = 1L;
        when(vehicleRepository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            vehicleService.getRegistrationInfo(id);
        });
    }
}
