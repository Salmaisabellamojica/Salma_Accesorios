package com.salma.salma_accesorios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "site_content")
public class SiteContent {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 120)
    private String heroEyebrow = "ORIGINAL DESIGNS";

    @Column(nullable = false, length = 120)
    private String heroTitle = "Nueva coleccion 2026";

    @Column(nullable = false, length = 260)
    private String heroCopy = "Joyeria artesanal, delicada y luminosa para vestir momentos cotidianos con encanto vintage.";

    @Column(nullable = false, length = 80)
    private String heroButtonLabel = "Explorar coleccion";

    @Column(length = 500)
    private String heroImageUrl;

    @Column(nullable = false, length = 160)
    private String footerTagline = "original designs";

    @Column(nullable = false, length = 80, columnDefinition = "varchar(80) default 'Categorias'")
    private String footerCategoriesTitle = "Categorias";

    @Column(nullable = false, length = 80, columnDefinition = "varchar(80) default 'Contacto'")
    private String footerContactTitle = "Contacto";

    @Column(nullable = false, length = 120, columnDefinition = "varchar(120) default 'WhatsApp +57 3212292317'")
    private String whatsappText = "WhatsApp +57 3212292317";

    @Column(nullable = false, length = 500, columnDefinition = "varchar(500) default 'https://wa.me/573212292317'")
    private String whatsappUrl = "https://wa.me/573212292317";

    @Column(nullable = false, length = 120, columnDefinition = "varchar(120) default 'Instagram'")
    private String instagramText = "Instagram";

    @Column(nullable = false, length = 500, columnDefinition = "varchar(500) default 'https://www.instagram.com/salmaaccesoriosss/?hl=es'")
    private String instagramUrl = "https://www.instagram.com/salmaaccesoriosss/?hl=es";

    @Column(nullable = false, length = 120, columnDefinition = "varchar(120) default '2026 Salma Accesorios'")
    private String footerCopyright = "2026 Salma Accesorios";
}
