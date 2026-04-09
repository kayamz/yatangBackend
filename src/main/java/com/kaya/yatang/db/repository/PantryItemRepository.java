package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.PantryItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUser_Id(Long userId);
}
