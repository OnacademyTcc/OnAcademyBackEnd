package com.onAcademy.tcc.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.onAcademy.tcc.model.Reminder;
import com.onAcademy.tcc.model.Teacher;
import com.onAcademy.tcc.repository.ClassStRepo;
import com.onAcademy.tcc.repository.ReminderRepo;
import com.onAcademy.tcc.repository.TeacherRepo;

/**
 * Serviço responsável pela gestão dos lembretes (reminders) no sistema.
 * 
 * - Este serviço permite realizar operações como criação, busca e filtro de
 * lembretes. - É possível buscar todos os lembretes, ou filtrar lembretes por
 * turma (`ClassStId`) ou por professor (`TeacherId`). - A criação de um
 * lembrete é realizada diretamente utilizando o método `criarLembrete`.
 * 
 * @see com.onAcademy.tcc.model.Reminder
 * @see com.onAcademy.tcc.repository.ReminderRepo
 */

@Service
public class ReminderService {
	@Autowired
	private ReminderRepo reminderRepo;

	@Autowired
	private ClassStRepo classStRepo;

	@Autowired
	private TeacherRepo teacherRepo;

	/**
	 * Cria um novo lembrete.
	 * 
	 * - O método salva um novo lembrete no banco de dados.
	 * 
	 * @param lembrete O objeto `Reminder` contendo os dados do lembrete.
	 * @return O lembrete criado e salvo no banco de dados.
	 */
	public Reminder criarLembrete(Reminder lembrete) {
		Reminder criarLembrete = reminderRepo.save(lembrete);
		return criarLembrete;
	}

	/**
	 * Busca todos os lembretes cadastrados no banco de dados.
	 * 
	 * @return Uma lista contendo todos os lembretes registrados no sistema.
	 */
	public List<Reminder> buscarTodosLembretes() {
		List<Reminder> buscarLembretes = reminderRepo.findAll();
		return buscarLembretes;
	}

	/**
	 * Busca lembretes filtrando pelo ID da turma (ClassSt).
	 * 
	 * - O método retorna todos os lembretes associados à turma identificada pelo
	 * `classStId`.
	 * 
	 * @param classStId O ID da turma pelos quais os lembretes serão filtrados.
	 * @return Uma lista de lembretes relacionados à turma especificada.
	 */
	public List<Reminder> buscarLembretePorClassStId(Long classStId) {
		return reminderRepo.findByClassStId(classStId);
	}

	/**
	 * Busca lembretes filtrando pelo ID do professor que criou o lembrete.
	 * 
	 * - O método retorna todos os lembretes criados pelo professor identificado
	 * pelo `teacherId` e os lembretes criados pela instituição. - Caso o professor não seja encontrado no banco de dados, é
	 * retornada uma lista apenas com os lembretes de instituição.
	 * 
	 * @param teacherId O ID do professor que criou os lembretes.
	 * @return Uma lista de lembretes criados pelo professor especificado, ou uma
	 *         lista vazia caso o professor não seja encontrado.
	 */
	public List<Reminder> buscarLembretePorCreatedByOuInstitutions(Long teacherId) {
	    // Busca o professor pelo ID
	    Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

	    // Lista final para juntar resultados
	    List<Reminder> allReminders = new ArrayList<>();

	    // Se o professor existir, busca lembretes criados por ele
	    if (teacher != null) {
	        List<Reminder> remindersByTeacher = reminderRepo.findByCreatedBy(teacher);
	        allReminders.addAll(remindersByTeacher);
	    }

	    // Busca todos os lembretes criados por qualquer instituição (independente de ID)
	    List<Reminder> remindersByInstitution = reminderRepo.findByCreatedByInstitutionIsNotNull();
	    allReminders.addAll(remindersByInstitution);

	    return allReminders;
	}

}
