package com.onAcademy.tcc.service;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.onAcademy.tcc.config.TokenProvider;
import com.onAcademy.tcc.dto.TeacherDTO;
import com.onAcademy.tcc.model.ClassSt;
import com.onAcademy.tcc.model.Discipline;
import com.onAcademy.tcc.model.Teacher;
import com.onAcademy.tcc.repository.DisciplineRepo;
import com.onAcademy.tcc.repository.FeedbackByStudentRepo;
import com.onAcademy.tcc.repository.FeedbackByTeacherRepo;
import com.onAcademy.tcc.repository.FeedbackFormRepo;
import com.onAcademy.tcc.repository.ReminderRepo;
import com.onAcademy.tcc.repository.StudentRepo;
import com.onAcademy.tcc.repository.TeacherRepo;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * Serviço responsável pela gestão dos professores no sistema.
 * 
 * - Este serviço permite realizar operações como login, criação, atualização,
 * exclusão e busca de professores. - Também oferece funcionalidade para gerar
 * senhas aleatórias para os professores e enviar e-mails com as credenciais de
 * acesso.
 * 
 * @see com.onAcademy.tcc.model.Teacher
 * @see com.onAcademy.tcc.repository.TeacherRepo
 */
@Service
public class TeacherService {

    public static final String ENROLLMENT_PREFIX = "p";

    @Autowired
    private TeacherRepo teacherRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private StudentRepo studentRepo;
    
    @Autowired
    private DisciplineRepo disciplineRepo;
    
    @Autowired
    private FeedbackByStudentRepo feedbackByStudentRepo;

    @Autowired
    private FeedbackByTeacherRepo feedbackByTeacherRepo;

    @Autowired
    private FeedbackFormRepo feedbackFormRepo;

    @Autowired
    private ReminderRepo reminderRepo;
    
    @Autowired
    private ImageUploaderService imageUploaderService;

    /**
     * Gera uma senha aleatória para o professor, utilizando o nome e um número
     * aleatório.
     * 
     * @param length O comprimento da parte numérica da senha.
     * @param nome   O nome do professor, que será incluído na senha.
     * @return A senha gerada com números aleatórios e o nome do professor.
     */
    private String generateRandomPasswordWithName(int length, String nome) {
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(numbers.charAt(random.nextInt(numbers.length())));
        }

        // Adiciona o nome do professor ao final da senha
        String nomeFormatado = nome.replaceAll("\\s+", ""); // Remove espaços em branco do nome
        sb.append(nomeFormatado); // Adiciona o nome formatado à senha

        return sb.toString();
    }

    /**
     * Realiza o login do professor no sistema.
     * 
     * @param identifierCode O código de matrícula do professor.
     * @param password       A senha fornecida pelo professor.
     * @return Um token JWT gerado para o professor logado.
     * @throws RuntimeException Se a matrícula ou a senha estiverem incorretas.
     */
    public String loginTeacher(String identifierCode, String password) {
        Teacher teacher = teacherRepo.findByIdentifierCode(identifierCode)
                .filter(i -> passwordEncoder.matches(password, i.getPassword()))
                .orElseThrow(() -> new RuntimeException("Matricula ou senha incorretos"));
        return tokenProvider.generate(teacher.getId().toString(), List.of("teacher"));
    }
    
    @Transactional
    public Teacher criarTeacher(TeacherDTO teacherDTO) throws MessagingException {
        validarTeacherDTO(teacherDTO);
        
        Teacher teacher = new Teacher();
        List<Discipline> disciplines = disciplineRepo.findAllById(teacherDTO.getDisciplineId());
        if (disciplines.size() != teacherDTO.getDisciplineId().size()) {
            throw new IllegalArgumentException("Algumas disciplinas não foram encontradas");
        }

        // Verifica se há uma imagem em Base64 no DTO
        String imageUrl = null;
        if (teacherDTO.getImageUrl() != null && !teacherDTO.getImageUrl().isEmpty()) {
            imageUrl = imageUploaderService.uploadBase64Image(teacherDTO.getImageUrl());
        }

        teacher.setNomeDocente(teacherDTO.getNomeDocente());
        teacher.setDataNascimentoDocente(teacherDTO.getDataNascimentoDocente());
        teacher.setEmailDocente(teacherDTO.getEmailDocente());
        teacher.setTelefoneDocente(teacherDTO.getTelefoneDocente());
        
        String rawPassword = generateRandomPasswordWithName(6, teacherDTO.getNomeDocente());
        String encoded = passwordEncoder.encode(rawPassword);
        teacher.setPassword(encoded);
        
        if (imageUrl != null) {
            teacher.setImageUrl(imageUrl);
        }

        teacher.setDisciplines(disciplines);
        Teacher savedTeacher = teacherRepo.save(teacher);
        
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
            + "  <h1>Olá, " + savedTeacher.getNomeDocente() + "! 👋</h1>"
            + "  <div class='info-box'>"
            + "    <p style='text-align: center; font-size: 16px; margin-bottom: 10px;'>Seu cadastro foi realizado com sucesso!</p>"
            + "    <p style='text-align: center; font-size: 16px;'>Estamos muito felizes em tê-lo(a) como parte da nossa comunidade educacional.</p>"
            + "  </div>"
            + "  <div class='credentials'>"
            + "    <p style='font-size: 15px; margin: 10px 0;'><strong>🔑 Código de Matrícula:</strong> " + savedTeacher.getIdentifierCode() + "</p>"
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

        emailService.sendEmail(savedTeacher.getEmailDocente(), emailSubject, emailText);

        return savedTeacher;
    }

    private void validarTeacherDTO(TeacherDTO teacherDTO) {
        if (teacherDTO.getNomeDocente() == null || teacherDTO.getNomeDocente().isEmpty()) {
            throw new IllegalArgumentException("Por favor preencha com um nome.");
        }

        if (!teacherDTO.getNomeDocente().matches("[a-zA-ZáàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ\\s]+")) {
            throw new IllegalArgumentException("O nome deve conter apenas letras.");
        }

        if (teacherDTO.getNomeDocente().length() < 2 || teacherDTO.getNomeDocente().length() > 30) {
            throw new IllegalArgumentException("O nome deve ter entre 2 e 30 caracteres.");
        }

        if (teacherDTO.getDataNascimentoDocente() == null) {
            throw new IllegalArgumentException("Por favor preencha a data de nascimento.");
        }
        
        if (teacherDTO.getEmailDocente() == null || teacherDTO.getEmailDocente().isEmpty()) {
            throw new IllegalArgumentException("Por favor preencha o campo email.");
        }
        
        if (!teacherDTO.getEmailDocente().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("O email fornecido não tem formato válido.");
        }
        
        if (teacherRepo.existsByEmailDocente(teacherDTO.getEmailDocente())) {
            throw new IllegalArgumentException("Email já cadastrado.");
        } else if (studentRepo.existsByEmailAluno(teacherDTO.getEmailDocente())) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        if (teacherDTO.getTelefoneDocente() == null || !teacherDTO.getTelefoneDocente().matches("\\d{11}")) {
            throw new IllegalArgumentException("Telefone deve conter exatamente 11 dígitos numéricos.");
        }
        
        if (teacherRepo.existsByTelefoneDocente(teacherDTO.getTelefoneDocente())) {
            throw new IllegalArgumentException("Telefone já cadastrado.");
        }

        if (teacherDTO.getDisciplineId() == null || teacherDTO.getDisciplineId().isEmpty()) {
            throw new IllegalArgumentException("Por favor preencha com no mínimo uma disciplina.");
        }
    }
    

    /**
     * Busca todos os professores cadastrados no sistema.
     * 
     * @return Uma lista de todos os professores.
     */
    public List<Teacher> buscarTeachers() {
        return teacherRepo.findAll();
    }

    /**
     * Atualiza os dados de um professor existente no sistema.
     * 
     * - Gera uma nova senha, codifica e envia um e-mail notificando o professor
     * sobre a atualização.
     * 
     * @param id      O ID do professor a ser atualizado.
     * @param teacher O objeto `Teacher` contendo os novos dados do professor.
     * @return O professor atualizado e salvo no banco de dados.
     */
    @Transactional
    public Teacher atualizarTeacher(Long id, Teacher teacher) {
        Optional<Teacher> existingTeacher = teacherRepo.findById(id);
        if (existingTeacher.isPresent()) {
            Teacher atualizarTeacher = existingTeacher.get();
            
         // Verifica se há uma imagem em Base64 no DTO
            String imageUrl = null;
            if(teacher.getImageUrl() != null && !teacher.getImageUrl().isEmpty() && teacher.getImageUrl().matches("^http://.*")) {
            	imageUrl = teacher.getImageUrl();
            }else if (teacher.getImageUrl() != null && !teacher.getImageUrl().isEmpty()) {
                imageUrl = imageUploaderService.uploadBase64Image(teacher.getImageUrl());
            }
            atualizarTeacher.setNomeDocente(teacher.getNomeDocente());
            atualizarTeacher.setDataNascimentoDocente(teacher.getDataNascimentoDocente());
            atualizarTeacher.setEmailDocente(teacher.getEmailDocente());
            atualizarTeacher.setTelefoneDocente(teacher.getTelefoneDocente());
            
            if (imageUrl != null) {
                atualizarTeacher.setImageUrl(imageUrl);
            }

            
            String rawPassword = generateRandomPasswordWithName(6, atualizarTeacher.getNomeDocente());
            String encodedPassword = passwordEncoder.encode(rawPassword);
            atualizarTeacher.setPassword(encodedPassword);
            teacherRepo.save(atualizarTeacher);

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
                + "  <h1>Olá, " + atualizarTeacher.getNomeDocente() + "!</h1>"
                + "  <div class='alert-box'>"
                + "    <p style='text-align: center; font-size: 16px;'><span class='warning'>⚠️ ATENÇÃO:</span> Seus dados de acesso foram atualizados.</p>"
                + "  </div>"
                + "  <div class='credentials'>"
                + "    <p style='text-align: center; font-size: 16px; margin-bottom: 15px;'>Confira suas novas credenciais:</p>"
                + "    <p style='font-size: 15px; margin: 12px 0;'><strong>🔑 Código de Matrícula:</strong> " + atualizarTeacher.getIdentifierCode() + "</p>"
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
                emailService.sendEmail(atualizarTeacher.getEmailDocente(), emailSubject, emailText);
            } catch (MessagingException e) {
                throw new RuntimeException("Erro ao enviar email com os novos dados de acesso.", e);
            }

            return atualizarTeacher;
        }
        return null;
    }

    /**
     * Exclui um professor do sistema.
     * 
     * @param id O ID do professor a ser excluído.
     * @return O professor excluído, ou `null` caso não exista.
     */
    @Transactional
    public Teacher deletarTeacher(Long id) {
        // 1) Verifica se existe
        Teacher teacher = teacherRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado com ID: " + id));

        // === 1.1) Deleta tudo que é FK OneToMany (cascade manual) ===
        feedbackByStudentRepo.deleteByRecipientTeacherId(id);
        feedbackByTeacherRepo.deleteByCreatedById(id);
        feedbackFormRepo.deleteByCreatedById(id);
        reminderRepo.deleteByCreatedById(id);

        // (b) Tabela de junção entre Teacher e Discipline
        disciplineRepo.deleteJoinWithTeacher(id);

        // === 2) Apaga o Teacher ===
        teacherRepo.delete(teacher);

        return teacher;
    }


    /**
     * Busca um professor específico pelo ID.
     * 
     * @param id O ID do professor.
     * @return O professor encontrado, ou `null` caso não exista.
     */
    public Teacher buscarUnicoTeacher(Long id) {
        Optional<Teacher> existingTeacher = teacherRepo.findById(id);
        return existingTeacher.orElse(null);
    }
}