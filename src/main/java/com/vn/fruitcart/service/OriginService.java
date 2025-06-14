package com.vn.fruitcart.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.dto.request.OriginReq;
import com.vn.fruitcart.entity.dto.request.category.OriginSearchCriteria;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.OriginRepository;
import com.vn.fruitcart.service.specification.OriginSpecification;
import com.vn.fruitcart.util.FruitCartUtils;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OriginService {
    private final OriginRepository originRepository;

    public List<Origin> getAllOrigins() {
        return originRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Page<Origin> getAllOriginsWithFiltersAndPagination(String nameKeyword, Boolean status, int page,
            int size, String sortField, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

        Specification<Origin> spec = Specification.where(null);

        if (nameKeyword != null && !nameKeyword.isEmpty()) {
            spec = spec.and(OriginSpecification.hasName(nameKeyword));
        }

        if (status != null) {
            spec = spec.and(OriginSpecification.hasStatus(status));
        }

        return originRepository.findAll(spec, pageable);
    }

    @Transactional
    public Origin createOrigin(OriginReq cReq) {
        if (originRepository.findByName(cReq.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên nguồn gốc đã tồn tại.");
        }

        Origin origin = new Origin();
        origin.setName(cReq.getName());
        origin.setSlug(FruitCartUtils.toSlug(cReq.getName()));
        origin.setDescription(cReq.getDescription());
        origin.setStatus(cReq.getStatus());

        Origin savedOrigin = originRepository.save(origin);
        return savedOrigin;
    }

    public Origin getOriginById(Long id) {
        return originRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nguồn gốc với id: " + id));
    }

    public Origin updateOrigin(Long id, OriginReq req) {
        Origin existingOrigin = getOriginById(id);

        if (!existingOrigin.getName().equalsIgnoreCase(req.getName())
                && originRepository.existsByName(req.getName())) {
            throw new DuplicateNameException("Tên nguồn gốc '" + req.getName() + "' đã tồn tại.");
        }

        existingOrigin.setName(req.getName());
        existingOrigin.setDescription(req.getDescription());
        existingOrigin.setStatus(req.getStatus());
        existingOrigin.setSlug(FruitCartUtils.toSlug(req.getName()));
        return originRepository.save(existingOrigin);
    }

    public void deleteOrigin(Long id) {
        if (!originRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nguồn gốc với ID: " + id + " để xóa.");
        }
        originRepository.deleteById(id);
    }

    public List<Origin> findAllActiveOrigins() {
        Specification<Origin> spec = OriginSpecification.hasStatus(true);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return originRepository.findAll(spec, sort);
    }

    public Page<Origin> findOriginsByCriteria(OriginSearchCriteria criteria, Pageable pageable) {
        Specification<Origin> spec = Specification.where(null);

        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            spec = spec.and(OriginSpecification.hasName(criteria.getKeyword()));
        }

        if (criteria.getStatus() != null) {
            spec = spec.and(OriginSpecification.hasStatus(criteria.getStatus()));
        }

        return originRepository.findAll(spec, pageable);
    }
}
