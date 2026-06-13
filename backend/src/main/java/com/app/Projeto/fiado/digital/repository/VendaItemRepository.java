package com.app.Projeto.fiado.digital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.Projeto.fiado.digital.model.VendaItem;

public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

    List<VendaItem> findByVendaId(Long vendaId);

}
