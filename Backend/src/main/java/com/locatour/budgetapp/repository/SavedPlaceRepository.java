package com.locatour.budgetapp.repository;

import com.locatour.budgetapp.model.SavedPlace;
import com.locatour.budgetapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findByUser(User user);
    void deleteByIdAndUser(Long id, User user);
} 