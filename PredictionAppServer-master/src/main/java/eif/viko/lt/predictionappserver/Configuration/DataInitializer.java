package eif.viko.lt.predictionappserver.Configuration;

import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Entities.Role;
import eif.viko.lt.predictionappserver.Repositories.ChatUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ChatUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ChatUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Sukurti pradinius vartotojus, jeigu jų nėra

        // Administratorius
        if (!userRepository.findByEmail("admin@example.com").isPresent()) {
            ChatUser admin = new ChatUser();
            admin.setUsername("Admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // Mokytojas
        if (!userRepository.findByEmail("teacher@example.com").isPresent()) {
            ChatUser teacher = new ChatUser();
            teacher.setUsername("Teacher");
            teacher.setEmail("teacher@example.com");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setEnabled(true);
            teacher.setRole(Role.TEACHER);
            userRepository.save(teacher);
        }

        // Studentas
        if (!userRepository.findByEmail("student@example.com").isPresent()) {
            ChatUser student = new ChatUser();
            student.setUsername("Student");
            student.setEmail("student@example.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setEnabled(true);
            student.setRole(Role.STUDENT);
            userRepository.save(student);
        }
    }
}