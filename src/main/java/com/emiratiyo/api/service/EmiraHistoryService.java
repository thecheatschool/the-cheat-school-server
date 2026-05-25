package com.emiratiyo.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.emiratiyo.api.dto.EmiraHistoryResponse;
import com.emiratiyo.api.entity.EmiraAnalysisEntity;
import com.emiratiyo.api.exception.ResourceNotFoundException;
import com.emiratiyo.api.repository.EmiraAnalysisRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmiraHistoryService {

    private final EmiraAnalysisRepository emiraAnalysisRepository;

    /**
     * Returns a lightweight summary list, cached for 10 minutes.
     * Cache is evicted whenever a new analysis is saved or any entry is deleted.
     */
    @Cacheable(value = "emiraHistory", key = "'all'")
    public List<EmiraHistoryResponse> getHistory() {
        log.debug("Cache miss — loading Emira history list from database");
        return emiraAnalysisRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> EmiraHistoryResponse.builder()
                        .id(a.getId())
                        .area(a.getArea())
                        .analysisType(a.getAnalysisType())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Returns the full analysis (including responseText), cached per ID for 30 minutes.
     */
    @Cacheable(value = "emiraHistoryItem", key = "#id")
    public EmiraAnalysisEntity getById(Long id) {
        log.debug("Cache miss — loading Emira analysis {} from database", id);
        return emiraAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis history not found for ID: " + id));
    }

    public boolean existsById(Long id) {
        return emiraAnalysisRepository.existsById(id);
    }

    /**
     * Persists a new analysis and evicts the list cache so the next GET reflects the new entry.
     */
    @CacheEvict(value = "emiraHistory", key = "'all'")
    public EmiraAnalysisEntity save(EmiraAnalysisEntity analysis) {
        EmiraAnalysisEntity saved = emiraAnalysisRepository.save(analysis);
        log.info("Saved Emira analysis id={}, area={} — history list cache evicted", saved.getId(), saved.getArea());
        return saved;
    }

    /**
     * Deletes an analysis and evicts both the list cache and the specific item cache.
     */
    @Caching(evict = {
            @CacheEvict(value = "emiraHistory", key = "'all'"),
            @CacheEvict(value = "emiraHistoryItem", key = "#id")
    })
    public void deleteById(Long id) {
        emiraAnalysisRepository.deleteById(id);
        log.info("Deleted Emira analysis id={} — list and item caches evicted", id);
    }
}
