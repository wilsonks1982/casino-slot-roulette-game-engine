package com.wildace.roulette.services;

import com.wildace.roulette.repositories.WheelResultRepository;
import org.springframework.stereotype.Service;

@Service
public class WheelResultService {
    private final WheelResultRepository wheelResultRepository;

    public WheelResultService(WheelResultRepository wheelResultRepository) {
        this.wheelResultRepository = wheelResultRepository;
    }
}
