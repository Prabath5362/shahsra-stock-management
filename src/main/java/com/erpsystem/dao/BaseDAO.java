package com.erpsystem.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Base Data Access Object interface providing common CRUD operations
 * 
 * @param <T> The entity type
 * @param <ID> The primary key type
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Insert a new entity
     * 
     * @param entity The entity to insert
     * @return The generated ID of the inserted entity
     * @throws SQLException if the operation fails
     */
    ID insert(T entity) throws SQLException;
    
    /**
     * Update an existing entity
     * 
     * @param entity The entity to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException if the operation fails
     */
    boolean update(T entity) throws SQLException;
    
    /**
     * Delete an entity by ID
     * 
     * @param id The ID of the entity to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if the operation fails
     */
    boolean delete(ID id) throws SQLException;
    
    /**
     * Find an entity by ID
     * 
     * @param id The ID of the entity to find
     * @return Optional containing the entity if found, empty otherwise
     * @throws SQLException if the operation fails
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Find all entities
     * 
     * @return List of all entities
     * @throws SQLException if the operation fails
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Count total number of entities
     * 
     * @return The total count
     * @throws SQLException if the operation fails
     */
    long count() throws SQLException;
    
    /**
     * Check if an entity exists by ID
     * 
     * @param id The ID to check
     * @return true if the entity exists, false otherwise
     * @throws SQLException if the operation fails
     */
    boolean exists(ID id) throws SQLException;
}