package com.smartlist.api.inventory.item.controller;

import com.smartlist.api.inventory.item.dto.UnitOfMeasureResponse;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory/items/units")
public class UnitOfMeasureController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitOfMeasureResponse>>> getAllUnits() {
        List<UnitOfMeasureResponse> units = Arrays.stream(UnitOfMeasure.values())
                .map(unit -> new UnitOfMeasureResponse(unit.name(), unit.getLabel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Unidades de medida listadas com sucesso.", units));
    }
}
