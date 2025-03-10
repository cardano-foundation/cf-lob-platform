package org.cardanofoundation.lob.app.organisation.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.organisation.domain.request.ReferenceCodeUpdate;
import org.cardanofoundation.lob.app.organisation.domain.view.ReferenceCodeView;
import org.cardanofoundation.lob.app.organisation.repository.ReferenceCodeRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReferenceCodeService {

    private final ReferenceCodeRepository referenceCodeRepository;

    public List<ReferenceCodeView> getAllReferenceCodes(String orgId) {
        return referenceCodeRepository.findAllByOrgId(orgId).stream()
                .map(ReferenceCodeView::fromEntity)
                .toList();
    }

    public Optional<ReferenceCodeView> getReferenceCode(String orgId, String referenceCode) {
        return referenceCodeRepository.findByOrgIdAndReferenceCode(orgId, referenceCode)
                .map(ReferenceCodeView::fromEntity);
    }

    public ReferenceCodeView upsertReferenceCode(String orgId, ReferenceCodeUpdate referenceCodeUpdate) {
        return ReferenceCodeView.fromEntity(referenceCodeRepository.save(referenceCodeUpdate.toEntity(orgId)));
    }
}
