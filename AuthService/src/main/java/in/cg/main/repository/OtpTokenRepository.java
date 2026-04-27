package in.cg.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.OtpToken;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    List<OtpToken> findByEmailAndPurposeAndConsumedAtIsNull(String email, String purpose);

    Optional<OtpToken> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
