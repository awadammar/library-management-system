package cc.maids.library.management.service;

import cc.maids.library.management.entity.Patron;
import cc.maids.library.management.exception.DuplicateEntityException;
import cc.maids.library.management.repository.PatronRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "patrons")
@AllArgsConstructor
public class PatronService {
    private PatronRepository patronRepository;

    @Cacheable("patrons")
    public List<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    @Cacheable(value = "patrons", key = "#id")
    public Patron getPatronById(Long id) {
        return patronRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patron not found with id: " + id));
    }

    public Patron addPatron(Patron patron) {
        // Validate if patron with the same contact information already exists
        if (patronRepository.existsByContactInformation(patron.getContactInformation())) {
            throw new DuplicateEntityException("Patron with contact information " + patron.getContactInformation() + " already exists.");
        }
        return patronRepository.save(patron);
    }

    @CachePut(key = "#id")
    public Patron updatePatron(Long id, Patron updatedPatron) {
        Patron existingPatron = getPatronById(id);

        // Validate if the updated contact information already exists for another patron
        if (!existingPatron.getContactInformation().equals(updatedPatron.getContactInformation()) &&
                patronRepository.existsByContactInformation(updatedPatron.getContactInformation())) {
            throw new DuplicateEntityException("Patron with contact information " + updatedPatron.getContactInformation() + " already exists.");
        }

        // Update patron attributes
        existingPatron.setName(updatedPatron.getName());
        existingPatron.setContactInformation(updatedPatron.getContactInformation());

        return patronRepository.save(existingPatron);
    }

    @CacheEvict(key = "#id")
    public void deletePatron(Long id) {
        Patron existingPatron = getPatronById(id);
        patronRepository.delete(existingPatron);
    }
}
