package com.salma.salma_accesorios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SiteContentRequest {

    @NotBlank
    @Size(max = 120)
    private String heroEyebrow;

    @NotBlank
    @Size(max = 120)
    private String heroTitle;

    @NotBlank
    @Size(max = 260)
    private String heroCopy;

    @NotBlank
    @Size(max = 80)
    private String heroButtonLabel;

    @Size(max = 500)
    private String heroImageUrl;

    @NotBlank
    @Size(max = 160)
    private String footerTagline;

    @NotBlank
    @Size(max = 80)
    private String footerCategoriesTitle;

    @NotBlank
    @Size(max = 80)
    private String footerContactTitle;

    @NotBlank
    @Size(max = 120)
    private String whatsappText;

    @NotBlank
    @Size(max = 500)
    private String whatsappUrl;

    @NotBlank
    @Size(max = 120)
    private String instagramText;

    @NotBlank
    @Size(max = 500)
    private String instagramUrl;

    @NotBlank
    @Size(max = 120)
    private String footerCopyright;
}
