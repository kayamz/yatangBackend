package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.ShoppingListItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findByUser_IdOrderByCreatedAtDesc(Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);
}
