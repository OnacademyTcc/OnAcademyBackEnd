### Setup de ambiente:
- [Azure SQL](https://azure.microsoft.com/pt-br/free/sql-database/search/?ef_id=_k_Cj0KCQjwotDBBhCQARIsAG5pinPl_3spTLDl-EmaYRyhH0uJ1VzPHvoJbkzP_BvWI14rXi0JPkJW1hEaAjt-EALw_wcB_k_&OCID=AIDcmmzmnb0182_SEM__k_Cj0KCQjwotDBBhCQARIsAG5pinPl_3spTLDl-EmaYRyhH0uJ1VzPHvoJbkzP_BvWI14rXi0JPkJW1hEaAjt-EALw_wcB_k_&gad_source=1&gad_campaignid=1635077466&gbraid=0AAAAADcJh_vx6Btl9zUo7RlHHvXEZR0-y&gclid=Cj0KCQjwotDBBhCQARIsAG5pinPl_3spTLDl-EmaYRyhH0uJ1VzPHvoJbkzP_BvWI14rXi0JPkJW1hEaAjt-EALw_wcB)
- [Eclipse](https://eclipseide.org/)

### Como rodar na minha máquina?
1. Clone o projeto: `https://github.com/OnacademyTcc/OnAcademyBackEnd`

2. Configure o Run Configurations Enviroments:
```
  cloudinary.api_key: 828664138232486
  cloudinary.api_secret: QlRCJ7Hnvus2jQMpsKi62SrYUwM
  cloudinary.cloud_name: dnqcwflgu
  email.onacademy: onacademy.tcc@gmail.com
  password.onacademy: dtua qogo snlg dxlx

```
3. Inicie a aplicação:
   
4. Pronto 🎉
## OnAcademy
### Estrutura do Projeto

`tcc (in AzureONA) [AzureONA master]`:
  - `tcc/src`:
    - `tcc/src/main`:
      - `tcc/src/main/java`:
        - `tcc/src/main/java/com.onAcademy.tcc`:
          - `tcc/src/main/java/com.onAcademy.tcc/TccApplication.java`: Classe principal que inicia a aplicação Spring Boot.

        - `tcc/src/main/java/com.config.copy`:
          - `tcc/src/main/java/com.config.copy/CloudinaryConfig.java`: Configuração para integração com o serviço Cloudinary (upload de imagens).
          - `tcc/src/main/java/com.config.copy/CorsConfig.java`: Configurações CORS para permitir requisições entre origens diferentes.
          - `tcc/src/main/java/com.config.copy/SecurityConfig.java`: Configurações de segurança da aplicação (autenticação, autorização).
          - `tcc/src/main/java/com.config.copy/SecurityFilter.java`: Filtro para validação de tokens JWT e autenticação.
          - `tcc/src/main/java/com.config.copy/SwaggerConfig.java`: Configuração do Swagger para documentação da API.
          - `tcc/src/main/java/com.config.copy/TokenProvider.java`: Geração e validação de tokens JWT.

        - `tcc/src/main/java/com.onAcademy.tcc.controller`:
          - `BoletimPDFController.java`: Controlador para geração de boletins em PDF.
          - `ClassStController.java`: CRUD para turmas/classes.
          - `DisciplineController.java`: CRUD para disciplinas.
          - `EmailController.java`: Envio de emails.
          - `EventController.java`: CRUD para eventos.
          - `FeedbackByStudentController.java`: Feedback dado por alunos.
          - `FeedbackByTeacherController.java`: Feedback dado por professores.
          - `FeedbackFormController.java`: CRUD para formulários de feedback.
          - `ImageUploaderController.java`: Upload de imagens.
          - `InstitutionController.java`: CRUD para instituições.
          - `NotesController.java`: CRUD para anotações.
          - `ReminderController.java`: CRUD para lembretes.
          - `StudentController.java`: CRUD para alunos.
          - `TeacherController.java`: CRUD para professores.

        - `tcc/src/main/java/com.onAcademy.tcc.dto`:
          - `ClassDTO.java`: DTO para turmas.
          - `FeedbackStudentDTO.java`: DTO para feedback de alunos.
          - `LoginDTO.java`: DTO para login genérico.
          - `LoginStudentDTO.java`: DTO para login de aluno.
          - `LoginTeacherDTO.java`: DTO para login de professor.
          - `NoteDTO.java`: DTO para anotações.
          - `StudentClassDTO.java`: DTO para relação aluno-turma.
          - `TeacherDTO.java`: DTO para professores.

        - `tcc/src/main/java/com.onAcademy.tcc.model`:
          - `ClassSt.java`: Entidade de turmas.
          - `Discipline.java`: Entidade de disciplinas.
          - `EmailRequest.java`: Modelo para requisição de email.
          - `Event.java`: Entidade de eventos.
          - `FeedBackByStudent.java`: Entidade de feedback de alunos.
          - `FeedbackByTeacher.java`: Entidade de feedback de professores.
          - `FeedbackForm.java`: Entidade de formulários de feedback.
          - `Institution.java`: Entidade de instituições.
          - `Note.java`: Entidade de anotações.
          - `Reminder.java`: Entidade de lembretes.
          - `Student.java`: Entidade de alunos.
          - `Teacher.java`: Entidade de professores.

        - `tcc/src/main/java/com.onAcademy.tcc.repositoryl`:
          - Arquivos de repositório (Repo) para cada entidade, responsáveis por operações de banco de dados.

        - `tcc/src/main/java/com.onAcademy.tcc.service`:
          - Serviços correspondentes a cada controlador, contendo a lógica de negócio.


# Conclusão:
A estrutura do projeto On Academy foi meticulosamente organizada seguindo as melhores práticas de desenvolvimento em Java.
