package eif.viko.lt.predictionappserver.Repositories;

import eif.viko.lt.predictionappserver.Entities.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByEmail(String email);
}
