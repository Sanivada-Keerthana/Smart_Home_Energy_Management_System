package com.srems.srems.controller;

import com.srems.srems.model.Block;
import com.srems.srems.model.CommonArea;
import com.srems.srems.repository.BlockRepository;
import com.srems.srems.repository.CommonAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicLookupController {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private CommonAreaRepository commonAreaRepository;

    @GetMapping("/validate-area")
    public Map<String, Object> validate(@RequestParam String name) {

        // Try BLOCK (A, B, C)
        Optional<Block> block =
                blockRepository.findByBlockNameIgnoreCase(name);

        if (block.isPresent()) {
            return Map.of(
                    "valid", true,
                    "type", "BLOCK",
                    "id", block.get().getBlockId()
            );
        }

        // Try COMMON AREA (Park, Gym)
        Optional<CommonArea> area =
                commonAreaRepository.findByAreaNameIgnoreCase(name);

        if (area.isPresent()) {
            return Map.of(
                    "valid", true,
                    "type", "COMMON_AREA",
                    "id", area.get().getCommonAreaId()
            );
        }

        return Map.of("valid", false);
    }
}