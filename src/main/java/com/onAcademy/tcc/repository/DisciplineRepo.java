package com.onAcademy.tcc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onAcademy.tcc.model.Discipline;

/**
 * Repositório para a entidade "Discipline".
 * 
 * - Estende a interface JpaRepository, fornecendo métodos básicos de
 * persistência (salvar, buscar, atualizar, excluir) para a entidade
 * `Discipline`. - A interface `DisciplineRepo` não requer implementação
 * explícita, pois o Spring Data JPA fornece a implementação automaticamente
 * durante o tempo de execução.
 * 
 * A interface oferece os seguintes recursos: - **Crud básico**: Métodos como
 * `save()`, `findById()`, `findAll()`, `deleteById()` estão disponíveis por
 * herança da `JpaRepository`.
 * 
 * A principal finalidade desta interface é permitir o acesso à tabela que
 * armazena os dados das disciplinas, permitindo a execução de operações de CRUD
 * (Create, Read, Update, Delete) de forma simplificada.
 * 
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.onAcademy.tcc.model.Discipline
 */
public interface DisciplineRepo extends JpaRepository<Discipline, Long> {
	
	@Modifying
    @Query(value = "DELETE FROM teacher_discipline WHERE teacher_id = :teacherId", nativeQuery = true)
    void deleteJoinWithTeacher(@Param("teacherId") Long teacherId);

}
