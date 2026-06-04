package com.lolcompanion.bg2ez.config.repository;

import com.lolcompanion.bg2ez.config.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {}