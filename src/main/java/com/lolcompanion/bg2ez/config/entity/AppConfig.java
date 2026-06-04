package com.lolcompanion.bg2ez.config.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    private String key;
    private String value;
    private OffsetDateTime updatedAt;
}