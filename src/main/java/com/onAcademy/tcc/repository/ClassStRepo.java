package com.onAcademy.tcc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onAcademy.tcc.model.ClassSt;

/**
 * Repositório para a entidade {@link ClassSt}.
 * Esta interface estende {@link JpaRepository}, fornecendo métodos CRUD (Create, Read, Update, Delete)
 * e operações de paginação e ordenação para a entidade {@link ClassSt}.
 *
 * @see JpaRepository
 * @see ClassSt
 */
public interface ClassStRepo extends JpaRepository<ClassSt, Long> {
    // Métodos personalizados podem ser adicionados aqui, se necessário.
	
	@Modifying
    @Query(value = "DELETE FROM classst_teacher WHERE teacher_id = :teacherId", nativeQuery = true)
    void deleteJoinWithTeacher(@Param("teacherId") Long teacherId);
}