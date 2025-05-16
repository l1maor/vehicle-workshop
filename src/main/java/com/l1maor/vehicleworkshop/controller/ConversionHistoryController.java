package com.l1maor.vehicleworkshop.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.l1maor.vehicleworkshop.dto.ConversionHistoryDto;
import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.service.DtoMapperService;

@RestController
@RequestMapping("/api/conversion-history")
public class ConversionHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(ConversionHistoryController.class);
    
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final DtoMapperService dtoMapperService;

    public ConversionHistoryController(ConversionHistoryRepository conversionHistoryRepository,
                                      DtoMapperService dtoMapperService) {
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.dtoMapperService = dtoMapperService;
    }
    
    @GetMapping("/all/paginated")
    public ResponseEntity<Page<ConversionHistoryDto>> getAllConversionHistoryPaginated(Pageable pageable) {
        logger.info("Fetching paginated conversion history: page={}, size={}, sort={}", 
                 pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
                 
        Page<ConversionHistory> historyPage = conversionHistoryRepository.findAll(pageable);
        if (!historyPage.hasContent()) {
            logger.info("No conversion history records found");
            return ResponseEntity.noContent().build();
        }
        
        logger.debug("Found {} conversion history records", historyPage.getTotalElements());
        Page<ConversionHistoryDto> dtoPage = historyPage.map(dtoMapperService::mapToDto);
        return ResponseEntity.ok(dtoPage);
    }

}
