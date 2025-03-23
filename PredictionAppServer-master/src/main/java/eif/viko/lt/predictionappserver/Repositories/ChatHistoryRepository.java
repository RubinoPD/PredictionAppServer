package eif.viko.lt.predictionappserver.Repositories;


import eif.viko.lt.predictionappserver.Entities.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;


@RepositoryRestResource(path = "chats")
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    @Override
    @RestResource(exported = false)
    void deleteById(Long id); // Disables DELETE /myEntities/{id}


//    @Query("SELECT c FROM ChatHistory c WHERE c.chatId = :chatId")
//    List<ChatHistory> findByChatId(@Param("chatId") Long chatId);

}
