package app.registrationSystem.services;

import app.registrationSystem.dto.request.DoctorRegistrationRequest;
import app.registrationSystem.dto.response.Response;
import app.registrationSystem.dto.request.VisitRequest;
import app.registrationSystem.jpa.entities.*;
import app.registrationSystem.jpa.repositories.DoctorRepository;
import app.registrationSystem.security.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserService userService;
    private final SpecializationService specializationService;
    private final IllnessService illnessService;
    private final MailSenderService mailSenderService;

    /**
     * Retrieves doctors by their ID
     * @param id ID of the doctor
     * @return Doctor object if successful
     */
    public Optional<Doctor> getById(Long id) {
        return doctorRepository.findById(id);
    }

    /**
     * Removes the doctor's account
     * @param id ID of the doctor to have the account removed
     * @return response with status of the performed action
     */
    @Transactional
    public Response removeDoctor(Long id) {
        Optional<Doctor> doctor = getById(id);

        if (doctor.isEmpty()) {
            return new Response(false, HttpStatus.NOT_FOUND, "User of the provided username not found");
        }

        userService.removeUser(doctor.get().getUser());
        doctorRepository.delete(doctor.get());

        return new Response(true, HttpStatus.OK, "Correctly removed the doctor account");
    }

    /**
     * Creates a new doctor account
     * @param doctorDTO DTO containing info about the doctor
     * @return response with status of the performed action
     */
    @Transactional
    public Response addDoctor(DoctorRegistrationRequest doctorDTO) {
        Optional<User> user = userService.createUser(doctorDTO, Role.DOCTOR);

        if (user.isEmpty()) {
            return new Response(false, HttpStatus.CONFLICT, "Provided username is already taken");
        }

        Doctor doctor = new Doctor();
        doctor.setUser(user.get());

        if (specializationService.getById(doctorDTO.getSpecialization()).isPresent()) {
            doctor.setSpecialization(specializationService.getById(doctorDTO.getSpecialization()).get());
        }

        return new Response(true, HttpStatus.OK, "Correctly created a patient account");
    }

    /**
     * Returns all the available doctors
     * @return list containing available doctors
     */
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    /**
     * Adds available visits for a doctor account
     * @param username username of the doctor account who has visits added
     * @param visits a list of visits to be added
     */
    @Transactional
    public void addVisits(String username, List<VisitRequest> visits) {
        Doctor doctor = getByUsername(username).get();

        List<AvailableVisit> availableVisits = new ArrayList<>(doctor.getAvailableVisits());

        for (VisitRequest visit : visits) {
            AvailableVisit availableVisit = new AvailableVisit();
            availableVisit.setDoctor(doctor);
            availableVisit.setDate(visit.getDate());
            availableVisit.setDuration(visit.getDuration());
            availableVisits.add(availableVisit);
        }

        doctor.setAvailableVisits(availableVisits);
        doctorRepository.save(doctor);
    }

    /**
     * Returns Doctor instance found by its username
     * @param username username of the account to be found
     * @return Doctor instance if user of such a username exists
     */
    public Optional<Doctor> getByUsername(String username) {
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        return doctorRepository.findByUser(user.get());
    }

    /**
     * Returns all the doctors that have the specified specialization
     * @param specialization specialization which determines which accounts should be found
     * @return list of Doctor instances if are present
     */
    public Optional<List<Doctor>> getBySpecialization(Specialization specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    /**
     * Returns a list containing scheduled visits for the account of the provided username
     * @param username username of the account to have scheduled visits checked
     * @return a list containing scheduled visits
     */
    public List<ScheduledVisit> checkScheduledVisits(String username) {
        return getByUsername(username).get().getScheduledVisits();
    }

    /**
     * Returns all the doctors that have the specialization named as the parameter
     * @param specializationName specialization name on which doctors are filetered
     * @return list of Doctor instances if are present
     */
    public Optional<List<Doctor>> getBySpecialization(String specializationName) {
        Optional<Specialization> specialization = specializationService.getByName(specializationName);

        if (specialization.isPresent()) {
            return getBySpecialization(specialization.get());
        }

       return Optional.empty();
    }

    /**
     * Returns all the doctors that have the specialization needed for the provided illness
     * @param illnessName illness name for which doctors should be found
     * @return list of Doctor instances if are present
     */
    public Optional<List<Doctor>> getByIllness(String illnessName) {
        Optional<Illness> illness = illnessService.getByName(illnessName);

        if (illness.isPresent()) {
            return getBySpecialization(illness.get().getSpecialization());
        }

        return Optional.empty();
    }
}
