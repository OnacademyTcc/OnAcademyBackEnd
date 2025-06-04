package com.onAcademy.tcc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.onAcademy.tcc.config.TokenProvider;
import com.onAcademy.tcc.dto.StudentClassDTO;
import com.onAcademy.tcc.model.ClassSt;
import com.onAcademy.tcc.model.Student;
import com.onAcademy.tcc.repository.ClassStRepo;
import com.onAcademy.tcc.repository.StudentRepo;
import com.onAcademy.tcc.repository.TeacherRepo;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

/**
 * Serviço responsável pela gestão dos estudantes no sistema.
 * 
 * - Este serviço permite realizar operações como login, criação, atualização,
 * exclusão e busca de estudantes. - Também oferece funcionalidade para gerar
 * senhas aleatórias para os estudantes e enviar emails com as credenciais de
 * acesso.
 * 
 * @see com.onAcademy.tcc.model.Student
 * @see com.onAcademy.tcc.repository.StudentRepo
 * @see com.onAcademy.tcc.repository.ClassStRepo
 * @see com.onAcademy.tcc.repository.TeacherRepo
 */

@Service
public class StudentService {

	@Autowired
	private EmailService emailService;

	@Autowired
	private StudentRepo studentRepo;

	@Autowired
	private ClassStRepo classStRepo;

	@Autowired
	private TeacherRepo teacherRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private ImageUploaderService imageUploaderService;

	/**
	 * Gera uma senha aleatória para o estudante, utilizando seu nome e um número
	 * aleatório.
	 * 
	 * @param length O comprimento da parte numérica da senha.
	 * @param nome   O nome do estudante, que será incluído na senha.
	 * @return A senha gerada com números aleatórios e o nome do estudante.
	 */
	private String generateRandomPasswordWithName(int length, String nome) {
		String numbers = "0123456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			sb.append(numbers.charAt(random.nextInt(numbers.length())));
		}

		String nomeFormatado = nome.replaceAll("\\s+", "");
		sb.append(nomeFormatado);

		return sb.toString();
	}

	/**
	 * Realiza o login do estudante no sistema.
	 * 
	 * @param identifierCode O código de matrícula do estudante.
	 * @param password       A senha fornecida pelo estudante.
	 * @return Um token JWT gerado para o estudante logado.
	 */
	public String loginStudent(String identifierCode, String password) {
		Student student = studentRepo.findByIdentifierCode(identifierCode)
				.filter(s -> passwordEncoder.matches(password, s.getPassword()))
				.orElseThrow(() -> new RuntimeException("Revise os campos!!"));
		return tokenProvider.generate(student.getId().toString(), List.of("student"));
	}

	/**
	 * Cria um novo estudante no sistema.
	 * 
	 * - Valida os dados fornecidos pelo usuário (nome, email, telefone, etc.). -
	 * Gera uma senha aleatória e envia um e-mail de boas-vindas com as credenciais.
	 * 
	 * @param studentDTO O objeto `StudentClassDTO` contendo os dados do estudante e
	 *                   da turma.
	 * @return O estudante criado e salvo no banco de dados.
	 * @throws MessagingException Caso ocorra um erro ao enviar o e-mail.
	 */
	@Transactional
	public Student criarEstudante(StudentClassDTO studentDTO) throws MessagingException {
		ClassSt classSt = classStRepo.findById(studentDTO.getTurmaId())
				.orElseThrow(() -> new RuntimeException("Turma não encontrada"));
		validarStudent(studentDTO);
		// Verifica se há uma imagem em Base64 no DTO
		String imageUrl = null;
		if (studentDTO.getImageUrl() != null && !studentDTO.getImageUrl().isEmpty()) {
			imageUrl = imageUploaderService.uploadBase64Image(studentDTO.getImageUrl());
		}
		Student student = new Student();
		student.setNomeAluno(studentDTO.getNomeAluno());
		student.setDataNascimentoAluno(studentDTO.getDataNascimentoAluno());
		student.setEmailAluno(studentDTO.getEmailAluno());
		student.setTelefoneAluno(studentDTO.getTelefoneAluno());
		student.setImageUrl(studentDTO.getImageUrl());

		String rawPassword = Student.generateRandomPassword(studentDTO, classSt);
		String encodedPassword = passwordEncoder.encode(rawPassword);
		student.setPassword(encodedPassword);

		student.setTurmaId(classSt.getId());
		student.setImageUrl(studentDTO.getImageUrl());

		// Define a URL da imagem após o upload
		if (imageUrl != null) {
			student.setImageUrl(imageUrl);
		}
		Student savedStudent = studentRepo.save(student);

		 String emailSubject = "✨ Bem-vindo(a) à ON Academy - Cadastro Realizado!";

	        String emailText = "<!DOCTYPE html>"
	            + "<html xmlns='http://www.w3.org/1999/xhtml'>"
	            + "<head>"
	            + "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>"
	            + "<style>"
	            + "  body { font-family: 'Poppins', sans-serif; background-color: #f8fbff; margin: 0; padding: 20px; color: #333; }"
	            + "  .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 25px; padding: 30px; box-shadow: 0 5px 15px rgba(0, 87, 255, 0.1); }"
	            + "  .logo { font-family: 'Fredoka One', cursive; font-size: 28px; color: #0038a8; text-align: center; margin-bottom: 10px; }"
	            + "  h1 { color: #0057ff; text-align: center; font-size: 24px; margin: 20px 0; }"
	            + "  .info-box { background: linear-gradient(135deg, #e6f2ff, #d0e4ff); padding: 20px; border-radius: 15px; margin: 20px 0; border: 2px dashed #a8d0ff; }"
	            + "  .credentials { background-color: #f5faff; padding: 15px; border-radius: 10px; margin: 15px 0; }"
	            + "  .login-btn { display: inline-block; background: linear-gradient(135deg, #0057ff, #0085ff); color: white; padding: 12px 25px; text-decoration: none; border-radius: 30px; font-weight: bold; margin: 15px 0; }"
	            + "  .footer { margin-top: 30px; color: #6699cc; font-size: 14px; text-align: center; }"
	            + "  .divider { border-top: 2px dashed #a8d0ff; margin: 25px 0; }"
	            + "</style>"
	            + "</head>"
	            + "<body>"
	            + "<div class='container'>"
	            + "  <div class='logo'>ON Academy</div>"
	            + "  <h1>Olá, " + savedStudent.getNomeAluno() + "! 👋</h1>"
	            + "  <div class='info-box'>"
	            + "    <p style='text-align: center; font-size: 16px; margin-bottom: 10px;'>Seu cadastro foi realizado com sucesso!</p>"
	            + "    <p style='text-align: center; font-size: 16px;'>Estamos muito felizes em tê-lo(a) como parte da nossa comunidade educacional.</p>"
	            + "  </div>"
	            + "  <div class='credentials'>"
	            + "    <p style='font-size: 15px; margin: 10px 0;'><strong>🔑 Código de Matrícula:</strong> " + savedStudent.getIdentifierCode() + "</p>"
	            + "    <p style='font-size: 15px; margin: 10px 0;'><strong>🔒 Senha temporária:</strong> " + rawPassword + "</p>"
	            + "  </div>"
	            + "  <div style='text-align: center;'>"
	            + "    <a href='https://www.onacademy.com.br/' class='login-btn'>ACESSAR PLATAFORMA</a>"
	            + "  </div>"
	            + "  <div class='divider'></div>"
	            + "  <p style='font-size: 14px;'>Se tiver qualquer dúvida ou precisar de ajuda, entre em contato conosco:</p>"
	            + "  <p style='font-size: 14px;'>📧 onacademy.tcc@gmail.com</p>"
	            + "  <div class='footer'>"
	            + "    <p>Equipe ON Academy</p>"
	            + "    <p style='font-size: 12px; color: #999;'>Este é um e-mail automático, por favor não responda.</p>"
	            + "  </div>"
	            + "</div>"
	            + "</body>"
	            + "</html>";

		emailService.sendEmail(savedStudent.getEmailAluno(), emailSubject, emailText);

		return savedStudent;
	}
	@Transactional
	public List<Student> criarMultiplosEstudantes(List<StudentClassDTO> studentsDTO) {
	    List<Student> estudantesCriados = new ArrayList<>();
	    List<String> erros = new ArrayList<>();

	    for (StudentClassDTO dto : studentsDTO) {
	        try {
	            // Validação básica antes de tentar criar
	            if (dto.getNomeAluno() == null || dto.getNomeAluno().isEmpty()) {
	                erros.add("Estudante sem nome: " + dto);
	                continue;
	            }

	            Student estudante = criarEstudante(dto);
	            estudantesCriados.add(estudante);
	        } catch (Exception e) {
	            erros.add("Erro ao criar estudante " + dto.getNomeAluno() + ": " + e.getMessage());
	        }
	    }

	    if (!erros.isEmpty()) {
	        throw new RuntimeException("Erros ao criar estudantes: " + String.join("; ", erros));
	    }

	    return estudantesCriados;
	}

	// Validação de dados do estudante
	public void validarStudent(StudentClassDTO studentDTO) {
		if (studentDTO.getNomeAluno().isEmpty()) {
			throw new IllegalArgumentException("Por favor preencha com um nome.");
		}
		if (!studentDTO.getNomeAluno().matches("[a-zA-ZáàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ\\s]+")) {
			throw new IllegalArgumentException("O nome deve conter apenas letras.");
		}

		if (studentDTO.getNomeAluno().length() < 2 || studentDTO.getNomeAluno().length() > 30) {
			throw new IllegalArgumentException("O nome deve ter entre 2 e 100 caracteres.");
		}

		if (studentDTO.getDataNascimentoAluno() == null) {
			throw new IllegalArgumentException("Por favor preencha a data de nascimento.");
		}

		if (studentDTO.getEmailAluno().isEmpty()) {
			throw new IllegalArgumentException("Por favor preencha o campo email.");
		}
		if (!studentDTO.getEmailAluno().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			throw new IllegalArgumentException("O email fornecido não tem formato válido.");
		}
		if (studentRepo.existsByEmailAluno(studentDTO.getEmailAluno())) {
			throw new IllegalArgumentException("Email já cadastrado.");

		} else if (teacherRepo.existsByEmailDocente(studentDTO.getEmailAluno())) {
			throw new IllegalArgumentException("Email já cadastrado.");
		}

		if (studentRepo.existsByTelefoneAluno(studentDTO.getTelefoneAluno())) {
			throw new IllegalArgumentException("Telefone já cadastrado.");
		}

		if (!studentDTO.getTelefoneAluno().matches("[0-9]+")) {
			throw new IllegalArgumentException("Telefone deve conter somente números.");
		}

		if (studentDTO.getTelefoneAluno().length() != 11) {
			throw new IllegalArgumentException("Telefone deve ter 11 dígitos.");
		}

		if (studentDTO.getTurmaId() == null) {
			throw new IllegalArgumentException("Por favor preencha o campo de turma.");
		}

	}

	/**
	 * Busca todos os estudantes cadastrados no sistema.
	 * 
	 * @return Uma lista de todos os estudantes.
	 */
	public List<Student> buscarTodosEstudantes() {
		return studentRepo.findAll();
	}

	/**
	 * Atualiza os dados de um estudante existente no sistema.
	 * 
	 * - Gera uma nova senha e envia um e-mail notificando o estudante sobre a
	 * atualização.
	 * 
	 * @param id      O ID do estudante a ser atualizado.
	 * @param student O objeto `Student` contendo os novos dados do estudante.
	 * @return O estudante atualizado e salvo no banco de dados.
	 */
	public Student atualizarEstudante(Long id, Student student) {
	    Optional<Student> existStudentOpt = studentRepo.findById(id);
	    if (existStudentOpt.isPresent()) {
	        Student existStudent = existStudentOpt.get();
	        
	        // Validate the updated student data
	        validarAtualizacaoStudent(student, existStudent.getId());

	        // Verifica se há uma imagem em Base64 no DTO
	        String imageUrl = null;
            if(student.getImageUrl() != null && !student.getImageUrl().isEmpty() && student.getImageUrl().matches("^http://.*")) {
            	imageUrl = student.getImageUrl();
            }else if (student.getImageUrl() != null && !student.getImageUrl().isEmpty()) {
                imageUrl = imageUploaderService.uploadBase64Image(student.getImageUrl());
            }
	        existStudent.setNomeAluno(student.getNomeAluno());
	        existStudent.setEmailAluno(student.getEmailAluno());
	        existStudent.setDataNascimentoAluno(student.getDataNascimentoAluno());
	        existStudent.setTelefoneAluno(student.getTelefoneAluno());
	        
	        if (imageUrl != null) {
	        	existStudent.setImageUrl(imageUrl);
            }
	       
	        String rawPassword = generateRandomPasswordWithName(6, existStudent.getNomeAluno());
	        String encodedPassword = passwordEncoder.encode(rawPassword);
	        existStudent.setPassword(encodedPassword);

	        String emailSubject = "🔒 Atualização Confirmada - Seus Dados Foram Atualizados | ON Academy";

	        String emailText = "<!DOCTYPE html>"
	            + "<html xmlns='http://www.w3.org/1999/xhtml'>"
	            + "<head>"
	            + "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>"
	            + "<style>"
	            + "  body { font-family: 'Poppins', sans-serif; background-color: #f8fbff; margin: 0; padding: 20px; color: #333; }"
	            + "  .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 25px; padding: 30px; box-shadow: 0 5px 15px rgba(0, 87, 255, 0.1); }"
	            + "  .logo { font-family: 'Fredoka One', cursive; font-size: 28px; color: #0038a8; text-align: center; margin-bottom: 10px; }"
	            + "  h1 { color: #0057ff; text-align: center; font-size: 24px; margin: 20px 0; }"
	            + "  .alert-box { background: #fff8e6; padding: 20px; border-radius: 15px; margin: 20px 0; border-left: 5px solid #ffc107; }"
	            + "  .credentials { background: linear-gradient(135deg, #e6f2ff, #d0e4ff); padding: 20px; border-radius: 15px; margin: 20px 0; border: 2px dashed #a8d0ff; }"
	            + "  .login-btn { display: inline-block; background: linear-gradient(135deg, #0057ff, #0085ff); color: white; padding: 12px 25px; text-decoration: none; border-radius: 30px; font-weight: bold; margin: 15px 0; }"
	            + "  .footer { margin-top: 30px; color: #6699cc; font-size: 14px; text-align: center; }"
	            + "  .divider { border-top: 2px dashed #a8d0ff; margin: 25px 0; }"
	            + "  .warning { color: #d32f2f; font-weight: bold; }"
	            + "</style>"
	            + "</head>"
	            + "<body>"
	            + "<div class='container'>"
	            + "  <div class='logo'>ON Academy</div>"
	            + "  <h1>Olá, " + existStudent.getNomeAluno() + "!</h1>"
	            + "  <div class='alert-box'>"
	            + "    <p style='text-align: center; font-size: 16px;'><span class='warning'>⚠️ ATENÇÃO:</span> Seus dados de acesso foram atualizados.</p>"
	            + "  </div>"
	            + "  <div class='credentials'>"
	            + "    <p style='text-align: center; font-size: 16px; margin-bottom: 15px;'>Confira suas novas credenciais:</p>"
	            + "    <p style='font-size: 15px; margin: 12px 0;'><strong>🔑 Código de Matrícula:</strong> " + existStudent.getIdentifierCode() + "</p>"
	            + "    <p style='font-size: 15px; margin: 12px 0;'><strong>🔒 Nova Senha:</strong> " + rawPassword + "</p>"
	            + "  </div>"
	            + "  <div style='text-align: center;'>"
	            + "    <a href='https://suaurl.onacademy.com.br/login' class='login-btn'>ACESSAR MINHA CONTA</a>"
	            + "  </div>"
	            + "  <p style='font-size: 15px; text-align: center;'>Por favor, altere sua senha no primeiro acesso.</p>"
	            + "  <div class='divider'></div>"
	            + "  <p style='font-size: 15px;'><span class='warning'>Importante:</span> Caso não tenha solicitado esta alteração, entre em contato imediatamente com nosso suporte.</p>"
	            + "  <p style='font-size: 14px;'>📧 suporte@onacademy.com.br<br>📞 (XX) XXXX-XXXX</p>"
	            + "  <div class='footer'>"
	            + "    <p>Atenciosamente,<br><strong>Equipe ON Academy</strong></p>"
	            + "    <p style='font-size: 12px; color: #999;'>Este é um e-mail automático, por favor não responda.</p>"
	            + "  </div>"
	            + "</div>"
	            + "</body>"
	            + "</html>";
	        try {
	            emailService.sendEmail(existStudent.getEmailAluno(), emailSubject, emailText);
	        } catch (MessagingException e) {
	            throw new RuntimeException("Erro ao enviar email com os novos dados de acesso.", e);
	        }

	        return studentRepo.save(existStudent);
	    }
	    return null;
	}

	
	private void validarAtualizacaoStudent(Student student, Long studentId) {
	    if (student.getNomeAluno().isEmpty()) {
	        throw new IllegalArgumentException("Por favor preencha com um nome.");
	    }
	    if (!student.getNomeAluno().matches("[a-zA-ZáàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ\\s]+")) {
	        throw new IllegalArgumentException("O nome deve conter apenas letras.");
	    }

	    if (student.getNomeAluno().length() < 2 || student.getNomeAluno().length() > 30) {
	        throw new IllegalArgumentException("O nome deve ter entre 2 e 100 caracteres.");
	    }

	    if (student.getDataNascimentoAluno() == null) {
	        throw new IllegalArgumentException("Por favor preencha a data de nascimento.");
	    }

	    if (student.getEmailAluno().isEmpty()) {
	        throw new IllegalArgumentException("Por favor preencha o campo email.");
	    }
	    if (!student.getEmailAluno().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
	        throw new IllegalArgumentException("O email fornecido não tem formato válido.");
	    }
	    
	    
	    Optional<Student> studentWithEmail = studentRepo.findByEmailAluno(student.getEmailAluno());
	    if (studentWithEmail.isPresent() && !studentWithEmail.get().getId().equals(studentId)) {
	        throw new IllegalArgumentException("Email já cadastrado por outro estudante.");
	    }
	    

	    
	    Optional<Student> studentWithPhone = studentRepo.findByTelefoneAluno(student.getTelefoneAluno());
	    if (studentWithPhone.isPresent() && !studentWithPhone.get().getId().equals(studentId)) {
	        throw new IllegalArgumentException("Telefone já cadastrado por outro estudante.");
	    }

	    if (!student.getTelefoneAluno().matches("[0-9]+")) {
	        throw new IllegalArgumentException("Telefone deve conter somente números.");
	    }

	    if (student.getTelefoneAluno().length() != 11) {
	        throw new IllegalArgumentException("Telefone deve ter 11 dígitos.");
	    }
	}
	

	/**
	 * Busca um estudante específico pelo ID.
	 * 
	 * @param id O ID do estudante.
	 * @return O estudante encontrado, ou `null` caso não exista.
	 */
	public Student buscarEstudanteUnico(Long id) {
		Optional<Student> existStudent = studentRepo.findById(id);
		return existStudent.orElse(null);
	}

	/**
	 * Exclui um estudante do sistema.
	 * 
	 * @param id O ID do estudante a ser excluído.
	 * @return O estudante excluído, ou `null` caso não exista.
	 */
	public Student deletarEstudante(Long id) {
		Optional<Student> existStudent = studentRepo.findById(id);
		if (existStudent.isPresent()) {
			Student deletarEstudante = existStudent.get();
			studentRepo.delete(deletarEstudante);
			return deletarEstudante;
		}
		return null;
	}

}
