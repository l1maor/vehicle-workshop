package com.l1maor.vehicleworkshop.service;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.l1maor.vehicleworkshop.dto.ConversionHistoryDto;
import com.l1maor.vehicleworkshop.dto.UserDto;
import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleRegistrationInfo;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;

@Service
public class DtoMapperService {

    private static final Logger logger = LoggerFactory.getLogger(DtoMapperService.class);

    private final VehicleRepository vehicleRepository;
    private final ConversionHistoryRepository conversionHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DtoMapperService(VehicleRepository vehicleRepository,
                            ConversionHistoryRepository conversionHistoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.conversionHistoryRepository = conversionHistoryRepository;
        logger.debug("DtoMapperService initialized");
    }

    public VehicleDto mapToDto(Vehicle vehicle) {
        if (vehicle == null) {
            logger.debug("Attempted to map null vehicle to DTO");
            return null;
        }

        logger.debug("Mapping vehicle to DTO: id={}, type={}", vehicle.getId(), vehicle.getType());

        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(vehicle.getType());
        dto.setVersion(vehicle.getVersion());
        dto.setConvertible(vehicle.isConvertible());

        if (vehicle instanceof DieselVehicle) {
            DieselVehicle diesel = (DieselVehicle) vehicle;
            dto.setInjectionPumpType(diesel.getInjectionPumpType().name());
            logger.debug("Mapped diesel vehicle: id={}, injectionPumpType={}",
                    vehicle.getId(), diesel.getInjectionPumpType());
        } else if (vehicle instanceof ElectricVehicle) {
            ElectricVehicle electric = (ElectricVehicle) vehicle;
            dto.setBatteryType(electric.getBatteryType().name());
            dto.setBatteryVoltage(electric.getBatteryVoltage());
            dto.setBatteryCurrent(electric.getBatteryCurrent());
            logger.debug("Mapped electric vehicle: id={}, batteryType={}, voltage={}, current={}",
                    vehicle.getId(), electric.getBatteryType(), electric.getBatteryVoltage(), electric.getBatteryCurrent());
        } else if (vehicle instanceof GasVehicle) {
            GasVehicle gas = (GasVehicle) vehicle;
            if (gas.getFuelTypes() != null && !gas.getFuelTypes().isEmpty()) {
                dto.setFuelTypes(gas.getFuelTypes().stream()
                    .map(Enum::name)
                    .toArray(String[]::new));
                logger.debug("Mapped gas vehicle: id={}, fuelTypes={}", vehicle.getId(), gas.getFuelTypes());
            } else {
                logger.debug("Mapped gas vehicle with no fuel types: id={}", vehicle.getId());
            }
        } else if (vehicle.getType() == VehicleType.ELECTRIC) {


            logger.debug("Using direct SQL query to fetch electric vehicle properties for id={}", vehicle.getId());
            try {
                String sql = "SELECT battery_type, battery_voltage, battery_current FROM vehicle WHERE id = ? AND type = 'ELECTRIC'";
                Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter(1, vehicle.getId())
                    .getSingleResult();

                if (result != null) {
                    String batteryType = (String) result[0];
                    Integer voltage = result[1] != null ? ((Number) result[1]).intValue() : null;
                    Integer current = result[2] != null ? ((Number) result[2]).intValue() : null;

                    dto.setBatteryType(batteryType);
                    dto.setBatteryVoltage(voltage);
                    dto.setBatteryCurrent(current);
                    logger.debug("Successfully fetched electric vehicle properties via SQL: batteryType={}, voltage={}, current={}",
                            batteryType, voltage, current);
                } else {
                    logger.warn("SQL query returned null result for electric vehicle with id={}", vehicle.getId());
                }
            } catch (Exception e) {
                logger.warn("Error fetching electric vehicle properties via SQL for id={}: {}", vehicle.getId(), e.getMessage());
                logger.debug("Fallback to repository for electric vehicle properties", e);

                try {
                    Optional<Vehicle> reloadedVehicle = vehicleRepository.findById(vehicle.getId());
                    if (reloadedVehicle.isPresent() && reloadedVehicle.get() instanceof ElectricVehicle) {
                        ElectricVehicle electric = (ElectricVehicle) reloadedVehicle.get();
                        dto.setBatteryType(electric.getBatteryType().name());
                        dto.setBatteryVoltage(electric.getBatteryVoltage());
                        dto.setBatteryCurrent(electric.getBatteryCurrent());
                        logger.debug("Successfully fetched electric vehicle properties via repository: batteryType={}, voltage={}, current={}",
                                electric.getBatteryType(), electric.getBatteryVoltage(), electric.getBatteryCurrent());
                    } else {
                        logger.warn("Failed to fetch electric vehicle properties via repository for id={}", vehicle.getId());
                    }
                } catch (Exception ex) {
                    logger.error("Error in fallback approach for electric vehicle id={}: {}", vehicle.getId(), ex.getMessage());
                    logger.debug("Fallback error details", ex);
                }
            }
        }

        return dto;
    }

    public ConversionHistoryDto mapToDto(ConversionHistory history) {
        if (history == null) {
            logger.debug("Attempted to map null conversion history to DTO");
            return null;
        }

        logger.debug("Mapping conversion history to DTO: id={}, vehicleId={}", history.getId(),
                history.getVehicle() != null ? history.getVehicle().getId() : "null");

        ConversionHistoryDto dto = new ConversionHistoryDto();
        dto.setId(history.getId());
        dto.setVehicleId(history.getVehicle().getId());


        dto.setFromType(history.getPreviousVehicleType() != null ?
                history.getPreviousVehicleType() : VehicleType.ELECTRIC.name());
        dto.setToType(history.getNewVehicleType() != null ?
                history.getNewVehicleType() : VehicleType.GASOLINE.name());


        if (history.getOriginalBatteryType() != null) {
            dto.setOriginalBatteryType(history.getOriginalBatteryType().name());
            dto.setFromType(dto.getFromType() + " (" + history.getOriginalBatteryType().name() + ")");
        }


        dto.setOriginalVoltage(history.getOriginalVoltage());
        dto.setOriginalCurrent(history.getOriginalCurrent());


        dto.setConversionDetails(history.getConversionDetails());


        Optional<Vehicle> currentVehicle = vehicleRepository.findById(history.getVehicle().getId());
        if (currentVehicle.isPresent() && currentVehicle.get().getType() == VehicleType.GASOLINE) {
            if (dto.getConversionDetails() == null) {
                dto.setToType(VehicleType.GASOLINE.name() + " (Converted from Electric)");
            }
        }

        dto.setConversionDate(history.getConversionDate());

        return dto;
    }

    public VehicleRegistrationDto mapToDto(VehicleRegistrationInfo info) {
        if (info == null) {
            logger.debug("Attempted to map null vehicle registration info to DTO");
            return null;
        }

        logger.debug("Mapping vehicle registration info to DTO: id={}, type={}", info.getId(), info.getType());

        VehicleRegistrationDto dto = new VehicleRegistrationDto();
        dto.setId(info.getId());


        try {
            dto.setType(VehicleType.valueOf(info.getType()));
        } catch (IllegalArgumentException e) {

            logger.warn("Invalid vehicle type '{}' in registration info with id={}", info.getType(), info.getId());
            dto.setType(null);
        }

        dto.setRegistrationInfo(info.getRegistrationInfo());
        dto.setConvertible(info.getConvertible());
        dto.setConversionData(info.getConversionData());


        long conversionCount = conversionHistoryRepository.countByVehicleId(info.getId());
        dto.setHasConversionHistory(conversionCount > 0);
        if (conversionCount > 0) {
            logger.debug("Vehicle with id={} has {} conversion history entries", info.getId(), conversionCount);
        }

        return dto;
    }

    public UserDto mapToDto(User user) {
        if (user == null) {
            logger.debug("Attempted to map null user to DTO");
            return null;
        }

        logger.debug("Mapping user to DTO: id={}, username={}", user.getId(), user.getUsername());

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        if (user.getRoleType() != null) {
            dto.setRoleType(user.getRoleType().name());
        }

        return dto;
    }
}
