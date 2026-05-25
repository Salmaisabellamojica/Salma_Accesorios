package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.dto.SiteContentRequest;
import com.salma.salma_accesorios.model.SiteContent;
import com.salma.salma_accesorios.repository.SiteContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteContentService {

    private static final long SITE_CONTENT_ID = 1L;

    private final SiteContentRepository repository;

    public SiteContentService(SiteContentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SiteContent current() {
        return repository.findById(SITE_CONTENT_ID).orElseGet(() -> repository.save(new SiteContent()));
    }

    @Transactional
    public SiteContent update(SiteContentRequest request) {
        SiteContent content = current();
        content.setHeroEyebrow(request.getHeroEyebrow());
        content.setHeroTitle(request.getHeroTitle());
        content.setHeroCopy(request.getHeroCopy());
        content.setHeroButtonLabel(request.getHeroButtonLabel());
        content.setHeroImageUrl(blankToNull(request.getHeroImageUrl()));
        content.setFooterTagline(request.getFooterTagline());
        content.setFooterCategoriesTitle(request.getFooterCategoriesTitle());
        content.setFooterContactTitle(request.getFooterContactTitle());
        content.setWhatsappText(request.getWhatsappText());
        content.setWhatsappUrl(request.getWhatsappUrl());
        content.setInstagramText(request.getInstagramText());
        content.setInstagramUrl(request.getInstagramUrl());
        content.setFooterCopyright(request.getFooterCopyright());
        return repository.save(content);
    }

    public SiteContentRequest toRequest(SiteContent content) {
        SiteContentRequest request = new SiteContentRequest();
        request.setHeroEyebrow(content.getHeroEyebrow());
        request.setHeroTitle(content.getHeroTitle());
        request.setHeroCopy(content.getHeroCopy());
        request.setHeroButtonLabel(content.getHeroButtonLabel());
        request.setHeroImageUrl(content.getHeroImageUrl());
        request.setFooterTagline(content.getFooterTagline());
        request.setFooterCategoriesTitle(content.getFooterCategoriesTitle());
        request.setFooterContactTitle(content.getFooterContactTitle());
        request.setWhatsappText(content.getWhatsappText());
        request.setWhatsappUrl(content.getWhatsappUrl());
        request.setInstagramText(content.getInstagramText());
        request.setInstagramUrl(content.getInstagramUrl());
        request.setFooterCopyright(content.getFooterCopyright());
        return request;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
